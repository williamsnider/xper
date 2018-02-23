package org.xper.png.drawing.preview;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jzy3d.plot3d.rendering.image.GLImage;
import org.lwjgl.opengl.GL11;
import org.xper.drawing.Drawable;
import org.xper.png.drawing.stick.MStickSpec;
import org.xper.png.drawing.stimuli.PngObject;
import org.xper.png.drawing.stimuli.PngObjectSpec;
import org.xper.png.util.PngDbUtil;

public class PNGmaker {
	int height = 600;
	int width = 600;
	
	String imageFolderName = "";
	
	PngDbUtil dbUtil;
	
	public PNGmaker() {}
	
	public PNGmaker(PngDbUtil dbUtil) {
		this.dbUtil = dbUtil;
	}
	
	public void MakeFromIds(List<Long> stimObjIds) {
		List<Drawable> objs = spec2obj(id2spec(stimObjIds));
		createAndSavePNGsfromObjs(objs,stimObjIds);
	}
	
	private Map<MStickSpec, PngObjectSpec> id2spec(List<Long> stimObjIds) {
		Map<MStickSpec, PngObjectSpec> specs = new HashMap<MStickSpec, PngObjectSpec>();
		
		for (Long id : stimObjIds) {
			String jspec_str = dbUtil.readStimSpec_java(id).getSpec();
			String stickspec_str = dbUtil.readStimSpec_stick(id).getSpec();
			MStickSpec stickspec = MStickSpec.fromXml(stickspec_str);
			PngObjectSpec jspec = PngObjectSpec.fromXml(jspec_str);
			jspec.setDoStickGen(false);
			specs.put(stickspec, jspec);
		}
		return specs;
	}
	
	private List<Drawable> spec2obj(Map<MStickSpec, PngObjectSpec> specs) {
		List<Drawable> objs = new ArrayList<Drawable>();
		
		for (Map.Entry<MStickSpec, PngObjectSpec> entry : specs.entrySet()) {
			MStickSpec stickspec = entry.getKey();
			PngObjectSpec jspec = entry.getValue();
			PngObject obj = new PngObject();
			obj.setSpec_stick(stickspec);
			obj.setSpec_java(jspec);
			
			objs.add(obj);
		}
		
		return objs;
	}

	public void createAndSavePNGsfromObjs(List<Drawable> objs,List<Long> stimObjIds) {
		DrawingManager testWindow = new DrawingManager(height,width);
		testWindow.setBackgroundColor(0.3f,0.3f,0.3f);
		testWindow.setPngMaker(this);
		testWindow.setImageFolderName(imageFolderName);
		System.out.println("creating and saving PNGs...");

		testWindow.setStimObjs(objs);
		testWindow.setStimObjIds(stimObjIds);
		
		testWindow.drawStimuli();
		testWindow.close();
		System.out.println("...done saving PNGs");
	}
	
	public void saveImage_db(long stimObjId, int height, int width) {
		byte[] data = screenShotBinary(width,height);  
		
		dbUtil.writeThumbnail(stimObjId,data);		
	}
	
	public void saveImage_file(long stimObjId, int height, int width,String imageFolderName) {
		byte[] data = screenShotBinary(width,height);  

		try {
			FileOutputStream fos = new FileOutputStream(imageFolderName + "/" + stimObjId + ".png");
		    fos.write(data);
		    fos.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private byte[] screenShotBinary(int width, int height) 
	{
		ByteBuffer framebytes = allocBytes(width * height * 3);

		int[] pixels = new int[width * height];
		int bindex;
		// grab a copy of the current frame contents as RGB (has to be UNSIGNED_BYTE or colors come out too dark)
		GL11.glReadPixels(0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, framebytes);
		// copy RGB data from ByteBuffer to integer array
		for (int i = 0; i < pixels.length; i++) {
			bindex = i * 3;
			pixels[i] =
					0xFF000000                                          // A
					| ((framebytes.get(bindex)   & 0x000000FF) << 16)   // R
					| ((framebytes.get(bindex+1) & 0x000000FF) <<  8)   // G
					| ((framebytes.get(bindex+2) & 0x000000FF) <<  0);  // B
		}
		// free up this memory
		framebytes = null;
		// flip the pixels vertically (opengl has 0,0 at lower left, java is upper left)
		pixels = GLImage.flipPixels(pixels, width, height);

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			image.setRGB(0, 0, width, height, pixels, 0, width);

			javax.imageio.ImageIO.write(image, "png", out);
			byte[] data = out.toByteArray();

			return data;
		}
		catch (Exception e) {
			System.out.println("screenShot(): exception " + e);
			return null;
		}
	}

	public static ByteBuffer allocBytes(int howmany) {
		final int SIZE_BYTE = 4;
		return ByteBuffer.allocateDirect(howmany * SIZE_BYTE).order(ByteOrder.nativeOrder());
	}
	
	public void setImageFolderName(String imageFolderName) {
		this.imageFolderName = imageFolderName;
	}
	
}

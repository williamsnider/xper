package org.xper.robot.drawing.stimuli;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.xper.drawing.Context;
import org.xper.drawing.Coordinates2D;
import org.xper.drawing.Drawable;
import org.xper.robot.drawing.preview.DrawingManager;
import org.xper.util.ThreadUtil;

public class Image implements Drawable {
	
	// private static final int BYTES_PER_PIXEL = 4;//3 for RGB, 4 for RGBA
	
	int NumFrames = 1; //26;
	ByteBuffer pixels;
	IntBuffer textureIds = BufferUtils.createIntBuffer(NumFrames);
	int imgWidth;
	int imgHeight;
	int textureIndex;
	
	boolean texturesLoaded = false;
	int frameNum = 0;
	String resourcePath = "/home/justin/jkcode/ConnorLab/Alice/images/"; 

	String ext = ".jpg"; 
	String baseFilename = "img";
	
	String imageName;
	String baseName;
	
	
	/**
	* @param args
	*/
	public static void main(String[] args) {
		
		if(true){
			testImage();
			return;
		}
	
	}
	
	
	public int loadTexture(String pathname) {
		textureIndex = 0;
	
		try {
			File imageFile = new File(pathname);
			BufferedImage img = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);   //
			img = ImageIO.read(imageFile);
			imgWidth = img.getWidth();
			imgHeight = img.getHeight();
			System.out.println("JK 651222 loaded image : " + imgWidth + ", " + imgHeight);
					
		    byte[] src = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();	
			bgr2rgb(src);
	
			pixels = (ByteBuffer)BufferUtils.createByteBuffer(src.length).put(src, 0x00000000, src.length).flip();
			
			return 0; 
	
		} catch(IOException e) {
			System.out.println("JK 92416 : " + pathname);
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	
	
	void bgr2rgb(byte[] target) {
		byte tmp;
	
		for(int i=0x00000000; i<target.length; i+=0x00000003) {
			tmp = target[i];
			target[i] = target[i+0x00000002];
			target[i+0x00000002] = tmp;
		}
	}
	
	
	public static void testImage(){
		//String resourcePath = "/home/justin/jkcode/ConnorLab/xper-png/images/"; 
		String resourcePath = "/home/oconnorlab/code/images/";
		String ext = ".png"; 
		String baseFilename = "img-";  //	
		String testImageName = resourcePath + baseFilename + ext;
		int numTrials = 14;    
		int offset = 0;

		DrawingManager testWindow = new DrawingManager(1200, 1920);
		List<Image> images = new ArrayList<Image>();
		
		for(int i = 1; i < numTrials; i++){
			Image img = new Image();	

			testImageName = resourcePath + baseFilename + Integer.toString(i + offset) + ext;
			img.loadTexture(testImageName);
			System.out.println("JK 272621 loading " + testImageName);
			images.add(img);

		}
		testWindow.setStimDurMs(2000);
		testWindow.setStimObjs(images);		// add objects to be drawn
		testWindow.drawStimuli();
		System.out.println("done " + testImageName);
	}

 

	@Override
	public void draw(Context context) {
		
//		Coordinates2D centermm = new Coordinates2D(0,0);
//
//
//		float width = (float) context.getRenderer().deg2mm((float)imgWidth); // texture.getImageWidth();
//		float height = (float) context.getRenderer().deg2mm((float)imgHeight); // texture.getImageHeight();		
//		
//		Coordinates2D pixels2D = context.getRenderer().pixel2mm(new Coordinates2D(imgWidth, imgHeight));
//		
//		float width = imgWidth; // texture.getImageWidth();
//		float height = imgHeight; // texture.getImageHeight();	
		float width = (float) context.getRenderer().getVpWidthmm();
		float height = (float) context.getRenderer().getVpHeightmm();
		System.out.println("AC WIDTH: " + width);
		System.out.println("AC HEIGHT: " + height);
		float yOffset = -height / 2.0f;
		float xOffset = -width / 2.0f; 
				
		
		System.out.printf("JK 254 draw() xOffset = %f, yOffset = %f\n", xOffset, yOffset );
	
		GL11.glEnable(GL11.GL_TEXTURE_2D);  	
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIds.get(textureIndex));
		// from http://wiki.lwjgl.org/index.php?title=Multi-Texturing_with_GLSL
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, imgWidth, imgHeight, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, pixels);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
	float x = 1.0f; // 1.0f;	
       GL11.glColor3d(1.0, 1.0, 1.0);
       GL11.glBegin(GL11.GL_QUADS);
           GL11.glTexCoord2f(0, x);
           GL11.glVertex2f(xOffset, yOffset);
           GL11.glTexCoord2f(x, x);
           GL11.glVertex2f(xOffset + width, yOffset);
           GL11.glTexCoord2f(x, 0);
           GL11.glVertex2f(xOffset + width, yOffset + height);
           GL11.glTexCoord2f(0, 0);
           GL11.glVertex2f(xOffset, yOffset + height);
       GL11.glEnd();

       // delete the texture
       GL11.glDeleteTextures(textureIds.get(textureIndex));
       
       GL11.glDisable(GL11.GL_TEXTURE_2D);
	}


	public void setBaseName(String baseFilename){
		baseName  = baseFilename;
	}



}

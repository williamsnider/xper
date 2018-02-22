package org.xper.png.drawing.stimuli;

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
import org.xper.drawing.Drawable;
import org.xper.png.drawing.preview.DrawingManager;

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
	String resourcePath = "/home/justin/jkcode/workspace/JKOpenGL/res/marked/"; //  res/marked/";  //"res/"; // 
	String ext = "_m.png"; // ".png";  // 
	String baseFilename = "sizing2";
	
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
	
	
	int loadTexture(String pathname) {
		textureIndex = 0;
	
		try {
			File imageFile = new File(pathname);
			BufferedImage img = ImageIO.read(imageFile);
			imgWidth = img.getWidth();
			imgHeight = img.getHeight();
			byte[] src = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();
	
			bgr2rgb(src);
	
			pixels = (ByteBuffer)BufferUtils.createByteBuffer(src.length).put(src, 0x00000000, src.length).flip();
			
			return 0; 
	
		} catch(IOException e) {
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
		String resourcePath = "/home/justin/jkcode/workspace/JKOpenGL/res/marked/"; 
		String ext = "_m.png"; // ".png";  // 
		String baseFilename = "sizing3";
		
		String testImageName = resourcePath + baseFilename + "_000" + Integer.toString(0) + ext;
		int numTrials = 5;    
		DrawingManager testWindow = new DrawingManager(1050, 1400);
		
		for(int i = 0; i < numTrials; i++){
			Image img = new Image();		
			img.loadTexture(testImageName);
			List<Image> images = new ArrayList<Image>();
			images.add(img);
			testWindow.setStimObjs(images);		// add object to be drawn
		}
		
		testWindow.drawStimuli();	
		
	}

 

	@Override
	public void draw(Context context) {

		float width = imgWidth; // texture.getImageWidth();
		float height = imgHeight; // texture.getImageHeight();		
		float yOffset = -height / 2;
		float xOffset = -width / 2; 
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);  	

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIds.get(textureIndex));
		
		// from http://wiki.lwjgl.org/index.php?title=Multi-Texturing_with_GLSL
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, imgWidth, imgHeight, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, pixels);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
		
       GL11.glColor3d(1.0, 1.0, 1.0);
       GL11.glBegin(GL11.GL_QUADS);
           GL11.glTexCoord2f(0, 1);
           GL11.glVertex2f(xOffset, yOffset);
           GL11.glTexCoord2f(1, 1);
           GL11.glVertex2f(xOffset + width, yOffset);
           GL11.glTexCoord2f(1, 0);
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
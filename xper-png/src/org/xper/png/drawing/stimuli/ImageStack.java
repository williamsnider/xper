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
import org.lwjgl.opengl.GL12;
import org.xper.classic.vo.TrialContext;
import org.xper.drawing.Context;
import org.xper.drawing.Drawable;
import org.xper.png.drawing.preview.DrawingManager;

public class ImageStack implements Drawable {

	 private static final int BYTES_PER_PIXEL = 4;//3 for RGB, 4 for RGBA

	int numFrames = 1;
    IntBuffer textureIds = BufferUtils.createIntBuffer(numFrames);
    boolean texturesLoaded = false;
    int frameNum = 0;
    
    // this probably should from the database 
    String resourcePath = "/home/alexandriya/catch_cluster_images/"; 
    String ext = "_R.png";  
 
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

	public void setNumFrames(int numFrames) {
		this.numFrames = numFrames;
		textureIds = BufferUtils.createIntBuffer(numFrames);	
		System.out.println("JK 3320 ImageStack:setNumFrames() : prepping for " + numFrames + " images ");
	}
    
    public void loadFrames(String baseFilename){
        
    	textureIds.clear();
    	
    	GL11.glGenTextures(textureIds);
    	
    	for(int n = 0; n < numFrames; n++){
//    		if(n <  10){
    			imageName = resourcePath + baseFilename + Integer.toString(n) + ext;
//    			imageName = resourcePath + baseFilename + ext;
//    		} else {
//    			imageName = resourcePath + baseFilename + ext;
//    		}
// JK 
//    		imageName = resourcePath + "sizing2_0000_m.png"    ;
    		
    		loadTexture(imageName, n);    		
    	}   
    	
    	// assume success?!
    	texturesLoaded = true;
    	
    }
    
    

    int loadTexture(String pathname, int textureIndex) {

    	try {
    		File imageFile = new File(pathname);
    		BufferedImage img = ImageIO.read(imageFile);

    		byte[] src = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();

    		bgr2rgb(src);

    		ByteBuffer pixels = (ByteBuffer)BufferUtils.createByteBuffer(src.length).put(src, 0x00000000, src.length).flip();

    		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIds.get(textureIndex));
		
    		// from http://wiki.lwjgl.org/index.php?title=Multi-Texturing_with_GLSL
    		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
    		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
    		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, img.getWidth(), img.getHeight(), 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, pixels);
    		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
System.out.println("ImageStack::loadTexture() : loaded " + imageFile );   		
    		return textureIds.get(textureIndex);

    	} catch(IOException e) {
    		System.out.println("ImageStack::loadTexture() : path is : " + pathname);
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

	
    
		@Override
		public void draw(Context context) {
			TrialContext c = (TrialContext)context;		

			int frameNum = c.getSlideIndex();
	
//			System.out.println("JK 093 imageStack:draw() slideIndex = " + 
//			c.getSlideIndex() + ", frameNum = " + frameNum);

			
//			if(frameNum >= numFrames){
//		       	return;
//		    }
			
//			// 24 Oct 2016  memleak tracking
//			if(!texturesLoaded){
//				loadFrames("180705_r-198_g-1_l-0_s-");
//				texturesLoaded = true;
//				System.out.println("loading frames");
//				frameNum = 0;
//			}
		      
			float width = 1400; // texture.getImageWidth();
			float height = 1050; // texture.getImageHeight();		
			float yOffset = -525;
			float xOffset = -width / 2; 

	      
			GL11.glEnable(GL11.GL_TEXTURE_2D);  	
		   	GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIds.get(frameNum));

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
	        GL11.glDeleteTextures(textureIds.get(frameNum));
	        
	        GL11.glDisable(GL11.GL_TEXTURE_2D);
	    
	        if(frameNum < numFrames){
	        	frameNum += 1;
	        }
	        		
		}

		
	
		

		public void setBaseName(String baseFilename){
			baseName  = baseFilename;
		}



		public static int loadTexture(BufferedImage image){

			int[] pixels = new int[image.getWidth() * image.getHeight()];
			image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

			ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL); //4 for RGBA, 3 for RGB

			for(int y = 0; y < image.getHeight(); y++){
				for(int x = 0; x < image.getWidth(); x++){
					int pixel = pixels[y * image.getWidth() + x];
					buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
					buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
					buffer.put((byte) (pixel & 0xFF));               // Blue component
					buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
				}
			}

			buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS

			// You now have a ByteBuffer filled with the color data of each pixel.
			// Now just create a texture ID and bind it. Then you can load it using 
			// whatever OpenGL method you want, for example:

			int textureID =  GL11.glGenTextures(); //Generate texture ID
			GL11.glBindTexture( GL11.GL_TEXTURE_2D, textureID); //Bind texture ID

			//Setup wrap mode
			GL11.glTexParameteri( GL11.GL_TEXTURE_2D,  GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri( GL11.GL_TEXTURE_2D,  GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

			//Setup texture scaling filtering
			GL11.glTexParameteri( GL11.GL_TEXTURE_2D,  GL11.GL_TEXTURE_MIN_FILTER,  GL11.GL_NEAREST);
			GL11.glTexParameteri( GL11.GL_TEXTURE_2D,  GL11.GL_TEXTURE_MAG_FILTER,  GL11.GL_NEAREST);

			//Send texel data to OpenGL
			GL11.glTexImage2D( GL11.GL_TEXTURE_2D, 0,  GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0,  GL11.GL_RGBA,  GL11.GL_UNSIGNED_BYTE, buffer);

			//Return the texture ID so we can bind it later again
			return textureID;
		}



		public static BufferedImage loadImage(String loc){
			try {
				return ImageIO.read(ImageStack.class.getResource(loc));
			} catch (IOException e) {
				//Error Handling Here
			}
			return null;
		}
		

		public static void testImage(){
		    int numTrials = 50;    
			DrawingManager testWindow = new DrawingManager(1050, 1400);
			
			for(int i = 0; i < numTrials; i++){
				ImageStack s = new ImageStack();	

       			s.setNumFrames(10);
     			s.loadFrames("180709_r-219_g-1_l-0_s-");
//				
//				List<ImageStack> images = new ArrayList<ImageStack>();
//				images.add(s);
//				testWindow.setStimObjs(images);		// add object to be drawn
			}
			
//			testWindow.drawStimuli();
			
		}
		
		  
}
		
		
		


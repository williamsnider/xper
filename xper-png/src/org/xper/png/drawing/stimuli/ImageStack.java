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
	// 10 Oct 2016 moving to texture list
	int NumFrames = 26;
//	ArrayList<Texture> textures = new ArrayList<Texture>();
    IntBuffer textureIds = BufferUtils.createIntBuffer(NumFrames);
    boolean texturesLoaded = false;
    int frameNum = 0;
    
    // this probably should from the database 
    String resourcePath = "/home/justin/jkcode/xper-alex/images/marked/"; //  res/marked/";  //"res/"; // 
    String ext = "_m.png"; // ".png";  // 
 
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

    
    public void loadFrames(String baseFilename){
        
    	textureIds.clear();
    	
    	GL11.glGenTextures(textureIds);
    	
    	for(int n = 0; n < NumFrames; n++){
    		if(n <  10){
    			imageName = resourcePath + baseFilename + "_000" + Integer.toString(n) + ext;
    		} else {
    			imageName = resourcePath + baseFilename + "_00" + Integer.toString(n) + ext;
    		}
    		loadTexture(imageName, n);    		
    	}   
    	
    	// assume success?!
    	texturesLoaded = true;
    	
    }
    
    

    int loadTexture(String pathname, int textureIndex) {

    	try {
//    		URL url = getClass().getResource(pathname);
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

	
	public static void testImage(){
	    int numTrials = 50;    
		DrawingManager testWindow = new DrawingManager(1050, 1400);
		
		for(int i = 0; i < numTrials; i++){
			ImageStack s = new ImageStack();		
			s.loadFrames("sizing3");
			
			List<ImageStack> images = new ArrayList<ImageStack>();
			images.add(s);
			testWindow.setStimObjs(images);		// add object to be drawn
		}
		
		testWindow.drawStimuli();
		
	}
	
	  
    
		@Override
		public void draw(Context context) {
			TrialContext c = (TrialContext)context;		
			
			int frameNum = c.getAnimationFrameIndex();
			
//			if(ndx >= textureIds.size()){
//				return;
//			}
//		      
			if(frameNum >= NumFrames){
		       	return;
		    }
			
			// 24 Oct 2016  memleak tracking
			if(!texturesLoaded){
				loadFrames("sizing3");
				texturesLoaded = true;
				System.out.println("loading frames");
				frameNum = 0;
			}
		      
			float width = 1400; // texture.getImageWidth();
			float height = 1050; // texture.getImageHeight();		
			float yOffset = -525;
			float xOffset = -width / 2; 
			
//	      GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);          
//	      GL11.glViewport(0,0, (int)width,(int)height);
//	      GL11.glMatrixMode(GL11.GL_MODELVIEW); 
//	      GL11.glMatrixMode(GL11.GL_PROJECTION);
//	      GL11.glLoadIdentity();
//	      GL11.glOrtho(0, (int)width,(int)height, 0, 1, -1);
//	      GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
	      
	      GL11.glEnable(GL11.GL_TEXTURE_2D);  	
		   	GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIds.get(frameNum));

	        
//	        GL13.glActiveTexture(GL13.GL_TEXTURE0 + frameNum);
//	        GL11.glColor3d(1.0, 1.0, 1.0);
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

//	        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

	        // delete the texture
	        GL11.glDeleteTextures(textureIds.get(frameNum));
	        
	        GL11.glDisable(GL11.GL_TEXTURE_2D);
	    
	        if(frameNum < NumFrames){
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
}
		
		
		


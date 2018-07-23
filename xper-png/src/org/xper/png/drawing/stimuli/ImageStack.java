package org.xper.png.drawing.stimuli;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.xper.classic.vo.TrialContext;
import org.xper.drawing.Context;
import org.xper.drawing.Drawable;
import org.xper.png.drawing.preview.DrawingManager;

public class ImageStack implements Drawable {

	 private static final int BYTES_PER_PIXEL = 4; //3 for RGB, 4 for RGBA

	int numFrames = 1;
    IntBuffer textureIds = BufferUtils.createIntBuffer(numFrames);
    boolean texturesLoaded = false;
    int frameNum = 0;
    float scaler = 3.45f;    // scales actual image size to viewport size
    
    // this probably should from the database 
//    String resourcePath = "/home/alexandriya/catch_cluster_images/"; 
    String resourcePath = "/Users/ecpc31/Dropbox/Blender/catch_cluster_images/Rendered/";
    String ext = "_R.png";  
 
    String imageName;
    String baseName;
    
    // keep track of which images go with with stim by storing the last image ndx for 
    // each stim (3)
    int currNdx = 0;
    int[] stopNdx = {0, 0, 0};
    
  
	
	// the list of filenames to load.  
    public void loadImages(List<Map<String, Object>> stimInfo){    
    	// Hard code it in stone ...
    	int numAnimacyImages = 10 * 2;
    	int numRollingImages = 60 * 2;
    	
    	String baseName;
    	String stimType;
    	String optionalPath = "";
    	String side;
//    	Boolean addExt = true;
    	
    	textureIds.clear();    	
    	GL11.glGenTextures(textureIds);    	
    	
    	for(int n = 0; n < numFrames; n++){
    		stimType = (String)((stimInfo.get((int)(java.lang.Math.floor(n/2)))).get("stimType"));
    		baseName = (String)((stimInfo.get((int)(java.lang.Math.floor(n/2)))).get("descId"));
    		
    		// look for special cases e.g. animacy to change optional path 
    		if(stimType.contains("ANIMACY")) {
    			optionalPath = baseName;
    		} else if(stimType.contains("BLANK")){
    			optionalPath ="";
    			baseName = "fakeBlank.png";
//    			addExt = true;
    		}
    		else {
    			optionalPath = "";
    		}
    		
//    		if(addExt) {
	    		if(n % 2 == 0) {
	    			side = "_L";
	    		}else {
	    			side = "_R";
	    		}    
//    		}
    		
    		imageName = resourcePath + optionalPath + baseName + ext;
    		System.out.println("JK 3330 ImageStack:loadImages() : " + imageName);
    		
    		// JK 11 July 2018 
    		// check for BLANK and load a standard png
    		if(imageName.contains("BLANK")) {
    			imageName = resourcePath + "180719_r-190_g-2_l-0_s-0_R.png";
//    			System.out.println("JK 3330 ImageStack:loadImages() :fakeBlank for " + filenames.get((int)(java.lang.Math.floor(n/2))));
    		}
    		loadTexture(imageName, n);    		
    	}       	
    	// assume success?!
    	texturesLoaded = true;
   	  	System.out.format("JK 3340 ImageStack:loadImages() : loaded %d images %n", numFrames);
    }
	
//	// the list of filenames to load.  
//    public void loadImages(List<String> filenames){        
//    	textureIds.clear();    	
//    	GL11.glGenTextures(textureIds);    	
//    	// load _L then _R
//    	for(int n = 0; n < numFrames; n++){
//    		if(n % 2 == 0) {
//    			ext = "_L.png";
//    		}else {
//    			ext = "_R.png";
//    		}    	
//    		imageName = resourcePath + filenames.get((int)(java.lang.Math.floor(n/2))) + ext;
//    		
//    		// JK 11 July 2018 
//    		// check for BLANK and load a standard png
//    		if(imageName.contains("BLANK")) {
//    			imageName = resourcePath + "fakeBlank.png";
////    			System.out.println("JK 3330 ImageStack:loadImages() :fakeBlank for " + filenames.get((int)(java.lang.Math.floor(n/2))));
//    		}
//    		loadTexture(imageName, n);    		
//    	}       	
//    	// assume success?!
//    	texturesLoaded = true;
//   	  	System.out.format("JK 3340 ImageStack:loadImages() : loaded %d images %n", numFrames);
//    }
//    
//    

	
    
	@Override
	public void draw(Context context) {
		TrialContext c = (TrialContext)context;		

		int frameNum = c.getSlideIndex();
		int vpNum = c.getViewportIndex();
		int animNdx = c.getAnimationFrameIndex();

		int ndx = 2 * frameNum + vpNum;
//		System.out.println("JK 0838 viewPort == " + vpNum);
//		System.out.println("JK 093 ***** ImageStack:draw() ***** ndx = " + ndx + ", animation ndx = " + c.getAnimationFrameIndex());
	      
		// JK 2981  18 July 2018 
		float width = 1400  / scaler; //  2    // texture.getImageWidth();
		float height = 1050 / scaler; //  2    // texture.getImageHeight();		
		float yOffset = -height / 2;
		float xOffset = -width / 2; 

	
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1f);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);  	
	   	GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIds.get(ndx));

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
        GL11.glDeleteTextures(textureIds.get(ndx));
        
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    
        if(frameNum < numFrames){
        	frameNum += 1;
        }
        		
	}

	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(true){
			testImage();
			return;
		}

	}

	public void setNumFrames(int numImgs) {
		this.numFrames = numImgs * 2;
		textureIds = BufferUtils.createIntBuffer(numFrames);	
		System.out.println("JK 3320 ImageStack:setNumFrames() : prepping for " + numFrames + " images ");
	}
	
	
    
    public void loadFrames(String baseFilename){
        
    	textureIds.clear();    	
    	GL11.glGenTextures(textureIds);
    	
    	for(int n = 0; n < numFrames; n++){
    		if(n <  10){
    			imageName = resourcePath + baseFilename + Integer.toString(n) + ext;
//    			imageName = resourcePath + baseFilename + ext;
    		} else {
    			imageName = resourcePath + baseFilename + ext;
    		}		
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

    		// reorder the 4 bytes per pixel data  
    		abgr2rgba(src);

    		ByteBuffer pixels = (ByteBuffer)BufferUtils.createByteBuffer(src.length).put(src, 0x00000000, src.length).flip();

    		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIds.get(textureIndex));
		
    		// from http://wiki.lwjgl.org/index.php?title=Multi-Texturing_with_GLSL
    		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
    		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
    		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
    		
    		// only for RGB
    		// GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, img.getWidth(), img.getHeight(), 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, pixels);
    		
    		// RGBA
    		GL11.glTexImage2D( GL11.GL_TEXTURE_2D, 0,  GL11.GL_RGBA8, img.getWidth(), img.getHeight(), 0,  GL11.GL_RGBA,  GL11.GL_UNSIGNED_BYTE, pixels);    		
    		
    		return textureIds.get(textureIndex);

    	} catch(IOException e) {
    		System.out.println("ImageStack::loadTexture() : path is : " + pathname);
    		e.printStackTrace();
    		throw new RuntimeException(e);
    	}
    }
    
    

    // use this to reorder the RGB bytes 
    void bgr2rgb(byte[] target) {
    	byte tmp;

    	for(int i=0x00000000; i<target.length; i+=0x00000003) {
    		tmp = target[i];
    		target[i] = target[i+0x00000002];
    		target[i+0x00000002] = tmp;
    	}
    }
    
    
    // repack abgr to rgba    
    void abgr2rgba(byte[] target) {
    	byte tmpAlphaVal;
    	byte tmpBlueVal;
    	
    	for(int i=0x00000000; i<target.length; i+=0x00000004) {
    		tmpAlphaVal = target[i];
    		target[i] = target[i+0x00000003];
    		tmpBlueVal = target[i+0x00000001];
    		target[i+0x00000001] = target[i+0x00000002];
    		target[i+0x00000002] = tmpBlueVal;
    		target[i+0x00000003] = tmpAlphaVal;
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

			buffer.flip(); // DO NOT FORGET THIS

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
			// JK 10 July 2018 	RGBA vs RGB		
			GL11.glTexImage2D( GL11.GL_TEXTURE_2D, 0,  GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0,  GL11.GL_RGBA,  GL11.GL_UNSIGNED_BYTE, buffer);
//    		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, image.getWidth(), image.getHeight(), 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);


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
		
		
		


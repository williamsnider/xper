package org.xper.png.drawing.stimuli;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
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

import org.xper.png.expt.generate.PngGAParams;

public class ImageStack implements Drawable {

	private static final int BYTES_PER_PIXEL = 4; //3 for RGB, 4 for RGBA

	int numFrames = 1;
    IntBuffer textureIds = BufferUtils.createIntBuffer(numFrames);
    boolean texturesLoaded = false;
    int frameNum = 0;
    float scaler = 1.0f; //  3.45f;    // scales actual image size to viewport size
    
    // JK 23 Oct 2018 
    // 
    int div = 1;
    
    // this probably should from the database 
    String resourcePath = "/home/alexandriya/catch_cluster_images/"; 
//    String ext = "_R.png";  
    String ext = ".png";
 
    String imageName;
    String baseName;
    
    // keep track of which images go with with stim by storing the last image ndx for 
    // each stim (3)
    int currNdx = 0;
    int[] stopNdx = {0, 0, 0};
    
	double screenWidth;
	double screenHeight;
    
	
	// the list of filenames to load.  
    public void loadImages(List<Map<String, Object>> stimInfo){    
    	// Hard code it in stone ...
    	
    	int numAnimacyImages;
    	int numStillImages;
    	int numRollingImages;
    	
    	if (PngGAParams.stereo) {
    		numAnimacyImages = PngGAParams.PH_animacy_numFrames*2;
    		numStillImages = 2;
    		numRollingImages = 40*2;
    	}
    	else {
    		numAnimacyImages = PngGAParams.PH_animacy_numFrames;
    		numStillImages = 1;
    		numRollingImages = 40;
    	}
    		
    	int animacyRepeat = 45 + 1; //???
    	int totalImgNum = animacyRepeat;
    	int currentImg = 0;
    
    	String baseName;
    	String stimType;
    	String optionalPath = "";
    	String side = "";
    	String numStr;
    	String ext = ".png";

    	List<String> fullFilenames = new ArrayList<String>();
    	
    	boolean end = false;

    	// determine how many frames for this trial while building filename(s) and loading the Texture
    	numFrames = 0;

    	for(Map<String, Object>si : stimInfo) {
    		stimType = (String)si.get("stimType");
    		baseName = (String)si.get("descId");
    		
    		if (PngGAParams.stereo) {
    			totalImgNum = animacyRepeat*2;
    		}
    		
    		if(stimType.contains("ANIMATE")) {
    			optionalPath = baseName + "/";
    			totalImgNum = 60;
    			
    			numFrames += totalImgNum;

    			for (int numImg = 0; numImg < totalImgNum; numImg++) {
    				if (PngGAParams.stereo) {

    					if(currentImg % 2 == 0) {
        					side = "_L";
        				} else {
        					side = "_R";
        				}
    					numStr = String.format("_%04d", (int)(Math.round(currentImg/2) + 1));
    				}
    				
    				else {
    					numStr = String.format("_%04d", (int)currentImg + 1);
    				}
    				
    				
    				imageName = resourcePath + optionalPath + baseName + side + numStr + ext;
    				fullFilenames.add(imageName);

    				if (currentImg == numAnimacyImages-1) {
    					currentImg = 0;
    					
    				}
    				else {
    					currentImg++;
    				}
    			}
    			
//    			numFrames += 10;
//    			for (int numImg = 0; numImg < 10; numImg++) {
//    				numStr = String.format("_%04d", 1);
//    				imageName = resourcePath + optionalPath + baseName + side + numStr + ext;
//    				fullFilenames.add(imageName);
//    			}

    		} else if(stimType.contains("BALL")) {
    			optionalPath = baseName + "/";
    			totalImgNum = (numRollingImages+7)*2;
    			
    			if (PngGAParams.stereo) {
        			totalImgNum = totalImgNum*2;
        		}
    			
    			numFrames += totalImgNum;
    			int pause = 0;

    			for (int numImg = 0; numImg < totalImgNum; numImg++) {

    				if (PngGAParams.stereo) {
    					if (pause < 30) {
        					if (pause % 2 == 0) {
            					currentImg = 0;
            				} else {
            					currentImg = 1;
            				}
        				}
        				
        				if(currentImg % 2 == 0) {
        					side = "_L";
        				} else {
        					side = "_R";
        				}
        				numStr = String.format("_%04d", (int)(Math.round(currentImg/2) + 1));
    				}
    				else {
    					if (pause < 15) {
//    						currentImg = currentImg;
    						currentImg = 0;
    					}
//    					numStr = String.format("_%04d", (int)currentImg + 1);
    					numStr = String.format("_%04d", (int)(Math.round(currentImg/2) + 1));
    				}
    				side = "";
    				ext = ".png";
    				imageName = resourcePath + optionalPath + baseName + side + numStr + ext;
    				fullFilenames.add(imageName);
    				pause++;
    				currentImg++;
    			}
    			
    		}  else if(stimType.contains("BLANK")) {
    			numFrames += numStillImages;
    			for(int n = 0; n < numStillImages; n++) {
    				if (PngGAParams.stereo) {
    					if(n % 2 == 0) {
        					side = "_L";
        				} else {
        					side = "_R";
        				}
    				}
    				
					imageName = resourcePath + "BLANK/BLANK_FIX" + side + ext;
					fullFilenames.add(imageName);
    			}
    		} else {
    			
    			
    			numFrames += numStillImages;
    			
    			for(int n = 0; n < numStillImages; n++) {
    				if (PngGAParams.stereo) {
    					if(n % 2 == 0) {
        					side = "_L";
        				} else {
        					side = "_R";
        				}
    				}
    				
    				imageName = resourcePath + baseName + side + ext;
    				fullFilenames.add(imageName);
//
//        			if (end==false) {
//        				fullFilenames.add(imageName);
//        				end = true;
//        			}
    			}
      		}
    	}
		 
		//this is important!
		setNumFrames(numFrames);
		frameNum = 0;
    	GL11.glGenTextures(textureIds); 
    	
		int n = 0;
		
//		System.out.println(numFrames);
//		System.out.println(textureIds);
		
		for(String str : fullFilenames) {
//			System.out.println(str);
			loadTexture(str, n++);
		}
  		
    	// assume success?!
    	texturesLoaded = true;
    }
	
    
    // call this before loadTexture and after setNumFrames
    public void genTextures() {
    	GL11.glGenTextures(textureIds); 

    }
    

    public int loadTexture(String pathname, int textureIndex) {
    	
//    	int actualTextureIndex = 0;
    	    	
    	// JK 23 Oct 2018
    	// only load and create every other texture i.e. the left image, skip the right image

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
    		//    		 GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, img.getWidth(), img.getHeight(), 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, pixels);

    		// RGBA
    		GL11.glTexImage2D( GL11.GL_TEXTURE_2D, 0,  GL11.GL_RGBA8, img.getWidth(), img.getHeight(), 0,  GL11.GL_RGBA,  GL11.GL_UNSIGNED_BYTE, pixels);    		
    		//   
//    			    		System.out.println("JK 5353 ImageStack:loadTexture() " + imageFile + " : " + textureIndex + 
//    			    				" id = " + textureIds.get(textureIndex) + " actualNdx = " + actualTextureIndex);    		

    		return textureIds.get(textureIndex);


    	} catch(IOException e) {
    		e.printStackTrace();
    		System.out.println("ImageStack::loadTexture() : path is : " + pathname);
    		throw new RuntimeException(e);
    	}
    }
    
	@Override
	public void draw(Context context) {
		TrialContext c = (TrialContext)context;		
				
		// JK 2981  18 July 2018 
		float width = (float) screenWidth  / scaler; //  2    // texture.getImageWidth();
		float height = (float) screenHeight / scaler; //  2    // texture.getImageHeight();		
		float yOffset = -height / 2;
		float xOffset = -width / 2;
//		int actualTextureIndex = 0;

		// JK 23 Oct 2018
		// only using the left image
//		actualTextureIndex = (int)(frameNum / div);
		
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1f);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);  	
		// JK 23 Oct 2018
//		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIds.get(actualTextureIndex));
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
     
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    
//System.out.println("JK 12353 ImageStack:draw() :  frameNum = " + frameNum + " : " + actualTextureIndex );    		

        
        if(frameNum < numFrames - 1){
        	frameNum += 1;
        } 

        		
	}

	
	public void cleanUp() {
//		int actualTextureIndex = 0;
		
		for(int i = 0; i < numFrames; i++) {
			// JK 23 Oct 2018
			// only using the left image
//			actualTextureIndex = (int)(frameNum / div);
			
//			GL11.glDeleteTextures(textureIds.get(actualTextureIndex));
			GL11.glDeleteTextures(textureIds.get(frameNum));
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		if(true){
//			testImage();
//			return;
//		}

	}

	public void setNumFrames(int numImgs) {
		
		// JK 23 Oct 2018
		// only using the left image
//		this.numFrames = numImgs / div;
		this.numFrames = numImgs;
		textureIds = BufferUtils.createIntBuffer(numFrames);	
	}
	
	
    
    public void loadFrames(String baseFilename){
        
    	textureIds.clear();    	
    	GL11.glGenTextures(textureIds);
    	
    	for(int n = 0; n < numFrames; n++){
    		if(n <  10){
    			imageName = resourcePath + baseFilename + Integer.toString(n) + ext;
    		} else {
    			imageName = resourcePath + baseFilename + ext;
    		}		
    		loadTexture(imageName, n);    		
    	}       	
    	// assume success?!
    	texturesLoaded = true;    	
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


		public void setFrameNum(int newFrameNum) {
			if(newFrameNum < numFrames && newFrameNum >= 0) {
				frameNum = newFrameNum;
			}else {
				System.out.println("ImageStack:setFrameNum() : newFrameNum is not right : " +  newFrameNum); 
			}
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
// JK 23 Oct 
//   this needs to change ....			
//		    int numTrials = 50;    
//			DrawingManager testWindow = new DrawingManager(1050, 1400);
//			
//			for(int i = 0; i < numTrials; i++){
//				ImageStack s = new ImageStack();
//				s.setScreenHeight(1050);
//				s.setScreenWidth(1400);
//
//       			s.setNumFrames(10);
//     			s.loadFrames("180709_r-219_g-1_l-0_s-");
//				
//				List<ImageStack> images = new ArrayList<ImageStack>();
//				images.add(s);
//				testWindow.setStimObjs(images);		// add object to be drawn
//			}
//			
//			testWindow.drawStimuli();
//			
		}
		
		public void setScreenWidth(double screenWidth) {
			this.screenWidth = screenWidth/2;
		}
		
		public void setScreenHeight(double screenHeight) {
			this.screenHeight = screenHeight;
		}
		
		
}
		
		
		


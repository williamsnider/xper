package org.xper.png.drawing.screenobj;

import org.lwjgl.opengl.GL11;
import org.xper.drawing.Context;
import org.xper.drawing.object.BlankScreen;
import org.xper.png.drawing.stimuli.ImageStack;

public class PngBlankScreen extends BlankScreen {
	
	String blankImageStr = "/home/alexandriya/catch_cluster_images/BLANK";
			
	ImageStack blankImage = new ImageStack();
	
	public PngBlankScreen() {
		super();

		System.out.println("PngBlankScreen()");
	}
	
	public void draw(Context context) {
//		System.out.println("custom draw : context = " + context.toString());
		
		if(context == null) {
			super.draw(context);
			System.out.println("super draw()");
		} else {
			blankImage.draw(context);	
			System.out.println("custom draw()");
		}		
	}
	
	
	public void loadImages() {
		System.out.println("custom draw : loading images");
		blankImage.setNumFrames(2);
		blankImage.loadTexture(blankImageStr + "_L.png", 0);
		blankImage.loadTexture(blankImageStr + "_R.png", 1);
		
	}
	

}

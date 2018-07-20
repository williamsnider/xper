package org.xper.png.expt;

import java.util.ArrayList;
import java.util.List;

import org.xper.Dependency;
import org.xper.classic.vo.TrialContext;
import org.xper.db.vo.StimSpecEntry;
import org.xper.drawing.AbstractTaskScene;
import org.xper.drawing.Context;
import org.xper.experiment.ExperimentTask;

import org.xper.png.drawing.stick.MStickSpec;
import org.xper.png.drawing.stimuli.ImageStack;
import org.xper.png.drawing.stimuli.PngObject;
import org.xper.png.drawing.stimuli.PngObjectSpec;
import org.xper.png.util.PngDbUtil;
import org.lwjgl.opengl.GL11;


public class PngExptScene extends AbstractTaskScene {
	
	@Dependency
	PngDbUtil dbUtil;
	
	ImageStack images = new ImageStack();
	PngExptSpec spec = new PngExptSpec();
	
 
	public void initGL(int w, int h) {
		super.setUseStencil(false);
		super.initGL(w, h);
		System.out.println("JK 2838 w = " + w + ", h = " + h);
		
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);          
		GL11.glViewport(0,0,w,h);
        GL11.glMatrixMode(GL11.GL_MODELVIEW); 
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
		

        GL11.glOrtho(0, w, h, 0, 1, -1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        
	}

	
	public void setTask(ExperimentTask task) {
		if(task == null) {
			System.out.println("PgnExptScene:setTask() : task is null ...");
		}
	}

	public void drawStimulus(Context context) {
		TrialContext c = (TrialContext)context;
//		System.out.println("JK 0239 PngExptScene:drawStimulus(): slide index = " + c.getSlideIndex());

		images.draw(c);
	}

	public PngDbUtil getDbUtil() {
		return dbUtil;
	}

	public void setDbUtil(PngDbUtil dbUtil) {
		this.dbUtil = dbUtil;
	}
	
	
	
	// JK 9 July 2018
	public void trialStart(TrialContext context) {
		System.out.println("\nJK 0639 PngExptScene:trialStart "); //+ context.getCurrentTask().get);
		
		spec = PngExptSpec.fromXml(context.getCurrentTask().getStimSpec());
		int numImages = spec.getStimObjIdCount();
		List<String> filenames = new ArrayList<String>();
		
		
		for(int i = 0; i < numImages; i++) {
			filenames.add(i, dbUtil.readDescriptiveIdFromStimObjId(spec.getStimObjId(i)));
//			System.out.println("PngExptScene:trialStart() : " + filenames.get(i) + " : " + spec.getStimObjId(i));
		}
		
		images = new ImageStack();
		images.setNumFrames(numImages);
		images.loadImages(filenames);
	}


	public void trialStop(TrialContext context) {
		System.out.println("JK 0739 PngExptScene:trialStop\n\n" );
	}
	
}

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
	
//	List<PngObject> objects = new ArrayList<PngObject>();
	List<ImageStack> objects = new ArrayList<ImageStack>();
	PngExptSpec spec = new PngExptSpec();
	
	public void initGL(int w, int h) {
		super.setUseStencil(false);
		super.initGL(w, h);	
		
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);          
		  
        GL11.glViewport(0,0,w,h);
        GL11.glMatrixMode(GL11.GL_MODELVIEW); 
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, w, h, 0, 1, -1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        
	}

	public void setTask(ExperimentTask task) {
		objects.clear();
		spec = PngExptSpec.fromXml(task.getStimSpec());
System.out.println("PngExptScene :: setTask() : spec = \n" + spec.toXml());
		
spec.setBaseFilename("180706_r-210_g-1_l-0_s-9");
		System.out.println("PngExptScene : setTask ()  " + spec.getBaseFilename());
		
		ImageStack obj = new ImageStack();
//		obj.setNumFrames(spec.getNumFrames());
		obj.loadFrames(spec.getBaseFilename()); // "sizing3");
		objects.add(obj);
//		
//		for (int i = 0; i < spec.getStimObjIdCount(); i ++) {
//			long id = spec.getStimObjId(i);
//			StimSpecEntry stimSpec = dbUtil.readStimSpec_java(id);
//			PngObjectSpec pngSpec = PngObjectSpec.fromXml(stimSpec.getSpec()); 
//			
//			PngObject obj = new PngObject();
//			
//			stimSpec = dbUtil.readStimSpec_stick(id);
//			MStickSpec mstickSpec = MStickSpec.fromXml(stimSpec.getSpec());
//			
//			obj.setSpec_java(pngSpec);
//			obj.setSpec_stick(mstickSpec);
//			obj.getSpec_java().setDoStickGen(false);
//			obj.getSpec_java().setDoStickMorph(false);
//			obj.finalizeObject();
//			
//			objects.add(obj);
//		}
	}

	public void drawStimulus(Context context) {
		TrialContext c = (TrialContext)context;
		
		int index = c.getSlideIndex();
		int numObjs = objects.size();
		
		if ((index >= 0) && (index < numObjs)) {
			ImageStack obj = objects.get(index);
			obj.draw(c);
		}
	}

	public PngDbUtil getDbUtil() {
		return dbUtil;
	}

	public void setDbUtil(PngDbUtil dbUtil) {
		this.dbUtil = dbUtil;
	}
	
}

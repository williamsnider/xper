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
import org.xper.png.drawing.stimuli.PngObject;
import org.xper.png.drawing.stimuli.PngObjectSpec;
import org.xper.png.util.PngDbUtil;

public class PngExptScene extends AbstractTaskScene {
	
	@Dependency
	PngDbUtil dbUtil;
	
	List<PngObject> objects = new ArrayList<PngObject>();
	PngExptSpec spec = new PngExptSpec();
	
	public void initGL(int w, int h) {
		super.initGL(w, h);	
	}

	public void setTask(ExperimentTask task) {
		objects.clear();
		spec = PngExptSpec.fromXml(task.getStimSpec());
		
		for (int i = 0; i < spec.getStimObjIdCount(); i ++) {
			long id = spec.getStimObjId(i);
			StimSpecEntry stimSpec = dbUtil.readStimSpec_java(id);
			PngObjectSpec pngSpec = PngObjectSpec.fromXml(stimSpec.getSpec()); 
			
			PngObject obj = new PngObject();
			
			stimSpec = dbUtil.readStimSpec_stick(id);
			MStickSpec mstickSpec = MStickSpec.fromXml(stimSpec.getSpec());
			
			obj.setSpec_java(pngSpec);
			obj.setSpec_stick(mstickSpec);
			obj.getSpec_java().setDoStickGen(false);
			obj.getSpec_java().setDoStickMorph(false);
			obj.finalizeObject();
			
			objects.add(obj);
		}
	}

	public void drawStimulus(Context context) {
		TrialContext c = (TrialContext)context;
		
		int index = c.getSlideIndex();
		int numObjs = objects.size();
		
		if ((index >= 0) && (index < numObjs)) {
			PngObject obj = objects.get(index);
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

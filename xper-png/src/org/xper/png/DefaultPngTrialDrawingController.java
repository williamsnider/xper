package org.xper.png;


import org.xper.png.util.PngExperimentUtil;
import org.xper.png.vo.PngTrialContext;
import org.xper.classic.MarkEveryStepTrialDrawingController;
import org.xper.classic.vo.TrialContext;
import org.xper.experiment.ExperimentTask;

public class DefaultPngTrialDrawingController extends
		MarkEveryStepTrialDrawingController {
	
	/*public void slideFinish(ExperimentTask task, TrialContext context) {
		taskScene.nextMarker();
		taskScene.drawBlank(context, true, true);
		window.swapBuffers();
	}*/
	
	public void showTarget(ExperimentTask task, TrialContext context) {
		taskScene.nextMarker();
		
		if (taskScene instanceof PngTaskScene && PngExperimentUtil.isTargetOn((PngTrialContext)context)) {
			((PngTaskScene)taskScene).drawTargetScene(context);
		} else {
			taskScene.drawBlank(context, true, true);
		}
		
		window.swapBuffers();
	}
	
	public void targetSelectionDone(ExperimentTask task, TrialContext context) {
		taskScene.nextMarker();
		taskScene.drawBlank(context, true, true);
		window.swapBuffers();
	}
}

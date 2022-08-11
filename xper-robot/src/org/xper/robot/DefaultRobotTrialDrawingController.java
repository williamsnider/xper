package org.xper.robot;


import org.xper.classic.MarkEveryStepTrialDrawingController;
import org.xper.classic.vo.TrialContext;
import org.xper.experiment.ExperimentTask;
import org.xper.robot.util.RobotExperimentUtil;
import org.xper.robot.vo.RobotTrialContext;

public class DefaultRobotTrialDrawingController extends
		MarkEveryStepTrialDrawingController {
	
	/*public void slideFinish(ExperimentTask task, TrialContext context) {
		taskScene.nextMarker();
		taskScene.drawBlank(context, true, true);
		window.swapBuffers();
	}*/
	
	public void showTarget(ExperimentTask task, TrialContext context) {
		taskScene.nextMarker();
		
		if (taskScene instanceof RobotTaskScene && RobotExperimentUtil.isTargetOn((RobotTrialContext)context)) {
			((RobotTaskScene)taskScene).drawTargetScene(context);
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

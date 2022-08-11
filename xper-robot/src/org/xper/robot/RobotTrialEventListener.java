package org.xper.robot;


import org.xper.classic.TrialEventListener;
import org.xper.robot.vo.RobotTrialContext;

public interface RobotTrialEventListener extends TrialEventListener {
	public void targetOn (long timestamp, RobotTrialContext context);
	//public void targetInitialSelection(long timestamp, RobotTrialContext context);
	public void targetSelectionSuccess(long timestamp, RobotTrialContext context);
	
	// added by shs for behavioral tracking:
	public void trialPASS(long timestamp, RobotTrialContext context);
	public void trialFAIL(long timestamp, RobotTrialContext context);
	public void trialBREAK(long timestamp, RobotTrialContext context);
	public void trialNOGO(long timestamp, RobotTrialContext context);
}

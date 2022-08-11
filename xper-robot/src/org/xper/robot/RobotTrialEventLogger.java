package org.xper.robot;


import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.xper.classic.TrialEventLogger;
import org.xper.robot.vo.RobotTrialContext;

public class RobotTrialEventLogger extends TrialEventLogger implements
		RobotTrialEventListener {
	static Logger logger = Logger.getLogger(RobotTrialEventLogger.class);

//	public void targetInitialSelection(long timestamp, RobotTrialContext context) {
//		log("targetInitialSelection", timestamp);
//	}

	public void targetOn(long timestamp, RobotTrialContext context) {
		log("targetOn", timestamp);
	}

	public void targetSelectionSuccess(long timestamp, RobotTrialContext context) {
		log("targetSelectionSuccess", timestamp);
	}
	
	protected void log(String event, long timestamp, String data) {
		logger.info(event + ": " + new Timestamp(timestamp/1000).toString() + " - " + data);
	}

	// added by shs for behavioral tracking:
	public void trialPASS(long timestamp, RobotTrialContext context) {
		log("trialPASS", timestamp);		
	}

	public void trialFAIL(long timestamp, RobotTrialContext context) {
		log("trialFAIL", timestamp);		
	}

	public void trialBREAK(long timestamp, RobotTrialContext context) {
		log("trialBREAK", timestamp);		
	}

	public void trialNOGO(long timestamp, RobotTrialContext context) {
		log("trialNOGO", timestamp);		
	}

}

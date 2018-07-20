package org.xper.png;


import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.xper.png.vo.PngTrialContext;
import org.xper.classic.TrialEventLogger;

public class PngTrialEventLogger extends TrialEventLogger implements
		PngTrialEventListener {
	static Logger logger = Logger.getLogger(PngTrialEventLogger.class);

//	public void targetInitialSelection(long timestamp, PngTrialContext context) {
//		log("targetInitialSelection", timestamp);
//	}

	public void targetOn(long timestamp, PngTrialContext context) {
		log("targetOn", timestamp);
	}

	public void targetSelectionSuccess(long timestamp, PngTrialContext context) {
		log("targetSelectionSuccess", timestamp);
	}
	
	protected void log(String event, long timestamp, String data) {
		logger.info(event + ": " + new Timestamp(timestamp/1000).toString() + " - " + data);
	}

	// added by shs for behavioral tracking:
	public void trialPASS(long timestamp, PngTrialContext context) {
		log("trialPASS", timestamp);		
	}

	public void trialFAIL(long timestamp, PngTrialContext context) {
		log("trialFAIL", timestamp);		
	}

	public void trialBREAK(long timestamp, PngTrialContext context) {
		log("trialBREAK", timestamp);		
	}

	public void trialNOGO(long timestamp, PngTrialContext context) {
		log("trialNOGO", timestamp);		
	}

}

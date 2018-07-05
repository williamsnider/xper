package org.xper.png.util;

import java.util.List;

import org.xper.png.PngTrialEventListener;
import org.xper.png.vo.PngTrialContext;
import org.xper.classic.TrialEventListener;
import org.xper.classic.vo.TrialResult;

public class PngEventUtil {

	public static void fireTargetOnEvent(long timestamp,
			List<? extends TrialEventListener> trialEventListeners,	PngTrialContext currentContext) {
		for (TrialEventListener listener : trialEventListeners) {
			if (listener instanceof PngTrialEventListener) {
				((PngTrialEventListener)listener).targetOn(timestamp, currentContext);
			}
		}
	}

	/*public static void fireTargetInitialSelectionEvent(long timestamp,
			List<? extends TrialEventListener> trialEventListeners, PngTrialContext currentContext) {
		for (TrialEventListener listener : trialEventListeners) {
			if (listener instanceof PngTrialEventListener) {
				((PngTrialEventListener)listener).targetInitialSelection(timestamp, currentContext);
			}
		}
	}*/
	
	public static void fireTargetSelectionSuccessEvent(long timestamp, List<? extends TrialEventListener> trialEventListeners, PngTrialContext currentContext) {
		for (TrialEventListener listener : trialEventListeners) {
			if (listener instanceof PngTrialEventListener) {
				((PngTrialEventListener)listener).targetSelectionSuccess(timestamp, currentContext);
			}
		}
	}
	
	// added by shs for behavioral tracking:
	public static void fireTrialPASSEvent(long timestamp, List<? extends TrialEventListener> trialEventListeners, PngTrialContext currentContext) {
		System.out.println("--PASS (non-target trial)--");

		for (TrialEventListener listener : trialEventListeners) {
			if (listener instanceof PngTrialEventListener) {
				((PngTrialEventListener)listener).trialPASS(timestamp, currentContext);
			}
		}
	}
	
	public static void fireTrialTARGETPASSEvent(long timestamp, List<? extends TrialEventListener> trialEventListeners, PngTrialContext currentContext,long requiredTargetSelectionHoldTime, long targetOnLocalTime) {
		System.out.println("--PASS (Target selection time= " + ((timestamp - targetOnLocalTime)/1000 - requiredTargetSelectionHoldTime) + ")--");

		for (TrialEventListener listener : trialEventListeners) {
			if (listener instanceof PngTrialEventListener) {
				((PngTrialEventListener)listener).trialPASS(timestamp, currentContext);
			}
		}
	}
	
	public static void fireTrialTARGETFAILEvent(long timestamp, List<? extends TrialEventListener> trialEventListeners, 
			PngTrialContext currentContext, TrialResult targetResult, long targetOnLocalTime) {

		if (targetResult == TrialResult.TARGET_SELECTION_EYE_FAIL) {
			System.out.println("--FAIL (Target timeout time= " + (timestamp - targetOnLocalTime)/1000 + ")--");
		} else if (targetResult == TrialResult.TARGET_SELECTION_EYE_BREAK) {
			System.out.println("--FAIL (Target break time= " + (timestamp - targetOnLocalTime)/1000 + ")--");
		} else {
			System.out.println("--FAIL (Target, unknown error)");
		}
		
		for (TrialEventListener listener : trialEventListeners) {
			if (listener instanceof PngTrialEventListener) {
				((PngTrialEventListener)listener).trialFAIL(timestamp, currentContext);
			}
		}
	}
	
	public static void fireTrialFAILEvent(long timestamp, List<? extends TrialEventListener> trialEventListeners, PngTrialContext currentContext) {
		System.out.println("--FAIL (non-target trial)--");
		for (TrialEventListener listener : trialEventListeners) {
			if (listener instanceof PngTrialEventListener) {
				((PngTrialEventListener)listener).trialFAIL(timestamp, currentContext);
			}
		}
	}
	
	public static void fireTrialBREAKEvent(long timestamp, List<? extends TrialEventListener> trialEventListeners, PngTrialContext currentContext,int slideNum, boolean isISI) {
		if (isISI) {
			System.out.println("--BREAK (ISI #" + slideNum + ")--");
		} else {
			System.out.println("--BREAK (slide #" + slideNum + ")--");
		}
		for (TrialEventListener listener : trialEventListeners) {
			if (listener instanceof PngTrialEventListener) {
				((PngTrialEventListener)listener).trialBREAK(timestamp, currentContext);
			}
		}
	}
	
	public static void fireTrialNOGOEvent(long timestamp,
			List<? extends TrialEventListener> trialEventListeners, PngTrialContext currentContext) {
		for (TrialEventListener listener : trialEventListeners) {
			if (listener instanceof PngTrialEventListener) {
				((PngTrialEventListener)listener).trialNOGO(timestamp, currentContext);
			}
		}
	}
}

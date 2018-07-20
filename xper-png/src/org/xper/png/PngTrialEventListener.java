package org.xper.png;


import org.xper.png.vo.PngTrialContext;
import org.xper.classic.TrialEventListener;

public interface PngTrialEventListener extends TrialEventListener {
	public void targetOn (long timestamp, PngTrialContext context);
	//public void targetInitialSelection(long timestamp, PngTrialContext context);
	public void targetSelectionSuccess(long timestamp, PngTrialContext context);
	
	// added by shs for behavioral tracking:
	public void trialPASS(long timestamp, PngTrialContext context);
	public void trialFAIL(long timestamp, PngTrialContext context);
	public void trialBREAK(long timestamp, PngTrialContext context);
	public void trialNOGO(long timestamp, PngTrialContext context);
}

package org.xper.png;


import org.xper.png.vo.PngTargetMessage;
import org.xper.png.vo.PngTrialContext;
import org.xper.png.vo.PngTrialOutcomeMessage;
import org.xper.classic.TrialExperimentMessageDispatcher;
import org.xper.classic.vo.TrialContext;

public class PngExperimentMessageDispatcher extends
		TrialExperimentMessageDispatcher implements PngTrialEventListener {

	public void targetOn(long timestamp, PngTrialContext context) {
		enqueue(timestamp, "TargetOn", PngTargetMessage
				.toXml(new PngTargetMessage(timestamp, context.getTargetPos(), context.getTargetEyeWindowSize())));
	}

//	public void targetInitialSelection(long timestamp, PngTrialContext context) {
//		enqueue(timestamp, "TargetInitialSelection", "");
//	}

	public void targetSelectionSuccess(long timestamp, PngTrialContext context) {
		enqueue(timestamp, "TargetSelectionSuccess", "");
	}

	// added by shs for behavioral tracking:
	// also, want to add another behmsg whose timestamp is the taskId (a clear hack) so I can easily 
	// 	the trial outcome by search for it's taskId!
	public void trialPASS(long timestamp, PngTrialContext context) {
		long taskId = context.getCurrentTask().getTaskId();
		long genId = context.getCurrentTask().getGenId();
		enqueue(timestamp,"TrialOutcome",PngTrialOutcomeMessage
				.toXml(new PngTrialOutcomeMessage(timestamp,"PASS",taskId,genId)));
//		enqueue(taskId,"TaskIdOutcome",PngTrialOutcomeMessage
//				.toXml(new PngTrialOutcomeMessage(timestamp,"PASS",taskId,genId)));		// hack so I can easily find outcome by taskId
		trialStat.setAllTrialsPASS(trialStat.getAllTrialsPASS()+1);		
	}

	public void trialFAIL(long timestamp, PngTrialContext context) {
		long taskId = context.getCurrentTask().getTaskId();
		long genId = context.getCurrentTask().getGenId();
		enqueue(timestamp,"TrialOutcome",PngTrialOutcomeMessage
				.toXml(new PngTrialOutcomeMessage(timestamp,"FAIL",taskId,genId)));
//		enqueue(taskId,"TaskIdOutcome",PngTrialOutcomeMessage
//				.toXml(new PngTrialOutcomeMessage(timestamp,"FAIL",taskId,genId)));
		trialStat.setAllTrialsFAIL(trialStat.getAllTrialsFAIL()+1);	
	}

	public void trialBREAK(long timestamp, PngTrialContext context) {
		long taskId = context.getCurrentTask().getTaskId();
		long genId = context.getCurrentTask().getGenId();
		enqueue(timestamp,"TrialOutcome",PngTrialOutcomeMessage
				.toXml(new PngTrialOutcomeMessage(timestamp,"BREAK",taskId,genId)));
//		enqueue(taskId,"TaskIdOutcome",PngTrialOutcomeMessage
//				.toXml(new PngTrialOutcomeMessage(timestamp,"BREAK",taskId,genId)));
		trialStat.setAllTrialsBREAK(trialStat.getAllTrialsBREAK()+1);	
	}

	public void trialNOGO(long timestamp, PngTrialContext context) {
		long taskId = context.getCurrentTask().getTaskId();
		long genId = context.getCurrentTask().getGenId();
		enqueue(timestamp,"TrialOutcome",PngTrialOutcomeMessage
				.toXml(new PngTrialOutcomeMessage(timestamp,"NOGO",taskId,genId)));
//		enqueue(taskId,"TaskIdOutcome",PngTrialOutcomeMessage					// can't do this or it will try to write this msg multiple times (when it re-runs any nogo stimuli) because it uses taskId instead of tstamp!
//				.toXml(new PngTrialOutcomeMessage(timestamp,"NOGO",taskId,genId)));
		trialStat.setAllTrialsNOGO(trialStat.getAllTrialsNOGO()+1);	
	}

	/* (non-Javadoc)
	 * @see org.xper.classic.TrialExperimentMessageDispatcher#initialEyeInFail(long, org.xper.classic.vo.TrialContext)
	 */
	@Override
	public void initialEyeInFail(long timestamp, TrialContext context) {
		// TODO Auto-generated method stub
		super.initialEyeInFail(timestamp, context);
		trialNOGO(timestamp,(PngTrialContext) context);
	}

	/* (non-Javadoc)
	 * @see org.xper.classic.TrialExperimentMessageDispatcher#eyeInHoldFail(long, org.xper.classic.vo.TrialContext)
	 */
	@Override
	public void eyeInHoldFail(long timestamp, TrialContext context) {
		// TODO Auto-generated method stub
		super.eyeInHoldFail(timestamp, context);
		trialNOGO(timestamp,(PngTrialContext) context);
	}

	
	
}

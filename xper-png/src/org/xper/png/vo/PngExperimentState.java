package org.xper.png.vo;

import org.dom4j.Document;
import org.xper.Dependency;
import org.xper.classic.vo.SlideTrialExperimentState;
import org.xper.experiment.TaskDataSource;
import org.xper.eye.EyeTargetSelector;
import org.xper.png.expt.PngExptSpec;
import org.xper.png.util.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Target position and size describe the response window.
 * 
 * @author john
 *
 */
public class PngExperimentState extends SlideTrialExperimentState {
	@Dependency
	EyeTargetSelector targetSelector;
	
	/**
	 * in ms
	 */
	@Dependency
	long timeAllowedForInitialTargetSelection;
	@Dependency
	long requiredTargetSelectionHoldTime;
	@Dependency
	long targetSelectionStartDelay;
	@Dependency
	long timeoutPenaltyDelay = 0;	// -shs, default to zero
	@Dependency
	boolean repeatTrialIfEyeBreak = false;
	
	// JK 25 July
	@Dependency
	long maintainHoldTime;
	
	int correctTrialCount = 1;
	int MaxTrialCount = 3;
	
	// JK 27 July 2018
	//   save animation states for each slide
	ArrayList<Boolean>animationStates = new ArrayList<Boolean>();
	
	public PngExperimentState () {
	}
	
	
	
	public int getNumSlidesPerTrial() {
		return PngExptSpec.fromXml(getCurrentTask().getStimSpec()).getStimObjIdCount();
		
	}

	public int getCorrectTrialCount() {
		return correctTrialCount;
	}
	
	// JK 21 April 2016
	// dynamic reward needs to keep track of trial performance
	// this  should / could be a lot smarter ...
	public void countCorrectTrial(boolean isCorrect){
		if(isCorrect){
			if(correctTrialCount < MaxTrialCount){
				correctTrialCount += 1;
			}
		}
		else {
			correctTrialCount = 1;
		}
		
		//
		System.out.println("PngExperimentState:countCorrectTrial() : correctTrialCount == " + correctTrialCount);
		
	}
	


	public EyeTargetSelector getTargetSelector() {
		return targetSelector;
	}

	public void setTargetSelector(EyeTargetSelector targetSelector) {
		this.targetSelector = targetSelector;
	}

	public long getTimeAllowedForInitialTargetSelection() {
		return timeAllowedForInitialTargetSelection;
	}

	public void setTimeAllowedForInitialTargetSelection(
			long timeAllowedForInitialTargetSelection) {
		this.timeAllowedForInitialTargetSelection = timeAllowedForInitialTargetSelection;
	}

	public long getRequiredTargetSelectionHoldTime() {
		return requiredTargetSelectionHoldTime;
	}

	public void setRequiredTargetSelectionHoldTime(
			long requiredTargetSelectionHoldTime) {
		this.requiredTargetSelectionHoldTime = requiredTargetSelectionHoldTime;
	}

	public long getTargetSelectionStartDelay() {
		return targetSelectionStartDelay;
	}

	public void setTargetSelectionStartDelay(long targetSelectionStartDelay) {
		this.targetSelectionStartDelay = targetSelectionStartDelay;
	}

	public long getTimeoutPenaltyDelay() { // -shs
		return timeoutPenaltyDelay;
	}
	
	public void setTimeoutPenaltyDelay(Integer timeoutPenaltyDelay) {	// -shs
		this.timeoutPenaltyDelay = timeoutPenaltyDelay;
	}
	
	public boolean isRepeatTrialIfEyeBreak() {
		return repeatTrialIfEyeBreak;
	}

	public void setRepeatTrialIfEyeBreak(boolean repeatTrialIfEyeBreak) {
		this.repeatTrialIfEyeBreak = repeatTrialIfEyeBreak;
	}
	
	// JK 25 July
	public long getMaintainHoldTime() {
		return maintainHoldTime;
	}

	public void setMaintainHoldTime(long maintainHoldTime) {
		this.maintainHoldTime = maintainHoldTime;
	}
	
	
	public void setAnimationStates(PngDbUtil dbUtil) {
		animationStates.clear();
		long taskId = this.getCurrentTask().getTaskId();
		List<String> typeStrs = dbUtil.readAllStimTypesByTask(taskId);
//		System.out.println(typeStrs);
		
		for(String str : typeStrs) {
			if(str.contains("ANIMATE")) {
				animationStates.add(Boolean.TRUE);
			}else if (str.contains("BALL")){
				animationStates.add(Boolean.TRUE);
			}else {
				animationStates.add(Boolean.FALSE);
			}
		}
		
//		for(Boolean b : animationStates) {
//			System.out.println("JK 83832 setAnimationStates() : state == " + b.toString());
//		}
	}
	
	public void setAnimationForSlide(int slideNum) {
		super.setAnimation(this.animationStates.get(slideNum));
	}
	
		
	
}

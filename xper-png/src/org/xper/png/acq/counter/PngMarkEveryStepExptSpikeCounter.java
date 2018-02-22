package org.xper.png.acq.counter;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xper.Dependency;
import org.xper.acq.counter.MarkEveryStepExperimentSpikeCounter;
import org.xper.acq.counter.MarkEveryStepTaskSpikeDataEntry;
import org.xper.acq.counter.TrialStageData;
import org.xper.db.vo.GenerationTaskDoneList;
import org.xper.db.vo.TaskDoneEntry;
import org.xper.png.expt.PngExptSpec;
import org.xper.png.util.PngDbUtil;
import org.xper.png.util.PngMathUtil;


public class PngMarkEveryStepExptSpikeCounter extends MarkEveryStepExperimentSpikeCounter {	
	@Dependency
	PngDbUtil dbUtil;
	
	// added by SHS for fake spikes:
	
	public SortedMap<Long, MarkEveryStepTaskSpikeDataEntry> getFakeTaskSpikeByGeneration(long genId) {
		GenerationTaskDoneList taskDone = dbUtil.readTaskDoneByGeneration(genId);
		return getFakeTaskSpike(taskDone.getDoneTasks());
	}
	
	public SortedMap<Long, MarkEveryStepTaskSpikeDataEntry> getFakeTaskSpike(List<TaskDoneEntry> tasks) {
		SortedMap<Long, MarkEveryStepTaskSpikeDataEntry> ret = new TreeMap<Long, MarkEveryStepTaskSpikeDataEntry>();
		if (tasks.size() <= 0) return ret;

		for (TaskDoneEntry task : tasks) {
			long taskId = task.getTaskId();
			MarkEveryStepTaskSpikeDataEntry spike = new MarkEveryStepTaskSpikeDataEntry();
			spike.setTaskId(taskId);
			
			// get number of stims in a task/trial:
			PngExptSpec trialSpec = PngExptSpec.fromXml(dbUtil.readStimSpec(dbUtil.getStimIdByTaskId(taskId)).getSpec());
			int numStims = trialSpec.getStimObjIdCount();
			
			// for each stim add spike info:
			for (int n=0;n<numStims;n++) { // *** I'm assuming there are only numStim epochs and the first one is 0 --- NO, I THINK THIS IS WRONG, MANY EPOCHS (fixation, stimuli, ISIs, target, etc)
				spike.addSpikePerSec(PngMathUtil.randRange(30, 1));	// add random spike rate
				
				TrialStageData d = new TrialStageData();
				spike.addTrialStageData(d);
			}
			
			ret.put(taskId, spike);
		}

		return ret;
	}
	
	public SortedMap<Long, MarkEveryStepTaskSpikeDataEntry> getTaskSpikeByGeneration(
			String prefix, long runNum, long genNum, int dataChan) {
		return getTaskSpikeByGeneration(prefix, runNum, genNum, dataChan, Integer.MAX_VALUE);
	}
	
	public SortedMap<Long, MarkEveryStepTaskSpikeDataEntry> getTaskSpikeByGeneration(
			String prefix, long runNum, long genNum, int dataChan, int maxStages) {
		
		GenerationTaskDoneList taskDone = dbUtil.readTaskDoneByFullGen(prefix, runNum, genNum);
		return getTaskSpike(taskDone.getDoneTasks(), dataChan, maxStages);
	}
	
}

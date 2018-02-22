package org.xper.png.expt.generate;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.xper.Dependency;
import org.xper.acq.counter.MarkEveryStepTaskSpikeDataEntry;
import org.xper.db.vo.GenerationInfo;
import org.xper.drawing.renderer.AbstractRenderer;
import org.xper.exception.InvalidAcqDataException;
import org.xper.exception.NoMoreAcqDataException;
import org.xper.exception.VariableNotFoundException;
import org.xper.png.acq.counter.PngMarkEveryStepExptSpikeCounter;
import org.xper.png.drawing.preview.PNGmaker;
import org.xper.png.drawing.stimuli.PngObjectSpec;
import org.xper.png.expt.PngExptSpec;
import org.xper.png.expt.PngExptSpecGenerator;
import org.xper.png.parsedata.DataObject;
import org.xper.png.util.ExpLogMessage;
import org.xper.png.util.PngDbUtil;
import org.xper.png.util.PngIOUtil;
import org.xper.time.TimeUtil;


public class PngRandomGeneration {
	@Dependency
	PngDbUtil dbUtil;
	@Dependency
	TimeUtil globalTimeUtil;
	@Dependency
	AbstractRenderer renderer;			
	@Dependency
	PngExptSpecGenerator generator;	
	@Dependency
	int taskCount;
	

	// ---- global variables:
	boolean realExp = true;							// controls output to file of expt info
	boolean saveThumbnails = true;								// do you want to save stim thumbnails?
	
	PNGmaker pngMaker;									// set of methods for creating and saving thumbnails
	
	String prefix = "";
	long runNum = 1;
	long genNum = 1;
	
	static public enum TrialType { GA };				// trial types
	TrialType trialType;
	
	int GA_numTrials;
	
	public void generateGA() {	
				
		System.out.println("Generating GA run... ");
		trialType = TrialType.GA;
		generator.setTrialType(trialType);
				
		saveThumbnails = true;
		
		GA_numTrials = dbUtil.readReadyGenerationInfo().getTaskCount();

		writeExptStart();
		
		genNum = getGenNum();
		runNum = getRunNum();
		prefix = getPrefix();
		
		// start by asking if there is a previous ga run to be continued
		// if so, run createNextGen for that prefix and ga run
		// else run createFirstGen with a new prefix and incremented ga run
		
		char cont = 'y';
		
		while (cont == 'y') {
			int c = PngIOUtil.promptInteger("Enter GA run number to continue. Else enter '0'");
			if (c==0) {
				genNum = 1;
				runNum = runNum + 1;
				prefix = "getPrefix";
				createFirstGen();
			} else {
				runNum = c;
				genNum = getGenId(runNum) + 1;
				prefix = getPrefix(runNum);
				createNextGen();
			}
				
			writeExptGenDone();
			System.out.println("\nGeneration has ended.");
			
			cont = PngIOUtil.prompt("Continue recording?");
		}
		
		writeExptStop();
	}
	
	void createFirstGen() {
		
		// -- create stimuli
		List<Long> blankStimObjIds = new ArrayList<Long>();
		List<Long> stimObjIds = new ArrayList<Long>();	// track stimObjIds for all stimuli created
		
		// make blank stims: (create one blank stimulus for each lineage, if just to have a better baseline measure)
		for (int n=0;n<PngGAParams.GA_numLineages;n++) {
			blankStimObjIds.add(generator.generateBlankStim(prefix, runNum, genNum, n));	
		}
				
		// make random stims:		
		for (int n=0;n<PngGAParams.GA_numLineages;n++) {
			for (int k=0;k<PngGAParams.GA_numNonBlankStimsPerLin;k++) {
				stimObjIds.add(generator.generateRandStim(prefix, runNum, genNum, n, k));
			}
		}
		
		// DO BLENDER CALL

		// create PNG thumbnails (not for blanks)
		if (saveThumbnails) {
//			pngMaker.MakeFromIds(stimObjIds);
		}
		
		// now add blanks
		stimObjIds.addAll(blankStimObjIds);
		
		// create trial structure, populate stimspec, write task-to-do
		createGATrialsFromStimObjs(stimObjIds);
		
		// write updated global genId and number of trials in this generation to db:
		dbUtil.updateReadyGenerationInfo(prefix, runNum, genNum, GA_numTrials);
		
		// get acq info and put into db:
		getSpikeResponses();
		
	}
	
	
	
	void createNextGen() {		
		List<Long> blankStimObjIds = new ArrayList<Long>();		
		List<Long> stimObjIds = new ArrayList<Long>();

		// make blank stims:		
		for (int n=0;n<PngGAParams.GA_numLineages;n++) {
			blankStimObjIds.add(generator.generateBlankStim(prefix, runNum, genNum, n));
		}
		
		// make random stims:		
		for (int n=0;n<PngGAParams.GA_numLineages;n++) {
			for (int k=0;k<PngGAParams.GA_morph_numNewStimPerLin;k++) {
				stimObjIds.add(generator.generateRandStim(prefix, runNum, genNum, n, k));
			}
		}
		
		// make offspring stims:
		// create stimulus/FR structure, sort by FR, randomly choose parents from FR quintiles
		// create morphed offspring from parents
		int numDecendantObjs = PngGAParams.GA_numNonBlankStimsPerLin-PngGAParams.GA_morph_numNewStimPerLin;
		
		// for each non-blank stimulus shown previously, find lineage and avgFR, then add to appropriate list:
		Map<Long, Double> stimObjId2avgFR_lin1 = new HashMap<Long, Double>();
		Map<Long, Double> stimObjId2avgFR_lin2 = new HashMap<Long, Double>();
		
		List<Long> allStimObjIds = dbUtil.readAllStimIdsForRun(prefix,runNum,genNum);
		
		DataObject data;
		long stimObjId;
		for (int n=0;n<allStimObjIds.size();n++) {
			stimObjId = allStimObjIds.get(n);
			data = DataObject.fromXml(dbUtil.readStimSpec_data(stimObjId).getSpec());
			
			if (data.getLineage() == 0) {	// first lineage 
				stimObjId2avgFR_lin1.put(stimObjId, data.getAvgFR());
			} else {						// second lineage
				stimObjId2avgFR_lin2.put(stimObjId, data.getAvgFR());
			}
		}
		

		// TODO: write a parent selection class that chooses decendants
		
		// choose stims top morph:
			// which fitness method? 	1 = using fixed probabilities by FR quintile
			// 							2 = using distance in firing rate space
		int fitnessMethod = 1;
		List<Long> stimsToMorph_lin1 = GAMaths.chooseStimsToMorph(stimObjId2avgFR_lin1,numDecendantObjs,fitnessMethod); 
		List<Long> stimsToMorph_lin2 = GAMaths.chooseStimsToMorph(stimObjId2avgFR_lin2,numDecendantObjs,fitnessMethod);
		
		System.out.println("lin1: " + stimsToMorph_lin1);
		System.out.println("lin2: " + stimsToMorph_lin2);
		
		// create morphed stimuli:
		for (int n=0;n<numDecendantObjs;n++) {
			stimObjIds.add(generator.generateMorphStim(prefix, runNum, genNum, 0,stimsToMorph_lin1.get(n),n));
			stimObjIds.add(generator.generateMorphStim(prefix, runNum, genNum, 1,stimsToMorph_lin2.get(n),n));
		}
		
		// DO BLENDER CALL
	
		if (saveThumbnails) {
//			pngMaker.MakeFromIds(stimObjIds);
		}
		
		// add blanks
		stimObjIds.addAll(blankStimObjIds);	

		// create trial structure, populate stimspec, write task-to-do
		createGATrialsFromStimObjs(stimObjIds);

		// write updated global genId and number of trials in this generation to db:
		dbUtil.updateReadyGenerationInfo(prefix, runNum, genNum, GA_numTrials);
		
		// get acq info and put into db:
		getSpikeResponses();

	}
	
	void createGATrialsFromStimObjs(List<Long> stimObjIds) {
		// -- create trial structure, populate stimspec, write task-to-do
		
		// first, log stimobjids for each genid:
//		dbUtil.writeStimObjIdsForEachGenId(genId, stimObjIds);
		
		// stim repetitions:
		List<Long> allStimObjIdsInGen = new ArrayList<Long>();
		for (int n=0;n<PngGAParams.GA_numRepsPerStim;n++) {
			allStimObjIdsInGen.addAll(stimObjIds);
		}

		// shuffle stimuli:
		Collections.shuffle(allStimObjIdsInGen);

		// create trials using shuffled stimuli:
		long taskId;
		int stimCounter = 0;

		for (int n=0;n<GA_numTrials;n++) {
			taskId = globalTimeUtil.currentTimeMicros();

			// create trialspec using sublist and taskId
			int endIdx = stimCounter + PngGAParams.GA_numStimsPerTrial;
			while (endIdx>allStimObjIdsInGen.size()) endIdx--;	// this makes sure there's no out index of bounds exception

			String spec = generator.generateGATrialSpec(allStimObjIdsInGen.subList(stimCounter,endIdx));

			if(n==0)
				writeExptFirstTrial(taskId);
			else if(n==GA_numTrials-1)
				writeExptLastTrial(taskId);
			
			// save spec and tasktodo to db
			dbUtil.writeStimSpec(taskId, spec);
			dbUtil.writeTaskToDo(taskId, taskId, -1, genNum);

			stimCounter = endIdx;
		}
	}
	
	public void getSpikeResponses() {
		
		long lastTrialToDo;
		long lastTrialDone;

		// first, wait for some time to make sure previous 'TaskToDo's are written to the db (the stimuli need to be presented anyway):
		try
		{	Thread.sleep(8000);	}
		catch (Exception e) {System.out.println(e);}
		
		// Wait for spike data collection to be completed:	
		int counter = 0;
		System.out.print("Waiting for ACQ process.");
		while (true)
		{
			lastTrialToDo = dbUtil.readTaskToDoMaxId();	// move this outside loop?
			lastTrialDone = dbUtil.readTaskDoneCompleteMaxId();
			if ( counter % 20 == 0)
				System.out.print(".");
			counter++;
			if ( lastTrialToDo == lastTrialDone) { // Completed the tasks in this generation:
				try
				{	Thread.sleep(3000);	}
				catch (Exception e) {System.out.println(e);}
				System.out.println();
				break;
			}
			try
			{	Thread.sleep(300);	}
			catch (Exception e) {System.out.println(e);}
		}		

		// obtain spike data:
		long taskId;

		//MarkStimExperimentSpikeCounter spikeCounter = new MarkStimExperimentSpikeCounter();
		PngMarkEveryStepExptSpikeCounter spikeCounter = new PngMarkEveryStepExptSpikeCounter(); 
		spikeCounter.setDbUtil(dbUtil);

		try{
			// get spike data for all trials:
			SortedMap<Long, MarkEveryStepTaskSpikeDataEntry> spikeEntry;
			spikeEntry = spikeCounter.getTaskSpikeByGeneration(prefix,runNum,genNum, 0);
			
			// for each trial done in a generation:
				// get blank FRs:
			List<Double> blankFRs = new ArrayList<Double>();
			for (SortedMap.Entry<Long, MarkEveryStepTaskSpikeDataEntry> entry : spikeEntry.entrySet())
			{
				MarkEveryStepTaskSpikeDataEntry ent = entry.getValue();				
				taskId = ent.getTaskId();
				
				// get TrialSpec:
				PngExptSpec trialSpec = PngExptSpec.fromXml(dbUtil.getSpecByTaskId(taskId).getSpec());
				
				// for each stimObj in the trial:
				long stimObjId;
				PngObjectSpec spec;
				int entIdx;				// MarkEveryStepTaskSpikeEntry gives the following epochs:
										//    [ fixation_pt_on, eye_in_succeed, stim, isi, ... (repeat x numStims), done_last_isi_to_task_end ]
										//    so to index the stimuli we skip the first 2 and do every other for as many stims as we present in a trial

				// first get blank stim FR data:
				for (int n=0;n<trialSpec.getStimObjIdCount();n++) {
					stimObjId = trialSpec.getStimObjId(n);
					spec = PngObjectSpec.fromXml(dbUtil.readStimSpec_java(stimObjId).getSpec());
					
					if ( spec.getStimType().compareTo("BLANK") == 0) {
						entIdx = 2*n+2;
						blankFRs.add(ent.getSpikePerSec(entIdx)); 
					}
				}
			}
			
			for (SortedMap.Entry<Long, MarkEveryStepTaskSpikeDataEntry> entry : spikeEntry.entrySet())
			{
				MarkEveryStepTaskSpikeDataEntry ent = entry.getValue();				
				taskId = ent.getTaskId();

				System.out.println("Entering spike info for trial: " + taskId);
				
				// get TrialSpec:
				PngExptSpec trialSpec = PngExptSpec.fromXml(dbUtil.getSpecByTaskId(taskId).getSpec());
				
				// for each stimObj in the trial get FR data for all stims and save:
				long stimObjId;
				DataObject data;
				int entIdx;

				for (int n=0;n<trialSpec.getStimObjIdCount();n++) {
					stimObjId = trialSpec.getStimObjId(n);
					data = DataObject.fromXml(dbUtil.readStimSpec_data(stimObjId).getSpec());
					
					// add acq info:					
					entIdx = 2*n+2;
					data.addTaskDoneId(taskId);
					data.setSampleFrequency(ent.getSampleFrequency());
					data.addSpikesPerSec(ent.getSpikePerSec(entIdx));
					data.setBkgdSpikesPerSec(blankFRs);					// add blank FR data
					data.addTrialStageData(ent.getTrialStageData(entIdx));
					
					// resave data:
					dbUtil.updateStimObjData(stimObjId, data.toXml());
				}
			}	
		} catch(InvalidAcqDataException ee) {
			ee.printStackTrace();
		} catch(NoMoreAcqDataException ee) {
			ee.printStackTrace();
		}
	}

	private String getPrefix() {
		try {
			prefix = dbUtil.readReadyGenerationInfo().getPrefix();
			return prefix;
		} catch (VariableNotFoundException e) {
			System.out.println("Could not find genId in database. Writing value of 0.");
			dbUtil.writeReadyGenerationInfo(new GenerationInfo());
			return "";
		}
	}
	private long getRunNum() {
		try {
			runNum = dbUtil.readReadyGenerationInfo().getRunNum();
			return runNum;
		} catch (VariableNotFoundException e) {
			System.out.println("Could not find genId in database. Writing value of 0.");
			dbUtil.writeReadyGenerationInfo(new GenerationInfo());
			return 1;
		}
	}
	private long getGenNum() {
		try {
			genNum = dbUtil.readReadyGenerationInfo().getGenId();
			return genNum;
		} catch (VariableNotFoundException e) {
			System.out.println("Could not find genId in database. Writing value of 0.");
			dbUtil.writeReadyGenerationInfo(new GenerationInfo());
			return 1;
		}
	}
	
	private String getPrefix(long runNum) {
		try {
			prefix = dbUtil.readPrefixForRunNum(runNum);
			return prefix;
		} catch (VariableNotFoundException e) {
			System.out.println("Could not find genId in database. Writing value of 0.");
			dbUtil.writeReadyGenerationInfo(new GenerationInfo());
			return "";
		}
	}
	private long getGenId(long runNum) {
		try {
			genNum = dbUtil.readGenIdForRunNum(runNum);
			return genNum;
		} catch (VariableNotFoundException e) {
			System.out.println("Could not find genId in database. Writing value of 0.");
			dbUtil.writeReadyGenerationInfo(new GenerationInfo());
			return 1;
		}
	}
	
	private void writeExptStart() {
		writeExptLogMsg("START");
	}
	
	private void writeExptStop() {
		writeExptLogMsg("STOP");
	}
	
	private void writeExptGenDone() {
		writeExptLogMsg("GEN_DONE");
	}
	
	private void writeExptFirstTrial(Long trialId) {
		writeExptLogMsg("FIRST_TRIAL=" + trialId);
		dbUtil.writeDescriptiveFirstTrial(trialId);
	}
	
	private void writeExptLastTrial(Long trialId) {
		writeExptLogMsg("LAST_TRIAL=" + trialId);
		dbUtil.writeDescriptiveLastTrial(trialId);
	}

	
	private void writeExptLogMsg(String status) {
		// write ExpLog message
		long tstamp = globalTimeUtil.currentTimeMicros();
		ExpLogMessage msg = new ExpLogMessage(status,trialType.toString(),prefix,runNum,genNum,tstamp);
		dbUtil.writeExpLog(tstamp,ExpLogMessage.toXml(msg));
	}
	
	// ---------------------------
	// ---- Getters & Setters ----
	// ---------------------------
	
	public PngDbUtil getDbUtil() {
		return dbUtil;
	}

	public void setDbUtil(PngDbUtil dbUtil) {
		this.dbUtil = dbUtil;
		pngMaker = new PNGmaker(dbUtil);
	}

	public TimeUtil getGlobalTimeUtil() {
		return globalTimeUtil;
	}

	public void setGlobalTimeUtil(TimeUtil globalTimeUtil) {
		this.globalTimeUtil = globalTimeUtil;
	}

	public PngExptSpecGenerator getGenerator() {
		return generator;
	}

	public void setGenerator(PngExptSpecGenerator generator) {
		this.generator = generator;
	}
	
	public AbstractRenderer getRenderer() {
		return renderer;
	}

	public void setRenderer(AbstractRenderer renderer) {
		this.renderer = renderer;
	}
	
	public int getTaskCount() {
		return taskCount;
	}

	public void setTaskCount(int taskCount) {
		this.taskCount = taskCount;
	}
}

package org.xper.png.expt.generate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import javax.vecmath.Point3d;

import org.xper.Dependency;
import org.xper.acq.counter.MarkEveryStepTaskSpikeDataEntry;
import org.xper.db.vo.GenerationInfo;
import org.xper.drawing.renderer.AbstractRenderer;
import org.xper.exception.InvalidAcqDataException;
import org.xper.exception.NoMoreAcqDataException;
import org.xper.exception.VariableNotFoundException;
import org.xper.png.acq.counter.PngMarkEveryStepExptSpikeCounter;
import org.xper.png.drawing.preview.PNGmaker;
import org.xper.png.drawing.stick.MStickSpec;
import org.xper.png.drawing.stimuli.BlenderSpec;
import org.xper.png.drawing.stimuli.AldenSpec_class;
import org.xper.png.drawing.stimuli.EnviroSpec_class;
import org.xper.png.drawing.stimuli.PngObjectSpec;
import org.xper.png.expt.PngExptSpec;
import org.xper.png.expt.PngExptSpecGenerator;
import org.xper.png.parsedata.DataObject;
import org.xper.png.util.BlenderRunnable;
import org.xper.png.util.ExpLogMessage;
import org.xper.png.util.PngDbUtil;
import org.xper.png.util.PngIOUtil;
import org.xper.png.util.PngMapUtil;
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
	
	boolean doSaveThumbnails = true;
	boolean useFakeSpikes = true;
	
	PNGmaker pngMaker;
	
	String prefix = "";
	long runNum = 1;
	long genNum = 1;
	
	static public enum TrialType { GA };
	TrialType trialType;
		
	public void generateGA() {	
		trialType = TrialType.GA;
		generator.setTrialType(trialType);
		
		doSaveThumbnails = true;
		
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
				DateFormat df = new SimpleDateFormat("yyMMdd");
				prefix = df.format(new Date()); 
				dbUtil.writeCurrentDescriptivePrefixAndGen(globalTimeUtil.currentTimeMicros(), prefix, runNum, genNum);
				createFirstGen();
			} else {
				runNum = c;
				genNum = getGenId(runNum) + 1;
				prefix = getPrefix(runNum);
				dbUtil.writeCurrentDescriptivePrefixAndGen(globalTimeUtil.currentTimeMicros(), prefix, runNum, genNum);
				
				String progressType = PngIOUtil.promptString("To continue GA, enter 'n'. To proceed with post-hoc, enter 'c' (composite), 's' (stability), 'a' (animacy), or 'd' (density)");
				String postHoc;

				switch (progressType) {
				case "n":
					createNextGen();
					break;
				case "c":
					postHoc = "COMPOSITE";
					createPHcomposite();
					break;
				case "s":
					postHoc = "STABILITY";
					createPHgeneric(postHoc);
					break;
				case "a":
					postHoc = "ANIMACY";
					createPHanimacy();
					break;
				case "d":
					postHoc = "DENSITY";
					createPHgeneric(postHoc);
					break;
				}
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
		System.out.println("Blank stimuli added.");
				
		// make random stims:		
		for (int n=0;n<PngGAParams.GA_numLineages;n++) {
			for (int k=0;k<PngGAParams.GA_numNonBlankStimsPerLin;k++) {
				stimObjIds.add(generator.generateRandStim(prefix, runNum, genNum, n, k));
				System.out.println("Lineage " + n + ": Generating and saving stimulus " + k);
			}
		}
		
		// create PNG thumbnails (not for blanks)
		if (doSaveThumbnails) {
			System.out.println("Saving PNGs.");
			pngMaker.MakeFromIds(stimObjIds);
		}
		
		/* 
		 * This python script is called within blender.
		 * It reads the latest descriptive ID from the DB
		 * and makes a list of stimuli to create. It then does
		 * a parallel blenderrender call to save all the blenderspec
		 * to stimobjdata. Finally, it does a cluster call
		 * to render png images. When done, it rsyncs all pngs
		 * to the rig and to ecpc31.
		 */
		
		BlenderRunnable blenderRunner = new BlenderRunnable("/Users/ecpc31/Dropbox/Blender/ProgressionClasses/randomSpec.py");
//		BlenderRunnable blenderRunner = new BlenderRunnable("/Users/alexandriya/Dropbox/Blender/ProgressionClasses/randomSpec.py");
		blenderRunner.run();
		
		// now add blanks
		stimObjIds.addAll(blankStimObjIds);
		
		// create trial structure, populate stimspec, write task-to-do
		System.out.println("Creating trial spec for this generation.");
		createGATrialsFromStimObjs(stimObjIds);
		
		// write updated global genId and number of trials in this generation to db:
		dbUtil.updateReadyGenerationInfo(prefix, runNum, genNum, PngGAParams.GA_numTasks);
		
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
		System.out.println("Blank stimuli added.");
		
		// make random stims:		
		for (int n=0;n<PngGAParams.GA_numLineages;n++) {
			for (int k=0;k<PngGAParams.GA_morph_numNewStimPerLin;k++) {
				stimObjIds.add(generator.generateRandStim(prefix, runNum, genNum, n, k));
				System.out.println("Lineage " + n + ": Generating and saving random stimulus " + k);
			}
		}
		
		/* 
		 * This python script is called within blender.
		 * It reads the latest descriptive ID from the DB
		 * and makes a list of stimuli to create. It then does
		 * a parallel blenderrender call to save all the blenderspec
		 * to stimobjdata. Finally, it does a cluster call
		 * to render png images. When done, it rsyncs all pngs
		 * to the rig and to ecpc31.
		 */
		
		BlenderRunnable blenderRunner = new BlenderRunnable("/Users/ecpc31/Dropbox/Blender/ProgressionClasses/randomSpec.py",stimObjIds);
//		BlenderRunnable blenderRunner = new BlenderRunnable("/Users/alexandriya/Dropbox/Blender/ProgressionClasses/randomSpec.py",stimObjIds);
		blenderRunner.run();
		
		System.out.println("Calculating fitness and selecting parents.");
		int numDescendantObjs = PngGAParams.GA_numNonBlankStimsPerLin-PngGAParams.GA_morph_numNewStimPerLin;
		
		// for each non-blank stimulus shown previously, find lineage and add z-score and id to appropriate map
		Map<Long, Double> stimObjId2FRZ_lin1 = new HashMap<Long, Double>();
		Map<Long, Double> stimObjId2FRZ_lin2 = new HashMap<Long, Double>();

		for (int gen=1;gen<genNum;gen++) {
			List<Long> allStimObjIds = dbUtil.readAllStimIdsForRun(prefix,runNum,gen);

			DataObject data;
			long stimObjId;

			for (int n=0;n<allStimObjIds.size();n++) {

				stimObjId = allStimObjIds.get(n);
				PngObjectSpec pngSpecTemp = PngObjectSpec.fromXml(dbUtil.readStimSpec_java(stimObjId).getSpec());

				if (!pngSpecTemp.getStimType().equals("BLANK")) {
					data = DataObject.fromXml(dbUtil.readStimSpec_data(stimObjId).getSpec());

					if (data.getLineage() == 0) {
						Double zScore = data.getAvgFRminusBkgd()/data.getStdFRplusBkgd();
						stimObjId2FRZ_lin1.put(stimObjId, zScore);
					}
					
					else {
						Double zScore = data.getAvgFRminusBkgd()/data.getStdFRplusBkgd();
						stimObjId2FRZ_lin2.put(stimObjId, zScore);
					}
				}
			}
		}
		
		// choose stims top morph:
			// which fitness method? 	1 = using fixed probabilities by FR quintile
			// 							2 = using distance in firing rate space
		int fitnessMethod = 1;
		
		List<Long> stimsToMorph_lin1 = GAMaths.chooseStimsToMorph(stimObjId2FRZ_lin1,numDescendantObjs,fitnessMethod); 
		List<Long> stimsToMorph_lin2 = GAMaths.chooseStimsToMorph(stimObjId2FRZ_lin2,numDescendantObjs,fitnessMethod);
		
		System.out.println("lin1: " + stimsToMorph_lin1);
		System.out.println("lin2: " + stimsToMorph_lin2);
		
//		// check generation designations of stimuli chosen to morph
//		for (int n=0;n<stimsToMorph_lin1.size();n++) {
//			System.out.println("LIN1 "+stimsToMorph_lin1.get(n)+" "+dbUtil.readDescriptiveIdFromStimObjId(stimsToMorph_lin1.get(n)));
//			System.out.println("LIN2 "+stimsToMorph_lin2.get(n)+" "+dbUtil.readDescriptiveIdFromStimObjId(stimsToMorph_lin2.get(n)));
//		}
		
		List<Long> stimsToMorph = new ArrayList<Long>();
		List<Long> stimsToRestore = new ArrayList<Long>();
		List<String> tempArray = new ArrayList<String>();
		Long whichStim;
		
		// create morphed stimuli:
		for (int n=0;n<numDescendantObjs;n++) {
			
			tempArray = generator.generateMorphStim(prefix, runNum, genNum, 0,stimsToMorph_lin1.get(n),n+PngGAParams.GA_morph_numNewStimPerLin);
			System.out.println("Lineage 0: Generating and saving morphed stimulus " + n);
			whichStim = Long.parseLong(tempArray.get(0));
			stimObjIds.add(whichStim);
			
			if (tempArray.get(1) == "NewBSpec")
				stimsToMorph.add(whichStim);

			else 
				stimsToRestore.add(whichStim);
			
			tempArray = generator.generateMorphStim(prefix, runNum, genNum, 1,stimsToMorph_lin2.get(n),n+PngGAParams.GA_morph_numNewStimPerLin);
			System.out.println("Lineage 1: Generating and saving morphed stimulus " + n);
			whichStim = Long.parseLong(tempArray.get(0));
			stimObjIds.add(whichStim);
			
			if (tempArray.get(1) == "NewBSpec")
				stimsToMorph.add(whichStim);

			else 
				stimsToRestore.add(whichStim);
		}
		
		/* 
		 * This python script is called within blender.
		 * It reads the latest descriptive ID from the DB
		 * and makes a list of stimuli to create. It then does
		 * a parallel blenderrender call to save all the blenderspec
		 * to stimobjdata. Finally, it does a cluster call
		 * to render png images. When done, it rsyncs all pngs
		 * to the rig and to ecpc31.
		 */
		
		System.out.println(stimsToMorph+" MORPH");
		System.out.println(stimsToRestore+" RESTORE");
		
		BlenderRunnable blenderRunnerMorph = new BlenderRunnable("/Users/ecpc31/Dropbox/Blender/ProgressionClasses/morphSpec.py",stimsToMorph);
//		BlenderRunnable blenderRunnerMorph = new BlenderRunnable("/Users/alexandriya/Dropbox/Blender/ProgressionClasses/morphSpec.py",stimsToMorph);
		blenderRunnerMorph.run();
			
		BlenderRunnable blenderRunnerRestore = new BlenderRunnable("/Users/ecpc31/Dropbox/Blender/ProgressionClasses/restoreMorphSpec.py",stimsToRestore);
//		BlenderRunnable blenderRunnerRestore = new BlenderRunnable("/Users/alexandriya/Dropbox/Blender/ProgressionClasses/restoreMorphSpec.py",stimsToRestore);
		blenderRunnerRestore.run();
		
		if (doSaveThumbnails) {
			System.out.println("Saving PNGs.");
			pngMaker.MakeFromIds(stimObjIds);
		}
		
		// add blanks
		stimObjIds.addAll(blankStimObjIds);	

		// create trial structure, populate stimspec, write task-to-do
		System.out.println("Creating trial spec for this generation.");
		createGATrialsFromStimObjs(stimObjIds);

		// write updated global genId and number of trials in this generation to db:
		dbUtil.updateReadyGenerationInfo(prefix, runNum, genNum, PngGAParams.GA_numTasks);
		
		// get acq info and put into db:
		getSpikeResponses();

	}
	
	void createPHcomposite() {
		
		List<Long> blankStimObjIds = new ArrayList<Long>();	
		List<Long> stimObjIds = new ArrayList<Long>();
		
		// find highest-responding environment features for incorporation into composite
		System.out.println("Assembling characteristics for composite construction.");
		Map<Double, List<Double>> horizonTilt_maxFinder_lin1 = new HashMap<Double, List<Double>>();
		Map<Double, List<Double>> horizonSlant_maxFinder_lin1 = new HashMap<Double, List<Double>>();
		Map<String, List<Double>> horizonMaterial_maxFinder_lin1 = new HashMap<String, List<Double>>();
		Map<Double, List<Double>> distance_maxFinder_lin1 = new HashMap<Double, List<Double>>();
		Map<String, List<Double>> structureMaterial_maxFinder_lin1 = new HashMap<String, List<Double>>();
		Map<Boolean, List<Double>> floor_maxFinder_lin1 = new HashMap<Boolean, List<Double>>();
		Map<Boolean, List<Double>> ceiling_maxFinder_lin1 = new HashMap<Boolean, List<Double>>();
		Map<Boolean, List<Double>> wallL_maxFinder_lin1 = new HashMap<Boolean, List<Double>>();
		Map<Boolean, List<Double>> wallR_maxFinder_lin1 = new HashMap<Boolean, List<Double>>();
		Map<Boolean, List<Double>> wallB_maxFinder_lin1 = new HashMap<Boolean, List<Double>>();
		Map<Point3d, List<Double>> sun_maxFinder_lin1 = new HashMap<Point3d, List<Double>>();
		
		Map<Double, List<Double>> horizonTilt_maxFinder_lin2 = new HashMap<Double, List<Double>>();
		Map<Double, List<Double>> horizonSlant_maxFinder_lin2 = new HashMap<Double, List<Double>>();
		Map<String, List<Double>> horizonMaterial_maxFinder_lin2 = new HashMap<String, List<Double>>();
		Map<Double, List<Double>> distance_maxFinder_lin2 = new HashMap<Double, List<Double>>();
		Map<String, List<Double>> structureMaterial_maxFinder_lin2 = new HashMap<String, List<Double>>();
		Map<Boolean, List<Double>> floor_maxFinder_lin2 = new HashMap<Boolean, List<Double>>();
		Map<Boolean, List<Double>> ceiling_maxFinder_lin2 = new HashMap<Boolean, List<Double>>();
		Map<Boolean, List<Double>> wallL_maxFinder_lin2 = new HashMap<Boolean, List<Double>>();
		Map<Boolean, List<Double>> wallR_maxFinder_lin2 = new HashMap<Boolean, List<Double>>();
		Map<Boolean, List<Double>> wallB_maxFinder_lin2 = new HashMap<Boolean, List<Double>>();
		Map<Point3d, List<Double>> sun_maxFinder_lin2 = new HashMap<Point3d, List<Double>>();
	
		Map<String, List<Double>> aldenMaterial_maxFinder_lin1 = new HashMap<String, List<Double>>();
		Map<String, List<Double>> aldenMaterial_maxFinder_lin2 = new HashMap<String, List<Double>>();

		// for each non-blank stimulus shown previously, find lineage and add z-score and id to appropriate map
		Map<Long, Double> stimObjId2FRZ_lin1 = new HashMap<Long, Double>();
		Map<Long, Double> stimObjId2FRZ_lin2 = new HashMap<Long, Double>();
	
		for (int gen=1;gen<genNum;gen++) {
			List<Long> allStimObjIds = dbUtil.readAllStimIdsForRun(prefix,runNum,gen);

			DataObject thisZ;
			long currentId;

			for (int n=0;n<allStimObjIds.size();n++) {
				currentId = allStimObjIds.get(n);
				PngObjectSpec pngSpecTemp = PngObjectSpec.fromXml(dbUtil.readStimSpec_java(currentId).getSpec());
				
				if (pngSpecTemp.getStimType().equals("BLANK"))
					continue;
				
				BlenderSpec blendObject = BlenderSpec.fromXml(dbUtil.readStimSpec_blender(currentId).getSpec());
				thisZ = DataObject.fromXml(dbUtil.readStimSpec_data(currentId).getSpec());
				Double zScore = thisZ.getAvgFRminusBkgd()/thisZ.getStdFRplusBkgd();

				if (pngSpecTemp.getStimType().equals("ENVT")) {
					System.out.println(currentId);
					EnviroSpec_class enviroSpec = blendObject.getEnviroSpec();

					//horizonTilt
					if (thisZ.getLineage() == 0) {
						List<Double> currentKeysTilt = new ArrayList<Double>(horizonTilt_maxFinder_lin1.keySet());
						double thisHorizonTilt = enviroSpec.getHorizonTilt();

						if (currentKeysTilt.contains(thisHorizonTilt)) {
							List<Double> zList = horizonTilt_maxFinder_lin1.get(thisHorizonTilt);
							zList.add(zScore);
							horizonTilt_maxFinder_lin1.put(thisHorizonTilt,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							horizonTilt_maxFinder_lin1.put(thisHorizonTilt,zList);
						}
					}
					else {
						List<Double> currentKeysTilt = new ArrayList<Double>(horizonTilt_maxFinder_lin2.keySet());
						double thisHorizonTilt = enviroSpec.getHorizonTilt();

						if (currentKeysTilt.contains(thisHorizonTilt)) {
							List<Double> zList = horizonTilt_maxFinder_lin2.get(thisHorizonTilt);
							zList.add(zScore);
							horizonTilt_maxFinder_lin2.put(thisHorizonTilt,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							horizonTilt_maxFinder_lin2.put(thisHorizonTilt,zList);
						}
					}

					//horizonSlant
					if (thisZ.getLineage() == 0) {
						List<Double> currentKeysSlant = new ArrayList<Double>(horizonSlant_maxFinder_lin1.keySet());
						double thisHorizonSlant = enviroSpec.getHorizonSlant();

						if (currentKeysSlant.contains(thisHorizonSlant)) {
							List<Double> zList = horizonSlant_maxFinder_lin1.get(thisHorizonSlant);
							zList.add(zScore);
							horizonSlant_maxFinder_lin1.put(thisHorizonSlant,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							horizonSlant_maxFinder_lin1.put(thisHorizonSlant,zList);
						}
					}
					else {
						List<Double> currentKeysSlant = new ArrayList<Double>(horizonSlant_maxFinder_lin2.keySet());
						double thisHorizonSlant = enviroSpec.getHorizonSlant();

						if (currentKeysSlant.contains(thisHorizonSlant)) {
							List<Double> zList = horizonSlant_maxFinder_lin2.get(thisHorizonSlant);
							zList.add(zScore);
							horizonSlant_maxFinder_lin2.put(thisHorizonSlant,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							horizonSlant_maxFinder_lin2.put(thisHorizonSlant,zList);
						}
					}
					
					//horizonMaterial
					if (thisZ.getLineage() == 0) {
						List<String> currentKeysHorizMat = new ArrayList<String>(horizonMaterial_maxFinder_lin1.keySet());
						String thisHorizonMat = enviroSpec.getHorizonMaterial();

						if (currentKeysHorizMat.contains(thisHorizonMat)) {
							List<Double> zList = horizonMaterial_maxFinder_lin1.get(thisHorizonMat);
							zList.add(zScore);
							horizonMaterial_maxFinder_lin1.put(thisHorizonMat,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							horizonMaterial_maxFinder_lin1.put(thisHorizonMat,zList);
						}
					}
					else {
						List<String> currentKeysHorizMat = new ArrayList<String>(horizonMaterial_maxFinder_lin2.keySet());
						String thisHorizonMat = enviroSpec.getHorizonMaterial();

						if (currentKeysHorizMat.contains(thisHorizonMat)) {
							List<Double> zList = horizonMaterial_maxFinder_lin2.get(thisHorizonMat);
							zList.add(zScore);
							horizonMaterial_maxFinder_lin2.put(thisHorizonMat,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							horizonMaterial_maxFinder_lin2.put(thisHorizonMat,zList);
						}
					}

					//sun
					if (thisZ.getLineage() == 0) {
						AldenSpec_class aldenSpec = blendObject.getAldenSpec();
						List<Point3d> currentKeysSun = new ArrayList<Point3d>(sun_maxFinder_lin1.keySet());
						Point3d thisSun = aldenSpec.getSun();

						if (currentKeysSun.contains(thisSun)) {
							List<Double> zList = sun_maxFinder_lin1.get(thisSun);
							zList.add(zScore);
							sun_maxFinder_lin1.put(thisSun,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							sun_maxFinder_lin1.put(thisSun,zList);
						}
					}
					else {
						AldenSpec_class aldenSpec = blendObject.getAldenSpec();
						List<Point3d> currentKeysSun = new ArrayList<Point3d>(sun_maxFinder_lin2.keySet());
						Point3d thisSun = aldenSpec.getSun();

						if (currentKeysSun.contains(thisSun)) {
							List<Double> zList = sun_maxFinder_lin2.get(thisSun);
							zList.add(zScore);
							sun_maxFinder_lin2.put(thisSun,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							sun_maxFinder_lin2.put(thisSun,zList);
						}
					}
					
					//distance
					if (thisZ.getLineage() == 0) {
						List<Double> currentKeysDistance = new ArrayList<Double>(distance_maxFinder_lin1.keySet());
						double thisDistance = enviroSpec.getDistance();

						if (currentKeysDistance.contains(thisDistance)) {
							List<Double> zList = distance_maxFinder_lin1.get(thisDistance);
							zList.add(zScore);
							distance_maxFinder_lin1.put(thisDistance,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							distance_maxFinder_lin1.put(thisDistance,zList);
						}
					}
					else {
						List<Double> currentKeysDistance = new ArrayList<Double>(distance_maxFinder_lin2.keySet());
						double thisDistance = enviroSpec.getDistance();

						if (currentKeysDistance.contains(thisDistance)) {
							List<Double> zList = distance_maxFinder_lin2.get(thisDistance);
							zList.add(zScore);
							distance_maxFinder_lin2.put(thisDistance,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							distance_maxFinder_lin2.put(thisDistance,zList);
						}
					}

					//structureMaterial
					if (thisZ.getLineage() == 0) {
						List<String> currentKeysStructMat = new ArrayList<String>(structureMaterial_maxFinder_lin1.keySet());
						String thisStructMat = enviroSpec.getStructureMaterial();

						if (currentKeysStructMat.contains(thisStructMat)) {
							List<Double> zList = structureMaterial_maxFinder_lin1.get(thisStructMat);
							zList.add(zScore);
							structureMaterial_maxFinder_lin1.put(thisStructMat,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							structureMaterial_maxFinder_lin1.put(thisStructMat,zList);
						}
					}
					else {
						List<String> currentKeysStructMat = new ArrayList<String>(structureMaterial_maxFinder_lin2.keySet());
						String thisStructMat = enviroSpec.getStructureMaterial();

						if (currentKeysStructMat.contains(thisStructMat)) {
							List<Double> zList = structureMaterial_maxFinder_lin2.get(thisStructMat);
							zList.add(zScore);
							structureMaterial_maxFinder_lin2.put(thisStructMat,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							structureMaterial_maxFinder_lin2.put(thisStructMat,zList);
						}
					}

					//floor
					if (thisZ.getLineage() == 0) {
						System.out.println("ONE");
						List<Boolean> currentKeysFloor = new ArrayList<Boolean>(floor_maxFinder_lin1.keySet());
						boolean thisFloor = enviroSpec.getHasFloor();
						System.out.println(thisFloor);

						if (currentKeysFloor.contains(thisFloor)) {
							List<Double> zList = floor_maxFinder_lin1.get(thisFloor);
							zList.add(zScore);
							floor_maxFinder_lin1.put(thisFloor,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							floor_maxFinder_lin1.put(thisFloor,zList);
						}
					}
					else {
						System.out.println("TWO");
						List<Boolean> currentKeysFloor = new ArrayList<Boolean>(floor_maxFinder_lin2.keySet());
						boolean thisFloor = enviroSpec.getHasFloor();

						if (currentKeysFloor.contains(thisFloor)) {
							List<Double> zList = floor_maxFinder_lin2.get(thisFloor);
							zList.add(zScore);
							floor_maxFinder_lin2.put(thisFloor,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							floor_maxFinder_lin2.put(thisFloor,zList);
						}
					}

					//ceiling
					if (thisZ.getLineage() == 0) {
						List<Boolean> currentKeysCeiling = new ArrayList<Boolean>(ceiling_maxFinder_lin1.keySet());
						boolean thisCeiling = enviroSpec.getHasCeiling();

						if (currentKeysCeiling.contains(thisCeiling)) {
							List<Double> zList = ceiling_maxFinder_lin1.get(thisCeiling);
							zList.add(zScore);
							ceiling_maxFinder_lin1.put(thisCeiling,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							ceiling_maxFinder_lin1.put(thisCeiling,zList);
						}
					}
					else {
						List<Boolean> currentKeysCeiling = new ArrayList<Boolean>(ceiling_maxFinder_lin2.keySet());
						boolean thisCeiling = enviroSpec.getHasCeiling();

						if (currentKeysCeiling.contains(thisCeiling)) {
							List<Double> zList = ceiling_maxFinder_lin2.get(thisCeiling);
							zList.add(zScore);
							ceiling_maxFinder_lin2.put(thisCeiling,zList); 
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							ceiling_maxFinder_lin2.put(thisCeiling,zList);
						}
					}

					//wallL
					if (thisZ.getLineage() == 0) {
						List<Boolean> currentKeysWallL = new ArrayList<Boolean>(wallL_maxFinder_lin1.keySet());
						boolean thisWallL = enviroSpec.getHasWallL();

						if (currentKeysWallL.contains(thisWallL)) {
							List<Double> zList = wallL_maxFinder_lin1.get(thisWallL);
							zList.add(zScore);
							wallL_maxFinder_lin1.put(thisWallL,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							wallL_maxFinder_lin1.put(thisWallL,zList);
						}
					}
					else {
						List<Boolean> currentKeysWallL = new ArrayList<Boolean>(wallL_maxFinder_lin2.keySet());
						boolean thisWallL = enviroSpec.getHasWallL();

						if (currentKeysWallL.contains(thisWallL)) {
							List<Double> zList = wallL_maxFinder_lin2.get(thisWallL);
							zList.add(zScore);
							wallL_maxFinder_lin2.put(thisWallL,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							wallL_maxFinder_lin2.put(thisWallL,zList);
						}
					}

					//wallR
					if (thisZ.getLineage() == 0) {
						List<Boolean> currentKeysWallR = new ArrayList<Boolean>(wallR_maxFinder_lin1.keySet());
						boolean thisWallR = enviroSpec.getHasWallR();

						if (currentKeysWallR.contains(thisWallR)) {
							List<Double> zList = wallR_maxFinder_lin1.get(thisWallR);
							zList.add(zScore);
							wallR_maxFinder_lin1.put(thisWallR,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							wallR_maxFinder_lin1.put(thisWallR,zList);
						}
					}
					else {
						List<Boolean> currentKeysWallR = new ArrayList<Boolean>(wallR_maxFinder_lin2.keySet());
						boolean thisWallR = enviroSpec.getHasWallR();

						if (currentKeysWallR.contains(thisWallR)) {
							List<Double> zList = wallR_maxFinder_lin2.get(thisWallR);
							zList.add(zScore);
							wallR_maxFinder_lin2.put(thisWallR,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							wallR_maxFinder_lin2.put(thisWallR,zList);
						}
					}

					//wallB
					if (thisZ.getLineage() == 0) {
						List<Boolean> currentKeysWallB = new ArrayList<Boolean>(wallB_maxFinder_lin1.keySet());
						boolean thisWallB = enviroSpec.getHasWallB();

						if (currentKeysWallB.contains(thisWallB)) {
							List<Double> zList = wallB_maxFinder_lin1.get(thisWallB);
							zList.add(zScore);
							wallB_maxFinder_lin1.put(thisWallB,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							wallB_maxFinder_lin1.put(thisWallB,zList);
						}
					}
					else {
						List<Boolean> currentKeysWallB = new ArrayList<Boolean>(wallB_maxFinder_lin2.keySet());
						boolean thisWallB = enviroSpec.getHasWallB();

						if (currentKeysWallB.contains(thisWallB)) {
							List<Double> zList = wallB_maxFinder_lin2.get(thisWallB);
							zList.add(zScore);
							wallB_maxFinder_lin2.put(thisWallB,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							wallB_maxFinder_lin2.put(thisWallB,zList);
						}
					}
				}

				else if (pngSpecTemp.getStimType().equals("OBJECT")) {
					AldenSpec_class aldenSpec = blendObject.getAldenSpec();

					//aldenMaterial
					if (thisZ.getLineage() == 0) {
						// collect object ids and scores
						stimObjId2FRZ_lin1.put(currentId, zScore);
						
						// aldenMaterial
						List<String> currentKeysAldenMat = new ArrayList<String>(aldenMaterial_maxFinder_lin1.keySet());
						String thisAldenMat = aldenSpec.getAldenMaterial();

						if (currentKeysAldenMat.contains(thisAldenMat)) {
							List<Double> zList = aldenMaterial_maxFinder_lin1.get(thisAldenMat);
							zList.add(zScore);
							aldenMaterial_maxFinder_lin1.put(thisAldenMat,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							aldenMaterial_maxFinder_lin1.put(thisAldenMat,zList);
						}
					}
					else {
						// collect object ids and scores
						stimObjId2FRZ_lin2.put(currentId, zScore);
						
						// aldenMaterial
						List<String> currentKeysAldenMat = new ArrayList<String>(aldenMaterial_maxFinder_lin2.keySet());
						String thisAldenMat = aldenSpec.getAldenMaterial();

						if (currentKeysAldenMat.contains(thisAldenMat)) {
							List<Double> zList = aldenMaterial_maxFinder_lin2.get(thisAldenMat);
							zList.add(zScore);
							aldenMaterial_maxFinder_lin2.put(thisAldenMat,zList);
						}
						else {
							List<Double> zList = new ArrayList<Double>();
							zList.add(zScore);
							aldenMaterial_maxFinder_lin2.put(thisAldenMat,zList);
						}
					}
				}
			}
		}

		List<String> constantAttributes_lin1 = new ArrayList<String>(); // tilt, slant, sun, distance, floor, ceiling, wallL, wallR, wallB, hMat, sMat, aMat
		List<String> constantAttributes_lin2 = new ArrayList<String>(); // tilt, slant, sun, distance, floor, ceiling, wallL, wallR, wallB, hMat, sMat, aMat
		
		Double horizonTilt_decision_lin1 = GAMaths.chooseStimsToMorphComposite_Double(horizonTilt_maxFinder_lin1);
		constantAttributes_lin1.add(Double.toString(horizonTilt_decision_lin1));
		Double horizonTilt_decision_lin2 = GAMaths.chooseStimsToMorphComposite_Double(horizonTilt_maxFinder_lin2);
		constantAttributes_lin2.add(Double.toString(horizonTilt_decision_lin2));
		
		Double horizonSlant_decision_lin1 = GAMaths.chooseStimsToMorphComposite_Double(horizonSlant_maxFinder_lin1);
		constantAttributes_lin1.add(Double.toString(horizonSlant_decision_lin1));
		Double horizonSlant_decision_lin2 = GAMaths.chooseStimsToMorphComposite_Double(horizonSlant_maxFinder_lin2);
		constantAttributes_lin2.add(Double.toString(horizonSlant_decision_lin2));
		
		Point3d sun_decision_lin1 = GAMaths.chooseStimsToMorphComposite_Point3d(sun_maxFinder_lin1);
		String strSun = sun_decision_lin1.x + "," + sun_decision_lin1.y + "," + sun_decision_lin1.z;
		constantAttributes_lin1.add(strSun);
		Point3d sun_decision_lin2 = GAMaths.chooseStimsToMorphComposite_Point3d(sun_maxFinder_lin2);
		strSun = sun_decision_lin2.x + "," + sun_decision_lin2.y + "," + sun_decision_lin2.z;
		constantAttributes_lin2.add(strSun);
		
		Double distance_decision_lin1 = GAMaths.chooseStimsToMorphComposite_Double(distance_maxFinder_lin1);
		constantAttributes_lin1.add(Double.toString(distance_decision_lin1));
		Double distance_decision_lin2 = GAMaths.chooseStimsToMorphComposite_Double(distance_maxFinder_lin2);
		constantAttributes_lin2.add(Double.toString(distance_decision_lin2));
		
		Boolean floor_decision_lin1 = GAMaths.chooseStimsToMorphComposite_Boolean(floor_maxFinder_lin1);
		
		constantAttributes_lin1.add(Boolean.toString(floor_decision_lin1));
		Boolean floor_decision_lin2 = GAMaths.chooseStimsToMorphComposite_Boolean(floor_maxFinder_lin2);
		constantAttributes_lin2.add(Boolean.toString(floor_decision_lin2));
		
		Boolean ceiling_decision_lin1 = GAMaths.chooseStimsToMorphComposite_Boolean(ceiling_maxFinder_lin1);
		constantAttributes_lin1.add(Boolean.toString(ceiling_decision_lin1));
		Boolean ceiling_decision_lin2 = true;//GAMaths.chooseStimsToMorphComposite_Boolean(ceiling_maxFinder_lin2);
		constantAttributes_lin2.add(Boolean.toString(ceiling_decision_lin2));
		
		Boolean wallL_decision_lin1 = GAMaths.chooseStimsToMorphComposite_Boolean(wallL_maxFinder_lin1);
		constantAttributes_lin1.add(Boolean.toString(wallL_decision_lin1));
		Boolean wallL_decision_lin2 = GAMaths.chooseStimsToMorphComposite_Boolean(wallL_maxFinder_lin2);
		constantAttributes_lin2.add(Boolean.toString(wallL_decision_lin2));

		Boolean wallR_decision_lin1 = GAMaths.chooseStimsToMorphComposite_Boolean(wallR_maxFinder_lin1);
		constantAttributes_lin1.add(Boolean.toString(wallR_decision_lin1));
		Boolean wallR_decision_lin2 = GAMaths.chooseStimsToMorphComposite_Boolean(wallR_maxFinder_lin2);
		constantAttributes_lin2.add(Boolean.toString(wallR_decision_lin2));

		Boolean wallB_decision_lin1 = GAMaths.chooseStimsToMorphComposite_Boolean(wallB_maxFinder_lin1);
		constantAttributes_lin1.add(Boolean.toString(wallB_decision_lin1));
		Boolean wallB_decision_lin2 = GAMaths.chooseStimsToMorphComposite_Boolean(wallB_maxFinder_lin2);
		constantAttributes_lin2.add(Boolean.toString(wallB_decision_lin2));

		String horizonMaterial_decision_lin1 = GAMaths.chooseStimsToMorphComposite_String(horizonMaterial_maxFinder_lin1);
		constantAttributes_lin1.add(horizonMaterial_decision_lin1);
		String horizonMaterial_decision_lin2 = GAMaths.chooseStimsToMorphComposite_String(horizonMaterial_maxFinder_lin2);
		constantAttributes_lin2.add(horizonMaterial_decision_lin2);

		String structureMaterial_decision_lin1 = GAMaths.chooseStimsToMorphComposite_String(structureMaterial_maxFinder_lin1);
		constantAttributes_lin1.add(structureMaterial_decision_lin1);
		String structureMaterial_decision_lin2 = GAMaths.chooseStimsToMorphComposite_String(structureMaterial_maxFinder_lin2);
		constantAttributes_lin2.add(structureMaterial_decision_lin2);

		String aldenMaterial_decision_lin1 = GAMaths.chooseStimsToMorphComposite_String(aldenMaterial_maxFinder_lin1);
		constantAttributes_lin1.add(aldenMaterial_decision_lin1);
		String aldenMaterial_decision_lin2 = GAMaths.chooseStimsToMorphComposite_String(aldenMaterial_maxFinder_lin2);
		constantAttributes_lin2.add(aldenMaterial_decision_lin2);

		System.out.println("lin1: " + constantAttributes_lin1);
		System.out.println("lin2: " + constantAttributes_lin2);

		// choose stims top morph:
		// which fitness method? 	1 = minima, maxima
		// 							2 = low, medium, high designation and random selection
		int fitnessMethod = 2;

		// choose best, worst alden stimuli
		List<Long> stimsToMorph_lin1 = GAMaths.choosePostHoc(stimObjId2FRZ_lin1, fitnessMethod); 
		List<Long> stimsToMorph_lin2 = GAMaths.choosePostHoc(stimObjId2FRZ_lin2, fitnessMethod);

		System.out.println("lin1: " + stimsToMorph_lin1);
		System.out.println("lin2: " + stimsToMorph_lin2);

		// lineage 1
		List<Integer> possiblePositions1 = new ArrayList<Integer>();
		
		if (wallB_decision_lin1 | wallL_decision_lin1 | wallR_decision_lin1 | ceiling_decision_lin1 | floor_decision_lin1) {

			// make blank stim:	
			blankStimObjIds.add(generator.generateBlankStim(prefix, runNum, genNum, 0));
			System.out.println("Blank stimulus added.");

			stimObjIds.add(generator.generatePHStim(prefix, runNum, genNum, 0, stimsToMorph_lin1.get(0), 0, "COMPOSITE"));
			System.out.println("Lineage 0: Generating and saving environment-only composite placeholder.");

			for (int m=0;m<stimsToMorph_lin1.size();m++) {
				stimObjIds.add(generator.generatePHStim(prefix, runNum, genNum, 0, stimsToMorph_lin1.get(m), m+1, "COMPOSITE"));
				System.out.println("Lineage 0: Generating and saving object-only composite placeholders.");
			}

			possiblePositions1.add(0);

			if (wallB_decision_lin1) {
				possiblePositions1.add(4);
				possiblePositions1.add(9);
			}

			if (ceiling_decision_lin1) {
				possiblePositions1.add(2);
				possiblePositions1.add(11);

				if (wallR_decision_lin1) {
					possiblePositions1.add(6);
					possiblePositions1.add(14);
				}

				if (wallL_decision_lin1) {
					possiblePositions1.add(7);
					possiblePositions1.add(15);
				}
			}

			if (wallR_decision_lin1) {
				possiblePositions1.add(1);
				possiblePositions1.add(10);
				possiblePositions1.add(5);
				possiblePositions1.add(13);
			}

			if (wallL_decision_lin1) {
				possiblePositions1.add(3);
				possiblePositions1.add(8);
				possiblePositions1.add(12);
				possiblePositions1.add(16);
			}

			// ready database for composite spec generation
			int stimNum = stimsToMorph_lin1.size()+1;
			
			System.out.println(stimsToMorph_lin1.size());
			System.out.println(possiblePositions1.size());
			
			for (int n=0;n<possiblePositions1.size();n++) {

				for (int m=0;m<stimsToMorph_lin1.size();m++) {
					stimObjIds.add(generator.generatePHStim(prefix, runNum, genNum, 0, stimsToMorph_lin1.get(m), stimNum, "COMPOSITE"));
					System.out.println("Lineage 0: Generating and saving composite placeholder " + stimNum);
					stimNum += 1;
				}
			}
		}

		else {
			System.out.println("Lineage 0: Optimal architecture not suitable for composite post-hoc.");
		}

		// lineage 2
		List<Integer> possiblePositions2 = new ArrayList<Integer>();

		if (wallB_decision_lin2 | wallL_decision_lin2 | wallR_decision_lin2 | ceiling_decision_lin2 | floor_decision_lin2) {

			// make blank stim:	
			blankStimObjIds.add(generator.generateBlankStim(prefix, runNum, genNum, 1));
			System.out.println("Blank stimulus added.");

			stimObjIds.add(generator.generatePHStim(prefix, runNum, genNum, 1, stimsToMorph_lin2.get(0), 0, "COMPOSITE"));
			System.out.println("Lineage 1: Generating and saving environment-only composite placeholder.");

			for (int m=0;m<stimsToMorph_lin2.size();m++) {
				stimObjIds.add(generator.generatePHStim(prefix, runNum, genNum, 1, stimsToMorph_lin2.get(m), m+1, "COMPOSITE"));
				System.out.println("Lineage 1: Generating and saving object-only composite placeholders.");
			}

			possiblePositions2.add(0);

			if (wallB_decision_lin2) {
				possiblePositions2.add(4);
				possiblePositions2.add(9);
			}

			if (ceiling_decision_lin2) {
				possiblePositions2.add(2);
				possiblePositions2.add(11);

				if (wallR_decision_lin2) {
					possiblePositions2.add(6);
					possiblePositions2.add(14);
				}

				if (wallL_decision_lin2) {
					possiblePositions2.add(7);
					possiblePositions2.add(15);
				}
			}

			if (wallR_decision_lin2) {
				possiblePositions2.add(1);
				possiblePositions2.add(10);
				possiblePositions2.add(5);
				possiblePositions2.add(13);
			}

			if (wallL_decision_lin2) {
				possiblePositions2.add(3);
				possiblePositions2.add(8);
				possiblePositions2.add(12);
				possiblePositions2.add(16);
			}

			// ready database for composite spec generation
			int stimNum = stimsToMorph_lin2.size()+1;
			
			for (int n=0;n<possiblePositions2.size();n++) {

				for (int m=0;m<stimsToMorph_lin2.size();m++) {
					stimObjIds.add(generator.generatePHStim(prefix, runNum, genNum, 1, stimsToMorph_lin2.get(m), stimNum, "COMPOSITE"));
					System.out.println("Lineage 1: Generating and saving composite placeholder " + stimNum);
					stimNum += 1;
				}
			}
		}

		else {
			System.out.println("Lineage 1: Optimal architecture not suitable for composite post-hoc.");
		}

		if ((possiblePositions1.size() == 0) & (possiblePositions2.size() == 0)) {
			System.out.println("Abort: Neither lineage suitable for composite post-hoc.");
			return;
		}
		
		/* 
		 * This python script is called within blender.
		 * It reads the latest descriptive ID from the DB
		 * and makes a list of stimuli to create. It then does
		 * a parallel blenderrender call to save all the blenderspec
		 * to stimobjdata. Finally, it does a cluster call
		 * to render png images. When done, it rsyncs all pngs
		 * to the rig and to ecpc31.
		 */
		
		if (possiblePositions1.size() != 0) {
			System.out.println("Do lineage 0.");
			possiblePositions1.add(0); // document lineage for python
			BlenderRunnable blenderRunnerComposite_lin1 = new BlenderRunnable("/Users/ecpc31/Dropbox/Blender/ProgressionClasses/compositePostHoc.py",constantAttributes_lin1,possiblePositions1);
//			BlenderRunnable blenderRunnerComposite_lin1 = new BlenderRunnable("/Users/alexandriya/Dropbox/Blender/ProgressionClasses/compositePostHoc.py",constantAttributes_lin1,possiblePositions);
			blenderRunnerComposite_lin1.run();
		}
		
		if (possiblePositions2.size() != 0) {
			System.out.println("Do lineage 1.");
			possiblePositions2.add(1); // document lineage for python
			BlenderRunnable blenderRunnerComposite_lin2 = new BlenderRunnable("/Users/ecpc31/Dropbox/Blender/ProgressionClasses/compositePostHoc.py",constantAttributes_lin2,possiblePositions2);
//			BlenderRunnable blenderRunnerComposite_lin2 = new BlenderRunnable("/Users/alexandriya/Dropbox/Blender/ProgressionClasses/compositePostHoc.py",constantAttributes_lin2,possiblePositions2);
			blenderRunnerComposite_lin2.run();
		}
		
		// remove for post-hocs? not sure this is really necessary...
//		if (doSaveThumbnails) {
//			System.out.println("Saving PNGs.");
//			pngMaker.MakeFromIds(stimObjIds);
//		}
		
		// add blanks
		stimObjIds.addAll(blankStimObjIds);	

		// create trial structure, populate stimspec, write task-to-do
		System.out.println("Creating trial spec for this generation.");
		createPHTrialsFromStimObjs(stimObjIds,"COMPOSITE");

		// write updated global genId and number of trials in this generation to db:
		int numTasks = (int) Math.ceil(stimObjIds.size()*PngGAParams.GA_numRepsPerStim/PngGAParams.GA_numStimsPerTrial);
		dbUtil.updateReadyGenerationInfo(prefix, runNum, genNum, numTasks);
		
		// get acq info and put into db:
		getSpikeResponses();
	}
	
	void createPHanimacy() {
		List<Long> blankStimObjIds = new ArrayList<Long>();	
		List<Long> stimObjIds = new ArrayList<Long>();

		// make blank stims:		
		for (int n=0;n<PngGAParams.GA_numLineages;n++) {
			blankStimObjIds.add(generator.generateBlankStim(prefix, runNum, genNum, n));
		}
		System.out.println("Blank stimuli added.");
		
		// for each non-blank stimulus shown previously, find lineage and add z-score and id to appropriate map
		Map<Long, Double> stimObjId2FRZ_lin1 = new HashMap<Long, Double>();
		Map<Long, Double> stimObjId2FRZ_lin2 = new HashMap<Long, Double>();
		Map<Long, Integer> stimObjId2numAnimations_lin1 = new HashMap<Long, Integer>();
		Map<Long, Integer> stimObjId2numAnimations_lin2 = new HashMap<Long, Integer>();
		
		for (int gen=1;gen<genNum;gen++) {
			List<Long> allStimObjIds = dbUtil.readAllStimIdsForRun(prefix,runNum,gen);

			DataObject thisZ;
			long currentId;

			for (int n=0;n<allStimObjIds.size();n++) {
				currentId = allStimObjIds.get(n);
				PngObjectSpec pngSpecTemp = PngObjectSpec.fromXml(dbUtil.readStimSpec_java(currentId).getSpec());
				
				if (pngSpecTemp.getStimType().equals("BLANK"))
					continue;
				
				BlenderSpec blendObject = BlenderSpec.fromXml(dbUtil.readStimSpec_blender(currentId).getSpec());
				thisZ = DataObject.fromXml(dbUtil.readStimSpec_data(currentId).getSpec());
				Double zScore = thisZ.getAvgFRminusBkgd()/thisZ.getStdFRplusBkgd();

				if (pngSpecTemp.getStimType().equals("OBJECT")) {
					AldenSpec_class aldenSpec = blendObject.getAldenSpec();
					MStickSpec stickSpec = MStickSpec.fromXml(dbUtil.readStimSpec_stick(currentId).getSpec());

					if (thisZ.getLineage() == 0) {
						// collect object ids and scores
						stimObjId2FRZ_lin1.put(currentId, zScore);
						
						// find number of endpts
						int numEndPts = stickSpec.getNEndPt();
						
						// find number of comps with two endpts
						int nDoubleEndPtComps = stickSpec.getNDoubleEndPtComps();
						
						// determine whether low potential and without precariousness
						boolean isLowPotential = aldenSpec.getLowPotentialEnergy();
						
						Point3d precariousness = aldenSpec.getMakePrecarious();
						double precariousnessContents = precariousness.x+precariousness.y+precariousness.z;
						boolean isNotPrecarious = false;
						
						if (precariousnessContents == 0.0) {
							isNotPrecarious = true;
						}
						
						int stabilityLegs = 1;
						
						if (isLowPotential & isNotPrecarious) {
							stabilityLegs = 0;
						}
						
						// record number of animations in post hoc
						int numPHanimations = numEndPts - nDoubleEndPtComps - stabilityLegs;
						stimObjId2numAnimations_lin1.put(currentId, numPHanimations);
						System.out.println(numPHanimations+","+numEndPts+","+nDoubleEndPtComps+","+stabilityLegs);
					}
					
					else {
						// collect object ids and scores
						stimObjId2FRZ_lin2.put(currentId, zScore);
						
						// find number of endpts
						int numEndPts = stickSpec.getNEndPt();
						
						// find number of comps with two endpts
						int nDoubleEndPtComps = stickSpec.getNDoubleEndPtComps();
						
						// determine whether low potential and without precariousness
						boolean isLowPotential = aldenSpec.getLowPotentialEnergy();
						
						Point3d precariousness = aldenSpec.getMakePrecarious();
						double precariousnessContents = precariousness.x+precariousness.y+precariousness.z;
						boolean isNotPrecarious = false;
						
						if (precariousnessContents == 0.0) {
							isNotPrecarious = true;
						}
						
						int stabilityLegs = 1;
						
						if (isLowPotential & isNotPrecarious) {
							stabilityLegs = 0;
						}
						
						// record number of animations in post hoc
						int numPHanimations = numEndPts - nDoubleEndPtComps - stabilityLegs;
						stimObjId2numAnimations_lin2.put(currentId, numPHanimations);
					}
				}
			}
		}

		// choose stims top morph:
		// which fitness method? 	1 = minima, maxima
		// 							2 = low, medium, high designation and random selection
		//							3 = highest only
		int fitnessMethod = 3;

		// choose best, worst alden stimuli
		List<Long> stimsToMorph_lin1 = GAMaths.choosePostHoc(stimObjId2FRZ_lin1, fitnessMethod); 
		List<Long> stimsToMorph_lin2 = GAMaths.choosePostHoc(stimObjId2FRZ_lin2, fitnessMethod);

		System.out.println("lin1: " + stimsToMorph_lin1);
		System.out.println("lin2: " + stimsToMorph_lin2);

		//each stimulus has associated number of limbs, should be repeated that many times
		
		// ready database for animacy spec generation
		int stimNum = 0;
		
		for (int n=0;n<stimsToMorph_lin1.size();n++) {
			long currentId = stimsToMorph_lin1.get(n);
			int numPHanimations = stimObjId2numAnimations_lin1.get(currentId);
			
			List<Long> stims_lin1 = new ArrayList<Long>();
			
			// include a copy that shall remain unchanged
			for (int m=0;m<numPHanimations+1;m++) {
				long whichStim_lin1 = generator.generatePHStim(prefix, runNum, genNum, 0, currentId, stimNum, "ANIMACY");
				stimObjIds.add(whichStim_lin1);
				stims_lin1.add(whichStim_lin1);
				System.out.println("Lineage 0: Generating and saving stimulus " + n + ", limb " + m);
				stimNum ++;
			}
		}

		stimNum = 0;
		
		for (int n=0;n<stimsToMorph_lin2.size();n++) {
			long currentId = stimsToMorph_lin2.get(n);
			int numPHanimations = stimObjId2numAnimations_lin2.get(currentId);
			
			List<Long> stims_lin2 = new ArrayList<Long>();
			
			// include a copy that shall remain unchanged
			for (int m=0;m<numPHanimations+1;m++) {
				long whichStim_lin2 = generator.generatePHStim(prefix, runNum, genNum, 1, currentId, stimNum, "ANIMACY");
				stimObjIds.add(whichStim_lin2);
				stims_lin2.add(whichStim_lin2);
				System.out.println("Lineage 1: Generating and saving stimulus " + n + ", limb " + m);
				stimNum ++;			
			}
		}

		/* 
		 * This python script is called within blender.
		 * It reads the latest descriptive ID from the DB
		 * and makes a list of stimuli to create. It then does
		 * a parallel blenderrender call to save all the blenderspec
		 * to stimobjdata. Finally, it does a cluster call
		 * to render png images. When done, it rsyncs all pngs
		 * to the rig and to ecpc31.
		 */

		BlenderRunnable blenderRunnerAnimate = new BlenderRunnable("/Users/ecpc31/Dropbox/Blender/ProgressionClasses/animatePostHoc.py");
//		BlenderRunnable blenderRunnerAnimate = new BlenderRunnable("/Users/alexandriya/Dropbox/Blender/ProgressionClasses/animatePostHoc.py");
		blenderRunnerAnimate.run();
		
		// remove for post-hocs? not sure this is really necessary...
		if (doSaveThumbnails) {
			System.out.println("Saving PNGs.");
			pngMaker.MakeFromIds(stimObjIds);
		}
		
		// add blanks
		stimObjIds.addAll(blankStimObjIds);	

		// create trial structure, populate stimspec, write task-to-do
		System.out.println("Creating trial spec for this generation.");
		createPHTrialsFromStimObjs(stimObjIds,"ANIMACY");

		// write updated global genId and number of trials in this generation to db:
		int numStimsPerTrial = 1;
		int numTasks = (int) Math.ceil(stimObjIds.size()*PngGAParams.GA_numRepsPerStim/numStimsPerTrial);
		dbUtil.updateReadyGenerationInfo(prefix, runNum, genNum, numTasks);

		// get acq info and put into db:
		getSpikeResponses();
	}
	
	void createPHgeneric(String postHoc) {
		// choose best and worst raw obj stimulus?
		// choose top 2 best-performing materials?
		
		List<Long> blankStimObjIds = new ArrayList<Long>();	
		List<Long> stimObjIds = new ArrayList<Long>();

		// make blank stims:		
		for (int n=0;n<PngGAParams.GA_numLineages;n++) {
			blankStimObjIds.add(generator.generateBlankStim(prefix, runNum, genNum, n));
		}
		System.out.println("Blank stimuli added.");
		
		// choose stims top morph:
				// which fitness method? 	1 = minima, maxima
				// 							2 = low, medium, high designation and random selection
				//							3 = highest only
		int fitnessMethod = 3;
		
		ArrayList<List<Long>> stimsToMorph = chooseBestObjs(fitnessMethod); 
		List<Long> stimsToMorph_lin1 = stimsToMorph.get(0);
		List<Long> stimsToMorph_lin2 = stimsToMorph.get(1);

		int numMorphs = 0;
		String whichRunner = "NONE";
		
		if (postHoc.equals("DENSITY")) {
			numMorphs = PngGAParams.PH_density_numMorphs;
			whichRunner = "densityPostHoc.py";
		}
		else if (postHoc.equals("STABILITY")) {
			numMorphs = PngGAParams.PH_stability_numMorphs;
			whichRunner = "stabilityPostHoc.py";
		}
		
		int stimNum = 0;

		for (int n=0;n<stimsToMorph_lin1.size();n++) {
			long currentId = stimsToMorph_lin1.get(n);

			// include a copy that shall remain unchanged
			for (int m=0;m<numMorphs+1;m++) {
				stimObjIds.add(generator.generatePHStim(prefix, runNum, genNum, 0, currentId, stimNum, postHoc));
				System.out.println("Lineage 0: Generating and saving stimulus " + n + " number " + m);
				stimNum ++;
			}
		}

		stimNum = 0;

		for (int n=0;n<stimsToMorph_lin2.size();n++) {
			long currentId = stimsToMorph_lin2.get(n);

			// include a copy that shall remain unchanged
			for (int m=0;m<numMorphs+1;m++) {
				stimObjIds.add(generator.generatePHStim(prefix, runNum, genNum, 1, currentId, stimNum, postHoc));
				System.out.println("Lineage 1: Generating and saving stimulus " + n + " number " + m);
				stimNum ++;			
			}
		}

		/* 
		 * This python script is called within blender.
		 * It reads the latest descriptive ID from the DB
		 * and makes a list of stimuli to create. It then does
		 * a parallel blenderrender call to save all the blenderspec
		 * to stimobjdata. Finally, it does a cluster call
		 * to render png images. When done, it rsyncs all pngs
		 * to the rig and to ecpc31.
		 */

		BlenderRunnable blenderRunnerPHGeneric = new BlenderRunnable("/Users/ecpc31/Dropbox/Blender/ProgressionClasses/" + whichRunner);
		//				BlenderRunnable blenderRunnerPHGeneric = new BlenderRunnable("/Users/ecpc31/Dropbox/Blender/ProgressionClasses/" + whichRunner);
		blenderRunnerPHGeneric.run();

		BlenderRunnable blenderRender = new BlenderRunnable("/Users/ecpc31/Dropbox/Blender/ProgressionClasses/singleRender.py");
		//				BlenderRunnable blenderRender = new BlenderRunnable("/Users/ecpc31/Dropbox/Blender/ProgressionClasses/singleRender.py");
		blenderRender.run();
		
		// remove for post-hocs? not sure this is really necessary...
		if (doSaveThumbnails) {
			System.out.println("Saving PNGs.");
			pngMaker.MakeFromIds(stimObjIds);
		}

		// add blanks
		stimObjIds.addAll(blankStimObjIds);	

		// create trial structure, populate stimspec, write task-to-do
		System.out.println("Creating trial spec for this generation.");
		createPHTrialsFromStimObjs(stimObjIds,postHoc);

		// write updated global genId and number of trials in this generation to db:
		int numStimsPerTrial = 1;
		int numTasks = (int) Math.ceil(stimObjIds.size()*PngGAParams.GA_numRepsPerStim/numStimsPerTrial);
		dbUtil.updateReadyGenerationInfo(prefix, runNum, genNum, numTasks);

		// get acq info and put into db:
		getSpikeResponses();
	}
	
	ArrayList<List<Long>> chooseBestObjs(int fitnessMethod) {
		
		Map<Long, Double> stimObjId2FRZ_lin1 = new HashMap<Long, Double>();
		Map<Long, Double> stimObjId2FRZ_lin2 = new HashMap<Long, Double>();
		
		for (int gen=1;gen<genNum;gen++) {
			List<Long> allStimObjIds = dbUtil.readAllStimIdsForRun(prefix,runNum,gen);

			DataObject thisZ;
			long currentId;

			for (int n=0;n<allStimObjIds.size();n++) {
				currentId = allStimObjIds.get(n);
				PngObjectSpec pngSpecTemp = PngObjectSpec.fromXml(dbUtil.readStimSpec_java(currentId).getSpec());
				
				if (pngSpecTemp.getStimType().equals("BLANK"))
					continue;
				
				thisZ = DataObject.fromXml(dbUtil.readStimSpec_data(currentId).getSpec());
				Double zScore = thisZ.getAvgFRminusBkgd()/thisZ.getStdFRplusBkgd();

				if (pngSpecTemp.getStimType().equals("OBJECT")) {

					if (thisZ.getLineage() == 0) {
						stimObjId2FRZ_lin1.put(currentId, zScore);
					}
					
					else {
						stimObjId2FRZ_lin2.put(currentId, zScore);
					}
				}
			}
		}

		// choose best, worst objects
		List<Long> stimsToMorph_lin1 = GAMaths.choosePostHoc(stimObjId2FRZ_lin1, fitnessMethod); 
		List<Long> stimsToMorph_lin2 = GAMaths.choosePostHoc(stimObjId2FRZ_lin2, fitnessMethod);
		
		System.out.println("lin1: " + stimsToMorph_lin1);
		System.out.println("lin2: " + stimsToMorph_lin2);
		
		ArrayList<List<Long>> stimsToMorph = new ArrayList<List<Long>>();
		stimsToMorph.add(stimsToMorph_lin1);
		stimsToMorph.add(stimsToMorph_lin2);
		return stimsToMorph;
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
		int filler = 0;

		for (int n=0;n<PngGAParams.GA_numTasks;n++) {
			taskId = globalTimeUtil.currentTimeMicros();

			// create trialspec using sublist and taskId
			int endIdx = stimCounter + PngGAParams.GA_numStimsPerTrial;
			while (endIdx>allStimObjIdsInGen.size()) endIdx--;	// this makes sure there's no out index of bounds exception

			String spec = generator.generateGATrialSpec(allStimObjIdsInGen.subList(stimCounter,endIdx));

			if(n==0)
				writeExptFirstTrial(taskId);
			else if(n==PngGAParams.GA_numTasks-1)
				writeExptLastTrial(taskId);
			
			// save spec and tasktodo to db
			dbUtil.writeStimSpec(taskId, spec);
			dbUtil.writeTaskToDo(taskId, taskId, -1, genNum);
			dbUtil.writeTaskDone(taskId, taskId, filler); ///!!!!!

			stimCounter = endIdx;
		}
	}
	
	void createPHTrialsFromStimObjs(List<Long> stimObjIds, String postHoc) {
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
		int filler = 0;

		int numStimsPerTrial;
		
		if (postHoc.equals("ANIMACY"))
			numStimsPerTrial = 1;
		
		else
			numStimsPerTrial = PngGAParams.GA_numStimsPerTrial;
		
		int numTasks = (int) Math.ceil(stimObjIds.size()*PngGAParams.GA_numRepsPerStim/numStimsPerTrial);
		
		for (int n=0;n<numTasks;n++) {
			taskId = globalTimeUtil.currentTimeMicros();

			// create trialspec using sublist and taskId
			int endIdx = stimCounter + numStimsPerTrial;
			while (endIdx>allStimObjIdsInGen.size()) endIdx--;	// this makes sure there's no out index of bounds exception

			String spec = generator.generateGATrialSpec(allStimObjIdsInGen.subList(stimCounter,endIdx));

			if(n==0)
				writeExptFirstTrial(taskId);
			else if(n==numTasks-1)
				writeExptLastTrial(taskId);
			
			// save spec and tasktodo to db
			dbUtil.writeStimSpec(taskId, spec);
			dbUtil.writeTaskToDo(taskId, taskId, -1, genNum);
			dbUtil.writeTaskDone(taskId, taskId, filler); ///!!!!!

			stimCounter = endIdx;
		}
	}
	
	// once you get past a certain point (number of generations), start building composites from best of env and obj
	// choose ideal
	
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
//			spikeEntry = spikeCounter.getTaskSpikeByGeneration(prefix,runNum,genNum, 0);
			
			if (useFakeSpikes) {
				spikeEntry = spikeCounter.getFakeTaskSpikeByGeneration(prefix,runNum,genNum);
			} else {
				spikeEntry = spikeCounter.getTaskSpikeByGeneration(prefix,runNum,genNum, 0);
			}
			
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
//						entIdx = 2*n+2;
						entIdx = n;
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
//					entIdx = 2*n+2;
					entIdx = n; ///!!!!!
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

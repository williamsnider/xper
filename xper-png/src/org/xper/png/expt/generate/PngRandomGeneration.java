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
import java.lang.Thread;

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
	long linNum = 0;
	
	String basePath = "/Users/ecpc31/Dropbox/Blender/ProgressionClasses/";
//	String basePath = "/home/alexandriya/jkBlendRend/"; // ProgressionClasses/";
	
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
		linNum = getLinNum(); 
		
		// start by asking if there is a previous ga run to be continued
		// if so, run createNextGen for that prefix and ga run
		// else run createFirstGen with a new prefix and incremented ga run
		
		char cont = 'y';
		
		while (cont == 'y') {
			int c = PngIOUtil.promptInteger("Enter GA run number to continue. Else enter '0'");
			if (c==0) {
				
				genNum = 1;
				runNum = runNum + 1;
				linNum = 0;
				
				DateFormat df = new SimpleDateFormat("yyMMdd");
				prefix = df.format(new Date()); 
				dbUtil.writeCurrentDescriptivePrefixAndGen(globalTimeUtil.currentTimeMicros(), prefix, runNum, genNum, linNum); 
				createFirstGen();
			} else {
				
				String progressType = "";
				String postHoc;
				
				while (!Arrays.asList("n","b","g","c","a","s","p","d","m").contains(progressType)) {
					System.out.println("To continue GA, enter 'n'.");
					System.out.println("To proceed with generic post-hoc, enter 'b' (rolling ball) or 'g' (grass gravity).");
					progressType = PngIOUtil.promptString("To proceed with GA post-hoc, enter 'c' (composite), 'a' (joint animacy), 's' (stability), 'p' (perturbation), 'd' (density), or 'm' (mass distribution)");
				}
				
				runNum = c;
				linNum = getLinId(runNum);
				// if current linNum is 1, genNum increments and linNum is set to 0
				
				if (linNum == 1) {
					linNum = 0;
					genNum = getGenId(runNum) + 1;
				}
				else {
					linNum = 1;
					genNum = getGenId(runNum);
				}
				
				prefix = getPrefix(runNum);
				System.out.println(genNum);
				System.out.println(linNum);

				dbUtil.writeCurrentDescriptivePrefixAndGen(globalTimeUtil.currentTimeMicros(), prefix, runNum, genNum, linNum); 
				
				switch (progressType) {
				case "n":
					if ((genNum == 1) && (linNum == 1)) {
						createFirstGen();
					}
					else {
						createNextGen();
					}
					break;
				case "b":
					postHoc = "BALL";
					createPHrollingBall();
					break;
				case "g":
					postHoc = "GRASSGRAVITY";
					createPHgrassGravity();
					break;
				case "c":
					postHoc = "COMPOSITE";
					createPHcomposite();
					break;
				case "a":
					postHoc = "ANIMACY";
					createPHanimacy();
					break;
				case "s":
					postHoc = "STABILITY";
					createPHstability();
					break;
				case "p":
					postHoc = "PERTURBATION";
					createPHperturbation();
					break;
				case "d":
					postHoc = "DENSITY";
					createPHdensity();
					break;
				case "m":
					postHoc = "MASS";
					createPHmass();
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
		blankStimObjIds.add(generator.generateBlankStim(prefix, runNum, genNum, linNum));	
		System.out.println("Blank stimulus added."); 

		// make random stims:		
		for (int k=0;k<PngGAParams.GA_numNonBlankStimsPerLin;k++) { 
			stimObjIds.add(generator.generateRandStim(prefix, runNum, genNum, linNum, k)); 
			System.out.println("Lineage " + linNum + ": Generating and saving stimulus " + k); 
		}

		// create PNG thumbnails (not for blanks)
		if (doSaveThumbnails) {
			System.out.println("Saving PNGs.");
			pngMaker.MakeFromIds(stimObjIds);
		}

		BlenderRunnable blenderRunner = new BlenderRunnable(basePath + "randomSpec.py");
//		BlenderRunnable blenderRunner = new BlenderRunnable(basePath + "ProgressionClasses/randomSpec.py");
//		BlenderRunnable blenderRunner = new BlenderRunnable("/Users/alexandriya/Dropbox/Blender/ProgressionClasses/randomSpec.py");
		blenderRunner.run();

		int numJobs = stimObjIds.size(); //all R, allL, all non-blank stims in lineage n;
		String prefixRunGen = prefix + "_r-" + runNum + "_g-" + genNum + "_l-" + linNum;
		
        BlenderRunnable photoRunner = new BlenderRunnable();
        List<String> args = new ArrayList<String>();
        args.add("ssh");
        args.add("alexandriya@172.30.9.11");
        args.add("/home/alexandriya/blendRend/masterSubmitScript.sh");
        args.add(Integer.toString(numJobs));
        args.add(prefixRunGen);
        photoRunner.setDoWaitFor(false);
        photoRunner.run(args);

        photoRunner.run(args);
		// now add blanks
		stimObjIds.addAll(blankStimObjIds);

		// create trial structure, populate stimspec, write task-to-do
		System.out.println("Creating trial spec for lineage " + linNum + " of this generation.");
		createGATrialsFromStimObjs(stimObjIds);

		// write updated global genId and number of trials in this generation to db:
		dbUtil.updateReadyGenerationInfo(prefix, runNum, genNum, linNum, PngGAParams.GA_numTasks);

		System.out.println("Waiting for render completion...");
		while (dbUtil.readRenderStatus(prefix, runNum, genNum, linNum) == 0) { 
			try
			{	Thread.sleep(10000);	}
			catch (Exception e) {System.out.println(e);}
		}
		
		// get acq info and put into db:
		getSpikeResponses(); 
	}

	void createNextGen() {
		List<Long> blankStimObjIds = new ArrayList<Long>();	
		List<Long> stimObjIds = new ArrayList<Long>();

		// make blank stims:		
		blankStimObjIds.add(generator.generateBlankStim(prefix, runNum, genNum, linNum)); 
		System.out.println("Blank stimuli added.");

		// make random stims:		
		for (int k=0;k<PngGAParams.GA_morph_numNewStimPerLin;k++) { 
			stimObjIds.add(generator.generateRandStim(prefix, runNum, genNum, linNum, k)); 
			System.out.println("Lineage " + linNum + ": Generating and saving random stimulus " + k); 
		}
		
		BlenderRunnable blenderRunner = new BlenderRunnable(basePath + "randomSpec.py",stimObjIds);
//		BlenderRunnable blenderRunner = new BlenderRunnable(basePath + "ProgressionClasses/randomSpec.py",stimObjIds);
//		BlenderRunnable blenderRunner = new BlenderRunnable("/Users/alexandriya/Dropbox/Blender/ProgressionClasses/randomSpec.py",stimObjIds);
		blenderRunner.run();

		System.out.println("Calculating fitness and selecting parents.");
		int numDescendantObjs = PngGAParams.GA_numNonBlankStimsPerLin-PngGAParams.GA_morph_numNewStimPerLin;
		double unRounded = numDescendantObjs*PngGAParams.GA_randgen_prob_objvsenvt;
		int numDescendantObjType = (int) unRounded;
		int numDescendantEnvType = numDescendantObjs - numDescendantObjType;

		// for each non-blank stimulus shown previously, find lineage and add z-score and id to appropriate map
		Map<Long, Double> stimObjId2FRZ_lin1_obj = new HashMap<Long, Double>();
		Map<Long, Double> stimObjId2FRZ_lin1_env = new HashMap<Long, Double>();

		for (int gen=1;gen<genNum;gen++) { 
			List<Long> allStimObjIds = dbUtil.readAllStimIdsForRun(prefix,runNum,gen);

			DataObject data;
			long stimObjId;

			for (int n=0;n<allStimObjIds.size();n++) {

				stimObjId = allStimObjIds.get(n);
				PngObjectSpec pngSpecTemp = PngObjectSpec.fromXml(dbUtil.readStimSpec_java(stimObjId).getSpec());

				if (pngSpecTemp.getStimType().equals("OBJECT") || pngSpecTemp.getStimType().equals("ENVT")) {
					data = DataObject.fromXml(dbUtil.readStimSpec_data(stimObjId).getSpec());
					
					if (data.getLineage() == linNum) { 
						Double zScore = data.getAvgFRminusBkgd()/data.getStdFRplusBkgd();
						
						if (pngSpecTemp.getStimType().equals("OBJECT")) {
							stimObjId2FRZ_lin1_obj.put(stimObjId, zScore);
						}
						else {
							stimObjId2FRZ_lin1_env.put(stimObjId, zScore);
						}
					}
				}
			}
		}

		// choose stims top morph:
		// which fitness method? 	1 = using fixed probabilities by FR quintile
		// 							2 = using distance in firing rate space
		int fitnessMethod = 1;

		List<Long> stimsToMorph_lin1_obj = GAMaths.chooseStimsToMorph(stimObjId2FRZ_lin1_obj,numDescendantObjType,fitnessMethod); 
		List<Long> stimsToMorph_lin1_env = GAMaths.chooseStimsToMorph(stimObjId2FRZ_lin1_env,numDescendantEnvType,fitnessMethod); 

		List<Long> stimsToMorph_lin1 = new ArrayList<Long>(stimsToMorph_lin1_obj);
		stimsToMorph_lin1.addAll(stimsToMorph_lin1_env);
		
		System.out.println("Lineage " + linNum + " stimuli to morph: " + stimsToMorph_lin1); 

		//		// check generation designations of stimuli chosen to morph
		//		for (int n=0;n<stimsToMorph_lin1.size();n++) {
		//			System.out.println("LIN STIM DESIGNATIONS "+stimsToMorph_lin1.get(n)+" "+dbUtil.readDescriptiveIdFromStimObjId(stimsToMorph_lin1.get(n)));
		//		}

		List<Long> stimsToMorph = new ArrayList<Long>();
		List<Long> stimsToRestore = new ArrayList<Long>();
		List<String> tempArray = new ArrayList<String>();
		Long whichStim;

		// create morphed stimuli:
		for (int n=0;n<numDescendantObjs;n++) {

			tempArray = generator.generateMorphStim(prefix, runNum, genNum, linNum, stimsToMorph_lin1.get(n),n+PngGAParams.GA_morph_numNewStimPerLin); 
			System.out.println("Lineage " + linNum + ": Generating and saving morphed stimulus " + n); 
			whichStim = Long.parseLong(tempArray.get(0));
			stimObjIds.add(whichStim);

			if (tempArray.get(1) == "NewBSpec")
				stimsToMorph.add(whichStim);

			else 
				stimsToRestore.add(whichStim);
		}

		System.out.println(stimsToMorph+" MORPH");
		System.out.println(stimsToRestore+" RESTORE");

		BlenderRunnable blenderRunnerMorph = new BlenderRunnable(basePath + "morphSpec.py",stimsToMorph);
//		BlenderRunnable blenderRunnerMorph = new BlenderRunnable(basePath + "ProgressionClasses/morphSpec.py",stimsToMorph);
//		BlenderRunnable blenderRunnerMorph = new BlenderRunnable("/Users/alexandriya/Dropbox/Blender/ProgressionClasses/morphSpec.py",stimsToMorph);
		blenderRunnerMorph.run();

		BlenderRunnable blenderRunnerRestore = new BlenderRunnable(basePath + "restoreMorphSpec.py",stimsToRestore);
//		BlenderRunnable blenderRunnerRestore = new BlenderRunnable(basePath + "ProgressionClasses/restoreMorphSpec.py",stimsToRestore);
//		BlenderRunnable blenderRunnerRestore = new BlenderRunnable("/Users/alexandriya/Dropbox/Blender/ProgressionClasses/restoreMorphSpec.py",stimsToRestore);
		blenderRunnerRestore.run();

//		if (doSaveThumbnails) {
//			System.out.println("Saving PNGs.");
//			pngMaker.MakeFromIds(stimObjIds);
//		}
		
		int numJobs = stimObjIds.size(); //all R, allL, all non-blank stims in lineage
		String prefixRunGen = prefix + "_r-" + runNum + "_g-" + genNum + "_l-" + linNum; 
		
        BlenderRunnable photoRunner = new BlenderRunnable();
        List<String> args = new ArrayList<String>();
        args.add("ssh");
        args.add("alexandriya@172.30.9.11");
//        args.add(basePath + "masterSubmitScript.sh");
        args.add("/home/alexandriya/blendRend/masterSubmitScript.sh");
        args.add(Integer.toString(numJobs));
        args.add(prefixRunGen);
        photoRunner.setDoWaitFor(false);
        photoRunner.run(args);
		
		// add blanks
		stimObjIds.addAll(blankStimObjIds);	

		// create trial structure, populate stimspec, write task-to-do
		System.out.println("Creating trial spec for lineage " + linNum + " of this generation."); 
		createGATrialsFromStimObjs(stimObjIds);

		// write updated global genId and number of trials in this generation to db:
		dbUtil.updateReadyGenerationInfo(prefix, runNum, genNum, linNum, PngGAParams.GA_numTasks); 
		
		System.out.println("Waiting for render completion...");
		while (dbUtil.readRenderStatus(prefix, runNum, genNum, linNum) == 0) { 
			try
			{	Thread.sleep(10000);	}
			catch (Exception e) {System.out.println(e);}
		}
		
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
		Map<Integer, List<Double>> floor_maxFinder_lin1 = new HashMap<Integer, List<Double>>();
		Map<Integer, List<Double>> ceiling_maxFinder_lin1 = new HashMap<Integer, List<Double>>();
		Map<Integer, List<Double>> wallL_maxFinder_lin1 = new HashMap<Integer, List<Double>>();
		Map<Integer, List<Double>> wallR_maxFinder_lin1 = new HashMap<Integer, List<Double>>();
		Map<Integer, List<Double>> wallB_maxFinder_lin1 = new HashMap<Integer, List<Double>>();
		Map<Point3d, List<Double>> sun_maxFinder_lin1 = new HashMap<Point3d, List<Double>>();

		Map<String, List<Double>> aldenMaterial_maxFinder_lin1 = new HashMap<String, List<Double>>();

		// for each non-blank stimulus shown previously, find lineage and add z-score and id to appropriate map
		Map<Long, Double> stimObjId2FRZ_lin1 = new HashMap<Long, Double>();

		for (int gen=1;gen<genNum;gen++) {
			List<Long> allStimObjIds = dbUtil.readAllStimIdsForRun(prefix,runNum,gen);

			DataObject thisZ;
			long currentId;

			for (int n=0;n<allStimObjIds.size();n++) {
				currentId = allStimObjIds.get(n);
				PngObjectSpec pngSpecTemp = PngObjectSpec.fromXml(dbUtil.readStimSpec_java(currentId).getSpec());

				if (Arrays.asList("COMPOSITE","STABILITY","PERTURBATION","BALL","GRASSGRAVITY","ANIMACY_ANIMATE","ANIMACY_STILL","DENSITY","BLANK").contains(pngSpecTemp.getStimType()))
					continue;

				BlenderSpec blendObject = BlenderSpec.fromXml(dbUtil.readStimSpec_blender(currentId).getSpec());
				thisZ = DataObject.fromXml(dbUtil.readStimSpec_data(currentId).getSpec());
				Double zScore = thisZ.getAvgFRminusBkgd()/thisZ.getStdFRplusBkgd();

				if (pngSpecTemp.getStimType().equals("ENVT")) {
					System.out.println(currentId);
					EnviroSpec_class enviroSpec = blendObject.getEnviroSpec();

					//horizonTilt
					if (thisZ.getLineage() == linNum) { 
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

					//horizonSlant
					if (thisZ.getLineage() == linNum) { 
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

					//horizonMaterial
					if (thisZ.getLineage() == linNum) { 
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

					//sun
					if (thisZ.getLineage() == linNum) { 
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

					//distance
					if (thisZ.getLineage() == linNum) { 
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

					//structureMaterial
					if (thisZ.getLineage() == linNum) { 
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

					//floor
					if (thisZ.getLineage() == linNum) { 
						List<Integer> currentKeysFloor = new ArrayList<Integer>(floor_maxFinder_lin1.keySet());
						int thisFloor = enviroSpec.getHasFloor();

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

					//ceiling
					if (thisZ.getLineage() == linNum) { 
						List<Integer> currentKeysCeiling = new ArrayList<Integer>(ceiling_maxFinder_lin1.keySet());
						int thisCeiling = enviroSpec.getHasCeiling();

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

					//wallL
					if (thisZ.getLineage() == linNum) { 
						List<Integer> currentKeysWallL = new ArrayList<Integer>(wallL_maxFinder_lin1.keySet());
						int thisWallL = enviroSpec.getHasWallL();

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

					//wallR
					if (thisZ.getLineage() == linNum) { 
						List<Integer> currentKeysWallR = new ArrayList<Integer>(wallR_maxFinder_lin1.keySet());
						int thisWallR = enviroSpec.getHasWallR();

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

					//wallB
					if (thisZ.getLineage() == linNum) { 
						List<Integer> currentKeysWallB = new ArrayList<Integer>(wallB_maxFinder_lin1.keySet());
						int thisWallB = enviroSpec.getHasWallB();

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
				}

				else if (pngSpecTemp.getStimType().equals("OBJECT")) {
					AldenSpec_class aldenSpec = blendObject.getAldenSpec();

					if (aldenSpec.getIsOptical() == 0) {
						//aldenMaterial
						
						if (thisZ.getLineage() == linNum) { 
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
					}			
				}
			}
		}

		List<String> constantAttributes_lin1 = new ArrayList<String>(); // tilt, slant, sun, distance, floor, ceiling, wallL, wallR, wallB, hMat, sMat, aMat

		Double horizonTilt_decision_lin1 = GAMaths.chooseStimsToMorphComposite_Double(horizonTilt_maxFinder_lin1);
		constantAttributes_lin1.add(Double.toString(horizonTilt_decision_lin1));

		Double horizonSlant_decision_lin1 = GAMaths.chooseStimsToMorphComposite_Double(horizonSlant_maxFinder_lin1);
		constantAttributes_lin1.add(Double.toString(horizonSlant_decision_lin1));

		Point3d sun_decision_lin1 = GAMaths.chooseStimsToMorphComposite_Point3d(sun_maxFinder_lin1);
		String strSun = sun_decision_lin1.x + "," + sun_decision_lin1.y + "," + sun_decision_lin1.z;
		constantAttributes_lin1.add(strSun);

		Double distance_decision_lin1 = GAMaths.chooseStimsToMorphComposite_Double(distance_maxFinder_lin1);
		constantAttributes_lin1.add(Double.toString(distance_decision_lin1));

		int floor_decision_lin1 = GAMaths.chooseStimsToMorphComposite_Integer(floor_maxFinder_lin1);
		constantAttributes_lin1.add(Integer.toString(floor_decision_lin1));
		
		int ceiling_decision_lin1 = GAMaths.chooseStimsToMorphComposite_Integer(ceiling_maxFinder_lin1);
		constantAttributes_lin1.add(Integer.toString(ceiling_decision_lin1));

		int wallL_decision_lin1 = GAMaths.chooseStimsToMorphComposite_Integer(wallL_maxFinder_lin1);
		constantAttributes_lin1.add(Integer.toString(wallL_decision_lin1));

		int wallR_decision_lin1 = GAMaths.chooseStimsToMorphComposite_Integer(wallR_maxFinder_lin1);
		constantAttributes_lin1.add(Integer.toString(wallR_decision_lin1));

		int wallB_decision_lin1 = GAMaths.chooseStimsToMorphComposite_Integer(wallB_maxFinder_lin1);
		constantAttributes_lin1.add(Integer.toString(wallB_decision_lin1));

		String horizonMaterial_decision_lin1 = GAMaths.chooseStimsToMorphComposite_String(horizonMaterial_maxFinder_lin1);
		constantAttributes_lin1.add(horizonMaterial_decision_lin1);

		String structureMaterial_decision_lin1 = GAMaths.chooseStimsToMorphComposite_String(structureMaterial_maxFinder_lin1);
		constantAttributes_lin1.add(structureMaterial_decision_lin1);

		String aldenMaterial_decision_lin1 = GAMaths.chooseStimsToMorphComposite_String(aldenMaterial_maxFinder_lin1);
		constantAttributes_lin1.add(aldenMaterial_decision_lin1);

		System.out.println("Lineage " + linNum + ": " + constantAttributes_lin1);

		// choose stims to morph:
		// which fitness method? 	1 = highest only
		// 							2 = minima, maxima
		//							3 = low, medium, high designation and random selection
		int fitnessMethod = 3;

		// choose best, worst alden stimuli
		List<Long> stimsToMorph_lin1 = GAMaths.choosePostHoc(stimObjId2FRZ_lin1, fitnessMethod); 

		System.out.println("Lineage " + linNum + ": " + stimsToMorph_lin1);

		// lineage 1
		List<Integer> possiblePositions1 = new ArrayList<Integer>();

		if (wallB_decision_lin1 + wallL_decision_lin1 + wallR_decision_lin1 + ceiling_decision_lin1 + floor_decision_lin1 != 0) {

			// make blank stim:	
			blankStimObjIds.add(generator.generateBlankStim(prefix, runNum, genNum, linNum)); 
			System.out.println("Blank stimulus added.");

			stimObjIds.add(generator.generatePHStimAnimacy(prefix, runNum, genNum, linNum, stimsToMorph_lin1.get(0), 0, "COMPOSITE")); 
			System.out.println("Lineage " + linNum + ": Generating and saving environment-only composite placeholder.");

			for (int m=0;m<stimsToMorph_lin1.size();m++) {
				stimObjIds.add(generator.generatePHStimAnimacy(prefix, runNum, genNum, linNum, stimsToMorph_lin1.get(m), m+1, "COMPOSITE")); 
				System.out.println("Lineage " + linNum + ": Generating and saving object-only composite placeholders.");
			}

			possiblePositions1.add(0);

			if (wallB_decision_lin1 == 1) {
				possiblePositions1.add(4);
				possiblePositions1.add(9);
			}

			if (ceiling_decision_lin1 == 1) {
				possiblePositions1.add(2);
				possiblePositions1.add(11);

				if (wallR_decision_lin1 == 1) {
					possiblePositions1.add(6);
					possiblePositions1.add(14);
				}

				if (wallL_decision_lin1 == 1) {
					possiblePositions1.add(7);
					possiblePositions1.add(15);
				}
			}

			if (wallR_decision_lin1 == 1) {
				possiblePositions1.add(1);
				possiblePositions1.add(10);
				possiblePositions1.add(5);
				possiblePositions1.add(13);
			}

			if (wallL_decision_lin1 == 1) {
				possiblePositions1.add(3);
				possiblePositions1.add(8);
				possiblePositions1.add(12);
				possiblePositions1.add(16);
			}

			// ready database for composite spec generation
			int stimNum = stimsToMorph_lin1.size()+1;

//			System.out.println(stimsToMorph_lin1.size());
//			System.out.println(possiblePositions1.size());

			System.out.println("Num possible positions: " + possiblePositions1.size());
			int placeholderNum = 0;
			
			for (int n=0;n<possiblePositions1.size();n++) {

				for (int m=0;m<stimsToMorph_lin1.size();m++) {
					stimObjIds.add(generator.generatePHStimAnimacy(prefix, runNum, genNum, linNum, stimsToMorph_lin1.get(m), stimNum, "COMPOSITE")); 
					System.out.println("Lineage " + linNum + ": Generating and saving composite location " + possiblePositions1.get(n) + ", stimulus " + m + ", placeholder " + placeholderNum);
					stimNum += 1;
					
					stimObjIds.add(generator.generatePHStimAnimacy(prefix, runNum, genNum, linNum, stimsToMorph_lin1.get(m), stimNum, "COMPOSITE")); 
					System.out.println("Lineage " + linNum + ": Generating and saving architecture perspective location " + possiblePositions1.get(n) + ", stimulus " + m + ", placeholder " + placeholderNum);
					stimNum += 1;
					placeholderNum += 1;
				}
			}
		}

		else {
			System.out.println("Lineage " + linNum + ": Optimal architecture not suitable for composite post-hoc.");
			return;
		}

		if (possiblePositions1.size() != 0) {
			System.out.println("Do lineage " + linNum + ".");
			possiblePositions1.add((int) (long) linNum); // document lineage for python
			System.out.println(constantAttributes_lin1);
			System.out.println(possiblePositions1);
			
			BlenderRunnable blenderRunnerComposite_lin1 = new BlenderRunnable(basePath + "compositePostHoc.py",constantAttributes_lin1,possiblePositions1);
//			BlenderRunnable blenderRunnerComposite_lin1 = new BlenderRunnable("/Users/alexandriya/Dropbox/Blender/ProgressionClasses/compositePostHoc.py",constantAttributes_lin1,possiblePositions);
			blenderRunnerComposite_lin1.run();
		}

		int numJobs = stimObjIds.size(); //all R, allL, all non-blank stims in lineage;
		String prefixRunGen = prefix + "_r-" + runNum + "_g-" + genNum + "_l-" + linNum;
		
        BlenderRunnable photoRunner = new BlenderRunnable();
        List<String> args = new ArrayList<String>();
        args.add("ssh");
        args.add("alexandriya@172.30.9.11");
//        args.add(basePath + "masterSubmitScript.sh");
        args.add("/home/alexandriya/blendRend/masterSubmitScript.sh");
        args.add(Integer.toString(numJobs));
        args.add(prefixRunGen);
        photoRunner.setDoWaitFor(false);
        photoRunner.run(args);
		
		// add blanks
		stimObjIds.addAll(blankStimObjIds);	

		// create trial structure, populate stimspec, write task-to-do
		System.out.println("Creating trial spec for this generation.");
		createPHTrialsFromStimObjs(stimObjIds,PngGAParams.GA_numStimsPerTrial);

		// write updated global genId and number of trials in this generation to db:
		int numTasks = (int) Math.ceil(stimObjIds.size()*PngGAParams.GA_numRepsPerStim/PngGAParams.GA_numStimsPerTrial);
		dbUtil.updateReadyGenerationInfo(prefix, runNum, genNum, linNum, numTasks);

		System.out.println("Waiting for render completion...");
		while (dbUtil.readRenderStatus(prefix, runNum, genNum, linNum) == 0) {
			try
			{	Thread.sleep(10000);	}
			catch (Exception e) {System.out.println(e);}
		}
		
		// get acq info and put into db:
		getSpikeResponses();
	}

	void createPHanimacy() {
		List<Long> blankStimObjIds = new ArrayList<Long>();	
		List<Long> stimObjIds = new ArrayList<Long>();

		List<List<Long>> trialGroups = new ArrayList<List<Long>>();
		List<Long> trialSubGroup = new ArrayList<Long>();

		// make blank stims. assuming two linages.		
		long blankStimObjId = generator.generateBlankStim(prefix, runNum, genNum, linNum); 
		blankStimObjIds.add(blankStimObjId);

		trialSubGroup.add(blankStimObjId);
		trialSubGroup.add(blankStimObjId);
		trialSubGroup.add(blankStimObjId);
		trialGroups.add(trialSubGroup);

		trialSubGroup = new ArrayList<Long>();

		System.out.println("Blank stimulus added.");

		// for each non-blank stimulus shown previously, find lineage and add z-score and id to appropriate map
		Map<Long, Double> stimObjId2FRZ_lin1 = new HashMap<Long, Double>();
		Map<Long, Integer> stimObjId2numAnimations_lin1 = new HashMap<Long, Integer>();

		for (int gen=1;gen<genNum;gen++) {
			List<Long> allStimObjIds = dbUtil.readAllStimIdsForRun(prefix,runNum,gen);

			DataObject thisZ;
			long currentId;

			for (int n=0;n<allStimObjIds.size();n++) {
				currentId = allStimObjIds.get(n);
				PngObjectSpec pngSpecTemp = PngObjectSpec.fromXml(dbUtil.readStimSpec_java(currentId).getSpec());

				if (Arrays.asList("COMPOSITE","STABILITY","PERTURBATION","BALL","GRASSGRAVITY","ANIMACY_ANIMATE","ANIMACY_STILL","DENSITY","BLANK").contains(pngSpecTemp.getStimType()))
					continue;

				BlenderSpec blendObject = BlenderSpec.fromXml(dbUtil.readStimSpec_blender(currentId).getSpec());
				thisZ = DataObject.fromXml(dbUtil.readStimSpec_data(currentId).getSpec());
				Double zScore = thisZ.getAvgFRminusBkgd()/thisZ.getStdFRplusBkgd();

				if (pngSpecTemp.getStimType().equals("OBJECT")) {
					AldenSpec_class aldenSpec = blendObject.getAldenSpec();
					MStickSpec stickSpec = MStickSpec.fromXml(dbUtil.readStimSpec_stick(currentId).getSpec());

					if (thisZ.getLineage() == linNum) { 
						// collect object ids and scores
						stimObjId2FRZ_lin1.put(currentId, zScore);

						// find number of endpts
						int numEndPts = stickSpec.getNEndPt();

						// find number of comps with two endpts
						int nDoubleEndPtComps = stickSpec.getNDoubleEndPtComps();

						// determine whether low potential and without precariousness
						int isLowPotential = aldenSpec.getLowPotentialEnergy();

						Point3d precariousness = aldenSpec.getMakePrecarious();
						double precariousnessContents = precariousness.x+precariousness.y+precariousness.z;
						int isNotPrecarious = 0;

						if (precariousnessContents == 0.0) {
							isNotPrecarious = 1;
						}

						int stabilityLegs = 1;

						if (isLowPotential + isNotPrecarious == 2) {
							stabilityLegs = 0;
						}

						// record number of animations in post hoc
						int numPHanimations = numEndPts - nDoubleEndPtComps - stabilityLegs;
						stimObjId2numAnimations_lin1.put(currentId, numPHanimations);
						System.out.println("createPHanimacy() : numPHanimations = " + numPHanimations+","+numEndPts+","+nDoubleEndPtComps+","+stabilityLegs);
					}
				}
			}
		}

		// choose stims to morph:
		// which fitness method? 	1 = highest only
		// 							2 = minima, maxima
		//							3 = low, medium, high designation and random selection
		int fitnessMethod = 3;

		// choose best, worst alden stimuli
		List<Long> stimsToMorph_lin1 = GAMaths.choosePostHoc(stimObjId2FRZ_lin1, fitnessMethod); 

		System.out.println("Lineage " + linNum + ": " + stimsToMorph_lin1);

		List<String> placeholder = new ArrayList<String>();
		List<Integer> limbCounts = new ArrayList<Integer>();
		limbCounts.add(PngGAParams.PH_animacy_numMaterials); // document the number of materials in use
		limbCounts.add(fitnessMethod); // document the number of objects in use per lineage
		limbCounts.add(PngGAParams.targetedColoration); // document whether targeted coloration
		
		//each stimulus has associated number of limbs, should be repeated that many times
		// generatePHStimAnimacy in lieu of generatePHStim drops the face and vert spec save to the database--to save time, animatePostHoc.py references the parent stimulus mesh 
		// that has already been saved in the inherited parent blender spec
		// now, we just have to limit the number of renders executed by the cluster and duplicate images as appropriate...

		// ready database for animacy spec generation
		// minimal descIds
		int stimNum = 0;

		for (int n=0;n<stimsToMorph_lin1.size();n++) {
			long currentId = stimsToMorph_lin1.get(n);
			int numPHanimations = stimObjId2numAnimations_lin1.get(currentId);
			numPHanimations = Math.min(PngGAParams.PH_max_animacy_animations,numPHanimations);
			limbCounts.add(numPHanimations); // document the number of limbs per object

			List<Long> stims_lin1 = new ArrayList<Long>();

			// plain unchanged stimulus
			long whichStim_lin1 = generator.generatePHStimAnimacy(prefix, runNum, genNum, linNum, currentId, stimNum, "ANIMACY_STILL"); // object
			stimObjIds.add(whichStim_lin1);
			stims_lin1.add(whichStim_lin1);
			System.out.println("Lineage " + linNum + ": Generating and saving stimulus " + n + ", conserved stimulus");
			stimNum ++;
			
			trialSubGroup.add(whichStim_lin1);
			trialSubGroup.add(whichStim_lin1);
			trialSubGroup.add(whichStim_lin1);
			trialGroups.add(trialSubGroup);
			
			trialSubGroup = new ArrayList<Long>();

			// include a copy for animation and a copy for still
			for (int c=0;c<PngGAParams.PH_animacy_numMaterials;c++) {
				
				// SQUISH
				if (PngGAParams.targetedColoration!=1) {
					whichStim_lin1 = generator.generatePHStimAnimacy(prefix, runNum, genNum, linNum, currentId, stimNum, "ANIMACY_STILL"); // object
					stimObjIds.add(whichStim_lin1);
					stims_lin1.add(whichStim_lin1);
					System.out.println("Lineage " + linNum + ": Generating and saving stimulus " + n + ", all limbs squish still");
					stimNum ++;
				}
				
				for (int m=0;m<numPHanimations;m++) {
				
					if (PngGAParams.targetedColoration==1) {
						whichStim_lin1 = generator.generatePHStimAnimacy(prefix, runNum, genNum, linNum, currentId, stimNum, "ANIMACY_STILL"); // object
						stimObjIds.add(whichStim_lin1);
						stims_lin1.add(whichStim_lin1);
						System.out.println("Lineage " + linNum + ": Generating and saving stimulus " + n + ", limb " + m + ", squish still");
						stimNum ++;
					}
					
					long whichStim_lin1_anim = generator.generatePHStimAnimacy(prefix, runNum, genNum, linNum, currentId, stimNum, "ANIMACY_ANIMATE");
					stimObjIds.add(whichStim_lin1_anim);
					stims_lin1.add(whichStim_lin1_anim);
					System.out.println("Lineage " + linNum + ": Generating and saving stimulus " + n + ", limb " + m + ", squish animated");
					stimNum ++;
					
					trialSubGroup.add(whichStim_lin1);
					trialSubGroup.add(whichStim_lin1_anim);
					trialSubGroup.add(whichStim_lin1);
					trialGroups.add(trialSubGroup);

					trialSubGroup = new ArrayList<Long>();

				}

				// STIFF
				if (PngGAParams.targetedColoration!=1) {
					whichStim_lin1 = generator.generatePHStimAnimacy(prefix, runNum, genNum, linNum, currentId, stimNum, "ANIMACY_STILL"); // object
					stimObjIds.add(whichStim_lin1);
					stims_lin1.add(whichStim_lin1);
					System.out.println("Lineage " + linNum + ": Generating and saving stimulus " + n + ", all limbs stiff still");
					stimNum ++;

					trialSubGroup.add(whichStim_lin1);
					trialSubGroup.add(whichStim_lin1);
					trialSubGroup.add(whichStim_lin1);
					trialGroups.add(trialSubGroup);

					trialSubGroup = new ArrayList<Long>();
				}

				else {

					for (int m=0;m<numPHanimations;m++) {
						whichStim_lin1 = generator.generatePHStimAnimacy(prefix, runNum, genNum, linNum, currentId, stimNum, "ANIMACY_STILL"); // object
						stimObjIds.add(whichStim_lin1);
						stims_lin1.add(whichStim_lin1);
						System.out.println("Lineage " + linNum + ": Generating and saving stimulus " + n + ", all limbs stiff still");
						stimNum ++;

						trialSubGroup.add(whichStim_lin1);
						trialSubGroup.add(whichStim_lin1);
						trialSubGroup.add(whichStim_lin1);
						trialGroups.add(trialSubGroup);

						trialSubGroup = new ArrayList<Long>();
					}
				}
			}
		}

		System.out.println(basePath + "animatePostHoc.py"+ placeholder+ limbCounts);
		
		BlenderRunnable blenderRunnerAnimate = new BlenderRunnable(basePath + "animatePostHoc.py",placeholder,limbCounts);
//		BlenderRunnable blenderRunnerAnimate = new BlenderRunnable("/Users/alexandriya/Dropbox/Blender/ProgressionClasses/animatePostHoc.py",placeholder,limbCounts);
		blenderRunnerAnimate.run();
		
		int numJobs = stimObjIds.size(); //all R, allL, all non-blank stims in lineages 1 and 2;
		String prefixRunGen = prefix + "_r-" + runNum + "_g-" + genNum + "_l-" + linNum;
		
        BlenderRunnable photoRunner = new BlenderRunnable();
        List<String> args = new ArrayList<String>();
        args.add("ssh");
        args.add("alexandriya@172.30.9.11");
        args.add("/home/alexandriya/blendRend/masterSubmitScript.sh");
        args.add(Integer.toString(numJobs));
        args.add(prefixRunGen);
        photoRunner.setDoWaitFor(false);
        photoRunner.run(args);
		
		// add blanks
		stimObjIds.addAll(blankStimObjIds);	

		// create trial structure, populate stimspec, write task-to-do
		System.out.println("Creating trial spec for lineage " + linNum + " of this generation.");
		createAnimacyTrialsFromStimObjs(trialGroups);

		// write updated global genId and number of trials in this generation to db:
		int numTasks = (int) Math.ceil(trialGroups.size()*PngGAParams.GA_numRepsPerStim);
		dbUtil.updateReadyGenerationInfo(prefix, runNum, genNum, linNum, numTasks);

		System.out.println("Waiting for render completion...");
		while (dbUtil.readRenderStatus(prefix, runNum, genNum, linNum) == 0) {
			try
			{	Thread.sleep(10000);	}
			catch (Exception e) {System.out.println(e);}
		}
		
		// get acq info and put into db:
		getSpikeResponses();
	}
	
	void createPHstability() {
		List<Long> blankStimObjIds = new ArrayList<Long>();	
		List<Long> stimObjIds = new ArrayList<Long>();

		// make blank stims:		
		blankStimObjIds.add(generator.generateBlankStim(prefix, runNum, genNum, linNum));
		System.out.println("Blank stimulus added.");

		// choose stims to morph:
		// which fitness method? 	1 = highest only
		// 							2 = minima, maxima
		//							3 = low, medium, high designation and random selection
		int fitnessMethod = 3;

		ArrayList<List<Long>> stimsToMorph = chooseBestObjs(fitnessMethod); 
		List<Long> stimsToMorph_lin1 = stimsToMorph.get((int)(long)linNum);

		int numMorphs = PngGAParams.PH_stability_numMorphs;
		List<String> placeholder = new ArrayList<String>();
		List<Integer> morphs = new ArrayList<Integer>();
		morphs.add(fitnessMethod); // document the number of objects in use per lineage
		morphs.add(numMorphs); // number of stability morphs that occur per lineage
		
		int stimNum = 0;

		for (int n=0;n<stimsToMorph_lin1.size();n++) {
			long currentId = stimsToMorph_lin1.get(n);

			// includes a copy that shall remain unchanged
			for (int m=0;m<numMorphs*2;m++) {
				stimObjIds.add(generator.generatePHStim(prefix, runNum, genNum, linNum, currentId, stimNum, "STABILITY"));
				System.out.println("Lineage " + linNum + ": Generating and saving stimulus " + n + " number " + m);
				stimNum ++;
			}
		}

		// do low stim all morphs, med stim all morphs, high stim all morphs. first stim in each category is the plain one.
		
		BlenderRunnable blenderRunnerPHGeneric = new BlenderRunnable(basePath + "stabilityPostHoc.py",placeholder,morphs);
//		BlenderRunnable blenderRunnerPHGeneric = new BlenderRunnable(basePath + "ProgressionClasses/stabilityPostHoc.py",placeholder,morphs);
//		BlenderRunnable blenderRunnerPHGeneric = new BlenderRunnable("/Users/ecpc31/Dropbox/Blender/ProgressionClasses/stabilityPostHoc.py",placeholder,morphs);
		blenderRunnerPHGeneric.run();

		int numJobs = stimObjIds.size(); //all R, allL, all non-blank stims in lineage;
		String prefixRunGen = prefix + "_r-" + runNum + "_g-" + genNum + "_l-" + linNum;
		
        BlenderRunnable photoRunner = new BlenderRunnable();
        List<String> args = new ArrayList<String>();
        args.add("ssh");
        args.add("alexandriya@172.30.9.11");
//        args.add(basePath + "masterSubmitScript.sh");
        args.add("/home/alexandriya/blendRend/masterSubmitScript.sh");
        args.add(Integer.toString(numJobs));
        args.add(prefixRunGen);
        photoRunner.setDoWaitFor(false);
        photoRunner.run(args);
		
		// add blanks
		stimObjIds.addAll(blankStimObjIds);	

		// create trial structure, populate stimspec, write task-to-do
		System.out.println("Creating trial spec for this generation.");
		createPHTrialsFromStimObjs(stimObjIds,PngGAParams.GA_numStimsPerTrial);

		// write updated global genId and number of trials in this generation to db:
		int numStimsPerTrial = PngGAParams.GA_numStimsPerTrial;
		int numTasks = (int) Math.ceil(stimObjIds.size()*PngGAParams.GA_numRepsPerStim/numStimsPerTrial);
		dbUtil.updateReadyGenerationInfo(prefix, runNum, genNum, linNum, numTasks);

		System.out.println("Waiting for render completion...");
		while (dbUtil.readRenderStatus(prefix, runNum, genNum, linNum) == 0) {
			try
			{	Thread.sleep(10000);	}
			catch (Exception e) {System.out.println(e);}
		}
		
		// get acq info and put into db:
		getSpikeResponses();
	}

	int combinations(int total, int choose) {
		
		int difference = total-choose;
		int totalFact = 1;
		int chooseFact = 1;
		int diffFact = 1;
		
		for (int c=1;c<=total;c++)
			totalFact *= c;

		if (choose != 0) {
			for (int c=1;c<=choose;c++)
				chooseFact *= c;
		}

		if (difference != 0) {
			for (int c=1;c<=difference;c++)
				diffFact *= c;
		}
		
		return totalFact/(chooseFact*diffFact);
	}
	
	void createPHdensity() {

		// choose best and worst raw obj stimulus?
		// choose top 2 best-performing materials?

		List<Long> blankStimObjIds = new ArrayList<Long>();	
		List<Long> stimObjIds = new ArrayList<Long>();

		// make blank stim:		
		blankStimObjIds.add(generator.generateBlankStim(prefix, runNum, genNum, linNum));
		System.out.println("Blank stimulus added.");

		// choose stims to morph:
		// which fitness method? 	1 = highest only
		// 							2 = minima, maxima
		//							3 = low, medium, high designation and random selection
		int fitnessMethod = 1;

		ArrayList<List<Long>> stimsToMorph = chooseBestObjsMat(fitnessMethod); // choose highest-performing non-optical-material objects
		List<Long> stimsToMorph_lin1 = stimsToMorph.get((int)(long)linNum);

		int numComps_lin1 = 0;
		
		int numMorphs_lin1 = 0;
		int stimNum = 0;

		for (int n=0;n<stimsToMorph_lin1.size();n++) {
			long currentId = stimsToMorph_lin1.get(n);

			MStickSpec stickSpec = MStickSpec.fromXml(dbUtil.readStimSpec_stick(currentId).getSpec());
			numComps_lin1 = stickSpec.getNComponent();
			
			for (int choices=0;choices<=numComps_lin1;choices++) {
				numMorphs_lin1 += combinations(numComps_lin1, choices);
			}

			System.out.println(numMorphs_lin1);
			numMorphs_lin1 -= 1;

			// include a copy that shall remain unchanged
			for (int m=0;m<numMorphs_lin1+1;m++) {
				stimObjIds.add(generator.generatePHStim(prefix, runNum, genNum, linNum, currentId, stimNum, "DENSITY"));
				System.out.println("Lineage " + linNum + ": Generating and saving stimulus " + n + " number " + m);
				stimNum ++;
			}
		}

		Map<Long, Double> stimObjId2FRZ_lin1 = new HashMap<Long, Double>();
		Map<String, List<Double>> aldenMaterial_maxFinder_lin1 = new HashMap<String, List<Double>>();

		for (int gen=1;gen<genNum;gen++) {
			List<Long> allStimObjIds = dbUtil.readAllStimIdsForRun(prefix,runNum,gen);

			DataObject thisZ;
			long currentId;

			for (int n=0;n<allStimObjIds.size();n++) {
				currentId = allStimObjIds.get(n);
				PngObjectSpec pngSpecTemp = PngObjectSpec.fromXml(dbUtil.readStimSpec_java(currentId).getSpec());

				if (Arrays.asList("ENVT","COMPOSITE","STABILITY","PERTURBATION","BALL","GRASSGRAVITY","ANIMACY_ANIMATE","ANIMACY_STILL","DENSITY","BLANK").contains(pngSpecTemp.getStimType()))
					continue;

				BlenderSpec blendObject = BlenderSpec.fromXml(dbUtil.readStimSpec_blender(currentId).getSpec());
				thisZ = DataObject.fromXml(dbUtil.readStimSpec_data(currentId).getSpec());
				Double zScore = thisZ.getAvgFRminusBkgd()/thisZ.getStdFRplusBkgd();

				if (pngSpecTemp.getStimType().equals("OBJECT")) {
					AldenSpec_class aldenSpec = blendObject.getAldenSpec();
					
					if (aldenSpec.getIsOptical() == 0) {
						//aldenMaterial
						if (thisZ.getLineage() == linNum) { 
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
					}
				}
			}
		}

		List<String> constantAttributes = new ArrayList<String>(); // aMats
		constantAttributes.add(Integer.toString(numComps_lin1));
		
		List<String> allMatAttrs_lin1 = GAMaths.chooseStimsToMorphComposite_String_OrderedChoice(aldenMaterial_maxFinder_lin1);
		
		constantAttributes.add(allMatAttrs_lin1.get(allMatAttrs_lin1.size()-2));

		BlenderRunnable blenderRunnerPHDensity = new BlenderRunnable(basePath + "densityPostHoc.py", constantAttributes, "both");
//		BlenderRunnable blenderRunnerPHDensity = new BlenderRunnable(basePath + "ProgressionClasses/densityPostHoc.py", constantAttributes, "both");
//		BlenderRunnable blenderRunnerPHDensity = new BlenderRunnable("/Users/ecpc31/Dropbox/Blender/ProgressionClasses/densityPostHoc.py" + constantAttributes);
		blenderRunnerPHDensity.run();
		
		int numJobs = stimObjIds.size(); //all R, allL, all non-blank stims in lineages 1 and 2;
		String prefixRunGen = prefix + "_r-" + runNum + "_g-" + genNum + "_l-" + linNum;
		
        BlenderRunnable photoRunner = new BlenderRunnable();
        List<String> args = new ArrayList<String>();
        args.add("ssh");
        args.add("alexandriya@172.30.9.11");
//        args.add(basePath + "masterSubmitScript.sh");
        args.add("/home/alexandriya/blendRend/masterSubmitScript.sh");
        args.add(Integer.toString(numJobs));
        args.add(prefixRunGen);
        photoRunner.setDoWaitFor(false);
        photoRunner.run(args);
		
		// add blanks
		stimObjIds.addAll(blankStimObjIds);	

		// create trial structure, populate stimspec, write task-to-do
		System.out.println("Creating trial spec for this generation.");
		createPHTrialsFromStimObjs(stimObjIds,PngGAParams.GA_numStimsPerTrial);

		// write updated global genId and number of trials in this generation to db:
		int numStimsPerTrial = PngGAParams.GA_numStimsPerTrial;
		int numTasks = (int) Math.ceil(stimObjIds.size()*PngGAParams.GA_numRepsPerStim/numStimsPerTrial);
		dbUtil.updateReadyGenerationInfo(prefix, runNum, genNum, linNum, numTasks);

		System.out.println("Waiting for render completion...");
		while (dbUtil.readRenderStatus(prefix, runNum, genNum, linNum) == 0) {
			try
			{	Thread.sleep(10000);	}
			catch (Exception e) {System.out.println(e);}
		}
		
		// get acq info and put into db:
		getSpikeResponses();
	}
	
	void createPHmass() {
		
		List<Long> blankStimObjIds = new ArrayList<Long>();	
		List<Long> stimObjIds = new ArrayList<Long>();

		// make blank stim:		
		blankStimObjIds.add(generator.generateBlankStim(prefix, runNum, genNum, linNum));
		System.out.println("Blank stimulus added.");

		// for each non-blank stimulus shown previously, find lineage and add z-score and id to appropriate map
		Map<Long, Double> stimObjId2FRZ_lin1 = new HashMap<Long, Double>();

		for (int gen=1;gen<genNum;gen++) {
			List<Long> allStimObjIds = dbUtil.readAllStimIdsForRun(prefix,runNum,gen);

			DataObject thisZ;
			long currentId;

			for (int n=0;n<allStimObjIds.size();n++) {
				currentId = allStimObjIds.get(n);
				PngObjectSpec pngSpecTemp = PngObjectSpec.fromXml(dbUtil.readStimSpec_java(currentId).getSpec());

				if (Arrays.asList("COMPOSITE","STABILITY","PERTURBATION","BALL","GRASSGRAVITY","ANIMACY_ANIMATE","ANIMACY_STILL","DENSITY","MASS","BLANK").contains(pngSpecTemp.getStimType()))
					continue;

				thisZ = DataObject.fromXml(dbUtil.readStimSpec_data(currentId).getSpec());
				Double zScore = thisZ.getAvgFRminusBkgd()/thisZ.getStdFRplusBkgd();

				if (pngSpecTemp.getStimType().equals("OBJECT")) {

					if (thisZ.getLineage() == linNum) { 
						// collect object ids and scores
						stimObjId2FRZ_lin1.put(currentId, zScore);
					}
				}
			}
		}

		// choose stims to morph:
		// which fitness method? 	1 = highest only
		// 							2 = minima, maxima
		//							3 = low, medium, high designation and random selection
		int fitnessMethod = 3;

		// choose best, worst alden stimuli
		List<Long> stimsToMorph_lin1 = GAMaths.choosePostHoc(stimObjId2FRZ_lin1, fitnessMethod); 

		System.out.println("Lineage " + linNum + ": " + stimsToMorph_lin1);

		List<String> placeholder = new ArrayList<String>();
		List<Integer> objCounts = new ArrayList<Integer>();
		objCounts.add(fitnessMethod); // document the number of objects in use per lineage

		for (int n=0;n<stimsToMorph_lin1.size();n++) {
			long currentId = stimsToMorph_lin1.get(n);
			long whichStim_lin1 = generator.generatePHStim(prefix, runNum, genNum, linNum, currentId, n, "MASS");
			stimObjIds.add(whichStim_lin1);
			System.out.println("Lineage " + linNum + ": Generating and saving stimulus 0: conserved stimulus");
		}
		
		// call python here to determine which is the limb of interest
		// load blenderspec back in order to extract id of limb of interest (in specGenerator)
		
		BlenderRunnable blenderRunnerAnimate = new BlenderRunnable(basePath + "massPostHoc.py");
//		BlenderRunnable blenderRunnerAnimate = new BlenderRunnable(basePath + "ProgressionClasses/massPostHoc.py");
//		BlenderRunnable blenderRunnerAnimate = new BlenderRunnable("/Users/alexandriya/Dropbox/Blender/ProgressionClasses/massPostHoc.py");
		blenderRunnerAnimate.run();

		// need to wait until python is finished to carry on (wait for a couple seconds for calculation)
		System.out.println("Waiting for python db update.");
		try {
			Thread.sleep(10000);
		}
		catch(InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		System.out.println("Past python db update.");
		
		int stimNum = stimsToMorph_lin1.size();
		
		for (int n=0;n<stimsToMorph_lin1.size();n++) {
			long currentId = stimsToMorph_lin1.get(n);

			// unchanged copy already accounted for
			for (int m=0;m<PngGAParams.PH_bulbousness_morphs.length;m++) {
				long whichStim_lin1 = generator.generatePHControlledMorph(prefix, runNum, genNum, linNum, currentId, stimNum, PngGAParams.PH_bulbousness_morphs[m], "MASS", fitnessMethod);
				stimObjIds.add(whichStim_lin1);
				System.out.println("Lineage " + linNum + ": Generating and saving stimulus " + stimNum);
				stimNum ++;
			}
		}
		
		BlenderRunnable blenderRunnerRefresh = new BlenderRunnable(basePath + "stimRefresh.py",placeholder,objCounts);
//		BlenderRunnable blenderRunnerRefresh = new BlenderRunnable(basePath + "ProgressionClasses/stimRefresh.py",placeholder,objCounts);
//		BlenderRunnable blenderRunnerRefresh = new BlenderRunnable("/Users/alexandriya/Dropbox/Blender/ProgressionClasses/stimRefresh.py",placeholder,objCounts);
		blenderRunnerRefresh.run();
		
//		// create PNG thumbnails (not for blanks)
//		if (doSaveThumbnails) {
//			System.out.println("Saving PNGs.");
//			pngMaker.MakeFromIds(stimObjIds);
//		}
		
		int numJobs = stimObjIds.size(); //all R, allL, all non-blank stims in lineages 1 and 2;
		String prefixRunGen = prefix + "_r-" + runNum + "_g-" + genNum + "_l-" + linNum;
		
//        BlenderRunnable photoRunner = new BlenderRunnable();
//        List<String> args = new ArrayList<String>();
//        args.add("ssh");
//        args.add("alexandriya@172.30.9.11");
////        args.add(basePath + "masterSubmitScript.sh");
//        args.add("/home/alexandriya/blendRend/masterSubmitScript.sh");
//        args.add(Integer.toString(numJobs));
//        args.add(prefixRunGen);
//        photoRunner.setDoWaitFor(false);
//        photoRunner.run(args);
        
		// add blanks
		stimObjIds.addAll(blankStimObjIds);	

		// create trial structure, populate stimspec, write task-to-do
		System.out.println("Creating trial spec for lineage " + linNum + " of this generation.");
		createPHTrialsFromStimObjs(stimObjIds,PngGAParams.GA_numStimsPerTrial); 

		// write updated global genId and number of trials in this generation to db:
		int numTasks = (int) Math.ceil(stimObjIds.size()*PngGAParams.GA_numRepsPerStim/PngGAParams.GA_numStimsPerTrial);
		dbUtil.updateReadyGenerationInfo(prefix, runNum, genNum, linNum, numTasks); 

		System.out.println("Waiting for render completion...");
		while (dbUtil.readRenderStatus(prefix, runNum, genNum, linNum) == 0) { 
			try
			{	Thread.sleep(10000);	}
			catch (Exception e) {System.out.println(e);}
		}
		
		// get acq info and put into db:
		getSpikeResponses();
	}
	
	void createPHgrassGravity() {
		
		List<Long> blankStimObjIds = new ArrayList<Long>();	
		List<Long> stimObjIds = new ArrayList<Long>();

		// make blank stim:		
		blankStimObjIds.add(generator.generateBlankStim(prefix, runNum, genNum, linNum));
		System.out.println("Blank stimulus added.");

		for (int n=0;n<13;n++) {
			stimObjIds.add(generator.generatePHStimBlank(prefix, runNum, genNum, linNum, n, "GRASSGRAVITY"));
			System.out.println("Lineage " + linNum + ": Generating and saving stimulus " + n);
		}

		BlenderRunnable blenderRunnerRefresh = new BlenderRunnable(basePath + "grassGravityPostHoc.py");
//		BlenderRunnable blenderRunnerRefresh = new BlenderRunnable("/Users/alexandriya/Dropbox/Blender/ProgressionClasses/grassGravityPostHoc.py");
		blenderRunnerRefresh.run();
		
//		// create PNG thumbnails (not for blanks)
//		if (doSaveThumbnails) {
//			System.out.println("Saving PNGs.");
//			pngMaker.MakeFromIds(stimObjIds);
//		}
		
		int numJobs = stimObjIds.size(); //all R, allL, all non-blank stims in lineages 1 and 2;
		String prefixRunGen = prefix + "_r-" + runNum + "_g-" + genNum + "_l-" + linNum;
		
        BlenderRunnable photoRunner = new BlenderRunnable();
        List<String> args = new ArrayList<String>();
        args.add("ssh");
        args.add("alexandriya@172.30.9.11");
        args.add("/home/alexandriya/blendRend/masterSubmitScript.sh");
        args.add(Integer.toString(numJobs));
        args.add(prefixRunGen);
        photoRunner.setDoWaitFor(false);
        photoRunner.run(args);
        System.out.println(args);
        
		// add blanks
		stimObjIds.addAll(blankStimObjIds);	

		// create trial structure, populate stimspec, write task-to-do
		System.out.println("Creating trial spec for lineage " + linNum + " of this generation.");
		createPHTrialsFromStimObjs(stimObjIds,PngGAParams.GA_numStimsPerTrial); 

		// write updated global genId and number of trials in this generation to db:
		int numTasks = (int) Math.ceil(stimObjIds.size()*PngGAParams.GA_numRepsPerStim/PngGAParams.GA_numStimsPerTrial);
		dbUtil.updateReadyGenerationInfo(prefix, runNum, genNum, linNum, numTasks); 

		System.out.println("Waiting for render completion...");
		while (dbUtil.readRenderStatus(prefix, runNum, genNum, linNum) == 0) { 
			try
			{	Thread.sleep(10000);	}
			catch (Exception e) {System.out.println(e);}
		}
		
		// get acq info and put into db:
		getSpikeResponses();
	}
	
	void createPHrollingBall() {
		
		List<Long> blankStimObjIds = new ArrayList<Long>();	
		List<Long> stimObjIds = new ArrayList<Long>();

		// make blank stim:		
		blankStimObjIds.add(generator.generateBlankStim(prefix, runNum, genNum, linNum));
		System.out.println("Blank stimulus added.");

		for (int n=0;n<5;n++) {
			stimObjIds.add(generator.generatePHStimBlank(prefix, runNum, genNum, linNum, n, "BALL"));
			System.out.println("Lineage " + linNum + ": Generating and saving stimulus " + n);
		}

		BlenderRunnable blenderRunnerRefresh = new BlenderRunnable(basePath + "rollingBallPostHoc.py");
//		BlenderRunnable blenderRunnerRefresh = new BlenderRunnable("/Users/alexandriya/Dropbox/Blender/ProgressionClasses/rollingBallPostHoc.py");
		blenderRunnerRefresh.run();
		
//		// create PNG thumbnails (not for blanks)
//		if (doSaveThumbnails) {
//			System.out.println("Saving PNGs.");
//			pngMaker.MakeFromIds(stimObjIds);
//		}
		
		int numJobs = stimObjIds.size(); //all R, allL, all non-blank stims in lineages 1 and 2;
		String prefixRunGen = prefix + "_r-" + runNum + "_g-" + genNum + "_l-" + linNum;
		
//        BlenderRunnable photoRunner = new BlenderRunnable();
//        List<String> args = new ArrayList<String>();
//        args.add("ssh");
//        args.add("alexandriya@172.30.9.11");
//        args.add("/home/alexandriya/blendRend/masterSubmitScript.sh");
//        args.add(Integer.toString(numJobs));
//        args.add(prefixRunGen);
//        photoRunner.setDoWaitFor(false);
//        photoRunner.run(args);
//        
		// add blanks
		stimObjIds.addAll(blankStimObjIds);	

		// create trial structure, populate stimspec, write task-to-do
		System.out.println("Creating trial spec for lineage " + linNum + " of this generation.");
		createPHTrialsFromStimObjs(stimObjIds,PngGAParams.GA_numStimsPerTrial); 

		// write updated global genId and number of trials in this generation to db:
		int numStimPerTrial = 1;
		int numTasks = (int) Math.ceil(stimObjIds.size()*PngGAParams.GA_numRepsPerStim/numStimPerTrial);
		dbUtil.updateReadyGenerationInfo(prefix, runNum, genNum, linNum, numTasks); 

		System.out.println("Waiting for render completion...");
		while (dbUtil.readRenderStatus(prefix, runNum, genNum, linNum) == 0) { 
			try
			{	Thread.sleep(10000);	}
			catch (Exception e) {System.out.println(e);}
		}
		
		// get acq info and put into db:
		getSpikeResponses();
	}
	
	void createPHperturbation() {
		List<Long> blankStimObjIds = new ArrayList<Long>();	
		List<Long> stimObjIds = new ArrayList<Long>();

		// make blank stims:		
		blankStimObjIds.add(generator.generateBlankStim(prefix, runNum, genNum, linNum));
		System.out.println("Blank stimulus added.");

		// choose stims to morph:
		// which fitness method? 	1 = highest only
		// 							2 = minima, maxima
		//							3 = low, medium, high designation and random selection
		int fitnessMethod = 3;

		ArrayList<List<Long>> stimsToMorph = chooseBestObjs(fitnessMethod); 
		List<Long> stimsToMorph_lin1 = stimsToMorph.get((int)(long)linNum);

		int numMorphs = PngGAParams.PH_stability_numMorphs;
		List<String> placeholder = new ArrayList<String>();
		List<Integer> morphs = new ArrayList<Integer>();
		morphs.add(fitnessMethod); // document the number of objects in use per lineage
		morphs.add(numMorphs); // number of stability morphs that occur per lineage
		
		int stimNum = 0;

		for (int n=0;n<stimsToMorph_lin1.size();n++) {
			long currentId = stimsToMorph_lin1.get(n);

			// includes a copy that shall remain unchanged
			for (int m=0;m<numMorphs*2;m++) {
				stimObjIds.add(generator.generatePHStim(prefix, runNum, genNum, linNum, currentId, stimNum, "PERTURBATION"));
				System.out.println("Lineage " + linNum + ": Generating and saving stimulus " + n + " number " + m);
				stimNum ++;
			}
		}

		// do low stim all morphs, med stim all morphs, high stim all morphs. first stim in each category is the plain one.
		
		BlenderRunnable blenderRunnerPHGeneric = new BlenderRunnable(basePath + "perturbationPostHoc.py",placeholder,morphs);
//		BlenderRunnable blenderRunnerPHGeneric = new BlenderRunnable("/Users/ecpc31/Dropbox/Blender/ProgressionClasses/stabilityPostHoc.py",placeholder,morphs);
		blenderRunnerPHGeneric.run();

		int numJobs = stimObjIds.size(); //all R, allL, all non-blank stims in lineage;
		String prefixRunGen = prefix + "_r-" + runNum + "_g-" + genNum + "_l-" + linNum;
		
        BlenderRunnable photoRunner = new BlenderRunnable();
        List<String> args = new ArrayList<String>();
        args.add("ssh");
        args.add("alexandriya@172.30.9.11");
        args.add("/home/alexandriya/blendRend/masterSubmitScript.sh");
        args.add(Integer.toString(numJobs));
        args.add(prefixRunGen);
        photoRunner.setDoWaitFor(false);
        photoRunner.run(args);
		
		// add blanks
		stimObjIds.addAll(blankStimObjIds);	

		// create trial structure, populate stimspec, write task-to-do
		System.out.println("Creating trial spec for this generation.");
		createPHTrialsFromStimObjs(stimObjIds,PngGAParams.GA_numStimsPerTrial);

		// write updated global genId and number of trials in this generation to db:
		int numStimsPerTrial = PngGAParams.GA_numStimsPerTrial;
		int numTasks = (int) Math.ceil(stimObjIds.size()*PngGAParams.GA_numRepsPerStim/numStimsPerTrial);
		dbUtil.updateReadyGenerationInfo(prefix, runNum, genNum, linNum, numTasks);

		System.out.println("Waiting for render completion...");
		while (dbUtil.readRenderStatus(prefix, runNum, genNum, linNum) == 0) {
			try
			{	Thread.sleep(10000);	}
			catch (Exception e) {System.out.println(e);}
		}
		
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

				if (Arrays.asList("ENVT","COMPOSITE","STABILITY","PERTURBATION","BALL","GRASSGRAVITY","ANIMACY_ANIMATE","ANIMACY_STILL","DENSITY","BLANK").contains(pngSpecTemp.getStimType())) {
					continue;
				}

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

	ArrayList<List<Long>> chooseBestObjsMat(int fitnessMethod) {

		Map<Long, Double> stimObjId2FRZ_lin1 = new HashMap<Long, Double>();
		Map<Long, Double> stimObjId2FRZ_lin2 = new HashMap<Long, Double>();

		for (int gen=1;gen<genNum;gen++) {
			List<Long> allStimObjIds = dbUtil.readAllStimIdsForRun(prefix,runNum,gen);

			DataObject thisZ;
			long currentId;

			for (int n=0;n<allStimObjIds.size();n++) {
				currentId = allStimObjIds.get(n);
				PngObjectSpec pngSpecTemp = PngObjectSpec.fromXml(dbUtil.readStimSpec_java(currentId).getSpec());

				if (Arrays.asList("ENVT","COMPOSITE","STABILITY","PERTURBATION","BALL","GRASSGRAVITY","ANIMACY_ANIMATE","ANIMACY_STILL","DENSITY","BLANK").contains(pngSpecTemp.getStimType()))
					continue;

				BlenderSpec blendObject = BlenderSpec.fromXml(dbUtil.readStimSpec_blender(currentId).getSpec());
				thisZ = DataObject.fromXml(dbUtil.readStimSpec_data(currentId).getSpec());
				Double zScore = thisZ.getAvgFRminusBkgd()/thisZ.getStdFRplusBkgd();

				if (pngSpecTemp.getStimType().equals("OBJECT")) {
					AldenSpec_class aldenSpec = blendObject.getAldenSpec();
					
					if (aldenSpec.getIsOptical() == 0) {
						if (thisZ.getLineage() == 0) {
							stimObjId2FRZ_lin1.put(currentId, zScore);
						}

						else {
							stimObjId2FRZ_lin2.put(currentId, zScore);
						}
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
			dbUtil.writeTaskToDo(taskId, taskId, -1, genNum, linNum); //#####!

			// JK 6 July don't do this for real
//			dbUtil.writeTaskDone(taskId, taskId, filler); ///!!!!!

			stimCounter = endIdx;
		}
	}

	void createPHTrialsFromStimObjs(List<Long> stimObjIds, int numStimsPerTrial) {
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

		int numTasks = (int) Math.ceil(stimObjIds.size()*PngGAParams.GA_numRepsPerStim/numStimsPerTrial);
		System.out.println(numTasks);

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
			dbUtil.writeTaskToDo(taskId, taskId, -1, genNum, linNum); //#####!
			
			// JK 6 July don't do this for real
//			dbUtil.writeTaskDone(taskId, taskId, filler); ///!!!!!

			stimCounter = endIdx;
		}
	}
	
	void createAnimacyTrialsFromStimObjs(List<List<Long>> trialGroups) {
			
		// -- create trial structure, populate stimspec, write task-to-do

		// first, log stimobjids for each genid:
		//		dbUtil.writeStimObjIdsForEachGenId(genId, stimObjIds);

		// stim repetitions:
		List<List<Long>> allStimTrialsInGen = new ArrayList<List<Long>>();
		for (int n=0;n<PngGAParams.GA_numRepsPerStim;n++) {
			allStimTrialsInGen.addAll(trialGroups);
		}

		// shuffle stimuli:
		Collections.shuffle(allStimTrialsInGen);
		
		// create trials using shuffled stimuli:
		long taskId;
		int stimCounter = 0;
		int filler = 0;

		int numTasks = (int) Math.ceil(trialGroups.size()*PngGAParams.GA_numRepsPerStim);
		System.out.println(numTasks);

		for (int n=0;n<numTasks;n++) {
			taskId = globalTimeUtil.currentTimeMicros();

			// create trialspec using sublist and taskId
			String spec = generator.generateGATrialSpec(allStimTrialsInGen.get(stimCounter));

			if(n==0)
				writeExptFirstTrial(taskId);
			else if(n==numTasks-1)
				writeExptLastTrial(taskId);

			// save spec and tasktodo to db
			dbUtil.writeStimSpec(taskId, spec);
			dbUtil.writeTaskToDo(taskId, taskId, -1, genNum, linNum); //#####!
			 
			// JK 6 July don't do this for real
//			dbUtil.writeTaskDone(taskId, taskId, filler); ///!!!!!

			stimCounter++;
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
//			spikeEntry = spikeCounter.getTaskSpikeByGeneration(prefix,runNum,genNum, 0);
			
			if (useFakeSpikes) {
				spikeEntry = spikeCounter.getFakeTaskSpikeByGeneration(prefix,runNum,genNum,linNum); 
			} else {
				spikeEntry = spikeCounter.getTaskSpikeByGeneration(prefix,runNum,genNum,linNum,0); 
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
	private long getLinNum() { 
		try {
			linNum = dbUtil.readReadyGenerationInfo().getLinId();
			return linNum;
		} catch (VariableNotFoundException e) {
			System.out.println("Could not find linId in database. Writing value of 0.");
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
	private long getLinId(long runNum) { 
		try {
			linNum = dbUtil.readLinIdForRunNum(runNum);
			return linNum;
		} catch (VariableNotFoundException e) {
			System.out.println("Could not find linId in database. Writing value of 0.");
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

package org.xper.png.expt.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import org.xper.png.util.PngMapUtil;

import javax.vecmath.Point3d;

public class GAMaths {
//	@Dependency
//	PngDbUtil dbUtil;
	
	static public List<Long> chooseStimsToMorph(Map<Long, Double> id_fr, int numChild, int method) {
		
		List<Long> allIds = new ArrayList<Long>(id_fr.keySet());
		List<Long> morphIds = new ArrayList<Long>();
		
		id_fr = PngMapUtil.sortByValue(id_fr);
		allIds = new ArrayList<Long>(id_fr.keySet());
		int numIds = allIds.size();
		Long currentId = null;

		// assign probability based on firing rate quintile
		int numPercentDivs = PngGAParams.GA_percentDivs.length;
		int[] stimsDivs = new int[numPercentDivs]; // how many stimuli in each group?
		
		for (int n=0;n<numPercentDivs;n++) {
			stimsDivs[n] = (int)Math.round(numIds*PngGAParams.GA_percentDivs[n]);
		}
		
		int prevStimDiv, thisStimDiv;
		Map<Long, Double> stimFitness = new HashMap<Long, Double>();
		
		for (int n=0;n<numIds;n++) {
			currentId = allIds.get(n);
			prevStimDiv = 0;
			
			for (int m=0;m<numPercentDivs;m++) {
				thisStimDiv = stimsDivs[m];
				
				if (stimsDivs[m] > n) {
					stimFitness.put(currentId,PngGAParams.GA_fracPerPercentDiv[m]/(thisStimDiv-prevStimDiv));
					break;
				}
				
				prevStimDiv = thisStimDiv;
			}
		}
		
		double choice;
		double place;
		
		for (int n=0;n<numChild;n++) {
			choice = Math.random();
			place = 0;
			
			for (int m=0;m<numIds;m++) {
				currentId = allIds.get(m);
				place += stimFitness.get(currentId);
				
				if (choice <= place) break;
			}
			
			morphIds.add(currentId);
		}
		
		return morphIds;
	}
	
	static public Boolean chooseStimsToMorphComposite_Boolean(Map<Boolean, List<Double>> attr_zscore) {
		
		Map<Boolean, Double> attr_zavg = new HashMap<Boolean, Double>();
		List<Boolean> allAttrsRaw = new ArrayList<Boolean>(attr_zscore.keySet());
		double zAvg;
//		System.out.println("RAW "+attr_zscore);
		
		for (int n=0;n<attr_zscore.size();n++) {
			Boolean currentAttr = allAttrsRaw.get(n);
			List<Double> toAvg = attr_zscore.get(currentAttr);
			int zNumber = toAvg.size();
			zAvg = 0;
			
			for (int m=0;m<zNumber;m++)
				zAvg += toAvg.get(m);
			
			zAvg = zAvg/zNumber;
			attr_zavg.put(currentAttr,zAvg);
		}
		
		attr_zavg = PngMapUtil.sortByValue(attr_zavg);
		List<Boolean> allAttrs = new ArrayList<Boolean>(attr_zavg.keySet());
		int numAttrs = allAttrs.size();
//		System.out.println("AVERAGED "+attr_zavg);
		return allAttrs.get(numAttrs-1);
	}

	static public Integer chooseStimsToMorphComposite_Integer(Map<Integer, List<Double>> attr_zscore) {
		
		Map<Integer, Double> attr_zavg = new HashMap<Integer, Double>();
		List<Integer> allAttrsRaw = new ArrayList<Integer>(attr_zscore.keySet());
		double zAvg;
//		System.out.println("RAW "+attr_zscore);
		
		for (int n=0;n<attr_zscore.size();n++) {
			int currentAttr = allAttrsRaw.get(n);
			List<Double> toAvg = attr_zscore.get(currentAttr);
			int zNumber = toAvg.size();
			zAvg = 0;
			
			for (int m=0;m<zNumber;m++)
				zAvg += toAvg.get(m);
			
			zAvg = zAvg/zNumber;
			attr_zavg.put(currentAttr,zAvg);
		}
		
		attr_zavg = PngMapUtil.sortByValue(attr_zavg);
		List<Integer> allAttrs = new ArrayList<Integer>(attr_zavg.keySet());
		int numAttrs = allAttrs.size();
//		System.out.println("AVERAGED "+attr_zavg);
		return allAttrs.get(numAttrs-1);
	}
	
	static public Double chooseStimsToMorphComposite_Double(Map<Double, List<Double>> attr_zscore) {
		
		Map<Double, Double> attr_zavg = new HashMap<Double, Double>();
		List<Double> allAttrsRaw = new ArrayList<Double>(attr_zscore.keySet());
		double zAvg;
//		System.out.println("RAW "+attr_zscore);
		
		for (int n=0;n<attr_zscore.size();n++) {
			Double currentAttr = allAttrsRaw.get(n);
			List<Double> toAvg = attr_zscore.get(currentAttr);
			int zNumber = toAvg.size();
			zAvg = 0;
			
			for (int m=0;m<zNumber;m++)
				zAvg += toAvg.get(m);
			
			zAvg = zAvg/zNumber;
			attr_zavg.put(currentAttr,zAvg);
		}
		
		attr_zavg = PngMapUtil.sortByValue(attr_zavg);
		List<Double> allAttrs = new ArrayList<Double>(attr_zavg.keySet());
		int numAttrs = allAttrs.size();
//		System.out.println("AVERAGED "+attr_zavg);
		return allAttrs.get(numAttrs-1);
	}
	
	static public String chooseStimsToMorphComposite_String(Map<String, List<Double>> attr_zscore) {
		
		Map<String, Double> attr_zavg = new HashMap<String, Double>();
		List<String> allAttrsRaw = new ArrayList<String>(attr_zscore.keySet());
		double zAvg;
//		System.out.println("RAW "+attr_zscore);
		
		for (int n=0;n<attr_zscore.size();n++) {
			String currentAttr = allAttrsRaw.get(n);
			List<Double> toAvg = attr_zscore.get(currentAttr);
			int zNumber = toAvg.size();
			zAvg = 0;
			
			for (int m=0;m<zNumber;m++)
				zAvg += toAvg.get(m);
			
			zAvg = zAvg/zNumber;
			attr_zavg.put(currentAttr,zAvg);
		}
		
		attr_zavg = PngMapUtil.sortByValue(attr_zavg);
		List<String> allAttrs = new ArrayList<String>(attr_zavg.keySet());
		int numAttrs = allAttrs.size();
//		System.out.println("AVERAGED "+attr_zavg);
		return allAttrs.get(numAttrs-1);
	}
	
	static public List<String> chooseStimsToMorphComposite_String_OrderedChoice(Map<String, List<Double>> attr_zscore) {
		
		Map<String, Double> attr_zavg = new HashMap<String, Double>();
		List<String> allAttrsRaw = new ArrayList<String>(attr_zscore.keySet());
		double zAvg;
		
		for (int n=0;n<attr_zscore.size();n++) {
			String currentAttr = allAttrsRaw.get(n);
			List<Double> toAvg = attr_zscore.get(currentAttr);
			int zNumber = toAvg.size();
			zAvg = 0;
			
			for (int m=0;m<zNumber;m++)
				zAvg += toAvg.get(m);
			
			zAvg = zAvg/zNumber;
			attr_zavg.put(currentAttr,zAvg);
		}
		
		attr_zavg = PngMapUtil.sortByValue(attr_zavg);
		List<String> allAttrs = new ArrayList<String>(attr_zavg.keySet());
		return allAttrs;
	}
	
	static public Point3d chooseStimsToMorphComposite_Point3d(Map<Point3d, List<Double>> attr_zscore) {
		
		Map<Point3d, Double> attr_zavg = new HashMap<Point3d, Double>();
		List<Point3d> allAttrsRaw = new ArrayList<Point3d>(attr_zscore.keySet());
		double zAvg;
//		System.out.println("RAW "+attr_zscore);
		
		for (int n=0;n<attr_zscore.size();n++) {
			Point3d currentAttr = allAttrsRaw.get(n);
			List<Double> toAvg = attr_zscore.get(currentAttr);
			int zNumber = toAvg.size();
			zAvg = 0;
			
			for (int m=0;m<zNumber;m++)
				zAvg += toAvg.get(m);
			
			zAvg = zAvg/zNumber;
			attr_zavg.put(currentAttr,zAvg);
		}
		
		attr_zavg = PngMapUtil.sortByValue(attr_zavg);
		List<Point3d> allAttrs = new ArrayList<Point3d>(attr_zavg.keySet());
		int numAttrs = allAttrs.size();
//		System.out.println("AVERAGED "+attr_zavg);
		return allAttrs.get(numAttrs-1);
	}
	static public List<Long> choosePostHoc(Map<Long, Double> id_fr, int method) {
		
		List<Long> allIds = new ArrayList<Long>(id_fr.keySet());
		System.out.println(allIds);
		List<Long> morphIds = new ArrayList<Long>();
		
		id_fr = PngMapUtil.sortByValue(id_fr);
		allIds = new ArrayList<Long>(id_fr.keySet());
		int numIds = allIds.size();
		Long currentId = null;
		
		int numPercentDivs = PngGAParams.PH_percentDivs.length;
		int[] stimsDivs = new int[numPercentDivs]; // how many stimuli in each group?

		switch (method) {
		case 1:
			morphIds.add(allIds.get(numIds-1));					// highest stimulus
			break;
		case 2:
			int numHigh = PngGAParams.PH_numResponders_highLow;
			int numLow = PngGAParams.PH_numResponders_highLow;

			if (numHigh+numLow < numIds) {

				for (int n=0;n<numLow;n++) {
					// lowest first
					currentId = allIds.get(n);
					morphIds.add(currentId);
				}

				for (int n=numIds-1;n>=numIds-numHigh;n--) {
					// highest first
					currentId = allIds.get(n);
					morphIds.add(currentId);
				}
			}
			else
				morphIds = allIds;
			break;
		case 3:
			List<Long> lows = new ArrayList<Long>(); 			// lower 30 percent
			List<Long> mediums = new ArrayList<Long>(); 			// middle 60 percent
			List<Long> highs = new ArrayList<Long>(); 			// upper 10 percent

			for (int n=0;n<numPercentDivs;n++) {
				stimsDivs[n] = (int)Math.round(numIds*PngGAParams.PH_percentDivs[n]);
			}

			for (int n=0;n<numIds;n++) {
				if (n < stimsDivs[0]) {
					lows.add(allIds.get(n));
				}
				else if (n < stimsDivs[1]) {
					mediums.add(allIds.get(n));
				}
				else if (n < numIds) {
					highs.add(allIds.get(n));
				}
			}
			int choice;
			choice = new Random().nextInt(stimsDivs[0]);
			morphIds.add(lows.get(choice));

			choice = new Random().nextInt(stimsDivs[1]-stimsDivs[0]);
			morphIds.add(mediums.get(choice));

			choice = new Random().nextInt(stimsDivs[2]-stimsDivs[1]);
			morphIds.add(highs.get(choice));
			break;
			
		case 4:
			// select PH_numObjects_fitnessMethod highest stimuli
			
			for (int n=1;n<=PngGAParams.PH_numObjects_fitnessMethod;n++) {
				morphIds.add(allIds.get(allIds.size()-n));
			}
			
//			List<Long> highResponders = new ArrayList<Long>(); 			// upper 10 percent
//			
//			for (int n=0;n<numPercentDivs;n++) {
//				stimsDivs[n] = (int)Math.round(numIds*PngGAParams.PH_percentDivs[n]);
//			}
//
//			for (int n=0;n<numIds;n++) {
//				if (n >= stimsDivs[1]) {
//					highResponders.add(allIds.get(n));
//				}
//			}
//
//			int numDistinctObjs;
//			
//			if (highResponders.size()<PngGAParams.PH_numObjects_fitnessMethod) {
//				numDistinctObjs = highResponders.size();
//			} else {
//				numDistinctObjs = PngGAParams.PH_numObjects_fitnessMethod;
//			}
//			
//			System.out.println(numDistinctObjs);
//			
//			for (int n=0;n<numDistinctObjs;n++) {
//				choice = new Random().nextInt(stimsDivs[2]-stimsDivs[1]);
//				morphIds.add(highResponders.get(choice));
//			}
			break;
			
		}
		return morphIds;
	}
}

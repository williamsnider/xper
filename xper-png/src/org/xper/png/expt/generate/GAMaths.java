package org.xper.png.expt.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

import org.xper.Dependency;
import org.xper.png.drawing.stimuli.PngObjectSpec;
import org.xper.png.drawing.stimuli.BlenderSpec;
import org.xper.png.parsedata.DataObject;
import org.xper.png.util.PngDbUtil;
import org.xper.png.util.PngMapUtil;

public class GAMaths {
	@Dependency
	PngDbUtil dbUtil;
	
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
	
	public List<Long> chooseStimsToMorphComposite(Map<Long, Double> id_fr, int numChild, int method) {
		
		List<Long> allIds = new ArrayList<Long>(id_fr.keySet());
		int numIds = allIds.size();
		Long currentId = null;
		BlenderSpec data;
		
		Map<String, Double> enviroAttrs = new HashMap<String, Double>();
		List<Long> morphIds = new ArrayList<Long>();
		
		// in composite case, need to take in allIds Z-scores. look at bspec of each Id to make maps of attribute to 
		
	// separate obj and env.
		// find normal max of obj in shape. (sort, etc.)
		// only one absolute winner for obj? perhaps make controllable in params
		// find max obj material
		
		// for env, find max env horizonTilt, horizonMaterial, distance, structureMaterial, architecture (floor, ceiling, wallL, wallR, wallB)
		
		// how?
		// only one absolute winner for the above attributes.
		// have ids to z-scores. these z-scores are matched to attributes. no longer care about ids, just attributes. make a new map with attribute to 
		// average Z-score in each case. (or Z-score of Z-score...? haha... yeah, I guess.)
		
		//can get from value
		// ideally look at each id only once
		
		for (int n=0;n<numIds;n++) {
// can just get blendspec and check there
			currentId = allIds.get(n);
			PngObjectSpec pngSpecTemp = PngObjectSpec.fromXml(dbUtil.readStimSpec_java(currentId).getSpec());
			
			if (pngSpecTemp.getStimType().equals("ENVT")) {
				BlenderSpec blendObject = BlenderSpec.fromXml(dbUtil.readStimSpec_blender(currentId).getSpec());
//				data = BlendObject.fromXml(dbUtil.readStimSpec_blender(currentId).getSpec());
//				
//				data.getLineage() == 0;
//				
//				enviroAttrs.put(stimObjId, zScore);
//				
			}
			
		}
		
//		 (do morph on architecture, position default settings)
		
		return morphIds;
	}

}

package org.xper.png.expt.generate;

public final class PngGAParams {
	public static final int GA_maxNumGens = 10;
	public static final int GA_numNonBlankStimsPerLin = 10;	//40
	public static final int GA_numStimsPerLin = GA_numNonBlankStimsPerLin + 1;
	public static final int GA_numRepsPerStim = 5;			
	public static final int GA_numStimsPerTrial = 4;
	public static final int GA_numLineages = 2;
	public static final int GA_numTasks = (int) Math.ceil(GA_numStimsPerLin*GA_numLineages*GA_numRepsPerStim/GA_numStimsPerTrial);
	
	public static double GA_randgen_prob_objvsenvt = 0.5; 					// probability random stimulus will be alden-containing "Object" (vs "Environment")
	
	public static int GA_morph_numNewStimPerLin = 5; 							// number of new random stimuli per lineage (10% - 20% of total stimuli)
	public static double GA_morph_prob_stick = 0.7; 							// probability of stick spec morph: new stick or stick morph (vs blender spec morph)
	public static double GA_morph_prob_stick_new = 0.1; 						// probability stick spec morph will produce new stick (vs stick morph)
	
	public static double[] GA_percentDivs = {0.3,0.5,0.7,0.9,1.0}; 			// thresholds on Z-score performance distribution for response binning
	public static double[] GA_fracPerPercentDiv = {0.1,0.15,0.2,0.2,0.35}; 	// percent of stimuli to morph chosen from each GA_percentDivs bin
}

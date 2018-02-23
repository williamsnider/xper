package org.xper.png.expt.generate;

public final class PngGAParams {
	public static final int GA_maxNumGens = 10;
	public static final int GA_numNonBlankStimsPerLin = 40;	
	public static final int GA_numStimsPerLin = GA_numNonBlankStimsPerLin + 1;
	public static final int GA_numRepsPerStim = 5;			
	public static final int GA_numStimsPerTrial = 4;
	public static final int GA_numLineages = 2;
	public static final int GA_numTasks = (int) Math.ceil(GA_numStimsPerLin*GA_numLineages*GA_numRepsPerStim/GA_numStimsPerTrial);
	
	public static double GA_randgen_prob_objvsenvt = 0.5;
	
	public static int GA_morph_numNewStimPerLin = 5;
	public static double GA_morph_prob_stick = 0.5;
}

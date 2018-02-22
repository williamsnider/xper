package org.xper.png.expt; 

import java.util.List;

import org.xper.Dependency;
import org.xper.drawing.renderer.AbstractRenderer;
import org.xper.png.drawing.stick.MStickSpec;
import org.xper.png.drawing.stimuli.PngObject;
import org.xper.png.drawing.stimuli.PngObjectSpec;
import org.xper.png.expt.generate.PngGAParams;
import org.xper.png.expt.generate.PngRandomGeneration.TrialType;
import org.xper.png.parsedata.DataObject;
import org.xper.png.expt.generate.PngStimSpecGenerator;
import org.xper.png.util.PngDbUtil;
import org.xper.time.TimeUtil;

public class PngExptSpecGenerator implements PngStimSpecGenerator {

	@Dependency
	PngDbUtil dbUtil;
	@Dependency
	TimeUtil globalTimeUtil;
	@Dependency
	AbstractRenderer renderer;
	
	static public enum StimType { OBJECT, ENVT, COMPOSITE, BLANK };

	TrialType trialType;	
	long taskId;	
	
	
	// --------------------------------
	// ---- GA stimulus generation ----
	// --------------------------------
	
	public long generateBlankStim(String prefix, long runNum, long gen, int lineage) {
		// GENERATE STIM	
		long stimObjId = globalTimeUtil.currentTimeMicros();

		PngObjectSpec s = new PngObjectSpec();
		DataObject d = new DataObject();
		
		// -- set spec values
		
		String descId = prefix + "_r-" + runNum + "_g-" + gen + "_l-" + lineage + "_s-BLANK";
		
		s.setId(stimObjId);
		s.setDescId(descId);
		s.setStimType(StimType.BLANK.toString());
		
		s.setGaPrefix(prefix);
		s.setGaRunNum(runNum);

		// -- set data values
		d.setStimObjId(stimObjId);
		d.setTrialType(TrialType.GA.toString());
		d.setRunNum(runNum);
		d.setBirthGen(gen);
		d.setLineage(lineage);
		
		// create stimObjId and add it to this and PngStimDataEntry, then write them to the DB
		dbUtil.writeStimObjData(stimObjId,descId, s.toXml(), "", "", d.toXml());
		
		return stimObjId;
	}
	
	public long generateRandStim(String prefix, long runNum, long gen, int lineage, int stimNum) {
		// GENERATE STIM	
		long stimObjId = globalTimeUtil.currentTimeMicros();

		PngObjectSpec s = new PngObjectSpec();
		DataObject d = new DataObject();
		
		// -- set spec values
		
		String descId = prefix + "_r-" + runNum + "_g-" + gen + "_l-" + lineage + "_s-" + stimNum;
		
		s.setId(stimObjId);
		s.setDescId(descId);
		if (Math.random() > PngGAParams.GA_randgen_prob_objvsenvt)
			s.setStimType(StimType.OBJECT.toString());
		else
			s.setStimType(StimType.ENVT.toString());
		
		s.setGaPrefix(prefix);
		s.setGaRunNum(runNum);
		s.setDoStickGen(true);
		
		PngObject object = new PngObject();
		object.setSpec_java(s);
		PngObjectSpec jspec = object.getSpec_java();
		MStickSpec stickspec = object.getSpec_stick();
		
		// -- set data values
		d.setStimObjId(stimObjId);
		d.setTrialType(TrialType.GA.toString());
		d.setRunNum(runNum);
		d.setBirthGen(gen);
		d.setLineage(lineage);
		
		// create stimObjId and add it to this and PngStimDataEntry, then write them to the DB
		dbUtil.writeStimObjData(stimObjId, descId, jspec.toXml(), stickspec.toXml(), "", d.toXml());
		
		return stimObjId;
	}
	
	public long generateMorphStim(String prefix, long runNum, long gen, int lineage, long parentId, int stimNum) {
		// GENERATE STIM	
		long stimObjId = globalTimeUtil.currentTimeMicros();
		
		// PARENT STIM
		PngObjectSpec parent_pngSpec = PngObjectSpec.fromXml(dbUtil.readStimSpec_java(parentId).getSpec());
		MStickSpec parent_stickSpec = MStickSpec.fromXml(dbUtil.readStimSpec_stick(parentId).getSpec());
		String parent_blenderSpec = dbUtil.readStimSpec_blender(parentId).getSpec();
		
		String descId = prefix + "_r-" + runNum + "_g-" + gen + "_l-" + lineage + "_s-" + stimNum;
		
		PngObjectSpec s = new PngObjectSpec();
		DataObject d = new DataObject();
		
		s.setId(stimObjId);
		s.setDescId(descId);
		s.setStimType(parent_pngSpec.getStimType());
		
		s.setGaPrefix(prefix);
		s.setGaRunNum(runNum);
		
		if (Math.random() > PngGAParams.GA_morph_prob_stick)
			s.setDoStickMorph(true);
		else
			s.setDoBlenderMorph(true);
		
		PngObject object = new PngObject();
		object.setSpec_stick(parent_stickSpec);
		object.setSpec_java(s);
		PngObjectSpec jspec = object.getSpec_java();
		MStickSpec stickspec = object.getSpec_stick();

		// -- set data values
		d.setStimObjId(stimObjId);
		d.setTrialType(TrialType.GA.toString());
		d.setRunNum(runNum);
		d.setBirthGen(gen);
		d.setLineage(lineage);
		
		// create stimObjId and add it to this and PngStimDataEntry, then write them to the DB
		dbUtil.writeStimObjData(stimObjId, descId, jspec.toXml(), stickspec.toXml(), parent_blenderSpec, d.toXml());
		
		return stimObjId;
	}
	
	
	public PngExptSpec generateGATrial(List<Long> stimObjIds, String trialType) {
		
		// TRIAL SETUP 
		PngExptSpec g = new PngExptSpec(); 	// spec for each trial
		int numObjects = stimObjIds.size();		// number of objects in this trial
		
		// ADD STIMULI TO TRIAL
		for (int n=0;n<numObjects;n++) {
			g.addStimObjId(stimObjIds.get(n));
		}
				
		// TRIAL SPECS
		g.setTrialType(trialType);						// shows whether trial is Behavioral or GA
		g.setReward(150);						// fixed reward size
				
		return g;
	}

	
	// ------------------------
	// ---- output methods ----
	// ------------------------
	
	public String generateGATrialSpec(List<Long> stimObjIds) {
		return this.generateGATrial(stimObjIds,TrialType.GA.toString()).toXml();
	}

	
	// -----------------------------
	// ---- setters and getters ----
	// -----------------------------

	public long getTaskId() {
		return taskId;
	}
	public void setTaskId(long id) {
		taskId = id;
	}
	
	public PngDbUtil getDbUtil() {
		return dbUtil;
	}
	public void setDbUtil(PngDbUtil dbUtil) {
		this.dbUtil = dbUtil;
	}

	public TimeUtil getGlobalTimeUtil() {
		return globalTimeUtil;
	}
	public void setGlobalTimeUtil(TimeUtil globalTimeUtil) {
		this.globalTimeUtil = globalTimeUtil;
	}

	public AbstractRenderer getRenderer() {
		return renderer;
	}
	public void setRenderer(AbstractRenderer renderer) {
		this.renderer = renderer;
	}

	public TrialType getTrialType() {
		return trialType;
	}
	public void setTrialType(TrialType trialType) {
		this.trialType = trialType;
	}
}

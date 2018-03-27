package org.xper.png.parsedata;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import org.xper.acq.counter.TrialStageData;
import org.xper.png.util.PngMathUtil;

import com.thoughtworks.xstream.XStream;

public class DataObject {

	// pre-run info:
	String trialType;									// "BEH" or "GA"
	int lineage = -1;									// for GA stim, the lineage in which it arose (prob 0 or 1)
	long runNum = -1;
	long birthGen = -1;									// for GA stim, the generation in which it first arose
	long parentId = -1;									// for GA stim, the parent stimObjId from which it was derived (-1 if no parent)
	long stimObjId;										// index into StimObjData db table
//	List<Long> stimSpecIds = new ArrayList<Long>();		// array of trials (indexed by StimSpec id) in which stim obj can be found
//	List<Long> taskToDoIds = new ArrayList<Long>();		// array of tasks (indexed by TaskToDo id) in which stim obj can be found
	
	// post-run info:
	double sampleFrequency;

	List<Long> taskDoneIds = new ArrayList<Long>();							// array of tasks (indexed by TaskDone id) in which stim obj was presented
	List<TrialStageData> trialStageData = new ArrayList<TrialStageData>();	// array of spike data for each stimulus presentation
	List<Double> spikesPerSec = new ArrayList<Double>();					// firing rate for each stimulus presentation
	List<Double> bkgdSpikesPerSec = new ArrayList<Double>();				// firing rate for any blank stimuli shown in the same generation

	double avgFR;															
	double stdFR;
	double avgBkgdFR = 0;	// this is calculated from any blank stimuli run in the same generation
	double stdBkgdFR;
	
//	List<BsplineObjectSpec> stimObjSpecs = new ArrayList<BsplineObjectSpec>();	// stimulus details for each stimulus presentation (useful for Beh stimuli when morphing or randomizing limb lengths)

	
	transient static XStream s;
	
	static {
		s = new XStream();
		s.alias("Data", DataObject.class);
//		s.addImplicitCollection(AlexStimDataEntry.class, "objects", "object", AlexStimDataEntry.class);
		
//		s.alias("limb", LimbSpec.class);
//		s.addImplicitCollection(BsplineObjectSpec.class, "limbs", "limb", LimbSpec.class);
	}

	public String toXml() {
		return DataObject.toXml(this);
	}
	
	public static String toXml(DataObject spec) {
		return s.toXML(spec);
	}
	
	public static DataObject fromXml(String xml) {
		DataObject g = (DataObject)s.fromXML(xml);
		return g;
	}

	// setters & getters:
	
	public long getStimObjId() {
		return stimObjId;
	}
	public void setStimObjId(long stimObjId) {
		this.stimObjId = stimObjId;
	}
	public String getTrialType() {
		return trialType;
	}
	public void setTrialType(String type) {
		this.trialType = type;
	}
	public int getLineage() {
		return lineage;
	}
	public void setLineage(int lineage) {
		this.lineage = lineage;
	}
	public long getBirthGen() {
		return birthGen;
	}
	public void setBirthGen(long birthGen) {
		this.birthGen = birthGen;
	}
	public long getRunNum() {
		return runNum;
	}
	public void setRunNum(long runNum) {
		this.runNum = runNum;
	}
	public long getParentId() {
		return parentId;
	}
	public void setParentId(long parentId) {
		this.parentId = parentId;
	}
	
//	public List<Long> getStimSpecIds() {
//		return stimSpecIds;
//	}
//	public long getStimSpecId(int i) {
//		return stimSpecIds.get(i);
//	}
//	public void setStimSpecIds(List<Long> stimSpecIds) {
//		this.stimSpecIds = stimSpecIds;
//	}
//	public void addStimSpecId(long id) {
//		stimSpecIds.add(id);
//	}
//	
//	public List<Long> getTaskToDoIds() {
//		return taskToDoIds;
//	}
//	public long getTaskToDoId(int i) {
//		return taskToDoIds.get(i);
//	}
//	public void setTaskToDoIds(List<Long> taskToDoIds) {
//		this.taskToDoIds = taskToDoIds;
//	}
//	public void addTaskToDoId(long id) {
//		taskToDoIds.add(id);
//	}
	
	// data:
	public double getSampleFrequency() {
		return sampleFrequency;
	}
	public void setSampleFrequency(double sampleFrequency) {
		this.sampleFrequency = sampleFrequency;
	}
	
	public List<Long> getTaskDoneIds() {
		return taskDoneIds;
	}
	public long getTaskDoneId(int i) {
		return taskDoneIds.get(i);
	}
	public void setTaskDoneIds(List<Long> taskDoneIds) {
		this.taskDoneIds = taskDoneIds;
	}
	public void addTaskDoneId(long d) {
		taskDoneIds.add(d);
	}

	public List<TrialStageData> getTrialStageData() {
		return trialStageData;
	}
	public TrialStageData getTrialStageData(int i) {
		return trialStageData.get(i);
	}
	public void setTrialStageData(List<TrialStageData> data) {
		this.trialStageData = data;
	}
	public void addTrialStageData(TrialStageData d) {
		trialStageData.add(d);
	}
	
	public List<Double> getSpikesPerSec() {
		return spikesPerSec;
	}
	public double getSpikesPerSec(int i) {
		return spikesPerSec.get(i);
	}
	public void setSpikesPerSec(List<Double> spikesPerSec) {
		this.spikesPerSec = spikesPerSec;
		
		setAvgFR(PngMathUtil.mean(spikesPerSec));
		setStdFR(PngMathUtil.std(spikesPerSec));
	}
	public void addSpikesPerSec(double r) {
		spikesPerSec.add(r);
		
		// also set the avg and std FRs:
		DescriptiveStatistics stats = DescriptiveStatistics.newInstance();

		for (int n=0;n<getNumPresentations();n++) {
			stats.addValue(spikesPerSec.get(n));
		}
		
		setAvgFR(stats.getMean());
		setStdFR(stats.getStandardDeviation());
	}

	public int getNumPresentations() {
		return taskDoneIds.size();
	}
	
	public double getAvgFR() {
		return avgFR;
	}
	public double getAvgFRminusBkgd() {
		return avgFR-avgBkgdFR;
	}
	private void setAvgFR(double avgFR) {
		this.avgFR = avgFR;
	}
	public double getStdFR() {
		return stdFR;
	}
	public double getStdFRplusBkgd() {
		return stdFR+stdBkgdFR;
	}
	private void setStdFR(double stdFR) {
		this.stdFR = stdFR;
	}
	
	public List<Double> getBkgdSpikesPerSec() {
		return bkgdSpikesPerSec;
	}
	public double getBkgdSpikesPerSec(int i) {
		return bkgdSpikesPerSec.get(i);
	}
	public void setBkgdSpikesPerSec(List<Double> bkgdSpikesPerSec) {
		this.bkgdSpikesPerSec = bkgdSpikesPerSec;
		
		setBkgdAvgFR(PngMathUtil.mean(bkgdSpikesPerSec));
		setBkgdStdFR(PngMathUtil.std(bkgdSpikesPerSec));
	}
	public void addBkgdSpikesPerSec(double r) {
		bkgdSpikesPerSec.add(r);
		
		setBkgdAvgFR(PngMathUtil.mean(bkgdSpikesPerSec));
		setBkgdStdFR(PngMathUtil.std(bkgdSpikesPerSec));
	}
	
	public double getBkgdAvgFR() {
		return avgBkgdFR;
	}
	private void setBkgdAvgFR(double avgBkgdFR) {
		this.avgBkgdFR = avgBkgdFR;
	}
	public double getBkgdStdFR() {
		return stdBkgdFR;
	}
	private void setBkgdStdFR(double stdBkgdFR) {
		this.stdBkgdFR = stdBkgdFR;
	}
}

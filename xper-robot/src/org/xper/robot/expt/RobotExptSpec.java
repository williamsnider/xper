package org.xper.robot.expt;

import java.util.ArrayList;
import java.util.List;

import org.xper.robot.drawing.stimuli.PngObjectSpec;

import com.thoughtworks.xstream.XStream;

public class RobotExptSpec {
	String trialType;
	long reward;
	List<Long> objects = new ArrayList<Long>();
	String baseFilename;

	transient static XStream s;
	
	static {
		s = new XStream();
		s.alias("StimSpec", RobotExptSpec.class);
		s.alias("object", PngObjectSpec.class);
		
		s.addImplicitCollection(RobotExptSpec.class, "objects", "object", Long.class);
		
	}
	
	public void addStimObjId(long id) {
		objects.add(id);
	}
	
	public long getStimObjId(int index) {
		if (index < 0 || index >= objects.size()) {
			return -1;
		} else {
			return objects.get(index);
		}
	}
	
	public int getStimObjIdCount() {
		return objects.size();
	}
	
	public String toXml() {
		return RobotExptSpec.toXml(this);
	}
	
	public static String toXml(RobotExptSpec spec) {
		return s.toXML(spec);
	}
	
	public static RobotExptSpec fromXml(String xml) {
		RobotExptSpec g = (RobotExptSpec)s.fromXML(xml);
		return g;
	}

	public long getReward() {
		return reward;
	}

	public void setReward(long reward) {
		this.reward = reward;
	}

	public String getTrialType() {
		return trialType;
	}

	public void setTrialType(String trialType) {
		this.trialType = trialType;
	}
		
}

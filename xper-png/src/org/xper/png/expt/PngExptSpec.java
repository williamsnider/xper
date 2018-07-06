package org.xper.png.expt;

import java.util.ArrayList;
import java.util.List;

//import org.xper.drawing.Coordinates2D;
import org.xper.png.drawing.stimuli.PngObjectSpec;

import com.thoughtworks.xstream.XStream;

public class PngExptSpec {
	String trialType;
	long reward;
	List<Long> objects = new ArrayList<Long>();
	String baseFilename;

	transient static XStream s;
	
	static {
		s = new XStream();
		s.alias("StimSpec", PngExptSpec.class);
		s.alias("object", PngObjectSpec.class);
		
		s.addImplicitCollection(PngExptSpec.class, "objects", "object", Long.class);
		
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
		return PngExptSpec.toXml(this);
	}
	
	public static String toXml(PngExptSpec spec) {
		return s.toXML(spec);
	}
	
	public static PngExptSpec fromXml(String xml) {
		PngExptSpec g = (PngExptSpec)s.fromXML(xml);
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

	// JK
	public void setBaseFilename(String baseFilename){
		this.baseFilename = baseFilename;
	}
	
	
	public String getBaseFilename(){
		return this.baseFilename;
	}
		
}

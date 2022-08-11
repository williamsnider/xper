package org.xper.robot.drawing.stimuli;

import java.io.BufferedWriter;
import java.io.FileWriter;

import com.thoughtworks.xstream.XStream;


public class BlenderSpec {
	AldenSpec_class AldenSpec = new AldenSpec_class();
	EnviroSpec_class EnvironmentSpec = new EnviroSpec_class();
	
	String stimulusID;
	String parentID;
	String morph;

	// general
	String monkeyID;
	double monkeyPerspectiveAngle;
	double monkeyDistanceY;
	double monkeyDistanceZ;
	double eyeSeparation;
	int cameraLens_mm;
	double cameraSensorWidth_mm;
	double architectureScale;
	int overallScale;
	
	transient static XStream s;

	static {
		s = new XStream();
		s.alias("BlenderSpec", BlenderSpec.class);
		s.alias("AldenSpec", AldenSpec_class.class);
		s.alias("EnvironmentSpec", EnviroSpec_class.class);
		
		s.alias("int", Integer.class);
		s.addImplicitCollection(BlenderSpec.class,"affectedLimbs","int",Integer.class);
		
		s.alias("limbMaterial", String.class);
		s.addImplicitCollection(BlenderSpec.class,"limbMaterials","limbMaterial",String.class);
	}

	public String toXml() {
		return BlenderSpec.toXml(this);
	}

	public static String toXml(BlenderSpec spec) {
		return s.toXML(spec);
	}

	public static BlenderSpec fromXml(String xml) {
		BlenderSpec bSpec = (BlenderSpec)s.fromXML(xml);
		return bSpec;
	}

	public void writeInfo2File(String fname) {
		String outStr = this.toXml();
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fname));
			out.write(outStr);
			out.flush();
			out.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public BlenderSpec() {}

	public BlenderSpec(BlenderSpec d) {
		this.stimulusID = d.getStimulusID();
		this.parentID = d.getParentID();
		this.morph = d.getMorphType();
		this.AldenSpec = d.getAldenSpec();
		this.EnvironmentSpec = d.getEnviroSpec();
		
		// general
		this.monkeyID = d.monkeyID;
		this.monkeyPerspectiveAngle = d.monkeyPerspectiveAngle;
		this.monkeyDistanceY = d.monkeyDistanceY;
		this.monkeyDistanceZ = d.monkeyDistanceZ;
		this.eyeSeparation = d.eyeSeparation;
		this.cameraLens_mm = d.cameraLens_mm;
		this.cameraSensorWidth_mm = d.cameraSensorWidth_mm;
		this.architectureScale = d.architectureScale;
		this.overallScale = d.overallScale;
	}

	public AldenSpec_class getAldenSpec(){
		return AldenSpec;
	}
	public EnviroSpec_class getEnviroSpec(){
		return EnvironmentSpec;
	}
	public String getStimulusID(){
		return stimulusID;
	}
	public String getParentID(){
		return parentID;
	}
	public String getMorphType(){
		return morph;
	}
	
	
	
}


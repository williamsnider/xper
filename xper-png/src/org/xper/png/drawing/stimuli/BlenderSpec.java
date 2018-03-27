package org.xper.png.drawing.stimuli;

import java.io.BufferedWriter;
import java.io.FileWriter;

import com.thoughtworks.xstream.XStream;

import org.xper.drawing.RGBColor;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;

////
////class LimbSeparation{
////	int howMany;
////	int limbIndex0;
////	int limbIndex1;
////	int limbIndex2;
////	int limbIndex3;
////	int limbIndex4;
////	int limbIndex5;
////	int limbIndex6;
////	int limbIndex7;
////	
////	public List<Integer> getAffectedLimbs() {
////		List<Integer> affectedLimbs = new ArrayList<Integer>();
////		affectedLimbs.add(x);
////
////		return affectedLimbs;
////	}	
////	public void setAffectedLimbs(List<Integer> affectedLimbs) {
////		this.howMany = affectedLimbs.size();
////		
////		for (n=0;n<this.howMany;n++) {
////			
////		}
////
////	}
////	public void setAffectedLimbs(LimbSeparation affectedLimbs) {
////		this.howMany = affectedLimbs.howMany;
////
////	}
////}
////
////class LimbMaterials{
////	String limbMaterial0;
////	String limbMaterial1;
////	String limbMaterial2;
////	String limbMaterial3;
////	String limbMaterial4;
////	String limbMaterial5;
////	String limbMaterial6;
////	String limbMaterial7;
////	
////	public List<String> getLimbMaterials() {
////		List<String> limbMaterials = new ArrayList<String>();
////		limbMaterials.add(x);
////
////		return limbMaterials;
////	}	
////	public void setLimbMaterials(List<String> limbMaterials) {
////		this.x = limbMaterials.get(0);
////		
////	}
////	public void setLimbMaterials(LimbMaterials limbMaterials) {
////		this.x = limbMaterials.x;
////
////	}
////}
//
//class AldenSpecy {
//	public boolean aldenPresent;
//	public String id;
//	public boolean blocky;
//	public MirrorSymmetry bilateralSymmetry;
//	public int fixationPoint;
//	public String whichWiggle;
//	public int location;
//	public double implantation;
//	public int scaleShiftInDepth;
//	public int wallInteraction;
//
//	public boolean densityUniform;
//	//	List<Integer> affectedLimbs = new ArrayList<Integer>(); //including howMany...
//	//	List<String> limbMaterials = new ArrayList<String>();
//
//	public String material;
//	public boolean optical;
//	public RGBColor opticalBeerLambertColor;
//	public double opticalIOR;
//	public double opticalTranslucency;
//	public double opticalAttenuation;
//	public double opticalTransparency;
//	public double opticalRoughness;
//	public double opticalReflectivity;
//
//	public boolean lowPotentialEnergy;
//	public Point3d makePrecarious;
//	public Point3d rotation;
//	public Point3d sun;
//
//	public AldenSpecy () {}
//
//	public AldenSpecy(AldenSpecy d) {
//		this.aldenPresent = d.getAldenPresent();
//		this.wallInteraction = d.getWallInteraction();
//		this.material = d.getAldenMaterial();
//
//		this.id = d.getAldenID();
//		this.blocky = d.getBlockiness();
//		this.bilateralSymmetry = d.getBilateralSymmetry();
//
//		this.fixationPoint = d.getFixationPoint();
//		this.whichWiggle = d.getWhichWiggle();
//		this.location = d.getLocInArchitecture();
//		this.implantation = d.getBurialDepth();
//		this.scaleShiftInDepth = d.getScaleShiftInDepth();
//
//		this.densityUniform = d.getIsUniformDensity();
//		//        this.affectedLimbs = d.getAffectedLimbs();
//		//        this.limbMaterials = d.getLimbMaterials();
//		//        
//		this.optical = d.getIsOptical();
//		this.opticalBeerLambertColor = d.getBeerLambertColor();
//		this.opticalIOR = d.getIOR();
//		this.opticalTranslucency = d.getTranslucency();
//		this.opticalAttenuation = d.getAttentuation();
//		this.opticalTransparency = d.getTransparency();
//		this.opticalRoughness = d.getRoughness();
//		this.opticalReflectivity = d.getReflectivity();
//
//		this.lowPotentialEnergy = d.getLowPotentialEnergy();
//		this.makePrecarious = d.getMakePrecarious();
//		this.rotation = d.getRotation();
//		this.sun = d.getSun();
//
//	}
//
//	public void setAldenPresent(boolean aldenPresent) {
//		this.aldenPresent = aldenPresent;
//	}
//	public boolean getAldenPresent(){
//		return aldenPresent;
//	}
//
//	public void setAldenID(String id){
//		this.id = id;
//	}
//	public String getAldenID(){
//		return id;
//	}
//
//	public void setBlockiness(boolean blocky){
//		this.blocky = blocky;
//	}
//	public boolean getBlockiness(){
//		return blocky;
//	}
//
//	public void setBilateralSymmetry(MirrorSymmetry bilateralSymmetry) {
//		this.bilateralSymmetry = bilateralSymmetry;
//	}
//	public MirrorSymmetry getBilateralSymmetry(){
//		return bilateralSymmetry;
//	}
//
//	public void setFixationPoint(int fixationPoint){
//		this.fixationPoint =  fixationPoint;
//	}
//	public int getFixationPoint(){
//		return fixationPoint;
//	}
//
//	public void setWhichWiggle(String whichWiggle){
//		this.whichWiggle =  whichWiggle;
//	}
//	public String getWhichWiggle(){
//		return whichWiggle;
//	}
//
//	public void setLocInArchitecture(int location){
//		this.location =  location;
//	}
//	public int getLocInArchitecture(){
//		return location;
//	}
//
//	public void setBurialDepth(int implantation){
//		this.implantation =  implantation;
//	}
//	public double getBurialDepth(){
//		return implantation;
//	}
//
//	public void setScaleShiftInDepth(int scaleShiftInDepth){
//		this.scaleShiftInDepth =  scaleShiftInDepth;
//	}
//	public int getScaleShiftInDepth(){
//		return scaleShiftInDepth;
//	}
//
//	public void setWallInteraction(int wallInteraction){
//		this.wallInteraction =  wallInteraction;
//	}
//	public int getWallInteraction(){
//		return wallInteraction;
//	}
//
//	public void setIsUniformDensity(boolean densityUniform){
//		this.densityUniform =  densityUniform;
//	}
//	public boolean getIsUniformDensity(){
//		return densityUniform;
//	}
//
//	//	public void setAffectedLimbs(List<Integer> affectedLimbs){
//	//		this.affectedLimbs.set =  affectedLimbs;
//	//	}
//	//	public List<Integer> getAffectedLimbs(){
//	//	return affectedLimbs;
//	//}
//
//	//	public void setLimbMaterials(List<String> limbMaterials){
//	//		this.limbMaterials =  limbMaterials;
//	//	}
//	//	public List<String> getLimbMaterials(){
//	//	return limbMaterials;
//	//}
//
//	public void setAldenMaterial(String material){
//		this.material =  material;
//	}
//	public String getAldenMaterial(){
//		return material;
//	}
//
//	public void setIsOptical(boolean optical){
//		this.optical =  optical;
//	}
//	public boolean getIsOptical(){
//		return optical;
//	}
//
//	public void setBeerLambertColor(RGBColor opticalBeerLambertColor){
//		this.opticalBeerLambertColor = opticalBeerLambertColor;
//	}
//	public RGBColor getBeerLambertColor(){
//		return opticalBeerLambertColor;
//	}
//
//	public void setIOR(double opticalIOR){
//		this.opticalIOR =  opticalIOR;
//	}
//	public double getIOR(){
//		return opticalIOR;
//	}
//
//	public void setTranslucency(double opticalTranslucency){
//		this.opticalTranslucency =  opticalTranslucency;
//	}
//	public double getTranslucency(){
//		return opticalTranslucency;
//	}
//
//	public void setAttentuation(double opticalAttenuation){
//		this.opticalAttenuation =  opticalAttenuation;
//	}
//	public double getAttentuation(){
//		return opticalAttenuation;
//	}
//
//	public void setTransparency(double opticalTransparency){
//		this.opticalTransparency =  opticalTransparency;
//	}
//	public double getTransparency(){
//		return opticalTransparency;
//	}
//
//	public void setRoughness(double opticalRoughness){
//		this.opticalRoughness =  opticalRoughness;
//	}
//	public double getRoughness(){
//		return opticalRoughness;
//	}
//
//	public void setReflectivity(double opticalReflectivity){
//		this.opticalReflectivity =  opticalReflectivity;
//	}
//	public double getReflectivity(){
//		return opticalReflectivity;
//	}
//
//	public void setLowPotentialEnergy(boolean lowPotentialEnergy){
//		this.lowPotentialEnergy =  lowPotentialEnergy;
//	}
//	public boolean getLowPotentialEnergy(){
//		return lowPotentialEnergy;
//	}
//
//	public void setMakePrecarious(Point3d makePrecarious){
//		this.makePrecarious = makePrecarious;
//	}
//	public Point3d getMakePrecarious(){
//		return makePrecarious;
//	}
//
//	public void setRotation(Point3d rotation){
//		this.rotation = rotation;
//	}
//	public Point3d getRotation(){
//		return rotation;
//	}
//
//	public void setSun(Point3d sun){
//		this.sun = sun;
//	}
//	public Point3d getSun(){
//		return sun;
//	}
//
//}
//
//class EnviroSpecy {
//	public double horizonTilt;
//	public double horizonSlant;
//	public String horizonMaterial;
//	public boolean gravity;
//	public String context;
//	public boolean architecture;
//	public boolean floor;
//	public boolean ceiling;
//	public boolean wallL;
//	public boolean wallR;
//	public boolean wallB;
//	public double architectureThickness;
//	public double distance;
//	public String structureMaterial;
//	public boolean aperture;
//	
//	public EnviroSpecy() {}
//	
//	public EnviroSpecy(EnviroSpecy d) {
//		this.horizonTilt = d.getHorizonTilt();
//		this.horizonSlant = d.getHorizonSlant();
//		this.horizonMaterial = d.getHorizonMaterial();
//		
//		this.gravity = d.getGravity();
//		this.context = d.getContext();
//		
//		this.architecture = d.getHasArchitecture();
//		this.floor = d.getHasFloor();
//		this.ceiling = d.getHasCeiling();
//		this.wallL = d.getHasWallL();
//		this.wallR = d.getHasWallR();
//		this.wallB = d.getHasWallB();
//        this.architectureThickness = d.getArchitectureThickness();
//
//        this.distance = d.getDistance();
//        this.structureMaterial = d.getStructureMaterial();
//        this.aperture = d.getAperture();
//	}	
//	
//	public void setHorizonTilt(double horizonTilt){
//		this.horizonTilt =  horizonTilt;
//	}
//	public double getHorizonTilt() {
//		return horizonTilt;
//	}
//	
//	public void setHorizonSlant(double horizonSlant){
//		this.horizonSlant =  horizonSlant;
//	}
//	public double getHorizonSlant() {
//		return horizonSlant;
//	}
//	
//	public void setHorizonMaterial(String horizonMaterial){
//		this.horizonMaterial =  horizonMaterial;
//	}
//	public String getHorizonMaterial() {
//		return horizonMaterial;
//	}
//	
//	public void setGravity(boolean gravity){
//		this.gravity =  gravity;
//	}
//	public boolean getGravity() {
//		return gravity;
//	}
//	
//	public void setContext(String context){
//		this.context =  context;
//	}
//	public String getContext() {
//		return context;
//	}
//	
//	public void setHasArchitecture(boolean architecture){
//		this.architecture =  architecture;
//	}
//	public boolean getHasArchitecture() {
//		return architecture;
//	}
//	
//	public void setArchitectureThickness(double architectureThickness){
//		this.architectureThickness =  architectureThickness;
//	}
//	public double getArchitectureThickness() {
//		return architectureThickness;
//	}
//	
//	public List<Boolean> getArchitectureInfo(){
//		List<Boolean> archiInfo = new ArrayList<Boolean>();
//		archiInfo.add(architecture);
//		archiInfo.add(floor);
//		archiInfo.add(ceiling);
//		archiInfo.add(wallL);
//		archiInfo.add(wallR);
//		archiInfo.add(wallB);
//		return archiInfo;
//	}
//	
//	public void setHasFloor(boolean floor){
//		this.floor =  floor;
//	}
//	public boolean getHasFloor() {
//		return floor;
//	}
//	
//	public void setHasCeiling(boolean ceiling){
//		this.ceiling =  ceiling;
//	}
//	public boolean getHasCeiling() {
//		return ceiling;
//	}
//	
//	public void setHasWallL(boolean wallL){
//		this.wallL =  wallL;
//	}
//	public boolean getHasWallL() {
//		return wallL;
//	}
//	
//	public void setHasWallR(boolean wallR){
//		this.wallR =  wallR;
//	}
//	public boolean getHasWallR() {
//		return wallR;
//	}
//	
//	public void setHasWallB(boolean wallB){
//		this.wallB =  wallB;
//	}
//	public boolean getHasWallB() {
//		return wallB;
//	}
//	
//	public void setDistance(double distance){
//		this.distance =  distance;
//	}
//	public double getDistance() {
//		return distance;
//	}
//	
//	public void setStructureMaterial(String structureMaterial){
//		this.structureMaterial =  structureMaterial;
//	}
//	public String getStructureMaterial() {
//		return structureMaterial;
//	}
//	
//	public void setAperture(boolean aperture){
//		this.aperture =  aperture;
//	}
//	public boolean getAperture() {
//		return aperture;
//	}
//	
//}

public class BlenderSpec {
	public AldenSpec aldenSpec;// = new AldenSpec(); // = new AldenSpec();
	public EnviroSpec enviroSpec;// = new EnviroSpec(); // = new EnviroSpec();
	
	public String stimulusID;
	public String parentID;
	public String morph;

	// general
	public String monkeyID;
	public double monkeyPerspectiveAngle;
	public double monkeyDistanceY;
	public double monkeyDistanceZ;
	public double eyeSeparation;
	public int cameraLens_mm;
	public double cameraSensorWidth_mm;
	public double architectureScale;
	public int overallScale;
	
	transient static XStream s;

	static {
		s = new XStream();
		s.alias("BlenderSpec", BlenderSpec.class);
		s.alias("AldenSpec", AldenSpec.class);
		s.alias("EnvironmentSpec", EnviroSpec.class);
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
//		this.aldenSpec = d.getAldenSpec();
//		this.enviroSpec = d.getEnviroSpec();
		
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

//	public AldenSpec getAldenSpec(){
//		return aldenSpec;
//	}
//	public EnviroSpec getEnviroSpec(){
//		return enviroSpec;
//	}
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


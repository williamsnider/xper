package org.xper.robot.drawing.stimuli;

import org.xper.drawing.RGBColor;

import javax.vecmath.Point3d;
import java.util.List;


public class AldenSpec_class {
	int aldenPresent;
	String id;
	int blocky;
	Point3d bilateralSymmetry;
	int fixationPoint;
	String whichWiggle;
	int location;
	double implantation;
	double scaleShiftInDepth;
	int wallInteraction;

	int densityUniform;
	List<Integer> affectedLimbs;
	List<String> limbMaterials;
	int howMany;
	int massManipulationLimb;

	String material;
	int optical;
	RGBColor opticalBeerLambertColor;
	double opticalIOR;
	double opticalTranslucency;
	double opticalAttenuation;
	double opticalTransparency;
	double opticalRoughness;
	double opticalReflectivity;

	int lowPotentialEnergy;
	Point3d makePrecarious;
	double makePrecariousFinal;
	Point3d rotation;
	Point3d sun;
	
	double boundingBoxLongAxis;
	Point3d comVector;
	double mass;
	Point3d horizonNormal;
	
	
	public AldenSpec_class () {}

	public AldenSpec_class(AldenSpec_class d) {
		this.aldenPresent = d.getAldenPresent();
		this.wallInteraction = d.getWallInteraction();
		this.material = d.getAldenMaterial();
		
		this.id = d.getAldenID();
		this.blocky = d.getBlockiness();
		this.bilateralSymmetry = d.getBilateralSymmetry();
		
		this.fixationPoint = d.getFixationPoint();
		this.whichWiggle = d.getWhichWiggle();
		this.location = d.getLocInArchitecture();
		this.implantation = d.getBurialDepth();
		this.scaleShiftInDepth = d.getScaleShiftInDepth();

        this.densityUniform = d.getIsUniformDensity();
        this.affectedLimbs = d.getAffectedLimbs();
        this.limbMaterials = d.getLimbMaterials(); 
        this.howMany = d.getNumLimbs();
        
        this.optical = d.getIsOptical();
        this.opticalBeerLambertColor = d.getBeerLambertColor();
        this.opticalIOR = d.getIOR();
        this.opticalTranslucency = d.getTranslucency();
        this.opticalAttenuation = d.getAttentuation();
        this.opticalTransparency = d.getTransparency();
        this.opticalRoughness = d.getRoughness();
        this.opticalReflectivity = d.getReflectivity();

        this.lowPotentialEnergy = d.getLowPotentialEnergy();
        this.makePrecarious = d.getMakePrecarious();
        this.makePrecariousFinal = d.getMakePrecariousFinal();
        this.rotation = d.getRotation();
        this.sun = d.getSun();
        
        this.boundingBoxLongAxis = d.getBoundingBoxLongAxis();
    	this.comVector = d.getComVector();
    	this.mass = d.getMass();
    	this.horizonNormal = d.getHorizonNormal();
	}
	
	public void setAldenPresent(int aldenPresent) {
		this.aldenPresent = aldenPresent;
	}
	public int getAldenPresent(){
		return aldenPresent;
	}
	
	public void setAldenID(String id){
		this.id = id;
	}
	public String getAldenID(){
		return id;
	}
	
	public void setBlockiness(int blocky){
		this.blocky = blocky;
	}
	public int getBlockiness(){
		return blocky;
	}
	
	public void setBilateralSymmetry(Point3d bilateralSymmetry) {
		this.bilateralSymmetry = bilateralSymmetry;
	}
	public Point3d getBilateralSymmetry(){
		return bilateralSymmetry;
	}
	
	public void setFixationPoint(int fixationPoint){
		this.fixationPoint =  fixationPoint;
	}
	public int getFixationPoint(){
		return fixationPoint;
	}
	
	public void setWhichWiggle(String whichWiggle){
		this.whichWiggle =  whichWiggle;
	}
	public String getWhichWiggle(){
		return whichWiggle;
	}
	
	public void setLocInArchitecture(int location){
		this.location =  location;
	}
	public int getLocInArchitecture(){
		return location;
	}
	
	public void setBurialDepth(int implantation){
		this.implantation =  implantation;
	}
	public double getBurialDepth(){
		return implantation;
	}
	
	public void setScaleShiftInDepth(int scaleShiftInDepth){
		this.scaleShiftInDepth =  scaleShiftInDepth;
	}
	public double getScaleShiftInDepth(){
		return scaleShiftInDepth;
	}
	
	public void setWallInteraction(int wallInteraction){
		this.wallInteraction =  wallInteraction;
	}
	public int getWallInteraction(){
		return wallInteraction;
	}
	
	public void setIsUniformDensity(int densityUniform){
		this.densityUniform =  densityUniform;
	}
	public int getIsUniformDensity(){
		return densityUniform;
	}

	public void setAffectedLimbs(List<Integer> affectedLimbs){
		this.affectedLimbs =  affectedLimbs;
	}
	public List<Integer> getAffectedLimbs(){
		return affectedLimbs;
	}
	
	public void setLimbMaterials(List<String> limbMaterials){
		this.limbMaterials =  limbMaterials;
	}
	public List<String> getLimbMaterials(){
		return limbMaterials;
	}
	
	public int getNumLimbs(){
		return howMany;
	}
	
	public void setMassManipulationLimb(int massManipulationLimb){
		this.massManipulationLimb = massManipulationLimb;
	}
	public int getMassManipulationLimb(){
		return massManipulationLimb;
	}

	public void setAldenMaterial(String material){
		this.material =  material;
	}
	public String getAldenMaterial(){
		return material;
	}

	public void setIsOptical(int optical){
		this.optical =  optical;
	}
	public int getIsOptical(){
		return optical;
	}
	
	public void setBeerLambertColor(RGBColor opticalBeerLambertColor){
		this.opticalBeerLambertColor = opticalBeerLambertColor;
	}
	public RGBColor getBeerLambertColor(){
		return opticalBeerLambertColor;
	}
	
	public void setIOR(double opticalIOR){
		this.opticalIOR =  opticalIOR;
	}
	public double getIOR(){
		return opticalIOR;
	}
	
	public void setTranslucency(double opticalTranslucency){
		this.opticalTranslucency =  opticalTranslucency;
	}
	public double getTranslucency(){
		return opticalTranslucency;
	}
	
	public void setAttentuation(double opticalAttenuation){
		this.opticalAttenuation =  opticalAttenuation;
	}
	public double getAttentuation(){
		return opticalAttenuation;
	}
	
	public void setTransparency(double opticalTransparency){
		this.opticalTransparency =  opticalTransparency;
	}
	public double getTransparency(){
		return opticalTransparency;
	}
	
	public void setRoughness(double opticalRoughness){
		this.opticalRoughness =  opticalRoughness;
	}
	public double getRoughness(){
		return opticalRoughness;
	}
	
	public void setReflectivity(double opticalReflectivity){
		this.opticalReflectivity =  opticalReflectivity;
	}
	public double getReflectivity(){
		return opticalReflectivity;
	}
	
	public void setLowPotentialEnergy(int lowPotentialEnergy){
		this.lowPotentialEnergy =  lowPotentialEnergy;
	}
	public int getLowPotentialEnergy(){
		return lowPotentialEnergy;
	}
	
	public void setMakePrecarious(Point3d makePrecarious){
		this.makePrecarious = makePrecarious;
	}
	public Point3d getMakePrecarious(){
		return makePrecarious;
	}
	
	public void setMakePrecariousFinal(double makePrecariousFinal){
		this.makePrecariousFinal = makePrecariousFinal;
	}
	public double getMakePrecariousFinal(){
		return makePrecariousFinal;
	}
	
	public void setRotation(Point3d rotation){
		this.rotation = rotation;
	}
	public Point3d getRotation(){
		return rotation;
	}
	
	public void setSun(Point3d sun){
		this.sun = sun;
	}
	public Point3d getSun(){
		return sun;
	}
	
	public void setBoundingBoxLongAxis(double boundingBoxLongAxis){
		this.boundingBoxLongAxis = boundingBoxLongAxis;
	}
	public double getBoundingBoxLongAxis(){
		return boundingBoxLongAxis;
	}
	public void setComVector(Point3d comVector) {
		this.comVector = comVector;
	}
	public Point3d getComVector(){
		return comVector;
	}
	public double getMass() {
		return mass;
	}
	public void setMass(double mass) {
		this.mass = mass;
	}
	public void setHorizonNormal(Point3d horizonNormal) {
		this.horizonNormal = horizonNormal;
	}
	public Point3d getHorizonNormal(){
		return horizonNormal;
	}
	
}
package org.xper.png.drawing.stimuli;

import org.xper.drawing.RGBColor;

import javax.vecmath.Point3d;
import java.util.List;


public class AldenSpec_class {
	boolean aldenPresent;
	String id;
	boolean blocky;
	Point3d bilateralSymmetry;
	int fixationPoint;
	String whichWiggle;
	int location;
	double implantation;
	int scaleShiftInDepth;
	int wallInteraction;

	boolean densityUniform;
	List<Integer> affectedLimbs;
	List<String> limbMaterials;
	int howMany;

	String material;
	boolean optical;
	RGBColor opticalBeerLambertColor;
	double opticalIOR;
	double opticalTranslucency;
	double opticalAttenuation;
	double opticalTransparency;
	double opticalRoughness;
	double opticalReflectivity;

	boolean lowPotentialEnergy;
	Point3d makePrecarious;
	Point3d rotation;
	Point3d sun;

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
        this.rotation = d.getRotation();
        this.sun = d.getSun();
	}
	
	public void setAldenPresent(boolean aldenPresent) {
		this.aldenPresent = aldenPresent;
	}
	public boolean getAldenPresent(){
		return aldenPresent;
	}
	
	public void setAldenID(String id){
		this.id = id;
	}
	public String getAldenID(){
		return id;
	}
	
	public void setBlockiness(boolean blocky){
		this.blocky = blocky;
	}
	public boolean getBlockiness(){
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
	public int getScaleShiftInDepth(){
		return scaleShiftInDepth;
	}
	
	public void setWallInteraction(int wallInteraction){
		this.wallInteraction =  wallInteraction;
	}
	public int getWallInteraction(){
		return wallInteraction;
	}
	
	public void setIsUniformDensity(boolean densityUniform){
		this.densityUniform =  densityUniform;
	}
	public boolean getIsUniformDensity(){
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

	public void setAldenMaterial(String material){
		this.material =  material;
	}
	public String getAldenMaterial(){
		return material;
	}

	public void setIsOptical(boolean optical){
		this.optical =  optical;
	}
	public boolean getIsOptical(){
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
	
	public void setLowPotentialEnergy(boolean lowPotentialEnergy){
		this.lowPotentialEnergy =  lowPotentialEnergy;
	}
	public boolean getLowPotentialEnergy(){
		return lowPotentialEnergy;
	}
	
	public void setMakePrecarious(Point3d makePrecarious){
		this.makePrecarious = makePrecarious;
	}
	public Point3d getMakePrecarious(){
		return makePrecarious;
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
}
package org.xper.png.drawing.stimuli;

import org.xper.drawing.RGBColor;

import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

//
//class LimbSeparation{
//	int howMany;
//	int limbIndex0;
//	int limbIndex1;
//	int limbIndex2;
//	int limbIndex3;
//	int limbIndex4;
//	int limbIndex5;
//	int limbIndex6;
//	int limbIndex7;
//	
//	public List<Integer> getAffectedLimbs() {
//		List<Integer> affectedLimbs = new ArrayList<Integer>();
//		affectedLimbs.add(x);
//
//		return affectedLimbs;
//	}	
//	public void setAffectedLimbs(List<Integer> affectedLimbs) {
//		this.howMany = affectedLimbs.size();
//		
//		for (n=0;n<this.howMany;n++) {
//			
//		}
//
//	}
//	public void setAffectedLimbs(LimbSeparation affectedLimbs) {
//		this.howMany = affectedLimbs.howMany;
//
//	}
//}
//
//class LimbMaterials{
//	String limbMaterial0;
//	String limbMaterial1;
//	String limbMaterial2;
//	String limbMaterial3;
//	String limbMaterial4;
//	String limbMaterial5;
//	String limbMaterial6;
//	String limbMaterial7;
//	
//	public List<String> getLimbMaterials() {
//		List<String> limbMaterials = new ArrayList<String>();
//		limbMaterials.add(x);
//
//		return limbMaterials;
//	}	
//	public void setLimbMaterials(List<String> limbMaterials) {
//		this.x = limbMaterials.get(0);
//		
//	}
//	public void setLimbMaterials(LimbMaterials limbMaterials) {
//		this.x = limbMaterials.x;
//
//	}
//}

public class AldenSpec {
	public boolean aldenPresent;
	public String id;
	public boolean blocky;
	public MirrorSymmetry bilateralSymmetry;
	public int fixationPoint;
	public String whichWiggle;
	public int location;
	public double implantation;
	public int scaleShiftInDepth;
	public int wallInteraction;

	public boolean densityUniform;
//	List<Integer> affectedLimbs = new ArrayList<Integer>(); //including howMany...
//	List<String> limbMaterials = new ArrayList<String>();

	public String material;
	public boolean optical;
	public RGBColor opticalBeerLambertColor;
	public double opticalIOR;
	public double opticalTranslucency;
	public double opticalAttenuation;
	public double opticalTransparency;
	public double opticalRoughness;
	public double opticalReflectivity;

	public boolean lowPotentialEnergy;
	public Point3d makePrecarious;
	public Point3d rotation;
	public Point3d sun;
	
//	transient static XStream s;
//
//	static {
//		s = new XStream();
//		s.alias("AldenSpec", AldenSpec.class);
//	}
//	
	public AldenSpec () {}
	
	public AldenSpec(AldenSpec d) {
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
//        this.affectedLimbs = d.getAffectedLimbs();
//        this.limbMaterials = d.getLimbMaterials();
//        
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
	
	public void setBilateralSymmetry(MirrorSymmetry bilateralSymmetry) {
		this.bilateralSymmetry = bilateralSymmetry;
	}
	public MirrorSymmetry getBilateralSymmetry(){
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
	
//	public void setAffectedLimbs(List<Integer> affectedLimbs){
//		this.affectedLimbs.set =  affectedLimbs;
//	}
//	public List<Integer> getAffectedLimbs(){
//	return affectedLimbs;
//}
	
//	public void setLimbMaterials(List<String> limbMaterials){
//		this.limbMaterials =  limbMaterials;
//	}
//	public List<String> getLimbMaterials(){
//	return limbMaterials;
//}
	
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
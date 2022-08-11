package org.xper.robot.drawing.stimuli;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;

public class EnviroSpec_class {
	double horizonTilt;
	double horizonSlant;
	String horizonMaterial;
	int gravity;
	String context;
	int compositeKeepAlden;
	int architecture;
	int floor;
	int ceiling;
	int wallL;
	int wallR;
	int wallB;
	double architectureThickness;
	double distance;
	double fixationPointDepth;
	String structureMaterial;
	int aperture;
	Point3d cameraLocation;
	
	
	public EnviroSpec_class() {}
	
	public EnviroSpec_class(EnviroSpec_class d) {
		this.horizonTilt = d.getHorizonTilt();
		this.horizonSlant = d.getHorizonSlant();
		this.horizonMaterial = d.getHorizonMaterial();
		
		this.gravity = d.getGravity();
		this.context = d.getContext();
		this.compositeKeepAlden = d.getCompositeKeepAlden();
		
		this.architecture = d.getHasArchitecture();
		this.floor = d.getHasFloor();
		this.ceiling = d.getHasCeiling();
		this.wallL = d.getHasWallL();
		this.wallR = d.getHasWallR();
		this.wallB = d.getHasWallB();
        this.architectureThickness = d.getArchitectureThickness();

        this.distance = d.getDistance();
        this.fixationPointDepth = d.getFixationPointDepth();
        this.structureMaterial = d.getStructureMaterial();
        this.aperture = d.getAperture();
        
        this.cameraLocation = d.getCameraLocation();
        
	}	
	
	public void setHorizonTilt(double horizonTilt){
		this.horizonTilt =  horizonTilt;
	}
	public double getHorizonTilt() {
		return horizonTilt;
	}
	
	public void setHorizonSlant(double horizonSlant){
		this.horizonSlant =  horizonSlant;
	}
	public double getHorizonSlant() {
		return horizonSlant;
	}
	
	public void setHorizonMaterial(String horizonMaterial){
		this.horizonMaterial =  horizonMaterial;
	}
	public String getHorizonMaterial() {
		return horizonMaterial;
	}
	
	public void setGravity(int gravity){
		this.gravity =  gravity;
	}
	public int getGravity() {
		return gravity;
	}
	
	public void setContext(String context){
		this.context =  context;
	}
	public String getContext() {
		return context;
	}
	
	public void setCompositeKeepAlden(int compositeKeepAlden){
		this.compositeKeepAlden =  compositeKeepAlden;
	}
	public int getCompositeKeepAlden() {
		return compositeKeepAlden;
	}
	
	public void setHasArchitecture(int architecture){
		this.architecture =  architecture;
	}
	public int getHasArchitecture() {
		return architecture;
	}
	
	public void setArchitectureThickness(double architectureThickness){
		this.architectureThickness =  architectureThickness;
	}
	public double getArchitectureThickness() {
		return architectureThickness;
	}
	
	public List<Integer> getArchitectureInfo(){
		List<Integer> archiInfo = new ArrayList<Integer>();
		archiInfo.add(architecture);
		archiInfo.add(floor);
		archiInfo.add(ceiling);
		archiInfo.add(wallL);
		archiInfo.add(wallR);
		archiInfo.add(wallB);
		return archiInfo;
	}
	
	public void setHasFloor(int floor){
		this.floor =  floor;
	}
	public int getHasFloor() {
		return floor;
	}
	
	public void setHasCeiling(int ceiling){
		this.ceiling =  ceiling;
	}
	public int getHasCeiling() {
		return ceiling;
	}
	
	public void setHasWallL(int wallL){
		this.wallL =  wallL;
	}
	public int getHasWallL() {
		return wallL;
	}
	
	public void setHasWallR(int wallR){
		this.wallR =  wallR;
	}
	public int getHasWallR() {
		return wallR;
	}
	
	public void setHasWallB(int wallB){
		this.wallB =  wallB;
	}
	public int getHasWallB() {
		return wallB;
	}
	
	public void setDistance(double distance){
		this.distance =  distance;
	}
	public double getDistance() {
		return distance;
	}
	
	public void setFixationPointDepth(double fixationPointDepth){
		this.fixationPointDepth =  fixationPointDepth;
	}
	public double getFixationPointDepth() {
		return fixationPointDepth;
	}
	
	public void setStructureMaterial(String structureMaterial){
		this.structureMaterial =  structureMaterial;
	}
	public String getStructureMaterial() {
		return structureMaterial;
	}
	
	public void setAperture(int aperture){
		this.aperture =  aperture;
	}
	public int getAperture() {
		return aperture;
	}
	public void setCameraLocation(Point3d cameraLocation) {
		this.cameraLocation = cameraLocation;
	}
	public Point3d getCameraLocation(){
		return cameraLocation;
	}

}
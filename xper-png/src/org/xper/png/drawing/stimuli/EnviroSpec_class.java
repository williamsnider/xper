package org.xper.png.drawing.stimuli;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

public class EnviroSpec_class {
	double horizonTilt;
	double horizonSlant;
	String horizonMaterial;
	boolean gravity;
	String context;
	boolean architecture;
	boolean floor;
	boolean ceiling;
	boolean wallL;
	boolean wallR;
	boolean wallB;
	double architectureThickness;
	double distance;
	String structureMaterial;
	boolean aperture;
	
	
	public EnviroSpec_class() {}
	
	public EnviroSpec_class(EnviroSpec_class d) {
		this.horizonTilt = d.getHorizonTilt();
		this.horizonSlant = d.getHorizonSlant();
		this.horizonMaterial = d.getHorizonMaterial();
		
		this.gravity = d.getGravity();
		this.context = d.getContext();
		
		this.architecture = d.getHasArchitecture();
		this.floor = d.getHasFloor();
		this.ceiling = d.getHasCeiling();
		this.wallL = d.getHasWallL();
		this.wallR = d.getHasWallR();
		this.wallB = d.getHasWallB();
        this.architectureThickness = d.getArchitectureThickness();

        this.distance = d.getDistance();
        this.structureMaterial = d.getStructureMaterial();
        this.aperture = d.getAperture();
        
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
	
	public void setGravity(boolean gravity){
		this.gravity =  gravity;
	}
	public boolean getGravity() {
		return gravity;
	}
	
	public void setContext(String context){
		this.context =  context;
	}
	public String getContext() {
		return context;
	}
	
	public void setHasArchitecture(boolean architecture){
		this.architecture =  architecture;
	}
	public boolean getHasArchitecture() {
		return architecture;
	}
	
	public void setArchitectureThickness(double architectureThickness){
		this.architectureThickness =  architectureThickness;
	}
	public double getArchitectureThickness() {
		return architectureThickness;
	}
	
	public List<Boolean> getArchitectureInfo(){
		List<Boolean> archiInfo = new ArrayList<Boolean>();
		archiInfo.add(architecture);
		archiInfo.add(floor);
		archiInfo.add(ceiling);
		archiInfo.add(wallL);
		archiInfo.add(wallR);
		archiInfo.add(wallB);
		return archiInfo;
	}
	
	public void setHasFloor(boolean floor){
		this.floor =  floor;
	}
	public boolean getHasFloor() {
		return floor;
	}
	
	public void setHasCeiling(boolean ceiling){
		this.ceiling =  ceiling;
	}
	public boolean getHasCeiling() {
		return ceiling;
	}
	
	public void setHasWallL(boolean wallL){
		this.wallL =  wallL;
	}
	public boolean getHasWallL() {
		return wallL;
	}
	
	public void setHasWallR(boolean wallR){
		this.wallR =  wallR;
	}
	public boolean getHasWallR() {
		return wallR;
	}
	
	public void setHasWallB(boolean wallB){
		this.wallB =  wallB;
	}
	public boolean getHasWallB() {
		return wallB;
	}
	
	public void setDistance(double distance){
		this.distance =  distance;
	}
	public double getDistance() {
		return distance;
	}
	
	public void setStructureMaterial(String structureMaterial){
		this.structureMaterial =  structureMaterial;
	}
	public String getStructureMaterial() {
		return structureMaterial;
	}
	
	public void setAperture(boolean aperture){
		this.aperture =  aperture;
	}
	public boolean getAperture() {
		return aperture;
	}

}
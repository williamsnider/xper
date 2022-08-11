package org.xper.robot.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xper.robot.expt.generate.PngGAParams;

public class BlenderRunnable implements Runnable {
	List<String> args = new ArrayList<String>();
	boolean doWaitFor = true;
	String scriptPath;
	List<Long> stimObjIds = new ArrayList<Long>();
	List<String> environAttrs = new ArrayList<String>();
	List<Integer> possiblePositions = new ArrayList<Integer>();
	List<String> constantAttributes = new ArrayList<String>();

	public BlenderRunnable(String scriptPath) {
		this.scriptPath = scriptPath;
	}

	public BlenderRunnable(String scriptPath, List<Long> stimObjIds) {
		this.scriptPath = scriptPath;
		this.stimObjIds = stimObjIds;
	}
	
	public BlenderRunnable(String scriptPath, List<String> environAttrs, List<Integer> possiblePositions) {
		this.scriptPath = scriptPath;
		this.environAttrs = environAttrs;
		this.possiblePositions = possiblePositions;
	}
	
	public BlenderRunnable(String scriptPath, List<String> constantAttributes, String lineage) {
		this.scriptPath = scriptPath;
		this.constantAttributes = constantAttributes;
	}
	
	public BlenderRunnable() {}
	
	@Override
	public void run() {
		args.add(PngGAParams.appPath);
		args.add(PngGAParams.blendFile);
		args.add("--background");
		args.add("--python");
		args.add(scriptPath);
		args.add("--");
		
		long stimObjId;
		
		for (int n=0;n<stimObjIds.size();n++) {
			stimObjId = stimObjIds.get(n);
			args.add(Long.toString(stimObjId));
		}
		
		String environAttr;
		
		for (int n=0;n<environAttrs.size();n++) {
			environAttr = environAttrs.get(n);
			args.add(environAttr);
		}
		
		int possiblePosition;
		
		for (int n=0;n<possiblePositions.size();n++) {
			possiblePosition = possiblePositions.get(n);
			args.add(Integer.toString(possiblePosition));
		}
		
		String constantAttribute;
		
		for (int n=0;n<constantAttributes.size();n++) {
			constantAttribute = constantAttributes.get(n);
			args.add(constantAttribute);
		}
		
		
		try {
			// JK 91823
			System.out.println(args);
			System.out.println("Editing blender specifications.");
			ProcessBuilder builder = new ProcessBuilder(args);
			Process process = builder.start();
//			process.getOutputStream();
			if (doWaitFor)
				process.waitFor();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void run(List<String> args) {
		try {
//			System.out.println(args);
			System.out.println("Sending stimuli to cluster to render.");
			ProcessBuilder builder = new ProcessBuilder(args);
			Process process = builder.start();
//			process.getOutputStream();
			if (doWaitFor)
				process.waitFor();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void setScriptPath(String scriptPath) {
		this.scriptPath = scriptPath;
	}
	
	public void setDoWaitFor(boolean doWaitFor) {
		this.doWaitFor = doWaitFor;
	}

	public void setArgs(List<String> args) {
		this.args = args;
	}
	
}

package org.xper.png.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.xper.png.drawing.stimuli.PngObjectSpec;

public class BlenderRunnable implements Runnable {
	List<String> args = new ArrayList<String>();
	boolean doWaitFor = false;
	String scriptPath;
	List<Long> stimObjIds = new ArrayList<Long>();
	
	static String appPath = "/Applications/blender-279/Blender.app/Contents/MacOS/blender";
//	static String appPath = "/Applications/blender279/Blender.app/Contents/MacOS/blender";
	static String blendFile = "/Users/ecpc31/Dropbox/Blender/ProgressionClasses/frameRate.blend";
//	static String blendFile = "/Users/alexandriya/Dropbox/Blender/ProgressionClasses/frameRate.blend";

	public BlenderRunnable(String scriptPath) {
		this.scriptPath = scriptPath;
	}

	public BlenderRunnable(String scriptPath, List<Long> stimObjIds) {
		this.scriptPath = scriptPath;
		this.stimObjIds = stimObjIds;
	}
	
	@Override
	public void run() {
		args.add(appPath);
		args.add(blendFile);
		args.add("--background");
		args.add("--python");
		args.add(scriptPath);
		args.add("--");
		
		long stimObjId;
		
		for (int n=0;n<stimObjIds.size();n++) {
			stimObjId = stimObjIds.get(n);
			args.add(Long.toString(stimObjId));
		}
		
		try {
			ProcessBuilder builder = new ProcessBuilder(args);
			Process process = builder.start();
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

}

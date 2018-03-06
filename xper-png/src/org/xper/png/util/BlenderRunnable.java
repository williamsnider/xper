package org.xper.png.util;

import java.io.IOException;
import java.util.List;

public class BlenderRunnable implements Runnable {
	List<String> args;
	boolean doWaitFor = false;
	String scriptPath;
	
	static String appPath = "/Applications/Blender.app/Contents/MacOS/blender";
	
	@Override
	public void run() {
		args.add(appPath);
		args.add("--background");
		args.add("--python");
		args.add(scriptPath);
		
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

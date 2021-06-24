package org.xper.png.expt.generate;

import org.xper.Dependency;
import org.xper.png.util.PngDbUtil;
import org.xper.exception.VariableNotFoundException;
import org.xper.time.TimeUtil;

public class SimpleRandomGeneration {
	private static final int NumImages = 8;
	@Dependency
	PngDbUtil dbUtil;
	//@Dependency
	//TimeUtil globalTimeUtil;
	@Dependency
	int taskCount;

	public int getTaskCount() {
		return taskCount;
	}

	public void setTaskCount(int taskCount) {
		this.taskCount = taskCount;
	}
	
	public void generate() {
		ImageSpec im1 = new ImageSpec();
		
		String basepath = "/home/justin/choiceImages/img";
		String ext = ".png";
		
		System.out.print("JK 833862 SimpleRandomGeneration generate() ");
		long genId = 1;
		try {
			genId = dbUtil.readReadyGenerationInfo().getGenId() + 1;
		} catch (VariableNotFoundException e) {
			dbUtil.writeReadyGenerationInfo(genId, 0);
		}
		for (int i = 0; i < taskCount; i++) {
			if (i % 10 == 0) {
				System.out.print(".");
			}
			im1.setFilename(basepath + Integer.toString((int)(Math.round(Math.random() * NumImages))) + ext);
		
			System.out.println(im1.toXml());
			
			long taskId = System.currentTimeMillis() * 1000L;
     		dbUtil.writeStimSpec(taskId, im1.toXml());
			dbUtil.writeTaskToDo(taskId, taskId, -1, genId);
		}
		dbUtil.updateReadyGenerationInfo(genId, taskCount);
		System.out.println("done.");
	}

	
	public PngDbUtil getDbUtil() {
		return dbUtil;
	}

	public void setDbUtil(PngDbUtil dbUtil) {
		this.dbUtil = dbUtil;
	}

//	public TimeUtil getGlobalTimeUtil() {
//		return globalTimeUtil;
//	}
//
//	public void setGlobalTimeUtil(TimeUtil globalTimeUtil) {
//		this.globalTimeUtil = globalTimeUtil;
//	}

}

package org.xper.robot.app.ga;

import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.xper.robot.expt.generate.RobotRandomGeneration;
import org.xper.util.FileUtil;

public class RandGen {
	public static void main(String[] args) {
		JavaConfigApplicationContext context = new JavaConfigApplicationContext(
				FileUtil.loadConfigClass("experiment.config_class"));
		RobotRandomGeneration gen = context.getBean(RobotRandomGeneration.class);
		gen.setTaskCount(100);
		gen.generate(); 
	}
}

package org.xper.png.app.ga;

import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.xper.experiment.ExperimentRunner;
import org.xper.util.FileUtil;

public class Experiment {
	public static void main(String[] args) {
		
		JavaConfigApplicationContext context = new JavaConfigApplicationContext(
				FileUtil.loadConfigClass("experiment.config_class"));
		ExperimentRunner runner = context.getBean(ExperimentRunner.class);
		runner.run();
	}
}
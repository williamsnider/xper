package org.xper.png.app.ga;

import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.xper.png.expt.generate.PngRandomGeneration;
import org.xper.util.FileUtil;

public class RandGen {
	public static void main(String[] args) {
		JavaConfigApplicationContext context = new JavaConfigApplicationContext(
				FileUtil.loadConfigClass("experiment.config_class"));

		PngRandomGeneration gen = context.getBean(PngRandomGeneration.class);
		gen.generateGA(); 
	}
}

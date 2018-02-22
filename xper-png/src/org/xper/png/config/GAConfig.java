package org.xper.png.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.Import;
import org.springframework.config.java.annotation.Lazy;
import org.springframework.config.java.annotation.valuesource.SystemPropertiesValueSource;
import org.springframework.config.java.plugin.context.AnnotationDrivenConfig;
import org.xper.classic.SlideTrialExperiment;
import org.xper.classic.vo.SlideTrialExperimentState;
import org.xper.config.AcqConfig;
import org.xper.config.BaseConfig;
import org.xper.config.ClassicConfig;
import org.xper.drawing.object.BlankScreen;
import org.xper.png.expt.PngExptScene;
import org.xper.png.expt.PngExptSpecGenerator;
import org.xper.png.expt.generate.PngRandomGeneration;



@Configuration(defaultLazy=Lazy.TRUE)
@SystemPropertiesValueSource
@AnnotationDrivenConfig
@Import(GeneralConfig.class)
public class GAConfig {
	@Autowired ClassicConfig classicConfig;
	@Autowired GeneralConfig generalConfig;
	@Autowired AcqConfig acqConfig;
	@Autowired BaseConfig baseConfig;
	
	@Bean
	public SlideTrialExperiment experiment() {	// use standard experiment for GA *** this isn't working
		SlideTrialExperiment xper = new SlideTrialExperiment();
		xper.setStateObject(experimentState());
		return xper;
	}

	
	@Bean
	public PngExptScene taskScene() {
		PngExptScene scene = new PngExptScene();
		scene.setRenderer(generalConfig.experimentGLRenderer());
		scene.setDbUtil(generalConfig.pngDbUtil());
		scene.setFixation(classicConfig.experimentFixationPoint());
		scene.setMarker(classicConfig.screenMarker());
		scene.setBlankScreen(new BlankScreen());
		return scene;
	}
	
	@Bean
	public PngExptSpecGenerator generator() {
		PngExptSpecGenerator gen = new PngExptSpecGenerator();
		gen.setDbUtil(generalConfig.pngDbUtil());
		gen.setGlobalTimeUtil(acqConfig.timeClient());
		gen.setRenderer(generalConfig.experimentGLRenderer());
		return gen;
	}
	
	@Bean
	public PngRandomGeneration randomGen() {
		PngRandomGeneration gen = new PngRandomGeneration();
		gen.setDbUtil(generalConfig.pngDbUtil());
		gen.setGlobalTimeUtil(acqConfig.timeClient());
		gen.setRenderer(generalConfig.experimentGLRenderer());
		gen.setGenerator(generator());
		return gen;
	}

	@Bean
	public SlideTrialExperimentState experimentState() {
		SlideTrialExperimentState state = new SlideTrialExperimentState();
		state.setLocalTimeUtil(baseConfig.localTimeUtil());
		state.setTrialEventListeners(generalConfig.trialEventListeners());
		state.setSlideEventListeners(classicConfig.slideEventListeners());
		state.setEyeController(classicConfig.eyeController());
		state.setExperimentEventListeners(classicConfig.experimentEventListeners());
		state.setTaskDataSource(generalConfig.databaseTaskDataSource());
		state.setTaskDoneCache(classicConfig.taskDoneCache());
		state.setGlobalTimeClient(acqConfig.timeClient());
		state.setDrawingController(generalConfig.drawingController());
		state.setInterTrialInterval(classicConfig.xperInterTrialInterval());
		state.setTimeBeforeFixationPointOn(classicConfig.xperTimeBeforeFixationPointOn());
		state.setTimeAllowedForInitialEyeIn(classicConfig.xperTimeAllowedForInitialEyeIn());
		state.setRequiredEyeInHoldTime(classicConfig.xperRequiredEyeInHoldTime());
		state.setSlidePerTrial(classicConfig.xperSlidePerTrial());
		state.setSlideLength(classicConfig.xperSlideLength());
		state.setInterSlideInterval(classicConfig.xperInterSlideInterval());
		state.setDoEmptyTask(classicConfig.xperDoEmptyTask());
		state.setSleepWhileWait(true);
		state.setPause(classicConfig.xperExperimentInitialPause());
		state.setDelayAfterTrialComplete(classicConfig.xperDelayAfterTrialComplete());

		return state;
	}
	
}

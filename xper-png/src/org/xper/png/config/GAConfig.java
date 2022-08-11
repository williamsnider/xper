package org.xper.png.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.Import;
import org.springframework.config.java.annotation.Lazy;
import org.springframework.config.java.annotation.valuesource.SystemPropertiesValueSource;
import org.springframework.config.java.plugin.context.AnnotationDrivenConfig;
import org.xper.config.AcqConfig;
import org.xper.config.BaseConfig;
import org.xper.config.ClassicConfig;
import org.xper.drawing.object.BlankScreen;
import org.xper.drawing.object.FixationPoint;
import org.xper.png.expt.PngExptScene;
import org.xper.png.expt.PngExptSpecGenerator;
import org.xper.png.expt.generate.PngRandomGeneration;
import org.xper.png.vo.PngExperimentState;
import org.xper.png.PngTrialExperiment;


@Configuration(defaultLazy=Lazy.TRUE)
@SystemPropertiesValueSource
@AnnotationDrivenConfig
@Import(GeneralConfig.class)
public class GAConfig {
	@Autowired ClassicConfig classicConfig;
	@Autowired GeneralConfig generalConfig;
	@Autowired AcqConfig acqConfig;
	@Autowired BaseConfig baseConfig;
	
//	@Bean
//	public SlideTrialExperiment experiment() {	// use standard experiment for GA *** this isn't working
//		SlideTrialExperiment xper = new SlideTrialExperiment();
//		xper.setStateObject(experimentState());
//		return xper;
//	}


	@Bean
	public PngTrialExperiment experiment() {
		PngTrialExperiment xper = new PngTrialExperiment();
		xper.setStateObject(experimentState());
		// JK share the dbUtil with the experiment
		xper.setDbUtil(generalConfig.pngDbUtil());

//		xper.setEyeMonitor(classicConfig.eyeMonitor());
//		xper.setFirstSlideISI(alexConfig.xperFirstInterSlideInterval());		// these are no longer used -- see AlexTrialExperiment
//		xper.setFirstSlideLength(alexConfig.xperFirstSlideLength());			// these are no longer used -- see AlexTrialExperiment
//		xper.setEarlyTargetFixationAllowableTime(0);	// do not allow eyemovements during last stimulus for GA
		return xper;
	}
	
	
	@Bean
	public PngExptScene taskScene() {
		PngExptScene scene = new PngExptScene();
		scene.setScreenHeight(classicConfig.xperMonkeyScreenHeight());
		scene.setScreenWidth(classicConfig.xperMonkeyScreenWidth());
		FixationPoint f = classicConfig.experimentFixationPoint();
		
		scene.setRenderer(generalConfig.experimentGLRenderer());
		scene.setDbUtil(generalConfig.pngDbUtil());
		
		// Ram Feb 14, 2019
		// if the images contain the fixation spot, set the size to zero here
		// why do it here? because then the db fixation size can be used for fixcal
		// but if you are using xper's fixation spot for the experiment (in mono)
		// then comment the next line
		
//	
		f.setSize(0);
		scene.setFixation(f);
		
		scene.setMarker(classicConfig.screenMarker());
		scene.setBlankScreen(new BlankScreen());
//		scene.setBlankScreen(new PngBlankScreen());
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
	

	// -shs: set slide length and ISI here (not via db):
	@Bean
	public PngExperimentState experimentState() {
		PngExperimentState state = new PngExperimentState();
		state.setLocalTimeUtil(baseConfig.localTimeUtil());
		state.setTrialEventListeners(generalConfig.trialEventListeners());
		state.setSlideEventListeners(classicConfig.slideEventListeners());
		state.setEyeController(classicConfig.eyeController());
		state.setExperimentEventListeners(classicConfig.experimentEventListeners());
		state.setTaskDataSource(generalConfig.databaseTaskDataSource());
		state.setTaskDoneCache(classicConfig.taskDoneCache());
		state.setGlobalTimeClient(acqConfig.timeClient());
		state.setRequiredTargetSelectionHoldTime(generalConfig.xperRequiredTargetSelectionHoldTime());
		state.setTargetSelectionStartDelay(generalConfig.xperTargetSelectionEyeMonitorStartDelay());
		state.setTimeAllowedForInitialTargetSelection(generalConfig.xperTimeAllowedForInitialTargetSelection());
		state.setTargetSelector(generalConfig.eyeTargetSelector());
		state.setDrawingController(generalConfig.drawingController());
		state.setInterTrialInterval(classicConfig.xperInterTrialInterval());
		state.setTimeBeforeFixationPointOn(classicConfig.xperTimeBeforeFixationPointOn());
		state.setTimeAllowedForInitialEyeIn(classicConfig.xperTimeAllowedForInitialEyeIn());
		state.setRequiredEyeInHoldTime(classicConfig.xperRequiredEyeInHoldTime());
		state.setSlidePerTrial(classicConfig.xperSlidePerTrial());
		state.setSlideLength(classicConfig.xperSlideLength());
		state.setInterSlideInterval(classicConfig.xperInterSlideInterval());
		//state.setDoEmptyTask(classicConfig.xperDoEmptyTask());
		state.setDoEmptyTask(false);
		state.setSleepWhileWait(true);
		state.setPause(classicConfig.xperExperimentInitialPause());
		state.setDelayAfterTrialComplete(classicConfig.xperDelayAfterTrialComplete());
		state.setRepeatTrialIfEyeBreak(true);
	
		return state;
	}	
}

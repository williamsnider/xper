package org.xper.png.config;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.PixelFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.Import;
import org.springframework.config.java.annotation.Lazy;
import org.springframework.config.java.annotation.valuesource.SystemPropertiesValueSource;
import org.springframework.config.java.plugin.context.AnnotationDrivenConfig;
import org.springframework.config.java.util.DefaultScopes;
import org.xper.classic.JuiceController;
import org.xper.classic.MarkEveryStepTrialDrawingController;
import org.xper.classic.SlideEventLogger;
import org.xper.classic.SlideTrialExperiment;
import org.xper.classic.TrialDrawingController;
import org.xper.classic.TrialEventListener;
import org.xper.classic.TrialExperimentConsoleRenderer;
import org.xper.classic.TrialExperimentMessageDispatcher;
import org.xper.classic.TrialExperimentMessageHandler;
import org.xper.classic.vo.SlideTrialExperimentState;

import org.xper.config.AcqConfig;
import org.xper.config.BaseConfig;
import org.xper.config.ClassicConfig;

import org.xper.console.ExperimentConsole;
import org.xper.console.ExperimentMessageReceiver;

import org.xper.drawing.Coordinates2D;
import org.xper.drawing.object.BlankScreen;
import org.xper.drawing.object.Circle;
import org.xper.drawing.object.MonkeyWindow;
import org.xper.drawing.object.Square;
import org.xper.drawing.renderer.AbstractRenderer;
import org.xper.drawing.renderer.PerspectiveRenderer;
import org.xper.drawing.renderer.PerspectiveStereoRenderer;
import org.xper.experiment.DatabaseTaskDataSource;
import org.xper.experiment.DatabaseTaskDataSource.UngetPolicy;

import org.xper.eye.RobustEyeTargetSelector;
import org.xper.eye.listener.EyeSamplerEventListener;
import org.xper.eye.strategy.AnyEyeInStategy;
import org.xper.eye.strategy.EyeInStrategy;
import org.xper.eye.vo.EyeDeviceReading;

import org.xper.juice.mock.NullDynamicJuice;

import org.xper.png.util.PngDbUtil;

@Configuration(defaultLazy=Lazy.TRUE)
@SystemPropertiesValueSource
@AnnotationDrivenConfig
@Import(ClassicConfig.class)
public class GeneralConfig {
	@Autowired BaseConfig baseConfig;
	@Autowired ClassicConfig classicConfig;
	@Autowired AcqConfig acqConfig;
	
	
	@Bean
	public PngDbUtil pngDbUtil() {
		PngDbUtil util = new PngDbUtil();
		util.setDataSource(baseConfig.dataSource());
		return util;
	}
	
	@Bean
	public ExperimentConsole experimentConsole() {
		ExperimentConsole console = new ExperimentConsole();
		
		console.setPaused(classicConfig.xperExperimentInitialPause());
		console.setConsoleRenderer(consoleRenderer());
		console.setMonkeyScreenDimension(monkeyWindow().getScreenDimension());
		console.setModel(classicConfig.experimentConsoleModel());
//		console.setCanvasScaleFactor(2.5);
		
		ExperimentMessageReceiver receiver = classicConfig.messageReceiver();
		// register itself to avoid circular reference
		receiver.addMessageReceiverEventListener(console);
		
		return console;
	}
	
	@Bean
	public AbstractRenderer consoleGLRenderer() {
		PerspectiveRenderer renderer = new PerspectiveRenderer();
		renderer.setDistance(classicConfig.xperMonkeyScreenDistance());
		renderer.setDepth(classicConfig.xperMonkeyScreenDepth());
		renderer.setHeight(classicConfig.xperMonkeyScreenHeight());
		renderer.setWidth(classicConfig.xperMonkeyScreenWidth()/2);
		renderer.setPupilDistance(classicConfig.xperMonkeyPupilDistance());
		return renderer;
	}
	
	@Bean
	public MonkeyWindow monkeyWindow() {
		MonkeyWindow win = new MonkeyWindow();
		win.setFullscreen(classicConfig.monkeyWindowFullScreen);
		win.setPixelFormat(new PixelFormat(0, 8, 1, 4));
		return win;
	}
	
	@Bean
	public SlideTrialExperiment experiment() {
		SlideTrialExperiment xper = new SlideTrialExperiment();
		xper.setStateObject(experimentState());
		return xper;
	}
	
	@Bean(scope = DefaultScopes.PROTOTYPE)
	public Integer xperFirstSlideLength() {
		return Integer.parseInt(baseConfig.systemVariableContainer().get("xper_first_slide_length", 0));
	}

	@Bean(scope = DefaultScopes.PROTOTYPE)
	public Integer xperFirstInterSlideInterval() {
		return Integer.parseInt(baseConfig.systemVariableContainer().get("xper_first_inter_slide_interval", 0));
	}
	
	@Bean(scope = DefaultScopes.PROTOTYPE)
	public Integer xperBlankTargetScreenDisplayTime() {
		return Integer.parseInt(baseConfig.systemVariableContainer().get("xper_blank_target_screen_display_time", 0));
	}
	
	@Bean(scope = DefaultScopes.PROTOTYPE)
	public Integer xperEarlyTargetFixationAllowableTime() {
		return Integer.parseInt(baseConfig.systemVariableContainer().get("xper_early_target_fixation_allowable_time", 0));
	}

	@Bean
	public SlideTrialExperimentState experimentState() {
		SlideTrialExperimentState state = new SlideTrialExperimentState();
		state.setLocalTimeUtil(baseConfig.localTimeUtil());
		state.setTrialEventListeners(trialEventListeners());
		state.setSlideEventListeners(classicConfig.slideEventListeners());
		state.setEyeController(classicConfig.eyeController());
		state.setExperimentEventListeners(classicConfig.experimentEventListeners());
		state.setTaskDataSource(databaseTaskDataSource());
		state.setTaskDoneCache(classicConfig.taskDoneCache());
		state.setGlobalTimeClient(acqConfig.timeClient());
		state.setDrawingController(drawingController());
		state.setInterTrialInterval(classicConfig.xperInterTrialInterval());
		state.setTimeBeforeFixationPointOn(classicConfig.xperTimeBeforeFixationPointOn());
		state.setTimeAllowedForInitialEyeIn(classicConfig.xperTimeAllowedForInitialEyeIn());
		state.setRequiredEyeInHoldTime(classicConfig.xperRequiredEyeInHoldTime());
		state.setSlidePerTrial(classicConfig.xperSlidePerTrial());
		state.setSlideLength(classicConfig.xperSlideLength());						// slide length
		state.setInterSlideInterval(classicConfig.xperInterSlideInterval());		// slide ISI
		state.setDoEmptyTask(classicConfig.xperDoEmptyTask());
		state.setSleepWhileWait(true);
		state.setPause(classicConfig.xperExperimentInitialPause());
		state.setDelayAfterTrialComplete(classicConfig.xperDelayAfterTrialComplete());

		return state;
	}
	
	@Bean
	public DatabaseTaskDataSource databaseTaskDataSource () {
		DatabaseTaskDataSource source = new DatabaseTaskDataSource();
		source.setDbUtil(baseConfig.dbUtil());
		source.setQueryInterval(1000);
//		source.setUngetBehavior(UngetPolicy.TAIL);
		source.setUngetBehavior(UngetPolicy.HEAD);
		return source;
	}
	
	@Bean
	public TrialExperimentMessageHandler messageHandler() {
		TrialExperimentMessageHandler messageHandler = new TrialExperimentMessageHandler();
		HashMap<String, EyeDeviceReading> eyeDeviceReading = new HashMap<String, EyeDeviceReading>();
		eyeDeviceReading.put(classicConfig.xperLeftIscanId(), classicConfig.zeroEyeDeviceReading());
		eyeDeviceReading.put(classicConfig.xperRightIscanId(), classicConfig.zeroEyeDeviceReading());
		HashMap<String, Coordinates2D> eyeZero = new HashMap<String, Coordinates2D>();
		eyeZero.put(classicConfig.xperLeftIscanId(), classicConfig.xperLeftIscanEyeZero());
		eyeZero.put(classicConfig.xperRightIscanId(), classicConfig.xperRightIscanEyeZero());
		return messageHandler;
	}
	
	@Bean
	public TrialExperimentConsoleRenderer consoleRenderer () {
		TrialExperimentConsoleRenderer renderer = new TrialExperimentConsoleRenderer();
		renderer.setMessageHandler(messageHandler());
		renderer.setFixation(classicConfig.consoleFixationPoint());
		renderer.setRenderer(consoleGLRenderer());
		renderer.setBlankScreen(new BlankScreen());
		renderer.setCircle(new Circle());
		renderer.setSquare(new Square());
		return renderer;
	}
	
	@Bean (scope = DefaultScopes.PROTOTYPE)
	public List<TrialEventListener> trialEventListeners () {
		List<TrialEventListener> trialEventListener = new LinkedList<TrialEventListener>();
		trialEventListener.add(classicConfig.eyeMonitorController());
		trialEventListener.add((TrialEventListener) trialEventLogger());
		trialEventListener.add(classicConfig.experimentProfiler());
		trialEventListener.add(messageDispatcher());
		trialEventListener.add(classicConfig.juiceController());
		trialEventListener.add(classicConfig.trialSyncController());
		trialEventListener.add(classicConfig.dataAcqController());
		trialEventListener.add(classicConfig.jvmManager());
		return trialEventListener;
	}
	
	@Bean
	public SlideEventLogger trialEventLogger() {
		SlideEventLogger logger = new SlideEventLogger();
		return logger;
	}
	
	@Bean
	public TrialDrawingController drawingController() {
		MarkEveryStepTrialDrawingController controller = new MarkEveryStepTrialDrawingController();
		controller.setWindow(monkeyWindow());
		controller.setTaskScene(classicConfig.taskScene());
		controller.setFixationOnWithStimuli(classicConfig.xperFixationOnWithStimuli());
		return controller;
	}
	
	@Bean
	public TrialExperimentMessageDispatcher messageDispatcher() {
		TrialExperimentMessageDispatcher dispatcher = new TrialExperimentMessageDispatcher();
		dispatcher.setHost(classicConfig.experimentHost);
		dispatcher.setDbUtil(baseConfig.dbUtil());
		return dispatcher;
	}
	
	@Bean (scope = DefaultScopes.PROTOTYPE)
	public List<EyeSamplerEventListener> eyeSamplerEventListeners () {
		List<EyeSamplerEventListener> sampleListeners = new LinkedList<EyeSamplerEventListener>();
		sampleListeners.add(eyeTargetSelector());
		sampleListeners.add(classicConfig.eyeMonitor());
		return sampleListeners;
	}
	
	@Bean
	public RobustEyeTargetSelector eyeTargetSelector() {
		RobustEyeTargetSelector selector = new RobustEyeTargetSelector();
		selector.setEyeInstrategy(targetSelectorEyeInStrategy());
		selector.setLocalTimeUtil(baseConfig.localTimeUtil());
		selector.setTargetInTimeThreshold(xperTargetSelectionEyeInTimeThreshold());
		selector.setTargetOutTimeThreshold(xperTargetSelectionEyeOutTimeThreshold());
		return selector;
	}
	
	@Bean
	public EyeInStrategy targetSelectorEyeInStrategy() {
		AnyEyeInStategy strategy = new AnyEyeInStategy();
		List<String> devices = new LinkedList<String>();
		devices.add(classicConfig.xperLeftIscanId());
		devices.add(classicConfig.xperRightIscanId());
		strategy.setEyeDevices(devices);
		return strategy;
	}
	
	@Bean(scope = DefaultScopes.PROTOTYPE)
	public Long xperTimeAllowedForInitialTargetSelection() {
		return Long.parseLong(baseConfig.systemVariableContainer().get("xper_time_allowed_for_initial_target_selection", 0));
	}
	
	@Bean(scope = DefaultScopes.PROTOTYPE)
	public Long xperRequiredTargetSelectionHoldTime() {
		return Long.parseLong(baseConfig.systemVariableContainer().get("xper_required_target_selection_hold_time", 0));
	}
	
	@Bean(scope = DefaultScopes.PROTOTYPE)
	public Long xperTargetSelectionEyeInTimeThreshold() {
		return Long.parseLong(baseConfig.systemVariableContainer().get("xper_target_selection_eye_in_time_threshold", 0));
	}
	
	@Bean(scope = DefaultScopes.PROTOTYPE)
	public Long xperTargetSelectionEyeOutTimeThreshold() {
		return Long.parseLong(baseConfig.systemVariableContainer().get("xper_target_selection_eye_out_time_threshold", 0));
	}
	
	@Bean(scope = DefaultScopes.PROTOTYPE)
	public Long xperTargetSelectionEyeMonitorStartDelay() {
		return Long.parseLong(baseConfig.systemVariableContainer().get("xper_target_selection_eye_monitor_start_delay", 0));
	}
	
	/*@Bean
	public DigitalPortJuice xperDynamicJuice() {
		DigitalPortJuice juice = new DigitalPortJuice();
		juice.setTriggerDelay(acqConfig.digitalPortJuiceTriggerDelay);
		if (acqConfig.acqDriverName.equalsIgnoreCase(acqConfig.DAQ_NI)) {
			juice.setDevice(classicConfig.niDigitalPortJuiceDevice());
		} else if (acqConfig.acqDriverName.equalsIgnoreCase(acqConfig.DAQ_COMEDI)) {
			juice.setDevice(classicConfig.comediDigitalPortJuiceDevice());
		} else {
			throw new ExperimentSetupException("Acq driver " + acqConfig.acqDriverName + " not supported.");
		}
		return juice;
	}*/
	
	@Bean
	public TrialEventListener juiceController() {
		JuiceController controller = new JuiceController();
		if (acqConfig.acqDriverName.equalsIgnoreCase(acqConfig.DAQ_NONE)) {
			controller.setJuice(new NullDynamicJuice());
		} else {
			controller.setJuice(classicConfig.xperDynamicJuice());
		}
		return controller;
	}
	
	// *** added by SHS ***  
	
	/*	@Bean
	public PngPerspectiveRenderer experimentGLRenderer () {
		//PerspectiveStereoRenderer renderer = new PerspectiveStereoRenderer();
		//PerspectiveRenderer renderer = new PerspectiveRenderer();				// not using stereo
		PngPerspectiveRenderer renderer = new PngPerspectiveRenderer();		// using my version w background color setting
		renderer.setDistance(classicConfig.xperMonkeyScreenDistance());
		renderer.setDepth(classicConfig.xperMonkeyScreenDepth());
		renderer.setHeight(classicConfig.xperMonkeyScreenHeight());
		renderer.setWidth(classicConfig.xperMonkeyScreenWidth());
		renderer.setPupilDistance(classicConfig.xperMonkeyPupilDistance());
		//renderer.setInverted(classicConfig.xperMonkeyScreenInverted());  		// only used for stereo rendering
		renderer.setRgbColor(new RGBColor(0.5f,0.5f,0.5f));						// set background color to gray		
		return renderer;
	}*/
	
	@Bean
	public PerspectiveStereoRenderer experimentGLRenderer () {
		PerspectiveStereoRenderer renderer = new PerspectiveStereoRenderer();
		renderer.setDistance(classicConfig.xperMonkeyScreenDistance());
		renderer.setDepth(classicConfig.xperMonkeyScreenDepth());
		renderer.setHeight(classicConfig.xperMonkeyScreenHeight());
		renderer.setWidth(classicConfig.xperMonkeyScreenWidth());
		renderer.setPupilDistance(classicConfig.xperMonkeyPupilDistance());
		renderer.setInverted(classicConfig.xperMonkeyScreenInverted());  		// only used for stereo rendering
		return renderer;
	}
	
}
package org.xper.png;

import org.apache.log4j.Logger;
import org.xper.Dependency;
import org.xper.png.util.PngDbUtil;
import org.xper.png.vo.PngExperimentState;
import org.xper.classic.TrialRunner;
import org.xper.classic.SlideRunner;
import org.xper.classic.TrialDrawingController;
import org.xper.classic.vo.SlideTrialExperimentState;
import org.xper.classic.vo.TrialContext;
import org.xper.classic.vo.TrialResult;
import org.xper.experiment.Experiment;
import org.xper.experiment.ExperimentTask;
import org.xper.experiment.TaskDoneCache;
import org.xper.time.TimeUtil;
import org.xper.util.ThreadHelper;
import org.xper.util.TrialExperimentUtil;

/**
 * Format of StimSpec:
 * 
 * <StimSpec animation="true"> ... </StimSpec>
 * 
 * If attribute animation is false or missing, the stimulus is treated as a
 * static slide.
 * 
 * @author wang
 * 
 */
public class PngTrialExperiment implements Experiment {
	static Logger logger = Logger.getLogger(PngTrialExperiment.class);

	ThreadHelper threadHelper = new ThreadHelper("SlideTrialExperiment", this);

	@Dependency
	PngExperimentState stateObject;
	
	// JK 
	PngDbUtil dbUtil;

	public boolean isRunning() {
		return threadHelper.isRunning();
	}

	public void start() {
		threadHelper.start();
	}

	public void run() {
		TrialExperimentUtil.run(stateObject, threadHelper, new TrialRunner() {
			public TrialResult runTrial() {
				try {
//					System.out.println("\n\n JK 04225 PngTrialExperiment : fetching next task");
					// get a task
					TrialExperimentUtil.getNextTask(stateObject);
//					System.out.println("CURRENT TASK: "+stateObject.getCurrentTask());
//					System.out.println(stateObject.isDoEmptyTask());
					
					

					if (stateObject.getCurrentTask() == null && !stateObject.isDoEmptyTask()) {
						try {
//							System.out.println("JK 01126 PngTrialExperiment : null task");
							Thread.sleep(SlideTrialExperimentState.NO_TASK_SLEEP_INTERVAL);
						} catch (InterruptedException e) {
						}
//						System.out.println("JK 04126 PngTrialExperiment : TrialResult.NO_MORE_TASKS");
						return TrialResult.NO_MORE_TASKS;
					}

					// initialize trial context
					stateObject.setCurrentContext(new TrialContext());
					stateObject.getCurrentContext().setCurrentTask(stateObject.getCurrentTask());
					
					// run trial
					return TrialExperimentUtil.runTrial(stateObject, threadHelper, new SlideRunner() {

						public TrialResult runSlide() {
							int slidesPerTrial = stateObject.getNumSlidesPerTrial();
							stateObject.setAnimationStates(dbUtil);
//							System.out.println("JK28 PngTrialExperiment : slidesPerTrial =  " + slidesPerTrial);
							TrialDrawingController drawingController = stateObject.getDrawingController();
							ExperimentTask currentTask = stateObject.getCurrentTask();
							TrialContext currentContext = stateObject.getCurrentContext();	
							TaskDoneCache taskDoneCache = stateObject.getTaskDoneCache();
							TimeUtil globalTimeClient = stateObject.getGlobalTimeClient();
							
							try {
								for (int i = 0; i < slidesPerTrial; i++) {
//									System.out.println("PngTrialExper() runTrial() slide " + (i+1) + " of " + slidesPerTrial);

									stateObject.setAnimationForSlide(i);								
							
									// draw the slide
									TrialResult result = TrialExperimentUtil.doSlide(i, stateObject);
									if (result != TrialResult.SLIDE_OK) {
//										System.out.println("JK 7263 PngTrialExper() runTrial() !OK : " + result.toString() );
										return result;
									}
									// slide done successfully
									if (currentTask != null && i == slidesPerTrial - 1) {
//										System.out.println("JK 2225 PngTrialExper() runTrial() taskDone! " );
										taskDoneCache.put(currentTask, globalTimeClient
												.currentTimeMicros(), false);
										currentTask = null;
										stateObject.setCurrentTask(currentTask);
									}

									// prepare next task
									if (i < slidesPerTrial - 1) {
									
										currentContext.setSlideIndex(i + 1);
										currentContext.setCurrentTask(currentTask);
										drawingController.prepareNextSlide(currentTask,
												currentContext);
									}
									// inter slide interval
									result = TrialExperimentUtil.waitInterSlideInterval(stateObject, threadHelper);
									if (result != TrialResult.SLIDE_OK) {
										return result;
									}
								}
//								System.out.println("JK 5232 PngTrialExper() runTrial() : returning TRIAL_COMPLETE");
								return TrialResult.TRIAL_COMPLETE;
								// end of SlideRunner.runSlide
							} finally {
								try {
//									System.out.println("JK 5632 PngTrialExper() runTrial() : cleanupTask ");
									TrialExperimentUtil.cleanupTask(stateObject);
								} catch (Exception e) {
									logger.warn(e.getMessage());
									e.printStackTrace();
								}
							}
						}
						
					}); // end of TrialExperimentUtil.runTrial 
					// end of TrialRunner.runTrial	
				} finally {
					try {
						TrialExperimentUtil.cleanupTrial(stateObject);
					} catch (Exception e) {
						logger.warn(e.getMessage());
						e.printStackTrace();
					}
				}
			}}
		);
	}

	public void stop() {
		System.out.println("Stopping SlideTrialExperiment ...");
		if (isRunning()) {
			threadHelper.stop();
			threadHelper.join();
		}
	}

	public SlideTrialExperimentState getStateObject() {
		return stateObject;
	}

	public void setStateObject(PngExperimentState stateObject) {
		this.stateObject = stateObject;
	}

	public void setPause(boolean pause) {
		stateObject.setPause(pause);
	}
	
	public void setDbUtil(PngDbUtil dbUtil) {
		this.dbUtil = dbUtil;
	}
	

}

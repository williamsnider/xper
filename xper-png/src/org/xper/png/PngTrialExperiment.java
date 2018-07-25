package org.xper.png;

import org.apache.log4j.Logger;
import org.xper.Dependency;
import org.xper.png.util.PngDbUtil;
import org.xper.png.util.PngEventUtil;
import org.xper.png.util.PngExperimentUtil;
import org.xper.png.util.PngXmlUtil;
import org.xper.png.vo.PngExperimentState;
import org.xper.png.vo.PngTrialContext;
import org.xper.classic.TrialRunner;
import org.xper.classic.SlideRunner;
import org.xper.classic.TrialDrawingController;
import org.xper.classic.TrialEventListener;
import org.xper.classic.vo.SlideTrialExperimentState;
import org.xper.classic.vo.TrialContext;
import org.xper.classic.vo.TrialResult;
import org.xper.experiment.Experiment;
import org.xper.experiment.ExperimentTask;
import org.xper.experiment.TaskDoneCache;
import org.xper.eye.EyeMonitor;
import org.xper.png.vo.PngExperimentState;
import org.xper.time.TimeUtil;
import org.xper.util.ThreadHelper;
import org.xper.util.TrialExperimentUtil;
import org.xper.util.XmlUtil;

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
// JK 					TrialExperimentUtil.checkCurrentTaskAnimation(stateObject);
//System.out.println(stateObject.getCurrentTask().getStimSpec());
//System.out.println("JK27 PngTrialExperiment : run task");
					
					// run trial
					return TrialExperimentUtil.runTrial(stateObject, threadHelper, new SlideRunner() {

						public TrialResult runSlide() {
							int slidePerTrial = stateObject.getSlidePerTrial();
							
							System.out.println("JK28 PngTrialExperiment : slidesPerTrial =  " + slidePerTrial);
							TrialDrawingController drawingController = stateObject.getDrawingController();
							ExperimentTask currentTask = stateObject.getCurrentTask();
							TrialContext currentContext = stateObject.getCurrentContext();	
							TaskDoneCache taskDoneCache = stateObject.getTaskDoneCache();
							TimeUtil globalTimeClient = stateObject.getGlobalTimeClient();
							
							try {
								for (int i = 0; i < slidePerTrial; i++) {
//									System.out.println("PngTrialExper() runTrial() slide " + (i+1) + " of " + slidePerTrial);
									// draw the slide
									TrialResult result = TrialExperimentUtil.doSlide(i, stateObject);
									if (result != TrialResult.SLIDE_OK) {
//										System.out.println("JK 7263 PngTrialExper() runTrial() !OK : " + result.toString() );
										return result;
									}
									// slide done successfully
									if (currentTask != null && i == slidePerTrial - 1) {
//										System.out.println("JK 2225 PngTrialExper() runTrial() taskDone! " );
										taskDoneCache.put(currentTask, globalTimeClient
												.currentTimeMicros(), false);
										currentTask = null;
										stateObject.setCurrentTask(currentTask);
									}

									// prepare next task
									if (i < slidePerTrial - 1) {
//										TrialExperimentUtil.getNextTask(stateObject);
//										currentTask = stateObject.getCurrentTask();
//										if (currentTask == null && !stateObject.isDoEmptyTask()) {
//											try {
//												Thread.sleep(SlideTrialExperimentState.NO_TASK_SLEEP_INTERVAL);
//											} catch (InterruptedException e) {
//											}
//											//return TrialResult.NO_MORE_TASKS;
//											//deliver juice after complete.
//											System.out.println("JK 3232 PngTrialExper() runTrial() : returning TRIAL_COMPLETE early");
//
//											return TrialResult.TRIAL_COMPLETE;
//										}
										// JK stateObject.setAnimation(XmlUtil.slideIsAnimation(currentTask));
//										stateObject.setAnimation(false);
										
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

}
package org.xper.png;


import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.xper.Dependency;
import org.xper.png.util.PngDbUtil;
import org.xper.png.util.PngEventUtil;
import org.xper.png.util.PngExperimentUtil;
import org.xper.png.util.PngXmlUtil;
import org.xper.png.vo.PngExperimentState;
import org.xper.png.vo.PngTrialContext;
import org.xper.classic.SlideRunner;
import org.xper.classic.TrialDrawingController;
import org.xper.classic.TrialEventListener;
import org.xper.classic.TrialRunner;
import org.xper.classic.vo.SlideTrialExperimentState;
import org.xper.classic.vo.TrialResult;
import org.xper.config.BaseConfig;
import org.xper.drawing.Coordinates2D;
import org.xper.exception.XmlDocInvalidFormatException;
import org.xper.experiment.Experiment;
import org.xper.experiment.ExperimentTask;
import org.xper.experiment.TaskDoneCache;
import org.xper.eye.EyeMonitor;
import org.xper.eye.EyeTargetSelector;
import org.xper.eye.EyeTargetSelectorConcurrentDriver;
import org.xper.eye.TargetSelectorResult;
import org.xper.time.TimeUtil;
import org.xper.util.ThreadHelper;
import org.xper.util.ThreadUtil;
import org.xper.util.TrialExperimentUtil;
import org.xper.util.XmlUtil;



/**
 * Format of StimSpec:
 * 
 * 
 * <StimSpec> 
 * 	<object animation="false"> ... </object> 
 * 	<object animation="false"> ... </object> 
 *  ... (one to ten objects)
 * 	<object animation="false"> ... </object>
 *  <targetPosition>...</targetPosition>
 *  <targetEyeWinSize>...</targetEyeWinSize>
 *  <targetIndex>...</targetIndex>
 *  <reward>...</reward>
 * </StimSpec>
 * 
 * If attribute animation is false or missing, the object is treated as a static
 * slide.
 * 
 * @author wang
 * 
 */

public class PngTrialExperiment implements Experiment {
	static Logger logger = Logger.getLogger(PngTrialExperiment.class);

	@Dependency
	PngExperimentState stateObject;
	
	@Dependency
	EyeMonitor eyeMonitor;
	
	@Dependency
	int firstSlideISI;
	
	@Dependency
	int firstSlideLength;
	
	@Dependency
	int blankTargetScreenDisplayTime; // in milliseconds
	
	@Dependency
	int earlyTargetFixationAllowableTime; // in milliseconds
	
	ThreadHelper threadHelper = new ThreadHelper("PngTrialExperiment", this);
	
	// JK added all this to write the modified reward in the stimSpec back to the database
	@Autowired BaseConfig baseConfig;	
	PngDbUtil sdb = new PngDbUtil();
	boolean rewardIsDynamic = true;

	// JK 7 Sept 2016
	int maintainHoldTimeAdjustment = 0;
	
	// JK advanceOnCorrectOnly
	boolean advanceOnCorrectOnly = false;
	

	public boolean isRunning() {
		return threadHelper.isRunning();
	}

	public void start() {
		System.out.println("JK22 PngTrialExperiment : start()  ");
		threadHelper.start();
		

	}

	
	public void run() {
		// JK
		stateObject.setRepeatTrialIfEyeBreak(false);
		
		// JK  Make sure the database connection is valid
		if(true){
			sdb.setDataSource(baseConfig.dataSource());		
			//System.out.println("JK23 PngTrialExperiment : run()  repeatTrialIfEyeBreak == true");
			//System.out.println("JK24 PngTrialExperiment : run()  ISI = " + stateObject.getInterSlideInterval() + ".");
		}
				
		TrialExperimentUtil.run(stateObject, threadHelper, new TrialRunner() {

			public TrialResult runTrial() {
						
				// JK  
				//   update rewardIsDynamic
//				Float temp = Float.parseFloat (sdb.readSystemVar("xper_training_reward_is_dynamic").get("xper_training_reward_is_dynamic").getValue(0));

//				if(temp > 0.5f) {
//					rewardIsDynamic = true;
//				} else {
					rewardIsDynamic =  false;		
//				}
							
				TrialResult ret = TrialResult.INITIAL_EYE_IN_FAIL;
				try {
					// get a task
					TrialExperimentUtil.getNextTask(stateObject);
					
					if (stateObject.getCurrentTask() == null) {
						try {
							Thread.sleep(SlideTrialExperimentState.NO_TASK_SLEEP_INTERVAL);
						} catch (InterruptedException e) {
						}
						return TrialResult.NO_MORE_TASKS;
					}
					
					// parse and save the doc object for later use.
					stateObject.setCurrentSpecDoc(XmlUtil.parseSpec(stateObject.getCurrentTask().getStimSpec()));

					
					// initialized context
					PngTrialContext context = new PngTrialContext();
					context.setCurrentTask(stateObject.getCurrentTask());	// add current task to context!
					stateObject.setCurrentContext(context);
					
					final List<?> objectNodeList = stateObject.getCurrentSpecDoc().selectNodes("/StimSpec/object");
	final int countObject = 1; // objectNodeList.size();
					if (countObject == 0) {
						throw new XmlDocInvalidFormatException("No objects in match task specification.");
					}
					context.setCountObjects(countObject);
					if (logger.isDebugEnabled()) {
						logger.debug(stateObject.getCurrentTask().getTaskId() + " *****  jk *******" + countObject);
					}
				
					logger.debug(stateObject.getCurrentTask().getTaskId() + "     jk    " + countObject);
					// target info -shs
					Coordinates2D targetPosition = PngXmlUtil.getTargetPosition(stateObject.getCurrentSpecDoc());
					double targetEyeWinSize = PngXmlUtil.getTargetEyeWinSize(stateObject.getCurrentSpecDoc());
					long targetIndex = PngXmlUtil.getTargetIndex(stateObject.getCurrentSpecDoc());
					context.setTargetPos(targetPosition);
					context.setTargetEyeWindowSize(targetEyeWinSize);
					context.setTargetIndex(targetIndex);

					// reward info -shs
					long reward = PngXmlUtil.getReward(stateObject.getCurrentSpecDoc());
					context.setReward(reward);
					
					// first object animated?
					Node objectNode = (Node)objectNodeList.get(0);
		stateObject.setAnimation(true) ; //XmlUtil.isAnimation(objectNode));								

					// run task
					ret = TrialExperimentUtil.runTrial(stateObject,
							threadHelper, new SlideRunner() {

						public TrialResult runSlide() {
							TrialDrawingController drawingController = stateObject.getDrawingController();
							ExperimentTask currentTask = stateObject.getCurrentTask();
							PngTrialContext currentContext = (PngTrialContext) stateObject.getCurrentContext();
			currentContext.setAnimationFrameIndex(0);
			stateObject.setAnimation(true);
							TaskDoneCache taskDoneCache = stateObject.getTaskDoneCache();
							TimeUtil globalTimeClient = stateObject.getGlobalTimeClient();
							TimeUtil timeUtil = stateObject.getLocalTimeUtil();
							EyeTargetSelector targetSelector = stateObject.getTargetSelector();
							List<? extends TrialEventListener> trialEventListeners = stateObject.getTrialEventListeners();
							TrialResult result = TrialResult.FIXATION_SUCCESS;
							
							// JK 6 Sept 2016 
							Coordinates2D fixPt = new Coordinates2D (0.0, 0.0);
							Coordinates2D targetPt = currentContext.getTargetPos(); //new Coordinates2D (0.0, 10.0);
							Coordinates2D locations[] = { targetPt, fixPt };
							double targetSizes[] = { currentContext.getTargetEyeWindowSize(), currentContext.getTargetEyeWindowSize() };


							// JK set the reward for this trial based on past performance and update the stimspec in the database
							if(rewardIsDynamic){
								long newReward = currentContext.getReward() * stateObject.getCorrectTrialCount();
								currentContext.setReward(newReward);					
								PngXmlUtil.setReward(stateObject.getCurrentSpecDoc(), newReward);
								sdb.updateStimSpec(currentTask.getTaskId(), stateObject.getCurrentSpecDoc().asXML());
							}

							try {
								//int interSlideInterval = stateObject.getInterSlideInterval();
								//int slideLength = stateObject.getSlideLength();
								for (int i = 0; i < countObject; i++) {
									
									//if (i == 0) {			// ***commented out: now always using regular values for all slides
									//	stateObject.setInterSlideInterval(firstSlideISI);
									//	stateObject.setSlideLength(firstSlideLength);
									//} else {
									//	stateObject.setInterSlideInterval(interSlideInterval);
									//	stateObject.setSlideLength(slideLength);
									//}
									
									// show first slide, it's already draw in drawingController while waiting for monkey fixation
									result = TrialExperimentUtil.doSlide(i, stateObject);

									if (result != TrialResult.SLIDE_OK) {
										if (PngExperimentUtil.isTargetOn(currentContext) && currentContext.getTargetIndex() >= 0) {
											if (earlyTargetFixationAllowableTime < 0) {
												// ok to break fixation
											} else {
												long currentTime = timeUtil.currentTimeMicros();
												long earliestTime = currentContext.getCurrentSlideOnTime() + stateObject.getSlideLength() * 1000 - 
														earlyTargetFixationAllowableTime * 1000;
												if (currentTime >= earliestTime) {
													// ok to break fixation
												} else {
													PngEventUtil.fireTrialBREAKEvent(timeUtil.currentTimeMicros(), trialEventListeners, currentContext,i,false);
													// JK fail ... reset trialCorrectCounter
													if(rewardIsDynamic){														
														stateObject.countCorrectTrial(false);						
													}													
												
													return result;
												}
											}
										} else {
											PngEventUtil.fireTrialBREAKEvent(timeUtil.currentTimeMicros(), trialEventListeners, currentContext,i,false);
											// JK fail ... reset trialCorrectCounter
											if(rewardIsDynamic){		
												stateObject.countCorrectTrial(false);		
											}												
											return result;
										}
									}
									
									// JK 15 August trying to handle 50% ambiguity case
									//  first wait for delay to indicate which 
									if(PngExperimentUtil.isTargetOn(currentContext) && currentContext.getTargetIndex() == 2) {
																		
										System.out.println("PngTrialExperiment: reward all targets ");
										
										long targetOnLocalTime = currentContext.getCurrentSlideOffTime();
										currentContext.setTargetOnTime(targetOnLocalTime);
										PngEventUtil.fireTargetOnEvent(targetOnLocalTime, trialEventListeners, currentContext);
										
										// for the target screen, test for saccade, otherwise wait
										ThreadUtil.sleep(stateObject.getTargetSelectionStartDelay());

										EyeTargetSelectorConcurrentDriver selectorDriver = new EyeTargetSelectorConcurrentDriver(targetSelector, timeUtil);
										selectorDriver.start (locations, // new Coordinates2D[] {currentContext.getTargetPos()},
													targetSizes, //new double[] {currentContext.getTargetEyeWindowSize()}, 
												currentContext.getTargetOnTime() + stateObject.getTimeAllowedForInitialTargetSelection() * 1000
												+ stateObject.getTargetSelectionStartDelay() * 1000, 
												stateObject.getRequiredTargetSelectionHoldTime() * 1000);
							
										boolean targetShown = false;
										while (!selectorDriver.isDone()) {
											if (!targetShown) {
												if (timeUtil.currentTimeMicros() > targetOnLocalTime + blankTargetScreenDisplayTime * 1000) {
													((DefaultPngTrialDrawingController)drawingController).showTarget(currentTask, currentContext);
													targetShown = true;
												}
											}
										}

										selectorDriver.stop();

										TargetSelectorResult selectorResult = selectorDriver.getResult();
										int selectionNdx = selectorResult.getSelection();
										System.out.println("PngTrialExperiment: selected target # " + selectionNdx + " : " + selectorResult.getSelectionStatusResult().toString());
										
										// if maintain gaze
										if(selectionNdx == 1){
											maintainHoldTimeAdjustment = 123;
										}
										
									} else {
									
										// if target index is -1, there are no targets, so monkey should maintain fixation during inter trial interval
										if (PngExperimentUtil.isTargetOn(currentContext) && currentContext.getTargetIndex() >= 0) {
											long targetOnLocalTime = currentContext.getCurrentSlideOffTime();
											currentContext.setTargetOnTime(targetOnLocalTime);
											PngEventUtil.fireTargetOnEvent(targetOnLocalTime, trialEventListeners, currentContext);
	
											// for the target screen, test for saccade, otherwise wait
											ThreadUtil.sleep(stateObject.getTargetSelectionStartDelay());
	
											EyeTargetSelectorConcurrentDriver selectorDriver = new EyeTargetSelectorConcurrentDriver(targetSelector, timeUtil);
											selectorDriver.start(new Coordinates2D[] {currentContext.getTargetPos()},
													new double[] {currentContext.getTargetEyeWindowSize()}, 
													currentContext.getTargetOnTime() + stateObject.getTimeAllowedForInitialTargetSelection() * 1000
													+ stateObject.getTargetSelectionStartDelay() * 1000, 
													stateObject.getRequiredTargetSelectionHoldTime() * 1000);
	
											/*
												xper_blank_target_screen_display_time has to be smaller than xper_time_allowed_for_initial_target_selection.
												Otherwise the target screen won't be shown. 
											 */
											boolean targetShown = false;
											while (!selectorDriver.isDone()) {
												if (!targetShown) {
													if (timeUtil.currentTimeMicros() > targetOnLocalTime + blankTargetScreenDisplayTime * 1000) {
														((DefaultPngTrialDrawingController)drawingController).showTarget(currentTask, currentContext);
														targetShown = true;
													}
												}
											}
	
											selectorDriver.stop();
	
											// monkey fixate target. These information won't be available when the target selection is run in another thread.
											// the context object and the event listeners are not thread-safe.
											/*long targetInitialSelectionLocalTime = timeUtil.currentTimeMicros();
													currentContext.setTargetInitialSelectionTime(targetInitialSelectionLocalTime);
													PngEventUtil.fireTargetInitialSelectionEvent( targetInitialSelectionLocalTime,
																	trialEventListeners, currentContext);*/
	
											TargetSelectorResult selectorResult = selectorDriver.getResult();
								
						System.out.println("PngTrialExperiment: * selected target # " + selectorResult.getSelection() + " : " + selectorResult.getSelectionStatusResult().toString());
							
											if (selectorResult.getSelectionStatusResult() != TrialResult.TARGET_SELECTION_DONE) {
												TrialExperimentUtil.breakTrial(stateObject);
												// shs -- print out elapsed target time here:
												long targetFailTime = timeUtil.currentTimeMicros();
												PngEventUtil.fireTrialTARGETFAILEvent(targetFailTime, trialEventListeners, currentContext,selectorResult.getSelectionStatusResult(),targetOnLocalTime);
												PngExperimentUtil.waitTimeoutPenaltyDelay(stateObject, threadHelper);
												
												// JK fail ... reset trialCorrectCounter
												if(rewardIsDynamic){		
													stateObject.countCorrectTrial(false);															
												}
												
												// JK	
												if(advanceOnCorrectOnly){											
													TrialExperimentUtil.cleanupTask(stateObject);											
												}
		
												return selectorResult.getSelectionStatusResult();
											}
	
											long targetSelectionSuccessLocalTime = timeUtil.currentTimeMicros();
											// shs -- print out elapsed target time here:
											currentContext.setTargetSelectionSuccessTime(targetSelectionSuccessLocalTime);
											PngEventUtil.fireTargetSelectionSuccessEvent(targetSelectionSuccessLocalTime, trialEventListeners, currentContext);
											PngEventUtil.fireTrialTARGETPASSEvent(targetSelectionSuccessLocalTime, trialEventListeners, currentContext, stateObject.getRequiredTargetSelectionHoldTime(), targetOnLocalTime);
											// JK pass ... trialCorrectCounter
											if(rewardIsDynamic){		
												stateObject.countCorrectTrial(true);
											}										
	
											// clear target
											((DefaultPngTrialDrawingController)drawingController).targetSelectionDone(currentTask, currentContext);
										}
									} // if reward all targets

									boolean doISI = false;
									
									if (!PngExperimentUtil.isTargetOn(currentContext)) {	
										doISI = true; // need to check this here because the context is updated in the next step
									}

									if (i < countObject - 1) {
										// prepare second object
//										stateObject.setAnimation(XmlUtil.isAnimation((Node)objectNodeList.get(i+1)));
		stateObject.setAnimation(true);										
										currentContext.setSlideIndex(i + 1);
										// setTask is being called in prepareNextSlide, which is redundant since we are not getting new tasks.
										// It was designed for classic experiment designs, which can have multiple tasks per trial with one slide per task.
										// This experiment scheme is doing one task per trial with multiple slides defined inside one task.
										// We still need to draw new objects for next slide by calling prepareNextSlide.
										drawingController.prepareNextSlide(currentTask, currentContext);
									}


									
									//if (!PngExperimentUtil.isLastSlide(currentContext) || !PngExperimentUtil.isTargetTrial(currentContext)) { // if this is not a target trial or not the last slide -shs
									if (doISI) {	
										// do inter slide interval
										// JK
										int origISI = stateObject.getInterSlideInterval();		
										int maintainHoldTime = (int)stateObject.getMaintainHoldTime() - maintainHoldTimeAdjustment; 	
										// xper_maintain_hold_time
					
										// HACK: modify the stateObject ISI to non-target fixation duration
										if ((i == countObject-1) && !PngExperimentUtil.isTargetTrial(currentContext)) {
											System.out.println("changing ISI from " + origISI + " to maintainHoldTime " + maintainHoldTime);
											stateObject.setInterSlideInterval(maintainHoldTime);
										} 
										// JK 7 Sept
										else if( currentContext.getTargetIndex() == 2 ) {
											System.out.println(i + "changing ISI from " + origISI + " to maintainHoldTime " + maintainHoldTime);
											stateObject.setInterSlideInterval(maintainHoldTime);
											maintainHoldTimeAdjustment = 0;
										} 
										
										System.out.println("*** " + maintainHoldTime );
										
											
										result = TrialExperimentUtil.waitInterSlideInterval(stateObject,threadHelper);
										
										if (result != TrialResult.SLIDE_OK) {
											if ((i == countObject-1) && !PngExperimentUtil.isTargetTrial(currentContext)) {	// last slide and not target trial; need to hold fixation here -shs
												PngEventUtil.fireTrialFAILEvent(timeUtil.currentTimeMicros(), trialEventListeners, currentContext);
												PngExperimentUtil.waitTimeoutPenaltyDelay(stateObject, threadHelper);
												// JK fail ... reset trialCorrectCounter	
												if(rewardIsDynamic){
													stateObject.countCorrectTrial(false);																											
												}

											} else {
												PngEventUtil.fireTrialBREAKEvent(timeUtil.currentTimeMicros(), trialEventListeners, currentContext,i,true);
												// JK fail ... reset trialCorrectCounter
												if(rewardIsDynamic){
													stateObject.countCorrectTrial(false);
												}
												
											}
											stateObject.setInterSlideInterval(origISI);
											
											if(advanceOnCorrectOnly){											
												TrialExperimentUtil.cleanupTask(stateObject);
											}
													
											return result;
										}
										stateObject.setInterSlideInterval(origISI);
									}
								} // end 'for' loop

								if (PngExperimentUtil.isLastSlide(currentContext) && !PngExperimentUtil.isTargetTrial(currentContext)) {	// shs
									PngEventUtil.fireTrialPASSEvent(timeUtil.currentTimeMicros(), trialEventListeners, currentContext);
									System.out.println("Correct?! On to next trial ");
									// JK pass ... trialCorrectCounter
									if(rewardIsDynamic){
										stateObject.countCorrectTrial(true);
									}
									
								}

								//stateObject.setInterSlideInterval(interSlideInterval);		// ***commented out: now always using regular values for all slides
								//stateObject.setSlideLength(slideLength);

								// trial finished successfully
								// set task to null so that it won't get repeated.
								if (currentTask != null) {
										taskDoneCache.put(currentTask,globalTimeClient.currentTimeMicros(),false);
										currentTask = null;
										stateObject.setCurrentTask(currentTask);			// not sure about this ....  
										
								}
								
								return TrialResult.TRIAL_COMPLETE;
							} finally {
								try {
									// Do not repeat task (unless repeatTrialIfEyeBreak=true & EYE_BREAK)
									if (!stateObject.isRepeatTrialIfEyeBreak() || result != TrialResult.EYE_BREAK) {
										stateObject.setCurrentTask(null); // Do not repeat task
										
									}
									TrialExperimentUtil.cleanupTask(stateObject);
								} catch (Exception e) {
									logger.warn(e.getMessage());
									e.printStackTrace();
								}
							}
						}
					});		// end 'run task'
					
					return ret;
					
				} finally {
					//System.out.println(ret);	// for debugging
					try {
						// repeat if INITIAL_EYE_IN_FAIL or EYE_IN_HOLD_FAIL, otherwise do not repeat
						if (ret != TrialResult.INITIAL_EYE_IN_FAIL && ret != TrialResult.EYE_IN_HOLD_FAIL && ret != TrialResult.EYE_BREAK) {
							stateObject.setCurrentTask(null); // Do not repeat task
						}
						TrialExperimentUtil.cleanupTrial(stateObject);
					} catch (Exception e) {
						logger.warn(e.getMessage());
						e.printStackTrace();
					}
				}


			}
		});
	}

	public void stop() {
		System.out.println("Stopping PngTrialExperiment ...");

				
		if (isRunning()) {
			threadHelper.stop();
			threadHelper.join();
		}
	}

	public void setPause(boolean pause) {
		stateObject.setPause(pause);
	}

	public PngExperimentState getStateObject() {
		return stateObject;
	}

	public void setStateObject(PngExperimentState stateObject) {
		this.stateObject = stateObject;
	}

	public EyeMonitor getEyeMonitor() {
		return eyeMonitor;
	}

	public void setEyeMonitor(EyeMonitor eyeMonitor) {
		this.eyeMonitor = eyeMonitor;
	}

	public int getFirstSlideISI() {
		return firstSlideISI;
	}

	public void setFirstSlideISI(int firstSlideISI) {
		this.firstSlideISI = firstSlideISI;
	}

	public int getFirstSlideLength() {
		return firstSlideLength;
	}

	public void setFirstSlideLength(int firstSlideLength) {
		this.firstSlideLength = firstSlideLength;
	}

	public int getBlankTargetScreenDisplayTime() {
		return blankTargetScreenDisplayTime;
	}

	public void setBlankTargetScreenDisplayTime(int blankTargetScreenDisplayTime) {
		this.blankTargetScreenDisplayTime = blankTargetScreenDisplayTime;
	}

	public int getEarlyTargetFixationAllowableTime() {
		return earlyTargetFixationAllowableTime;
	}

	public void setEarlyTargetFixationAllowableTime(
			int earlyTargetFixationAllowableTime) {
		this.earlyTargetFixationAllowableTime = earlyTargetFixationAllowableTime;
	}
}

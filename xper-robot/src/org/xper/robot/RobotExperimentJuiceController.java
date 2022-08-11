package org.xper.robot;

import java.sql.Timestamp;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xper.Dependency;
import org.xper.classic.TrialEventListener;
import org.xper.classic.vo.TrialContext;
import org.xper.juice.DynamicJuice;
import org.xper.robot.vo.RobotTrialContext;

public class RobotExperimentJuiceController implements TrialEventListener {
	// JK 26 Apr 2017  static Logger logger = Logger.getLogger(RobotExperimentJuiceController.class);
	static Logger logger = LogManager.getLogger(RobotExperimentJuiceController.class);
	
	@Dependency
	DynamicJuice juice;

	public void eyeInBreak(long timestamp, TrialContext context) {
	}

	public void eyeInHoldFail(long timestamp, TrialContext context) {
	}

	public void fixationPointOn(long timestamp, TrialContext context) {
	}

	public void fixationSucceed(long timestamp, TrialContext context) {
	}

	public void initialEyeInFail(long timestamp, TrialContext context) {
	}

	public void initialEyeInSucceed(long timestamp, TrialContext context) {
	}

	public void trialComplete(long timestamp, TrialContext context) {
		RobotTrialContext c = (RobotTrialContext)context;
		long reward = c.getReward();
		juice.setReward(reward);
		juice.deliver();
		System.out.println("Juice delivered " + reward + " @ " + new Timestamp(timestamp/1000).toString());
	}
	
	public void trialInit(long timestamp, TrialContext context) {
	}

	public void trialStart(long timestamp, TrialContext context) {
	}

	public void trialStop(long timestamp, TrialContext context) {
	}

	public DynamicJuice getJuice() {
		return juice;
	}

	public void setJuice(DynamicJuice juice) {
		this.juice = juice;
	}

}

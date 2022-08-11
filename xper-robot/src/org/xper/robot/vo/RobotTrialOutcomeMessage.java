package org.xper.robot.vo;

import com.thoughtworks.xstream.XStream;

public class RobotTrialOutcomeMessage {
	String outcome;
	long taskID;
	long timestamp;
	long genId;
	
	public RobotTrialOutcomeMessage(long timestamp, String outcome, long taskID, long genId) {
		super();
		this.outcome = outcome;
		this.taskID = taskID;
		this.timestamp = timestamp;
		this.genId = genId;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getOutcome() {
		return outcome;
	}
	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}
	public long getTaskID() {
		return taskID;
	}
	public void setTaskID(long taskID) {
		this.taskID = taskID;
	}
	public long getGenId() {
		return genId;
	}
	public void setGenId(long genId) {
		this.genId = genId;
	}


	static XStream xstream = new XStream();

	static {
		xstream.alias("RobotTrialOutcomeMessage", RobotTrialOutcomeMessage.class);
	}
	
	public static RobotTrialOutcomeMessage fromXml (String xml) {
		return (RobotTrialOutcomeMessage)xstream.fromXML(xml);
	}
	
	public static String toXml (RobotTrialOutcomeMessage msg) {
		return xstream.toXML(msg);
	}
}

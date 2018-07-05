package org.xper.png.vo;

import com.thoughtworks.xstream.XStream;

public class PngTrialOutcomeMessage {
	String outcome;
	long taskID;
	long timestamp;
	long genId;
	
	public PngTrialOutcomeMessage(long timestamp, String outcome, long taskID, long genId) {
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
		xstream.alias("PngTrialOutcomeMessage", PngTrialOutcomeMessage.class);
	}
	
	public static PngTrialOutcomeMessage fromXml (String xml) {
		return (PngTrialOutcomeMessage)xstream.fromXML(xml);
	}
	
	public static String toXml (PngTrialOutcomeMessage msg) {
		return xstream.toXML(msg);
	}
}

package org.xper.png.util;

import org.xper.png.util.PngIOUtil;

import com.thoughtworks.xstream.XStream;

public class ExpLogMessage {
	String status;		// start, stop, gen_done, ... ?
	String trialType;	// which trial type (ga, beh, etc) ?
	String prefix;		// which generation?
	long runNum;		// which generation?
	long genNum;		// which generation?
	String dateTime;	// readable date/time string
	long timestamp;

	
	public ExpLogMessage(String status, String trialType, String prefix, long runNum, long genNum, long timestamp) {
		super();
		setStatus(status);
		setTrialType(trialType);
		setGenNum(genNum);
		setPrefix(prefix);
		setRunNum(runNum);
		setTimestamp(timestamp);
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
		this.dateTime = PngIOUtil.formatMicroSeconds(timestamp);
	}
	public String getDateTime() {
		return dateTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTrialType() {
		return trialType;
	}
	public void setTrialType(String trialType) {
		this.trialType = trialType;
	}
	public long getGenNum() {
		return genNum;
	}
	public void setGenNum(long genNum) {
		this.genNum = genNum;
	}
	public long getRunNum() {
		return runNum;
	}
	public void setRunNum(long runNum) {
		this.runNum = runNum;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	static XStream xstream = new XStream();

	static {
		xstream.alias("ExpLogMessage", ExpLogMessage.class);
	}
	
	public static ExpLogMessage fromXml (String xml) {
		return (ExpLogMessage)xstream.fromXML(xml);
	}
	
	public static String toXml (ExpLogMessage msg) {
		return xstream.toXML(msg);
	}
}

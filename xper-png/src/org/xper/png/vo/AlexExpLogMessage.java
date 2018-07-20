package org.xper.png.vo;

import org.xper.png.util.PngIOUtil;

import com.thoughtworks.xstream.XStream;

public class AlexExpLogMessage {
	String status;				// start, stop, gen_done, ... ?
	String trialType;			// which trial type (ga, beh, etc) ?
	long depth = -1;			// electrode depth, if applicable
	long genNum = -1;			// which generation?
	long globalGenId = -1;		// this is the genId in TaskToDo db table
	boolean realExp;			// is this a real or mock expt?
	String dateTime;			// readable date/time string
	long timestamp;
	long cellNum = -1;			// cell number as YYYYMMDDXXX

	
	public AlexExpLogMessage(String status, String trialType, long genNum, long globalGenId, boolean realExp, long timestamp) {
		super();
		setStatus(status);
		setTrialType(trialType);
		setGenNum(genNum);
		setGlobalGenId(globalGenId);
		setRealExp(realExp);
		setTimestamp(timestamp);
	}
	public AlexExpLogMessage(String status, String trialType,long depth, long genNum, long globalGenId, boolean realExp, long timestamp) {
		super();
		setStatus(status);
		setTrialType(trialType);
		setDepth(depth);
		setGenNum(genNum);
		setGlobalGenId(globalGenId);
		setRealExp(realExp);
		setTimestamp(timestamp);
	}
	
	public AlexExpLogMessage(String status, String trialType,long depth, long genNum, long globalGenId, long cellNum, boolean realExp, long timestamp) {
		super();
		setStatus(status);
		setTrialType(trialType);
		setDepth(depth);
		setGenNum(genNum);
		setGlobalGenId(globalGenId);
		setCellNum(cellNum);
		setRealExp(realExp);
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
	public long getGlobalGenId() {
		return globalGenId;
	}
	public void setGlobalGenId(long genId) {
		this.globalGenId = genId;
	}
	public boolean getRealExp() {
		return realExp;
	}
	public void setRealExp(boolean realExp) {
		this.realExp = realExp;
	}

	public long getDepth() {
		return depth;
	}
	public void setDepth(long depth) {
		this.depth = depth;
	}

	
	public long getCellNum() {
		return cellNum;
	}
	public void setCellNum(long cellNum) {
		this.cellNum = cellNum;
	}


	static XStream xstream = new XStream();

	static {
		xstream.alias("AlexExpLogMessage", AlexExpLogMessage.class);
	}
	
	public static AlexExpLogMessage fromXml (String xml) {
		return (AlexExpLogMessage)xstream.fromXML(xml);
	}
	
	public static String toXml (AlexExpLogMessage msg) {
		return xstream.toXML(msg);
	}
}

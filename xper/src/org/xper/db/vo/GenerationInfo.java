package org.xper.db.vo;

import com.thoughtworks.xstream.XStream;

public class GenerationInfo {
	String prefix = "";
	long runNum = 0;
	long genId = 0;
	int taskCount = 0;
	
	transient static XStream s;
	
	static {
		s = new XStream();
		s.alias("GenerationInfo", GenerationInfo.class);
	}
	
	public String toXml () {
		return GenerationInfo.toXml(this);
	}
	
	public static String toXml (GenerationInfo genInfo) {
		return s.toXML(genInfo);
	}
	
	public static GenerationInfo fromXml (String xml) {
		GenerationInfo g = (GenerationInfo)s.fromXML(xml);
		return g;
	}
	
	public long getRunNum() {
		return runNum;
	}

	public void setRunNum(long runNum) {
		this.runNum = runNum;
	}
	
	public long getGenId() {
		return genId;
	}

	public void setGenId(long genId) {
		this.genId = genId;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public int getTaskCount() {
		return taskCount;
	}

	public void setTaskCount(int taskCount) {
		this.taskCount = taskCount;
	}
	
}

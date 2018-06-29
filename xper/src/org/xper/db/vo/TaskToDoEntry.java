package org.xper.db.vo;

public class TaskToDoEntry {
	long taskId;
	long stimId;
	long xfmId;
	long genId;
	long linId;
	public long getGenId() {
		return genId;
	}
	public void setGenId(long genId) {
		this.genId = genId;
	}
	public long getLinId() {
		return linId;
	}
	public void setLinId(long linId) {
		this.linId = linId;
	}
	public long getStimId() {
		return stimId;
	}
	public void setStimId(long stimId) {
		this.stimId = stimId;
	}
	public long getTaskId() {
		return taskId;
	}
	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}
	public long getXfmId() {
		return xfmId;
	}
	public void setXfmId(long xfmId) {
		this.xfmId = xfmId;
	}
}

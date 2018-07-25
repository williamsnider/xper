package org.xper.png.vo;

import org.xper.db.vo.TaskDoneEntry;

public class PngTaskDoneEntry extends TaskDoneEntry {
	long tstamp_local;

	public long getTstampLocal() {
		return tstamp_local;
	}
	public void setTstampLocal(long tstamp_local) {
		this.tstamp_local = tstamp_local;
	}
}

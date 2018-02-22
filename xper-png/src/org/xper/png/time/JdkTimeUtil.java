package org.xper.png.time;

import org.xper.time.TimeUtil;

public class JdkTimeUtil implements TimeUtil {

	public long currentTimeMicros() {
		return System.currentTimeMillis() * 1000;
	}

}

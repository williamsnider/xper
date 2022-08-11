package org.xper.robot;


import org.xper.classic.TrialExperimentConsoleRenderer;
import org.xper.drawing.Context;
import org.xper.drawing.Coordinates2D;
import org.xper.drawing.RGBColor;
import org.xper.robot.util.RobotExperimentUtil;

public class RobotExperimentConsoleRenderer extends
		TrialExperimentConsoleRenderer {
	
	@Override
	public void drawCanvas(Context context, String devId) {
		super.drawCanvas(context, devId);
		if (messageHandler instanceof RobotExperimentMessageHandler) {
			RobotExperimentMessageHandler r = (RobotExperimentMessageHandler) messageHandler;
			if (r.isTargetOn()) {
				RGBColor targetColor = new RGBColor(1f, 0f, 0f);
				
				Coordinates2D pos = r.getTargetPosition();
				double eyeWinSize = r.getTargetEyeWindowSize();
				if (pos != null) {
					RobotExperimentUtil.drawTargetEyeWindow(renderer, pos, eyeWinSize, targetColor);
				}
			}
		}
	}
}

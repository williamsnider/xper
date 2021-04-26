package org.xper.png;


import org.xper.png.util.PngExperimentUtil;
import org.xper.classic.TrialExperimentConsoleRenderer;
import org.xper.drawing.Context;
import org.xper.drawing.Coordinates2D;
import org.xper.drawing.RGBColor;

public class PngExperimentConsoleRenderer extends
		TrialExperimentConsoleRenderer {
	
	@Override
	public void drawCanvas(Context context, String devId) {
		super.drawCanvas(context, devId);
		if (messageHandler instanceof PngExperimentMessageHandler) {
			PngExperimentMessageHandler r = (PngExperimentMessageHandler) messageHandler;
			if (r.isTargetOn()) {
				RGBColor targetColor = new RGBColor(1f, 0f, 0f);
				
				Coordinates2D pos = r.getTargetPosition();
				double eyeWinSize = r.getTargetEyeWindowSize();
				if (pos != null) {
					PngExperimentUtil.drawTargetEyeWindow(renderer, pos, eyeWinSize, targetColor);
				}
			}
		}
	}
}

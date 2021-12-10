package org.xper.png.util;


import org.xper.png.vo.PngExperimentState;
import org.xper.png.vo.PngTrialContext;
import org.xper.drawing.Coordinates2D;
import org.xper.drawing.RGBColor;
import org.xper.drawing.object.Circle;
import org.xper.drawing.renderer.AbstractRenderer;
import org.xper.time.TimeUtil;
import org.xper.util.ThreadHelper;
import org.xper.util.ThreadUtil;

public class PngExperimentUtil {
	
	public static void getNextTask(PngDbUtil dbUtil, PngExperimentState state) {
		
		// this  TrialExperimentUtil.getNextTask(state):
		state.setCurrentTask(state.getTaskDataSource().getNextTask());
	}
	
	public static boolean isTargetOn (PngTrialContext context) {
		//if (context.getSlideIndex() == context.getCountObjects() - 1) {
		if ((context.getTargetIndex() >= 0) && (context.getSlideIndex() == context.getCountObjects() - 1)) {	// this actually asks if this is the target, and not just the last slide
			return true;
		} else {
			return false;
		}
	}

	public static boolean isLastSlide (PngTrialContext context) {
		if (context.getSlideIndex() == context.getCountObjects() - 1) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isTargetTrial (PngTrialContext context) {
		if (context.getTargetIndex() >= 0) {
			return true;
		} else {
			return false;
		}
	}

	public static void drawTargetEyeWindow(AbstractRenderer renderer, Coordinates2D pos, double size, RGBColor targetColor) {
		Circle eyeWin = new Circle();
		eyeWin.setSolid(false);

		double x = renderer.deg2mm(pos.getX());
		double y = renderer.deg2mm(pos.getY());
		double s = renderer.deg2mm(size);

		PngGLUtil.drawCircle(eyeWin, x, y, s, targetColor.getRed(), targetColor.getGreen(), targetColor.getBlue());
	}
	
	public static void waitTimeoutPenaltyDelay(PngExperimentState state, ThreadHelper threadHelper) {
		TimeUtil timeUtil = state.getLocalTimeUtil();
		
		// -shs: wait for timeout penalty delay after trial failure
		if (state.getTimeoutPenaltyDelay() > 0) {
			long current = timeUtil.currentTimeMicros();
			ThreadUtil.sleepOrPinUtil(current+state.getTimeoutPenaltyDelay()*1000,state,threadHelper);
		}
	}
	
	
}
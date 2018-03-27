package org.xper.png.drawing.stimuli;

public class MirrorSymmetry {
	boolean mirrorX;

	boolean mirrorY;

	boolean mirrorZ;

	public MirrorSymmetry(boolean mirrorX, boolean mirrorY, boolean mirrorZ) {
		super();
		this.mirrorX = mirrorX;
		this.mirrorY = mirrorY;
		this.mirrorZ = mirrorZ;
	}

	public boolean getMirrorX() {
		return mirrorX;
	}

	public void setMirrorX(boolean mirrorX) {
		this.mirrorX = mirrorX;
	}

	public boolean getMirrorY() {
		return mirrorY;
	}

	public void setMirrorY(boolean mirrorY) {
		this.mirrorY = mirrorY;
	}

	public boolean getMirrorZ() {
		return mirrorZ;
	}

	public void setMirrorZ(boolean mirrorZ) {
		this.mirrorZ = mirrorZ;
	}

	public MirrorSymmetry() {
	}
}
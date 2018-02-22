package org.xper.png.drawing.stimuli;

import org.xper.drawing.Context;
import org.xper.drawing.Drawable;
import org.xper.png.drawing.stick.MStickSpec;
import org.xper.png.drawing.stick.MatchStick;

public class PngObject implements Drawable {
	Long id;
	String descId;
	String stimType;
	
	PngObjectSpec pngSpec;
	MStickSpec stickSpec;
	
	MatchStick stick;
		
	@Override
	public void draw(Context context) {
		stick.drawSkeleton();
	}
	
	
	void finalizeObject() {
		if (pngSpec.doStickGen) {
			stick = new MatchStick();
			stick.genMatchStickRand();
			stickSpec = new MStickSpec();
			stickSpec.setMStickInfo(stick);
		} else if (pngSpec.doStickMorph) {
			stick = new MatchStick();
			stick.genMatchStickFromShapeSpec(stickSpec);
			stick.mutate(0);
			stickSpec.setMStickInfo(stick);
		} else if (pngSpec.doBlenderMorph) {
			stick = new MatchStick();
			stick.genMatchStickFromShapeSpec(stickSpec);
		}
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDescId() {
		return descId;
	}
	public void setDescId(String descId) {
		this.descId = descId;
	}
	public String getStimType() {
		return stimType;
	}
	public void setStimType(String type) {
		this.stimType = type;
	}
	public PngObjectSpec getSpec_java() {
		return pngSpec;
	}
	public void setSpec_java(PngObjectSpec spec) {
		this.pngSpec = spec;
		finalizeObject();
	}
	public MStickSpec getSpec_stick() {
		return stickSpec;
	}
	public void setSpec_stick(MStickSpec spec) {
		this.stickSpec = spec;
	}


}

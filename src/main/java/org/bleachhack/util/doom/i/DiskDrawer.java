package org.bleachhack.util.doom.i;

import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.rr.patch_t;
import static org.bleachhack.util.doom.v.renderers.DoomScreen.FG;

public class DiskDrawer implements IDiskDrawer {

    private DoomMain<?,?> DOOM;
	private patch_t disk;
	private int timer=0;
	private String diskname;
	
	public static final String STDISK="STDISK";
	public static final String STCDROM="STCDROM";
	
	public DiskDrawer(DoomMain<?,?> DOOM, String icon){		
		this.DOOM = DOOM;
		this.diskname=icon;
	}

	@Override
	public void Init(){
		this.disk=DOOM.wadLoader.CachePatchName(diskname);
	}
	
	@Override
	public void Drawer() {
		if (timer>0){
			if (timer%2==0)
                DOOM.graphicSystem.DrawPatchScaled(FG, disk, DOOM.vs, 304, 184);
		}
		if (timer>=0)
			timer--;
	}

	@Override
	public void setReading(int reading) {
		timer=reading;
	}

	@Override
	public boolean isReading() {
		return timer>0;
	}

	@Override
	public boolean justDoneReading() {
		return timer==0;
	}
	
}

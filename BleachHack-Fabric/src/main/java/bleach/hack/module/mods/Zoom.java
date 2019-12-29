package bleach.hack.module.mods;

import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;


public class Zoom extends Module {
	private static boolean activated = false;
	private static double loz; //level of zoom
	
	public Zoom() {
		super("Zoom", -1, Category.MOVEMENT, "I spy with my little eye",
			new SettingSlider("CPS: ", 2, 64, 3, 0));
	}

	@Override
	public void onDisable() {
		activated = false;
	}

	@Override
	public void onEnable() {
		activated = true;
		loz = getSettings().get(0).toSlider().getValue();
	}
	
	public static double getZoom(double realFov) {
		if(activated) return realFov/loz;
		return realFov;
	}
}

package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;

public class Flight extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingSlider(0, 5, 1, 1, "Speed: "),
			new SettingToggle(true, "Anti Kick"));
	
	private int timer = 0;
	
	public Flight() {
		super("Flight", -1, Category.MOVEMENT, "Allows you to fly", settings);
	}
	
	public void onDisable() {
		if(!mc.player.abilities.isCreativeMode) mc.player.abilities.allowFlying = false;
		mc.player.abilities.isFlying = false;
	}

	public void onUpdate() {
		if(this.isToggled()) {
			float speed = (float) getSettings().get(0).toSlider().getValue();
			
			mc.player.abilities.setFlySpeed(speed / 10);
			mc.player.abilities.allowFlying = true;
			mc.player.abilities.isFlying = true;
			
			if(getSettings().get(1).toToggle().state) {
				if(timer == 16) mc.player.setMotion(mc.player.getMotion().x, -0.155, mc.player.getMotion().z);
				if(timer == 20) mc.player.setMotion(mc.player.getMotion().x, 0.1, mc.player.getMotion().z);
			}

			
			if(timer <= 20) {timer++;}else {timer=0;}
		}
	}
}

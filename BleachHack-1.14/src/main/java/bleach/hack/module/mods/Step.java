package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.module.Category;
import bleach.hack.module.Module;

public class Step extends Module{
	
	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode(new String[] {"Simple", "??? idk"}, "Mode: "));
	
	public Step() {
		super("Step", 0, Category.MOVEMENT, "Allows you to Run up blocks like stairs.", settings);
	}
	
	private int tickCount;
	
	public void onUpdate() {
		if(this.isToggled()) {
			if(getSettings().get(0).toMode().mode == 0) {
				mc.player.stepHeight = 1.065F;
			}else if(getSettings().get(0).toMode().mode == 1) {
				
				if(mc.player.collidedHorizontally) {
					if(tickCount<1) {
						mc.player.posY = mc.player.posY + 1.065;
						mc.player.jump();
						tickCount++;
					}
					
					if(tickCount==1) {
						mc.player.posY = mc.player.posY - 0.5;
						mc.player.setSprinting(true);
						tickCount = 0;
					}
				}
			}
		}
	}
	
	public void onDisable() {
		mc.player.stepHeight = 0.5F;
	}

}

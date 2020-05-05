/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
 * Copyright (c) 2019 Bleach.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.module.Category;
import bleach.hack.module.Module;

public class Step extends Module {
	
	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode(new String[] {"Simple", "Spider"}, "Mode: "));
	
	public Step() {
		super("Step", -1, Category.MOVEMENT, "Allows you to Run up blocks like stairs.", settings);
	}
	
	private int tickCount;
	
	public void onUpdate() {
		if (this.isToggled()) {
			if (getSettings().get(0).toMode().mode == 0) {
				mc.player.stepHeight = 1.065F;
			} else if (getSettings().get(0).toMode().mode == 1) {
				
				if (mc.player.collidedHorizontally) {
					tickCount = 0;
				}
				
				tickCount++;
				
				if (tickCount < 2) {
					mc.player.setMotion(mc.player.getMotion().x, 1, mc.player.getMotion().z);
					mc.player.jump();
				}
					
				if (tickCount == 2) {
					mc.player.setMotion(mc.player.getMotion().x, 0, mc.player.getMotion().z);
					mc.player.setSprinting(true);
				}
			}
		}
	}
	
	public void onDisable() {
		mc.player.stepHeight = 0.5F;
	}

}

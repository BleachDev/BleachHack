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

import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.util.Hand;

public class CrystalAura extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(false, "Aimbot"),
			new SettingToggle(false, "Thru Walls"),
			new SettingSlider(0, 6, 4.25, 2, "Range: "),
			new SettingSlider(0, 20, 16, 0, "CPS: "));
	
	private int delay = 0;
	
	public CrystalAura() {
		super("CrystalAura", GLFW.GLFW_KEY_I, Category.COMBAT, "Automatically attacks crystals for you.", settings);
	}
	
	public void onUpdate() {
		if(this.isToggled()) {
			delay++;
			int reqDelay = (int) Math.round(20/getSettings().get(3).toSlider().getValue());
			
			for(Entity e: EntityUtils.getLoadedEntities()) {
				if(e instanceof EnderCrystalEntity && mc.player.getDistance(e) < getSettings().get(2).toSlider().getValue()) {
					if(!mc.player.canEntityBeSeen(e) && !getSettings().get(1).toToggle().state) continue;
					if(getSettings().get(0).toToggle().state) EntityUtils.facePos(e.posX, e.posY+e.getHeight()/2, e.posZ);
					
					if(delay > reqDelay || reqDelay == 0) {
						mc.playerController.attackEntity(mc.player, e);
						mc.player.swingArm(Hand.MAIN_HAND);
						delay=0;
					}
				}
			}
		}
	}

}

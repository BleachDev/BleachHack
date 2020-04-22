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
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.util.math.Vec3d;

public class ElytraFly extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingSlider(0, 5, 0.8, 2, "Speed: "));
			
	public ElytraFly() {
		super("ElytraFly", -1, Category.MOVEMENT, "Improves the elytra", settings);
	}
	
	public void onUpdate() {
		if(this.isToggled()) {
			if (mc.player.isElytraFlying()) {
				Vec3d vec3d = new Vec3d(0,0,getSettings().get(0).toSlider().getValue())
						.rotatePitch(-(float) Math.toRadians(mc.player.rotationPitch))
						.rotateYaw(-(float) Math.toRadians(mc.player.rotationYaw));
				
				mc.player.setMotion(
						mc.player.getMotion().x + vec3d.x + (vec3d.x - mc.player.getMotion().x),
						mc.player.getMotion().y + vec3d.y + (vec3d.y - mc.player.getMotion().y),
						mc.player.getMotion().z + vec3d.z + (vec3d.z - mc.player.getMotion().z));
				
			}
		}
	}

}

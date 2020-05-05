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
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.util.math.Vec3d;

public class Flight extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode(new String[] {"Normal","Static","Jetpack"}, "Mode: "),
			new SettingSlider(0, 5, 1, 1, "Speed: "));
	
	public Flight() {
		super("Flight", GLFW.GLFW_KEY_G, Category.MOVEMENT, "Allows you to fly", settings);
	}
	
	public void onDisable() {
		if (!mc.player.abilities.isCreativeMode) mc.player.abilities.allowFlying = false;
		mc.player.abilities.isFlying = false;
	}

	public void onUpdate() {
		if (this.isToggled()) {
			float speed = (float) getSettings().get(1).toSlider().getValue();
			
			if (getSettings().get(0).toMode().mode == 0) {
				mc.player.abilities.setFlySpeed(speed / 10);
				mc.player.abilities.allowFlying = true;
				mc.player.abilities.isFlying = true;
			} else if (getSettings().get(0).toMode().mode == 1) {
				mc.player.setMotion(0, mc.player.ticksExisted % 20 == 0 ? -0.06 : 0, 0);
				Vec3d forward = new Vec3d(0, 0, speed).rotateYaw(-(float) Math.toRadians(mc.player.rotationYaw));
				Vec3d strafe = forward.rotateYaw((float) Math.toRadians(90));
				
				if (mc.gameSettings.keyBindJump.isKeyDown()) mc.player.setMotion(mc.player.getMotion().add(0, speed, 0));
				if (mc.gameSettings.keyBindSneak.isKeyDown()) mc.player.setMotion(mc.player.getMotion().add(0, -speed, 0));
				if (mc.gameSettings.keyBindForward.isKeyDown()) mc.player.setMotion(mc.player.getMotion().add(forward.x, 0, forward.z));
				if (mc.gameSettings.keyBindBack.isKeyDown()) mc.player.setMotion(mc.player.getMotion().add(-forward.x, 0, -forward.z));
				if (mc.gameSettings.keyBindLeft.isKeyDown()) mc.player.setMotion(mc.player.getMotion().add(strafe.x, 0, strafe.z));
				if (mc.gameSettings.keyBindRight.isKeyDown()) mc.player.setMotion(mc.player.getMotion().add(-strafe.x, 0, -strafe.z));

			} else if (getSettings().get(0).toMode().mode == 2) {
				if (!mc.gameSettings.keyBindJump.isKeyDown()) return;
				mc.player.setMotion(mc.player.getMotion().x, speed / 3, mc.player.getMotion().z);
			}
		}
	}
}

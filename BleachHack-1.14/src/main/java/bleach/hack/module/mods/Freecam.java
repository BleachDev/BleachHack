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
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;

public class Freecam extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingSlider(0, 2, 0.5, 2, "Speed: "));
	
	private BoatEntity camera; /* Invisible boat used as the camera */
	private ArmorStandEntity dummy; /* Armorstand used as a dummy for the player */
	double[] playerPos;
	
	public Freecam() {
		super("Freecam", GLFW.GLFW_KEY_U, Category.PLAYER, "Its freecam, you know what it does", settings);
	}
	
	public void onEnable() {
		playerPos = new double[]{mc.player.posX,mc.player.posY,mc.player.posZ};
		
		camera = new BoatEntity(mc.world, mc.player.posX, mc.player.posY, mc.player.posZ);
		camera.copyLocationAndAnglesFrom(mc.player);
		camera.entityCollisionReduction = 1;
		
		dummy = new ArmorStandEntity(mc.world, mc.player.posX, mc.player.posY, mc.player.posZ);
		dummy.copyLocationAndAnglesFrom(mc.player);
		dummy.setBoundingBox(dummy.getBoundingBox().grow(0.1));
		EntityUtils.setGlowing(dummy, TextFormatting.RED, "starmygithubpls");
		
		mc.world.addEntity(camera.getEntityId(), camera);
		mc.world.addEntity(dummy.getEntityId(), dummy);
		mc.renderViewEntity = camera;
	}
	
	public void onDisable() {
		mc.renderViewEntity = mc.player;
		camera.remove();
		dummy.remove();
	}
	
	public void onUpdate() {
		if (this.isToggled()) {
			mc.player.setMotion(0, 0, 0);
			mc.player.setPosition(playerPos[0], playerPos[1], playerPos[2]);
			
			dummy.rotationYaw = camera.rotationYaw = mc.player.rotationYaw;
			dummy.rotationPitch = camera.rotationPitch = mc.player.rotationPitch;
			
			double speed = getSettings().get(0).toSlider().getValue();
			Vec3d forward = new Vec3d(0, 0, speed * 2.5).rotateYaw(-(float) Math.toRadians(camera.rotationYaw));
			Vec3d strafe = forward.rotateYaw((float) Math.toRadians(90));
			Vec3d motion = Vec3d.ZERO;
			
			if (mc.gameSettings.keyBindJump.isKeyDown()) motion = motion.add(0, speed, 0);
			if (mc.gameSettings.keyBindSneak.isKeyDown()) motion = motion.add(0, -speed, 0);
			if (mc.gameSettings.keyBindForward.isKeyDown()) motion = motion.add(forward.x, 0, forward.z);
			if (mc.gameSettings.keyBindBack.isKeyDown()) motion = motion.add(-forward.x, 0, -forward.z);
			if (mc.gameSettings.keyBindLeft.isKeyDown()) motion = motion.add(strafe.x, 0, strafe.z);
			if (mc.gameSettings.keyBindRight.isKeyDown()) motion = motion.add(-strafe.x, 0, -strafe.z);
			
			camera.setPosition(camera.posX + motion.x, camera.posY + motion.y, camera.posZ + motion.z);
		}
	}

}

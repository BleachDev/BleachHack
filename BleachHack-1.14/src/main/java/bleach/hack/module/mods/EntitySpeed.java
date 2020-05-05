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
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.BlockPos;

public class EntitySpeed extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingSlider(0, 5, 1.2, 2, "Speed: "),
			new SettingToggle(false, "EntityFly"),
			new SettingToggle(false, "Ground Snap"));
	
	public EntitySpeed() {
		super("EntitySpeed", GLFW.GLFW_KEY_GRAVE_ACCENT, Category.MOVEMENT, "Allows you to go fast while riding entities.", settings);
	}
	
	public void onUpdate() {
		if (this.isToggled()) {
			if (mc.player.getRidingEntity() == null) return;
			
			Entity e = mc.player.getRidingEntity();
			double speed = getSettings().get(0).toSlider().getValue();
			
			if (e instanceof LlamaEntity) {
				e.rotationYaw = mc.player.rotationYaw;
				((LlamaEntity) e).rotationYawHead = mc.player.rotationYawHead;
			}
			MovementInput movementInput = mc.player.movementInput;
			double forward = movementInput.moveForward;
			double strafe = movementInput.moveStrafe;
			float yaw = mc.player.rotationYaw;
			if ((forward == 0.0D) && (strafe == 0.0D)) {
				e.setMotion(0, e.getMotion().y, 0);
			} else {
				if (forward != 0.0D) {
					if (strafe > 0.0D) { yaw += (forward > 0.0D ? -45 : 45);
					} else if (strafe < 0.0D) yaw += (forward > 0.0D ? 45 : -45);
					strafe = 0.0D;
					if (forward > 0.0D) { forward = 1.0D;
					} else if (forward < 0.0D) forward = -1.0D;
				}
				e.setMotion((forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F))), e.getMotion().y,
						forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F)));
				if (e instanceof MinecartEntity) {
					MinecartEntity em = (MinecartEntity) e;
					em.setVelocity((forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F))), em.getMotion().y, (forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F))));
				}
				
				if (getSettings().get(1).toToggle().state) if (mc.gameSettings.keyBindJump.isKeyDown()) e.setMotion(e.getMotion().x, 0.3, e.getMotion().z);
				
				if (getSettings().get(2).toToggle().state) {
					BlockPos p = e.getPosition().add(0, 0.5, 0);
					if (mc.world.getBlockState(p.down()).getBlock() == Blocks.AIR &&
						mc.world.getBlockState(p.down(2)).getBlock() != Blocks.AIR &&
						!(mc.world.getBlockState(p.down(2)).getMaterial() == Material.WATER) &&
						e.fallDistance > 0.01) e.setMotion(e.getMotion().x, -1, e.getMotion().z);
				}
			}
		}
	}

}

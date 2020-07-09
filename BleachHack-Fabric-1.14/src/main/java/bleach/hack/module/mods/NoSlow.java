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

import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.WorldUtils;

import org.lwjgl.glfw.GLFW;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

public class NoSlow extends Module {
	
	private Vec3d addVelocity = Vec3d.ZERO;
	
	public NoSlow() {
		super("NoSlow", KEY_UNBOUND, Category.MOVEMENT, "Disables Stuff From Slowing You Down",
				new SettingToggle("Slowness", true),
				new SettingToggle("Soul Sand", true),
				new SettingToggle("Slime Blocks", true),
				new SettingToggle("Webs", true),
				new SettingToggle("Items", true),
				new SettingToggle("Inventory", false));
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (!isToggled()) return;
			
		/* Slowness */
		if (getSettings().get(0).toToggle().state && (mc.player.getStatusEffect(StatusEffects.SLOWNESS) != null || mc.player.getStatusEffect(StatusEffects.BLINDNESS) != null)) {
			if (mc.options.keyForward.isPressed() 
					&& mc.player.getVelocity().x > -0.15 && mc.player.getVelocity().x < 0.15
					&& mc.player.getVelocity().z > -0.15 && mc.player.getVelocity().z < 0.15) {
				mc.player.setVelocity(mc.player.getVelocity().add(addVelocity));
				addVelocity = addVelocity.add(new Vec3d(0, 0, 0.05).rotateY(-(float)Math.toRadians(mc.player.yaw)));
			} else addVelocity = addVelocity.multiply(0.75, 0.75, 0.75);
		}
		
		/* Soul Sand */
		if (getSettings().get(1).toToggle().state && WorldUtils.doesBoxTouchBlock(mc.player.getBoundingBox(), Blocks.SOUL_SAND)) {
			Vec3d m = new Vec3d(0, 0, 0.125).rotateY(-(float) Math.toRadians(mc.player.yaw));
			if (!mc.player.abilities.flying && mc.options.keyForward.isPressed()) {
				mc.player.setVelocity(mc.player.getVelocity().add(m));
			}
		}
		
		/* Slime Block */
		if (getSettings().get(2).toToggle().state && WorldUtils.doesBoxTouchBlock(mc.player.getBoundingBox().offset(0,-0.02,0), Blocks.SLIME_BLOCK)) {
			Vec3d m1 = new Vec3d(0, 0, 0.1).rotateY(-(float) Math.toRadians(mc.player.yaw));
			if (!mc.player.abilities.flying && mc.options.keyForward.isPressed()) {
				mc.player.setVelocity(mc.player.getVelocity().add(m1));
			}
		}
		
		/* Web */
		if (getSettings().get(3).toToggle().state && WorldUtils.doesBoxTouchBlock(mc.player.getBoundingBox(), Blocks.COBWEB)) {
			Vec3d m2 = new Vec3d(0, -1, 0.9).rotateY(-(float) Math.toRadians(mc.player.yaw));
			if (!mc.player.abilities.flying && mc.options.keyForward.isPressed()) {
				mc.player.setVelocity(mc.player.getVelocity().add(m2));
			}
		}
		
		// Items handled in MixinPlayerEntity:sendMovementPackets_isUsingItem
		
		/* Inventory */
		if (getSettings().get(5).toToggle().state && mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen)) {
			for (KeyBinding k: new KeyBinding[] {
					mc.options.keyForward, mc.options.keyBack, mc.options.keyLeft,
					mc.options.keyRight, mc.options.keyJump, mc.options.keySneak}) {
				KeyBinding.setKeyPressed(
						InputUtil.fromName(k.getName()), InputUtil.isKeyPressed(mc.window.getHandle(), InputUtil.fromName(k.getName()).getKeyCode()));
			}

			if (InputUtil.isKeyPressed(mc.window.getHandle(), GLFW.GLFW_KEY_LEFT)) {
				mc.player.yaw -= 3.5f;
			}

			if (InputUtil.isKeyPressed(mc.window.getHandle(), GLFW.GLFW_KEY_RIGHT)) {
				mc.player.yaw += 3.5f;
			}
		}
	}
}

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
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;

import com.google.common.eventbus.Subscribe;

import net.minecraft.item.Items;
import net.minecraft.server.network.packet.ClientCommandC2SPacket;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;
import net.minecraft.server.network.packet.ClientCommandC2SPacket.Mode;
import net.minecraft.util.math.Vec3d;

public class ElytraFly extends Module {
			
	public ElytraFly() {
		super("ElytraFly", -1, Category.MOVEMENT, "Improves the elytra",
				new SettingMode("Mode: ", "Normal", "Control", "Bruh Momentum"),
				new SettingToggle("FlatFly", false),
				new SettingSlider("Speed: ", 0, 5, 0.8, 2));
	}

	@Subscribe
	public void onTick(EventTick event) {
		Vec3d vec3d = new Vec3d(0,0,getSettings().get(2).toSlider().getValue())
				.rotateX(getSettings().get(1).toToggle().state ? 0 : -(float) Math.toRadians(mc.player.pitch))
				.rotateY(-(float) Math.toRadians(mc.player.yaw));
		
		if (mc.player.isFallFlying()) {
			if (getSettings().get(0).toMode().mode == 0) {
				mc.player.setVelocity(
						mc.player.getVelocity().x + vec3d.x + (vec3d.x - mc.player.getVelocity().x),
						mc.player.getVelocity().y + vec3d.y + (vec3d.y - mc.player.getVelocity().y),
						mc.player.getVelocity().z + vec3d.z + (vec3d.z - mc.player.getVelocity().z));
			} else if (getSettings().get(0).toMode().mode == 1) {
				if (mc.options.keyBack.isPressed()) vec3d = vec3d.multiply(-1);
				else if (mc.options.keyLeft.isPressed()) vec3d = vec3d.rotateY((float) Math.toRadians(90));
				else if (mc.options.keyRight.isPressed()) vec3d = vec3d.rotateY(-(float) Math.toRadians(90));
				else if (mc.options.keyJump.isPressed()) vec3d = new Vec3d(0, getSettings().get(2).toSlider().getValue(), 0);
				else if (mc.options.keySneak.isPressed()) vec3d = new Vec3d(0, -getSettings().get(2).toSlider().getValue(), 0);
				else if (!mc.options.keyForward.isPressed()) vec3d = Vec3d.ZERO;
				mc.player.setVelocity(vec3d);
			}
		} else if (getSettings().get(0).toMode().mode == 2 && !mc.player.onGround 
				&& mc.player.inventory.getArmorStack(2).getItem() == Items.ELYTRA && mc.player.fallDistance > 0.5) {
			/* I tried packet mode and got whatever the fuck **i mean frick** this is */
			if (mc.options.keySneak.isPressed()) return;
			mc.player.setVelocity(vec3d);
			mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket(true));
		}
	}
}

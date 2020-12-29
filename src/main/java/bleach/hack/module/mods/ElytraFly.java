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

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventClientMove;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class ElytraFly extends Module {

	public ElytraFly() {
		super("ElytraFly", KEY_UNBOUND, Category.MOVEMENT, "Improves the elytra",
				new SettingMode("Mode", "Normal", "Control", "Bruh Momentum"),
				new SettingSlider("Speed", 0, 5, 0.8, 2));
	}

	@Subscribe
	public void onClientMove(EventClientMove event) {
		/* Cancel the retarded auto elytra movement */
		if (getSetting(0).asMode().mode == 1 && mc.player.isFallFlying()) {
			if (!mc.options.keyJump.isPressed() && !mc.options.keySneak.isPressed()) {
				event.vec3d = new Vec3d(event.vec3d.x, 0, event.vec3d.z);
			}

			if (!mc.options.keyBack.isPressed() && !mc.options.keyLeft.isPressed()
					&& !mc.options.keyRight.isPressed() && !mc.options.keyForward.isPressed()) {
				event.vec3d = new Vec3d(0, event.vec3d.y, 0);
			}
		}
	}

	@Subscribe
	public void onTick(EventTick event) {
		Vec3d vec3d = new Vec3d(0, 0, getSetting(1).asSlider().getValue())
				.rotateX(getSetting(0).asMode().mode == 1 ? 0 : -(float) Math.toRadians(mc.player.pitch))
				.rotateY(-(float) Math.toRadians(mc.player.yaw));

		// if (getSetting(0).toMode().mode == 1) vec3d = new Vec3d(vec3d.x, 0, vec3d.z);

		if (mc.player.isFallFlying()) {
			if (getSetting(0).asMode().mode == 0 && mc.options.keyForward.isPressed()) {
				mc.player.setVelocity(
						mc.player.getVelocity().x + vec3d.x + (vec3d.x - mc.player.getVelocity().x),
						mc.player.getVelocity().y + vec3d.y + (vec3d.y - mc.player.getVelocity().y),
						mc.player.getVelocity().z + vec3d.z + (vec3d.z - mc.player.getVelocity().z));
			} else if (getSetting(0).asMode().mode == 1) {
				if (mc.options.keyBack.isPressed())
					vec3d = vec3d.multiply(-1);
				if (mc.options.keyLeft.isPressed())
					vec3d = vec3d.rotateY((float) Math.toRadians(90));
				if (mc.options.keyRight.isPressed())
					vec3d = vec3d.rotateY(-(float) Math.toRadians(90));
				if (mc.options.keyJump.isPressed())
					vec3d = vec3d.add(0, getSetting(1).asSlider().getValue(), 0);
				if (mc.options.keySneak.isPressed())
					vec3d = vec3d.add(0, -getSetting(1).asSlider().getValue(), 0);
				if (!mc.options.keyBack.isPressed() && !mc.options.keyLeft.isPressed()
						&& !mc.options.keyRight.isPressed() && !mc.options.keyForward.isPressed()
						&& !mc.options.keyJump.isPressed() && !mc.options.keySneak.isPressed())
					vec3d = Vec3d.ZERO;
				mc.player.setVelocity(vec3d.multiply(2));
			}
		} else if (getSetting(0).asMode().mode == 2 && !mc.player.isOnGround()
				&& mc.player.inventory.getArmorStack(2).getItem() == Items.ELYTRA && mc.player.fallDistance > 0.5) {
			/* I tried packet mode and got whatever the fuck **i mean frick** this is */
			if (mc.options.keySneak.isPressed())
				return;
			mc.player.setVelocity(vec3d);
			mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket(true));
		}
	}
}

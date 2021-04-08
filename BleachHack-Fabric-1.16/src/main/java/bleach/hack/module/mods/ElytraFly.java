/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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

import org.apache.commons.lang3.RandomUtils;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventClientMove;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSendMovementPackets;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.util.FabricReflect;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ElytraFly extends Module {

	public ElytraFly() {
		super("ElytraFly", KEY_UNBOUND, Category.MOVEMENT, "Improves the elytra",
				new SettingMode("Mode", "AutoBoost", "Boost", "Control", "BruhFly", "Pak\u00e8tFly").withDesc("Elytrafly mode"),
				new SettingSlider("Boost", 0, 0.15, 0.05, 2).withDesc("Boost speed"),
				new SettingSlider("MaxBoost", 0, 5, 2.5, 1).withDesc("Max boost speed"),
				new SettingSlider("Speed", 0, 5, 0.8, 2).withDesc("Speed for all the other modes"),
				new SettingSlider("Packets", 1, 10, 2, 0).withDesc("How many packets to send in packet mode"));
	}

	@Subscribe
	public void onClientMove(EventClientMove event) {
		/* Cancel the retarded auto elytra movement */
		if (getSetting(0).asMode().mode == 2 && mc.player.isFallFlying()) {
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
		Vec3d vec3d = new Vec3d(0, 0, getSetting(3).asSlider().getValue())
				.rotateY(-(float) Math.toRadians(mc.player.yaw));

		double currentVel = Math.abs(mc.player.getVelocity().x) + Math.abs(mc.player.getVelocity().y) + Math.abs(mc.player.getVelocity().z);
		float radianYaw = (float) Math.toRadians(mc.player.yaw);
		float boost = (float) getSetting(1).asSlider().getValue();

		switch (getSetting(0).asMode().mode) {
			case 0:
				if (mc.player.isFallFlying() && currentVel <= getSetting(2).asSlider().getValue()) {
					if (mc.options.keyBack.isPressed()) {
						mc.player.addVelocity(MathHelper.sin(radianYaw) * boost, 0, MathHelper.cos(radianYaw) * -boost);
					} else if (mc.player.pitch > 0) {
						mc.player.addVelocity(MathHelper.sin(radianYaw) * -boost, 0, MathHelper.cos(radianYaw) * boost);
					}
				}

				break;
			case 1:
				if (mc.player.isFallFlying() && currentVel <= getSetting(2).asSlider().getValue()) {
					if (mc.options.keyForward.isPressed()) {
						mc.player.addVelocity(MathHelper.sin(radianYaw) * -boost, 0, MathHelper.cos(radianYaw) * boost);
					} else if (mc.options.keyBack.isPressed()) {
						mc.player.addVelocity(MathHelper.sin(radianYaw) * boost, 0, MathHelper.cos(radianYaw) * -boost);
					}
				}

				break;
			case 2:
				if (mc.player.isFallFlying() && mc.options.keyForward.isPressed()) {
					mc.player.setVelocity(
							mc.player.getVelocity().x + vec3d.x + (vec3d.x - mc.player.getVelocity().x),
							mc.player.getVelocity().y + vec3d.y + (vec3d.y - mc.player.getVelocity().y),
							mc.player.getVelocity().z + vec3d.z + (vec3d.z - mc.player.getVelocity().z));
				}

				break;
			case 3:
				if (shouldPacketFly()) {
					mc.player.setVelocity(vec3d);
					mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
					mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(
							mc.player.getX() + vec3d.x, mc.player.getY() + vec3d.y, mc.player.getZ() + vec3d.z, true));
				}

				break;
			case 4:
				if (shouldPacketFly()) {
					mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
					double randMult = RandomUtils.nextDouble(0.9, 1.1);

					mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(
							mc.player.getX() + vec3d.x * randMult,
							mc.player.getY(),
							mc.player.getZ() + vec3d.z * randMult,
							false));

					for (int i = 0; i < 6; i++) {
						mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(
								mc.player.getX() + vec3d.x * (randMult + i),
								mc.player.getY() - 0.0001,
								mc.player.getZ() + vec3d.z * (randMult + i),
								true));
					}
				}
		}
	}

	// Packet moment

	@Subscribe
	public void onMovement(EventSendMovementPackets event) {
		if (getSetting(0).asMode().mode == 4 && shouldPacketFly()) {
			mc.player.setVelocity(Vec3d.ZERO);
			event.setCancelled(true);
		}
	}

	@Subscribe
	public void onMovement(EventClientMove event) {
		if (getSetting(0).asMode().mode == 4 && shouldPacketFly()) {
			event.setCancelled(true);
		}
	}

	@Subscribe
	public void onReadPacket(EventReadPacket event) {
		if (getSetting(0).asMode().mode == 4 && shouldPacketFly() && event.getPacket() instanceof PlayerPositionLookS2CPacket) {
			PlayerPositionLookS2CPacket p = (PlayerPositionLookS2CPacket) event.getPacket();

			FabricReflect.writeField(p, mc.player.yaw, "field_12391", "yaw");
			FabricReflect.writeField(p, mc.player.pitch, "field_12393", "pitch");
		}
	}

	@Subscribe
	public void onSendPacket(EventSendPacket event) {
		if (getSetting(0).asMode().mode == 4 && shouldPacketFly()) {
			if (event.getPacket() instanceof PlayerMoveC2SPacket.LookOnly) {
				event.setCancelled(true);
				return;
			}

			if (event.getPacket() instanceof PlayerMoveC2SPacket.Both) {
				event.setCancelled(true);
				PlayerMoveC2SPacket p = (PlayerMoveC2SPacket) event.getPacket();
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(p.getX(0), p.getY(0), p.getZ(0), p.isOnGround()));
				return;
			}
		}
	}

	private boolean shouldPacketFly() {
		return !mc.player.isOnGround()
				&& !mc.options.keySneak.isPressed()
				&& mc.player.inventory.getArmorStack(2).getItem() == Items.ELYTRA;
	}
}

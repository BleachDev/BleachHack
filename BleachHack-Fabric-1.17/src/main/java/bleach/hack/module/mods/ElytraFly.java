/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import org.apache.commons.lang3.RandomUtils;

import bleach.hack.eventbus.BleachSubscribe;

import bleach.hack.event.events.EventClientMove;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSendMovementPackets;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingMode;
import bleach.hack.module.setting.base.SettingSlider;
import bleach.hack.module.Module;
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
		super("ElytraFly", KEY_UNBOUND, ModuleCategory.MOVEMENT, "Improves the elytra.",
				new SettingMode("Mode", "AutoBoost", "Boost", "Control", "BruhFly", "Pak\u00e8tFly").withDesc("Elytrafly mode."),
				new SettingSlider("Boost", 0, 0.15, 0.05, 2).withDesc("Boost speed."),
				new SettingSlider("MaxBoost", 0, 5, 2.5, 1).withDesc("Max boost speed."),
				new SettingSlider("Speed", 0, 5, 0.8, 2).withDesc("Speed for all the other modes."),
				new SettingSlider("Packets", 1, 10, 2, 0).withDesc("How many packets to send in packet mode."));
	}

	@BleachSubscribe
	public void onClientMove(EventClientMove event) {
		/* Cancel the retarded auto elytra movement */
		if (getSetting(0).asMode().mode == 2 && mc.player.isFallFlying()) {
			if (!mc.options.keyJump.isPressed() && !mc.options.keySneak.isPressed()) {
				event.setVec(new Vec3d(event.getVec().x, 0, event.getVec().z));
			}

			if (!mc.options.keyBack.isPressed() && !mc.options.keyLeft.isPressed()
					&& !mc.options.keyRight.isPressed() && !mc.options.keyForward.isPressed()) {
				event.setVec(new Vec3d(0, event.getVec().y, 0));
			}
		}
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		Vec3d vec3d = new Vec3d(0, 0, getSetting(3).asSlider().getValue())
				.rotateY(-(float) Math.toRadians(mc.player.getYaw()));

		double currentVel = Math.abs(mc.player.getVelocity().x) + Math.abs(mc.player.getVelocity().y) + Math.abs(mc.player.getVelocity().z);
		float radianYaw = (float) Math.toRadians(mc.player.getYaw());
		float boost = getSetting(1).asSlider().getValueFloat();

		switch (getSetting(0).asMode().mode) {
			case 0:
				if (mc.player.isFallFlying() && currentVel <= getSetting(2).asSlider().getValue()) {
					if (mc.options.keyBack.isPressed()) {
						mc.player.addVelocity(MathHelper.sin(radianYaw) * boost, 0, MathHelper.cos(radianYaw) * -boost);
					} else if (mc.player.getPitch() > 0) {
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
				if (mc.player.isFallFlying()) {
					if (mc.options.keyBack.isPressed()) vec3d = vec3d.negate();
					if (mc.options.keyLeft.isPressed()) vec3d = vec3d.rotateY((float) Math.toRadians(90));
					else if (mc.options.keyRight.isPressed()) vec3d = vec3d.rotateY(-(float) Math.toRadians(90));
					if (mc.options.keyJump.isPressed()) vec3d = vec3d.add(0, getSetting(3).asSlider().getValue(), 0);
					if (mc.options.keySneak.isPressed()) vec3d = vec3d.add(0, -getSetting(3).asSlider().getValue(), 0);

					mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
							mc.player.getX() + vec3d.x, mc.player.getY() - 0.01, mc.player.getZ() + vec3d.z, false));

					mc.player.setVelocity(vec3d.x, vec3d.y, vec3d.z);
				}

				break;
			case 3:
				if (shouldPacketFly()) {
					mc.player.setVelocity(vec3d);
					mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
					mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
							mc.player.getX() + vec3d.x, mc.player.getY() + vec3d.y, mc.player.getZ() + vec3d.z, true));
				}

				break;
			case 4:
				if (shouldPacketFly()) {
					mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
					double randMult = RandomUtils.nextDouble(0.9, 1.1);

					mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
							mc.player.getX() + vec3d.x * randMult,
							mc.player.getY(),
							mc.player.getZ() + vec3d.z * randMult,
							false));

					for (int i = 0; i < 6; i++) {
						mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
								mc.player.getX() + vec3d.x * (randMult + i),
								mc.player.getY() - 0.0001,
								mc.player.getZ() + vec3d.z * (randMult + i),
								true));
					}
				}
		}
	}

	// Packet moment

	@BleachSubscribe
	public void onMovement(EventSendMovementPackets event) {
		if (getSetting(0).asMode().mode == 4 && shouldPacketFly()) {
			mc.player.setVelocity(Vec3d.ZERO);
			event.setCancelled(true);
		}
	}

	@BleachSubscribe
	public void onMovement(EventClientMove event) {
		if (getSetting(0).asMode().mode == 4 && shouldPacketFly()) {
			event.setCancelled(true);
		}
	}

	@BleachSubscribe
	public void onReadPacket(EventReadPacket event) {
		if (getSetting(0).asMode().mode == 4 && shouldPacketFly() && event.getPacket() instanceof PlayerPositionLookS2CPacket) {
			PlayerPositionLookS2CPacket p = (PlayerPositionLookS2CPacket) event.getPacket();

			FabricReflect.writeField(p, mc.player.getYaw(), "field_12391", "yaw");
			FabricReflect.writeField(p, mc.player.getPitch(), "field_12393", "pitch");
		}
	}

	@BleachSubscribe
	public void onSendPacket(EventSendPacket event) {
		if (getSetting(0).asMode().mode == 4 && shouldPacketFly()) {
			if (event.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround) {
				event.setCancelled(true);
				return;
			}

			if (event.getPacket() instanceof PlayerMoveC2SPacket.Full) {
				event.setCancelled(true);
				PlayerMoveC2SPacket p = (PlayerMoveC2SPacket) event.getPacket();
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(p.getX(0), p.getY(0), p.getZ(0), p.isOnGround()));
				return;
			}
		}
	}

	private boolean shouldPacketFly() {
		return !mc.player.isOnGround()
				&& !mc.options.keySneak.isPressed()
				&& mc.player.getInventory().getArmorStack(2).getItem() == Items.ELYTRA;
	}
}

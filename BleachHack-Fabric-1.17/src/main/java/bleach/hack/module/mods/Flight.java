/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import bleach.hack.eventbus.BleachSubscribe;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingMode;
import bleach.hack.module.setting.base.SettingSlider;
import bleach.hack.module.Module;
import bleach.hack.util.FabricReflect;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Flight extends Module {

	private boolean flyTick = false;

	public Flight() {
		super("Flight", KEY_UNBOUND, ModuleCategory.MOVEMENT, "Allows you to fly.",
				new SettingMode("Mode", "Static", "Jetpack", "ec.me").withDesc("Flight mode."),
				new SettingSlider("Speed", 0, 5, 1, 1).withDesc("Flight speed."),
				new SettingMode("AntiKick", "Off", "Fall", "Bob", "Packet").withDesc("How to bypass \"you have been kicked for flying\" kicks."));
	}

	@Override
	public void onDisable(boolean inWorld) {
		if (inWorld)
			mc.player.getAbilities().flying = false;
		
		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		float speed = getSetting(1).asSlider().getValueFloat();

		if (mc.player.age % 20 == 0 && getSetting(2).asMode().mode == 3 && !(getSetting(0).asMode().mode == 1)) {
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 0.069, mc.player.getZ(), false));
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getZ() + 0.069, mc.player.getZ(), true));
		}

		if (getSetting(0).asMode().mode == 0) {
			Vec3d antiKickVel = Vec3d.ZERO;

			if (getSetting(2).asMode().mode == 1
					&& mc.player.age % 20 == 0
					&& mc.world.getBlockState(new BlockPos(new BlockPos(mc.player.getPos().add(0, -0.069, 0)))).getMaterial().isReplaceable()) {
				antiKickVel = antiKickVel.add(0, -0.069, 0);
			} else if (getSetting(2).asMode().mode == 2) {
				if (mc.player.age % 40 == 0) {
					if (mc.world.getBlockState(new BlockPos(new BlockPos(mc.player.getPos().add(0, 0.15, 0)))).getMaterial().isReplaceable()) {
						antiKickVel = antiKickVel.add(0, 0.15, 0);
					}
				} else if (mc.player.age % 20 == 0) {
					if (mc.world.getBlockState(new BlockPos(new BlockPos(mc.player.getPos().add(0, -0.15, 0)))).getMaterial().isReplaceable()) {
						antiKickVel = antiKickVel.add(0, -0.15, 0);
					}
				}
			}

			mc.player.setVelocity(antiKickVel);

			Vec3d forward = new Vec3d(0, 0, speed).rotateY(-(float) Math.toRadians(mc.player.getYaw()));
			Vec3d strafe = forward.rotateY((float) Math.toRadians(90));

			if (mc.options.keyJump.isPressed())
				mc.player.setVelocity(mc.player.getVelocity().add(0, speed, 0));
			if (mc.options.keySneak.isPressed())
				mc.player.setVelocity(mc.player.getVelocity().add(0, -speed, 0));
			if (mc.options.keyBack.isPressed())
				mc.player.setVelocity(mc.player.getVelocity().add(-forward.x, 0, -forward.z));
			if (mc.options.keyForward.isPressed())
				mc.player.setVelocity(mc.player.getVelocity().add(forward.x, 0, forward.z));
			if (mc.options.keyLeft.isPressed())
				mc.player.setVelocity(mc.player.getVelocity().add(strafe.x, 0, strafe.z));
			if (mc.options.keyRight.isPressed())
				mc.player.setVelocity(mc.player.getVelocity().add(-strafe.x, 0, -strafe.z));

		} else if (getSetting(0).asMode().mode == 1) {
			if (!mc.options.keyJump.isPressed())
				return;
			mc.player.setVelocity(mc.player.getVelocity().x, speed / 3, mc.player.getVelocity().z);
		} else if (getSetting(0).asMode().mode == 2) {
			if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.keyJump.getBoundKeyTranslationKey()).getCode())) {
				mc.player.jump();
			} else {
				if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.keyJump.getBoundKeyTranslationKey()).getCode())) {
					mc.player.updatePosition(mc.player.getX(), mc.player.getY() - speed / 10f, mc.player.getZ());
				}
			}
		}
	}

	@BleachSubscribe
	public void onSendPacket(EventSendPacket event) {
		if (getSetting(0).asMode().mode == 2 && event.getPacket() instanceof PlayerMoveC2SPacket) {
			if (!flyTick) {
				boolean onGround = true;// mc.player.fallDistance >= 0.1f;
				mc.player.setOnGround(onGround);
				FabricReflect.writeField(event.getPacket(), onGround, "field_29179", "onGround");

				flyTick = true;
			} else {
				flyTick = false;
			}
		}
	}
}

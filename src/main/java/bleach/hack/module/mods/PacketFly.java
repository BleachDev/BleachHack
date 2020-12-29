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

import org.lwjgl.glfw.GLFW;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventMovementTick;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class PacketFly extends Module {

	private double posX;
	private double posY;
	private double posZ;
	private int timer = 0;

	public PacketFly() {
		super("PacketFly", GLFW.GLFW_KEY_H, Category.MOVEMENT, "Allows you to fly with packets.",
				new SettingMode("Mode", "Phase", "Packet"),
				new SettingSlider("HSpeed", 0.05, 2, 0.5, 2),
				new SettingSlider("VSpeed", 0.05, 2, 0.5, 2),
				new SettingSlider("Fall", 0, 40, 20, 0),
				new SettingToggle("Packet Cancel", false));
	}

	@Override
	public void onEnable() {
		super.onEnable();
		posX = mc.player.getX();
		posY = mc.player.getY();
		posZ = mc.player.getZ();
	}

	@Subscribe
	public void onMovement(EventMovementTick event) {
		mc.player.setVelocity(0, 0, 0);
		event.setCancelled(true);
	}

	@Subscribe
	public void readPacket(EventReadPacket event) {
		if (mc.world == null || mc.player == null)
			return;
		if (event.getPacket() instanceof PlayerPositionLookS2CPacket && getSetting(4).asToggle().state) {
			event.setCancelled(true);
		}
	}

	@Subscribe
	public void onTick(EventTick event) {
		double hspeed = getSetting(1).asSlider().getValue();
		double vspeed = getSetting(2).asSlider().getValue();

		if (!mc.player.isAlive())
			return;
		timer++;

		Entity target = mc.player.getVehicle() == null ? mc.player : mc.player.getVehicle();
		if (getSetting(0).asMode().mode == 0) {
			if (mc.options.keyJump.isPressed())
				posY += vspeed;
			if (mc.options.keySneak.isPressed())
				posY -= vspeed;

			Vec3d forward = new Vec3d(0, 0, hspeed).rotateY(-(float) Math.toRadians(mc.player.yaw));
			Vec3d strafe = forward.rotateY((float) Math.toRadians(90));
			if (mc.options.keyForward.isPressed()) {
				posX += forward.x;
				posZ += forward.z;
			}
			if (mc.options.keyBack.isPressed()) {
				posX -= forward.x;
				posZ -= forward.z;
			}
			if (mc.options.keyLeft.isPressed()) {
				posX += strafe.x;
				posZ += strafe.z;
			}
			if (mc.options.keyRight.isPressed()) {
				posX -= strafe.x;
				posZ -= strafe.z;
			}

			if (timer > getSetting(3).asSlider().getValue()) {
				posY -= 0.2;
				timer = 0;
			}

			target.noClip = true;
			target.updatePositionAndAngles(posX, posY, posZ, mc.player.yaw, mc.player.pitch);
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(posX, posY, posZ, false));
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(posX, posY - 0.01, posZ, true));
			mc.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(timer));

		} else if (getSetting(0).asMode().mode == 1) {
			double mX = 0;
			double mY = 0;
			double mZ = 0;
			if (mc.player.headYaw != mc.player.yaw) {
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(
						mc.player.headYaw, mc.player.pitch, mc.player.isOnGround()));
				return;
			}

			if (mc.options.keyJump.isPressed())
				mY = 0.062;
			if (mc.options.keySneak.isPressed())
				mY = -0.062;

			if (mc.options.keyForward.isPressed()) {
				if (mc.player.getMovementDirection().equals(Direction.NORTH))
					mZ = -0.275;
				if (mc.player.getMovementDirection().equals(Direction.EAST))
					mX = 0.275;
				if (mc.player.getMovementDirection().equals(Direction.SOUTH))
					mZ = 0.275;
				if (mc.player.getMovementDirection().equals(Direction.WEST))
					mX = -0.275;
			}

			if (timer > getSetting(3).asSlider().getValue()) {
				mX = 0;
				mZ = 0;
				mY = -0.062;
				timer = 0;
			}

			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(
					mc.player.getX() + mX, mc.player.getY() + mY, mc.player.getZ() + mZ, false));
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(
					mc.player.getX() + mX, mc.player.getY() - 420.69, mc.player.getZ() + mZ, true));

		}
	}

}

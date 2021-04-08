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

import org.lwjgl.glfw.GLFW;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventSendMovementPackets;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventClientMove;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.FabricReflect;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

public class PacketFly extends Module {

	private double posX;
	private double posY;
	private double posZ;
	private int timer = 0;

	public PacketFly() {
		super("PacketFly", GLFW.GLFW_KEY_H, Category.MOVEMENT, "Allows you to fly with packets.",
				new SettingMode("Mode", "Phase", "Packet").withDesc("Packetfly mode"),
				new SettingSlider("HSpeed", 0.05, 2, 0.5, 2).withDesc("Horizontal speed"),
				new SettingSlider("VSpeed", 0.05, 2, 0.5, 2).withDesc("Vertical speed"),
				new SettingSlider("Fall", 0, 40, 20, 0).withDesc("How often to fall (antikick)"),
				new SettingToggle("Packet Cancel", false).withDesc("Cancel rubberband packets clientside"));
	}

	@Override
	public void onEnable() {
		super.onEnable();
		posX = mc.player.getX();
		posY = mc.player.getY();
		posZ = mc.player.getZ();
	}

	@Subscribe
	public void onMovement(EventSendMovementPackets event) {
		mc.player.setVelocity(Vec3d.ZERO);
		event.setCancelled(true);
	}
	
	@Subscribe
	public void onMovement(EventClientMove event) {
		event.setCancelled(true);
	}

	@Subscribe
	public void onReadPacket(EventReadPacket event) {
		if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
			PlayerPositionLookS2CPacket p = (PlayerPositionLookS2CPacket) event.getPacket();
		
			FabricReflect.writeField(p, mc.player.yaw, "field_12391", "yaw");
			FabricReflect.writeField(p, mc.player.pitch, "field_12393", "pitch");
			
			if (getSetting(4).asToggle().state) {
				event.setCancelled(true);
			}
		}
		
	}
	
	@Subscribe
	public void onSendPacket(EventSendPacket event) {
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
			Vec3d forward = new Vec3d(0, 0, hspeed).rotateY(-(float) Math.toRadians(mc.player.yaw));
			
			if (mc.player.input.jumping) {
				forward = new Vec3d(0, vspeed, 0);
			}
			else if (mc.player.input.jumping) {
				forward = new Vec3d(0, -vspeed, 0);
			}
			else if (mc.player.input.pressingBack) {
				forward = forward.multiply(-1);
			}
			else if (mc.player.input.pressingLeft) {
				forward = forward.rotateY((float) Math.toRadians(90));
			}
			else if (mc.player.input.pressingRight) {
				forward = forward.rotateY((float) -Math.toRadians(90));
			}
			else if (!mc.player.input.pressingForward) {
				forward = Vec3d.ZERO;
			}
			
			//forward = Vec3d.ZERO;
			/*if (mc.player.headYaw != mc.player.yaw) {
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(
						mc.player.headYaw, mc.player.pitch, mc.player.isOnGround()));
				return;
			}*/

			/*if (mc.options.keyJump.isPressed())
				mouseY = 0.062;
			if (mc.options.keySneak.isPressed())
				mouseY = -0.062;*/

			if (timer > getSetting(3).asSlider().getValue()) {
				forward = new Vec3d(0, -vspeed, 0);
				timer = 0;
			}

			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(
					mc.player.getX() + forward.x, mc.player.getY() + forward.y, mc.player.getZ() + forward.z, false));
	
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(
					mc.player.getX() + forward.x, mc.player.getY() - 420.69, mc.player.getZ() + forward.z, true));
		}
	}

}

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

import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.FabricReflect;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EntityControl extends Module {

	public EntityControl() {
		super("EntityControl", GLFW.GLFW_KEY_GRAVE_ACCENT, Category.MOVEMENT, "Manipulate Entities.",
				new SettingToggle("EntitySpeed", true).withDesc("Lets you control the speed of riding entites").withChildren(
						new SettingSlider("Speed", 0, 5, 1.2, 2).withDesc("Entity speed")),
				new SettingToggle("EntityFly", false).withDesc("Lets you fly with entites").withChildren(
						new SettingSlider("Ascend", 0, 2, 0.3, 2).withDesc("Ascend speed"),
						new SettingSlider("Descend", 0, 2, 0.5, 2).withDesc("Descend speed"),
						new SettingToggle("EcmeBypass", false).withDesc("Prevents you from getting kicked off when flying on ec.me")),
				new SettingToggle("HorseJump", true).withDesc("Always makes your horse do the highest jump it can"),
				new SettingToggle("GroundSnap", false).withDesc("Snaps the entity to the ground when going down blocks"),
				new SettingToggle("AntiStuck", false).withDesc("Tries to prevent rubberbanding when going up blocks"),
				new SettingToggle("NoAI", true).withDesc("Disables the entities AI"),
				new SettingToggle("RotationLock", false).withDesc("Locks the rotation of the vehicle to a certain angle serverside").withChildren(
						new SettingSlider("Yaw", -180, 180, 0, 0).withDesc("Yaw of the vehicle"),
						new SettingSlider("Pitch", -90, 90, 0, 0).withDesc("Pitch of the vehicle"),
						new SettingToggle("Player", true).withDesc("Also locks roation for player packets")));
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (mc.player.getVehicle() == null)
			return;

		Entity e = mc.player.getVehicle();
		double speed = getSetting(0).asToggle().getChild(0).asSlider().getValue();

		double forward = mc.player.forwardSpeed;
		double strafe = mc.player.sidewaysSpeed;
		float yaw = mc.player.yaw;

		e.yaw = mc.player.yaw;
		if (e instanceof LlamaEntity) {
			((LlamaEntity) e).headYaw = mc.player.headYaw;
		}

		if (getSetting(5).asToggle().state && forward == 0 && strafe == 0) {
			e.setVelocity(new Vec3d(0, e.getVelocity().y, 0));
		}

		if (getSetting(0).asToggle().state) {
			if (forward != 0.0D) {
				if (strafe > 0.0D) {
					yaw += (forward > 0.0D ? -45 : 45);
				} else if (strafe < 0.0D) {
					yaw += (forward > 0.0D ? 45 : -45);
				}

				if (forward > 0.0D) {
					forward = 1.0D;
				} else if (forward < 0.0D) {
					forward = -1.0D;
				}

				strafe = 0.0D;
			}
			e.setVelocity(forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F)),
					e.getVelocity().y,
					forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F)));

			if (e instanceof MinecartEntity) {
				MinecartEntity em = (MinecartEntity) e;
				em.setVelocity(forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F)),
						em.getVelocity().y,
						forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F)));
			}
		}

		if (getSetting(1).asToggle().state) {
			if (mc.options.keyJump.isPressed()) {
				e.setVelocity(e.getVelocity().x, getSetting(1).asToggle().getChild(0).asSlider().getValue(), e.getVelocity().z);
			} else {
				e.setVelocity(e.getVelocity().x, -getSetting(1).asToggle().getChild(1).asSlider().getValue(), e.getVelocity().z);
			}
		}

		if (getSetting(3).asToggle().state) {
			BlockPos p = new BlockPos(e.getPos());
			if (!mc.world.getBlockState(p.down()).getMaterial().isReplaceable() && e.fallDistance > 0.01) {
				e.setVelocity(e.getVelocity().x, -1, e.getVelocity().z);
			}
		}

		if (getSetting(4).asToggle().state) {
			Vec3d vel = e.getVelocity().multiply(2);
			if (!WorldUtils.isBoxEmpty(e.getBoundingBox().offset(vel.x, 0, vel.z))) {
				for (int i = 2; i < 10; i++) {
					if (WorldUtils.isBoxEmpty(e.getBoundingBox().offset(vel.x / i, 0, vel.z / i))) {
						e.setVelocity(vel.x / i / 2, vel.y, vel.z / i / 2);
						break;
					}
				}
			}
		}
	}

	@Subscribe
	public void onSendPacket(EventSendPacket event) {
		if (getSetting(6).asToggle().state) {
			if (event.getPacket() instanceof VehicleMoveC2SPacket) {
				FabricReflect.writeField(event.getPacket(), (float) getSetting(6).asToggle().getChild(0).asSlider().getValue(), "field_12898", "yaw");
				FabricReflect.writeField(event.getPacket(), (float) getSetting(6).asToggle().getChild(1).asSlider().getValue(), "field_12896", "pitch");
			} else if (event.getPacket() instanceof PlayerMoveC2SPacket
					&& mc.player.hasVehicle()
					&& getSetting(6).asToggle().getChild(2).asToggle().state) {
				FabricReflect.writeField(event.getPacket(), (float) getSetting(6).asToggle().getChild(0).asSlider().getValue(), "field_12887", "yaw");
				FabricReflect.writeField(event.getPacket(), (float) getSetting(6).asToggle().getChild(1).asSlider().getValue(), "field_12885", "pitch");
			}
		}

		if (getSetting(1).asToggle().state && getSetting(1).asToggle().getChild(2).asToggle().state
				&& mc.player != null && mc.player.getVehicle() != null && event.getPacket() instanceof VehicleMoveC2SPacket) {
			mc.interactionManager.interactEntity(mc.player, mc.player.getVehicle(), Hand.MAIN_HAND);
		}
	}

	@Subscribe
	public void onReadPacket(EventReadPacket event) {
		if (getSetting(1).asToggle().state && getSetting(1).asToggle().getChild(2).asToggle().state
				&& mc.player != null && mc.player.hasVehicle()) {
			if (event.getPacket() instanceof PlayerPositionLookS2CPacket
					|| event.getPacket() instanceof EntityPassengersSetS2CPacket)
				event.setCancelled(true);
		}
	}

	// HorseJump handled in MixinClientPlayerEntity.method_3151
}

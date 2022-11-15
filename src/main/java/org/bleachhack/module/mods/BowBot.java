/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import java.util.Comparator;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.world.EntityUtils;
import org.bleachhack.util.world.WorldUtils;

import com.google.common.collect.Streams;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BowBot extends Module {

	public BowBot() {
		super("BowBot", KEY_UNBOUND, ModuleCategory.COMBAT, "Automatically aims and shoots at entities.",
				new SettingToggle("Shoot", true).withDesc("Automatically shoots arrows.").withChildren(
						new SettingSlider("Charge", 0.1, 1, 0.5, 2).withDesc("How much to charge the bow before shooting.")),
				new SettingToggle("Aim", false).withDesc("Automatically aims.").withChildren(
						new SettingToggle("Players", true).withDesc("Aims at players."),
						new SettingToggle("Mobs", false).withDesc("Aims at mobs."),
						new SettingToggle("Animals", false).withDesc("Aims at animals."),
						new SettingToggle("Raycast", true).withDesc("Doesn't aim at entites you can't see.")));
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (!(mc.player.getMainHandStack().getItem() instanceof RangedWeaponItem) || !mc.player.isUsingItem())
			return;

		if (getSetting(0).asToggle().getState()) {
			if (mc.player.getMainHandStack().getItem() == Items.CROSSBOW
					&& (float) mc.player.getItemUseTime() / (float) CrossbowItem.getPullTime(mc.player.getMainHandStack()) >= 1f) {
				mc.player.stopUsingItem();
				mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.UP));
				mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
			} else if (mc.player.getMainHandStack().getItem() == Items.BOW
					&& BowItem.getPullProgress(mc.player.getItemUseTime()) >= getSetting(0).asToggle().getChild(0).asSlider().getValueFloat()) {
				mc.player.stopUsingItem();
				mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.UP));
			}
		}

		// Credit: https://github.com/Wurst-Imperium/Wurst7/blob/master/src/main/java/net/wurstclient/hacks/BowAimbotHack.java
		SettingToggle aimToggle = getSetting(1).asToggle();
		if (aimToggle.getState()) {
			LivingEntity target = Streams.stream(mc.world.getEntities())
					.filter(e -> EntityUtils.isAttackable(e, true)
							&& (!aimToggle.getChild(3).asToggle().getState() || mc.player.canSee(e)))
					.filter(e -> (aimToggle.getChild(0).asToggle().getState() && EntityUtils.isPlayer(e))
							|| (aimToggle.getChild(1).asToggle().getState() && EntityUtils.isMob(e))
							|| (aimToggle.getChild(2).asToggle().getState() && EntityUtils.isAnimal(e)))
					.sorted(Comparator.comparing(mc.player::distanceTo))
					.map(e -> (LivingEntity) e)
					.findFirst().orElse(null);

			if (target == null)
				return;

			// set velocity
			float velocity = (72000 - mc.player.getItemUseTimeLeft()) / 20F;
			velocity = Math.min(1f, (velocity * velocity + velocity * 2) / 3);

			// set position to aim at
			Vec3d newTargetVec = target.getPos().add(target.getVelocity());
			double d = mc.player.getEyePos().distanceTo(target.getBoundingBox().offset(target.getVelocity()).getCenter());
			double x = newTargetVec.x + (newTargetVec.x - target.getX()) * d - mc.player.getX();
			double y = newTargetVec.y + (newTargetVec.y - target.getY()) * d + target.getHeight() * 0.5 - mc.player.getY() - mc.player.getEyeHeight(mc.player.getPose());
			double z = newTargetVec.z + (newTargetVec.z - target.getZ()) * d - mc.player.getZ();

			// set yaw
			mc.player.setYaw((float) Math.toDegrees(Math.atan2(z, x)) - 90);

			// calculate needed pitch
			double hDistance = Math.sqrt(x * x + z * z);
			double hDistanceSq = hDistance * hDistance;
			float g = 0.006F;
			float velocitySq = velocity * velocity;
			float velocityPow4 = velocitySq * velocitySq;
			float neededPitch = (float) -Math.toDegrees(Math.atan((velocitySq - Math
					.sqrt(velocityPow4 - g * (g * hDistanceSq + 2 * y * velocitySq)))
					/ (g * hDistance)));

			// set pitch
			if (Float.isNaN(neededPitch)) {
				WorldUtils.facePos(target.getX(), target.getY() + target.getHeight() / 2, target.getZ());
			} else {
				mc.player.setPitch(neededPitch);
			}
		}
	}
}

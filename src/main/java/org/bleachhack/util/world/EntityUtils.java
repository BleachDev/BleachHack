/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.world;

import org.bleachhack.BleachHack;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;

public class EntityUtils {

	public static boolean isAnimal(Entity e) {
		return e instanceof PassiveEntity
				|| e instanceof AmbientEntity
				|| e instanceof WaterCreatureEntity
				|| e instanceof IronGolemEntity
				|| e instanceof SnowGolemEntity;
	}

	public static boolean isMob(Entity e) {
		return e instanceof Monster;
	}

	public static boolean isPlayer(Entity e) {
		return e instanceof PlayerEntity;
	}

	public static boolean isOtherServerPlayer(Entity e) {
		return e instanceof PlayerEntity
				&& e != MinecraftClient.getInstance().player
				&& !(e instanceof PlayerCopyEntity);
	}

	public static boolean isAttackable(Entity e, boolean ignoreFriends) {
		return (e instanceof LivingEntity || e instanceof ShulkerBulletEntity || e instanceof AbstractFireballEntity)
				&& e.isAlive()
				&& e != MinecraftClient.getInstance().player
				&& !e.isConnectedThroughVehicle(MinecraftClient.getInstance().player)
				&& !(e instanceof PlayerCopyEntity)
				&& (!ignoreFriends || !BleachHack.friendMang.has(e));
	}
}

/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.util.BleachLogger;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AbstractHorseEntity;

public class CmdEntityStats extends Command {

	public CmdEntityStats() {
		super("estats", "Get the stats of your vehicle entity.", "estats", CommandCategory.MISC,
				"entitystats", "horsestats");
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (mc.player.getVehicle() != null) {
			if (mc.player.getVehicle() instanceof AbstractHorseEntity) {
				AbstractHorseEntity h = (AbstractHorseEntity) mc.player.getVehicle();

				BleachLogger.info("Entity Stats:");
				BleachLogger.info("\u00a7cMax Health: \u00a7r" + (int) h.getMaxHealth() + " HP");
				BleachLogger.info("\u00a7cSpeed: \u00a7r" + getSpeed(h) + " m/s");
				BleachLogger.info("\u00a7cJump: \u00a7r" + getJumpHeight(h) + " m");
			} else if (mc.player.getVehicle() instanceof LivingEntity) {
				LivingEntity l = (LivingEntity) mc.player.getVehicle();

				BleachLogger.info("Entity Stats:");
				BleachLogger.info("\u00a7cMax Health: \u00a7r" + (int) l.getMaxHealth() + " HP");
				BleachLogger.info("\u00a7cSpeed: \u00a7r" + getSpeedLiving(l) + " m/s");
			} else {
				BleachLogger.error("Current vehicle doesn't have stats.");
			}
		} else {
			BleachLogger.error("Not riding a living entity.");
		}
	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static double getSpeed(AbstractHorseEntity horse) {
		return round(20 * horse.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED), 2);
	}

	public static double getSpeedLiving(LivingEntity entity) {
		return round(43.17 * entity.getMovementSpeed(), 2);
	}

	public static double getJumpHeight(AbstractHorseEntity horse) {
		return round(-0.1817584952 * Math.pow(horse.getJumpStrength(), 3) + 3.689713992 * Math.pow(horse.getJumpStrength(), 2) + 2.128599134 * horse.getJumpStrength() - 0.343930367, 3);
	}
}

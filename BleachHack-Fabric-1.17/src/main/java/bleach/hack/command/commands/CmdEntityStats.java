/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command.commands;

import java.math.BigDecimal;
import java.math.RoundingMode;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.util.BleachLogger;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.HorseBaseEntity;

public class CmdEntityStats extends Command {

	public CmdEntityStats() {
		super("estats", "Get stats of vehicle entity.", "estats", CommandCategory.MISC,
				"entitystats", "horsestats");
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (mc.player.getVehicle() != null) {
			if (mc.player.getVehicle() instanceof HorseBaseEntity) {
				HorseBaseEntity h = (HorseBaseEntity) mc.player.getVehicle();

				BleachLogger.infoMessage("\n\u00a7bEntity Stats:"
						+ "\n\u00a7cMax Health: \u00a7b" + (int) h.getMaxHealth() + " HP"
						+ "\n\u00a7cSpeed: \u00a7b" + getSpeed(h) + " m/s"
						+ "\n\u00a7cJump: \u00a7b" + getJumpHeight(h) + " m");
			} else if (mc.player.getVehicle() instanceof LivingEntity) {
				LivingEntity l = (LivingEntity) mc.player.getVehicle();

				BleachLogger.infoMessage("\n\u00a76Entity Stats:"
						+ "\n\u00a7cMax Health: \u00a7b" + (int) l.getMaxHealth() + " HP"
						+ "\n\u00a7cSpeed: \u00a7b" + getSpeedLiving(l) + " m/s");
			}
		} else {
			BleachLogger.errorMessage("Not riding a living entity.");
		}
	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static double getSpeed(HorseBaseEntity horse) {
		return round(20 * horse.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED), 2);
	}

	public static double getSpeedLiving(LivingEntity entity) {
		return round(43.17 * entity.getMovementSpeed(), 2);
	}

	public static double getJumpHeight(HorseBaseEntity horse) {
		return round(-0.1817584952 * Math.pow(horse.getJumpStrength(), 3) + 3.689713992 * Math.pow(horse.getJumpStrength(), 2) + 2.128599134 * horse.getJumpStrength() - 0.343930367, 3);
	}
}

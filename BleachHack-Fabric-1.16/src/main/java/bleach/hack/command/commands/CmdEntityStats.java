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
package bleach.hack.command.commands;

import java.math.BigDecimal;
import java.math.RoundingMode;

import bleach.hack.command.Command;
import bleach.hack.utils.BleachLogger;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.HorseBaseEntity;

public class CmdEntityStats extends Command {

	@Override
	public String getAlias() {
		return "estats";
	}

	@Override
	public String getDescription() {
		return "Get stats of vehicle entity.";
	}

	@Override
	public String getSyntax() {
		return "estats";
	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if (mc.player.getVehicle() != null) {
			if (mc.player.getVehicle() instanceof HorseBaseEntity) {
				HorseBaseEntity h = (HorseBaseEntity) mc.player.getVehicle();
				int maxHealth = (int) h.getMaxHealth();
				double speed = round(43.17 * h.getMovementSpeed(), 2);
				double jumpHeight = round(-0.1817584952 * Math.pow(h.getJumpStrength(), 3) + 3.689713992 * Math.pow(h.getJumpStrength(), 2) + 2.128599134 * h.getJumpStrength() - 0.343930367, 4);
				
				BleachLogger.infoMessage(
						"\n\u00a7bEntity Stats:\n\u00a7cMax Health: \u00a7b" + maxHealth + " HP"
						+ "\n\u00a7cSpeed: \u00a7b" + speed + " m/s"
						+ "\n\u00a7cJump: \u00a7b" + jumpHeight + " m");
			} else if (mc.player.getVehicle() instanceof LivingEntity) {
				LivingEntity l = (LivingEntity) mc.player.getVehicle();
				int maxHealth = (int) l.getMaxHealth();
				double speed = round(43.17 * l.getMovementSpeed(), 2);
				BleachLogger.infoMessage("\n\u00a76Entity Stats:\n\u00a7cMax Health: \u00a7b" + maxHealth + " HP"
						+ "\n\u00a7cSpeed: \u00a7b" + speed + " m/s");
			}
		} else {
			BleachLogger.errorMessage("Not riding a living entity.");
		}
	}
}

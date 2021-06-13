/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command.commands;

import java.util.Locale;

import org.apache.commons.lang3.math.NumberUtils;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.command.exception.CmdSyntaxException;

public class CmdClip extends Command {

	public CmdClip() {
		super("clip", "Teleports you a certain amount of blocks horizontally/vertically.", "clip v <distance> | clip h <x distance> <z distance>", CommandCategory.MISC);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length < 2) {
			throw new CmdSyntaxException();
		}
		
		if (args[0].toLowerCase(Locale.ENGLISH).startsWith("v")) {
			if (!NumberUtils.isCreatable(args[1])) {
				throw new CmdSyntaxException("Invalid distance \"" + args[1] + "\"");
			}

			move(0, NumberUtils.createNumber(args[1]).doubleValue(), 0);
		} else if (args.length == 3 && args[0].toLowerCase(Locale.ENGLISH).startsWith("h")) {
			if (!NumberUtils.isCreatable(args[1])) {
				throw new CmdSyntaxException("Invalid x distance \"" + args[1] + "\"");
			}
			
			if (!NumberUtils.isCreatable(args[2])) {
				throw new CmdSyntaxException("Invalid z distance \"" + args[2] + "\"");
			}
			
			move(NumberUtils.createNumber(args[1]).doubleValue(), 0, NumberUtils.createNumber(args[2]).doubleValue());
		} else {
			throw new CmdSyntaxException();
		}
	}
	
	private void move(double xOffset, double yOffset, double zOffset) {
		if (mc.player.hasVehicle()) {
			mc.player.getVehicle().updatePosition(
					mc.player.getVehicle().getX() + xOffset,
					mc.player.getVehicle().getY() + yOffset,
					mc.player.getVehicle().getZ() + zOffset);
		}
		mc.player.updatePosition(mc.player.getX() + xOffset, mc.player.getY() + yOffset, mc.player.getZ() + zOffset);
	}

}

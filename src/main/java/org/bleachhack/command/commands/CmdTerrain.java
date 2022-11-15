/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.command.exception.CmdSyntaxException;
import org.bleachhack.util.BleachLogger;

import net.minecraft.util.math.BlockPos;

public class CmdTerrain extends Command {

	public CmdTerrain() {
		super("terrain", "Export an area to the c++ terrain finder format (don't use at 3am).", "terrain <x1> <y1> <z1> <x2> <y2> <z2>", CommandCategory.MISC,
				"tf");
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length != 6) {
			throw new CmdSyntaxException();
		}

		int[] poses = new int[6];
		for (int i = 0; i < 6; i++) {
			try {
				poses[i] = Integer.parseInt(args[i]);

				if (i >= 3 && poses[i - 3] > poses[i]) {
					int temp = poses[i - 3];
					poses[i - 3] = poses[i];
					poses[i] = temp;
				}
			} catch (Exception e) {
				throw new CmdSyntaxException("Could not parse number at pos " + i + " (\"" + args[i] + "\")");
			}
		}

		StringBuilder builder = new StringBuilder();
		for (int x = poses[0]; x <= poses[3]; x++) {
			for (int z = poses[2]; z <= poses[5]; z++) {
				for (int y = poses[4]; y >= poses[1]; y--) {
					if (mc.world.getBlockState(new BlockPos(x, y, z)).isFullCube(mc.world, new BlockPos(x, y, z))) {
						builder.append(x - poses[0]).append(",").append(y - poses[1]).append(",").append(z - poses[2]).append("\n");
						break;
					}
				}
			}
		}

		mc.keyboard.setClipboard(builder.toString().trim());
		BleachLogger.info("Copied terrain to clipboard!");
	}
}

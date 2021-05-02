/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.command.exception.CmdSyntaxException;
import bleach.hack.util.BleachLogger;
import net.minecraft.world.GameMode;

public class CmdGamemode extends Command {

	public CmdGamemode() {
		super("gamemode", "Sets your clientside gamemode.", "gm <0-3>", CommandCategory.MISC,
				"gm");
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		int gm;

		try {
			gm = Integer.parseInt(args[0]);
		} catch (Exception e) {
			throw new CmdSyntaxException("Unable to parse gamemode.");
		}

		if (gm == 0) {
			//mc.player.setGameMode(GameMode.SURVIVAL);
			mc.interactionManager.setGameMode(GameMode.SURVIVAL);
			BleachLogger.infoMessage("Set gamemode to survival.");
		} else if (gm == 1) {
			//mc.player.setGameMode(GameMode.CREATIVE);
			mc.interactionManager.setGameMode(GameMode.CREATIVE);
			BleachLogger.infoMessage("Set gamemode to creative.");
		} else if (gm == 2) {
			//mc.player.setGameMode(GameMode.ADVENTURE);
			mc.interactionManager.setGameMode(GameMode.ADVENTURE);
			BleachLogger.infoMessage("Set gamemode to adventure.");
		} else if (gm == 3) {
			//mc.player.setGameMode(GameMode.SPECTATOR);
			mc.interactionManager.setGameMode(GameMode.SPECTATOR);
			BleachLogger.infoMessage("Set gamemode to spectator.");
		} else {
			throw new CmdSyntaxException("Unknown Gamemode Number.");
		}
	}

}

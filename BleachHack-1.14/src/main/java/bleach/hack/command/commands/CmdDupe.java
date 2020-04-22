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

import bleach.hack.command.Command;

public class CmdDupe extends Command {

	@Override
	public String getAlias() {
		return "dupe";
	}

	@Override
	public String getDescription() {
		return "Dupes an item on vanilla servers.";
	}

	@Override
	public String getSyntax() {
		return ".dupe";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		mc.player.dropItem(true);
		mc.playerController.attackEntity(mc.player, mc.player);
	}

}

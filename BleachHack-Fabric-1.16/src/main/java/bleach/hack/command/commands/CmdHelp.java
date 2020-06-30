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
import bleach.hack.command.CommandManager;
import bleach.hack.utils.BleachLogger;

public class CmdHelp extends Command {

	@Override
	public String getAlias() {
		return "help";
	}

	@Override
	public String getDescription() {
		return "Displays all the commands";
	}

	@Override
	public String getSyntax() {
		return "help / .help [Command]";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		String cmd = null;
		try { cmd = args[0]; } catch (Exception e) {}
		
		for (Command c: CommandManager.getCommands()) {
			if (!cmd.isEmpty() && !cmd.equalsIgnoreCase(c.getAlias())) continue;
			BleachLogger.noPrefixMessage("\u00a75." + c.getAlias() + " | \u00a76" + c.getDescription() + " | \u00a7e" + c.getSyntax());
		}
	}

}

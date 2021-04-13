/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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
package bleach.hack.command;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonElement;

import bleach.hack.command.commands.*;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.file.BleachFileHelper;

public class CommandManager {

	public static boolean allowNextMsg = false;

	private static List<Command> commands = Arrays.asList(
			new CmdBind(),
			new CmdCI(),
			new CmdCustomChat(),
			new CmdCustomSign(),
			new CmdDupe(),
			new CmdEnchant(),
			new CmdEntityMenu(),
			new CmdEntityStats(),
			new CmdFriends(),
			new CmdGamemode(),
			new CmdGive(),
			new CmdGuiReset(),
			new CmdHelp(),
			new CmdInvPeek(),
			new CmdNBT(),
			new CmdNotebot(),
			new CmdPeek(),
			new CmdPrefix(),
			new CmdRbook(),
			new CmdRename(),
			new CmdRpc(),
			new CmdSay(),
			new CmdSetting(),
			new CmdSkull(),
			new CmdToggle());

	public static List<Command> getCommands() {
		return commands;
	}

	public static void readPrefix() {
		JsonElement prefix = BleachFileHelper.readMiscSetting("prefix");

		if (prefix != null) {
			Command.PREFIX = prefix.getAsString();
		}
	}

	public static void callCommand(String input) {
		String[] split = input.split(" ");
		System.out.println(Arrays.toString(split));

		for (Command c : getCommands()) {
			if (c.getAlias().equalsIgnoreCase(split[0])) {
				try {
					c.onCommand(split[0], ArrayUtils.subarray(split, 1, split.length));
				} catch (Exception e) {
					e.printStackTrace();
					c.printSyntaxError();
				}
				return;
			}
		}

		BleachLogger.errorMessage("Command Not Found, Maybe Try " + Command.PREFIX + "help");
	}
}

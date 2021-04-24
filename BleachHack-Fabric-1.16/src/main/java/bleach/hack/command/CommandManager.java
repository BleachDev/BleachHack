/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonElement;

import bleach.hack.BleachHack;
import bleach.hack.command.commands.*;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.file.BleachFileHelper;

public class CommandManager {

	public static boolean allowNextMsg = false;

	private static List<Command> commands = Arrays.asList(
			new CmdBind(),
			new CmdCI(),
			new CmdClickGui(),
			new CmdCustomChat(),
			new CmdCustomSign(),
			new CmdDupe(),
			new CmdEnchant(),
			new CmdEntityMenu(),
			new CmdEntityStats(),
			new CmdFriends(),
			new CmdGamemode(),
			new CmdGive(),
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
		BleachHack.logger.info("Running command: " + Arrays.toString(split));

		for (Command c : getCommands()) {
			if (c.hasAlias(split[0])) {
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

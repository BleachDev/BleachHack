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
import bleach.hack.command.exception.CmdSyntaxException;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.file.BleachFileHelper;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class CommandManager {

	public static boolean allowNextMsg = false;

	private static List<Command> commands = Arrays.asList(
			new CmdBind(),
			new CmdCI(),
			new CmdClickGui(),
			new CmdClip(),
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
			new CmdSpammer(),
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
				} catch (CmdSyntaxException e) {
					BleachLogger.errorMessage((MutableText) e.getTextMessage());

					MutableText text = new LiteralText("\u00a7b" + Command.PREFIX + c.getAliases()[0] + " - \u00a7f" + c.getDescription());
					Text tooltip = new LiteralText(
							"\u00a72Category: " + c.getCategory()
							+ "\n\u00a7bAliases: \u00a7f" + Command.PREFIX + String.join(" \u00a77/\u00a7f " + Command.PREFIX, c.getAliases())
							+ "\n\u00a7bUsage: \u00a7f" + c.getSyntax()
							+ "\n\u00a7bDesc: \u00a7f" + c.getDescription());

					BleachLogger.infoMessage(
							text.styled(style -> style
									.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
				} catch (Exception e) {
					e.printStackTrace();

					BleachLogger.errorMessage("\u00a7l" + e.getClass().getSimpleName() + ":");
					BleachLogger.errorMessage("\u00a7l" + e.getMessage());

					int i = 0;
					for (StackTraceElement st: e.getStackTrace()) {
						if (i >= 8) break;

						String[] bruh = st.getClassName().split("\\.");
						BleachLogger.errorMessage(bruh[bruh.length - 1] + "." + st.getMethodName() + "():" + st.getLineNumber());
						i++;
					}
				}

				return;
			}
		}

		BleachLogger.errorMessage("Command Not Found, Maybe Try " + Command.PREFIX + "help");
	}
}

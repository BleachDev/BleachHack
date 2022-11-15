/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import java.util.Locale;
import java.util.stream.Stream;

import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.command.CommandManager;
import org.bleachhack.util.BleachLogger;

import net.minecraft.text.HoverEvent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class CmdHelp extends Command {

	public CmdHelp() {
		super("help", "Displays all the commands.", "help | help <command>", CommandCategory.MISC);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		String cmd = args.length == 0 ? "" : args[0];

		if (cmd.isEmpty()) {
			BleachLogger.info("Commands:");
		} else {
			BleachLogger.info("Syntax for " + getPrefix() + cmd.toLowerCase(Locale.ENGLISH) + ":");
		}

		for (Command c : CommandManager.getCommands()) {
			if (!cmd.isEmpty() && Stream.of(c.getAliases()).noneMatch(cmd::equalsIgnoreCase))
				continue;

			MutableText text = Text.literal(getPrefix() + c.getAliases()[0] + ": \u00a7f" + c.getDescription())
					.styled(s -> s.withColor(BleachLogger.INFO_COLOR));

			BleachLogger.noPrefix(
					text.styled(style -> style
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, c.getHelpTooltip()))));
		}
	}

}

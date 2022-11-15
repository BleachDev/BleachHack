/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import org.apache.commons.lang3.StringUtils;
import org.bleachhack.BleachHack;
import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.command.exception.CmdSyntaxException;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.io.BleachFileHelper;

import java.util.Locale;

public class CmdFriends extends Command {

	public CmdFriends() {
		super("friends", "Manage friends.", "friends add <user> | friends remove <user> | friends list | friends clear", CommandCategory.MISC,
				"friend");
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length == 0 || args.length > 2) {
			throw new CmdSyntaxException();
		}

		if (args[0].equalsIgnoreCase("add")) {
			if (args.length < 2) {
				throw new CmdSyntaxException("No username selected");
			}

			BleachHack.friendMang.add(args[1]);
			BleachLogger.info("Added \"" + args[1] + "\" to the friend list");
		} else if (args[0].equalsIgnoreCase("remove")) {
			if (args.length < 2) {
				throw new CmdSyntaxException("No username selected");
			}

			BleachHack.friendMang.remove(args[1].toLowerCase(Locale.ENGLISH));
			BleachLogger.info("Removed \"" + args[1] + "\" from the friend list");
		} else if (args[0].equalsIgnoreCase("list")) {
			if (BleachHack.friendMang.getFriends().isEmpty()) {
				BleachLogger.info("You don't have any friends :(");
			} else {
				int len = BleachHack.friendMang.getFriends().stream()
						.min((f1, f2) -> f2.length() - f1.length())
						.get().length() + 3;

				MutableText text = Text.literal("Friends:");

				for (String f : BleachHack.friendMang.getFriends()) {
					String spaces = StringUtils.repeat(' ', len - f.length());

					text
					.append(Text.literal("\n> " + f + spaces)
							.styled(style -> style
									.withColor(BleachLogger.INFO_COLOR)))
					.append(Text.literal("\u00a7c[Del]")
							.styled(style -> style
									.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Remove " + f + " from your friendlist")))
									.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getPrefix() + "friends remove " + f))))
					.append("   ")
					.append(Text.literal("\u00a73[NameMC]")
							.styled(style -> style
									.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Open NameMC page of " + f)))
									.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://namemc.com/profile/" + f))));
				}

				BleachLogger.info(text);
			}
		} else if (args[0].equalsIgnoreCase("clear")) {
			BleachHack.friendMang.getFriends().clear();

			BleachLogger.info("Cleared Friend list");
		} else {
			throw new CmdSyntaxException();
		}

		BleachFileHelper.SCHEDULE_SAVE_FRIENDS.set(true);
	}

}

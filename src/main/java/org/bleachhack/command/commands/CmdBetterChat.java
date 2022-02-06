/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.command.exception.CmdSyntaxException;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.BetterChat;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.io.BleachFileHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

public class CmdBetterChat extends Command {

	public CmdBetterChat() {
		super("betterchat", "Changes betterchat settings.", "betterchat filter list | betterchat filter [add/remove] <filter> | betterchat [prefix/suffix] current | betterchat [prefix/suffix] reset | betterchat [prefix/suffix] set <text>", CommandCategory.MODULES);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length < 2) {
			throw new CmdSyntaxException();
		}

		BetterChat chat = ModuleManager.getModule(BetterChat.class);

		if (args[0].equalsIgnoreCase("filter")) {
			if (args[1].equalsIgnoreCase("list")) {
				MutableText text = new LiteralText("Filter Entries:");

				int i = 1;
				for (Pattern pat: chat.filterPatterns) {
					text = text.append(new LiteralText("\n\u00a76" + i + " > \u00a7f" + pat.pattern())
							.styled(style -> style
									.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to remove this filter.")))
									.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getPrefix() + "betterchat filter remove " + pat.pattern()))));
					i++;
				}

				BleachLogger.info(text);
			} else if (args[1].equalsIgnoreCase("add")) {
				String arg = String.join(" ", ArrayUtils.subarray(args, 2, args.length)).trim();
				chat.filterPatterns.add(Pattern.compile(arg));

				JsonArray jsonFilter = new JsonArray();
				chat.filterPatterns.forEach(p -> jsonFilter.add(p.toString()));
				BleachFileHelper.saveMiscSetting("betterChatFilter", jsonFilter);

				BleachLogger.info("Added \"" + arg + "\" to the filter patterns.");
			} else if (args[1].equalsIgnoreCase("remove")) {
				String arg = String.join(" ", ArrayUtils.subarray(args, 2, args.length)).trim();
				if (chat.filterPatterns.removeIf(p -> p.toString().equals(arg))) {
					JsonArray jsonFilter = new JsonArray();
					chat.filterPatterns.forEach(p -> jsonFilter.add(p.toString()));
					BleachFileHelper.saveMiscSetting("betterChatFilter", jsonFilter);

					BleachLogger.info("Removed \"" + arg + "\" from the filter patterns.");
				} else {
					BleachLogger.info("Could not find \"" + arg + "\" in the pattern list.");
				}
			} else {
				throw new CmdSyntaxException();
			}
		} else if (args[0].equalsIgnoreCase("prefix")) {
			if (args[1].equalsIgnoreCase("current")) {
				BleachLogger.info("Current prefix: \"" + chat.prefix + "\"");
			} else if (args[1].equalsIgnoreCase("reset")) {
				chat.prefix = "";
				BleachFileHelper.saveMiscSetting("betterChatPrefix", new JsonPrimitive(chat.prefix));
				BleachLogger.info("Reset the customchat prefix!");
			} else if (args[1].equalsIgnoreCase("set") && args.length >= 3) {
				chat.prefix = String.join(" ", ArrayUtils.subarray(args, 2, args.length)).trim() + " ";

				BleachFileHelper.saveMiscSetting("betterChatPrefix", new JsonPrimitive(chat.prefix));
				BleachLogger.info("Set prefix to: \"" + chat.prefix + "\"");
			} else {
				throw new CmdSyntaxException();
			}
		} else if (args[0].equalsIgnoreCase("suffix")) {
			if (args[1].equalsIgnoreCase("current")) {
				BleachLogger.info("Current suffix: \"" + chat.suffix + "\"");
			} else if (args[1].equalsIgnoreCase("reset")) {
				chat.suffix = " \u25ba \u0432\u029f\u0454\u03b1c\u043d\u043d\u03b1c\u043a";
				BleachFileHelper.saveMiscSetting("betterChatSuffix", new JsonPrimitive(chat.suffix));
				BleachLogger.info("Reset the customchat suffix!");
			} else if (args[1].equalsIgnoreCase("set") && args.length >= 3) {
				chat.suffix = String.join(" ", ArrayUtils.subarray(args, 2, args.length)).trim() + " ";

				BleachFileHelper.saveMiscSetting("betterChatSuffix", new JsonPrimitive(chat.suffix));
				BleachLogger.info("Set suffix to: \"" + chat.suffix + "\"");
			} else {
				throw new CmdSyntaxException();
			}
		} else {
			throw new CmdSyntaxException();
		}
	}

}
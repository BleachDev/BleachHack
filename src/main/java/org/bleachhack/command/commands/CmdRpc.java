/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import org.apache.commons.lang3.StringUtils;
import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.command.exception.CmdSyntaxException;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.DiscordRPC;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.io.BleachFileHelper;

import com.google.gson.JsonPrimitive;

public class CmdRpc extends Command {

	public CmdRpc() {
		super("rpc", "Sets custom discord rpc text.", "rpc [top/bottom] <text> | rpc current", CommandCategory.MODULES,
				"discordrpc");
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length == 0) {
			throw new CmdSyntaxException();
		}

		DiscordRPC rpc = (DiscordRPC) ModuleManager.getModule("DiscordRPC");
		String text = StringUtils.join(args, ' ', 1, args.length);
		
		if (args[0].equalsIgnoreCase("top")) {
			rpc.setTopText(text);

			BleachLogger.info("Set top RPC text to \"" + text + "\"");
			BleachFileHelper.saveMiscSetting("discordRPCTopText", new JsonPrimitive(text));
		} else if (args[0].equalsIgnoreCase("bottom")) {
			rpc.setBottomText(text);

			BleachLogger.info("Set bottom RPC text to \"" + text + "\"");
			BleachFileHelper.saveMiscSetting("discordRPCBottomText", new JsonPrimitive(text));
		} else if (args[0].equalsIgnoreCase("current")) {
			BleachLogger.info("Current RPC status:\n" + rpc.getTopText() + "\n" + rpc.getBottomText());
		} else {
			throw new CmdSyntaxException();
		}
	}

}

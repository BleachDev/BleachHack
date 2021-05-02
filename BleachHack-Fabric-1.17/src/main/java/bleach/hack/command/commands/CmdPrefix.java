/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command.commands;

import com.google.gson.JsonPrimitive;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.command.exception.CmdSyntaxException;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.file.BleachFileHelper;

public class CmdPrefix extends Command {

	public CmdPrefix() {
		super("prefix", "Sets the BleachHack command prefix.", "prefix <char>", CommandCategory.MISC);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args[0].isEmpty()) {
			throw new CmdSyntaxException("Prefix Cannot Be Empty");
		}

		PREFIX = args[0];
		BleachFileHelper.saveMiscSetting("prefix", new JsonPrimitive(PREFIX));
		BleachLogger.infoMessage("Set Prefix To: \"" + args[0] + "\"");
	}

}

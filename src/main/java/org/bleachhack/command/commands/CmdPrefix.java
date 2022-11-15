/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.command.exception.CmdSyntaxException;
import org.bleachhack.setting.option.Option;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.io.BleachFileHelper;

public class CmdPrefix extends Command {

	public CmdPrefix() {
		super("prefix", "Sets the BleachHack command prefix.", "prefix <prefix>", CommandCategory.MISC);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args[0].isEmpty()) {
			throw new CmdSyntaxException("Prefix Cannot Be Empty");
		}

		Option.CHAT_COMMAND_PREFIX.setValue(args[0]);
		BleachFileHelper.SCHEDULE_SAVE_OPTIONS.set(true);
		BleachLogger.info("Set Prefix To: \"" + getPrefix() + "\"");
	}

}

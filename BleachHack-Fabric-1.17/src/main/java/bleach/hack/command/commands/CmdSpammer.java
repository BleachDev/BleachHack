/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.file.BleachFileMang;
import net.minecraft.util.Util;

public class CmdSpammer extends Command {

	public CmdSpammer() {
		super("spammer", "Opens the spammer file.", "spammer", CommandCategory.MODULES,
				"editspammer");
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		BleachFileMang.createFile("spammer.txt");
		Util.getOperatingSystem().open(BleachFileMang.stringsToPath("spammer.txt").toUri());

		BleachLogger.infoMessage("Opened spammer file.");
	}

}

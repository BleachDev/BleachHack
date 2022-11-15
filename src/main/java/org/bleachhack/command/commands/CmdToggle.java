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
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.BleachQueue;

public class CmdToggle extends Command {

	public CmdToggle() {
		super("toggle", "Toggles a mod with a command.", "toggle <module>", CommandCategory.MODULES);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length != 1) {
			throw new CmdSyntaxException();
		}

		for (Module m : ModuleManager.getModules()) {
			if (args[0].equalsIgnoreCase(m.getName())) {
				BleachQueue.add(m::toggle);
				BleachLogger.info(m.getName() + " Toggled");
				return;
			}
		}

		BleachLogger.error("Module \"" + args[0] + "\" Not Found!");
	}

}

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
import bleach.hack.command.exception.CmdSyntaxException;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.BleachQueue;

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
				BleachQueue.add(() -> m.toggle());
				BleachLogger.infoMessage(m.getName() + " Toggled");
				return;
			}
		}

		BleachLogger.errorMessage("Module \"" + args[0] + "\" Not Found!");
	}

}

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
import org.bleachhack.setting.module.ModuleSetting;
import org.bleachhack.util.BleachLogger;

public class CmdSetting extends Command {

	public CmdSetting() {
		super("setting", "Changes a setting in a module.", "setting <module> <setting name> <value>", CommandCategory.MODULES);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length < 2) {
			throw new CmdSyntaxException();
		}

		Module module = ModuleManager.getModule(args[0]);
		ModuleSetting<?> setting = module.getSettings().stream().filter(s -> s.getName().equals(args[1])).findFirst().get();
		
		if (setting == null)
			throw new CmdSyntaxException("Invalid setting \"" + args[1] + "\"");

		Object value = setting.getValue();
		if (value instanceof Double) {
			((ModuleSetting<Double>) setting).setValue(Double.parseDouble(args[2]));
		} else if (value instanceof Integer) {
			((ModuleSetting<Integer>) setting).setValue(Integer.parseInt(args[2]));
		} else if (value instanceof String) {
			((ModuleSetting<String>) setting).setValue(args[2]);
		} else {
			BleachLogger.error("Setting \"" + setting.getClass().getSimpleName() + "\" is not supported!");
			return;
		}

		BleachLogger.info("Set " + args[1] + " in " + module.getName() + " to " + args[2]);
	}

}

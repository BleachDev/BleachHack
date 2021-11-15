/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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
import org.bleachhack.module.setting.base.SettingBase;
import org.bleachhack.module.setting.base.SettingMode;
import org.bleachhack.module.setting.base.SettingSlider;
import org.bleachhack.module.setting.base.SettingToggle;
import org.bleachhack.util.BleachLogger;

public class CmdSetting extends Command {

	public CmdSetting() {
		super("setting", "Changes a setting in a module.", "setting <module> <setting number (starts at 0)> <value>", CommandCategory.MODULES);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length < 2) {
			throw new CmdSyntaxException();
		}

		Module m = ModuleManager.getModule(args[0]);
		SettingBase s = m.getSetting(Integer.parseInt(args[1]));

		if (s instanceof SettingSlider) {
			s.asSlider().setValue(Double.parseDouble(args[2]));
		} else if (s instanceof SettingToggle) {
			s.asToggle().state = Boolean.valueOf(args[2]);
		} else if (s instanceof SettingMode) {
			s.asMode().mode = Integer.parseInt(args[2]);
		} else {
			BleachLogger.error("Invalid Command");
			return;
		}

		BleachLogger.info("Set Setting " + args[1] + " Of " + m.getName() + " To " + args[2]);
	}

}

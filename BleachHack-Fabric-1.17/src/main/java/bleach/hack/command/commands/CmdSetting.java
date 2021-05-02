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
import bleach.hack.setting.base.SettingBase;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.BleachLogger;

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
			BleachLogger.errorMessage("Invalid Command");
			return;
		}

		BleachLogger.infoMessage("Set Setting " + args[1] + " Of " + m.getName() + " To " + args[2]);
	}

}

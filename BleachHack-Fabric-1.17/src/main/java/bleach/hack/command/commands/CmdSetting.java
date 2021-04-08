/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2019 Bleach.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.setting.base.SettingBase;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.BleachLogger;

public class CmdSetting extends Command {

	@Override
	public String getAlias() {
		return "setting";
	}

	@Override
	public String getDescription() {
		return "Changes a setting in a module";
	}

	@Override
	public String getSyntax() {
		return "setting <Module> <Setting number (starts at 0)> <value>";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if (args.length < 2) {
			printSyntaxError();
			return;
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

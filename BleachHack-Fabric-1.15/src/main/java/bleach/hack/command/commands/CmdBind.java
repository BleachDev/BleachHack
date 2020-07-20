/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
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
import bleach.hack.utils.BleachLogger;
import net.minecraft.client.util.InputUtil;

public class CmdBind extends Command {

	@Override
	public String getAlias() {
		return "bind";
	}

	@Override
	public String getDescription() {
		return "Binds a module";
	}

	@Override
	public String getSyntax() {
		return "bind add [Module] [Key] | bind del [Module]";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		for (Module m: ModuleManager.getModules()) {
			if (m.getName().equalsIgnoreCase(args[1])) {
				if (args[0].equalsIgnoreCase("add")) {
					m.setKey(InputUtil.fromName("key.keyboard." + args[2].toLowerCase()).getKeyCode());
					BleachLogger.infoMessage("Bound " + m.getName() + " To " + args[2]);
				} else if (args[0].equalsIgnoreCase("del")) {
					m.setKey(-1);
					BleachLogger.infoMessage("Removed Bind For " + m.getName());
				}
				return;
			}
		}
		BleachLogger.errorMessage("Could Not Find Module \"" + args[1] + "\"");
	}

}

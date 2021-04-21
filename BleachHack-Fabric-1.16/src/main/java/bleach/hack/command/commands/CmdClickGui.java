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

import org.apache.commons.lang3.math.NumberUtils;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.gui.clickgui.window.ClickGuiWindow;
import bleach.hack.gui.window.Window;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.file.BleachFileHelper;

public class CmdClickGui extends Command {

	public CmdClickGui() {
		super("clickgui", "Modify the clickgui windows", "clickgui reset <open/closed> | clickgui length <length>", CommandCategory.MODULES);
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if (args.length != 1 && args.length != 2) {
			printSyntaxError();
			return;
		}

		if (args[0].equalsIgnoreCase("reset")) {
			if (args.length == 1 || args[1].equalsIgnoreCase("open")) {
				int x = 10;

				for (Window m : ClickGui.clickGui.getWindows()) {
					if (m instanceof ClickGuiWindow) {
						((ClickGuiWindow) m).hiding = false;
						m.x1 = x;
						m.y1 = 35;
						x += (int) ModuleManager.getModule("ClickGui").getSetting(0).asSlider().getValue() + 5;
					}
				}
			} else if (args[1].equalsIgnoreCase("closed")) {
				int y = 50;

				for (Window m : ClickGui.clickGui.getWindows()) {
					if (m instanceof ClickGuiWindow) {
						((ClickGuiWindow) m).hiding = true;
						m.x1 = 30;
						m.y1 = y;
						y += 16;
					}
				}
			} else {
				printSyntaxError("Invalid reset mode!");
				return;
			}

			BleachFileHelper.SCHEDULE_SAVE_CLICKGUI = true;
			BleachLogger.infoMessage("Reset the clickgui!");
		} else if (args[0].equalsIgnoreCase("length")) {
			if (!NumberUtils.isCreatable(args[1])) {
				printSyntaxError("Invalid clickgui length: " + args[1]);
				return;
			}

			ModuleManager.getModule("ClickGui").getSetting(0).asSlider().setValue(NumberUtils.createNumber(args[1]).intValue());
			BleachFileHelper.SCHEDULE_SAVE_MODULES = true;

			BleachLogger.infoMessage("Set the clickgui length to: " + args[1]);
		} else {
			printSyntaxError();
		}
	}

}

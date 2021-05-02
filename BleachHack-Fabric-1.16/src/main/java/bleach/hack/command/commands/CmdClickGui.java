/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command.commands;

import org.apache.commons.lang3.math.NumberUtils;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.command.exception.CmdSyntaxException;
import bleach.hack.gui.clickgui.window.ClickGuiWindow;
import bleach.hack.gui.window.Window;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.file.BleachFileHelper;

public class CmdClickGui extends Command {

	public CmdClickGui() {
		super("clickgui", "Modify the clickgui windows.", "clickgui reset <open/closed> | clickgui length <length>", CommandCategory.MODULES);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length != 1 && args.length != 2) {
			throw new CmdSyntaxException();
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
				throw new CmdSyntaxException("Invalid reset mode!");
			}

			BleachFileHelper.SCHEDULE_SAVE_CLICKGUI = true;
			BleachLogger.infoMessage("Reset the clickgui!");
		} else if (args[0].equalsIgnoreCase("length")) {
			if (!NumberUtils.isCreatable(args[1])) {
				throw new CmdSyntaxException("Invalid clickgui length: " + args[1]);
			}

			ModuleManager.getModule("ClickGui").getSetting(0).asSlider().setValue(NumberUtils.createNumber(args[1]).intValue());
			BleachFileHelper.SCHEDULE_SAVE_MODULES = true;

			BleachLogger.infoMessage("Set the clickgui length to: " + args[1]);
		} else {
			throw new CmdSyntaxException();
		}
	}

}

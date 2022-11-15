/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import org.apache.commons.lang3.math.NumberUtils;
import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.command.exception.CmdSyntaxException;
import org.bleachhack.gui.clickgui.ModuleClickGuiScreen;
import org.bleachhack.gui.clickgui.window.ClickGuiWindow;
import org.bleachhack.gui.window.Window;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.ClickGui;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.io.BleachFileHelper;

public class CmdClickGui extends Command {

	public CmdClickGui() {
		super("clickgui", "Modify the clickgui windows.", "clickgui reset [open/closed] | clickgui length <length>", CommandCategory.MODULES);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length != 1 && args.length != 2) {
			throw new CmdSyntaxException();
		}

		if (args[0].equalsIgnoreCase("reset")) {
			if (args.length == 1 || args[1].equalsIgnoreCase("closed")) {
				int y = 50;

				for (Window m : ModuleClickGuiScreen.INSTANCE.getWindows()) {
					if (m instanceof ClickGuiWindow) {
						((ClickGuiWindow) m).hiding = true;
						m.x1 = 30;
						m.y1 = y;
						y += 16;
					}
				}
			} else if (args[1].equalsIgnoreCase("open")) {
				int x = 10;

				for (Window m : ModuleClickGuiScreen.INSTANCE.getWindows()) {
					if (m instanceof ClickGuiWindow) {
						((ClickGuiWindow) m).hiding = false;
						m.x1 = x;
						m.y1 = 35;
						x += ModuleManager.getModule(ClickGui.class).getSetting(0).asSlider().getValueInt() + 5;
					}
				}
			} else {
				throw new CmdSyntaxException("Invalid reset mode!");
			}

			BleachFileHelper.SCHEDULE_SAVE_CLICKGUI.set(true);
			BleachLogger.info("Reset the clickgui!");
		} else if (args[0].equalsIgnoreCase("length")) {
			if (!NumberUtils.isCreatable(args[1])) {
				throw new CmdSyntaxException("Invalid clickgui length: " + args[1]);
			}

			ModuleManager.getModule(ClickGui.class).getSetting(0).asSlider().setValue(NumberUtils.createNumber(args[1]).doubleValue());
			BleachFileHelper.SCHEDULE_SAVE_MODULES.set(true);

			BleachLogger.info("Set the clickgui length to: " + args[1]);
		} else {
			throw new CmdSyntaxException();
		}
	}

}

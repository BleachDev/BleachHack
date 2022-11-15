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
import org.bleachhack.gui.NotebotScreen;
import org.bleachhack.util.BleachQueue;

public class CmdNotebot extends Command {

	public CmdNotebot() {
		super("notebot", "Shows the notebot gui.", "notebot", CommandCategory.MODULES);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		BleachQueue.add(() -> mc.setScreen(new NotebotScreen()));
	}

}

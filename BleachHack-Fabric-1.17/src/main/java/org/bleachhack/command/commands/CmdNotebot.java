/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import java.util.List;

import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.gui.NotebotScreen;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.BleachQueue;
import org.bleachhack.util.NotebotUtils;
import org.bleachhack.util.io.BleachFileMang;

public class CmdNotebot extends Command {

	public CmdNotebot() {
		super("notebot", "Shows the notebot gui.", "notebot | notebot convert <midi file in .minecraft/bleach/>", CommandCategory.MODULES);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length >= 2 && args[0].equalsIgnoreCase("convert")) {
			int i = 0;
			String s = "";
			List<int[]> notes = NotebotUtils.convertMidi(BleachFileMang.getDir().resolve(args[1]));

			while (BleachFileMang.fileExists("notebot/notebot" + i + ".txt"))
				i++;

			for (int[] i1 : notes)
				s += i1[0] + ":" + i1[1] + ":" + i1[2] + "\n";

			BleachFileMang.createEmptyFile("notebot/notebot" + i + ".txt");
			BleachFileMang.appendFile("notebot/notebot" + i + ".txt", s);
			BleachLogger.info("Saved Song As: notebot" + i + ".txt [" + notes.size() + " Notes]");
		} else {
			BleachQueue.add(() -> mc.setScreen(new NotebotScreen()));
		}
	}

}

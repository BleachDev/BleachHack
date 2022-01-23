/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import java.nio.file.Path;
import java.util.stream.Collectors;

import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.gui.NotebotScreen;
import org.bleachhack.module.mods.Notebot.Note;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.BleachQueue;
import org.bleachhack.util.NotebotUtils;
import org.bleachhack.util.io.BleachFileMang;

import com.google.common.collect.Multimap;

public class CmdNotebot extends Command {

	public CmdNotebot() {
		super("notebot", "Shows the notebot gui.", "notebot | notebot convert <midi/nbs file location>", CommandCategory.MODULES);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length >= 2 && args[0].equalsIgnoreCase("convert")) {
			Path path = BleachFileMang.getDir().resolve(args[1]);
			Multimap<Integer, Note> notes = args[1].endsWith(".nbs")
					? NotebotUtils.parseNbs(path) : NotebotUtils.parseMidi(path);

			int i = 0;
			while (BleachFileMang.fileExists("notebot/notebot" + i + ".txt"))
				i++;

			String output = notes.entries().stream()
					.map(e -> e.getKey() + ":" + e.getValue().pitch + ":" + e.getValue().instrument)
					.collect(Collectors.joining("\n"));

			BleachFileMang.createEmptyFile("notebot/notebot" + i + ".txt");
			BleachFileMang.appendFile("notebot/notebot" + i + ".txt", output);
			BleachLogger.info("Saved Song As: notebot" + i + ".txt [" + notes.size() + " Notes]");
		} else {
			BleachQueue.add(() -> mc.setScreen(new NotebotScreen()));
		}
	}

}

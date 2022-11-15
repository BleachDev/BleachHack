/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import java.util.Map.Entry;

import org.bleachhack.event.events.EventSoundPlay;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.module.mods.Notebot.Note;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.io.BleachFileMang;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

public class NotebotStealer extends Module {

	private Multimap<Integer, Note> notes = MultimapBuilder.linkedHashKeys().arrayListValues().build();
	private int ticks = 0;

	public NotebotStealer() {
		super("NotebotStealer", KEY_UNBOUND, ModuleCategory.MISC, "Steals noteblock songs.");
	}

	@Override
	public void onEnable(boolean inWorld) {
		super.onEnable(inWorld);

		notes.clear();
		ticks = 0;
	}

	@Override
	public void onDisable(boolean inWorld) {
		int i = 0;
		StringBuilder s = new StringBuilder();

		while (BleachFileMang.fileExists("notebot/notebot" + i + ".txt"))
			i++;

		for (Entry<Integer, Note> note : notes.entries())
			s.append(note.getKey()).append(":").append(note.getValue().pitch).append(":").append(note.getValue().instrument).append("\n");

		BleachFileMang.createEmptyFile("notebot/notebot" + i + ".txt");
		BleachFileMang.appendFile("notebot/notebot" + i + ".txt", s.toString());
		BleachLogger.info("Saved Song As: notebot" + i + ".txt [" + notes.size() + " Notes]");

		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (!notes.isEmpty())
			ticks++;
	}

	@BleachSubscribe
	public void onSoundPlay(EventSoundPlay.Normal event) {
		if (event.getInstance().getId().getPath().contains("note_block")) {
			int type = 0;
			int note = 0;

			for (int n = 0; n < 25; n++) {
				if ((float) Math.pow(2.0D, (n - 12) / 12.0D) - 0.01 < event.getInstance().getPitch() &&
						(float) Math.pow(2.0D, (n - 12) / 12.0D) + 0.01 > event.getInstance().getPitch()) {
					note = n;
					break;
				}
			}

			if (event.getInstance().getId().getPath().contains("basedrum"))
				type = 1;
			else if (event.getInstance().getId().getPath().contains("snare"))
				type = 2;
			else if (event.getInstance().getId().getPath().contains("hat"))
				type = 3;
			else if (event.getInstance().getId().getPath().contains("bass"))
				type = 4;
			else if (event.getInstance().getId().getPath().contains("flute"))
				type = 5;
			else if (event.getInstance().getId().getPath().contains("bell"))
				type = 6;
			else if (event.getInstance().getId().getPath().contains("guitar"))
				type = 7;
			else if (event.getInstance().getId().getPath().contains("chime"))
				type = 8;
			else if (event.getInstance().getId().getPath().contains("xylophone"))
				type = 9;
			else if (event.getInstance().getId().getPath().contains("iron_xylophone"))
				type = 10;
			else if (event.getInstance().getId().getPath().contains("cow_bell"))
				type = 11;
			else if (event.getInstance().getId().getPath().contains("didgeridoo"))
				type = 12;
			else if (event.getInstance().getId().getPath().contains("bit"))
				type = 13;
			else if (event.getInstance().getId().getPath().contains("banjo"))
				type = 14;
			else if (event.getInstance().getId().getPath().contains("pling"))
				type = 15;
			notes.put(ticks, new Note(note, type));
		}
	}

}

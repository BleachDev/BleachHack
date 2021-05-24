/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.io.File;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.Module;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.FabricReflect;
import bleach.hack.util.file.BleachFileMang;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;

public class NotebotStealer extends Module {

	private List<List<Integer>> notes = new ArrayList<>();
	private Multimap<SoundCategory, SoundInstance> prevSoundMap = HashMultimap.create();
	private int ticks = 0;

	public NotebotStealer() {
		super("NotebotStealer", KEY_UNBOUND, ModuleCategory.MISC, "Steals noteblock songs");
	}

	@Override
	public void onEnable() {
		super.onEnable();
		notes.clear();
		prevSoundMap.clear();
		ticks = 0;
	}

	@Override
	public void onDisable() {
		super.onDisable();
		int i = 0;
		String s = "";

		// Figure out how to use BleachFileMang instead
		File theDir = new File("./bleach/notebot");
		if (!theDir.exists()) {
			theDir.mkdirs();
		}

		while (BleachFileMang.fileExists("notebot", "notebot" + i + ".txt"))
			i++;
		for (List<Integer> i1 : notes)
			s += i1.get(0) + ":" + i1.get(1) + ":" + i1.get(2) + "\n";
		BleachFileMang.createFile("notebot", "notebot" + i + ".txt");
		BleachFileMang.appendFile(s, "notebot", "notebot" + i + ".txt");
		BleachLogger.infoMessage("Saved Song As: notebot" + i + ".txt [" + notes.size() + " Notes]");
	}

	@SuppressWarnings("unchecked")
	@Subscribe
	public void onTick(EventTick event) {
		Multimap<SoundCategory, SoundInstance> soundMap = (Multimap<SoundCategory, SoundInstance>) FabricReflect.getFieldValue(
				FabricReflect.getFieldValue(mc.getSoundManager(), "field_5590", "soundSystem"), "field_18951", "sounds");

		for (Entry<SoundCategory, SoundInstance> e : HashMultimap.create(soundMap).entries()) {
			if (prevSoundMap.containsEntry(e.getKey(), e.getValue()))
				soundMap.remove(e.getKey(), e.getValue());
		}

		for (Entry<SoundCategory, SoundInstance> e : soundMap.entries()) {
			if (e.getValue().getId().getPath().contains("note_block")) {
				int type = 0;
				int note = 0;

				for (int n = 0; n < 25; n++) {
					if ((float) Math.pow(2.0D, (n - 12) / 12.0D) - 0.01 < e.getValue().getPitch() &&
							(float) Math.pow(2.0D, (n - 12) / 12.0D) + 0.01 > e.getValue().getPitch()) {
						note = n;
						break;
					}
				}

				if (e.getValue().getId().getPath().contains("basedrum"))
					type = 1;
				else if (e.getValue().getId().getPath().contains("snare"))
					type = 2;
				else if (e.getValue().getId().getPath().contains("hat"))
					type = 3;
				else if (e.getValue().getId().getPath().contains("bass"))
					type = 4;
				else if (e.getValue().getId().getPath().contains("flute"))
					type = 5;
				else if (e.getValue().getId().getPath().contains("bell"))
					type = 6;
				else if (e.getValue().getId().getPath().contains("guitar"))
					type = 7;
				else if (e.getValue().getId().getPath().contains("chime"))
					type = 8;
				else if (e.getValue().getId().getPath().contains("xylophone"))
					type = 9;
				else if (e.getValue().getId().getPath().contains("iron_xylophone"))
					type = 10;
				else if (e.getValue().getId().getPath().contains("cow_bell"))
					type = 11;
				else if (e.getValue().getId().getPath().contains("didgeridoo"))
					type = 12;
				else if (e.getValue().getId().getPath().contains("bit"))
					type = 13;
				else if (e.getValue().getId().getPath().contains("banjo"))
					type = 14;
				else if (e.getValue().getId().getPath().contains("pling"))
					type = 15;
				notes.add(Arrays.asList(ticks + 0, note, type));
			}
		}

		prevSoundMap.clear();
		prevSoundMap.putAll(soundMap);
		if (!notes.isEmpty())
			ticks++;
	}

}

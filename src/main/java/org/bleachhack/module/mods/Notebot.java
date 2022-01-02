/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.Instrument;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.bleachhack.command.Command;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.io.BleachFileMang;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.QuadColor;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class Notebot extends Module {

	/* All the lines of the file [tick:pitch:instrument] */
	private Multimap<Integer, Note> notes = MultimapBuilder.hashKeys().arrayListValues().build();

	/* All unique instruments and pitches [pitch:instrument] */
	private Set<Note> requirements = new HashSet<>();

	/* Map of noteblocks to hit when playing and the pitch of each [blockpos:pitch] */
	private Map<BlockPos, Integer> blockTunes = new HashMap<>();
	private int timer = -10;
	private int tuneDelay = 0;

	public static String filePath = "";

	public Notebot() {
		super("Notebot", KEY_UNBOUND, ModuleCategory.MISC, "Plays those noteblocks nicely.",
				new SettingToggle("Tune", true).withDesc("Tunes the noteblocks before and while playing.").withChildren(
						new SettingMode("Tune", "Normal", "Wait-1", "Wait-2", "Batch-5", "All").withDesc("How to tune the noteblocks.")),
				new SettingToggle("Loop", false).withDesc("Loop the song you're playing."),
				new SettingToggle("NoInstruments", false).withDesc("Ignores instruments."),
				new SettingToggle("AutoPlay", false).withDesc("Auto plays a random song after one is finished."));
	}

	@Override
	public void onEnable(boolean inWorld) {
		if (!inWorld)
			return;

		super.onEnable(inWorld);
		blockTunes.clear();

		if (!mc.interactionManager.getCurrentGameMode().isSurvivalLike()) {
			BleachLogger.error("Not In Survival Mode!");
			setEnabled(false);
			return;
		} else if (filePath.isEmpty()) {
			BleachLogger.error("No Song Loaded!, Use " + Command.getPrefix() + "notebot to select a song.");
			setEnabled(false);
			return;
		} else {
			readFile(filePath);
		}

		timer = -10;

		List<BlockPos> noteblocks = BlockPos.streamOutwards(new BlockPos(mc.player.getEyePos()), 4, 4, 4)
				.filter(this::isNoteblock)
				.map(BlockPos::toImmutable)
				.toList();

		for (Note note : requirements) {
			for (BlockPos pos: noteblocks) {
				if (blockTunes.containsKey(pos))
					continue;

				if (getSetting(2).asToggle().getState()) {
					if (!blockTunes.containsValue(note.pitch)) {
						blockTunes.put(pos, note.pitch); 
						break;
					}
				} else {
					int instrument = getInstrument(pos).ordinal();
					if (note.instrument == instrument
							&& blockTunes.entrySet().stream()
							.filter(e -> e.getValue() == note.pitch)
							.noneMatch(e -> getInstrument(e.getKey()).ordinal() == instrument)) {
						blockTunes.put(pos, note.pitch);
						break;
					}
				}
			}
		}

		int required = getSetting(2).asToggle().getState()
				? (int) requirements.stream().mapToInt(i -> i.instrument).distinct().count() : requirements.size();
		if (required > blockTunes.size()) {
			BleachLogger.warn("Mapping Error: Missing " + (required - blockTunes.size()) + " Noteblocks");
		}
	}

	@BleachSubscribe
	public void onRender(EventWorldRender.Post event) {
		for (Entry<BlockPos, Integer> e : blockTunes.entrySet()) {
			if (getNote(e.getKey()) != e.getValue()) {
				Renderer.drawBoxBoth(e.getKey(), QuadColor.single(1F, 0F, 0F, 0.4F), 2.5f);
			} else {
				Renderer.drawBoxBoth(e.getKey(), QuadColor.single(0F, 1F, 0F, 0.4F), 2.5f);
			}
		}
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		// Tune Noteblocks
		int tuneMode = getSetting(0).asToggle().getChild(0).asMode().getMode();

		if (getSetting(0).asToggle().getState()) {
			for (Entry<BlockPos, Integer> e : blockTunes.entrySet()) {
				int note = getNote(e.getKey());
				if (note == -1)
					continue;

				if (note != e.getValue()) {
					if (tuneMode <= 2) {
						if (tuneMode >= 1) {
							if (mc.player.age % 2 == 0 ||
									(mc.player.age % 3 == 0 && tuneMode == 2))
								return;
						}

						mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
								new BlockHitResult(mc.player.getPos(), Direction.UP, e.getKey(), true));
					} else if (tuneMode >= 3) {
						if (tuneDelay < (tuneMode == 3 ? 3 : 5)) {
							tuneDelay++;
							return;
						}

						int neededNote = e.getValue() < note ? e.getValue() + 25 : e.getValue();
						int reqTunes = Math.min(tuneMode == 3 ? 5 : 25, neededNote - note);
						for (int i = 0; i < reqTunes; i++)
							mc.interactionManager.interactBlock(mc.player, mc.world,
									Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos(), Direction.UP, e.getKey(), true));

						tuneDelay = 0;
					}

					return;
				}
			}
		}

		// Loop
		boolean loop = timer - 10 > notes.keySet().stream().max(Comparator.naturalOrder()).get();

		if (loop) {
			if (getSetting(3).asToggle().getState()) {
				try (Stream<Path> paths = Files.walk(BleachFileMang.getDir().resolve("notebot"))) {
					List<Path> lst = paths.toList();

					filePath = lst.get(ThreadLocalRandom.current().nextInt(lst.size() - 1) + 1).getFileName().toString();
					setEnabled(false);
					setEnabled(true);
					BleachLogger.info("Now Playing: \u00a7a" + filePath);
				} catch (IOException ignored) {
				}
			} else if (getSetting(1).asToggle().getState()) {
				timer = -10;
			}
		}

		// Play Noteblocks
		timer++;

		Collection<Note> curNotes = notes.get(timer);

		if (curNotes.isEmpty())
			return;

		for (Entry<BlockPos, Integer> e : blockTunes.entrySet()) {
			for (Note i : curNotes) {
				if (isNoteblock(e.getKey()) && (i.pitch == (getNote(e.getKey()))
						&& (getSetting(2).asToggle().getState() || i.instrument == getInstrument(e.getKey()).ordinal())))
					playBlock(e.getKey());
			}
		}
	}

	public Instrument getInstrument(BlockPos pos) {
		if (!isNoteblock(pos))
			return Instrument.HARP;

		return mc.world.getBlockState(pos).get(NoteBlock.INSTRUMENT);
	}

	public int getNote(BlockPos pos) {
		if (!isNoteblock(pos))
			return -1;

		return mc.world.getBlockState(pos).get(NoteBlock.NOTE);
	}

	public boolean isNoteblock(BlockPos pos) {
		// Checks if this block is a noteblock and the noteblock can be played
		return mc.world.getBlockState(pos).getBlock() instanceof NoteBlock
				&& mc.world.getBlockState(pos.up()).isAir();
	}

	public void playBlock(BlockPos pos) {
		if (!isNoteblock(pos))
			return;

		mc.interactionManager.attackBlock(pos, Direction.UP);
		mc.player.swingHand(Hand.MAIN_HAND);
	}

	public void readFile(String fileName) {
		requirements.clear();
		notes.clear();

		// Read the file
		BleachFileMang.createFile("notebot/" + fileName);
		List<String> lines = BleachFileMang.readFileLines("notebot/" + fileName).stream()
				.filter(s -> !(s.isEmpty() || s.startsWith("//") || s.startsWith(";")))
				.map(s -> s.replaceAll(" ", ""))
				.toList();

		// Parse notes
		for (String s : lines) {
			String[] s1 = s.split(":");
			try {
				notes.put(Integer.parseInt(s1[0]), new Note(Integer.parseInt(s1[1]), Integer.parseInt(s1[2])));
			} catch (Exception e) {
				BleachLogger.warn("Error Parsing Note: \u00a7o" + s);
			}
		}

		// Get all unique pitches and instruments
		requirements.addAll(notes.values());
	}

	public static class Note {

		public int pitch;
		public int instrument;

		public Note(int pitch, int instrument) {
			this.pitch = pitch;
			this.instrument = instrument;
		}

		@Override
		public int hashCode() {
			return pitch * 31 + instrument;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Note))
				return false;

			Note other = (Note) obj;
			return instrument == other.instrument && pitch == other.pitch;
		}
	}

}

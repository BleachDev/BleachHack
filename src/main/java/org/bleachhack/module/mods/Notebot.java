/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
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
import net.minecraft.util.math.Vec3d;

import org.bleachhack.command.Command;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.NotebotUtils;
import org.bleachhack.util.io.BleachFileMang;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.QuadColor;

import com.google.common.collect.Multimap;

import java.io.File;
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

public class Notebot extends Module {

	/* The loaded song */
	public Song song;

	/* Map of noteblocks and their pitch around the player [blockpos:pitch] */
	private Map<BlockPos, Integer> blockPitches = new HashMap<>();
	private int timer = -10;
	private int tuneDelay = 0;

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
		blockPitches.clear();

		if (!mc.interactionManager.getCurrentGameMode().isSurvivalLike()) {
			BleachLogger.error("Not In Survival Mode!");
			setEnabled(false);
			return;
		} else if (song == null) {
			BleachLogger.error("No Song Loaded!, Use " + Command.getPrefix() + "notebot to select a song.");
			setEnabled(false);
			return;
		}

		timer = -10;

		List<BlockPos> noteblocks = BlockPos.streamOutwards(new BlockPos(mc.player.getEyePos()), 4, 4, 4)
				.filter(this::isNoteblock)
				.map(BlockPos::toImmutable)
				.toList();

		for (Note note : song.requirements) {
			for (BlockPos pos: noteblocks) {
				if (blockPitches.containsKey(pos))
					continue;

				if (getSetting(2).asToggle().getState()) {
					if (!blockPitches.containsValue(note.pitch)) {
						blockPitches.put(pos, note.pitch); 
						break;
					}
				} else {
					int instrument = getInstrument(pos).ordinal();
					if (note.instrument == instrument
							&& blockPitches.entrySet().stream()
							.filter(e -> e.getValue() == note.pitch)
							.noneMatch(e -> getInstrument(e.getKey()).ordinal() == instrument)) {
						blockPitches.put(pos, note.pitch);
						break;
					}
				}
			}
		}

		int required = getSetting(2).asToggle().getState()
				? (int) song.requirements.stream().mapToInt(i -> i.instrument).distinct().count() : song.requirements.size();
		if (required > blockPitches.size()) {
			BleachLogger.warn("Mapping Error: Missing " + (required - blockPitches.size()) + " Noteblocks");
		}
	}

	@BleachSubscribe
	public void onRender(EventWorldRender.Post event) {
		for (Entry<BlockPos, Integer> e : blockPitches.entrySet()) {
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
			for (Entry<BlockPos, Integer> e : blockPitches.entrySet()) {
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

						mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
								new BlockHitResult(Vec3d.ofCenter(e.getKey(), 1), Direction.UP, e.getKey(), true));
					} else if (tuneMode >= 3) {
						if (tuneDelay < (tuneMode == 3 ? 3 : 5)) {
							tuneDelay++;
							return;
						}

						int neededNote = e.getValue() < note ? e.getValue() + 25 : e.getValue();
						int reqTunes = Math.min(tuneMode == 3 ? 5 : 25, neededNote - note);
						for (int i = 0; i < reqTunes; i++)
							mc.interactionManager.interactBlock(mc.player,
									Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(e.getKey(), 1), Direction.UP, e.getKey(), true));

						tuneDelay = 0;
					}

					return;
				}
			}
		}

		// Loop
		if (timer - 10 > song.length) {
			if (getSetting(3).asToggle().getState()) {
				File[] files = BleachFileMang.getDir().resolve("notebot/").toFile().listFiles();
				Path path = files[ThreadLocalRandom.current().nextInt(files.length)].toPath();

				song = NotebotUtils.parse(path);

				setEnabled(false);
				setEnabled(true);
				BleachLogger.info("Now Playing: \u00a7a" + song.filename);
			} else if (getSetting(1).asToggle().getState()) {
				timer = -10;
			}
		}

		// Play Noteblocks
		timer++;

		Collection<Note> curNotes = song.notes.get(timer);

		if (curNotes.isEmpty())
			return;

		for (Entry<BlockPos, Integer> e : blockPitches.entrySet()) {
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

	public static class Song {

		public String filename;
		public String name;
		public String author;
		public String format;

		public Multimap<Integer, Note> notes;
		public Set<Note> requirements = new HashSet<>();
		public int length;

		public Song(String filename, String name, String author, String format, Multimap<Integer, Note> notes) {
			this.filename = filename;
			this.name = name;
			this.author = author;
			this.format = format;
			this.notes = notes;

			notes.values().stream().distinct().forEach(requirements::add);
			length = notes.keySet().stream().max(Comparator.naturalOrder()).orElse(0);
		}
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

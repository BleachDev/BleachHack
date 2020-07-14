/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
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
package bleach.hack.module.mods;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import bleach.hack.event.events.EventWorldRender;
import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.RenderUtils;
import bleach.hack.utils.file.BleachFileMang;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.Instrument;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Notebot extends Module {
	
	/* All the lines of the file [tick:pitch:instrument] */
	private List<List<Integer>> notes = new ArrayList<>();
	
	/* All unique intruments and pitches [pitch:instrument] */
	private List<List<Integer>> tunes = new ArrayList<>();
	
	/* Map of noteblocks to hit when playing and the pitch of each */
	private HashMap<BlockPos, Integer> blockTunes = new HashMap<>();
	private int timer = -10;
	private int tuneDelay = 0;
	
	public static String filePath = "";
	
	public Notebot() {
		super("Notebot", KEY_UNBOUND, Category.MISC, "Plays those noteblocks nicely",
				new SettingToggle("Tune", true),
				new SettingMode("Tune: ", "Normal", "Wait-1", "Wait-2", "Batch-5", "All"),
				new SettingToggle("Loop", false),
				new SettingToggle("NoInstruments", false),
				new SettingToggle("AutoPlay", false));
	}

	@Override
	public void onEnable() {
		super.onEnable();
		blockTunes.clear();
		if (mc.player.abilities.creativeMode) {
			BleachLogger.errorMessage("Not In Survival Mode!");
			setToggled(false);
			return;
		} else if (filePath.isEmpty()) {
			BleachLogger.errorMessage("No File Loaded!, Use .notebot load [File]");
			setToggled(false);
			return;
		} else readFile(filePath);
		timer = -10;

		for (List<Integer> i: tunes) {
			BlockPos found = null;
			loop: for (int x = -4; x <= 4; x++) {
				for (int y = -4; y <= 4; y++) {
					for (int z = -4; z <= 4; z++) {
						BlockPos pos = mc.player.getBlockPos().add(x, y, z);
						if (!isNoteblock(pos)) continue;
						if (getSettings().get(3).toToggle().state) {
							if (blockTunes.get(pos) != null) if (!blockTunes.get(pos).equals(i.get(0))) continue;
							blockTunes.put(pos, i.get(0));
							break loop;
						} else {
							if (i.get(1) != getInstrument(pos).ordinal() || blockTunes.get(pos) != null) continue;
							found = pos;
							if (i.get(0) == getNote(pos)) break loop;
						}
					}
				}
			}
			if (found != null) blockTunes.put(found, i.get(0));
		}
		
		if (tunes.size() > blockTunes.size() && !getSettings().get(3).toToggle().state) {
			BleachLogger.warningMessage("Mapping Error: Missing " + (tunes.size() - blockTunes.size()) + " Noteblocks");
		}
	}

	@Subscribe
	public void onRender(EventWorldRender event) {
		for (Entry<BlockPos, Integer> e: blockTunes.entrySet()) {
			if (getNote(e.getKey()) != e.getValue()) {
				RenderUtils.drawFilledBox(e.getKey(), 1F, 0F, 0F, 0.8F);
			} else {
				RenderUtils.drawFilledBox(e.getKey(), 0F, 1F, 0F, 0.4F);
			}
		}
	}

	@Subscribe
	public void onTick(EventTick event) {
		/* Tune Noteblocks */
		if (getSettings().get(0).toToggle().state) {
			for (Entry<BlockPos, Integer> e: blockTunes.entrySet()) {
				if (getNote(e.getKey()) != e.getValue()) {
					if (getSettings().get(1).toMode().mode <= 2) {
						if (getSettings().get(1).toMode().mode >= 1) {
							if (mc.player.age % 2 == 0 ||
									(mc.player.age % 3 == 0 && getSettings().get(1).toMode().mode == 2)) return;
						}
						mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
								new BlockHitResult(mc.player.getPos(), Direction.UP, e.getKey(), true));
					} else if (getSettings().get(1).toMode().mode >= 3) {
						if (tuneDelay < (getSettings().get(1).toMode().mode == 3 ? 3 : 5)) {
							tuneDelay++;
							return;
						}
						
						int tunes = getNote(e.getKey());
						int reqTunes = 0;
						for (int i = 0; i < (getSettings().get(1).toMode().mode == 3 ? 5 : 25); i++) {
							if (tunes == 25) tunes = 0;
							if (tunes == e.getValue()) break;
							tunes++;
							reqTunes++;
						}
						
						for (int i = 0; i < reqTunes; i++) mc.interactionManager.interactBlock(mc.player, mc.world, 
								Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos(), Direction.UP, e.getKey(), true));
						tuneDelay = 0;
					}
					return;
				}
			}
		}
		
		/* Loop */
		boolean loopityloop = true;
		for (List<Integer> n: notes) if (timer - 10 < n.get(0)) loopityloop = false;
		if (getSettings().get(4).toToggle().state && loopityloop) {
			try {
				List<String> files = new ArrayList<>();
				Stream<Path> paths = Files.walk(BleachFileMang.getDir().resolve("notebot"));
				paths.forEach(p -> files.add(p.getFileName().toString()));
				paths.close();
				filePath = files.get(new Random().nextInt(files.size() - 1) + 1);
				setToggled(false);
				setToggled(true);
				BleachLogger.infoMessage("Now Playing: \u00a7a" + filePath);
			} catch (IOException e) {}
		} else if (getSettings().get(2).toToggle().state && loopityloop) {
			if (loopityloop) timer = -10;
		}
		
		/* Play Noteblocks */
		timer++;
		
		List<List<Integer>> curNotes = new ArrayList<>();
		for (List<Integer> i: notes) if (i.get(0) == timer) curNotes.add(i);
		if (curNotes.isEmpty()) return;
		
		for (Entry<BlockPos, Integer> e: blockTunes.entrySet()) {
			for (List<Integer> i: curNotes) {
				if (isNoteblock(e.getKey()) && (i.get(1) == (getNote(e.getKey()))
						&& (getSettings().get(3).toToggle().state
								|| i.get(2) == (getInstrument(e.getKey()).ordinal())))) playBlock(e.getKey());
			}
		}
	}
	
	public Instrument getInstrument(BlockPos pos) {
		if (!isNoteblock(pos)) return Instrument.HARP;
		
		return mc.world.getBlockState(pos).get(NoteBlock.INSTRUMENT);
	}
	
	public int getNote(BlockPos pos) {
		if (!isNoteblock(pos)) return 0;
		
		return mc.world.getBlockState(pos).get(NoteBlock.NOTE);
	}
	
	public boolean isNoteblock(BlockPos pos) {
		/* Checks if this block is a noteblock and the noteblock can be played */
		if (mc.world.getBlockState(pos).getBlock() instanceof NoteBlock) {
            return mc.world.getBlockState(pos.up()).getBlock() == Blocks.AIR;
		}
		return false;
	}
	
	public void playBlock(BlockPos pos) {
		if (!isNoteblock(pos)) return;
		mc.interactionManager.attackBlock(pos, Direction.UP);
		mc.player.swingHand(Hand.MAIN_HAND);
	}
	
	public void readFile(String fileName) {
		tunes.clear();
		notes.clear();
		
		/* Read the file */
		BleachFileMang.createFile("notebot", fileName);
		List<String> lines = BleachFileMang.readFileLines("notebot", fileName)
				.stream().filter(s -> !(s.isEmpty() || s.startsWith("//") || s.startsWith(";"))).collect(Collectors.toList());
		for (String s: lines) s = s.replaceAll(" ", "");

		/* Parse note info into "memory" */
		for (String s: lines) {
			String[] s1 = s.split(":");
			try { notes.add(Arrays.asList(Integer.parseInt(s1[0]), Integer.parseInt(s1[1]), Integer.parseInt(s1[2])));
			} catch (Exception e) { BleachLogger.warningMessage("Error Parsing Note: \u00a7o" + s); }
		}
		
		/* Get all unique pitches and instruments */
		for (String s: lines) {
			try { 
				List<String> strings = Arrays.asList(s.split(":"));
				if (!tunes.contains(Arrays.asList(Integer.parseInt(strings.get(1)),Integer.parseInt( strings.get(2))))) {
					tunes.add(Arrays.asList(Integer.parseInt(strings.get(1)), Integer.parseInt(strings.get(2))));
				}
			} catch (Exception e) { BleachLogger.warningMessage("Error Trying To Tune: \u00a7o" + s); }
		}
	}

}

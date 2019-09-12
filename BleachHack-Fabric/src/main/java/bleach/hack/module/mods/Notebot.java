package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import bleach.hack.event.events.Event3DRender;
import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingBase;
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

	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(true, "Tune"),
			new SettingMode("Tune: ", "Normal", "Wait-1", "Wait-2", "Batch-5", "All"),
			new SettingToggle(false, "Loop"));
	
	private List<List<Integer>> tunes = new ArrayList<>();
	private HashMap<BlockPos, Integer> blockTunes = new HashMap<>();
	private List<List<Integer>> notes = new ArrayList<>();
	private int timer = -10;
	private int tuneDelay = 0;
	
	public static String filePath = "";
	
	public Notebot() {
		super("Notebot", -1, Category.MISC, "Plays those noteblocks nicely", settings);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		blockTunes.clear();
		if(mc.player.abilities.creativeMode) {
			BleachLogger.errorMessage("Not In Survival Mode!");
			setToggled(false);
			return;
		}else if(filePath.isEmpty()) {
			BleachLogger.errorMessage("No File Loaded!, Use .notebot load [File]");
			setToggled(false);
			return;
		}else readFile(filePath);
		timer = -10;

		for(List<Integer> i: tunes) {
			loop: for(int x = -4; x <= 4; x++) {
				for(int y = -4; y <= 4; y++) {
					for(int z = -4; z <= 4; z++) {
						BlockPos pos = mc.player.getBlockPos().add(x, y, z);
						if(!isNoteblock(pos) || i.get(1) != getInstrument(pos).ordinal()
								|| (i.get(1) == getInstrument(pos).ordinal() && blockTunes.get(pos) != null)) continue;
						blockTunes.put(pos, i.get(0));
						break loop;
					}
				}
			}
		}
		if(tunes.size() > blockTunes.size()) {
			BleachLogger.warningMessage("Mapping Error: Missing " + (tunes.size() - blockTunes.size()) + " Noteblocks");
		}
	}

	@Subscribe
	public void onRender(Event3DRender event) {
		for(Entry<BlockPos, Integer> e: blockTunes.entrySet()) {
			if(getNote(e.getKey()) != e.getValue()) {
				RenderUtils.drawFilledBox(e.getKey(), 1F, 0F, 0F, 0.8F);
			}else {
				RenderUtils.drawFilledBox(e.getKey(), 0F, 1F, 0F, 0.4F);
			}
		}
	}

	@Subscribe
	public void onTick(EventTick event) {
		/* Tune Noteblocks */
		if(getSettings().get(0).toToggle().state) {
			for(Entry<BlockPos, Integer> e: blockTunes.entrySet()) {
				if(getNote(e.getKey()) != e.getValue()) {
					if(getSettings().get(1).toMode().mode <= 2) {
						if(getSettings().get(1).toMode().mode >= 1) {
							if(mc.player.age % 2 == 0 ||
									(mc.player.age % 3 == 0 && getSettings().get(1).toMode().mode == 2)) return;
						}
						mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
								new BlockHitResult(mc.player.getPos(), Direction.UP, e.getKey(), true));
					}else if(getSettings().get(1).toMode().mode >= 3) {
						if(tuneDelay < (getSettings().get(1).toMode().mode == 3 ? 3 : 5)) {
							tuneDelay++;
							return;
						}
						
						int tunes = getNote(e.getKey());
						int reqTunes = 0;
						for(int i = 0; i < (getSettings().get(1).toMode().mode == 3 ? 5 : 25); i++) {
							if(tunes == 25) tunes = 0;
							if(tunes == e.getValue()) break;
							tunes++;
							reqTunes++;
						}
						
						for(int i = 0; i < reqTunes; i++) mc.interactionManager.interactBlock(mc.player, mc.world, 
								Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos(), Direction.UP, e.getKey(), true));
						tuneDelay = 0;
					}
					return;
				}
			}
		}
		
		/* Loop */
		loop: {
			for(List<Integer> n: notes) if(timer < n.get(0) || !getSettings().get(2).toToggle().state) break loop;
			timer = -20;
		}
		
		/* Play Noteblocks */
		timer++;
		
		List<List<Integer>> curNotes = new ArrayList<>();
		for(List<Integer> i: notes) if(i.get(0) == timer) curNotes.add(i);
		if(curNotes.isEmpty()) return;
		
		for(Entry<BlockPos, Integer> e: blockTunes.entrySet()) {
			for(List<Integer> i: curNotes) {
				if(isNoteblock(e.getKey()) && i.get(1) == (getNote(e.getKey()))
						&& i.get(2) == (getInstrument(e.getKey()).ordinal())) playBlock(e.getKey());
			}
		}
	}
	
	
	/* i have literally no idea how to do this, scuff 100 */
	public Instrument getInstrument(BlockPos pos) {
		if(!isNoteblock(pos)) return Instrument.HARP;
		
		return mc.world.getBlockState(pos).get(NoteBlock.INSTRUMENT);
	}
	
	public int getNote(BlockPos pos) {
		if(!isNoteblock(pos)) return 0;
		
		return mc.world.getBlockState(pos).get(NoteBlock.NOTE);
	}
	
	public boolean isNoteblock(BlockPos pos) {
		/* Checks if this block is a noteblock and the noteblock can be played */
		if(mc.world.getBlockState(pos).getBlock() instanceof NoteBlock) {
            return mc.world.getBlockState(pos.up()).getBlock() == Blocks.AIR;
		}
		return false;
	}
	
	public void playBlock(BlockPos pos) {
		if(!isNoteblock(pos)) return;
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
		for(String s: lines) s = s.replaceAll(" ", " ");

		/* Parse note info into "memory" */
		for(String s: lines) {
			String[] s1 = s.split(":");
			try { notes.add(Arrays.asList(Integer.parseInt(s1[0]), Integer.parseInt(s1[1]), Integer.parseInt(s1[2])));
			}catch(Exception e) { BleachLogger.warningMessage("Error Parsing Note: §o" + s); }
		}
		
		/* Generate tuners */
		List<List<String>> neededTunes = new ArrayList<>();
		for(String s: lines) {
			List<String> strings = Arrays.asList(s.split(":"));
			if(!neededTunes.contains(Arrays.asList(strings.get(1), strings.get(2)))) {
				neededTunes.add(Arrays.asList(strings.get(1), strings.get(2)));
			}
		}
		for(List<String> s: neededTunes) {
			try { tunes.add(Arrays.asList(Integer.parseInt(s.get(0)), Integer.parseInt(s.get(1))));
			}catch(Exception e) { BleachLogger.warningMessage("Error Parsing Tuner: §o" + s); }
		}
	}

}

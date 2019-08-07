package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.RenderUtils;
import bleach.hack.utils.file.BleachFileMang;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.Instrument;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Notebot extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(true, "Tune"),
			new SettingMode(new String[] {"Normal", "Wait-1", "Wait-2"}, "Tune: "),
			new SettingToggle(false, "Loop"));
			
	private BleachFileMang fileMang = new BleachFileMang();
	
	private List<List<Integer>> tunes = new ArrayList<>();
	private HashMap<BlockPos, Integer> blockTunes = new HashMap<>();
	private List<List<Integer>> notes = new ArrayList<>();
	private int timer = -10;
	
	public static String filePath = "";
	
	public Notebot() {
		super("Notebot", -1, Category.MISC, "Plays those noteblocks nicely", settings);
	}
	
	public void onEnable() {
		blockTunes.clear();
		if(filePath.isEmpty()) {
			BleachLogger.errorMessage("No File Loaded!, Use .notebot load [File]");
			setToggled(false);
			return;
		}else readFile(filePath);
		timer = -10;
		
		/* I think my brain died while making this work */
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
	
	public void onRender() {
		for(Entry<BlockPos, Integer> e: blockTunes.entrySet()) {
			if(getNote(e.getKey()) != e.getValue()) {
				RenderUtils.drawFilledBox(e.getKey(), 1F, 0F, 0F, 0.8F);
			}else {
				RenderUtils.drawFilledBox(e.getKey(), 0F, 1F, 0F, 0.4F);
			}
		}
	}
	
	public void onUpdate() {
		/* Tune Noteblocks */
		if(getSettings().get(0).toToggle().state) {
			for(Entry<BlockPos, Integer> e: blockTunes.entrySet()) {
				if(getNote(e.getKey()) != e.getValue()) {
					if(getSettings().get(1).toMode().mode >= 1) {
						if(mc.player.age % 2 == 0 ||
								(mc.player.age % 3 == 0 && getSettings().get(1).toMode().mode == 2)) return;
					}
					mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
							new BlockHitResult(mc.player.getPos(), Direction.UP, e.getKey(), true));
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
		
		for(Entry<Property<?>, Comparable<?>> e: mc.world.getBlockState(pos).getEntries().entrySet()) {
			return (Instrument) e.getValue();
		}
		return Instrument.HARP;
	}
	
	public int getNote(BlockPos pos) {
		if(!isNoteblock(pos)) return 0;
		
		int c = 0;
		for(Entry<Property<?>, Comparable<?>> e: mc.world.getBlockState(pos).getEntries().entrySet()) {
			if(c == 1) {
				System.out.println(Integer.parseInt(e.getValue().toString()));
				return Integer.parseInt(e.getValue().toString());
			}
			c++;
		}
		return 0;
	}
	
	public boolean isNoteblock(BlockPos pos) {
		/* Checks if this block is a noteblock and the noteblock can be played */
		if(mc.world.getBlockState(pos).getBlock() instanceof NoteBlock) {
			if(mc.world.getBlockState(pos.up()).getBlock() == Blocks.AIR) return true;
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
		fileMang.createFile("notebot/" + fileName, "");
		List<String> lines = fileMang.readFileLines("notebot/" + fileName)
				.stream().filter(s -> !(s.isEmpty() || s.startsWith("//"))).collect(Collectors.toList());
		for(String s: lines) s = s.replaceAll(" ", " ");
		
		/* Find tuner Info */
		List<String> tunes1 = lines.stream().filter(s -> s.startsWith(";")).collect(Collectors.toList());
		String[] tunes2 = tunes1.isEmpty() ? new String[] {} : tunes1.get(0).replace(";", "").split(":");
		List<List<String>> tunes3 = new ArrayList<>();
		for(String s: tunes2) tunes3.add(Arrays.asList(s.split("-")));
		
		/* Parse tuner info into "memory" */
		for(List<String> s: tunes3) {
			try { tunes.add(Arrays.asList(Integer.parseInt(s.get(0)), Integer.parseInt(s.get(1))));
			}catch(Exception e) { BleachLogger.warningMessage("Error Parsing Tuner: §o" + s); }
		}
		
		/* Parse note info into "memory" */
		lines = lines.stream().filter(s -> !(s.startsWith(";"))).collect(Collectors.toList());
		for(String s: lines) {
			String[] s1 = s.split(":");
			try { notes.add(Arrays.asList(Integer.parseInt(s1[0]), Integer.parseInt(s1[1]), Integer.parseInt(s1[2])));
			}catch(Exception e) { BleachLogger.warningMessage("Error Parsing Note: §o" + s); }
		}
		
		/* Generate tuners if it doesn't exist */
		if(tunes.isEmpty()) {
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

}

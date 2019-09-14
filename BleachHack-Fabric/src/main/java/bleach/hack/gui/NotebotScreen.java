package bleach.hack.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import bleach.hack.module.mods.Notebot;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.file.BleachFileMang;
import net.minecraft.block.enums.Instrument;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.util.SystemUtil;

public class NotebotScreen extends Screen {

	public List<String> files;
	public NotebotEntry entry;
	public String selected = "";
	public int page = 0;
	
	public NotebotScreen() {
		super(new LiteralText("Notebot Gui"));
	}
	
	public void init() {
		files = new ArrayList<>();
		
		try {
			Stream<Path> paths = Files.walk(BleachFileMang.getDir().resolve("notebot"));
			paths.forEach(p -> files.add(p.getFileName().toString()));
			paths.close();
			files.remove(0);
		} catch (IOException e) {}
	}
	
	public void render(int int_1, int int_2, float float_1) {
		renderBackground();
		
		int x = width / 4, y = height / 4, w = width / 2, h = height / 2;
		
		fill(x, y, x + w, y + h, 0xd0505059);
		fill(x + 1, y + 1, x + w - 2, y + 2, 0xff303030);
		fill(x + 1, y + h - 2, x + w - 2, y + h - 1, 0xff303030);
		drawCenteredString(font, "Notebot Gui", x + w / 2, y - 10, 0x909090);
		drawCenteredString(font, "Tutorial..", x + w - 22, y - 10, 0x9090c0);
		
		int pageEntries = 0;
		for(int i = y + 20; i < y + h - 27; i += 10) pageEntries++;
		
		drawCenteredString(font, "<  Page " + (page + 1) + "  >", x + 55, y + 5, 0xc0c0ff);
		
		fill(x + 10, y + h - 13, x + 99, y + h - 3, 0xff353535);
		drawCenteredString(font, "Download Songs..", x + 55, y + h - 12, 0xc0dfdf);
		
		int c = 0, c1 = -1;
		for(String s: files) {
			c1++;
			if(c1 < page * pageEntries) continue;
			if(c1 > (page + 1) * pageEntries) break;
			
			fill(x + 5, y + 15 + c * 10, x + 105, y + 25 + c * 10, 
					Notebot.filePath.equals(s) ? 0xf0408040 : selected.equals(s) ? 0xf0303030 : 0xf0404040);
			if(int_1 > x + 5 && int_1 < x + 105 && int_2 > y + 15 + c * 10 && int_2 < y + 25 + c * 10) {
				fill(x + 5, y + 15 + c * 10, x + 105, y + 25 + c * 10, 0xf0202020);
			}
			if(cutText(s, 105).equals(s)) drawCenteredString(font, s, x + 55, y + 16 + c * 10, -1);
			else drawString(font, cutText(s, 105), x + 5, y + 16 + c * 10, -1);
			c++;
		}
		
		if(entry != null) {
			drawCenteredString(font, entry.fileName, x + w - w / 4, y + 10, 0x800080);
			drawCenteredString(font, entry.length / 20 + "s", x + w - w / 4, y + 20, 0xa000a0);
			drawCenteredString(font, "Notes: ", x + w - w / 4, y + 38, 0x60a060);
			
			int c2 = 0;
			for(Entry<Instrument, Integer> e: entry.notes.entrySet()) {
				itemRenderer.zOffset = 500 - c2 * 20;
				drawCenteredString(font, StringUtils.capitalize(e.getKey().asString()) + " x" + e.getValue(), 
						x + w - w / 4, y + 50 + c2 * 10, 0x70c070);
				GL11.glPushMatrix();
				GuiLighting.enableForItems();
				if(e.getKey() == Instrument.HARP) itemRenderer.renderGuiItem(new ItemStack(Items.DIRT), x + w - w / 4 + 40, y + 46 + c2 * 10);
				if(e.getKey() == Instrument.BASEDRUM) itemRenderer.renderGuiItem(new ItemStack(Items.STONE), x + w - w / 4 + 40, y + 46 + c2 * 10);
				if(e.getKey() == Instrument.SNARE) itemRenderer.renderGuiItem(new ItemStack(Items.SAND), x + w - w / 4 + 40, y + 46 + c2 * 10);
				if(e.getKey() == Instrument.HAT) itemRenderer.renderGuiItem(new ItemStack(Items.GLASS), x + w - w / 4 + 40, y + 46 + c2 * 10);
				if(e.getKey() == Instrument.BASS) itemRenderer.renderGuiItem(new ItemStack(Items.OAK_WOOD), x + w - w / 4 + 40, y + 46 + c2 * 10);
				if(e.getKey() == Instrument.FLUTE) itemRenderer.renderGuiItem(new ItemStack(Items.CLAY), x + w - w / 4 + 40, y + 46 + c2 * 10);
				if(e.getKey() == Instrument.BELL) itemRenderer.renderGuiItem(new ItemStack(Items.GOLD_BLOCK), x + w - w / 4 + 40, y + 46 + c2 * 10);
				if(e.getKey() == Instrument.GUITAR) itemRenderer.renderGuiItem(new ItemStack(Items.WHITE_WOOL), x + w - w / 4 + 40, y + 46 + c2 * 10);
				if(e.getKey() == Instrument.XYLOPHONE) itemRenderer.renderGuiItem(new ItemStack(Items.BONE_BLOCK), x + w - w / 4 + 40, y + 46 + c2 * 10);
				if(e.getKey() == Instrument.IRON_XYLOPHONE) itemRenderer.renderGuiItem(new ItemStack(Items.IRON_BLOCK), x + w - w / 4 + 40, y + 46 + c2 * 10);
				if(e.getKey() == Instrument.COW_BELL) itemRenderer.renderGuiItem(new ItemStack(Items.SOUL_SAND), x + w - w / 4 + 40, y + 46 + c2 * 10);
				if(e.getKey() == Instrument.DIDGERIDOO) itemRenderer.renderGuiItem(new ItemStack(Items.PUMPKIN), x + w - w / 4 + 40, y + 46 + c2 * 10);
				if(e.getKey() == Instrument.BIT) itemRenderer.renderGuiItem(new ItemStack(Items.EMERALD_BLOCK), x + w - w / 4 + 40, y + 46 + c2 * 10);
				if(e.getKey() == Instrument.BANJO) itemRenderer.renderGuiItem(new ItemStack(Items.HAY_BLOCK), x + w - w / 4 + 40, y + 46 + c2 * 10);
				if(e.getKey() == Instrument.PLING) itemRenderer.renderGuiItem(new ItemStack(Items.GLOWSTONE), x + w - w / 4 + 40, y + 46 + c2 * 10);
				c2++;
				GL11.glPopMatrix();
			}
			
			fill(x + w - w / 2 + 10, y + h - 15, x + w - w / 4, y + h - 5, 0xff903030);
			fill(x + w - w / 4 + 5, y + h - 15, x + w - 5, y + h - 5, 0xff308030);
			
			drawCenteredString(font, "Delete", (int)(x + w - w / 2.8), y + h - 14, 0xff0000);
			drawCenteredString(font, "Select", x + w - w / 8, y + h - 14, 0x00ff00);
		}
		
		super.render(int_1, int_2, float_1);
	}
	
	public boolean mouseClicked(double double_1, double double_2, int int_1) {
		int x = width / 4, y = height / 4, w = width / 2, h = height / 2;
		
		if(double_1 > x + 20 && double_1 < x + 35 && double_2 > y + 5 && double_2 < y + 15) if(page > 0) page--;
		if(double_1 > x + 77 && double_1 < x + 92 && double_2 > y + 5 && double_2 < y + 15) page++;
		if(double_1 > x + w - 22 && double_1 < x + w && double_2 > y - 12 && double_2 < y) {
			try { SystemUtil.getOperatingSystem().open(new URI("https://www.youtube.com/watch?v=clT_aNvQedk")); }catch(Exception e) {}
		}
		if(double_1 > x + 10 && double_1 < x + 99 && double_2 > y + h - 13 && double_2 < y + h - 3) {
			try {
				FileUtils.copyURLToFile(
						  new URL("https://github.com/BleachDrinker420/bleachhack-1.14/raw/master/online/notebot/songs.zip"), 
						  BleachFileMang.getDir().resolve("notebot").resolve("songs.zip").toFile());
				ZipFile zip = new ZipFile(BleachFileMang.getDir().resolve("notebot").resolve("songs.zip").toFile());
				Enumeration<? extends ZipEntry> files = zip.entries();
				int count = 0;
				while (files.hasMoreElements()) {
					count++;
					ZipEntry file = files.nextElement();
					File outFile = BleachFileMang.getDir().resolve("notebot").resolve(file.getName()).toFile();
					if (file.isDirectory()) outFile.mkdirs();
				    else {
				        outFile.getParentFile().mkdirs();
				        InputStream in = zip.getInputStream(file);
				        FileOutputStream out = new FileOutputStream(outFile);
				        IOUtils.copy(in, out);
				        IOUtils.closeQuietly(in);
				        out.close();
				    }
				}
				zip.close();
				Files.deleteIfExists(BleachFileMang.getDir().resolve("notebot").resolve("songs.zip"));
	
				BleachLogger.infoMessage("Downloaded " + count + " Songs");
				minecraft.openScreen(this);
			} catch (Exception e) { BleachLogger.warningMessage("Looks like i did an oopsie");}

		}
		
		if(entry != null) {
			if(double_1 > x + w - w / 2 + 10 && double_1 < x + w - w / 4 && double_2 > y + h - 15 && double_2 < y + h - 5) {
				BleachFileMang.deleteFile("notebot", entry.fileName);
				minecraft.openScreen(this);
			}
			if(double_1 > x + w - w / 4 + 5 && double_1 < x + w - 5 && double_2 > y + h - 15 && double_2 < y + h - 5) {
				Notebot.filePath = entry.fileName;
			}
		}
		
		int pageEntries = 0;
		for(int i = y + 20; i < y + h - 27; i += 10) pageEntries++;
		
		int c = 0, c1 = -1;
		for(String s: files) {
			c1++;
			if(c1 < page * pageEntries) continue;
			if(double_1 > x + 5 && double_1 < x + 105 && double_2 > y + 15 + c * 10 && double_2 < y + 25 + c * 10) {
				entry = new NotebotEntry(s);
				selected = s;
			}
			c++;
		}
		
		return super.mouseClicked(double_1, double_2, int_1);
	}
	
	private String cutText(String text, int leng) {
		String text1 = text;
		for(int i = 0; i < text.length(); i++) {
			if(font.getStringWidth(text1) < leng) return text1;
			text1 = text1.replaceAll(".$", "");
		}
		return "";
	}
	
	public class NotebotEntry {
		public String fileName;
		public List<String> lines = new ArrayList<>();
		public HashMap<Instrument, Integer> notes = new HashMap<>();
		public int length;
		
		public NotebotEntry(String file) {
			/* File and lines */
			fileName = file;
			lines = BleachFileMang.readFileLines("notebot", file)
					.stream().filter(s -> !(s.isEmpty() || s.startsWith("//") || s.startsWith(";"))).collect(Collectors.toList());
			
			/* Get length */
			int maxLeng = 0;
			for(String s: lines) {
				try { if(Integer.parseInt(s.split(":")[0]) > maxLeng) maxLeng = Integer.parseInt(s.split(":")[0]); }catch(Exception e) {}
			}
			length = maxLeng;
			
			/* Requirements */
			List<List<Integer>> t = new ArrayList<>();
			List<List<String>> neededTunes = new ArrayList<>();
			
			for(String s: lines) {
				List<String> strings = Arrays.asList(s.split(":"));
				if(!neededTunes.contains(Arrays.asList(strings.get(1), strings.get(2)))) {
					neededTunes.add(Arrays.asList(strings.get(1), strings.get(2)));
				}
			}
			
			for(List<String> s: neededTunes) t.add(Arrays.asList(Integer.parseInt(s.get(0)), Integer.parseInt(s.get(1))));
			
			List<Integer> t1 = Arrays.asList(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
			
			for(List<Integer> i: t) t1.set(i.get(1), t1.get(i.get(1)) + 1);
			for(int i = 0; i < t1.size(); i++) {
				if(t1.get(i) != 0) notes.put(Instrument.values()[i], t1.get(i));
			}
		}
	}
}

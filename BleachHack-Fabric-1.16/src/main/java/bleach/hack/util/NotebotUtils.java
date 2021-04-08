/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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
package bleach.hack.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import bleach.hack.util.file.BleachFileMang;
import net.minecraft.block.enums.Instrument;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class NotebotUtils {

	public static void downloadSongs(boolean log) {
		try {
			FileUtils.copyURLToFile(
					new URL("https://github.com/BleachDrinker420/BH-resources/raw/master/notebot/songs.zip"),
					BleachFileMang.stringsToPath("notebot", "songs.zip").toFile());
			ZipFile zip = new ZipFile(BleachFileMang.stringsToPath("notebot", "songs.zip").toFile());
			Enumeration<? extends ZipEntry> files = zip.entries();
			int count = 0;
			while (files.hasMoreElements()) {
				count++;
				ZipEntry file = files.nextElement();
				File outFile = BleachFileMang.getDir().resolve("notebot").resolve(file.getName()).toFile();
				if (file.isDirectory()) {
					outFile.mkdirs();
				} else {
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

			if (log)
				BleachLogger.infoMessage("Downloaded " + count + " Songs");
		} catch (Exception e) {
			if (log)
				BleachLogger.warningMessage("Error Downloading Songs... " + e);
			e.printStackTrace();
		}
	}

	public static void playNote(List<String> lines, int tick) {
		HashMap<Instrument, Integer> notes = new HashMap<>();

		for (String s : lines) {
			try {
				String[] split = s.split(":");
				if (split[0].equals(tick + ""))
					notes.put(Instrument.values()[Integer.parseInt(split[2])], Integer.parseInt(split[1]));
			} catch (Exception e) {
				System.out.println("oops");
			}
		}

		for (Entry<Instrument, Integer> e : notes.entrySet()) {
			// System.out.println(e.getValue() + " | " + i + " | "); /* this is how debugging works right? */
			play(e.getKey().getSound(), (float) Math.pow(2.0D, (e.getValue() - 12) / 12.0D));
		}
	}

	private static void play(SoundEvent sound, float pitch) {
		if (MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().player != null) {
			MinecraftClient.getInstance().world.playSound(MinecraftClient.getInstance().player,
					MinecraftClient.getInstance().player.getBlockPos(), sound, SoundCategory.RECORDS, 3.0F, pitch);
		} else {
			MinecraftClient.getInstance().getSoundManager().play(
					new PositionedSoundInstance(sound, SoundCategory.RECORDS, 3.0F, pitch, 0F, 0F, 0F));
		}
	}
}

/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bleachhack.util.io.BleachFileMang;
import org.bleachhack.util.io.BleachOnlineMang;

import net.minecraft.block.enums.Instrument;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class NotebotUtils {

	private static final String[] NOTE_NAMES = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
	private static final int[] NOTE_POSES = { 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 };

	public static void downloadSongs(boolean log) {
		try {
			FileUtils.copyURLToFile(
					BleachOnlineMang.getResourceUrl().resolve("notebot/songs.zip").toURL(),
					BleachFileMang.getDir().resolve("notebot/songs.zip").toFile());
			ZipFile zip = new ZipFile(BleachFileMang.getDir().resolve("notebot/songs.zip").toFile());
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
				BleachLogger.info("Downloaded " + count + " Songs");
		} catch (Exception e) {
			if (log)
				BleachLogger.warn("Error Downloading Songs... " + e);
			e.printStackTrace();
		}
	}

	public static void playNote(List<String> lines, int tick) {
		for (String s : lines) {
			try {
				String[] split = s.split(":");
				if (split[0].equals(tick + ""))
					play(Instrument.values()[Integer.parseInt(split[2])].getSound(),
							(float) Math.pow(2.0D, (Integer.parseInt(split[1]) - 12) / 12.0D));
			} catch (Exception e) {
				BleachLogger.logger.error("oops");
			}
		}
	}

	private static void play(SoundEvent sound, float pitch) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.world != null && mc.player != null) {
			mc.world.playSound(mc.player, mc.player.getBlockPos(), sound, SoundCategory.RECORDS, 3.0F, pitch);
		} else {
			mc.getSoundManager().play(new PositionedSoundInstance(sound, SoundCategory.RECORDS, 3.0F, pitch, 0F, 0F, 0F));
		}
	}

	public static List<int[]> convertMidi(Path path) {
		List<int[]> noteList = new ArrayList<>();

		try {
			Sequence seq = MidiSystem.getSequence(path.toFile());

			int res = seq.getResolution();

			BleachLogger.logger.info("Tracks: " + seq.getTracks().length + " | " + seq.getDivisionType());
			int trackCount = 0;
			for (Track track : seq.getTracks()) {
				// Track track = seq.getTracks()[0]

				long time = 0;
				long bpm = 120;
				boolean skipNote = false;
				int instrument = 0;
				for (int i = 0; i < track.size(); i++) {
					MidiEvent event = track.get(i);
					MidiMessage message = event.getMessage();

					int ticksPerSecond = (int) (res * (bpm / 60.0));
					time = (long) ((1000d / ticksPerSecond) * event.getTick());

					long millis = time % 1000;
					long second = (time / 1000) % 60;
					long minute = (time / (1000 * 60)) % 60;

					String out = trackCount + "-" + event.getTick() + " | [" + String.format(Locale.ENGLISH, "%02d:%02d.%d", minute, second, millis) + "]";

					if (message instanceof ShortMessage) {
						ShortMessage msg = (ShortMessage) message;

						if (msg.getCommand() == 0x90 || msg.getCommand() == 0x80) {
							int key = msg.getData1();
							int octave = (key / 12) - 1;
							int note = key % 12;
							String noteName = NOTE_NAMES[note];
							int velocity = msg.getData2();
							out += " Note " + (msg.getCommand() == 0x80 ? "off" : "on") + " > "
									+ noteName + octave + " key=" + key + " velocity: " + velocity;

							if (!skipNote) {
								noteList.add(new int[] { (int) Math.round(time / 50d), NOTE_POSES[note], instrument });
								skipNote = true;
							} else {
								skipNote = false;
							}
						} else if (msg.getCommand() == 0xB0) {
							int control = (msg.getLength() > 1) ? (msg.getMessage()[1] & 0xFF) : -1;
							int value = (msg.getLength() > 2) ? (msg.getMessage()[2] & 0xFF) : -1;
							out += " Control: " + control + " | " + value;
						} else if (msg.getCommand() == 0xB0) {
							out += " Program: " + msg.getData1();
							// if (msg.getData1() <= 20) instrument = 0;
							// else if (msg.getData1() <= 51) instrument = 7;
						} else {
							out += " Command: " + msg.getCommand() + " > " + msg.getData1() + " | " + msg.getData2();
						}
					} else if (message instanceof MetaMessage) {
						MetaMessage msg = (MetaMessage) message;

						byte[] data = msg.getData();
						if (msg.getType() == 0x03) {
							out += " Meta Instrument: " + new String(data);
						} else if (msg.getType() == 0x51) {
							int tempo = (data[0] & 0xff) << 16 | (data[1] & 0xff) << 8 | (data[2] & 0xff);
							bpm = 60_000_000 / tempo;
							out += " Meta Tempo: " + bpm;
						} else {
							out += " Meta 0x" + Integer.toHexString(msg.getType()) + ": (" + msg.getClass().getSimpleName() + ")";
							for (byte b : data)
								out += (b & 0xff) + " | ";
						}

					} else {
						out += " Other message: " + message.getClass();
					}

					if (time < 10000)
						BleachLogger.logger.info(out);
				}

				trackCount++;
			}
			Synthesizer synthesizer = MidiSystem.getSynthesizer();
			synthesizer.open();
			javax.sound.midi.Instrument[] instruments = synthesizer.getDefaultSoundbank().getInstruments();
			for (javax.sound.midi.Instrument i : instruments)
				BleachLogger.logger.info(i);

			synthesizer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return noteList;
	}
}

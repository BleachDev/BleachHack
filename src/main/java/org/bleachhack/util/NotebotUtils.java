/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bleachhack.module.mods.Notebot.Note;
import org.bleachhack.module.mods.Notebot.Song;
import org.bleachhack.util.io.BleachFileMang;
import org.bleachhack.util.io.BleachOnlineMang;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import net.minecraft.block.enums.Instrument;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class NotebotUtils {

	public static final String[] NOTE_NAMES = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
	private static final int[] NOTE_POSES = { 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 };

	public static final EnumMap<Instrument, ItemStack> INSTRUMENT_TO_ITEM = Util.make(new EnumMap<>(Instrument.class), it -> {
		it.put(Instrument.HARP, new ItemStack(Items.DIRT));
		it.put(Instrument.BASEDRUM, new ItemStack(Items.STONE));
		it.put(Instrument.SNARE, new ItemStack(Items.SAND));
		it.put(Instrument.HAT, new ItemStack(Items.GLASS));
		it.put(Instrument.BASS, new ItemStack(Items.OAK_WOOD));
		it.put(Instrument.FLUTE, new ItemStack(Items.CLAY));
		it.put(Instrument.BELL, new ItemStack(Items.GOLD_BLOCK));
		it.put(Instrument.GUITAR, new ItemStack(Items.WHITE_WOOL));
		it.put(Instrument.CHIME, new ItemStack(Items.PACKED_ICE));
		it.put(Instrument.XYLOPHONE, new ItemStack(Items.BONE_BLOCK));
		it.put(Instrument.IRON_XYLOPHONE, new ItemStack(Items.IRON_BLOCK));
		it.put(Instrument.COW_BELL, new ItemStack(Items.SOUL_SAND));
		it.put(Instrument.DIDGERIDOO, new ItemStack(Items.PUMPKIN));
		it.put(Instrument.BIT, new ItemStack(Items.EMERALD_BLOCK));
		it.put(Instrument.BANJO, new ItemStack(Items.HAY_BLOCK));
		it.put(Instrument.PLING, new ItemStack(Items.GLOWSTONE));
	});

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
				Path outFile = BleachFileMang.getDir().resolve("notebot").resolve(file.getName());
				if (file.isDirectory()) {
					outFile.toFile().mkdirs();
				} else {
					outFile.toFile().getParentFile().mkdirs();

					try (InputStream zipStream = zip.getInputStream(file)){
						Files.copy(zipStream, outFile);
					}
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

	public static void playNote(Multimap<Integer, Note> song, int tick) {
		for (Note note: song.get(tick)) {
			play(Instrument.values()[note.instrument].getSound(), (float) Math.pow(2.0D, (note.pitch - 12) / 12.0D));
		}
	}

	private static void play(SoundEvent sound, float pitch) {
		MinecraftClient mc = MinecraftClient.getInstance();
		Vec3d vec = mc.player == null ? Vec3d.ZERO : mc.player.getPos();
		mc.getSoundManager().play(new PositionedSoundInstance(sound, SoundCategory.RECORDS, 3.0F, pitch, Random.create(0L), vec.x, vec.y, vec.z));
	}

	public static Song parse(Path path) {
		String string = path.toString();
		if (string.endsWith(".mid") || string.endsWith(".midi")) {
			return parseMidi(path);
		} else if (string.endsWith(".nbs")) {
			return parseNbs(path);
		} else {
			return parseNl(path);
		}
	}

	public static Song parseNl(Path path) {
		Multimap<Integer, Note> notes = MultimapBuilder.linkedHashKeys().arrayListValues().build();
		String name = FilenameUtils.getBaseName(path.toString());
		String author = "Unknown";

		try {
			for (String s: Files.readAllLines(path)) {
				if (s.startsWith("// Name: ")) {
					name = name.substring(9);
				} else if (s.startsWith("// Author: ")) {
					author = name.substring(11);
				} else if (!s.isEmpty()) {
					try {
						String[] split = s.split(":");
						notes.put(Integer.parseInt(split[0]), new Note(Integer.parseInt(split[1]), Integer.parseInt(split[2])));
					} catch (NumberFormatException | IndexOutOfBoundsException e) {
						BleachLogger.warn("Error trying to parse note: \u00a7o" + s);
					}
				}
			}
		} catch (IOException e) {
			BleachLogger.error("Error reading NL file!");
			e.printStackTrace();
		}

		return new Song(path.getFileName().toString(), name, author, "Notelist", notes);
	}

	public static Song parseMidi(Path path) {
		Multimap<Integer, Note> notes = MultimapBuilder.linkedHashKeys().arrayListValues().build();
		String name = FilenameUtils.getBaseName(path.toString());
		String author = "Unknown";

		try {
			MidiFileFormat midiFormat = MidiSystem.getMidiFileFormat(path.toFile());
			BleachLogger.info(midiFormat.properties().toString());

			Sequence seq = MidiSystem.getSequence(path.toFile());

			int res = seq.getResolution();
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
								notes.put((int) Math.round(time / 50d), new Note(NOTE_POSES[note], instrument));
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

		} catch (Exception e) {
			e.printStackTrace();
		}

		return new Song(path.getFileName().toString(), name, author, "MIDI", notes);
	}

	public static Song parseNbs(Path path) {
		Multimap<Integer, Note> notes = MultimapBuilder.linkedHashKeys().arrayListValues().build();
		String name = FilenameUtils.getBaseName(path.toString());
		String author = "Unknown";
		int version = 0;

		try (InputStream input = Files.newInputStream(path)) {
			// Signature
			version = readShort(input) != 0 ? 0 : input.read();

			// Skipping most of the headers because we don't need them
			input.skip(version >= 3 ? 5 : version >= 1 ? 3 : 2);
			String iname = readString(input);
			String iauthor = readString(input);
			String ioauthor = readString(input);
			if (!iname.isEmpty())
				name = iname;

			if (!ioauthor.isEmpty()) {
				author = ioauthor;
			} else if (!iauthor.isEmpty()) {
				author = iauthor;
			}

			readString(input);

			float tempo = readShort(input) / 100f;

			input.skip(23);
			readString(input);
			if (version >= 4)
				input.skip(4);

			// Notes
			double tick = -1;
			short jump;
			while ((jump = readShort(input)) != 0) {
				tick += jump * (20f / tempo);

				// Iterate through layers
				while (readShort(input) != 0) {
					int instrument = input.read();
					if (instrument == 1) {
						instrument = 4;
					} else if (instrument == 2) {
						instrument = 1;
					} else if (instrument == 3) {
						instrument = 2;
					} else if (instrument == 5) {
						instrument = 7;
					} else if (instrument == 6) {
						instrument = 5;
					} else if (instrument == 7) {
						instrument = 6;
					} else if (instrument > 15) {
						instrument = 0;
					}

					int key = input.read() - 33;
					if (key < 0) {
						BleachLogger.info("Note @" + tick + " Key: " + key + " is below the 2-octave range!");
						key = Math.floorMod(key, 12);
					} else if (key > 25) {
						BleachLogger.info("Note @" + tick + " Key: " + key + " is above the 2-octave range!");
						key = Math.floorMod(key, 12) + 12;
					}

					notes.put((int) Math.round(tick), new Note(key, instrument));

					if (version >= 4)
						input.skip(4);
				}
			}
		} catch (IOException e) {
			BleachLogger.error("Error reading Nbs file!");
			e.printStackTrace();
		}

		return new Song(path.getFileName().toString(), name, author, "NBS v" + version, notes);
	}

	// Reads a little endian short
	private static short readShort(InputStream input) throws IOException {
		return (short) (input.read() & 0xFF | input.read() << 8);
	}

	// Reads a little endian int
	private static int readInt(InputStream input) throws IOException {
		return input.read() | input.read() << 8 | input.read() << 16 | input.read() << 24;
	}

	private static String readString(InputStream input) throws IOException {
		return new String(input.readNBytes(readInt(input)));
	}
}

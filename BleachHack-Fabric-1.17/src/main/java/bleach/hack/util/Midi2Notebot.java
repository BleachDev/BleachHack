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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.midi.Instrument;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;

public class Midi2Notebot {

	public static final String[] NOTE_NAMES = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
	public static final int[] NOTE_POSES = { 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 };

	public static List<List<Integer>> convert(Path path) {
		List<List<Integer>> noteList = new ArrayList<>();

		try {
			Sequence seq = MidiSystem.getSequence(path.toFile());

			int res = seq.getResolution();

			System.out.println("Tracks: " + seq.getTracks().length + " | " + seq.getDivisionType());
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

					String out = trackCount + "-" + event.getTick() + " | [" + String.format("%02d:%02d.%d", minute, second, millis) + "]";

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
								noteList.add(Arrays.asList((int) Math.round(time / 50d), NOTE_POSES[note], instrument));
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
							out += " Meta 0x" + Integer.toHexString(msg.getType()) + ": ";
							for (byte b : data)
								out += (b & 0xff) + " | ";
							out += " (" + msg.getClass() + ")";
						}

					} else {
						out += " Other message: " + message.getClass();
					}

					if (time < 10000)
						/* if (!out.contains("Note")) */ System.out.println(out);
				}

				trackCount++;
			}
			Synthesizer synthesizer = MidiSystem.getSynthesizer();
			synthesizer.open();
			Instrument[] instruments = synthesizer.getDefaultSoundbank().getInstruments();
			for (Instrument i : instruments)
				System.out.println(i);

			synthesizer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return noteList;
	}
}

package org.bleachhack.util.doom.s;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.bleachhack.util.doom.m.Swap;

/**
 * A MUS lump reader that loads directly to a Sequence.
 * 
 * Unlike QMusToMid, does not keep the MIDI version in a temporary file.
 *
 * @author finnw
 *
 */
public class MusReader {

    /** Create a sequence from an InputStream.
     *  This is the counterpart of {@link MidiSystem#getSequence(InputStream)}
     *  for MUS format.
     *
     * @param is MUS data (this method does not try to auto-detect the format.)
     */
    public static Sequence getSequence(InputStream is)
    throws IOException, InvalidMidiDataException {
        DataInputStream dis = new DataInputStream(is);
        dis.skip(6);
        int rus = dis.readUnsignedShort();
        short scoreStart = Swap.SHORT((char) rus);
        dis.skip(scoreStart - 8);
        Sequence sequence = new Sequence(Sequence.SMPTE_30, 14, 1);
        Track track = sequence.getTracks()[0];
        int[] chanVelocity = new int[16];
        Arrays.fill(chanVelocity, 100);
        EventGroup eg;
        long tick = 0;
        while ((eg = nextEventGroup(dis, chanVelocity)) != null) {
            tick = eg.appendTo(track, tick);
        }
        MetaMessage endOfSequence = new MetaMessage();
        endOfSequence.setMessage(47, new byte[] {0}, 1);
        track.add(new MidiEvent(endOfSequence, tick));
        return sequence;
    }

    private static EventGroup
    nextEventGroup(InputStream is, int[] channelVelocity) throws IOException {
        EventGroup result = new EventGroup();
        boolean last;
        do {
            int b = is.read();
            if (b < 0) {
                return result.emptyToNull();
            }
            int descriptor = b & 0xff;
            last = (descriptor & 0x80) != 0;
            int eventType = (descriptor >> 4) & 7;
            int chanIndex = descriptor & 15;
            final int midiChan;
            if (chanIndex < 9) {
                midiChan = chanIndex;
            } else if (chanIndex < 15) {
                midiChan = chanIndex + 1;
            } else {
                midiChan = 9;
            }
            switch (eventType) {
            case 0:
                {
                    int note = is.read() & 0xff;
                    if ((note & 0x80) != 0) {
                        throw new IllegalArgumentException("Invalid note byte");
                    }
                    result.noteOff(midiChan, note);
                }
                break;
            case 1:
                {
                    int note = is.read() & 0xff;
                    boolean hasVelocity = (note & 0x80) != 0;
                    final int velocity;
                    if (hasVelocity) {
                        velocity = is.read() & 0xff;
                        if ((velocity & 0x80) != 0) {
                            throw new IllegalArgumentException("Invalid velocity byte");
                        }
                        channelVelocity[midiChan] = velocity;
                    } else {
                        velocity = channelVelocity[midiChan];
                    }
                    result.noteOn(midiChan, note & 0x7f, velocity);
                }
                break;
            case 2:
                {
                    int wheelVal = is.read() & 0xff;
                    result.pitchBend(midiChan, wheelVal);
                }
                break;
            case 3:
                {
                    int sysEvt = is.read() & 0xff;
                    switch (sysEvt) {
                    case 10:
                        result.allSoundsOff(midiChan);
                        break;
                    case 11:
                        result.allNotesOff(midiChan);
                        break;
                    case 14:
                        result.resetAllControllers(midiChan);
                        break;
                    default:
                        String msg = String.format("Invalid system event (%d)", sysEvt);
                        throw new IllegalArgumentException(msg);
                    }
                }
                break;
            case 4:
                int cNum = is.read() & 0xff;
                if ((cNum & 0x80) != 0) {
                    throw new IllegalArgumentException("Invalid controller number ");
                }
                int cVal = is.read() & 0xff;
                if (cNum == 3 && 133 <= cVal && cVal <= 135) {
                    // workaround for some TNT.WAD tracks
                    cVal = 127;
                }
                if ((cVal & 0x80) != 0) {
                    String msg = String.format("Invalid controller value (%d; cNum=%d)", cVal, cNum);
                    throw new IllegalArgumentException(msg);
                }
                switch (cNum) {
                case 0:
                    result.patchChange(midiChan, cVal);
                    break;
                case 1:
                    // Don't forward this to the MIDI device.  Some synths if
                    // in GM level 1 mode will react badly to banks that are
                    // undefined in GM Level 1
                    break;
                case 2:
                    result.vibratoChange(midiChan, cVal);
                    break;
                case 3:
                    result.volume(midiChan, cVal);
                    break;
                case 4:
                    result.pan(midiChan, cVal);
                    break;
                case 5:
                    result.expression(midiChan, cVal);
                    break;
                case 6:
                    result.reverbDepth(midiChan, cVal);
                    break;
                case 7:
                    result.chorusDepth(midiChan, cVal);
                    break;
                case 8:
                    result.sustain(midiChan, cVal);
                    break;
                default:
                    throw new AssertionError("Unknown controller number: " + cNum + "(value: " + cVal + ")");
                }
                break;
            case 6:
                return result.emptyToNull();
            default:
                String msg = String.format("Unknown event type: %d", eventType);
                throw new IllegalArgumentException(msg);
            }
        } while (! last);
        int qTics = readVLV(is);
        result.addDelay(qTics);
        return result;
    }

    private static int readVLV(InputStream is) throws IOException {
        int result = 0;
        boolean last;
        do {
            int digit = is.read() & 0xff;
            last = (digit & 0x80) == 0;
            result <<= 7;
            result |= digit & 127;
        } while (! last);
        return result;
    }

    private static class EventGroup {
        EventGroup() {
            this.messages = new ArrayList<MidiMessage>();
        }
        void addDelay(long ticks) {
            delay += ticks;
        }
        void allNotesOff(int midiChan) {
            addControlChange(midiChan, CHM_ALL_NOTES_OFF, 0);
        }
        void allSoundsOff(int midiChan) {
            addControlChange(midiChan, CHM_ALL_SOUND_OFF, 0);
        }
        long appendTo(Track track, long tick) {
            for (MidiMessage msg: messages) {
                track.add(new MidiEvent(msg, tick));
            }
            return tick + delay * 3;
        }
        void chorusDepth(int midiChan, int depth) {
            addControlChange(midiChan, CTRL_CHORUS_DEPTH, depth);
        }
        EventGroup emptyToNull() {
            if (messages.isEmpty()) {
                return null;
            } else {
                return this;
            }
        }
        void expression(int midiChan, int expr) {
            addControlChange(midiChan, CTRL_EXPRESSION_POT, expr);
        }
        void noteOn(int midiChan, int note, int velocity) {
            addShortMessage(midiChan, ShortMessage.NOTE_ON, note, velocity);
        }
        void noteOff(int midiChan, int note) {
            addShortMessage(midiChan, ShortMessage.NOTE_OFF, note, 0);
        }
        void pan(int midiChan, int pan) {
            addControlChange(midiChan, CTRL_PAN, pan);
        }
        void patchChange(int midiChan, int patchId) {
            addShortMessage(midiChan, ShortMessage.PROGRAM_CHANGE, patchId, 0);
        }
        void pitchBend(int midiChan, int wheelVal) {
            int pb14 = wheelVal * 64;
            addShortMessage(midiChan, ShortMessage.PITCH_BEND, pb14 % 128, pb14 / 128);
        }
        void resetAllControllers(int midiChan) {
            addControlChange(midiChan, CHM_RESET_ALL, 0);
        }
        void reverbDepth(int midiChan, int depth) {
            addControlChange(midiChan, CTRL_REVERB_DEPTH, depth);
        }
        void sustain(int midiChan, int on) {
            addControlChange(midiChan, CTRL_SUSTAIN, on);
        }
        void vibratoChange(int midiChan, int depth) {
            addControlChange(midiChan, CTRL_MODULATION_POT, depth);
        }
        void volume(int midiChan, int vol) {
            addControlChange(midiChan, CTRL_VOLUME, vol);
        }
        private void addControlChange(int midiChan, int ctrlId, int ctrlVal) {
            addShortMessage(midiChan, ShortMessage.CONTROL_CHANGE, ctrlId, ctrlVal);
        }
        private void addShortMessage(int midiChan, int cmd, int data1, int data2) {
            try {
                ShortMessage msg = new ShortMessage();
                msg.setMessage(cmd, midiChan, data1, data2);
                messages.add(msg);
            } catch (InvalidMidiDataException ex) {
                throw new RuntimeException(ex);
            }
        }

        private static final int CHM_ALL_NOTES_OFF = 123;
        private static final int CHM_ALL_SOUND_OFF = 120;
        private static final int CTRL_CHORUS_DEPTH = 93;
        private static final int CTRL_EXPRESSION_POT = 11;
        private static final int CTRL_PAN = 10;
        private static final int CTRL_SUSTAIN = 64;
        private static final int CHM_RESET_ALL = 121;
        private static final int CTRL_REVERB_DEPTH = 91;
        private static final int CTRL_MODULATION_POT = 1;
        private static final int CTRL_VOLUME = 7;

        private long delay;
        private final List<MidiMessage> messages;
    }

}

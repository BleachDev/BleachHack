package org.bleachhack.util.doom.s;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;

/** A music driver that bypasses Sequences and sends events from a MUS lump
 *  directly to a MIDI device.
 *
 *  Some songs (e.g. D_E1M8) vary individual channel volumes dynamically. This
 *  driver multiplies the dynamic volume by the music volume set in the menu.
 *  This does not work well with a {@link Sequence} because changes to events
 *  (e.g. channel volume change events) do not take effect while the sequencer
 *  is running.
 *  
 *  Disadvantages of this driver:
 *  <ul><li>Supports MUS lumps only (no MID, OGG etc.)</li>
 *      <li>Creates its own thread</li>
 *      <li>Pausing is not implemented yet</li></ul>
 *
 * @author finnw
 *
 */
public class FinnwMusicModule implements IMusic {

    public FinnwMusicModule() {
        this.lock = new ReentrantLock();
        this.channels = new ArrayList<Channel>(15);
        this.songs = new ArrayList<Song>(1);
        for (int midiChan = 0; midiChan < 16; ++ midiChan) {
            if (midiChan != 9) {
                channels.add(new Channel(midiChan));
            }
        }
        channels.add(new Channel(9));
    }

    @Override
    public void InitMusic() {
        try {
            receiver = getReceiver();
            EventGroup genMidiEG = new EventGroup(1f);
            genMidiEG.generalMidi(1);
            genMidiEG.sendTo(receiver);
            sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
        } catch (MidiUnavailableException ex) {
            System.err.println(ex);
            receiver = null;
        }
        exec = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl());
    }

    /** Not yet implemented */
    @Override
    public void PauseSong(int handle) {
    }

    @Override
    public void PlaySong(int handle, boolean looping) {
        lock.lock();
        try {
            if (currentTransmitter != null) {
                currentTransmitter.stop();
            }
            currentTransmitter = null;
            if (0 <= handle && handle < songs.size()) {
                prepare(receiver);
                Song song = songs.get(handle);
                currentTransmitter =
                    new ScheduledTransmitter(song.getScoreBuffer(), looping);
                currentTransmitter.setReceiver(receiver);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int RegisterSong(byte[] data) {
        return RegisterSong(ByteBuffer.wrap(data));
    }

    public int RegisterSong(ByteBuffer data) {
        Song song = new Song(data);
        lock.lock();
        try {
            int result = songs.indexOf(null);
            if (result >= 0) {
                songs.set(result, song);
            } else {
                result = songs.size();
                songs.add(song);
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void ResumeSong(int handle) {
    }

    @Override
    public void SetMusicVolume(int volume) {
        float fVol = volume * (1/127f);
        fVol = Math.max(0f, Math.min(fVol, 1f));
        lock.lock();
        try {
            this.volume = fVol;
            if (currentTransmitter != null) {
                currentTransmitter.volumeChanged();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void ShutdownMusic() {
        exec.shutdown();
    }

    @Override
    public void StopSong(int handle) {
        lock.lock();
        try {
            if (currentTransmitter != null) {
                currentTransmitter.stop();
                currentTransmitter = null;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void UnRegisterSong(int handle) {
        lock.lock();
        try {
            if (0 <= handle && handle < songs.size()) {
                songs.set(handle, null);
            }
        } finally {
            lock.unlock();
        }
    }

    static boolean hasMusMagic(ByteBuffer magicBuf) {
        return magicBuf.get(0) == 'M' &&
               magicBuf.get(1) == 'U' &&
               magicBuf.get(2) == 'S' &&
               magicBuf.get(3) == 0x1a;
    }

    EventGroup nextEventGroup(ByteBuffer scoreBuffer, boolean looping)  {
        EventGroup result = new EventGroup(volume);
        boolean last;
        do {
            if (! scoreBuffer.hasRemaining()) {
                if (looping) {
                    scoreBuffer.flip();
                } else {
                    return result.emptyToNull();
                }
            }
            int descriptor = scoreBuffer.get() & 0xff;
            last = (descriptor & 0x80) != 0;
            int eventType = (descriptor >> 4) & 7;
            int chanIndex = descriptor & 15;
            Channel channel = channels.get(chanIndex);
            switch (eventType) {
            case 0:
                {
                    int note = scoreBuffer.get() & 0xff;
                    if ((note & 0x80) != 0) {
                        throw new IllegalArgumentException("Invalid note byte");
                    }
                    checkChannelExists("note off", channel).noteOff(note, result);
                }
                break;
            case 1:
                {
                    int note = scoreBuffer.get() & 0xff;
                    boolean hasVelocity = (note & 0x80) != 0;
                    if (hasVelocity) {
                        int velocity = scoreBuffer.get() & 0xff;
                        if ((velocity & 0x80) != 0) {
                            throw new IllegalArgumentException("Invalid velocity byte");
                        }
                        checkChannelExists("note on", channel).noteOn(note & 127, velocity, result);
                    } else {
                        checkChannelExists("note on", channel).noteOn(note, result);
                    }
                }
                break;
            case 2:
                {
                    int wheelVal = scoreBuffer.get() & 0xff;
                    checkChannelExists("pitch bend", channel).pitchBend(wheelVal, result);
                }
                break;
            case 3:
                {
                    int sysEvt = scoreBuffer.get() & 0xff;
                    switch (sysEvt) {
                    case 10:
                        checkChannelExists("all sounds off", channel).allSoundsOff(result);
                        break;
                    case 11:
                        checkChannelExists("all notes off", channel).allNotesOff(result);
                        break;
                    case 14:
                        checkChannelExists("reset all controllers", channel).resetAll(result);
                        break;
                    default:
                        String msg = String.format("Invalid system event (%d)", sysEvt);
                        throw new IllegalArgumentException(msg);
                    }
                }
                break;
            case 4:
                int cNum = scoreBuffer.get() & 0xff;
                if ((cNum & 0x80) != 0) {
                    throw new IllegalArgumentException("Invalid controller number ");
                }
                int cVal = scoreBuffer.get() & 0xff;
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
                    checkChannelExists("patch change", channel).patchChange(cVal, result);
                    break;
                case 1:
                    // Don't forward this to the MIDI device.  Some devices
                    // react badly to banks that are undefined in GM Level 1
                    checkChannelExists("bank switch", channel);
                    break;
                case 2:
                    checkChannelExists("vibrato change", channel).vibratoChange(cVal, result);
                    break;
                case 3:
                    checkChannelExists("volume", channel).volume(cVal, result);
                    break;
                case 4:
                    checkChannelExists("pan", channel).pan(cVal, result);
                    break;
                case 5:
                    checkChannelExists("expression", channel).expression(cVal, result);
                    break;
                case 6:
                    checkChannelExists("reverb depth", channel).reverbDepth(cVal, result);
                    break;
                case 7:
                    checkChannelExists("chorus depth", channel).chorusDepth(cVal, result);
                    break;
                default:
                    throw new AssertionError("Controller number " + cNum + ": not yet implemented");
                }
                break;
            case 6:
                if (looping) {
                    scoreBuffer.flip();
                } else {
                    return result.emptyToNull();
                }
                break;
            default:
                String msg = String.format("Unknown event type: last=%5s eventType=%d chanIndex=%d%n", last, eventType, chanIndex);
                throw new IllegalArgumentException(msg);
            }
        } while (! last);
        int qTics = readTime(scoreBuffer);
        result.addDelay(qTics);
        return result;
    }

    static class EventGroup {
        EventGroup(float volScale) {
            this.messages = new ArrayList<MidiMessage>();
            this.volScale = volScale;
        }
        void addDelay(int tics) {
            delay += tics;
        }
        private static final int CHM_ALL_NOTES_OFF = 123;
        private static final int CHM_ALL_SOUND_OFF = 120;
        private static final int CTRL_CHORUS_DEPTH = 93;
        private static final int CTRL_EXPRESSION_POT = 11;
        private static final int CTRL_PAN = 10;
        private static final int RPM_PITCH_BEND_SENSITIVITY = 0;
        private static final int RPL_PITCH_BEND_SENSITIVITY = 0;
        private static final int CHM_RESET_ALL = 121;
        private static final int CTRL_REVERB_DEPTH = 91;
        private static final int CTRL_MODULATION_POT = 1;
        private static final int CTRL_VOLUME = 7;
        void allNotesOff(int midiChan) {
            addControlChange(midiChan, CHM_ALL_NOTES_OFF, 0);
        }
        void allSoundsOff(int midiChan) {
            addControlChange(midiChan, CHM_ALL_SOUND_OFF, 0);
        }
        long appendTo(Sequence sequence, int trackNum, long pos) {
            Track track = sequence.getTracks()[trackNum];
            for (MidiMessage msg: messages) {
                track.add(new MidiEvent(msg, pos));
            }
            return pos + delay * 3;
        }
        long appendTo(Track track, long pos, int scale) {
            for (MidiMessage msg: messages) {
                track.add(new MidiEvent(msg, pos));
            }
            return pos + delay * scale;
        }
        void chorusDepth(int midiChan, int depth) {
            addControlChange(midiChan, CTRL_CHORUS_DEPTH, depth);
        }
        void generalMidi(int mode) {
             addSysExMessage(0xf0, (byte)0x7e, (byte)0x7f, (byte)9, (byte)mode, (byte)0xf7);
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
        int getDelay() {
            return delay;
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
        void pitchBendSensitivity(int midiChan, int semitones) {
            addRegParamChange(midiChan, RPM_PITCH_BEND_SENSITIVITY, RPL_PITCH_BEND_SENSITIVITY, semitones);
        }
        void resetAllControllern(int midiChan) {
            addControlChange(midiChan, CHM_RESET_ALL, 0);
        }
        void reverbDepth(int midiChan, int depth) {
            addControlChange(midiChan, CTRL_REVERB_DEPTH, depth);
        }
        void sendTo(Receiver receiver) {
            for (MidiMessage msg: messages) {
                receiver.send(msg, -1);
            }
        }
        void vibratoChange(int midiChan, int depth) {
            addControlChange(midiChan, CTRL_MODULATION_POT, depth);
        }
        void volume(int midiChan, int vol) {
            vol = (int) Math.round(vol * volScale);
            addControlChange(midiChan, CTRL_VOLUME, vol);
        }
        private void addControlChange(int midiChan, int ctrlId, int ctrlVal) {
            addShortMessage(midiChan, ShortMessage.CONTROL_CHANGE, ctrlId, ctrlVal);
        }
        private void addRegParamChange(int midiChan, int paramMsb, int paramLsb, int valMsb) {
            addControlChange(midiChan, 101, paramMsb);
            addControlChange(midiChan, 100, paramLsb);
            addControlChange(midiChan, 6, valMsb);
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
        private void addSysExMessage(int status, byte... data) {
            try {
                SysexMessage msg = new SysexMessage();
                msg.setMessage(status, data, data.length);
                messages.add(msg);
            } catch (InvalidMidiDataException ex) {
                throw new RuntimeException(ex);
            }
        }
        private int delay;
        private final List<MidiMessage> messages;
        private final float volScale;
    }

    /** A collection of kludges to pick a MIDI output device until cvars are implemented */
    static class MidiDeviceComparator implements Comparator<MidiDevice.Info> {
        @Override
        public int compare(MidiDevice.Info o1, MidiDevice.Info o2) {
            float score1 = score(o1), score2 = score(o2);
            if (score1 < score2) {
                return 1;
            } else if (score1 > score2) {
                return -1;
            } else {
                return 0;
            }
        }
        private float score(MidiDevice.Info info) {
            String lcName = info.getName().toLowerCase(Locale.ENGLISH);
            float result = 0f;
            if (lcName.contains("mapper")) {
                // "Midi Mapper" is ideal, because the user can select the default output device in the control panel
                result += 100;
            } else {
                if (lcName.contains("synth")) {
                    // A synthesizer is usually better than a sequencer or USB MIDI port
                    result += 50;
                    if (lcName.contains("java")) {
                        // "Java Sound Synthesizer" has a low sample rate; Prefer another software synth
                        result -= 20;
                    }
                    if (lcName.contains("microsoft")) {
                        // "Microsoft GS Wavetable Synth" is notoriously unpopular, but sometimes it's the only one
                        // with a decent sample rate.
                        result -= 7;
                    }
                }
            }
            return result;
        }
    }

    static class ThreadFactoryImpl implements ThreadFactory {
        @Override
        public Thread newThread(final Runnable r) {
            Thread thread =
                new Thread(r, String.format("FinnwMusicModule-%d", NEXT_ID.getAndIncrement()));
            thread.setPriority(Thread.MAX_PRIORITY - 1);
            return thread;
        }
        private static final AtomicInteger NEXT_ID =
            new AtomicInteger(1);
    }

    final Lock lock;

    static final long nanosPerTick = 1000000000 / 140;

    /** Channels in MUS order (0-14 = instruments, 15 = percussion) */
    final List<Channel> channels;

    ScheduledExecutorService exec;

    float volume;

    private static Receiver getReceiver() throws MidiUnavailableException {
        List<MidiDevice.Info> dInfos =
            new ArrayList<MidiDevice.Info>(Arrays.asList(MidiSystem.getMidiDeviceInfo()));
        for (Iterator<MidiDevice.Info> it = dInfos.iterator();
             it.hasNext();
             ) {
            MidiDevice.Info dInfo = it.next();
            MidiDevice dev = MidiSystem.getMidiDevice(dInfo);
            if (dev.getMaxReceivers() == 0) {
                // We cannot use input-only devices
                it.remove();
            }
        }
        if (dInfos.isEmpty()) return null;
        Collections.sort(dInfos, new MidiDeviceComparator());
        MidiDevice.Info dInfo = dInfos.get(0);
        MidiDevice dev = MidiSystem.getMidiDevice((MidiDevice.Info) dInfo);
        dev.open();
        return dev.getReceiver();
    }

    private void prepare(Receiver receiver) {
        EventGroup setupEG = new EventGroup(volume);
        for (Channel chan: channels) {
            chan.allSoundsOff(setupEG);
            chan.resetAll(setupEG);
            chan.pitchBendSensitivity(2, setupEG);
            chan.volume(127, setupEG);
        }
        setupEG.sendTo(receiver);
    }

    private static void sleepUninterruptibly(int timeout, TimeUnit timeUnit) {
        boolean interrupted = false;
        long now = System.nanoTime();
        final long expiry = now + timeUnit.toNanos(timeout);
        long remaining;
        while ((remaining = expiry - now) > 0L) {
            try {
                TimeUnit.NANOSECONDS.sleep(remaining);
            } catch (InterruptedException ex) {
                interrupted = true;
            } finally {
                now = System.nanoTime();
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
    private static Channel checkChannelExists(String type, Channel channel)
            throws IllegalArgumentException {
        if (channel == null) {
            String msg = String.format("Invalid channel for %s message", type);
            throw new IllegalArgumentException(msg);
        } else {
            return channel;
        }
    }

    private int readTime(ByteBuffer scoreBuffer) {
        int result = 0;
        boolean last;
        do {
            int digit = scoreBuffer.get() & 0xff;
            last = (digit & 0x80) == 0;
            result <<= 7;
            result |= digit & 127;
        } while (! last);
        return result;
    }

    private static class Channel {
        Channel(int midiChan) {
            this.midiChan = midiChan;
        }
        void allNotesOff(EventGroup eventGroup) {
            eventGroup.allNotesOff(midiChan);
        }
        void allSoundsOff(EventGroup eventGroup) {
            eventGroup.allSoundsOff(midiChan);
        }
        void chorusDepth(int depth, EventGroup eventGroup) {
            eventGroup.chorusDepth(midiChan, depth);
        }
        void expression(int expr, EventGroup eventGroup) {
            eventGroup.expression(midiChan, expr);
        }
        void noteOff(int note, EventGroup eventGroup) {
            eventGroup.noteOff(midiChan, note);
        }
        void noteOn(int note, EventGroup eventGroup) {
            eventGroup.noteOn(midiChan, note, lastVelocity);
        }
        void noteOn(int note, int velocity, EventGroup eventGroup) {
            lastVelocity = velocity;
            noteOn(note, eventGroup);
        }
        void pan(int pan, EventGroup eventGroup) {
            eventGroup.pan(midiChan, pan);
        }
        void patchChange(int patchId, EventGroup eventGroup) {
            eventGroup.patchChange(midiChan, patchId);
        }
        void pitchBend(int wheelVal, EventGroup eventGroup) {
            eventGroup.pitchBend(midiChan, wheelVal);
        }
        void pitchBendSensitivity(int semitones, EventGroup eventGroup) {
            eventGroup.pitchBendSensitivity(midiChan, semitones);
        }
        void resetAll(EventGroup eventGroup) {
            eventGroup.resetAllControllern(midiChan);
        }
        void reverbDepth(int depth, EventGroup eventGroup) {
            eventGroup.reverbDepth(midiChan, depth);
        }
        void vibratoChange(int depth, EventGroup eventGroup) {
            eventGroup.vibratoChange(midiChan, depth);
        }
        void volume(int vol, EventGroup eventGroup) {
            eventGroup.volume(midiChan, vol);
            lastVolume = vol;
        }
        void volumeChanged(EventGroup eventGroup) {
            eventGroup.volume(midiChan, lastVolume);
        }
        private int lastVelocity;
        private int lastVolume;
        private final int midiChan;
    }

    private class ScheduledTransmitter implements Transmitter {

        @Override
        public void close() {
            lock.lock();
            try {
                if (autoShutdown && exec != null) {
                    exec.shutdown();
                }
                autoShutdown = false;
                exec = null;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public Receiver getReceiver() {
            return receiver;
        }

        @Override
        public void setReceiver(Receiver receiver) {
            EventGroup currentGroup = null;
            lock.lock();
            try {
                if (this.receiver != null) {
                    if (this.future.cancel(false)) {
                        currentGroup = triggerTask.eventGroup;
                    }
                } else {
                    nextGroupTime = System.nanoTime();
                }
                this.receiver = receiver;
                scheduleIfRequired(receiver, currentGroup);
            } finally {
                lock.unlock();
            }
        }

        ScheduledTransmitter(ByteBuffer scoreBuffer, boolean looping) {
            this.exec = FinnwMusicModule.this.exec;
            this.looping = looping;
            this.scoreBuffer = scoreBuffer;
        }

        void scheduleIfRequired(Receiver receiver,
                                EventGroup currentGroup) {
            assert (((ReentrantLock) lock).isHeldByCurrentThread());
            if (currentGroup == null) {
                try {
                    currentGroup = nextEventGroup(scoreBuffer, looping);
                    if (currentGroup != null) {
                        triggerTask = new TriggerTask(currentGroup, receiver);
                        long delay = Math.max(0, nextGroupTime - System.nanoTime());
                        future =
                            exec.schedule(triggerTask, delay, TimeUnit.NANOSECONDS);
                        nextGroupTime += currentGroup.getDelay() * nanosPerTick;
                    } else {
                        triggerTask = null;
                        future = null;
                    }
                } catch (RejectedExecutionException ex) {
                    // This is normal when shutting down
                } catch (Exception ex) {
                    System.err.println(ex);
                }
            }
        }

        void stop() {
            assert (((ReentrantLock) lock).isHeldByCurrentThread());
            if (future != null) {
                future.cancel(false);
                try {
                    future.get();
                } catch (InterruptedException ex) {
                } catch (ExecutionException ex) {
                } catch (CancellationException ex) {
                }
                future = null;
            }
            EventGroup cleanup = new EventGroup(0f);
            for (Channel chan: channels) {
                chan.allNotesOff(cleanup);
            }
            cleanup.sendTo(receiver);
        }

        void volumeChanged() {
            assert (((ReentrantLock) lock).isHeldByCurrentThread());
            EventGroup adjust = new EventGroup(volume);
            for (Channel chan: channels) {
                chan.volumeChanged(adjust);
            }
            adjust.sendTo(receiver);
        }
        TriggerTask triggerTask;

        private class TriggerTask implements Runnable {
            @Override
            public void run() {
                boolean shouldSend = false;
                lock.lock();
                try {
                    if (triggerTask == this) {
                        shouldSend = true;
                        scheduleIfRequired(receiver, null);
                    }
                } finally {
                    lock.unlock();
                }
                if (shouldSend) {
                    eventGroup.sendTo(receiver);
                }
            }
            TriggerTask(EventGroup eventGroup, Receiver receiver) {
                this.eventGroup = eventGroup;
                this.receiver = receiver;
            }

            final EventGroup eventGroup;
            final Receiver receiver;
        }

        private boolean autoShutdown;

        private ScheduledExecutorService exec;

        private ScheduledFuture<?> future;

        private final boolean looping;

        private long nextGroupTime;

        private Receiver receiver;

        private final ByteBuffer scoreBuffer;
    }

    /** Contains unfiltered MUS data */
    private class Song {
        Song(ByteBuffer data) {
            this.data = data.asReadOnlyBuffer();
            this.data.order(ByteOrder.LITTLE_ENDIAN);
            byte[] magic = new byte[4];
            this.data.get(magic);
            ByteBuffer magicBuf = ByteBuffer.wrap(magic);
            if (! hasMusMagic(magicBuf)) {
                throw new IllegalArgumentException("Expected magic string \"MUS\\x1a\" but found " + Arrays.toString(magic));
            }
            this.scoreLen = this.data.getShort() & 0xffff;
            this.scoreStart = this.data.getShort() & 0xffff;
        }

        /** Get only the score part of the data (skipping the header) */
        ByteBuffer getScoreBuffer() {
            ByteBuffer scoreBuffer = this.data.duplicate();
            scoreBuffer.position(scoreStart);
            scoreBuffer.limit(scoreStart + scoreLen);
            ByteBuffer slice = scoreBuffer.slice();
            return slice;
        }
        private final ByteBuffer data;
        private final int scoreLen;
        private final int scoreStart;
    }

    private ScheduledTransmitter currentTransmitter;

    private Receiver receiver;

    /** Songs indexed by handle */
    private final List<Song> songs;

}

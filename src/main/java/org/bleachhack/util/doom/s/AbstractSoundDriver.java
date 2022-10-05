package org.bleachhack.util.doom.s;

import org.bleachhack.util.doom.data.sfxinfo_t;
import org.bleachhack.util.doom.data.sounds;
import static org.bleachhack.util.doom.data.sounds.S_sfx;
import org.bleachhack.util.doom.doom.DoomMain;

/**
 * Functionality and fields that are common among the various "sound drivers"
 * should go here.
 * 
 * @author Maes
 */

public abstract class AbstractSoundDriver implements ISoundDriver {

    protected final static boolean D = false; // debug
    
    protected final DoomMain<?,?> DM;

    /**
     * The global mixing buffer. Basically, samples from all active internal
     * channels are modifed and added, and stored in the buffer that is
     * submitted to the audio device. This is a 16-bit stereo signed PCM
     * mixbuffer. Memory order is LSB (?) and channel order is L-R-L-R...
     * 
     * Not all i
     * 
     */

    protected byte[] mixbuffer;// = new byte[MIXBUFFERSIZE];
    
    protected final int numChannels;

    /** The actual lengths of all sound effects. */
    protected final int[] lengths = new int[NUMSFX];

    /**
     * The sound in channel handles, determined on registration, might be used
     * to unregister/stop/modify, currently unused.
     */

    protected final int[] channelhandles;

    /**
     * SFX id of the playing sound effect. Used to catch duplicates (like
     * chainsaw).
     */
    protected final int[] channelids;

    /**
     * Pitch to stepping lookup, used in ClassicSoundDriver It's actually rigged
     * to have a -/+ 400% pitch variation!
     */
    protected final int[] steptable = new int[256];

    /** Volume lookups. 128 levels */
    protected final int[][] vol_lookup = new int[128][256];

    /**
     * Time/gametic that the channel started playing, used to determine oldest,
     * which automatically has lowest priority. In case number of active sounds
     * exceeds available channels.
     */
    protected final int[] channelstart;

    // protected final static DataLine.Info info = new DataLine.Info(Clip.class,
    // format);

    public AbstractSoundDriver(DoomMain<?,?> DM, int numChannels) {
        this.DM = DM;
        this.numChannels = numChannels;
        channelids = new int[numChannels];
        channelhandles = new int[numChannels];
        channelstart = new int[numChannels];
    }

    /**
     * Generates volume lookup tables which also turn the unsigned samples into
     * signed samples.
     */

    protected final void generateVolumeLUT() {
        for (int i = 0; i < 128; i++)
            for (int j = 0; j < 256; j++)
                vol_lookup[i][j] = (i * (j - 128) * 256) / 127;
    }

    /**
     * This table provides step widths for pitch parameters. Values go from 16K
     * to 256K roughly, with the middle of the table being 64K, and presumably
     * representing unitary pitch. So the pitch variation can be quite extreme,
     * allowing -/+ 400% stepping :-S
     * 
     * @param steptablemid
     * @return
     */

    protected void generateStepTable(int steptablemid) {
        for (int i = -128; i < 128; i++) {
            steptable[steptablemid + i] =
                (int) (Math.pow(2.0, (i / 64.0)) * 65536.0);
            //System.out.printf("Pitch %d %d %f\n",i,steptable[steptablemid + i],FixedFloat.toFloat(steptable[steptablemid + i]));
        }
    }

    /** Read a Doom-format sound effect from disk, leaving it in 8-bit mono format but
     *  upsampling it to the target sample rate.
     *  
     * @param sfxname
     * @param len
     * @param index
     * @return
     */
    
    protected byte[] getsfx(String sfxname, int[] len, int index) {
        byte[] sfx;
        byte[] paddedsfx;
        int i;
        int size;
        int paddedsize;
        String name;
        int sfxlump;

        // Get the sound data from the WAD, allocate lump
        // in zone memory.
        name = String.format("ds%s", sfxname).toUpperCase();

        // Now, there is a severe problem with the
        // sound handling, in it is not (yet/anymore)
        // gamemode aware. That means, sounds from
        // DOOM II will be requested even with DOOM
        // shareware.
        // The sound list is wired into sounds.c,
        // which sets the external variable.
        // I do not do runtime patches to that
        // variable. Instead, we will use a
        // default sound for replacement.
        if (DM.wadLoader.CheckNumForName(name) == -1)
            sfxlump = DM.wadLoader.GetNumForName("dspistol");
        else
            sfxlump = DM.wadLoader.GetNumForName(name);

        DMXSound dmx= DM.wadLoader.CacheLumpNum(sfxlump, 0, DMXSound.class);
        
        // KRUDE
        if (dmx.speed==SAMPLERATE/2){
            // Plain linear interpolation.
            dmx.data=DSP.crudeResample(dmx.data,2);
            //DSP.filter(dmx.data,SAMPLERATE, SAMPLERATE/4);
            dmx.datasize=dmx.data.length;
            
        }
        
        sfx = dmx.data;

        // MAES: A-ha! So that's how they do it.
        // SOund effects are padded to the highest multiple integer of
        // the mixing buffer's size (with silence)

        paddedsize =
            ((dmx.datasize + (SAMPLECOUNT - 1)) / SAMPLECOUNT) * SAMPLECOUNT;

        // Allocate from zone memory.
        paddedsfx = new byte[paddedsize];

        // Now copy and pad. The first 8 bytes are header info, so we discard
        // them.
        System.arraycopy(sfx, 0, paddedsfx, 0, dmx.datasize);

        // Pad with silence (unsigned)
        for (i = dmx.datasize; i < paddedsize; i++)
            paddedsfx[i] = (byte) 127;

        // Remove the cached lump.
        DM.wadLoader.UnlockLumpNum(sfxlump);

        if (D) System.out.printf("SFX %d name %s size %d speed %d padded to %d\n", index, S_sfx[index].name, dmx.datasize,dmx.speed,paddedsize);
        // Preserve padded length.
        len[index] = paddedsize;

        // Return allocated padded data.
        // So the first 8 bytes are useless?
        return paddedsfx;
    }

    /**
     * Modified getsfx, which transforms samples into 16-bit, signed, stereo
     * beforehand, before being "fed" to the audio clips.
     * 
     * @param sfxname
     * @param index
     * @return
     */
    protected final byte[] getsfx16(String sfxname, int[] len, int index) {
        byte[] sfx;
        byte[] paddedsfx;
        int i;
        int size;
        int paddedsize;
        String name;
        int sfxlump;

        // Get the sound data from the WAD, allocate lump
        // in zone memory.
        name = String.format("ds%s", sfxname).toUpperCase();

        // Now, there is a severe problem with the
        // sound handling, in it is not (yet/anymore)
        // gamemode aware. That means, sounds from
        // DOOM II will be requested even with DOOM
        // shareware.
        // The sound list is wired into sounds.c,
        // which sets the external variable.
        // I do not do runtime patches to that
        // variable. Instead, we will use a
        // default sound for replacement.
        if (DM.wadLoader.CheckNumForName(name) == -1)
            sfxlump = DM.wadLoader.GetNumForName("dspistol");
        else
            sfxlump = DM.wadLoader.GetNumForName(name);

        size = DM.wadLoader.LumpLength(sfxlump);

        sfx = DM.wadLoader.CacheLumpNumAsRawBytes(sfxlump, 0);

        // Size blown up to accommodate two channels and 16 bits.
        // Sampling rate stays the same.

        paddedsize = (size - 8) * 2 * 2;
        // Allocate from zone memory.
        paddedsfx = new byte[paddedsize];

        // Skip first 8 bytes (header), blow up the data
        // to stereo, BIG ENDIAN, SIGNED, 16 bit. Don't expect any fancy DSP
        // here!

        int sample = 0;
        for (i = 8; i < size; i++) {
            // final short sam=(short) vol_lookup[127][0xFF&sfx[i]];
            final short sam = (short) ((0xFF & sfx[i] - 128) << 8);
            paddedsfx[sample++] = (byte) (0xFF & (sam >> 8));
            paddedsfx[sample++] = (byte) (0xFF & sam);
            paddedsfx[sample++] = (byte) (0xFF & (sam >> 8));
            paddedsfx[sample++] = (byte) (0xFF & sam);
        }

        // Remove the cached lump.
        DM.wadLoader.UnlockLumpNum(sfxlump);

        // Preserve padded length.
        len[index] = paddedsize;

        // Return allocated padded data.
        // So the first 8 bytes are useless?
        return paddedsfx;
    }

    /**
     * Starting a sound means adding it to the current list of active sounds in
     * the internal channels. As the SFX info struct contains e.g. a pointer to
     * the raw data it is ignored. As our sound handling does not handle
     * priority, it is ignored. Pitching (that is, increased speed of playback)
     * is set, but whether it's used or not depends on the final implementation
     * (e.g. classic mixer uses it, but AudioLine-based implementations are not
     * guaranteed.
     */

    @Override
    public int StartSound(int id, int vol, int sep, int pitch, int priority) {

        if (id < 1 || id > S_sfx.length - 1)
            return BUSY_HANDLE;

        // Find a free channel and get a timestamp/handle for the new sound.
        int handle = this.addsfx(id, vol, steptable[pitch], sep);

        return handle;
    }

    /**
     * This function adds a sound to the list of currently active sounds, which
     * is maintained as a given number (eight, usually) of internal channels.
     * Returns a handle.
     * 
     * @param sfxid
     * @param volume
     * @param step
     * @param seperation
     * @return
     */

    protected abstract int addsfx(int sfxid, int volume, int step,
            int seperation);

    protected short handlenums = 0;

    //
    // Retrieve the raw data lump index
    // for a given SFX name.
    //
    public final int GetSfxLumpNum(sfxinfo_t sfx) {
        String namebuf;
        namebuf = String.format("ds%s", sfx.name).toUpperCase();
        if (namebuf.equals("DSNONE"))
            return -1;

        int lump;
        try {
            lump = DM.wadLoader.GetNumForName(namebuf);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        return lump;
    }

    /**
     * Initialize 
     * 
     * @return
     */
    protected  final void initMixBuffer() {
        for (int i = 0; i < MIXBUFFERSIZE; i += 4) {
            mixbuffer[i] =
                (byte) (((int) (0x7FFF * Math.sin(1.5 * Math.PI * (double) i
                        / MIXBUFFERSIZE)) & 0xff00) >>> 8);
            mixbuffer[i + 1] =
                (byte) ((int) (0x7FFF * Math.sin(1.5 * Math.PI * (double) i
                        / MIXBUFFERSIZE)) & 0xff);
            mixbuffer[i + 2] =
                (byte) (((int) (0x7FFF * Math.sin(1.5 * Math.PI * (double) i
                        / MIXBUFFERSIZE)) & 0xff00) >>> 8);
            mixbuffer[i + 3] =
                (byte) ((int) (0x7FFF * Math.sin(1.5 * Math.PI * (double) i
                        / MIXBUFFERSIZE)) & 0xff);

        }
    }
    
    /**
     * Loads samples in 8-bit format, forcibly converts them to the common sampling rate.
     * Used by.
     */

    protected final void initSound8() {
        int i;

        // Initialize external data (all sounds) at start, keep static.

        for (i = 1; i < NUMSFX; i++) {
            // Alias? Example is the chaingun sound linked to pistol.
            if (sounds.S_sfx[i].link == null) {
                // Load data from WAD file.
                S_sfx[i].data = getsfx(S_sfx[i].name, lengths, i);
            } else {
                // Previously loaded already?
                S_sfx[i].data = S_sfx[i].link.data;
            }
        }
    }

    /**
     * This is only the common part of InitSound that caches sound data in
     * 16-bit, stereo format (used by Audiolines). INTO sfxenum_t.
     * 
     * Only used by the Clip and David "drivers".
     * 
     */

    protected final void initSound16() {
        int i;

        // Initialize external data (all sounds) at start, keep static.

        for (i = 1; i < NUMSFX; i++) {
            // Alias? Example is the chaingun sound linked to pistol.
            if (sounds.S_sfx[i].link == null) {
                // Load data from WAD file.
                S_sfx[i].data = getsfx16(S_sfx[i].name, lengths, i);
            } else {
                // Previously loaded already?
                S_sfx[i].data = S_sfx[i].link.data;
            }
        }
    }

}

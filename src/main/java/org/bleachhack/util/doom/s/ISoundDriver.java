package org.bleachhack.util.doom.s;

import org.bleachhack.util.doom.data.sfxinfo_t;
import org.bleachhack.util.doom.data.sounds.sfxenum_t;
import org.bleachhack.util.doom.doom.CVarManager;
import org.bleachhack.util.doom.doom.CommandVariable;
import org.bleachhack.util.doom.doom.DoomMain;

//Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: ISoundDriver.java,v 1.1 2012/11/08 17:12:42 velktron Exp $
//
// Copyright (C) 1993-1996 by id Software, Inc.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
//
// DESCRIPTION:
// System interface, sound. Anything implementation-specific should
// implement this.
//
//-----------------------------------------------------------------------------

public interface ISoundDriver {

	
	public static final int VOLUME_STEPS = 128;
	public static final int PANNING_STEPS = 256;
	public static final int IDLE_HANDLE = -1;
	public static final int BUSY_HANDLE = -2;
	// Needed for calling the actual sound output
	// We mix 1024 samples each time, but we only call UpdateSound()
	// 1 time out of three.
	
	public static final int NUM_CHANNELS	=	8;
	// It is 2 for 16bit, and 2 for two channels.
	public static final int BUFMUL            =      4;

	public static final int SAMPLERATE	=	22050;	// Hz
	
	// Update all 30 millisecs, approx. 30fps synchronized.
	// Linux resolution is allegedly 10 millisecs,
	//  scale is microseconds.
	public static final int SOUND_INTERVAL =    500;
	
	/** Yes, it's possible to select a different sound frame rate */
	
	public static final int SND_FRAME_RATE =21;
	// Was 512, but if you mix that many samples per tic you will
	// eventually outrun the buffer :-/ I fail to see the mathematical
	// justification behind this, unless they simply wanted the buffer to 
	// be a nice round number in size.
	public static final int SAMPLECOUNT		=SAMPLERATE/SND_FRAME_RATE;
	public static final int MIXBUFFERSIZE		=(SAMPLECOUNT*BUFMUL);
	public static final int SAMPLESIZE	=	16 ;  	// 16bit
	public static final int NUMSFX	=	sfxenum_t.NUMSFX.ordinal() ; 
	public static final int MAXHANDLES = 100;
	/** How many audio chunks/frames to mix before submitting them to
	 *  the output.
	 */
	public static final int BUFFER_CHUNKS=5;
	
	/** Ths audio buffer size of the audioline itself. 
	 *  Increasing this is the only effective way to combat output stuttering on
	 *  slower machines.
	 */
	public static final int AUDIOLINE_BUFFER=2*BUFFER_CHUNKS*MIXBUFFERSIZE;
	public static final int SOUND_PERIOD = 1000/SND_FRAME_RATE; // in ms

    public static ISoundDriver chooseModule(DoomMain<?, ?> DM, CVarManager CVM) {
        final ISoundDriver driver;
        if (CVM.bool(CommandVariable.NOSFX) || CVM.bool(CommandVariable.NOSOUND)) {
            driver = new DummySFX();
        } else {
            // Switch between possible sound drivers.
            if (CVM.bool(CommandVariable.AUDIOLINES)) { // Crudish.
                driver = new DavidSFXModule(DM, DM.numChannels);
            } else  if (CVM.bool(CommandVariable.SPEAKERSOUND)) { // PC Speaker emulation
                driver = new SpeakerDoomSoundDriver(DM, DM.numChannels);
            } else if (CVM.bool(CommandVariable.CLIPSOUND)) {
                driver = new ClipSFXModule(DM, DM.numChannels);
            } else if (CVM.bool(CommandVariable.CLASSICSOUND)) { // This is the default
                driver = new ClassicDoomSoundDriver(DM, DM.numChannels);
            } else { // This is the default
                driver = new SuperDoomSoundDriver(DM, DM.numChannels);
            }
        }
        // Check for sound init failure and revert to dummy
        if (!driver.InitSound()) {
            System.err.println("S_InitSound: failed. Reverting to dummy...\n");
            return new DummySFX();
        }
        return driver;
    }
	
	/** Init at program start. Return false if device invalid,
	 *  so that caller can decide best course of action. 
	 *  The suggested one is to swap the sound "driver" for a dummy.
	 *  
	 * @return
	 */
	boolean InitSound();

	// ... update sound buffer and audio device at runtime...
	void UpdateSound();
	void SubmitSound();

	// ... shut down and relase at program termination.
	void ShutdownSound();


	//
	//  SFX I/O
	//

	// Initialize channels?
	void SetChannels(int numChannels);

	// Get raw data lump index for sound descriptor.
	int GetSfxLumpNum (sfxinfo_t sfxinfo );


	// Starts a sound in a particular sound channel.
	int StartSound
	( int		id,
	  int		vol,
	  int		sep,
	  int		pitch,
	  int		priority );


	// Stops a sound channel.
	void StopSound(int handle);

	/** Called by S_*() functions to see if a channel is still playing.
	    Returns false if no longer playing, true if playing. This is 
	    a relatively "high level" function, so its accuracy relies on
	    what the "system specific" sound code reports back */
	boolean SoundIsPlaying(int handle);

	/* Updates the volume, separation,
	   and pitch of a sound channel. */
	void UpdateSoundParams
	( int		handle,
	  int		vol,
	  int		sep,
	  int		pitch );
	
}

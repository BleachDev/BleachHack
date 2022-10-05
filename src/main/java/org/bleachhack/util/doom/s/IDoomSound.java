package org.bleachhack.util.doom.s;

import org.bleachhack.util.doom.data.sfxinfo_t;
import org.bleachhack.util.doom.data.sounds.musicenum_t;
import org.bleachhack.util.doom.data.sounds.sfxenum_t;
import org.bleachhack.util.doom.doom.CVarManager;
import org.bleachhack.util.doom.doom.CommandVariable;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.p.mobj_t;

// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: IDoomSound.java,v 1.5 2011/08/24 15:55:12 velktron Exp $
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
// DESCRIPTION:
//	The not so system specific sound interface (s_sound.*)
// Anything high-level like e.g. handling of panning, sound origin,
// sound multiplicity etc. should be handled here, but not e.g. actual
// sound playback, sound threads, etc.
//  That's the job of ISound and IMusic (roughly equivelnt to i_sound.*, but
// with separate concerns for SFX and MUSIC.
//
//-----------------------------------------------------------------------------

public interface IDoomSound {

    static IDoomSound chooseSoundIsPresent(DoomMain<?, ?> DM, CVarManager CVM, ISoundDriver ISND) {
        if (!CVM.bool(CommandVariable.NOSOUND) || (ISND instanceof DummySFX && DM.music instanceof DummyMusic)) {
            return new AbstractDoomAudio(DM, DM.numChannels);
        } else {
            /**
             * Saves a lot of distance calculations,
             * if we're not to output any sound at all.
             * TODO: create a Dummy that can handle music alone.
             */
            return new DummySoundDriver();
        }
    }

	class channel_t{

		    // sound information (if null, channel avail.)
		    sfxinfo_t	sfxinfo;

		    // origin of sound
		    ISoundOrigin	origin;

		    // handle of the sound being played
		    int		handle;
			}
	
	/** Convenience hack */
	public static final int NUMSFX=sfxenum_t.NUMSFX.ordinal();
	
	// Purpose?
	public static final char snd_prefixen[]
	                                      = { 'P', 'P', 'A', 'S', 'S', 'S', 'M', 'M', 'M', 'S', 'S', 'S' };

	public static final int S_MAX_VOLUME=127;

	// when to clip out sounds
	// Does not fit the large outdoor areas.
	public static final int S_CLIPPING_DIST	=	(1200*0x10000);

	// Distance tp origin when sounds should be maxed out.
	// This should relate to movement clipping resolution
	// (see BLOCKMAP handling).
	// Originally: (200*0x10000).
	public static final int S_CLOSE_DIST	=(160*0x10000);

	public static final int S_ATTENUATOR	=((S_CLIPPING_DIST-S_CLOSE_DIST)>>org.bleachhack.util.doom.m.fixed_t.FRACBITS);

	// Adjustable by menu.
	//protected final int NORM_VOLUME    		snd_MaxVolume

	public static final int NORM_PITCH    = 		128;
	public final static int NORM_PRIORITY	=	64;
	public final static int NORM_SEP		=128;

	public final static int S_PITCH_PERTURB	=	1;
	public final static int S_STEREO_SWING	=	(96*0x10000);

	// percent attenuation from front to back
	public final static int S_IFRACVOL	=	30;

	public final static int NA	=		0;
	public final static int S_NUMCHANNELS=		2;

	/**
	 * Initializes sound stuff, including volume Sets channels, SFX and music
	 * volume, allocates channel buffer, sets S_sfx lookup.
	 */

	void Init(int sfxVolume, int musicVolume);

	/**
	 * Per level startup code. Kills playing sounds at start of level,
	 * determines music if any, changes music.
	 */
	public void Start();

	/**
	 * Start sound for thing at <origin> using <sound_id> from sounds.h
	 */
	public void StartSound(ISoundOrigin origin, int sound_id);

	/**
	 * Start sound for thing at <origin> using <sound_id> from sounds.h
	 * Convenience method using sfxenum_t instead. Delegated to int version.
	 * 
	 */
	public void StartSound(ISoundOrigin origin, sfxenum_t sound_id);
	
	/** Will start a sound at a given volume. */
	public void StartSoundAtVolume(ISoundOrigin origin, int sound_id, int volume);

	/** Stop sound for thing at <origin> */
	public void StopSound(ISoundOrigin origin);

	/**
	 * Start music using <music_id> from sounds.h, and set whether looping
	 * 
	 * @param musicnum
	 * @param looping
	 */
	public void ChangeMusic(int musicnum, boolean looping);
	
	public void ChangeMusic(musicenum_t musicnum, boolean looping);

	/** Stops the music fer sure. */
	public void StopMusic();

	/** Stop and resume music, during game PAUSE. */
	public void PauseSound();

	public void ResumeSound();

	/**
	 * Updates music & sounds
	 * 
	 * @param listener
	 */
	public void UpdateSounds(mobj_t listener);

	public void SetMusicVolume(int volume);

	public void SetSfxVolume(int volume);


	/** Start music using <music_id> from sounds.h */
	public void StartMusic(int music_id);
	
	/** Start music using <music_id> from sounds.h 
	 *  Convenience method using musicenum_t.
	 */
	public void StartMusic(musicenum_t music_id);
	
	//
	// Internals. 
	// 
	// MAES: these appear to be only of value for internal implementation,
	// and are never called externally. Thus, they might as well
	// not be part of the interface, even though it's convenient to reuse them.
	//
	
	/*
	int
	S_getChannel
	( mobj_t		origin,
	  sfxinfo_t	sfxinfo );


	int
	S_AdjustSoundParams
	( mobj_t	listener,
	  mobj_t	source,
	  int		vol,
	  int		sep,
	  int		pitch );

	void S_StopChannel(int cnum);
	*/

}

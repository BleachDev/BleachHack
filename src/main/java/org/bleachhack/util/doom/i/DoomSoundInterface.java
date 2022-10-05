package org.bleachhack.util.doom.i;

// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: DoomSoundInterface.java,v 1.3 2011/02/11 00:11:13 velktron Exp $
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
//	System interface, sound.
//
//-----------------------------------------------------------------------------



import org.bleachhack.util.doom.data.sfxinfo_t;


/*
// UNIX hack, to be removed.
#ifdef SNDSERV
#include <stdio.h>
extern FILE* sndserver;
extern char* sndserver_filename;
#endif*/


public interface DoomSoundInterface{


// Init at program start...
public void I_InitSound();

// ... update sound buffer and audio device at runtime...
public void I_UpdateSound();
public void I_SubmitSound();

// ... shut down and relase at program termination.
public void I_ShutdownSound();


//
//  SFX I/O
//

// Initialize channels?
void I_SetChannels();

// Get raw data lump index for sound descriptor.
public int I_GetSfxLumpNum (sfxinfo_t sfxinfo );


// Starts a sound in a particular sound channel.
public int
I_StartSound
( int		id,
  int		vol,
  int		sep,
  int		pitch,
  int		priority );


// Stops a sound channel.
public void I_StopSound(int handle);

// Called by S_*() functions
//  to see if a channel is still playing.
// Returns 0 if no longer playing, 1 if playing.
public boolean I_SoundIsPlaying(int handle);

// Updates the volume, separation,
//  and pitch of a sound channel.
public void
I_UpdateSoundParams
( int		handle,
  int		vol,
  int		sep,
  int		pitch );


//
//  MUSIC I/O
//
public void I_InitMusic();
public void I_ShutdownMusic();
// Volume.
public void I_SetMusicVolume(int volume);
// PAUSE game handling.
public void I_PauseSong(int handle);
public void I_ResumeSong(int handle);
// Registers a song handle to song data.
public int I_RegisterSong(byte[] data);
// Called by anything that wishes to start music.
//  plays a song, and when the song is done,
//  starts playing it again in an endless loop.
// Horrible thing to do, considering.
public void
I_PlaySong
( int		handle,
  int		looping );
// Stops a song over 3 seconds.
public void I_StopSong(int handle);
// See above (register), then think backwards
public void I_UnRegisterSong(int handle);
}
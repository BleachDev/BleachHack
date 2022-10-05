package org.bleachhack.util.doom.i;

// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: SystemSoundInterface.java,v 1.2 2011/05/17 16:51:20 velktron Exp $
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


public interface SystemSoundInterface{


// Init at program start...
public void InitSound();

// ... update sound buffer and audio device at runtime...
public void UpdateSound();
public void SubmitSound();

// ... shut down and relase at program termination.
public void ShutdownSound();


//
//  SFX I/O
//

// Initialize channels?
void SetChannels();

// Get raw data lump index for sound descriptor.
public int GetSfxLumpNum (sfxinfo_t sfxinfo );


// Starts a sound in a particular sound channel.
public int
StartSound
( int		id,
  int		vol,
  int		sep,
  int		pitch,
  int		priority );


// Stops a sound channel.
public void StopSound(int handle);

// Called by S_*() functions
//  to see if a channel is still playing.
// Returns 0 if no longer playing, 1 if playing.
public boolean SoundIsPlaying(int handle);

// Updates the volume, separation,
//  and pitch of a sound channel.
public void
UpdateSoundParams
( int		handle,
  int		vol,
  int		sep,
  int		pitch );


//
//  MUSIC I/O
//
public void InitMusic();
public void ShutdownMusic();
// Volume.
public void SetMusicVolume(int volume);
// PAUSE game handling.
public void PauseSong(int handle);
public void ResumeSong(int handle);
// Registers a song handle to song data.
public int RegisterSong(byte[] data);
// Called by anything that wishes to start music.
//  plays a song, and when the song is done,
//  starts playing it again in an endless loop.
// Horrible thing to do, considering.
public void
PlaySong
( int		handle,
  int		looping );
// Stops a song over 3 seconds.
public void StopSong(int handle);
// See above (register), then think backwards
public void UnRegisterSong(int handle);
}
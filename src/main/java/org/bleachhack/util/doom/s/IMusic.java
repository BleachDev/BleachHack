package org.bleachhack.util.doom.s;

//

import org.bleachhack.util.doom.doom.CVarManager;
import org.bleachhack.util.doom.doom.CommandVariable;

//  MUSIC I/O
//

public interface IMusic {

	void InitMusic();
	void ShutdownMusic();
	// Volume.
	void SetMusicVolume(int volume);
	/** PAUSE game handling. */
	void PauseSong(int handle);
	void ResumeSong(int handle);
	
	/** Registers a song handle to song data. 
	 *  This should handle any conversions from MUS/MIDI/OPL/etc.
	 * 
	 * */
	int RegisterSong(byte[] data);
	
	
	/** Called by anything that wishes to start music.
	   plays a song, and when the song is done,
	  starts playing it again in an endless loop.
     Horrible thing to do, considering. */
	
	void
	PlaySong
	( int		handle,
	  boolean		looping );
	
	/** Stops a song over 3 seconds. */
	void StopSong(int handle);
	
	/** See above (register), then think backwards */
	void UnRegisterSong(int handle);

    public static IMusic chooseModule(CVarManager CVM) {
        if (CVM.bool(CommandVariable.NOMUSIC) || CVM.bool(CommandVariable.NOSOUND)) {
            return new DummyMusic();
        } else {
            return new DavidMusicModule();
        }
    }
}

package org.bleachhack.util.doom.s;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;

import org.bleachhack.util.doom.p.mobj_t;
import org.bleachhack.util.doom.data.sfxinfo_t;

public class channel_t 
{
	
	public channel_t(){
		sfxinfo=new sfxinfo_t();
	}
    
	/** Currently playing sound. If null, then it's free */
	DoomSound currentSound = null;
	
    sfxinfo_t	sfxinfo;

    // origin of sound (usually a mobj_t).
    mobj_t	origin;

    // handle of the sound being played
    int		handle;
    
    AudioFormat format;
    
	public int sfxVolume;
    
	SourceDataLine auline = null;
}

package org.bleachhack.util.doom.s;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import org.bleachhack.util.doom.data.sfxinfo_t;

/** A class representing a sample in memory 
 *  Convenient for wrapping/mirroring it regardless of what it represents.
 * */
class DoomSound extends sfxinfo_t {

	/** This audio format is the one used by internal samples (16 bit, 11KHz, Stereo) 
     *  for Clips and AudioLines. Sure, it's not general enough... who cares though?
     */
	public final static AudioFormat DEFAULT_SAMPLES_FORMAT=new AudioFormat(Encoding.PCM_SIGNED, ISoundDriver.SAMPLERATE, 16, 2, 4, ISoundDriver.SAMPLERATE, true);
	
	public final static AudioFormat DEFAULT_DOOM_FORMAT=new AudioFormat(Encoding.PCM_UNSIGNED, ISoundDriver.SAMPLERATE, 8, 1, 1, ISoundDriver.SAMPLERATE, true);
	
	
	public AudioFormat format;
	
	public DoomSound(AudioFormat format) {
		this.format=format;
	}
	
	public DoomSound(){
		this.format=DEFAULT_DOOM_FORMAT;
	}
	
	public DoomSound(sfxinfo_t sfx,AudioFormat format){
		this(format);
		this.data=sfx.data;
		this.pitch=sfx.pitch;
		this.link=sfx.link;
		this.lumpnum=sfx.lumpnum;
		this.name=sfx.name;
		this.priority=sfx.priority;
		this.singularity=sfx.singularity;
		this.usefulness=sfx.usefulness;
		this.volume=sfx.volume;
		}
	
}

package org.bleachhack.util.doom.s;

import org.bleachhack.util.doom.data.Defines;
import static org.bleachhack.util.doom.data.Tables.ANGLETOFINESHIFT;
import static org.bleachhack.util.doom.data.Tables.BITS32;
import static org.bleachhack.util.doom.data.Tables.finesine;
import org.bleachhack.util.doom.data.musicinfo_t;
import org.bleachhack.util.doom.data.sfxinfo_t;
import org.bleachhack.util.doom.data.sounds;
import static org.bleachhack.util.doom.data.sounds.S_sfx;
import org.bleachhack.util.doom.data.sounds.musicenum_t;
import org.bleachhack.util.doom.data.sounds.sfxenum_t;
import org.bleachhack.util.doom.doom.DoomMain;
import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;
import static org.bleachhack.util.doom.m.fixed_t.FixedMul;
import org.bleachhack.util.doom.p.mobj_t;

/** Some stuff that is not implementation dependant
 *  This includes channel management, sound priorities,
 *  positioning, distance attenuation etc. It's up to 
 *  lower-level "drivers" to actually implements those.
 *  This particular class needs not be a dummy itself, but
 *  the drivers it "talks" to might be. 
 *  
 * 
 * */


public class AbstractDoomAudio implements IDoomSound{

	protected final DoomMain<?,?> DS;
	protected final IMusic IMUS;
	protected final ISoundDriver ISND;

	protected final int numChannels;

	protected final static boolean D=false;

	/** the set of channels available. These are "soft" descriptor
	   channels,  not to be confused with actual hardware audio 
	   lines, which are an entirely different concern.

	 */

	protected final channel_t[]	channels;


	// These are not used, but should be (menu).
	// Maximum volume of a sound effect.
	// Internal default is max out of 0-15.
	protected int 		snd_SfxVolume = 15;

	// Maximum volume of music. Useless so far.
	protected int 		snd_MusicVolume = 15; 

	// whether songs are mus_paused
	protected boolean mus_paused;

	// music currently being played
	protected musicinfo_t mus_playing;

	protected int nextcleanup;

	public AbstractDoomAudio(DoomMain<?,?> DS, int numChannels){
		this.DS = DS;
		this.numChannels=numChannels;
		this.channels=new channel_t[numChannels];
		this.IMUS=DS.music;
		this.ISND=DS.soundDriver;
	}



	/** Volume, pitch, separation  & priority packed for parameter passing */

	protected class vps_t{
		int volume;
		int pitch;
		int sep;
		int priority;
	}


	/**
	 * Initializes sound stuff, including volume
	 * Sets channels, SFX and music volume,
	 *  allocates channel buffer, sets S_sfx lookup.
	 */

	public void Init
	( int		sfxVolume,
			int		musicVolume )
	{  
		int		i;

		System.err.printf("S_Init: default sfx volume %d\n", sfxVolume);

		this.snd_SfxVolume=sfxVolume;
		this.snd_MusicVolume=musicVolume;
		// Whatever these did with DMX, these are rather dummies now.
		// MAES: any implementation-dependant channel setup should start here.
		ISND.SetChannels(numChannels);

		SetSfxVolume(sfxVolume);
		// No music with Linux - another dummy.
		// MAES: these must be initialized somewhere, perhaps not here?
		IMUS.SetMusicVolume(musicVolume);

		// Allocating the internal channels for mixing
		// (the maximum numer of sounds rendered
		// simultaneously) within zone memory.
		// MAES: already done that in the constructor.

		// Free all channels for use
		for (i=0 ; i<numChannels ; i++){
			channels[i]=new channel_t();
			//channels[i].sfxinfo = null;
		}

		// no sounds are playing, and they are not mus_paused
		mus_paused = false;

		// Note that sounds have not been cached (yet).
		for (i=1 ; i<S_sfx.length ; i++)
			S_sfx[i].lumpnum = S_sfx[i].usefulness = -1;
	}

	//
	// Per level startup code.
	// Kills playing sounds at start of level,
	//  determines music if any, changes music.
	//
	public void Start()
	{
		int cnum;
		int mnum;

		// kill all playing sounds at start of level
		//  (trust me - a good idea)
		for (cnum=0 ; cnum<numChannels ; cnum++)
			if (channels[cnum].sfxinfo!=null)
				StopChannel(cnum);

		// start new music for the level
		mus_paused = false;

		if (DS.isCommercial())
			mnum = musicenum_t.mus_runnin.ordinal() + DS.gamemap - 1;
		else
		{
			musicenum_t[] spmus=
			{
					// Song - Who? - Where?

					musicenum_t.mus_e3m4,	// American	e4m1
					musicenum_t.mus_e3m2,	// Romero	e4m2
					musicenum_t.mus_e3m3,	// Shawn	e4m3
					musicenum_t.mus_e1m5,	// American	e4m4
					musicenum_t.mus_e2m7,	// Tim 	e4m5
					musicenum_t.mus_e2m4,	// Romero	e4m6
					musicenum_t.mus_e2m6,	// J.Anderson	e4m7 CHIRON.WAD
					musicenum_t.mus_e2m5,	// Shawn	e4m8
					musicenum_t.mus_e1m9	// Tim		e4m9
			};

			if (DS.gameepisode < 4)
				mnum = musicenum_t.mus_e1m1.ordinal() + (DS.gameepisode-1)*9 + DS.gamemap-1;
			else
				mnum = spmus[DS.gamemap-1].ordinal();
		}	

		// HACK FOR COMMERCIAL
		//  if (commercial && mnum > mus_e3m9)	
		//      mnum -= mus_e3m9;

		ChangeMusic(mnum, true);

		nextcleanup = 15;
	}

	private vps_t vps=new vps_t();

	public void
	StartSoundAtVolume
	( ISoundOrigin		origin_p,
			int		sfx_id,
			int		volume )
	{

		boolean		rc;
		int		sep = 0; // This is set later.
		int		pitch;
		int		priority;
		sfxinfo_t	sfx;
		int		cnum;

		ISoundOrigin	origin = (ISoundOrigin) origin_p;


		// Debug.
		
		//if (origin!=null && origin.type!=null)
		// System.err.printf(
	  	//   "S_StartSoundAtVolume: playing sound %d (%s) from %s %d\n",
	  	 //  sfx_id, S_sfx[sfx_id].name , origin.type.toString(),origin.hashCode());
		 

		// check for bogus sound #
		if (sfx_id < 1 || sfx_id > NUMSFX){
			Exception e=new Exception();
			e.printStackTrace();
			DS.doomSystem.Error("Bad sfx #: %d", sfx_id);
		}

		sfx = S_sfx[sfx_id];

		// Initialize sound parameters
		if (sfx.link!=null)
		{
			pitch = sfx.pitch;
			priority = sfx.priority;
			volume += sfx.volume;

			if (volume < 1)
				return;

			if (volume > snd_SfxVolume)
				volume = snd_SfxVolume;
		}	
		else
		{
			pitch = NORM_PITCH;
			priority = NORM_PRIORITY;
		}


		// Check to see if it is audible,
		//  and if not, modify the params
		if ((origin!=null) && origin != DS.players[DS.consoleplayer].mo)
		{
			vps.volume=volume;
			vps.pitch=pitch;
			vps.sep=sep;
			rc = AdjustSoundParams(DS.players[DS.consoleplayer].mo,
					origin, vps);
			volume=vps.volume;
			pitch=vps.pitch;
			sep=vps.sep;


			if ( origin.getX() == DS.players[DS.consoleplayer].mo.x
					&& origin.getY() == DS.players[DS.consoleplayer].mo.y)
			{	
				sep 	= NORM_SEP;
			}

			if (!rc) {
				//System.err.printf("S_StartSoundAtVolume: Sound %d (%s) rejected because: inaudible\n",
			  	//   sfx_id, S_sfx[sfx_id].name );
				return;
			}
		}	
		else
		{
			sep = NORM_SEP;
		}

		// hacks to vary the sfx pitches
		if (sfx_id >= sfxenum_t.sfx_sawup.ordinal()
				&& sfx_id <= sfxenum_t.sfx_sawhit.ordinal())
		{	
			pitch += 8 - (DS.random.M_Random()&15);

			if (pitch<0)
				pitch = 0;
			else if (pitch>255)
				pitch = 255;
		}
		else if (sfx_id != sfxenum_t.sfx_itemup.ordinal()
				&& sfx_id != sfxenum_t.sfx_tink.ordinal())
		{
			pitch += 16 - (DS.random.M_Random()&31);

			if (pitch<0)
				pitch = 0;
			else if (pitch>255)
				pitch = 255;
		}

		// kill old sound
		StopSound(origin);

		// try to find a channel
		cnum = getChannel(origin, sfx);

		if (cnum<0)
			return;

		//
		// This is supposed to handle the loading/caching.
		// For some odd reason, the caching is done nearly
		//  each time the sound is needed?
		//

		// get lumpnum if necessary
		if (sfx.lumpnum < 0) // Now, it crosses into specific territory.
			sfx.lumpnum = ISND.GetSfxLumpNum(sfx);

		/*
	#ifndef SNDSRV
	  // cache data if necessary
	  if (!sfx->data)
	  {
	    fprintf( stderr,
		     "S_StartSoundAtVolume: 16bit and not pre-cached - wtf?\n");

	    // DOS remains, 8bit handling
	    //sfx->data = (void *) W_CacheLumpNum(sfx->lumpnum, PU_MUSIC);
	    // fprintf( stderr,
	    //	     "S_StartSoundAtVolume: loading %d (lump %d) : 0x%x\n",
	    //       sfx_id, sfx->lumpnum, (int)sfx->data );

	  }
	#endif */

		// increase the usefulness
		if (sfx.usefulness++ < 0)
			sfx.usefulness = 1;

		// Assigns the handle to one of the channels in the
		//  mix/output buffer. This is when things actually
		// become hard (pun intended).
		// TODO: which channel? How do we know how the actual hardware 
		// ones map with the "soft" ones?
		// Essentially we're begging to get an actual channel.		
		
		channels[cnum].handle = ISND.StartSound(sfx_id,
				/*sfx->data,*/
				volume,
				sep,
				pitch,
				priority);
		
		if (D) System.err.printf("Handle %d for channel %d for sound %s vol %d sep %d\n",channels[cnum].handle,
				cnum,sfx.name,volume,sep);
	}	


	public void
	StartSound
	( ISoundOrigin		origin,
			sfxenum_t		sfx_id ){
		//  MAES: necessary sanity check at this point.
		if (sfx_id!=null && sfx_id.ordinal()>0)
			StartSound(origin,sfx_id.ordinal());
	}

	public void
	StartSound
	( ISoundOrigin		origin,
			int		sfx_id )
	{
		/* #ifdef SAWDEBUG
	    // if (sfx_id == sfx_sawful)
	    // sfx_id = sfx_itemup;
	#endif */

		StartSoundAtVolume(origin, sfx_id, snd_SfxVolume);


		// UNUSED. We had problems, had we not?
		/* #ifdef SAWDEBUG
	{
	    int i;
	    int n;

	    static mobj_t*      last_saw_origins[10] = {1,1,1,1,1,1,1,1,1,1};
	    static int		first_saw=0;
	    static int		next_saw=0;

	    if (sfx_id == sfx_sawidl
		|| sfx_id == sfx_sawful
		|| sfx_id == sfx_sawhit)
	    {
		for (i=first_saw;i!=next_saw;i=(i+1)%10)
		    if (last_saw_origins[i] != origin)
			fprintf(stderr, "old origin 0x%lx != "
				"origin 0x%lx for sfx %d\n",
				last_saw_origins[i],
				origin,
				sfx_id);

		last_saw_origins[next_saw] = origin;
		next_saw = (next_saw + 1) % 10;
		if (next_saw == first_saw)
		    first_saw = (first_saw + 1) % 10;

		for (n=i=0; i<numChannels ; i++)
		{
		    if (channels[i].sfxinfo == &S_sfx[sfx_sawidl]
			|| channels[i].sfxinfo == &S_sfx[sfx_sawful]
			|| channels[i].sfxinfo == &S_sfx[sfx_sawhit]) n++;
		}

		if (n>1)
		{
		    for (i=0; i<numChannels ; i++)
		    {
			if (channels[i].sfxinfo == &S_sfx[sfx_sawidl]
			    || channels[i].sfxinfo == &S_sfx[sfx_sawful]
			    || channels[i].sfxinfo == &S_sfx[sfx_sawhit])
			{
			    fprintf(stderr,
				    "chn: sfxinfo=0x%lx, origin=0x%lx, "
				    "handle=%d\n",
				    channels[i].sfxinfo,
				    channels[i].origin,
				    channels[i].handle);
			}
		    }
		    fprintf(stderr, "\n");
		}
	    }
	}
	#endif*/

	}

	// This one is public.
	public void StopSound(ISoundOrigin origin)
	{

		int cnum;

		for (cnum=0 ; cnum<numChannels ; cnum++)
		{
			if (channels[cnum].sfxinfo!=null && channels[cnum].origin == origin)
			{
				// This one is not.
				StopChannel(cnum);
				break;
			}
		}
	}

	//
	// Stop and resume music, during game PAUSE.
	//
	public void PauseSound()
	{
		if (mus_playing!=null && !mus_paused)
		{
			IMUS.PauseSong(mus_playing.handle);
			mus_paused = true;
		}
	}

	public void ResumeSound()
	{
		if (mus_playing!=null && mus_paused)
		{
			IMUS.ResumeSong(mus_playing.handle);
			mus_paused = false;
		}
	}

	@Override
	public void UpdateSounds(mobj_t listener) {
		boolean		audible;
		int		cnum;
		//int		volume;
		//int		sep;
		//int		pitch;
		sfxinfo_t	sfx;
		channel_t	c;

		// Clean up unused data.
		// This is currently not done for 16bit (sounds cached static).
		// DOS 8bit remains. 
		/*if (gametic.nextcleanup)
		    {
			for (i=1 ; i<NUMSFX ; i++)
			{
			    if (S_sfx[i].usefulness < 1
				&& S_sfx[i].usefulness > -1)
			    {
				if (--S_sfx[i].usefulness == -1)
				{
				    Z_ChangeTag(S_sfx[i].data, PU_CACHE);
				    S_sfx[i].data = 0;
				}
			    }
			}
			nextcleanup = gametic + 15;
		    }*/

		for (cnum=0 ; cnum<numChannels ; cnum++)
		{		    
			c = channels[cnum];
			sfx = c.sfxinfo;

			//System.out.printf("Updating channel %d %s\n",cnum,c);
			if (c.sfxinfo!=null)
			{
				if (ISND.SoundIsPlaying(c.handle))
				{
					// initialize parameters
					vps.volume = snd_SfxVolume;
					vps.pitch = NORM_PITCH;
					vps.sep = NORM_SEP;

					sfx=c.sfxinfo;

					if (sfx.link!=null)
					{
						vps.pitch = sfx.pitch;
						vps.volume += sfx.volume;
						if (vps.volume < 1)
						{
							StopChannel(cnum);
							continue;
						}
						else if (vps.volume > snd_SfxVolume)
						{
							vps.volume = snd_SfxVolume;
						}
					}

					// check non-local sounds for distance clipping
					//  or modify their params
					if (c.origin!=null && (listener != c.origin))
					{
						audible = AdjustSoundParams(listener,
								c.origin,
								vps);

						if (!audible)
						{
							StopChannel(cnum);
						}
						else
							ISND.UpdateSoundParams(c.handle, vps.volume, vps.sep, vps.pitch);
					}
				}
				else
				{
					// if channel is allocated but sound has stopped,
					//  free it
					StopChannel(cnum);
				}
			}
		}
		// kill music if it is a single-play && finished
		// if (	mus_playing
		//      && !I_QrySongPlaying(mus_playing->handle)
		//      && !mus_paused )
		// S_StopMusic();
	}

	public void SetMusicVolume(int volume)
	{
		if (volume < 0 || volume > 127)
		{
			DS.doomSystem.Error("Attempt to set music volume at %d",
					volume);
		}    

		IMUS.SetMusicVolume(volume);
		snd_MusicVolume = volume;
	}

	public void SetSfxVolume(int volume)
	{

		if (volume < 0 || volume > 127)
			DS.doomSystem.Error("Attempt to set sfx volume at %d", volume);

		snd_SfxVolume = volume;

	}

	//
	// Starts some music with the music id found in sounds.h.
	//
	public void StartMusic(int m_id)
	{
		ChangeMusic(m_id, false);
	}

	//
	// Starts some music with the music id found in sounds.h.
	//
	public void StartMusic(musicenum_t m_id)
	{
		ChangeMusic(m_id.ordinal(), false);
	}
	
	public void ChangeMusic(musicenum_t musicnum,
			boolean			looping )
	{
		ChangeMusic(musicnum.ordinal(), false);
	}


	public void
	ChangeMusic
	( int			musicnum,
			boolean			looping )
	{
		musicinfo_t	music = null;
		String		namebuf;

		if ( (musicnum <= musicenum_t.mus_None.ordinal())
				|| (musicnum >= musicenum_t.NUMMUSIC.ordinal()) )
		{

			DS.doomSystem.Error("Bad music number %d", musicnum);
		}
		else
			music = sounds.S_music[musicnum];

		if (mus_playing == music)
			return;

		// shutdown old music
		StopMusic();

		// get lumpnum if neccessary
		if (music.lumpnum==0)
		{
			namebuf=String.format("d_%s", music.name);
			music.lumpnum = DS.wadLoader.GetNumForName(namebuf);
		}

		// load & register it
		music.data = DS.wadLoader.CacheLumpNumAsRawBytes(music.lumpnum, Defines.PU_MUSIC);
		music.handle = IMUS.RegisterSong(music.data);

		// play it
		IMUS.PlaySong(music.handle, looping);
		SetMusicVolume(this.snd_MusicVolume);

		mus_playing = music;
	}

	public void StopMusic()
	{
		if (mus_playing!=null)
		{
			if (mus_paused)
				IMUS.ResumeSong(mus_playing.handle);

			IMUS.StopSong(mus_playing.handle);
			IMUS.UnRegisterSong(mus_playing.handle);
			//Z_ChangeTag(mus_playing->data, PU_CACHE);

			mus_playing.data = null;
			mus_playing = null;
		}
	}


	/** This is S_StopChannel. There's another StopChannel
	 *  with a similar contract in ISound. Don't confuse the two.
	 *  
	 * 
	 *  
	 * @param cnum
	 */

	protected void StopChannel(int cnum)
	{

		int		i;
		channel_t	c = channels[cnum];

		// Is it playing?
		if (c.sfxinfo!=null)
		{
			// stop the sound playing
			if (ISND.SoundIsPlaying(c.handle))
			{
				/*#ifdef SAWDEBUG
		    if (c.sfxinfo == &S_sfx[sfx_sawful])
			fprintf(stderr, "stopped\n");
	#endif*/
				ISND.StopSound(c.handle);
			}

			// check to see
			//  if other channels are playing the sound
			for (i=0 ; i<numChannels ; i++)
			{
				if (cnum != i
						&& c.sfxinfo == channels[i].sfxinfo)
				{
					break;
				}
			}

			// degrade usefulness of sound data
			c.sfxinfo.usefulness--;

			c.sfxinfo = null;
		}
	}

	//
	// Changes volume, stereo-separation, and pitch variables
	//  from the norm of a sound effect to be played.
	// If the sound is not audible, returns a 0.
	// Otherwise, modifies parameters and returns 1.
	//
	protected boolean 
	AdjustSoundParams
	( mobj_t	listener,
			ISoundOrigin	source,
			vps_t vps)
	{
		int	approx_dist;
		int	adx;
		int	ady;
		long	angle;

		// calculate the distance to sound origin
		//  and clip it if necessary
		adx = Math.abs(listener.x - source.getX());
		ady = Math.abs(listener.y - source.getY());

		// From _GG1_ p.428. Appox. eucledian distance fast.
		approx_dist = adx + ady - ((adx < ady ? adx : ady)>>1);

		if (DS.gamemap != 8
				&& approx_dist > S_CLIPPING_DIST)
		{
			return false;
		}

		// angle of source to listener
		angle = org.bleachhack.util.doom.rr.RendererState.PointToAngle(listener.x,
				listener.y,
				source.getX(),
				source.getY());

		if (angle > listener.angle)
			angle = angle - listener.angle;
		else
			angle = angle + (0xffffffffL - listener.angle&BITS32);

		angle&=BITS32;
		angle >>= ANGLETOFINESHIFT;

		// stereo separation
		vps.sep = 128 - (FixedMul(S_STEREO_SWING,finesine[(int) angle])>>FRACBITS);

		// volume calculation
		if (approx_dist < S_CLOSE_DIST)
		{
			vps.volume = snd_SfxVolume;
		}
		else if (DS.gamemap == 8)
		{
			if (approx_dist > S_CLIPPING_DIST)
				approx_dist = S_CLIPPING_DIST;

			vps.volume = 15+ ((snd_SfxVolume-15)
					*((S_CLIPPING_DIST - approx_dist)>>FRACBITS))
					/ S_ATTENUATOR;
		}
		else
		{
			// distance effect
			vps.volume = (snd_SfxVolume
					* ((S_CLIPPING_DIST - approx_dist)>>FRACBITS))
					/ S_ATTENUATOR;
			// Let's do some maths here: S_CLIPPING_DIST-approx_dist
			// can be at most 0x04100000. shifting left means 0x0410,
			// or 1040 in decimal. 
			// The unmultiplied max volume is 15, attenuator is 1040.
			// So snd_SfxVolume should be 0-127.
			
		}
		
		// MAES: pitch calculation for doppler effects. Nothing to write
		// home about.
		
		/*
		
		// calculate the relative speed between source and sound origin.
		//  and clip it if necessary
		adx = Math.abs(listener.momx - source.momx);
		ady = Math.abs(listener.momy - source.momy);
			
		// From _GG1_ p.428. Appox. eucledian distance fast.
		// Here used for "approximate speed"
		approx_dist = adx + ady - ((adx < ady ? adx : ady)>>1);
		
		// The idea is that for low speeds, no doppler effect occurs.
		// For higher ones however, a shift occurs. We don't want this
		// to be annoying, so we'll only apply it for large speed differences
		// Then again, Doomguy can sprint like Carl Lewis...
			
		if (approx_dist>0x100000){
		
		// Quickly decide sign of pitch based on speed vectors
			
			// angle of source (speed) to listener (speed)
			angle = rr.RendererState.PointToAngle(listener.momx,
					listener.momy,
					source.momx,
					source.momy);
			
			if ((0<=angle && angle<=Tables.ANG90)||
				(180<=angle && angle<=Tables.ANG270))
		vps.pitch+=(approx_dist>>16);
			else
		vps.pitch-=(approx_dist>>16);
		}

		if (vps.pitch<0) vps.pitch=0;
		if (vps.pitch>255) vps.pitch=255;
		*/
		
		return (vps.volume > 0);
	}




	//
	// S_getChannel :
	//   If none available, return -1.  Otherwise channel #.
	//
	protected int 	getChannel( ISoundOrigin origin,sfxinfo_t	sfxinfo )
	{
		// channel number to use
		int		cnum;

		channel_t	c;

		// Find an open channel
		// If it's null, OK, use that.
		// If it's an origin-specific sound and has the same origin, override.
		for (cnum=0 ; cnum<numChannels ; cnum++)
		{
			if (channels[cnum].sfxinfo==null)
				break;
			else if (origin!=null &&  channels[cnum].origin ==  origin)
			{
				StopChannel(cnum);
				break;
			}
		}

		// None available
		if (cnum == numChannels)
		{
			// Look for lower priority
			for (cnum=0 ; cnum<numChannels ; cnum++)
				if (channels[cnum].sfxinfo.priority >= sfxinfo.priority) break;

			if (cnum == numChannels)
			{
				// FUCK!  No lower priority.  Sorry, Charlie.
				return -1;
			}
			else
			{
				// Otherwise, kick out lower priority.
				StopChannel(cnum);
			}
		}

		c = channels[cnum];

		// channel is decided to be cnum.
		c.sfxinfo = sfxinfo;
		c.origin = origin;

		return cnum;
	}	

	/** Nice one. A sound should have a maximum duration in tics,
	 * and we can give it a handle proportional to the future tics
	 * it should play until. Ofc, this means the minimum timeframe
	 * for cutting a sound off is just 1 tic.
	 * 
	 * @param handle
	 * @return
	 */

	/*
	public boolean SoundIsPlaying(int handle)
	{
	    // Ouch.
	    return (DS.gametic < handle);
	} */



}

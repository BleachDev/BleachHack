package org.bleachhack.util.doom.s;

import static org.bleachhack.util.doom.data.sounds.S_sfx;
import org.bleachhack.util.doom.data.sounds.sfxenum_t;
import org.bleachhack.util.doom.doom.DoomMain;
import java.util.Collection;
import java.util.HashMap;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;
import javax.sound.sampled.LineUnavailableException;

/** Experimental Clip based driver. It does work, but it has no
 *  tangible advantages over the Audioline or Classic one. If the
 *  Audioline can be used, there's no reason to fall back to this 
 *  one.
 * 
 * KNOWN ISSUES:
 * 
 * a) Same general restrictions as audiolines (in fact, Clips ARE Audioline 
 *    in disguise)
 * b) Multiple instances of the same sound require multiple clips, so
 *    even caching them is a half-baked solution, and if you have e.g. 40 imps
 *    sound in a room.... 
 *    
 *    
 *  Currently unused.
 * 
 * @author Velktron
 *
 */

public class ClipSFXModule extends AbstractSoundDriver{
	
	HashMap<Integer,Clip> cachedSounds = new HashMap<Integer,Clip>();

	
	// Either it's null (no clip is playing) or non-null (some clip is playing).
	Clip[] channels;

	
	public final float[] linear2db;
	
	public ClipSFXModule(DoomMain<?, ?> DM, int numChannels) {
		super(DM,numChannels);
		linear2db=computeLinear2DB();		
		}
	
    private float[] computeLinear2DB() {
    	
    	// Maximum volume is 0 db, minimum is ... -96 db.
    	// We rig this so that half-scale actually gives quarter power,
    	// and so is -6 dB.
    	float[] tmp=new float[VOLUME_STEPS];
    	
    	for (int i=0;i<VOLUME_STEPS;i++){
    		float linear=(float)(20*Math.log10((float)i/(float)VOLUME_STEPS));
    		// Hack. The minimum allowed value as of now is -80 db.
    		if (linear<-36.0) linear=-36.0f;
    		tmp[i]= linear;
    		
    	}
    		
    		
    		
		return tmp;
	}



	@Override
	public boolean InitSound() {
        // Secure and configure sound device first.
        System.err.println("I_InitSound: ");

        // We don't actually do this here (will happen only when we
        // create the first audio clip).

        // Initialize external data (all sounds) at start, keep static.

        initSound16();

        System.err.print(" pre-cached all sound data\n");
        // Finished initialization.
        System.err.print("I_InitSound: sound module ready\n");
        return true;

    }


/** Modified getsfx. The individual length of each sfx is not of interest.
 * However, they must be transformed into 16-bit, signed, stereo samples
 * beforehand, before being "fed" to the audio clips.
 * 
 * @param sfxname
 * @param index
 * @return
 */
	 protected byte[] getsfx(String sfxname,int index) {
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
	        
	        paddedsize = (size-8)*2*2;
	        // Allocate from zone memory.
	        paddedsfx = new byte[paddedsize];

	        // Skip first 8 bytes (header), blow up the data
	        // to stereo, BIG ENDIAN, SIGNED, 16 bit. Don't expect any fancy DSP here!

	        int sample=0;
	        for (i = 8; i < size; i++){
	        	// final short sam=(short) vol_lookup[127][0xFF&sfx[i]];
	        	final short sam=(short) ((0xFF&sfx[i]-128)<<8);
	            paddedsfx[sample++] = (byte) (0xFF&(sam>>8));
	            paddedsfx[sample++]=(byte) (0xFF&sam);
	            paddedsfx[sample++]=(byte) (0xFF&(sam>>8));
	            paddedsfx[sample++]=(byte) (0xFF&sam);
	        }
	        
	        // Remove the cached lump.
	        DM.wadLoader.UnlockLumpNum(sfxlump);

	        // Return allocated padded data.
	        // So the first 8 bytes are useless?
	        return paddedsfx;
	    }

	@Override
	public void UpdateSound() {
		// We do nothing here, since the mixing is delegated to the OS
		// Just hope that it's more efficient that our own...
		
	}

	@Override
	public void SubmitSound() {
		// Dummy. Nothing actual to do here.
		
	}

	@Override
	public void ShutdownSound() {
		 // Wait till all pending sounds are finished.
		  boolean done = false;
		  int i;
		  

		  // FIXME (below).
		  //fprintf( stderr, "I_ShutdownSound: NOT finishing pending sounds\n");
		  //fflush( stderr );
		  
		  while ( !done)
		  {
		    for( i=0 ; i<numChannels && ((channels[i]==null)||(!channels[i].isActive())) ; i++);
		    // FIXME. No proper channel output.
		    if (i==numChannels)  done=true;
		  }
		  
		  for( i=0 ; i<numChannels; i++){
			  if (channels[i]!=null)
			channels[i].close();			
		  	}
		  
		  // Free up resources taken up by cached clips.
		  Collection<Clip> clips=this.cachedSounds.values();
		  for (Clip c:clips){
			  c.close();
		  }
		  
		  // Done.
		  return;
		
	}

	@Override
	public void SetChannels(int numChannels) {
		channels= new Clip[numChannels];
	}
	
	private final void  getClipForChannel(int c, int sfxid){
		
		// Try to see if we already have such a clip.
		Clip clip=this.cachedSounds.get(sfxid);
		
		boolean exists=false;
		
		// Does it exist?
		if (clip!=null){
			
			// Well, it does, but we are not done yet.
			exists=true;
			// Is it NOT playing already?
			if (!clip.isActive()){
				// Assign it to the channel.
				channels[c]=clip;
				return;
			}
		}
		
		// Sorry, Charlie. Gotta make a new one.
		DataLine.Info info = new DataLine.Info(Clip.class, DoomSound.DEFAULT_SAMPLES_FORMAT);
		
		try {
			clip = (Clip) AudioSystem.getLine(info);
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    try {
			clip.open( DoomSound.DEFAULT_SAMPLES_FORMAT, S_sfx[sfxid].data, 0, S_sfx[sfxid].data.length);
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (!exists)
		this.cachedSounds.put(sfxid,clip);
		
	    channels[c]=clip;
	    
		
	   // Control[] cs=clip.getControls();
	   // 
	   // for (Control cc:cs){
	   // 	System.out.println("Control "+cc.getType().toString());
       // 		}
	}
	
	//
	// This function adds a sound to the
	//  list of currently active sounds,
	//  which is maintained as a given number
	//  (eight, usually) of internal channels.
	// Returns a handle.
	//
	protected short	handlenums = 0;

	protected int addsfx 	( int sfxid,int		volume,int pitch,int		seperation)
	{
		int		i;
		int		rc = -1;

		int		oldest = DM.gametic;
		int		oldestnum = 0;
		int		slot;

		// Chainsaw troubles.
		// Play these sound effects only one at a time.
		if ( sfxid == sfxenum_t.sfx_sawup.ordinal()
				|| sfxid == sfxenum_t.sfx_sawidl.ordinal()
				|| sfxid == sfxenum_t.sfx_sawful.ordinal()
				|| sfxid == sfxenum_t.sfx_sawhit.ordinal()
				|| sfxid == sfxenum_t.sfx_stnmov.ordinal()
				|| sfxid == sfxenum_t.sfx_pistol.ordinal()	 )
		{
			// Loop all channels, check.
			for (i=0 ; i<numChannels ; i++)
			{
				// Active, and using the same SFX?
				if (channels[i]!=null && channels[i].isRunning()
						&& channelids[i] == sfxid)
				{
					// Reset.
					channels[i].stop();
					// We are sure that iff,
					//  there will only be one.
					break;
				}
			}
		}

		// Loop all channels to find oldest SFX.
		for (i=0; (i<numChannels) && (channels[i]!=null); i++)
		{
			if (channelstart[i] < oldest)
			{
				oldestnum = i;
				oldest = channelstart[i];
			}
		}

		// Tales from the cryptic.
		// If we found a channel, fine.
		// If not, we simply overwrite the first one, 0.
		// Probably only happens at startup.
		if (i == numChannels)
			slot = oldestnum;
		else
			slot = i;

		// Okay, in the less recent channel,
		//  we will handle the new SFX.
		
		// We need to decide whether we can reuse an existing clip
		// or create a new one. In any case, when this method return 
		// we should have a valid clip assigned to channel "slot".

        getClipForChannel(slot,sfxid);

        
		// Reset current handle number, limited to 0..100.
		if (handlenums==0) // was !handlenums, so it's actually 1...100?
			handlenums = MAXHANDLES;

		// Assign current handle number.
		// Preserved so sounds could be stopped (unused).
		channelhandles[slot]= rc = handlenums--;

		// Should be gametic, I presume.
		channelstart[slot] = DM.gametic;

		// Get the proper lookup table piece
		//  for this volume level???
		//channelleftvol_lookup[slot] = vol_lookup[leftvol];
		//channelrightvol_lookup[slot] = vol_lookup[rightvol];

		// Preserve sound SFX id,
		//  e.g. for avoiding duplicates of chainsaw.
		channelids[slot] = sfxid;

		setVolume(slot,volume);
		setPanning(slot,seperation);
		//channels[slot].addSound(sound, handlenums);
		//channels[slot].setPitch(pitch);
		
		if(D) System.err.println(channelStatus());
        if(D) System.err.printf("Playing %d vol %d on channel %d\n",rc,volume,slot);
		// Well...play it.
      
        // FIXME VERY BIG PROBLEM: stop() is blocking!!!! WTF ?!
        //channels[slot].stop();
        //long  a=System.nanoTime();
        channels[slot].setFramePosition(0);
		channels[slot].start();
		// b=System.nanoTime();
		//System.err.printf("Sound playback completed in %d\n",(b-a));
        
        // You tell me.
		return rc;
	}
	
	
	/** Accepts volume in "Doom" format (0-127).
	 * 
	 * @param volume
	 */
	public void setVolume(int chan,int volume){
		Clip c=channels[chan];
		
		if (c.isControlSupported(Type.MASTER_GAIN)){
			FloatControl vc=(FloatControl) c.getControl(Type.MASTER_GAIN);
				float vol = linear2db[volume];
				vc.setValue(vol);
				}
			else if (c.isControlSupported(Type.VOLUME)){
				FloatControl vc=(FloatControl) c.getControl(Type.VOLUME);
				float vol = vc.getMinimum()+(vc.getMaximum()-vc.getMinimum())*(float)volume/127f;
				vc.setValue(vol);
			}
		}
	
	public void setPanning(int chan,int sep){
		Clip c=channels[chan];
		
		if (c.isControlSupported(Type.PAN)){
			FloatControl bc=(FloatControl) c.getControl(Type.PAN);
			// Q: how does Doom's sep map to stereo panning?
			// A: Apparently it's 0-255 L-R.
			float pan= bc.getMinimum()+(bc.getMaximum()-bc.getMinimum())*(float)sep/ISoundDriver.PANNING_STEPS;
			bc.setValue(pan);
			}
		}
	
	@Override
	public void StopSound(int handle) {
		// Which channel has it?
		int  hnd=getChannelFromHandle(handle);
		if (hnd>=0) {
			channels[hnd].stop();
			channels[hnd]=null;
		}
	}

	@Override
	public boolean SoundIsPlaying(int handle) {
		
		return getChannelFromHandle(handle)!=BUSY_HANDLE;
		}

	
	@Override
	public void UpdateSoundParams(int handle, int vol, int sep, int pitch) {
		
		// This should be called on sounds that are ALREADY playing. We really need
		// to retrieve channels from their handles.
		
		//System.err.printf("Updating sound with handle %d vol %d sep %d pitch %d\n",handle,vol,sep,pitch);
		
		int i=getChannelFromHandle(handle);
		// None has it?
		if (i!=BUSY_HANDLE){
			//System.err.printf("Updating sound with handle %d in channel %d\n",handle,i);
			setVolume(i,vol);
			setPanning(i,sep);
			//channels[i].setPanning(sep);
			}
		
	}
	
	
	/** Internal use. 
	 * 
	 * @param handle
	 * @return the channel that has the handle, or -2 if none has it.
	 */
	private int getChannelFromHandle(int handle){
		// Which channel has it?
		for (int i=0;i<numChannels;i++){
			if (channelhandles[i]==handle) return i;
		}
		
		return BUSY_HANDLE;
	}

		StringBuilder sb=new StringBuilder();
	
		public String channelStatus(){
			sb.setLength(0);
			for (int i=0;i<numChannels;i++){
				if (channels[i]!=null && channels[i].isActive())
				sb.append(i);
				else sb.append('-');
			}
			
			return sb.toString();
			
			
		}
	
}


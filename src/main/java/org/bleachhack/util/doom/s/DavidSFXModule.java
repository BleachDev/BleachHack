package org.bleachhack.util.doom.s;

import org.bleachhack.util.doom.data.sounds;
import org.bleachhack.util.doom.data.sounds.sfxenum_t;
import org.bleachhack.util.doom.doom.DoomMain;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/** David Martel's sound driver for Mocha Doom. Excellent work!
 * 
 *  However, it's based on Java Audiolines, and as such has a number
 *  of drawbacks:
 *  
 * a) Sounds are forcibly blown to be stereo, 16-bit otherwise it's 
 *    impossible to get panning controls.
 * b) Volume, master gain, panning, pitch etc. controls are NOT guaranteed
 *    to be granted across different OSes , and your mileage may vary. It's
 *    fairly OK under Windows and OS X, but Linux is very clunky. The only
 *    control that is -somewhat- guaranteed is the volume one.
 * c) Spawns as many threads as channels. Even if semaphore waiting it used,
 *    that can be taxing for slower systems.

 * 
 * @author David
 * @author Velktron
 *
 */

public class DavidSFXModule extends AbstractSoundDriver{
	
	ArrayList<DoomSound> cachedSounds=new ArrayList<DoomSound>();
	
	public final float[] linear2db;	
	
	private SoundWorker[] channels;
	private Thread[] soundThread;
	
	public DavidSFXModule(DoomMain<?, ?> DM,int numChannels) {
		super(DM,numChannels);
		linear2db=computeLinear2DB();
		
		}
	
    private float[] computeLinear2DB() {
    	
    	// Maximum volume is 0 db, minimum is ... -96 db.
    	// We rig this so that half-scale actually gives quarter power,
    	// and so is -6 dB.
    	float[] tmp=new float[VOLUME_STEPS];
    	
    	for (int i=0;i<VOLUME_STEPS;i++){
    		float linear=(float)(10*Math.log10((float)i/(float)VOLUME_STEPS));
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

        // Initialize external data (all sounds) at start, keep static.

        initSound16();

        // Cache sounds internally so they can be "fed" to AudioLine threads later.
        // These can be more than the usual built-in sounds.
        
        
        for (int i=0;i<sounds.S_sfx.length;i++){
        	DoomSound tmp=new DoomSound(sounds.S_sfx[i],DoomSound.DEFAULT_SAMPLES_FORMAT);
        	cachedSounds.add(tmp);	
        	}
        
        System.err.print(" pre-cached all sound data\n");
        // Finished initialization.
        System.err.print("I_InitSound: sound module ready\n");
        
        return true;
		
	}

	@Override
	public void UpdateSound() {
		// In theory, we should update volume + panning for each active channel.
		// Ouch. Ouch Ouch.
		
	}

	@Override
	public void SubmitSound() {
		// Sound should be submitted to the sound threads, which they pretty much
		// do themselves.
		
	}

	@Override
	public void ShutdownSound() {
		 // Wait till all pending sounds are finished.
		  boolean done = false;
		  int i;
		  
		  while ( !done)
		  {
		    for( i=0 ; i<numChannels && !(channels[i].isPlaying()) ; i++);
		    if (i==numChannels)  done=true;
		  }
		  
		  for( i=0 ; i<numChannels; i++){
			channels[i].terminate=true;  
			channels[i].wait.release();
			try {
				this.soundThread[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		  // Done.
		  return;
		
	}

	@Override
	public void SetChannels(int numChannels) {

		channels= new SoundWorker[numChannels];
		soundThread= new Thread[numChannels];	
		
		// This is actually called from IDoomSound.
		for (int i = 0; i < numChannels; i++) {
			channels[i]=new SoundWorker(i);
			soundThread[i] = new Thread(channels[i]);
			soundThread[i].start();
		}
		
	}
	
	/** This one will only create datalines for common clip/audioline samples
	 *  directly.
	 * 
	 * @param c
	 * @param sfxid
	 */
	private final void  createDataLineForChannel(int c, int sfxid){
		
		// None? Make a new one.
		
		if (channels[c].auline == null) {
        	try {
        		DoomSound tmp=cachedSounds.get(sfxid);
        		// Sorry, Charlie. Gotta make a new one.
        		DataLine.Info info = new DataLine.Info(SourceDataLine.class, DoomSound.DEFAULT_SAMPLES_FORMAT);
				channels[c].auline = (SourceDataLine) AudioSystem.getLine(info);
				channels[c].auline.open(tmp.format);
			} catch (LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					boolean errors=false;
        			// Add individual volume control.
        			if (channels[c].auline.isControlSupported(Type.MASTER_GAIN))
        				channels[c].vc=(FloatControl) channels[c].auline
        				.getControl(Type.MASTER_GAIN);
        			else {
        			System.err.print("MASTER_GAIN, ");
        			errors=true;
        			if (channels[c].auline.isControlSupported(Type.VOLUME))
            				channels[c].vc=(FloatControl) channels[c].auline
            				.getControl(Type.VOLUME);
        			else 
        				System.err.print("VOLUME, ");
        			} 
        			

        			// Add individual pitch control.
        			if (channels[c].auline.isControlSupported(Type.SAMPLE_RATE)){
        				channels[c].pc=(FloatControl) channels[c].auline
        				.getControl(Type.SAMPLE_RATE);
        			} else {
        				errors=true;
        				System.err.print("SAMPLE_RATE, ");
        			} 
        			
        			// Add individual pan control
        			if (channels[c].auline.isControlSupported(Type.BALANCE)){
        				channels[c].bc=(FloatControl) channels[c].auline
        				.getControl(FloatControl.Type.BALANCE);
        			} else {
        				System.err.print("BALANCE, ");
        				errors=true;
        				if (channels[c].auline.isControlSupported(Type.PAN)){        					
        				channels[c].bc=(FloatControl) channels[c].auline
        				.getControl(FloatControl.Type.PAN);
        			} else {
        				System.err.print("PANNING ");
        				}
        			}

        			if (errors) System.err.printf("for channel %d NOT supported!\n",c);
        			
        			channels[c].auline.start();
        		}
	}

	/* UNUSED version, designed to work on any type of sample (in theory).
	   Requires a DoomSound container for separate format information.
	  
	 private final void  createDataLineForChannel(int c, DoomSound sound){
		if (channels[c].auline == null) {
        	AudioFormat format = sound.ais.getFormat();
        	DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        	try {
				channels[c].auline = (SourceDataLine) AudioSystem.getLine(info);
				channels[c].auline.open(format);
			} catch (LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        			// Add individual volume control.
        			if (channels[c].auline.isControlSupported(Type.MASTER_GAIN))
        				channels[c].vc=(FloatControl) channels[c].auline
        				.getControl(Type.MASTER_GAIN);
        			else {
        			System.err.printf("MASTER_GAIN for channel %d NOT supported!\n",c);
        			if (channels[c].auline.isControlSupported(Type.VOLUME))
            				channels[c].vc=(FloatControl) channels[c].auline
            				.getControl(Type.VOLUME);
        			else 
        				System.err.printf("VOLUME for channel %d NOT supported!\n",c);
        			} 
        			

        			// Add individual pitch control.
        			if (channels[c].auline.isControlSupported(Type.SAMPLE_RATE)){
        				channels[c].pc=(FloatControl) channels[c].auline
        				.getControl(Type.SAMPLE_RATE);
        			} else {
        				System.err.printf("SAMPLE_RATE for channel %d NOT supported!\n",c);
        			} 
        			
        			// Add individual pan control (TODO: proper positioning).
        			if (channels[c].auline.isControlSupported(Type.BALANCE)){
        				channels[c].bc=(FloatControl) channels[c].auline
        				.getControl(FloatControl.Type.BALANCE);
        			} else {
        				System.err.printf("BALANCE for channel %d NOT supported!\n",c);
        				if (channels[c].auline.isControlSupported(Type.PAN)){        					
        				channels[c].bc=(FloatControl) channels[c].auline
        				.getControl(FloatControl.Type.PAN);
        			} else {
        				System.err.printf("PAN for channel %d NOT supported!\n",c);
        			}
        			}

        			channels[c].auline.start();
        		}
	}
	*/
	
	@Override
	protected int addsfx( int sfxid,int volume,int pitch, int seperation)
	{
		int		i;
		int		rc = -1;

		int		oldest = DM.gametic;
		int		oldestnum = 0;
		int		slot;

		int		rightvol;
		int		leftvol;

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
				if ( (channels[i].isPlaying())
						&& (channelids[i] == sfxid) )
				{
					// Reset.
					channels[i].stopSound();
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
		// Set pointer to raw data.
	      
        // Create a dataline for the "lucky" channel,
		// or reuse an existing one if it exists.
        createDataLineForChannel(slot,sfxid);

		// Reset current handle number, limited to 0..100.
		if (handlenums==0) // was !handlenums, so it's actually 1...100?
			handlenums = MAXHANDLES;

		// Assign current handle number.
		// Preserved so sounds could be stopped (unused).
		channelhandles[slot]= rc = handlenums--;
		channelstart[slot] = DM.gametic;

		// Separation, that is, orientation/stereo.
		//  range is: 1 - 256
		seperation += 1;

		// Per left/right channel.
		//  x^2 seperation,
		//  adjust volume properly.
		leftvol =
			volume - ((volume*seperation*seperation) >> 16); ///(256*256);
		seperation = seperation - 257;
		rightvol =
			volume - ((volume*seperation*seperation) >> 16);	


		// Sanity check, clamp volume.

		if (rightvol < 0 || rightvol > 127)
			DM.doomSystem.Error("rightvol out of bounds");

		if (leftvol < 0 || leftvol > 127)
			DM.doomSystem.Error("leftvol out of bounds"); 

		// Preserve sound SFX id,
		//  e.g. for avoiding duplicates of chainsaw.
		channelids[slot] = sfxid;

		channels[slot].setVolume(volume);
		channels[slot].setPanning(seperation+256);
		channels[slot].addSound(cachedSounds.get(sfxid).data, handlenums);
		channels[slot].setPitch(pitch);
		
		if (D) System.err.println(channelStatus());
        if (D) System.err.printf("Playing %d vol %d on channel %d\n",rc,volume,slot);
		// You tell me.
		return rc;
	}
	
	@Override
	public void StopSound(int handle) {
		// Which channel has it?
		int  hnd=getChannelFromHandle(handle);
		if (hnd>=0) 
			channels[hnd].stopSound();
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
			channels[i].setVolume(vol);
			channels[i].setPitch(pitch);
			channels[i].setPanning(sep);
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
	
	/** A Thread for playing digital sound effects.
	 * 
	 *  Obviously you need as many as channels?
	 *   
	 *  In order not to end up in a hell of effects,
	 *  certain types of sounds must be limited to 1 per object.
	 *
	 */


	private class SoundWorker implements Runnable {
			public Semaphore wait; // Holds the worker still until there's a new sound
			FloatControl vc; // linear volume control
			FloatControl bc; // balance/panning control
			FloatControl pc; // pitch control
			byte[] currentSoundSync;
			byte[] currentSound;
			
			public SoundWorker(int id){
				this.id=id;
				this.handle=IDLE_HANDLE;
				wait=new Semaphore(1);
			}
			

			int id;
			/** Used to find out whether the same object is continuously making
			 *  sounds. E.g. the player, ceilings etc. In that case, they must be
			 *  interrupted.
			 */
			int handle;
			public boolean terminate;
			SourceDataLine auline;
			
			/** This is how you tell the thread to play a sound,
			 * I suppose.  */
			
			public void addSound(byte[] ds, int handle) {
				
				if (D) System.out.printf("Added handle %d to channel %d\n",handle,id);
				this.handle=handle;
				this.currentSound=ds;
				this.auline.stop();
				this.auline.start();
				this.wait.release();
				
			}

			/** Accepts volume in "Doom" format (0-127).
			 * 
			 * @param volume
			 */
			public void setVolume(int volume){
				if (vc!=null){
					if (vc.getType()==FloatControl.Type.MASTER_GAIN) {
						float vol = linear2db[volume];
						vc.setValue(vol);
						}
					else if (vc.getType()==FloatControl.Type.VOLUME){
						float vol = vc.getMinimum()+(vc.getMaximum()-vc.getMinimum())*(float)volume/127f;
						vc.setValue(vol);
					}
				}
				}
			
			public void setPanning(int sep){
				// Q: how does Doom's sep map to stereo panning?
				// A: Apparently it's 0-255 L-R.
				if (bc!=null){
				float pan= bc.getMinimum()+(bc.getMaximum()-bc.getMinimum())*(float)(sep)/ISoundDriver.PANNING_STEPS;
				//System.err.printf("Panning %d %f %f %f\n",sep,bc.getMinimum(),bc.getMaximum(),pan);
				bc.setValue(pan);
				}
			}
			
			/** Expects a steptable value between 16K and 256K, with
			 *  64K being the middle.
			 * 
			 * @param pitch
			 */
			public void setPitch(int pitch){
				if (pc!=null){
				float pan= (float) (pc.getValue()*((float)pitch/65536.0));
				pc.setValue(pan);
				}
			}
			
			public void run() {
				System.err.printf("Sound thread %d started\n",id);
				while (!terminate) {
					currentSoundSync = currentSound;
					if (currentSoundSync != null) {

						try {
							auline.write(currentSoundSync, 0, currentSoundSync.length);
						} catch (Exception e) { 
							e.printStackTrace();
							return;
						} finally {
							// The previous steps are actually VERY fast.
							// However this one waits until the data has been
							// consumed, Interruptions/signals won't reach  here,
							// so it's pointless trying to interrupt the actual filling.
							//long a=System.nanoTime();
							auline.drain();
							//long b=System.nanoTime();
							//System.out.printf("Channel %d completed in %f.\n",id,(float)(b-a)/1000000000f);
							}
						// Report that this channel is free.
						currentSound = null;
						// Remove its handle.
						
						//System.out.printf("Channel  %d with handle %d done. Marking as free\n",id,handle);
						if (handle>0)
						channelhandles[this.id]=IDLE_HANDLE;
						this.handle=IDLE_HANDLE;
					}

					// If we don't sleep at least a bit here, busy waiting becomes
					// way too taxing. Waiting on a semaphore (triggered by adding a new sound)
					// seems like a better method.

					try {
						wait.acquire();
					} catch (InterruptedException e) {
					} 
				}
			}

			public void stopSound() {
					auline.stop();
					auline.flush();
					//System.out.printf("Channel %d with handle %d interrupted. Marking as free\n",id,handle);
					channelhandles[this.id]=IDLE_HANDLE;
					this.handle=IDLE_HANDLE;
					currentSound = null;
					auline.start();
					}

			public boolean isPlaying() {
				//System.out.printf("Channel %d with handle %d queried\n",id,handle);
				return (this.handle!=IDLE_HANDLE||this.currentSound!=null);
			}

		}

		StringBuilder sb=new StringBuilder();
	
		public String channelStatus(){
			sb.setLength(0);
			for (int i=0;i<numChannels;i++){
				if (channels[i].isPlaying())
				sb.append(i);
				else sb.append('-');
			}
			
			return sb.toString();
			
			
		}
	
}


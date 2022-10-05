package org.bleachhack.util.doom.s;

import static org.bleachhack.util.doom.data.sounds.S_sfx;
import org.bleachhack.util.doom.data.sounds.sfxenum_t;
import org.bleachhack.util.doom.doom.DoomMain;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import org.bleachhack.util.doom.pooling.AudioChunkPool;

/**
 * A spiffy new sound system, based on the Classic sound driver.
 * It is entirely asynchronous (runs in its own thread) and even has its own timer.
 * This allows it to continue mixing even when the main loop is not responding 
 * (something which, arguably, could be achieved just with a timer calling
 * UpdateSound and SubmitSound). Uses message passing to deliver channel status
 * info, and mixed audio directly without using an intermediate buffer,
 * saving memory bandwidth.
 * 
 * PROS:
 * a) All those of ClassicSoundDriver plus:
 * b) Continues normal playback even under heavy CPU load, works smoother
 *    even on lower powered CPUs.
 * c) More efficient due to less copying of audio blocks.
 * c) Fewer audio glitches compared to ClassicSoundDriver.
 * 
 * CONS:
 * a) All those of ClassicSoundDriver plus regarding timing accuracy.
 * 
 * @author Maes
 */

public class SuperDoomSoundDriver extends AbstractSoundDriver {

    protected final Semaphore produce;

    protected final Semaphore consume;

    protected final Semaphore update_mixer;

    protected int chunk = 0;

    //protected FileOutputStream fos;
    //protected DataOutputStream dao;

    // The one and only line
    protected SourceDataLine line = null;

    protected HashMap<Integer, byte[]> cachedSounds =
        new HashMap<Integer, byte[]>();

    protected final Timer MIXTIMER;
        
    public SuperDoomSoundDriver(DoomMain<?, ?> DM, int numChannels) {
    	super(DM,numChannels);
        channels = new boolean[numChannels];
        produce = new Semaphore(1);
        consume = new Semaphore(1);
        update_mixer = new Semaphore(1);
        produce.drainPermits();
        update_mixer.drainPermits();
        this.MIXSRV=new MixServer(numChannels);
        MIXTIMER= new Timer(true);
        // Sound tics every 1/35th of a second. Grossly
        // inaccurate under Windows though, will get rounded
        // down to the closest multiple of 15 or 16 ms.
        MIXTIMER.schedule(new SoundTimer(), 0,SOUND_PERIOD);        
    }




    /** These are still defined here to decouple them from the mixer's 
     *  ones, however they serve  more as placeholders/status indicators;
     */
    protected volatile boolean[] channels;

    protected volatile boolean mixed = false;

    /**
     * This function loops all active (internal) sound channels, retrieves a
     * given number of samples from the raw sound data, modifies it according to
     * the current (internal) channel parameters, mixes the per channel samples
     * into the global mixbuffer, clamping it to the allowed range, and sets up
     * everything for transferring the contents of the mixbuffer to the (two)
     * hardware channels (left and right, that is). This function currently
     * supports only 16bit.
     */

    public void UpdateSound() {
    	// This is pretty much a dummy.
    	// The mixing thread goes on by itself, guaranteeing that it will
    	// carry out at least currently enqueued mixing messages, regardless
    	// of how badly the engine lags.

    }

    /**
     * SFX API Note: this was called by S_Init. However, whatever they did in
     * the old DPMS based DOS version, this were simply dummies in the Linux
     * version. See soundserver initdata().
     */

    @Override
    public void SetChannels(int numChannels) {
        // Init internal lookups (raw data, mixing buffer, channels).
        // This function sets up internal lookups used during
        // the mixing process.

        int steptablemid = 128;

        // Okay, reset internal mixing channels to zero.
        for (int i = 0; i < this.numChannels; i++) {
            channels[i] = false;
        }
        
        generateStepTable(steptablemid);

        generateVolumeLUT();
    }
    
    protected  PlaybackServer SOUNDSRV;
    protected final MixServer MIXSRV;
    
    protected Thread MIXTHREAD;
    protected Thread SOUNDTHREAD;

    @Override
    public boolean InitSound() {

        // Secure and configure sound device first.
        System.err.print("I_InitSound: ");

        // We only need a single data line.
        // PCM, signed, 16-bit, stereo, 22025 KHz, 2048 bytes per "frame",
        // maximum of 44100/2048 "fps"
        AudioFormat format = new AudioFormat(SAMPLERATE, 16, 2, true, true);

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        if (AudioSystem.isLineSupported(info))
            try {
                line = (SourceDataLine) AudioSystem.getSourceDataLine(format);
                line.open(format, AUDIOLINE_BUFFER);
            }	catch (Exception e) {
                e.printStackTrace();
                System.err.print("Could not play signed 16 data\n");
                return false;
            }

        if (line != null) {
            System.err.print("configured audio device\n");
            line.start();
        } else {
        	 System.err.print("could not configure audio device\n");
        	 return false;
        }

        SOUNDSRV = new PlaybackServer(line);
        SOUNDTHREAD = new Thread(SOUNDSRV);
        SOUNDTHREAD.setDaemon(true);
        SOUNDTHREAD.start();
        // Vroom!        
        MIXTHREAD= new Thread(MIXSRV);
        MIXTHREAD.setDaemon(true);
        MIXTHREAD.start();
        
        // Initialize external data (all sounds) at start, keep static.
        System.err.print("I_InitSound: ");

        super.initSound8();

        System.err.print("pre-cached all sound data\n");

        // Finished initialization.
        System.err.print("I_InitSound: sound module ready\n");
        
        return true;

    }

    @Override
    protected int addsfx(int sfxid, int volume, int step, int seperation) {
        int i;
        int rc = -1;

        int oldest = DM.gametic;
        int oldestnum = 0;
        int slot;

        int rightvol;
        int leftvol;

        int broken=-1;
        
        // Chainsaw troubles.
        // Play these sound effects only one at a time.
        if (sfxid == sfxenum_t.sfx_sawup.ordinal()
                || sfxid == sfxenum_t.sfx_sawidl.ordinal()
                || sfxid == sfxenum_t.sfx_sawful.ordinal()
                || sfxid == sfxenum_t.sfx_sawhit.ordinal()
                || sfxid == sfxenum_t.sfx_stnmov.ordinal()
                || sfxid == sfxenum_t.sfx_pistol.ordinal()) {
            // Loop all channels, check.
            for (i = 0; i < numChannels; i++) {
                // Active, and using the same SFX?
                if (channels[i] && (channelids[i] == sfxid)) {
                    // Reset.
                	
                	MixMessage m=new MixMessage();
                	m.stop=true;
                	
                    // We are sure that iff,
                    // there will only be one.
                    broken=i;
                    break;
                }
            }
        }

        // Loop all channels to find oldest SFX.
        if (broken>=0) {
        	i=broken;
        	oldestnum=broken;
        }
        else
        for (i = 0; (i < numChannels) && channels[i]; i++) {
            if (channelstart[i] < oldest) {
                oldestnum = i;
            }
        }

        oldest = channelstart[oldestnum];
        
        // Tales from the cryptic.
        // If we found a channel, fine.
        // If not, we simply overwrite the first one, 0.
        // Probably only happens at startup.
        if (i == numChannels)
            slot = oldestnum;
        else
            slot = i;

        
        MixMessage m=new MixMessage();
        
        // Okay, in the less recent channel,
        // we will handle the new SFX.
        // Set pointer to raw data.
        channels[slot]=true;
        m.channel=slot;
        m.data=S_sfx[sfxid].data;

        // MAES: if you don't zero-out the channel pointer here, it gets ugly
        m.pointer= 0;

        // Set pointer to end of raw data.
        m.end = lengths[sfxid];

        // Reset current handle number, limited to 0..100.
        if (handlenums == 0) // was !handlenums, so it's actually 1...100?
            handlenums = 100;

        // Assign current handle number.
        // Preserved so sounds could be stopped (unused).
        // Maes: this should really be decreasing, otherwide handles
        // should start at 0 and go towards 100. Just saying.
        channelhandles[slot] = rc = handlenums--;

        // Set stepping???
        // Kinda getting the impression this is never used.
        // MAES: you're wrong amigo.
        m.step= step;
        // ???
        m.remainder = 0;
        // Should be gametic, I presume.
        channelstart[slot] = DM.gametic;

        // Separation, that is, orientation/stereo.
        // range is: 1 - 256
        seperation += 1;

        // Per left/right channel.
        // x^2 seperation,
        // adjust volume properly.
        leftvol = volume - ((volume * seperation * seperation) >> 16); // /(256*256);
        seperation = seperation - 257;
        rightvol = volume - ((volume * seperation * seperation) >> 16);

        // Sanity check, clamp volume.

        if (rightvol < 0 || rightvol > 127)
            DM.doomSystem.Error("rightvol out of bounds");

        if (leftvol < 0 || leftvol > 127)
            DM.doomSystem.Error("leftvol out of bounds");

        // Get the proper lookup table piece
        // for this volume level???
        m.leftvol_lookup = vol_lookup[leftvol];
        m.rightvol_lookup = vol_lookup[rightvol];

        // Preserve sound SFX id,
        // e.g. for avoiding duplicates of chainsaw.
        channelids[slot] = sfxid;

        if (D) System.err.println(channelStatus());
        if (D) System.err.printf(
                "Playing sfxid %d handle %d length %d vol %d on channel %d\n",
                sfxid, rc, S_sfx[sfxid].data.length, volume, slot);

        
        MIXSRV.submitMixMessage(m);
        
        // You tell me.
        return rc;
    }

    @Override
    public void ShutdownSound() {

        boolean done;

        // Unlock sound thread if it's waiting.
        produce.release();
        update_mixer.release();

        int i=0;
        do {
        	done=true;
            for (i=0; i < numChannels; i++) {
            	// If even one channel is playing, loop again.
            	done&=!channels[i];            	
            	}
            	//System.out.println(done+" "+this.channelStatus());
            	
            } while (!done);
        
        
        this.line.flush();
        
        
        SOUNDSRV.terminate = true;
        MIXSRV.terminate = true;
        produce.release();
        update_mixer.release();
        try {
            SOUNDTHREAD.join();
            MIXTHREAD.join();
        } catch (InterruptedException e) {
        	// Well, I don't care.
        }
        System.err.printf("3\n");
        line.close();
        System.err.printf("4\n");

    }

    protected class PlaybackServer
            implements Runnable {

        public boolean terminate = false;

        public PlaybackServer(SourceDataLine line) {
            this.auline = line;
        }

        private SourceDataLine auline;

        private ArrayBlockingQueue<AudioChunk> audiochunks =
            new ArrayBlockingQueue<AudioChunk>(BUFFER_CHUNKS * 2);

        public void addChunk(AudioChunk chunk) {
            audiochunks.offer(chunk);
        }

        public volatile int currstate = 0;

        public void run() {

            while (!terminate) {

                // while (timing[mixstate]<=mytime){

                // Try acquiring a produce permit before going on.

                try {
                    //System.err.print("Waiting for a permit...");
                    produce.acquire();
                    //System.err.print("...got it\n");
                } catch (InterruptedException e) {
                    // Well, ouch.
                    e.printStackTrace();
                }

                int chunks = 0;

                // System.err.printf("Audio queue has %d chunks\n",audiochunks.size());

                // Play back only at most a given number of chunks once you reach
                // this spot.
                
                int atMost=Math.min(ISoundDriver.BUFFER_CHUNKS,audiochunks.size());
                
                while (atMost-->0){

                    AudioChunk chunk = null;
                    try {
                        chunk = audiochunks.take();
                    } catch (InterruptedException e1) {
                        // Should not block
                    }
                    // Play back all chunks present in a buffer ASAP
                    auline.write(chunk.buffer, 0, MIXBUFFERSIZE);
                    chunks++;
                    // No matter what, give the chunk back!
                    chunk.free = true;
                    audiochunkpool.checkIn(chunk);
                }
                
                //System.err.println(">>>>>>>>>>>>>>>>> CHUNKS " +chunks);
                // Signal that we consumed a whole buffer and we are ready for
                // another one.
                
                consume.release();
            }
        }
    }
    
    /** A single channel does carry a lot of crap, figuratively speaking.
     *  Instead of making updates to ALL channel parameters, it makes more
     *  sense having a "mixing queue" with instructions that tell the 
     *  mixer routine to do so-and-so with a certain channel. The mixer
     *  will then "empty" the queue when it has completed a complete servicing
     *  of all messages and mapped them to its internal status.
     *
     */
    protected class MixMessage {
    	/** If this is set, the mixer considers that channel "muted" */
    	public boolean stop;
    	
    	/** This signals an update of a currently active channel. 
    	 * Therefore pointer, remainder and data should remain untouched. 
    	 * However volume and step of a particular channel can change.
    	 */
    	public boolean update; 
    	
		public int remainder;
		public int end;
		public int channel;
    	public byte[] data;    	
    	public int step;
    	public int stepremainder;
    	public int[] leftvol_lookup;
    	public int[] rightvol_lookup;

    	public int pointer;
    	
    }
    
    /** Mixing thread. Mixing and submission must still go on even if
     *  the engine lags behind due to excessive CPU load.
     * 
     * @author Maes
     *
     */
    protected class MixServer
    implements Runnable {

        private final ArrayBlockingQueue<MixMessage> mixmessages;
    	
        /**
         * MAES: we'll have to use this for actual pointing. channels[] holds just
         * the data.
         */
        protected int[] p_channels;

        /**
         * The second one is supposed to point at "the end", so I'll make it an int.
         */
        protected int[] channelsend;

    	private final byte[][] channels;
        /** The channel step amount... */
        protected final int[] channelstep;

        /** ... and a 0.16 bit remainder of last step. */
        protected final int[] channelstepremainder;
        
        protected final int[][] channelrightvol_lookup;
        protected final int[][] channelleftvol_lookup;
    	    	
    	private volatile boolean update=false;
    	
    	public MixServer(int numChannels){
    		// We can put only so many messages "on hold"
    		mixmessages=new ArrayBlockingQueue<MixMessage>(35*numChannels);
    		this.p_channels=new int[numChannels];
    		this.channels=new byte[numChannels][];
    		this.channelstepremainder=new int[numChannels];
    		this.channelsend=new int[numChannels];
    		this.channelstep=new int[numChannels];
    		this.channelleftvol_lookup=new int[numChannels][];
    		this.channelrightvol_lookup=new int[numChannels][];
    	}
    	
    	/** Adds a channel mixing message to the queue */
    	
    	public void submitMixMessage(MixMessage m){
    	    try{
    		this.mixmessages.add(m);
    	    } catch (IllegalStateException  e){
    	        // Queue full. Force clear (VERY rare).
    	        mixmessages.clear();
    	        mixmessages.add(m);
    	    }
    		}
    	
    	public boolean terminate=false;
    	
    	@Override
		public void run()  {

	        // Mix current sound data.
	        // Data, from raw sound, for right and left.
	        int sample = 0;
	        int dl;
	        int dr;

	        // Pointers in global mixbuffer, left, right, end.
	        // Maes: those were explicitly signed short pointers...

	        int leftout;
	        int rightout;
	        
	        // Step in mixbuffer, left and right, thus two.
	        final int step=4;

	        // Mixing channel index.
	        int chan;
	        
	        // Determine end, for left channel only
	        // (right channel is implicit).
	        // MAES: this implies that the buffer will only mix
	        // that many samples at a time, and that the size is just right.
	        // Thus, it must be flushed (p_mixbuffer=0) before reusing it.
	        final int leftend = SAMPLECOUNT * step;

	        // Mix the next chunk, regardless of what the rest of the game is doing. 
	        while (!terminate) {
	        	
		        // POINTERS to Left and right channel
		        // which are in global mixbuffer, alternating.

		        leftout = 0;
		        rightout = 2;

	        	// Wait on interrupt semaphore anyway before draining queue.
	        	// This allows continuing mixing even if the main game loop
	        	// is stalled. This will result in continuous sounds,
	        	// rather than choppy interruptions.

	        		try {
	        			//System.err.print("Waiting on semaphore...");
						update_mixer.acquire();
						//System.err.print("...broke free\n");
					} catch (InterruptedException e) {
						// Nothing to do. Suck it down.
					}
	        	
	        	
	        	
	        	// Get current number of element in queue.
	        	// At worse, there will be none.
	        	int messages=mixmessages.size();
	
	        	// Drain the queue, applying changes to currently
	        	// looping channels, if applicable. This may result in new channels,
	        	// older ones being stopped, or current ones being altered. Changes
	        	// will be applied with priority either way.
	        	if (messages>0) drainAndApply(messages);

	        	// This may have changed in the mean.
	        	mixed=activeChannels();
	        
	        if (mixed) {// Avoid mixing entirely if no active channel.
			
				// Get audio chunk NOW
				gunk= audiochunkpool.checkOut();
    	        // Ha ha you're ass is mine!
    	        gunk.free = false;
				mixbuffer=gunk.buffer;
			
	        while (leftout < leftend) {
	            // Reset left/right value.
	            dl = 0;
	            dr = 0;

	            // Love thy L2 chache - made this a loop.
	            // Now more channels could be set at compile time
	            // as well. Thus loop those channels.

	            for (chan = 0; chan < numChannels; chan++) {

	                // Check channel, if active.
	                // MAES: this means that we must point to raw data here.
	                if (channels[chan] != null) {
	                    int channel_pointer = p_channels[chan];

	                    // Get the raw data from the channel.
	                    // Maes: this is supposed to be an 8-bit unsigned value.
                        sample = 0x00FF & channels[chan][channel_pointer];
	                        
	                    // Add left and right part for this channel (sound)
	                    // to the current data. Adjust volume accordingly.                        
	                    // Q: could this be optimized by converting samples to 16-bit
	                    // at load time, while also allowing for stereo samples?
	                    // A: Only for the stereo part. You would still look a lookup
	                    // for the CURRENT volume level.

	                    dl += channelleftvol_lookup[chan][sample];
	                    dr += channelrightvol_lookup[chan][sample];

	                    // This should increment the index inside a channel, but is
	                    // expressed in 16.16 fixed point arithmetic.
	                    channelstepremainder[chan] += channelstep[chan];

	                    // The actual channel pointer is increased here.
	                    // The above trickery allows playing back different pitches.
	                    // The shifting retains only the integer part.
	                    channel_pointer += channelstepremainder[chan] >> 16;

	                    // This limits it to the "decimal" part in order to
	                    // avoid undue accumulation.
	                    channelstepremainder[chan] &= 0xFFFF;

	                    // Check whether we are done. Also to avoid overflows.
	                    if (channel_pointer >= channelsend[chan]) {
	                        // Reset pointer for a channel.
	                           if (D)  System.err
	                                    .printf(
	                                        "Channel %d handle %d pointer %d thus done, stopping\n",
	                                        chan, channelhandles[chan],
	                                        channel_pointer);
	                        channels[chan] = null;
	                        
	                        // Communicate back to driver.
	                        SuperDoomSoundDriver.this.channels[chan]=false;
	                        channel_pointer = 0;
	                    }

	                    // Write pointer back, so we know where a certain channel
	                    // is the next time UpdateSounds is called.

	                    p_channels[chan] = channel_pointer;
	                }

	            } // for all channels.

	            // MAES: at this point, the actual values for a single sample
	            // (YIKES!) are in d1 and d2. We must use the leftout/rightout
	            // pointers to write them back into the mixbuffer.

	            // Clamp to range. Left hardware channel.
	            // Remnant of 8-bit mixing code? That must have raped ears
	            // and made them bleed.
	            // if (dl > 127) *leftout = 127;
	            // else if (dl < -128) *leftout = -128;
	            // else *leftout = dl;

	            if (dl > 0x7fff)
	                dl = 0x7fff;
	            else if (dl < -0x8000)
	                dl = -0x8000;

	            // Write left channel
	            mixbuffer[leftout] = (byte) ((dl & 0xFF00) >>> 8);
	            mixbuffer[leftout + 1] = (byte) (dl & 0x00FF);

	            // Same for right hardware channel.
	            if (dr > 0x7fff)
	                dr = 0x7fff;
	            else if (dr < -0x8000)
	                dr = -0x8000;

	            // Write right channel.
	            mixbuffer[rightout] = (byte) ((dr & 0xFF00) >>> 8);
	            mixbuffer[rightout + 1] = (byte) (dr & 0x00FF);

	            // Increment current pointers in mixbuffer.
	            leftout += step;
	            rightout += step;
	        } // End leftend/leftout while

	       // for (chan = 0; chan < numChannels; chan++) {
	       // 	if (channels[chan]!=null){
	       // 		System.err.printf("Channel %d pointer %d\n",chan,this.p_channels[chan]);
	       // 	}
	       // }

		   } // if-mixed
		   
	        // After an entire buffer has been mixed, we can apply any updates.
			// This includes silent updates.
	        submitSound();
			
	        
	        } // terminate loop
    	}	
    	
		private AudioChunk gunk;
		
    		private final void submitSound(){
    			// It's possible to stay entirely silent and give the audio
    	        // queue a chance to get drained. without sending any data.
    			// Saves BW and CPU cycles.
    	        if (mixed) {
    	            silence=0;


    	            // System.err.printf("Submitted sound chunk %d to buffer %d \n",chunk,mixstate);

    	            // Copy the currently mixed chunk into its position inside the
    	            // master buffer.
    	            // System.arraycopy(mixbuffer, 0, gunk.buffer, 0, MIXBUFFERSIZE);

    	            SOUNDSRV.addChunk(gunk);

    	            // System.err.println(chunk++);

    	            chunk++;
    	            // System.err.println(chunk);

    	            if (consume.tryAcquire())
    	                produce.release();

    	        } else {
    	            silence++;
    	            // MAES: attempt to fix lingering noise error
    	            if (silence >ISoundDriver.BUFFER_CHUNKS){
    	                line.flush();
    	                silence=0;
    	                }
    	        }
    		}
    	
    		/** Drains message queue and applies to individual channels. 
    		 *  More recently enqueued messages will trump older ones. This method
    		 *  only changes the STATUS of channels, and actual message submissions 
    		 *  can occur at most every sound frame. 
    		 *  
    		 * @param messages
    		 */
    		
	        private void drainAndApply(int messages ) {			
	        	MixMessage m;
	        	for (int i=0;i<messages;i++){
	        		// There should be no problems, in theory.
	        		m=this.mixmessages.remove();
	        		if (m.stop){
	        			stopChannel(m.channel);
	        			}
	        		else if (m.update){
	        			updateChannel(m);
	        		} else insertChannel(m);			
	        		}
	        	}
	        
	        private final void stopChannel(int channel){
	        	//System.err.printf("Stopping channel %d\n",channel);
	        	this.channels[channel]=null;
    			this.p_channels[channel]=0;
	        	}
	        
	        private final void updateChannel(MixMessage m){
	        	//System.err.printf("Updating channel %d\n",m.channel);
	        	this.channelleftvol_lookup[m.channel]=m.leftvol_lookup;
	        	this.channelrightvol_lookup[m.channel]=m.rightvol_lookup;
	        	this.channelstep[m.channel]=m.step;
	        	this.channelsend[m.channel]=m.end;
	        }
	        
	        private final void insertChannel(MixMessage m){
	        	int ch=m.channel;
	        	//System.err.printf("Inserting channel %d\n",ch);
    			this.channels[ch]=m.data;
    			this.p_channels[ch]=m.pointer;
    			this.channelsend[ch]=m.end;
    			this.channelstepremainder[ch]=m.remainder;
	        	this.channelleftvol_lookup[ch]=m.leftvol_lookup;
	        	this.channelrightvol_lookup[ch]=m.rightvol_lookup;
	        	this.channelstep[ch]=m.step;
	        }
			
			private final boolean activeChannels(){
		        for (int chan = 0; chan < numChannels; chan++) {
		            if (channels[chan] != null)
		                // SOME mixing has taken place.
		                return true;
		        		}
		        
		        return false;
	        }
			
			public final boolean channelIsPlaying(int num){
				return (channels[num]!=null);
			}
	        
		}

    
    @Override
    public boolean SoundIsPlaying(int handle) {

        int c = getChannelFromHandle(handle);
        return (c != -2 && channels[c]);

    }

    /**
     * Internal use.
     * 
     * @param handle
     * @return the channel that has the handle, or -2 if none has it.
     */
    protected int getChannelFromHandle(int handle) {
        // Which channel has it?
        for (int i = 0; i < numChannels; i++) {
            if (channelhandles[i] == handle)
                return i;
        }

        return BUSY_HANDLE;
    }

    @Override
    public void StopSound(int handle) {
        // Which channel has it?
        int hnd = getChannelFromHandle(handle);
        if (hnd >= 0) {
        	
            channels[hnd] = false;
            
            
            this.channelhandles[hnd] = IDLE_HANDLE;
            
			MixMessage m=new MixMessage();
			m.channel=hnd;
			m.stop=true;
			// We can only "ask" the mixer to stop at the next
			//chunk.
            MIXSRV.submitMixMessage(m);
        }
    }

    @Override
    public void SubmitSound() {

        // Also a dummy. The mixing thread is in a better position to
    	// judge when sound should be submitted.
    }
    
    private int silence=0; 

    @Override
    public void UpdateSoundParams(int handle, int vol, int sep, int pitch) {

        int chan = this.getChannelFromHandle(handle);
        // Per left/right channel.
        // x^2 seperation,
        // adjust volume properly.
        int leftvol = vol - ((vol * sep * sep) >> 16); // /(256*256);
        sep = sep - 257;
        int rightvol = vol - ((vol * sep * sep) >> 16);

        // Sanity check, clamp volume.

        if (rightvol < 0 || rightvol > 127)
            DM.doomSystem.Error("rightvol out of bounds");

        if (leftvol < 0 || leftvol > 127)
            DM.doomSystem.Error("leftvol out of bounds");

        MixMessage m=new MixMessage();
        
        // We are updating a currently active channel
        m.update=true;
        m.channel=chan;
        
        // Get the proper lookup table piece
        // for this volume level???
        
        m.leftvol_lookup = vol_lookup[leftvol];
        m.rightvol_lookup = vol_lookup[rightvol];

        // Well, if you can get pitch to change too...
        m.step = steptable[pitch];
        
        // Oddly enough, we could be picking a different channel here? :-S
        m.end = lengths[channelids[chan]];
        
        
        MIXSRV.submitMixMessage(m);
    }

    protected StringBuilder sb = new StringBuilder();

    public String channelStatus() {
        sb.setLength(0);
        for (int i = 0; i < numChannels; i++) {
            if (MIXSRV.channelIsPlaying(i))
                sb.append(i);
            else
                sb.append('-');
        }

        return sb.toString();

    }

    
    // Schedule this to release the sound thread at regular intervals
    // so that it doesn't outrun the audioline's buffer and game updates.
    
    protected class SoundTimer extends TimerTask {
        public void run() {
           update_mixer.release();         
        }
      }
    
    
    protected final AudioChunk SILENT_CHUNK = new AudioChunk();

    protected final AudioChunkPool audiochunkpool = new AudioChunkPool();
}
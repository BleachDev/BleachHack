package org.bleachhack.util.doom.data;

/**
 *   SoundFX struct.
 *    
 *   
 *
 */

public class sfxinfo_t {
	
		public sfxinfo_t(){
			
		}
	
        /** up to 6-character name */
        public String   name;

        /** Sfx singularity (only one at a time) */
        public boolean     singularity;

        /** Sfx priority */
        public int     priority;

        // referenced sound if a link
        // MAES: since in pure hackish C style, a "0" value would be used as a boolean, we'll need to distinguish more
        // unambiguously. So for querying, look at the "linked" boolean or a getter.
        public boolean linked;
        
        public sfxinfo_t  link;

        public sfxinfo_t getLink() {
            if (linked) return link;
            else return null;
        }

        public void setLink(sfxinfo_t link) {
            this.link=link;
        }
        
        // pitch if a link
        public int     pitch;

        // volume if a link
        public int     volume;

        /** sound data (used to be void*) */
        public byte[]  data;

        // this is checked every second to see if sound
        // can be thrown out (if 0, then decrement, if -1,
        // then throw out, if > 0, then it is in use)
        public int     usefulness;

        // lump number of sfx
        public int     lumpnum;

        public sfxinfo_t(String name, boolean singularity, int priority,
                sfxinfo_t link, int pitch, int volume, byte[] data,
                int usefulness, int lumpnum) {
            this.name = name;
            this.singularity = singularity;
            this.priority = priority;
            this.link = link;
            this.pitch = pitch;
            this.volume = volume;
            this.data = data;
            this.usefulness = usefulness;
            this.lumpnum = lumpnum;
        }
        
        /** MAES: Call this constructor if you don't want a cross-linked sound.
         * 
         * @param name
         * @param singularity
         * @param priority
         * @param pitch
         * @param volume
         * @param usefulness
         */
        
        public sfxinfo_t(String name, boolean singularity, int priority,
                int pitch, int volume, int usefulness) {
            this.name = name;
            this.singularity = singularity;
            this.priority = priority;
            this.linked = false;
            this.pitch = pitch;
            this.volume = volume;
            this.usefulness = usefulness;
        }
        
        public sfxinfo_t(String name, boolean singularity, int priority, boolean linked,
                int pitch, int volume, int usefulness) {
            this.name = name;
            this.singularity = singularity;
            this.priority = priority;
            this.linked = linked;
            this.pitch = pitch;
            this.volume = volume;
            this.usefulness = usefulness;
        }
        
        public int identify(sfxinfo_t[] array){
        	for (int i=0;i<array.length;i++){
        		if (array[i]==this){
        			return i;
        		}
        	}
        	// Duh
        	return 0;
        }
        
    };

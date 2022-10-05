package org.bleachhack.util.doom.p;

/** Animating textures and planes
 *  There is another anim_t used in wi_stuff, unrelated.
 * 
 * @author admin
 *
 */



public class anim_t {
	
		public anim_t(){
			
		}
    
      public anim_t(boolean istexture, int picnum, int basepic, int numpics,
            int speed) {
        super();
        this.istexture = istexture;
        this.picnum = picnum;
        this.basepic = basepic;
        this.numpics = numpics;
        this.speed = speed;
    }
    public  boolean istexture;
      public   int     picnum;
      public  int     basepic;
      public  int     numpics;
      public int     speed;
        
    }

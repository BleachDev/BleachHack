package org.bleachhack.util.doom.doom;

import static org.bleachhack.util.doom.data.Limits.MAXPLAYERS;
import static org.bleachhack.util.doom.utils.GenericCopy.malloc;

public class wbstartstruct_t implements Cloneable{

        public wbstartstruct_t(){
            plyr = malloc(wbplayerstruct_t::new, wbplayerstruct_t[]::new, MAXPLAYERS);
        }
    
        public int      epsd;   // episode # (0-2)

        // if true, splash the secret level
        public boolean  didsecret;
        
        // previous and next levels, origin 0
        public int      last;
        public int      next;   
        
        public int      maxkills;
        public int      maxitems;
        public int      maxsecret;
        public int      maxfrags;

        /** the par time */
        public int      partime;
        
        /** index of this player in game */
        public int      pnum;   
        /** meant to be treated as a "struct", therefore assignments should be deep copies */
        public wbplayerstruct_t[]   plyr;
        
        public wbstartstruct_t clone(){
            wbstartstruct_t cl=null;
            try {
                cl=(wbstartstruct_t)super.clone();
            } catch (CloneNotSupportedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            /*cl.epsd=this.epsd;
            cl.didsecret=this.didsecret;
            cl.last=this.last;
            cl.next=this.next;
            cl.maxfrags=this.maxfrags;
            cl.maxitems=this.maxitems;
            cl.maxsecret=this.maxsecret;
            cl.maxkills=this.maxkills;
            cl.partime=this.partime;
            cl.pnum=this.pnum;*/
            for (int i=0;i<cl.plyr.length;i++){
                cl.plyr[i]=this.plyr[i].clone();    
            }
            //cl.plyr=this.plyr.clone();
            
            return cl;
            
        }
        
    } 

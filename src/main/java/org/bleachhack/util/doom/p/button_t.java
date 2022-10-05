package org.bleachhack.util.doom.p;

import org.bleachhack.util.doom.rr.line_t;
import org.bleachhack.util.doom.s.degenmobj_t;

public class button_t implements Resettable{

        public line_t line;
        public bwhere_e    where;
        public int     btexture;
        public int     btimer;
        public degenmobj_t soundorg;
        
        public button_t(){	
        	this.btexture=0;
        	this.btimer=0;
        	this.where=bwhere_e.top;
        }
 
        public void reset(){
            this.line=null;
            this.where=bwhere_e.top;
            this.btexture=0;
            this.btimer=0;
            this.soundorg=null;
            
        }

    }
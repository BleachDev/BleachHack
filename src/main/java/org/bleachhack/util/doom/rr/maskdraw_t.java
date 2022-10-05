package org.bleachhack.util.doom.rr;

/** Purpose unknown, probably unused.
 *  On a closer examination, it could have been part of a system to
 *  "enqueue" masked draws, not much unlike the current parallel
 *  rendering subsystem, but discarded because of simplifications.
 *  In theory it could be brought back one day if parallel sprite
 *  drawing comes back.. just a thought ;-)
 * 
 * 
 * @author Maes
 *
 */

public class maskdraw_t {
        public int     x1;
        public  int     x2;
        
        public  int     column;
        public  int     topclip;
        public  int     bottomclip;

    }
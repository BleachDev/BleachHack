package org.bleachhack.util.doom.rr;

/** Resets limit-removing stuff back to their initial values,
 *  either for initialization reasons or to regain memory 
 *  e.g. playing MAP02 after nuts.wad should free up some vissprite buffers. 
 *  
 * @author admin
 *
 */

public interface ILimitResettable {
    public void resetLimits();   
}

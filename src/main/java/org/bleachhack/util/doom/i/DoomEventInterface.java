package org.bleachhack.util.doom.i;

/** Interface for Doom-to-System event handling methods
 * 
 * @author Velktron
 *
 */


public interface DoomEventInterface {

	/** The implementation is windowing subsystem-specific 
	 *  e.g. DOS, XServer, AWT or Swing or whatever.
	 * 
	 */
	public void GetEvent();

    public boolean mouseMoving();
    
    public void setMouseMoving(boolean mousMoving);
}

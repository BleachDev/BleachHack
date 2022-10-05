package org.bleachhack.util.doom.doom;

import java.io.IOException;

/** Stuff that the "main" is supposed to do. DoomMain implements those.
 * 
 * @author Maes
 *
 */


public interface IDoom {


	/** Called by IO functions when input is detected. */
	void PostEvent (event_t ev);
	void PageTicker ();
	void PageDrawer ();
	void AdvanceDemo ();
	void StartTitle ();
    void QuitNetGame() throws IOException; 

}
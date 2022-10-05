package org.bleachhack.util.doom.doom;

import java.io.IOException;

/** Doom is actually tied to its networking module.
 *  Therefore, no matter where and how you implement it, these functions
 *  need to be callable from within many modules.
 *  
 *  This is the so called "game networking" which is internal and game-specific,
 *  and not system networking which deals with the low level sockets and packet 
 *  stuff. You'll need DoomSystemNetworking for that one.
 *  
 * @author Velktron
 *
 */

public interface IDoomGameNetworking {
	
	public void TryRunTics() throws IOException;
	
	/**
	 * NetUpdate
	 * Builds ticcmds for console player,
	 * sends out a packet
	 * @throws IOException 
	 */

	public void NetUpdate ();
	
	public doomcom_t getDoomCom();
	
	public void setDoomCom(doomcom_t doomcom);
	
	public int getTicdup();

	public void setTicdup(int ticdup);

}

package org.bleachhack.util.doom.n;

import org.bleachhack.util.doom.doom.CommandVariable;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.NetConsts;
import org.bleachhack.util.doom.doom.doomcom_t;
import org.bleachhack.util.doom.mochadoom.Engine;

/** Does nothing.
 *  Allows running single-player games without an actual network.
 *  Hopefully, it will be replaced by a real UDP-based driver one day.
 *  
 * @author Velktron
 *
 */

public class DummyNetworkDriver<T, V> implements NetConsts, DoomSystemNetworking {

	////////////// STATUS ///////////

    private final DoomMain<T, V> DOOM;

	public DummyNetworkDriver(DoomMain<T, V> DOOM){
        this.DOOM = DOOM;
	}

	@Override
	public void InitNetwork() {
		doomcom_t doomcom =new doomcom_t();
		doomcom.id=DOOMCOM_ID;
		doomcom.ticdup=1;

		// single player game
        DOOM.netgame = Engine.getCVM().present(CommandVariable.NET);
		doomcom.id = DOOMCOM_ID;
		doomcom.numplayers = doomcom.numnodes = 1;
		doomcom.deathmatch = 0;
		doomcom.consoleplayer = 0;
		DOOM.gameNetworking.setDoomCom(doomcom);
	}

	@Override
	public void NetCmd() {
		// TODO Auto-generated method stub

	}
}

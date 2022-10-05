package org.bleachhack.util.doom.m;

import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.p.ActiveStates;

public interface IRandom {
	public int P_Random ();
	public int M_Random ();
	public void ClearRandom ();
	public int getIndex();
	public int P_Random(int caller);
	public int P_Random(String message);
	public int P_Random(ActiveStates caller, int sequence);
	public int P_Random(ActiveStates caller, mobjtype_t type,int sequence);
}

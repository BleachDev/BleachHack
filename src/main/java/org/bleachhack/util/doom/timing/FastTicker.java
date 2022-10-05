package org.bleachhack.util.doom.timing;

public class FastTicker implements ITicker {

    /**
     * I_GetTime
     * returns time in 1/70th second tics
     */
    @Override
    public int GetTime() {
        return fasttic++;
    }

    protected volatile int fasttic = 0;
}
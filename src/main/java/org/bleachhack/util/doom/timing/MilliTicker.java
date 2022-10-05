package org.bleachhack.util.doom.timing;

import static org.bleachhack.util.doom.data.Defines.TICRATE;

public class MilliTicker
        implements ITicker {

    /**
     * I_GetTime
     * returns time in 1/70th second tics
     */
   
    @Override
    public int GetTime() {
        long tp;
        //struct timezone   tzp;
        int newtics;

        tp = System.currentTimeMillis();
        if (basetime == 0) {
            basetime = tp;
        }
        newtics = (int) (((tp - basetime) * TICRATE) / 1000);
        return newtics;
    }
    
    protected volatile long basetime=0;
    protected volatile int oldtics=0;
    protected volatile int discrepancies;
    
}

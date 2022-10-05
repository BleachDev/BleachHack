package org.bleachhack.util.doom.timing;

import static org.bleachhack.util.doom.data.Defines.TICRATE;

public class NanoTicker
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

        // Attention: System.nanoTime() might not be consistent across multicore CPUs.
        // To avoid the core getting back to the past,
        tp = System.nanoTime();
        if (basetime == 0) {
            basetime = tp;
        }
        newtics = (int) (((tp - basetime) * TICRATE) / 1000000000);// + tp.tv_usec*TICRATE/1000000;
        if (newtics < oldtics) {
            System.err.printf("Timer discrepancies detected : %d", (++discrepancies));
            return oldtics;
        }
        return (oldtics = newtics);
    }

    protected volatile long basetime=0;
    protected volatile int oldtics=0;
    protected volatile int discrepancies;
    
}

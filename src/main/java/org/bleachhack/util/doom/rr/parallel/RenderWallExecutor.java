package org.bleachhack.util.doom.rr.parallel;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.bleachhack.util.doom.rr.IDetailAware;
import org.bleachhack.util.doom.rr.drawfuns.ColVars;
import org.bleachhack.util.doom.rr.drawfuns.DoomColumnFunction;

import org.bleachhack.util.doom.rr.drawfuns.R_DrawColumnBoomOpt;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawColumnBoomOptLow;

/**
 * This is what actual executes the RenderWallInstruction. Essentially it's a
 * self-contained column rendering function.
 * 
 * @author admin
 */

public class RenderWallExecutor<T,V>
        implements Runnable,IDetailAware {

    protected CyclicBarrier barrier;

    protected ColVars<T,V>[] RWI;

    protected int start, end;

    protected DoomColumnFunction<T,V> colfunchi, colfunclow;

    protected DoomColumnFunction<T,V> colfunc;

    public RenderWallExecutor(int SCREENWIDTH, int SCREENHEIGHT,
            int[] columnofs, int[] ylookup, V screen,
            ColVars<T,V>[] RWI, CyclicBarrier barrier) {
        this.RWI = RWI;
        this.barrier = barrier;
        this.SCREENWIDTH = SCREENWIDTH;
        this.SCREENHEIGHT = SCREENHEIGHT;

    }

    public void setRange(int start, int end) {
        this.end = end;
        this.start = start;
    }

    public void setDetail(int detailshift) {
        if (detailshift == 0)
            colfunc = colfunchi;
        else
            colfunc = colfunclow;
    }

    public void run() {

        // System.out.println("Wall executor from "+start +" to "+ end);

        for (int i = start; i < end; i++) {
            colfunc.invoke(RWI[i]);
        }

        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    public void updateRWI(ColVars<T,V>[] RWI) {
        this.RWI = RWI;

    }

    /////////////// VIDEO SCALE STUFF//////////////////////

    protected final int SCREENWIDTH;

    protected final int SCREENHEIGHT;
    /*
     * protected IVideoScale vs;
     * @Override public void setVideoScale(IVideoScale vs) { this.vs=vs; }
     * @Override public void initScaling() {
     * this.SCREENHEIGHT=vs.getScreenHeight();
     * this.SCREENWIDTH=vs.getScreenWidth(); }
     */

    public static final class HiColor extends RenderWallExecutor<byte[],short[]> {

        public HiColor(int SCREENWIDTH, int SCREENHEIGHT, int[] columnofs,
                int[] ylookup, short[] screen,
                ColVars<byte[], short[]>[] RWI, CyclicBarrier barrier) {
            super(SCREENWIDTH, SCREENHEIGHT, columnofs, ylookup, screen, RWI, barrier);
            colfunc =
                colfunchi =
                    new R_DrawColumnBoomOpt.HiColor(SCREENWIDTH, SCREENHEIGHT, ylookup,
                            columnofs, null, screen, null);
            colfunclow =
                new R_DrawColumnBoomOptLow.HiColor(SCREENWIDTH, SCREENHEIGHT, ylookup,
                        columnofs, null, screen, null);
        }
        
    }
    
    public static final class Indexed extends RenderWallExecutor<byte[],byte[]> {

        public Indexed(int SCREENWIDTH, int SCREENHEIGHT, int[] columnofs,
                int[] ylookup, byte[] screen,
                ColVars<byte[], byte[]>[] RWI, CyclicBarrier barrier) {
            super(SCREENWIDTH, SCREENHEIGHT, columnofs, ylookup, screen, RWI, barrier);
            colfunc =
                colfunchi =
                    new R_DrawColumnBoomOpt.Indexed(SCREENWIDTH, SCREENHEIGHT, ylookup,
                            columnofs, null, screen, null);
            colfunclow =
                new R_DrawColumnBoomOptLow.Indexed(SCREENWIDTH, SCREENHEIGHT, ylookup,
                        columnofs, null, screen, null);
        }
        
    }
    
    public static final class TrueColor extends RenderWallExecutor<byte[],int[]> {

        public TrueColor(int SCREENWIDTH, int SCREENHEIGHT, int[] columnofs,
                int[] ylookup, int[] screen,
                ColVars<byte[], int[]>[] RWI, CyclicBarrier barrier) {
            super(SCREENWIDTH, SCREENHEIGHT, columnofs, ylookup, screen, RWI, barrier);
            colfunc =
                colfunchi =
                    new R_DrawColumnBoomOpt.TrueColor(SCREENWIDTH, SCREENHEIGHT, ylookup,
                            columnofs, null, screen, null);
            colfunclow =
                new R_DrawColumnBoomOptLow.TrueColor(SCREENWIDTH, SCREENHEIGHT, ylookup,
                        columnofs, null, screen, null);
        }
        
    }
    
}

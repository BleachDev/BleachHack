package org.bleachhack.util.doom.rr.parallel;

import org.bleachhack.util.doom.i.IDoomSystem;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import org.bleachhack.util.doom.rr.IDetailAware;
import org.bleachhack.util.doom.rr.drawfuns.ColVars;
import org.bleachhack.util.doom.rr.drawfuns.DcFlags;
import org.bleachhack.util.doom.rr.drawfuns.DoomColumnFunction;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawColumnBoom;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawColumnBoomLow;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawFuzzColumn;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawFuzzColumnLow;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawTranslatedColumn;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawTranslatedColumnLow;
import org.bleachhack.util.doom.v.tables.BlurryTable;

/**
 * This is what actual executes the RenderWallInstruction. Essentially it's a
 * self-contained column rendering function.
  * 
 * @author velktron
 */

public abstract class RenderMaskedExecutor<T,V>
        implements Runnable,IDetailAware {

    protected CyclicBarrier barrier;

    protected ColVars<T,V>[] RMI;
   
    protected int rmiend;

    protected boolean lowdetail=false;
    
    protected int start, end;

    protected DoomColumnFunction<T,V> colfunchi, colfunclow;
    protected DoomColumnFunction<T,V> fuzzfunchi, fuzzfunclow;
    protected DoomColumnFunction<T,V> transfunchi, transfunclow;
    
    protected DoomColumnFunction<T,V> colfunc;

    public RenderMaskedExecutor(int SCREENWIDTH, int SCREENHEIGHT,            
            ColVars<T,V>[] RMI, CyclicBarrier barrier
            ) {
        this.RMI = RMI;
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
            lowdetail=false;
        else
            lowdetail=true;
    }

    public void run() {

        // System.out.println("Wall executor from "+start +" to "+ end);
        int dc_flags=0;
        
        // Check out ALL valid RMIs, but only draw those on YOUR side of the screen.
        for (int i = 0; i < rmiend; i++) {
            
            if (RMI[i].dc_x>=start && RMI[i].dc_x<=end){
            // Change function type according to flags.
            // No flag change means reusing the last used type
            dc_flags=RMI[i].dc_flags;
                //System.err.printf("Flags transition %d\n",dc_flags);
                if (lowdetail){
                    if ((dc_flags&DcFlags.FUZZY)!=0)                        
                        colfunc=fuzzfunclow;
                    else
                    if ((dc_flags&DcFlags.TRANSLATED)!=0)
                            colfunc=transfunclow;
                    else
                        colfunc=colfunclow;
                } else {
                    if ((dc_flags&DcFlags.FUZZY)!=0)
                        colfunc=fuzzfunchi;
                    else
                    if ((dc_flags&DcFlags.TRANSLATED)!=0)
                        colfunc=transfunchi;
                    else
                        colfunc=colfunchi;
                    }
            
            // No need to set shared DCvars, because it's passed with the arg.
            colfunc.invoke(RMI[i]);
            }
        }

        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
        
    public void setRMIEnd(int rmiend){
        this.rmiend=rmiend;
    }         

    public void updateRMI(ColVars<T,V>[] RMI) {
        this.RMI = RMI;

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
    
    public static final class HiColor extends RenderMaskedExecutor<byte[],short[]>{

        public HiColor(int SCREENWIDTH, int SCREENHEIGHT, int[] columnofs,
                int[] ylookup, short[] screen, ColVars<byte[], short[]>[] RMI,
                CyclicBarrier barrier,IDoomSystem I, BlurryTable BLURRY_MAP) {
            super(SCREENWIDTH, SCREENHEIGHT,RMI, barrier);
            
            // Regular masked columns
            this.colfunc = new R_DrawColumnBoom.HiColor(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,null,screen,I);
            this.colfunclow = new R_DrawColumnBoomLow.HiColor(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,null,screen,I);
            
            // Fuzzy columns
            this.fuzzfunchi= new R_DrawFuzzColumn.HiColor(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,null,screen,I,BLURRY_MAP);
            this.fuzzfunclow =new R_DrawFuzzColumnLow.HiColor(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,null,screen,I,BLURRY_MAP);

            // Translated columns
            this.transfunchi=new R_DrawTranslatedColumn.HiColor(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,null,screen,I);
            this.transfunclow= new R_DrawTranslatedColumnLow.HiColor(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,null,screen,I);

        }
        
    }
    
    public static final class Indexed extends RenderMaskedExecutor<byte[],byte[]>{

        public Indexed(int SCREENWIDTH, int SCREENHEIGHT, int[] columnofs,
                int[] ylookup, byte[] screen, ColVars<byte[], byte[]>[] RMI,
                CyclicBarrier barrier,IDoomSystem I, BlurryTable BLURRY_MAP) {
            super(SCREENWIDTH, SCREENHEIGHT,RMI, barrier);
            
            // Regular masked columns
            this.colfunc = new R_DrawColumnBoom.Indexed(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,null,screen,I);
            this.colfunclow = new R_DrawColumnBoomLow.Indexed(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,null,screen,I);
            
            // Fuzzy columns
            this.fuzzfunchi= new R_DrawFuzzColumn.Indexed(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,null,screen,I,BLURRY_MAP);
            this.fuzzfunclow =new R_DrawFuzzColumnLow.Indexed(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,null,screen,I,BLURRY_MAP);

            // Translated columns
            this.transfunchi=new R_DrawTranslatedColumn.Indexed(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,null,screen,I);
            this.transfunclow= new R_DrawTranslatedColumnLow.Indexed(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,null,screen,I);

        }
        
    }
    
    public static final class TrueColor extends RenderMaskedExecutor<byte[],int[]>{

        public TrueColor(int SCREENWIDTH, int SCREENHEIGHT, int[] columnofs,
                int[] ylookup, int[] screen, ColVars<byte[], int[]>[] RMI,
                CyclicBarrier barrier,IDoomSystem I, BlurryTable BLURRY_MAP) {
            super(SCREENWIDTH, SCREENHEIGHT,RMI, barrier);
            
            // Regular masked columns
            this.colfunc = new R_DrawColumnBoom.TrueColor(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,null,screen,I);
            this.colfunclow = new R_DrawColumnBoomLow.TrueColor(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,null,screen,I);
            
            // Fuzzy columns
            this.fuzzfunchi= new R_DrawFuzzColumn.TrueColor(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,null,screen,I,BLURRY_MAP);
            this.fuzzfunclow =new R_DrawFuzzColumnLow.TrueColor(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,null,screen,I,BLURRY_MAP);

            // Translated columns
            this.transfunchi=new R_DrawTranslatedColumn.TrueColor(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,null,screen,I);
            this.transfunclow= new R_DrawTranslatedColumnLow.TrueColor(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,null,screen,I);


        }
        
    }
    
}

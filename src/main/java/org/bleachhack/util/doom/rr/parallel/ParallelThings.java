package org.bleachhack.util.doom.rr.parallel;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import org.bleachhack.util.doom.rr.AbstractThings;
import org.bleachhack.util.doom.rr.IDetailAware;
import org.bleachhack.util.doom.rr.SceneRenderer;
import org.bleachhack.util.doom.rr.drawfuns.ColVars;
import org.bleachhack.util.doom.rr.drawfuns.DcFlags;
import org.bleachhack.util.doom.utils.C2JUtils;
import org.bleachhack.util.doom.v.scale.VideoScale;
import org.bleachhack.util.doom.v.tables.BlurryTable;

/**
 * Parallel Things drawing class, column based, using RMI pipeline.
 * For N threads, each thread only draws those columns of sprites that
 * are in its own 1/N portion of the screen.
 * 
 * Overrides only the terminal drawing methods from things, using a
 * mechanism very similar to column-based wall threading. It's not very
 * efficient, since some of the really heavy parts (such as visibility
 * priority) are still done serially, and actually do take up a lot of the
 * actual rendering time, and the number of columns generated is REALLY
 * enormous (100K+ for something like nuts.wad), and the thing chokes on
 * synchronization, more than anything. The only appropriate thing to do
 * would be to have a per-vissprite renderer, which would actually move much
 * of the brunt work away from the main thread. Some interesting benchmarks
 * on nuts.wad timedemo: Normal things serial renderer: 60-62 fps "Dummy"
 * completeColumns: 72 fps "Dummy" things renderer without final drawing: 80
 * fps "Dummy" things renderer without ANY calculations: 90 fps. This means
 * that even a complete parallelization will likely have a quite limited
 * impact.
 * 
 * @author velktron
 */

public abstract class ParallelThings<T,V> extends AbstractThings<T,V> {
    
    // stuff to get from container
    
    
    /** Render Masked Instuction subsystem. Essentially, a way to split sprite work
     *  between threads on a column-basis.
     */

    protected ColVars<T, V>[] RMI;

    /**
     * Increment this as you submit RMIs to the "queue". Remember to reset to 0
     * when you have drawn everything!
     */
    protected int RMIcount = 0;

    protected RenderMaskedExecutor<T,V>[] RMIExec;
    
    protected final int NUMMASKEDTHREADS;
    protected final CyclicBarrier maskedbarrier;
    protected final Executor tp;
    
    public ParallelThings(VideoScale vs, SceneRenderer<T, V> R, Executor tp, int numthreads) {
        super(vs, R);
        this.tp=tp;
        this.NUMMASKEDTHREADS=numthreads;
        this.maskedbarrier=new CyclicBarrier(NUMMASKEDTHREADS+1);
    }

    @Override
    public void DrawMasked() {
        
        // This just generates the RMI instructions.
        super.DrawMasked();
        // This splits the work among threads and fires them up
        RenderRMIPipeline();

        try {
            // Wait for them to be done.
            maskedbarrier.await();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void completeColumn() {

        if (view.detailshift == 1)
            flags = DcFlags.LOW_DETAIL;
        // Don't wait to go over
        if (RMIcount >= RMI.length) {
            ResizeRMIBuffer();
        }

        // A deep copy is still necessary, as well as setting dc_flags
        RMI[RMIcount].copyFrom(maskedcvars, colfunc.getFlags());

        // We only need to point to the next one in the list.
        RMIcount++;
    }

    int flags;
    
   
    protected void RenderRMIPipeline() {

        for (int i = 0; i < NUMMASKEDTHREADS; i++) {

            RMIExec[i].setRange((i * this.vs.getScreenWidth()) / NUMMASKEDTHREADS,
                ((i + 1) * this.vs.getScreenWidth()) / NUMMASKEDTHREADS);
            RMIExec[i].setRMIEnd(RMIcount);
            // RWIExec[i].setRange(i%NUMWALLTHREADS,RWIcount,NUMWALLTHREADS);
            tp.execute(RMIExec[i]);
        }

        // System.out.println("RWI count"+RWIcount);
        RMIcount = 0;
    }
    
    protected void ResizeRMIBuffer() {
        ColVars<T, V> fake = new ColVars<T, V>();
        ColVars<T, V>[] tmp =            // TODO Auto-generated constructor stub
            C2JUtils.createArrayOfObjects(fake, RMI.length * 2);
        System.arraycopy(RMI, 0, tmp, 0, RMI.length);

        // Bye bye, old RMI.
        RMI = tmp;

        for (int i = 0; i < NUMMASKEDTHREADS; i++) {
            RMIExec[i].updateRMI(RMI);
        }
        
        System.err.println("RMI Buffer resized. Actual capacity " + RMI.length);
    }
    
    protected abstract void InitRMISubsystem(int[] columnofs, int[] ylookup,V screen, CyclicBarrier
            maskedbarrier,BlurryTable BLURRY_MAP, List<IDetailAware> detailaware);
    
    public static class Indexed extends ParallelThings<byte[],byte[]>{

    public Indexed(VideoScale vs, SceneRenderer<byte[], byte[]> R, Executor tp, int numthreads) {
            super(vs, R, tp, numthreads);
        }

    protected void InitRMISubsystem(int[] columnofs, int[] ylookup,byte[] screen, CyclicBarrier
            maskedbarrier,BlurryTable BLURRY_MAP, List<IDetailAware> detailaware)    {
        for (int i = 0; i < NUMMASKEDTHREADS; i++) {
            RMIExec[i] =
                new RenderMaskedExecutor.Indexed(vs.getScreenWidth(), vs.getScreenHeight(), columnofs,
                        ylookup, screen, RMI, maskedbarrier,I,BLURRY_MAP);
                
            detailaware.add(RMIExec[i]);
            }
        }
    }
    
    public static class HiColor extends ParallelThings<byte[],short[]>{
        

        public HiColor(VideoScale vs, SceneRenderer<byte[], short[]> R, Executor tp, int numthreads) {
            super(vs, R, tp, numthreads);
        }

        protected void InitRMISubsystem(int[] columnofs, int[] ylookup,short[] screen, CyclicBarrier
                maskedbarrier,BlurryTable BLURRY_MAP, List<IDetailAware> detailaware)    {
            for (int i = 0; i < NUMMASKEDTHREADS; i++) {
                RMIExec[i] =
                    new RenderMaskedExecutor.HiColor(vs.getScreenWidth(), vs.getScreenHeight(), columnofs,
                            ylookup, screen, RMI, maskedbarrier,I, BLURRY_MAP);
                    
                detailaware.add(RMIExec[i]);
                }
            }
        }
    
public static class TrueColor extends ParallelThings<byte[],int[]>{


        public TrueColor(VideoScale vs, SceneRenderer<byte[], int[]> R, Executor tp, int numthreads) {
        super(vs, R, tp, numthreads);
    }

        protected void InitRMISubsystem(int[] columnofs, int[] ylookup,int[] screen, CyclicBarrier
                maskedbarrier,BlurryTable BLURRY_MAP, List<IDetailAware> detailaware)    {
            for (int i = 0; i < NUMMASKEDTHREADS; i++) {
                RMIExec[i] =
                    new RenderMaskedExecutor.TrueColor(vs.getScreenWidth(), vs.getScreenHeight(), columnofs,
                            ylookup, screen, RMI, maskedbarrier,I, BLURRY_MAP);
                    
                detailaware.add(RMIExec[i]);
                }
            }
        }

}

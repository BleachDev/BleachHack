package org.bleachhack.util.doom.rr.parallel;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import org.bleachhack.util.doom.rr.IMaskedDrawer;
import org.bleachhack.util.doom.rr.ISpriteManager;
import org.bleachhack.util.doom.rr.IVisSpriteManagement;
import org.bleachhack.util.doom.rr.SceneRenderer;
import org.bleachhack.util.doom.v.scale.VideoScale;

/**  Alternate parallel sprite renderer using a split-screen strategy.
 *  For N threads, each thread gets to render only the sprites that are entirely
 *  in its own 1/Nth portion of the screen.
 *  
 *  Sprites that span more than one section, are drawn partially. Each thread
 *  only has to worry with the priority of its own sprites. Similar to the 
 *  split-seg parallel drawer.
 * 
 *  Uses the "masked workers" subsystem, there is no column pipeline: workers
 *  "tap" directly in the sprite sorted table and act accordingly (draw entirely,
 *  draw nothing, draw partially).
 *  
 *  It uses masked workers to perform the actual work, each of which is a complete
 *  Thing Drawer. 
 * 
 * @author velktron
 *
 */

public final class ParallelThings2<T,V> implements IMaskedDrawer<T,V> {

    MaskedWorker<T,V>[] maskedworkers;
    CyclicBarrier maskedbarrier;
    Executor tp;
    protected final IVisSpriteManagement<V> VIS;
    protected final VideoScale vs;
    
    public ParallelThings2(VideoScale vs, SceneRenderer<T,V> R) {
        this.VIS=R.getVisSpriteManager();
        this.vs = vs;
    }

    @Override
    public void DrawMasked() {

        VIS.SortVisSprites();

        for (int i = 0; i < maskedworkers.length; i++) {
            tp.execute(maskedworkers[i]);
        }

        try {
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
        // Does nothing. Dummy.
    }

    @Override
    public void setPspriteScale(int scale) {
        for (int i = 0; i < maskedworkers.length; i++)
            maskedworkers[i].setPspriteScale(scale);
    }

    @Override
    public void setPspriteIscale(int scale) {
        for (int i = 0; i < maskedworkers.length; i++)
            maskedworkers[i].setPspriteIscale(scale);
    }

    @Override
    public void setDetail(int detailshift) {
        for (int i = 0; i < maskedworkers.length; i++)
            maskedworkers[i].setDetail(detailshift);
        
    }

    @Override
    public void cacheSpriteManager(ISpriteManager SM) {
        for (int i = 0; i < maskedworkers.length; i++)
            maskedworkers[i].cacheSpriteManager(SM);
        
    }

}

package org.bleachhack.util.doom.rr.parallel;

import static org.bleachhack.util.doom.data.Defines.ANGLETOSKYSHIFT;
import static org.bleachhack.util.doom.data.Tables.addAngles;
import org.bleachhack.util.doom.doom.DoomMain;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;
import org.bleachhack.util.doom.rr.IDetailAware;
import org.bleachhack.util.doom.rr.PlaneDrawer;
import org.bleachhack.util.doom.rr.SceneRenderer;
import org.bleachhack.util.doom.rr.drawfuns.ColVars;
import org.bleachhack.util.doom.rr.drawfuns.DoomColumnFunction;
import org.bleachhack.util.doom.rr.drawfuns.DoomSpanFunction;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawColumnBoomOpt;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawColumnBoomOptLow;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawSpanLow;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawSpanUnrolled;
import org.bleachhack.util.doom.rr.drawfuns.SpanVars;
import org.bleachhack.util.doom.rr.visplane_t;
import org.bleachhack.util.doom.v.graphics.Palettes;

/** Visplane worker which shares work in an equal-visplane number strategy
 *  with other workers. Might be unbalanced if one worker gets too large
 *  visplanes and others get smaller ones. Balancing strategy is applied in 
 *  run(), otherwise it's practically similar to a PlaneDrwer.
 *  
 *  
 * @author velktron
 *
 */

public abstract class VisplaneWorker<T,V> extends PlaneDrawer<T,V> implements Runnable,IDetailAware{

    // Private to each thread.
    protected final int id;
    protected final int NUMFLOORTHREADS;
    protected final CyclicBarrier barrier;
    
    protected int vpw_planeheight;
    protected V[] vpw_planezlight;
    protected int vpw_basexscale,vpw_baseyscale;

    protected SpanVars<T,V> vpw_dsvars;
    protected ColVars<T,V> vpw_dcvars;
    
    // OBVIOUSLY each thread must have its own span functions.
    protected DoomSpanFunction<T,V> vpw_spanfunc;
    protected DoomColumnFunction<T,V> vpw_skyfunc;
    protected DoomSpanFunction<T,V> vpw_spanfunchi;
    protected DoomSpanFunction<T,V> vpw_spanfunclow;
    protected DoomColumnFunction<T,V> vpw_skyfunchi;
    protected DoomColumnFunction<T,V> vpw_skyfunclow;
        
    public VisplaneWorker(DoomMain<T,V> DOOM,int id,int SCREENWIDTH, int SCREENHEIGHT, SceneRenderer<T,V> R,CyclicBarrier visplanebarrier,int NUMFLOORTHREADS) {
        super(DOOM, R); 
        this.barrier=visplanebarrier;
        this.id=id;
        this.NUMFLOORTHREADS=NUMFLOORTHREADS;
    }

    public static final class HiColor extends VisplaneWorker<byte[], short[]> {

        public HiColor(DoomMain<byte[],short[]> DOOM,int id, int SCREENWIDTH, int SCREENHEIGHT, SceneRenderer<byte[], short[]> R,
                int[] columnofs, int[] ylookup, short[] screen,
                CyclicBarrier visplanebarrier, int NUMFLOORTHREADS) {
            super(DOOM, id, SCREENWIDTH, SCREENHEIGHT, R, visplanebarrier, NUMFLOORTHREADS);
            // Alias to those of Planes.

            vpw_dsvars = new SpanVars<byte[], short[]>();
            vpw_dcvars = new ColVars<byte[], short[]>();
            vpw_spanfunc = vpw_spanfunchi = new R_DrawSpanUnrolled.HiColor(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, vpw_dsvars, screen, I);
            vpw_spanfunclow = new R_DrawSpanLow.HiColor(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, vpw_dsvars, screen, I);
            vpw_skyfunc = vpw_skyfunchi = new R_DrawColumnBoomOpt.HiColor(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, vpw_dcvars, screen, I);
            vpw_skyfunclow = new R_DrawColumnBoomOptLow.HiColor(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, vpw_dcvars, screen, I);

        }

    }
    
    public void setDetail(int detailshift) {
        if (detailshift == 0){
            vpw_spanfunc = vpw_spanfunchi;
            vpw_skyfunc= vpw_skyfunchi;
        }
        else{
            vpw_spanfunc = vpw_spanfunclow;
            vpw_skyfunc =vpw_skyfunclow;
        }
    }
    
    @Override
    public void run() {
        visplane_t      pln=null; //visplane_t
        // These must override the global ones
        int         light;
        int         x;
        int         stop;
        int         angle;
      
        // Now it's a good moment to set them.
        vpw_basexscale=vpvars.getBaseXScale();
        vpw_baseyscale=vpvars.getBaseYScale();
        
        // TODO: find a better way to split work. As it is, it's very uneven
        // and merged visplanes in particular are utterly dire.
        
        for (int pl= this.id; pl <vpvars.lastvisplane; pl+=NUMFLOORTHREADS) {
             pln=vpvars.visplanes[pl];
            // System.out.println(id +" : "+ pl);
             
         if (pln.minx > pln.maxx)
             continue;

         
         // sky flat
         if (pln.picnum == TexMan.getSkyFlatNum() )
         {
             // Cache skytexture stuff here. They aren't going to change while
             // being drawn, after all, are they?
             int skytexture=TexMan.getSkyTexture();
             // MAES: these must be updated to keep up with screen size changes.
             vpw_dcvars.viewheight=view.height;
             vpw_dcvars.centery=view.centery;
             vpw_dcvars.dc_texheight=TexMan.getTextureheight(skytexture)>>FRACBITS;                 
             vpw_dcvars.dc_iscale = vpvars.getSkyScale()>>view.detailshift;
             
             vpw_dcvars.dc_colormap = colormap.colormaps[Palettes.COLORMAP_FIXED];
             vpw_dcvars.dc_texturemid = TexMan.getSkyTextureMid();
             for (x=pln.minx ; x <= pln.maxx ; x++)
             {
           
                 vpw_dcvars.dc_yl = pln.getTop(x);
                 vpw_dcvars.dc_yh = pln.getBottom(x);
             
             if (vpw_dcvars.dc_yl <= vpw_dcvars.dc_yh)
             {
                 angle = (int) (addAngles(view.angle, view.xtoviewangle[x])>>>ANGLETOSKYSHIFT);
                 vpw_dcvars.dc_x = x;
                 // Optimized: texheight is going to be the same during normal skies drawing...right?
                 vpw_dcvars.dc_source = TexMan.GetCachedColumn(TexMan.getSkyTexture(), angle);
                 vpw_skyfunc.invoke();
             }
             }
             continue;
         }
         
         // regular flat
         vpw_dsvars.ds_source = TexMan.getSafeFlat(pln.picnum);

         vpw_planeheight = Math.abs(pln.height-view.z);
         light = (pln.lightlevel >>> colormap.lightSegShift())+colormap.extralight;

         if (light >= colormap.lightLevels())
             light = colormap.lightLevels()-1;

         if (light < 0)
             light = 0;

         vpw_planezlight = colormap.zlight[light];

         // We set those values at the border of a plane's top to a "sentinel" value...ok.
         pln.setTop(pln.maxx+1,(char) 0xffff);
         pln.setTop(pln.minx-1, (char) 0xffff);
         
         stop = pln.maxx + 1;

         
         for (x=pln.minx ; x<= stop ; x++) {
          MakeSpans(x,pln.getTop(x-1),
             pln.getBottom(x-1),
             pln.getTop(x),
             pln.getBottom(x));
            }
         
         }
         // We're done, wait.

            try {
                barrier.await();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

     }
  
      
          
      
      
  }
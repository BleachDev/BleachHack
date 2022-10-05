package org.bleachhack.util.doom.rr.parallel;

import static org.bleachhack.util.doom.data.Limits.*;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.player_t;
import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawColumnBoom;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawColumnBoomLow;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawColumnBoomOpt;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawColumnBoomOptLow;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawFuzzColumn;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawFuzzColumnLow;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawSpanLow;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawSpanUnrolled;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawTLColumn;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawTranslatedColumn;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawTranslatedColumnLow;
import static org.bleachhack.util.doom.utils.GenericCopy.malloc;

/** This is a second attempt at building a seg-focused parallel renderer, instead of
 * column-based. It does function, but is broken and has unsolved data dependencies.
 * It's therefore not used in official releases, and I chose to deprecate it.
 * If you still want to develop it, be my guest.
 * 
 * @author velktron
 *
 */

public abstract class ParallelRenderer2<T, V> extends AbstractParallelRenderer<T, V> {
    
    @SuppressWarnings("unchecked")
    public ParallelRenderer2(DoomMain<T, V> DOOM, int wallthread, int floorthreads, int nummaskedthreads) {
        super(DOOM, wallthread, floorthreads, nummaskedthreads);
        System.out.println("Parallel Renderer 2 (Seg-based)");
        
        this.MySegs = new ParallelSegs2<>(this);
        this.MyPlanes = new ParallelPlanes<>(DOOM, this);
        this.MyThings = new ParallelThings2<>(DOOM.vs, this);

        // TO BE LATE INIT? AFTER CONS?
        // Masked workers.
        ((ParallelThings2<T, V>) MyThings).maskedworkers = maskedworkers = new MaskedWorker[NUMMASKEDTHREADS];
        InitMaskedWorkers();
        
        ((ParallelSegs2<T, V>) MySegs).RSI = malloc(RenderSegInstruction::new, RenderSegInstruction[]::new, MAXSEGS * 3);
    }

	@Override
    @SuppressWarnings("unchecked")
	protected void InitParallelStuff() {
		// Prepare parallel stuff
		((ParallelSegs2<T, V>) MySegs).RSIExec = new RenderSegExecutor[NUMWALLTHREADS];
        tp = Executors.newFixedThreadPool(NUMWALLTHREADS + NUMFLOORTHREADS);
        // Prepare the barrier for MAXTHREADS + main thread.
        //wallbarrier=new CyclicBarrier(NUMWALLTHREADS+1);
        visplanebarrier = new CyclicBarrier(NUMFLOORTHREADS + NUMWALLTHREADS + 1);

        vpw = new VisplaneWorker2[NUMFLOORTHREADS];

        // Uses "seg" parallel drawer, so RSI.
        InitRSISubsystem();

        maskedbarrier = new CyclicBarrier(NUMMASKEDTHREADS + 1);

        // If using masked threads, set these too.
        TexMan.setSMPVars(NUMMASKEDTHREADS);
	}

	///////////////////////// The actual rendering calls ///////////////////////

	/**
	 * R_RenderView
	 * 
	 * As you can guess, this renders the player view of a particular player object.
	 * In practice, it could render the view of any mobj too, provided you adapt the
	 * SetupFrame method (where the viewing variables are set).
	 * 
	 */

    @Override
    @SuppressWarnings("unchecked")
	public void RenderPlayerView (player_t player)
	{   
		// Viewing variables are set according to the player's mobj. Interesting hacks like
		// free cameras or monster views can be done.
		SetupFrame (player);

		/* Uncommenting this will result in a very existential experience
  if (Math.random()>0.999){
	  thinker_t shit=P.getRandomThinker();
	  try {
	  mobj_t crap=(mobj_t)shit;
	  player.mo=crap;
	  } catch (ClassCastException e){

	  }
  	}*/

		// Clear buffers. 
		MyBSP.ClearClipSegs();
        seg_vars.ClearDrawSegs();
        vp_vars.ClearPlanes();
		MySegs.ClearClips();
        VIS.ClearSprites();

        // Check for new console commands.
        DOOM.gameNetworking.NetUpdate();

		// The head node is the last node output.
		MyBSP.RenderBSPNode(DOOM.levelLoader.numnodes - 1);
		
        // RenderRMIPipeline();
        /*
         * try { maskedbarrier.await(); } catch (Exception e) {
         * e.printStackTrace(); }
         */

		((ParallelSegs2<T, V>) MySegs).RenderRSIPipeline();
        // System.out.printf("Submitted %d RSIs\n",RSIcount);

        MySegs.CompleteRendering();

        // Check for new console commands.
        DOOM.gameNetworking.NetUpdate();

		// "Warped floor" fixed, same-height visplane merging fixed.
		MyPlanes.DrawPlanes ();

		try {
			visplanebarrier.await();
		} catch (InterruptedException | BrokenBarrierException e){
			e.printStackTrace();
		}

        // Check for new console commands.
        DOOM.gameNetworking.NetUpdate();

        MySegs.sync();
        MyPlanes.sync();

//            drawsegsbarrier.await();
//            visplanebarrier.await();


        MyThings.DrawMasked();
	}

    abstract protected void InitRSISubsystem();
    /*
     * { // CATCH: this must be executed AFTER screen is set, and // AFTER we
     * initialize the RWI themselves, // before V is set (right?) //offsets=new
     * int[NUMWALLTHREADS]; for (int i=0;i<NUMWALLTHREADS;i++){ RSIExec[i]=new
     * RenderSegExecutor.HiColor( SCREENWIDTH, SCREENHEIGHT, i, screen, this,
     * TexMan, RSI, MySegs.getBLANKCEILINGCLIP(), MySegs.getBLANKFLOORCLIP(),
     * MySegs.getCeilingClip(), MySegs.getFloorClip(), columnofs, xtoviewangle,
     * ylookup, this.visplanes, this.visplanebarrier);
     * RSIExec[i].setVideoScale(this.vs); RSIExec[i].initScaling(); // Each
     * SegExecutor sticks to its own half (or 1/nth) of the screen.
     * RSIExec[i].setScreenRange
     * (i*(SCREENWIDTH/NUMWALLTHREADS),(i+1)*(SCREENWIDTH/NUMWALLTHREADS));
     * detailaware.add(RSIExec[i]); } for (int i=0;i<NUMFLOORTHREADS;i++){
     * vpw[i]=new VisplaneWorker(i,SCREENWIDTH,SCREENHEIGHT,columnofs,ylookup,
     * screen,visplanebarrier,NUMFLOORTHREADS); detailaware.add((IDetailAware)
     * vpw[i]); } }
     */


    /**
	 * R_Init
	 */

	//public int  detailLevel;
	//public int  screenblocks=9; // has defa7ult

    protected abstract void InitMaskedWorkers();

    public static final class Indexed extends ParallelRenderer2<byte[], byte[]> {

        public Indexed(DoomMain<byte[], byte[]> DM, int wallthread, int floorthreads, int nummaskedthreads) {
            super(DM, wallthread, floorthreads, nummaskedthreads);

            // Init light levels
            colormaps.scalelight = new byte[colormaps.lightLevels()][colormaps.maxLightScale()][];
            colormaps.scalelightfixed = new byte[colormaps.maxLightScale()][];
            colormaps.zlight = new byte[colormaps.lightLevels()][colormaps.maxLightZ()][];
            
            completeInit();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void InitRSISubsystem() {
            // int[] offsets = new int[NUMWALLTHREADS];
            final ParallelSegs2<byte[], byte[]> parallelSegs = ((ParallelSegs2<byte[], byte[]>) MySegs);
            for (int i = 0; i < NUMWALLTHREADS; i++) {
                parallelSegs.RSIExec[i] = new RenderSegExecutor.Indexed(
                        DOOM, i, screen, TexMan,
                        parallelSegs.RSI, MySegs.getBLANKCEILINGCLIP(), MySegs.getBLANKFLOORCLIP(),
                        MySegs.getCeilingClip(), MySegs.getFloorClip(), columnofs, view.xtoviewangle,
                        ylookup, vp_vars.visplanes, this.visplanebarrier, colormaps
                );
                // SegExecutor sticks to its own half (or 1/nth) of the screen.
                parallelSegs.RSIExec[i].setScreenRange(i * (DOOM.vs.getScreenWidth() / NUMWALLTHREADS), (i + 1) * (DOOM.vs.getScreenWidth() / NUMWALLTHREADS));
                detailaware.add(parallelSegs.RSIExec[i]);
            }

            for (int i = 0; i < NUMFLOORTHREADS; i++) {
                final VisplaneWorker2<?, ?> w = new VisplaneWorker2.Indexed(
                        DOOM, this, i, columnofs, ylookup, screen, visplanebarrier, NUMFLOORTHREADS
                );
                vpw[i] = w;
                detailaware.add(w);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void InitMaskedWorkers() {
            for (int i = 0; i < NUMMASKEDTHREADS; i++) {
                maskedworkers[i] = new MaskedWorker.Indexed(
                    DOOM.vs, this, i, ylookup, columnofs, NUMMASKEDTHREADS,
                    screen, maskedbarrier, BLURRY_MAP
                );
                
                detailaware.add(maskedworkers[i]);
                // "Peg" to sprite manager.
                maskedworkers[i].cacheSpriteManager(DOOM.spriteManager);
            }
        }
        
        @Override
        protected void InitColormaps() throws IOException {
            // Load in the light tables,
            // 256 byte align tables.
            colormaps.colormaps = DOOM.graphicSystem.getColorMap();
            // MAES: blurry effect is hardcoded to this colormap.
            BLURRY_MAP = DOOM.graphicSystem.getBlurryTable();
            // colormaps = (byte *)( ((int)colormaps + 255)&~0xff);     
        }
        
        @Override
        protected void R_InitDrawingFunctions() {

            // Span functions. Common to all renderers unless overriden
            // or unused e.g. parallel renderers ignore them.
            DrawSpan = new R_DrawSpanUnrolled.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dsvars, screen, DOOM.doomSystem);
            DrawSpanLow = new R_DrawSpanLow.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dsvars, screen, DOOM.doomSystem);

            // Translated columns are usually sprites-only.
            DrawTranslatedColumn = new R_DrawTranslatedColumn.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            DrawTranslatedColumnLow = new R_DrawTranslatedColumnLow.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            //  DrawTLColumn=new R_DrawTLColumn(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,maskedcvars,screen,I);

            // Fuzzy columns. These are also masked.
            DrawFuzzColumn = new R_DrawFuzzColumn.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem, BLURRY_MAP);
            DrawFuzzColumnLow = new R_DrawFuzzColumnLow.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem, BLURRY_MAP);

            // Regular draw for solid columns/walls. Full optimizations.
            DrawColumn = new R_DrawColumnBoomOpt.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dcvars, screen, DOOM.doomSystem);
            DrawColumnLow = new R_DrawColumnBoomOptLow.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dcvars, screen, DOOM.doomSystem);

            // Non-optimized stuff for masked.
            DrawColumnMasked = new R_DrawColumnBoom.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            DrawColumnMaskedLow = new R_DrawColumnBoomLow.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);

            // Player uses masked
            DrawColumnPlayer = DrawColumnMasked; // Player normally uses masked.

            // Skies use their own. This is done in order not to stomp parallel threads.
            DrawColumnSkies = new R_DrawColumnBoomOpt.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, skydcvars, screen, DOOM.doomSystem);
            DrawColumnSkiesLow = new R_DrawColumnBoomOptLow.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, skydcvars, screen, DOOM.doomSystem);

            super.R_InitDrawingFunctions();
        }
    }
    
    public static final class HiColor extends ParallelRenderer2<byte[], short[]> {

        public HiColor(DoomMain<byte[], short[]> DM, int wallthread, int floorthreads, int nummaskedthreads) {
            super(DM, wallthread, floorthreads, nummaskedthreads);

            // Init light levels
            colormaps.scalelight = new short[colormaps.lightLevels()][colormaps.maxLightScale()][];
            colormaps.scalelightfixed = new short[colormaps.maxLightScale()][];
            colormaps.zlight = new short[colormaps.lightLevels()][colormaps.maxLightZ()][];

            completeInit();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void InitRSISubsystem() {
            // int[] offsets = new int[NUMWALLTHREADS];
            final ParallelSegs2<byte[], short[]> parallelSegs = ((ParallelSegs2<byte[], short[]>) MySegs);
            for (int i = 0; i < NUMWALLTHREADS; i++) {
                parallelSegs.RSIExec[i] = new RenderSegExecutor.HiColor(
                        DOOM, i, screen, TexMan,
                        parallelSegs.RSI, MySegs.getBLANKCEILINGCLIP(), MySegs.getBLANKFLOORCLIP(),
                        MySegs.getCeilingClip(), MySegs.getFloorClip(), columnofs, view.xtoviewangle,
                        ylookup, vp_vars.visplanes, this.visplanebarrier, colormaps
                );
                // SegExecutor sticks to its own half (or 1/nth) of the screen.
                parallelSegs.RSIExec[i].setScreenRange(i * (DOOM.vs.getScreenWidth() / NUMWALLTHREADS), (i + 1) * (DOOM.vs.getScreenWidth() / NUMWALLTHREADS));
                detailaware.add(parallelSegs.RSIExec[i]);
            }

            for (int i = 0; i < NUMFLOORTHREADS; i++) {
                final VisplaneWorker2<?, ?> w = new VisplaneWorker2.HiColor(
                        DOOM, this, i, columnofs, ylookup, screen, visplanebarrier, NUMFLOORTHREADS
                );
                vpw[i] = w;
                detailaware.add(w);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void InitMaskedWorkers() {
            for (int i = 0; i < NUMMASKEDTHREADS; i++) {
                maskedworkers[i] = new MaskedWorker.HiColor(
                    DOOM.vs, this, i, ylookup, columnofs, NUMMASKEDTHREADS,
                    screen, maskedbarrier, BLURRY_MAP
                );
                
                detailaware.add(maskedworkers[i]);
                // "Peg" to sprite manager.
                maskedworkers[i].cacheSpriteManager(DOOM.spriteManager);
            }
        }
        
        @Override
        protected void InitColormaps() throws IOException {
            colormaps.colormaps = DOOM.graphicSystem.getColorMap();
            System.out.println("COLORS15 Colormaps: " + colormaps.colormaps.length);

            // MAES: blurry effect is hardcoded to this colormap.
            // Pointless, since we don't use indexes. Instead, a half-brite
            // processing works just fine.
            BLURRY_MAP = DOOM.graphicSystem.getBlurryTable();
        }
        
        @Override
        protected void R_InitDrawingFunctions() {

            // Span functions. Common to all renderers unless overriden
            // or unused e.g. parallel renderers ignore them.
            DrawSpan = new R_DrawSpanUnrolled.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dsvars, screen, DOOM.doomSystem);
            DrawSpanLow = new R_DrawSpanLow.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dsvars, screen, DOOM.doomSystem);

            // Translated columns are usually sprites-only.
            DrawTranslatedColumn = new R_DrawTranslatedColumn.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            DrawTranslatedColumnLow = new R_DrawTranslatedColumnLow.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            DrawTLColumn = new R_DrawTLColumn(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);

            // Fuzzy columns. These are also masked.
            DrawFuzzColumn = new R_DrawFuzzColumn.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem, BLURRY_MAP);
            DrawFuzzColumnLow = new R_DrawFuzzColumnLow.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem, BLURRY_MAP);

            // Regular draw for solid columns/walls. Full optimizations.
            DrawColumn = new R_DrawColumnBoomOpt.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dcvars, screen, DOOM.doomSystem);
            DrawColumnLow = new R_DrawColumnBoomOptLow.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dcvars, screen, DOOM.doomSystem);

            // Non-optimized stuff for masked.
            DrawColumnMasked = new R_DrawColumnBoom.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            DrawColumnMaskedLow = new R_DrawColumnBoomLow.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);

            // Player uses masked
            DrawColumnPlayer = DrawColumnMasked; // Player normally uses masked.

            // Skies use their own. This is done in order not to stomp parallel threads.
            DrawColumnSkies = new R_DrawColumnBoomOpt.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, skydcvars, screen, DOOM.doomSystem);
            DrawColumnSkiesLow = new R_DrawColumnBoomOptLow.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, skydcvars, screen, DOOM.doomSystem);

            super.R_InitDrawingFunctions();
        }
    }
    
    public static final class TrueColor extends ParallelRenderer2<byte[], int[]> {

        public TrueColor(DoomMain<byte[], int[]> DM, int wallthread, int floorthreads, int nummaskedthreads) {
            super(DM, wallthread, floorthreads, nummaskedthreads);

            // Init light levels
            colormaps.scalelight = new int[colormaps.lightLevels()][colormaps.maxLightScale()][];
            colormaps.scalelightfixed = new int[colormaps.maxLightScale()][];
            colormaps.zlight = new int[colormaps.lightLevels()][colormaps.maxLightZ()][];

            completeInit();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void InitRSISubsystem() {
            // int[] offsets = new int[NUMWALLTHREADS];
            final ParallelSegs2<byte[], int[]> parallelSegs = ((ParallelSegs2<byte[], int[]>) MySegs);
            for (int i = 0; i < NUMWALLTHREADS; i++) {
                parallelSegs.RSIExec[i] = new RenderSegExecutor.TrueColor(
                        DOOM, i, screen, TexMan,
                        parallelSegs.RSI, MySegs.getBLANKCEILINGCLIP(), MySegs.getBLANKFLOORCLIP(),
                        MySegs.getCeilingClip(), MySegs.getFloorClip(), columnofs, view.xtoviewangle,
                        ylookup, vp_vars.visplanes, this.visplanebarrier, colormaps
                );
                // SegExecutor sticks to its own half (or 1/nth) of the screen.
                parallelSegs.RSIExec[i].setScreenRange(i * (DOOM.vs.getScreenWidth() / NUMWALLTHREADS), (i + 1) * (DOOM.vs.getScreenWidth() / NUMWALLTHREADS));
                detailaware.add(parallelSegs.RSIExec[i]);
            }

            for (int i = 0; i < NUMFLOORTHREADS; i++) {
                final VisplaneWorker2<?, ?> w = new VisplaneWorker2.TrueColor(
                        DOOM, this, i, columnofs, ylookup, screen, visplanebarrier, NUMFLOORTHREADS
                );
                vpw[i] = w;
                detailaware.add(w);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void InitMaskedWorkers() {
            for (int i = 0; i < NUMMASKEDTHREADS; i++) {
                maskedworkers[i] = new MaskedWorker.TrueColor(
                    DOOM.vs, this, i, ylookup, columnofs, NUMMASKEDTHREADS, screen,
                    maskedbarrier, BLURRY_MAP
                );
                
                detailaware.add(maskedworkers[i]);
                // "Peg" to sprite manager.
                maskedworkers[i].cacheSpriteManager(DOOM.spriteManager);
            }
        }
        
        @Override
        protected void InitColormaps() throws IOException {
            colormaps.colormaps = DOOM.graphicSystem.getColorMap();
            System.out.println("COLORS15 Colormaps: " + colormaps.colormaps.length);

            // MAES: blurry effect is hardcoded to this colormap.
            // Pointless, since we don't use indexes. Instead, a half-brite
            // processing works just fine.
            BLURRY_MAP = DOOM.graphicSystem.getBlurryTable();
       }
        
        @Override
        protected void R_InitDrawingFunctions() {

            // Span functions. Common to all renderers unless overriden
            // or unused e.g. parallel renderers ignore them.
            DrawSpan = new R_DrawSpanUnrolled.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dsvars, screen, DOOM.doomSystem);
            DrawSpanLow = new R_DrawSpanLow.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dsvars, screen, DOOM.doomSystem);

            // Translated columns are usually sprites-only.
            DrawTranslatedColumn = new R_DrawTranslatedColumn.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            DrawTranslatedColumnLow = new R_DrawTranslatedColumnLow.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            //DrawTLColumn=new R_DrawTLColumn(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,maskedcvars,screen,I);

            // Fuzzy columns. These are also masked.
            DrawFuzzColumn = new R_DrawFuzzColumn.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem, BLURRY_MAP);
            DrawFuzzColumnLow = new R_DrawFuzzColumnLow.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem, BLURRY_MAP);

            // Regular draw for solid columns/walls. Full optimizations.
            DrawColumn = new R_DrawColumnBoomOpt.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dcvars, screen, DOOM.doomSystem);
            DrawColumnLow = new R_DrawColumnBoomOptLow.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dcvars, screen, DOOM.doomSystem);

            // Non-optimized stuff for masked.
            DrawColumnMasked = new R_DrawColumnBoom.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            DrawColumnMaskedLow = new R_DrawColumnBoomLow.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);

            // Player uses masked
            DrawColumnPlayer = DrawColumnMasked; // Player normally uses masked.

            // Skies use their own. This is done in order not to stomp parallel threads.
            DrawColumnSkies = new R_DrawColumnBoomOpt.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, skydcvars, screen, DOOM.doomSystem);
            DrawColumnSkiesLow = new R_DrawColumnBoomOptLow.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, skydcvars, screen, DOOM.doomSystem);

            super.R_InitDrawingFunctions();
        }
    }
}

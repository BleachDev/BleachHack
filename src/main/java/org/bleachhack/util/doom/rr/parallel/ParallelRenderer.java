package org.bleachhack.util.doom.rr.parallel;

import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.player_t;
import java.io.IOException;
import org.bleachhack.util.doom.rr.SimpleThings;
import org.bleachhack.util.doom.rr.drawfuns.ColVars;
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

/**
 * This is Mocha Doom's famous parallel software renderer. It builds on the
 * basic software renderer, but adds specialized handling for drawing segs
 * (walls) and spans (floors) in parallel. There's inherent parallelism between
 * walls and floor, and internal parallelism between walls and between floors.
 * However, visplane limits and openings need to be pre-computed before any
 * actual drawing starts, that's why rendering of walls is stored in "RWI"s or
 * "Render Wall Instructions", and then rendered once they are all in place and
 * the can be parallelized between rendering threads. Rendering of sprites is
 * NOT parallelized yet (and probably not worth it, at this point).
 * 
 * @author admin
 */

public abstract class ParallelRenderer<T, V> extends AbstractParallelRenderer<T, V> {

    public ParallelRenderer(DoomMain<T, V> DM, int wallthread,
            int floorthreads, int nummaskedthreads) {
        super(DM, wallthread, floorthreads, nummaskedthreads);
        
        // Register parallel seg drawer with list of RWI subsystems.
        ParallelSegs tmp= new ParallelSegs(this);
        this.MySegs = tmp;
        RWIs= tmp;
        
        this.MyThings = new SimpleThings<>(DM.vs, this);
        //this.MyPlanes = new Planes(this);// new ParallelPlanes<T, V>(DM.R);

    }

    /**
     * Default constructor, 1 seg, 1 span and two masked threads.
     * 
     * @param DM
     */
    public ParallelRenderer(DoomMain<T, V> DM) {
        this(DM, 1, 1, 2);
    }



    /**
     * R_RenderView As you can guess, this renders the player view of a
     * particular player object. In practice, it could render the view of any
     * mobj too, provided you adapt the SetupFrame method (where the viewing
     * variables are set).
     * 
     * @throws IOException
     */

    public void RenderPlayerView(player_t player) {

        // Viewing variables are set according to the player's mobj. Interesting
        // hacks like
        // free cameras or monster views can be done.
        SetupFrame(player);

        /*
         * Uncommenting this will result in a very existential experience if
         * (Math.random()>0.999){ thinker_t shit=P.getRandomThinker(); try {
         * mobj_t crap=(mobj_t)shit; player.mo=crap; } catch (ClassCastException
         * e){ } }
         */

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

        // System.out.printf("Submitted %d RWIs\n",RWIcount);

        MySegs.CompleteRendering();

        // Check for new console commands.
        DOOM.gameNetworking.NetUpdate();

        // "Warped floor" fixed, same-height visplane merging fixed.
        MyPlanes.DrawPlanes();

        // Check for new console commands.
        DOOM.gameNetworking.NetUpdate();

        MySegs.sync();
        MyPlanes.sync();

//            drawsegsbarrier.await();
//            visplanebarrier.await();


        MyThings.DrawMasked();

        // RenderRMIPipeline();
        /*
         * try { maskedbarrier.await(); } catch (Exception e) {
         * e.printStackTrace(); }
         */

        // Check for new console commands.
        DOOM.gameNetworking.NetUpdate();
    }

    public static final class Indexed extends ParallelRenderer<byte[], byte[]> {

        public Indexed(DoomMain<byte[], byte[]> DM, int wallthread,
                int floorthreads, int nummaskedthreads) {
            super(DM, wallthread, floorthreads, nummaskedthreads);

            // Init light levels
            colormaps.scalelight = new byte[colormaps.lightLevels()][colormaps.maxLightScale()][];
            colormaps.scalelightfixed = new byte[colormaps.maxLightScale()][];
            colormaps.zlight = new byte[colormaps.lightLevels()][colormaps.maxLightZ()][];

            completeInit();

        }

        /**
         * R_InitColormaps
         *
         * @throws IOException
         */
        @Override
        protected void InitColormaps() throws IOException {
            // Load in the light tables,
            // 256 byte align tables.
            colormaps.colormaps = DOOM.graphicSystem.getColorMap();

            // MAES: blurry effect is hardcoded to this colormap.
            BLURRY_MAP = DOOM.graphicSystem.getBlurryTable();
            // colormaps = (byte *)( ((int)colormaps + 255)&~0xff);     
        }

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

        protected void completeInit() {
            super.completeInit();
            InitMaskedWorkers();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void InitMaskedWorkers() {
            maskedworkers = new MaskedWorker[NUMMASKEDTHREADS];
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
        public RenderWallExecutor<byte[], byte[]>[] InitRWIExecutors(
                int num, ColVars<byte[], byte[]>[] RWI) {
            RenderWallExecutor<byte[], byte[]>[] tmp
                    = new RenderWallExecutor.Indexed[num];

            for (int i = 0; i < num; i++) {
                tmp[i] = new RenderWallExecutor.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), columnofs, ylookup, screen, RWI, drawsegsbarrier);
            }

            return tmp;
        }

    }

    @Override
    protected void InitParallelStuff() {

        // ...yeah, it works.
        if (!(RWIs == null)) {
            ColVars<T, V>[] RWI = RWIs.getRWI();
            RenderWallExecutor<T, V>[] RWIExec = InitRWIExecutors(NUMWALLTHREADS, RWI);
            RWIs.setExecutors(RWIExec);

            for (int i = 0; i < NUMWALLTHREADS; i++) {

                detailaware.add(RWIExec[i]);
            }
        }

        // CATCH: this must be executed AFTER screen is set, and
        // AFTER we initialize the RWI themselves,
        // before V is set (right?)
        // This actually only creates the necessary arrays and
        // barriers. Things aren't "wired" yet.
        // Using "render wall instruction" subsystem
        // Using masked sprites
        // RMIExec = new RenderMaskedExecutor[NUMMASKEDTHREADS];
        // Using
        //vpw = new Runnable[NUMFLOORTHREADS];
        //maskedworkers = new MaskedWorker.Indexed[NUMMASKEDTHREADS];
        // RWIcount = 0;
        // InitRWISubsystem();
        // InitRMISubsystem();
        // InitPlaneWorkers();
        // InitMaskedWorkers();
        // If using masked threads, set these too.
        TexMan.setSMPVars(NUMMASKEDTHREADS);

    }

    /*
     * private void InitPlaneWorkers(){ for (int i = 0; i < NUMFLOORTHREADS;
     * i++) { vpw[i] = new VisplaneWorker2(i,SCREENWIDTH, SCREENHEIGHT,
     * columnofs, ylookup, screen, visplanebarrier, NUMFLOORTHREADS);
     * //vpw[i].id = i; detailaware.add((IDetailAware) vpw[i]); } }
     */


        /*
         * TODO: relay to dependent objects. super.initScaling();
         * ColVars<byte[],byte[]> fake = new ColVars<byte[],byte[]>(); RWI =
         * C2JUtils.createArrayOfObjects(fake, SCREENWIDTH * 3); // Be MUCH more
         * generous with this one. RMI = C2JUtils.createArrayOfObjects(fake,
         * SCREENWIDTH * 6);
         */

    protected abstract void InitMaskedWorkers();

    public static final class HiColor extends ParallelRenderer<byte[], short[]> {

        public HiColor(DoomMain<byte[], short[]> DM, int wallthread,
                int floorthreads, int nummaskedthreads) {
            super(DM, wallthread, floorthreads, nummaskedthreads);

            // Init light levels
            colormaps.scalelight = new short[colormaps.lightLevels()][colormaps.maxLightScale()][];
            colormaps.scalelightfixed = new short[colormaps.maxLightScale()][];
            colormaps.zlight = new short[colormaps.lightLevels()][colormaps.maxLightZ()][];

            completeInit();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void InitMaskedWorkers() {
            maskedworkers = new MaskedWorker[NUMMASKEDTHREADS];
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

        /**
         * R_InitColormaps This is VERY different for hicolor.
         *
         * @throws IOException
         */
        protected void InitColormaps() throws IOException {

            colormaps.colormaps = DOOM.graphicSystem.getColorMap();
            System.out.println("COLORS15 Colormaps: " + colormaps.colormaps.length);

            // MAES: blurry effect is hardcoded to this colormap.
            // Pointless, since we don't use indexes. Instead, a half-brite
            // processing works just fine.
            BLURRY_MAP = DOOM.graphicSystem.getBlurryTable();
        }

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

        @Override
        public RenderWallExecutor<byte[], short[]>[] InitRWIExecutors(
                int num, ColVars<byte[], short[]>[] RWI) {
            RenderWallExecutor<byte[], short[]>[] tmp
                    = new RenderWallExecutor.HiColor[num];

            for (int i = 0; i < num; i++) {
                tmp[i] = new RenderWallExecutor.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), columnofs, ylookup, screen, RWI, drawsegsbarrier);
            }

            return tmp;
        }

    }

    public static final class TrueColor extends ParallelRenderer<byte[], int[]> {

        public TrueColor(DoomMain<byte[], int[]> DM, int wallthread,
                int floorthreads, int nummaskedthreads) {
            super(DM, wallthread, floorthreads, nummaskedthreads);

            // Init light levels
            colormaps.scalelight = new int[colormaps.lightLevels()][colormaps.maxLightScale()][];
            colormaps.scalelightfixed = new int[colormaps.maxLightScale()][];
            colormaps.zlight = new int[colormaps.lightLevels()][colormaps.maxLightZ()][];

            completeInit();
        }

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

        /**
         * R_InitColormaps This is VERY different for hicolor.
         *
         * @throws IOException
         */
        protected void InitColormaps()
                throws IOException {

            colormaps.colormaps = DOOM.graphicSystem.getColorMap();
            System.out.println("COLORS15 Colormaps: " + colormaps.colormaps.length);

            // MAES: blurry effect is hardcoded to this colormap.
            // Pointless, since we don't use indexes. Instead, a half-brite
            // processing works just fine.
            BLURRY_MAP = DOOM.graphicSystem.getBlurryTable();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void InitMaskedWorkers() {
            maskedworkers = new MaskedWorker[NUMMASKEDTHREADS];
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
        public RenderWallExecutor<byte[], int[]>[] InitRWIExecutors(
                int num, ColVars<byte[], int[]>[] RWI) {
            RenderWallExecutor<byte[], int[]>[] tmp
                    = new RenderWallExecutor.TrueColor[num];

            for (int i = 0; i < num; i++) {
                tmp[i] = new RenderWallExecutor.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), columnofs, ylookup, screen, RWI, drawsegsbarrier);
            }

            return tmp;
        }

    }

}

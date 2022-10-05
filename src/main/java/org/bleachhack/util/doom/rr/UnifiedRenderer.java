package org.bleachhack.util.doom.rr;

import org.bleachhack.util.doom.doom.DoomMain;
import java.io.IOException;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawColumnBoom;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawColumnBoomLow;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawColumnBoomOpt;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawColumnBoomOptLow;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawFuzzColumn;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawFuzzColumnLow;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawSpan;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawSpanLow;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawTLColumn;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawTranslatedColumn;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawTranslatedColumnLow;

public abstract class UnifiedRenderer<T, V> extends RendererState<T, V> {

    public UnifiedRenderer(DoomMain<T, V> DOOM) {
        super(DOOM);
        this.MySegs = new Segs(this);
    }

    /**
     * A very simple Seg (Wall) drawer, which just completes abstract SegDrawer by calling the final column functions.
     *
     * TODO: move out of RendererState.
     *
     * @author velktron
     *
     */
    protected final class Segs
            extends SegDrawer {

        public Segs(SceneRenderer<?, ?> R) {
            super(R);
        }

        /**
         * For serial version, just complete the call
         */
        @Override
        protected final void CompleteColumn() {
            colfunc.main.invoke();
        }

    }

    ////////////////// The actual rendering calls ///////////////////////
    public static final class HiColor extends UnifiedRenderer<byte[], short[]> {

        public HiColor(DoomMain<byte[], short[]> DOOM) {
            super(DOOM);

            // Init any video-output dependant stuff            
            // Init light levels
            final int LIGHTLEVELS = colormaps.lightLevels();
            final int MAXLIGHTSCALE = colormaps.maxLightScale();
            final int MAXLIGHTZ = colormaps.maxLightZ();
            
            colormaps.scalelight = new short[LIGHTLEVELS][MAXLIGHTSCALE][];
            colormaps.scalelightfixed = new short[MAXLIGHTSCALE][];
            colormaps.zlight = new short[LIGHTLEVELS][MAXLIGHTZ][];

            completeInit();
        }

        /**
         * R_InitColormaps This is VERY different for hicolor.
         *
         * @throws IOException
         */
        @Override
        protected void InitColormaps() throws IOException {
            colormaps.colormaps = DOOM.graphicSystem.getColorMap();
            System.out.println("COLORS15 Colormaps: " + colormaps.colormaps.length);

            // MAES: blurry effect is hardcoded to this colormap.
            BLURRY_MAP = DOOM.graphicSystem.getBlurryTable();
        }

        /**
         * Initializes the various drawing functions. They are all "pegged" to the same dcvars/dsvars object. Any
         * initializations of e.g. parallel renderers and their supporting subsystems should occur here.
         */
        @Override
        protected void R_InitDrawingFunctions() {

            // Span functions. Common to all renderers unless overriden
            // or unused e.g. parallel renderers ignore them.
            DrawSpan = new R_DrawSpan.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dsvars, screen, DOOM.doomSystem);
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

    public static final class Indexed extends UnifiedRenderer<byte[], byte[]> {

        public Indexed(DoomMain<byte[], byte[]> DOOM) {
            super(DOOM);
            
            // Init light levels
            final int LIGHTLEVELS = colormaps.lightLevels();
            final int MAXLIGHTSCALE = colormaps.maxLightScale();
            final int MAXLIGHTZ = colormaps.maxLightZ();
            
            colormaps.scalelight = new byte[LIGHTLEVELS][MAXLIGHTSCALE][];
            colormaps.scalelightfixed = new byte[MAXLIGHTSCALE][];
            colormaps.zlight = new byte[LIGHTLEVELS][MAXLIGHTZ][];

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

        /**
         * Initializes the various drawing functions. They are all "pegged" to the same dcvars/dsvars object. Any
         * initializations of e.g. parallel renderers and their supporting subsystems should occur here.
         */
        @Override
        protected void R_InitDrawingFunctions() {

            // Span functions. Common to all renderers unless overriden
            // or unused e.g. parallel renderers ignore them.
            DrawSpan = new R_DrawSpan.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dsvars, screen, DOOM.doomSystem);
            DrawSpanLow = new R_DrawSpanLow.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dsvars, screen, DOOM.doomSystem);
            // Translated columns are usually sprites-only.
            DrawTranslatedColumn = new R_DrawTranslatedColumn.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            DrawTranslatedColumnLow = new R_DrawTranslatedColumnLow.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            //DrawTLColumn=new R_DrawTLColumn(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,maskedcvars,screen,I);

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

    public static final class TrueColor extends UnifiedRenderer<byte[], int[]> {

        public TrueColor(DoomMain<byte[], int[]> DOOM) {
            super(DOOM);

            // Init light levels
            final int LIGHTLEVELS = colormaps.lightLevels();
            final int MAXLIGHTSCALE = colormaps.maxLightScale();
            final int MAXLIGHTZ = colormaps.maxLightZ();
            
            colormaps.scalelight = new int[LIGHTLEVELS][MAXLIGHTSCALE][];
            colormaps.scalelightfixed = new int[MAXLIGHTSCALE][];
            colormaps.zlight = new int[LIGHTLEVELS][MAXLIGHTZ][];

            completeInit();
        }

        /**
         * R_InitColormaps This is VERY different for hicolor.
         *
         * @throws IOException
         */
        protected void InitColormaps() throws IOException {
            colormaps.colormaps = DOOM.graphicSystem.getColorMap();
            System.out.println("COLORS32 Colormaps: " + colormaps.colormaps.length);

            // MAES: blurry effect is hardcoded to this colormap.
            BLURRY_MAP = DOOM.graphicSystem.getBlurryTable();
        }

        /**
         * Initializes the various drawing functions. They are all "pegged" to the same dcvars/dsvars object. Any
         * initializations of e.g. parallel renderers and their supporting subsystems should occur here.
         */
        @Override
        protected void R_InitDrawingFunctions() {

            // Span functions. Common to all renderers unless overriden
            // or unused e.g. parallel renderers ignore them.
            DrawSpan = new R_DrawSpan.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dsvars, screen, DOOM.doomSystem);
            DrawSpanLow = new R_DrawSpanLow.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dsvars, screen, DOOM.doomSystem);
            // Translated columns are usually sprites-only.
            DrawTranslatedColumn = new R_DrawTranslatedColumn.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            DrawTranslatedColumnLow = new R_DrawTranslatedColumnLow.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            //DrawTLColumn=new R_DrawTLColumn.TrueColor(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,maskedcvars,screen,I);

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

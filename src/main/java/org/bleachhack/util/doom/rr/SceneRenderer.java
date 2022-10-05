package org.bleachhack.util.doom.rr;

import static org.bleachhack.util.doom.data.Tables.FINEANGLES;
import org.bleachhack.util.doom.doom.SourceCode.R_Draw;
import static org.bleachhack.util.doom.doom.SourceCode.R_Draw.R_FillBackScreen;
import org.bleachhack.util.doom.doom.player_t;
import org.bleachhack.util.doom.i.IDoomSystem;
import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import org.bleachhack.util.doom.rr.drawfuns.ColFuncs;
import org.bleachhack.util.doom.rr.drawfuns.ColVars;
import org.bleachhack.util.doom.rr.drawfuns.SpanVars;
import org.bleachhack.util.doom.v.tables.LightsAndColors;
import org.bleachhack.util.doom.wad.IWadLoader;

public interface SceneRenderer<T, V> {

    /**
     * Fineangles in the SCREENWIDTH wide window.
     */
    public static final int FIELDOFVIEW = FINEANGLES / 4;
    public static final int MINZ = (FRACUNIT * 4);
    public static final int FUZZTABLE = 50;

    /**
     * killough: viewangleoffset is a legacy from the pre-v1.2 days, when Doom
     * had Left/Mid/Right viewing. +/-ANG90 offsets were placed here on each
     * node, by d_net.c, to set up a L/M/R session.
     */
    public static final long viewangleoffset = 0;

    public void Init();
    public void RenderPlayerView(player_t player);
    public void ExecuteSetViewSize();
    @R_Draw.C(R_FillBackScreen)
    public void FillBackScreen();
    public void DrawViewBorder();
    public void SetViewSize(int size, int detaillevel);
    public long PointToAngle2(int x1, int y1, int x2, int y2);
    public void PreCacheThinkers();
    public int getValidCount();
    public void increaseValidCount(int amount);
    public boolean isFullHeight();
    public void resetLimits();
    public boolean getSetSizeNeeded();
    public boolean isFullScreen();

    // Isolation methods
    public TextureManager<T> getTextureManager();
    public PlaneDrawer<T, V> getPlaneDrawer();
    public ViewVars getView();
    public SpanVars<T, V> getDSVars();
    public LightsAndColors<V> getColorMap();
    public IDoomSystem getDoomSystem();
    public IWadLoader getWadLoader();

    /**
     * Use this to "peg" visplane drawers (even parallel ones) to
     * the same set of visplane variables.
     *
     * @return
     */
    public Visplanes getVPVars();
    public SegVars getSegVars();
    public ISpriteManager getSpriteManager();
    public BSPVars getBSPVars();
    public IVisSpriteManagement<V> getVisSpriteManager();
    public ColFuncs<T, V> getColFuncsHi();
    public ColFuncs<T, V> getColFuncsLow();
    public ColVars<T, V> getMaskedDCVars();

    //public subsector_t PointInSubsector(int x, int y);
}

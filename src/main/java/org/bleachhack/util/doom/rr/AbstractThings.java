package org.bleachhack.util.doom.rr;

import static org.bleachhack.util.doom.data.Defines.FF_FRAMEMASK;
import static org.bleachhack.util.doom.data.Defines.FF_FULLBRIGHT;
import static org.bleachhack.util.doom.data.Defines.SIL_BOTTOM;
import static org.bleachhack.util.doom.data.Defines.SIL_TOP;
import static org.bleachhack.util.doom.data.Defines.pw_invisibility;
import static org.bleachhack.util.doom.doom.player_t.NUMPSPRITES;
import org.bleachhack.util.doom.i.IDoomSystem;
import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;
import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import static org.bleachhack.util.doom.m.fixed_t.FixedMul;
import static org.bleachhack.util.doom.p.mobj_t.MF_TRANSLATION;
import org.bleachhack.util.doom.p.pspdef_t;
import org.bleachhack.util.doom.rr.drawfuns.ColFuncs;
import org.bleachhack.util.doom.rr.drawfuns.ColVars;
import org.bleachhack.util.doom.rr.drawfuns.ColumnFunction;
import static org.bleachhack.util.doom.rr.line_t.ML_DONTPEGBOTTOM;
import org.bleachhack.util.doom.v.graphics.Palettes;
import org.bleachhack.util.doom.v.scale.VideoScale;
import org.bleachhack.util.doom.v.tables.LightsAndColors;
import org.bleachhack.util.doom.wad.IWadLoader;

/**
 * Refresh of things, i.e. objects represented by sprites. This abstract
 * class is the base for all implementations, and contains the gory clipping
 * and priority stuff. It can terminate by drawing directly, or by buffering
 * into a pipeline for parallelized drawing.
 * 
 * It need to be aware of almost everything in the renderer, which means that
 * it's a PITA to keep "disembodied". Then again, this probably means it's more 
 * extensible...
 * 
 * 
 * 
 */

public abstract class AbstractThings<T,V> implements IMaskedDrawer<T,V> {

    private final static boolean RANGECHECK=false;
    
    protected short[] maskedtexturecol;
    protected int pmaskedtexturecol = 0;
    
    // Cache those you get from the sprite manager
    protected int[] spritewidth, spriteoffset, spritetopoffset;

    /** fixed_t */
    protected int pspritescale, pspriteiscale, pspritexscale,
            pspriteyscale, skyscale;

    // Used for masked segs
    protected int rw_scalestep;

    protected int spryscale;

    protected int sprtopscreen;
    
    protected short[] mfloorclip;

    protected int p_mfloorclip;

    protected short[] mceilingclip;

    protected int p_mceilingclip;

    protected sector_t frontsector;

    protected sector_t backsector;

    // This must be "pegged" to the one used by the default renderer.
    protected ColVars<T, V> maskedcvars;
    
    protected ColumnFunction<T,V> colfunc;
    protected ColFuncs<T,V> colfuncs;
    protected ColFuncs<T,V> colfuncshi;
    protected ColFuncs<T,V> colfuncslow;
    protected final VideoScale vs;
    protected final LightsAndColors<V> colormaps;
    protected final ViewVars view;
    protected final SegVars seg_vars;
    protected final TextureManager<T> TexMan;
    protected final IDoomSystem I;
    protected final ISpriteManager SM;
    protected final BSPVars MyBSP;
    protected final IVisSpriteManagement<V> VIS;
    protected final IWadLoader W;
    protected final vissprite_t<V> avis;
    
    public AbstractThings(VideoScale vs, SceneRenderer<T,V> R) {
        this.colfuncshi = R.getColFuncsHi();
        this.colfuncslow = R.getColFuncsLow();
        this.colormaps = R.getColorMap();
        this.view = R.getView();
        this.seg_vars = R.getSegVars();
        this.TexMan = R.getTextureManager();
        this.I = R.getDoomSystem();
        this.SM = R.getSpriteManager();
        this.MyBSP = R.getBSPVars();
        this.VIS = R.getVisSpriteManager();
        this.W = R.getWadLoader();
        this.avis = new vissprite_t<V>();
        this.maskedcvars = R.getMaskedDCVars();
        this.vs = vs;
        clipbot = new short[vs.getScreenWidth()];
        cliptop = new short[vs.getScreenWidth()];
    }

    @Override
    public void cacheSpriteManager(ISpriteManager SM) {
        this.spritewidth = SM.getSpriteWidth();
        this.spriteoffset = SM.getSpriteOffset();
        this.spritetopoffset = SM.getSpriteTopOffset();
    }

    /**
     * R_DrawVisSprite mfloorclip and mceilingclip should also be set.
     * Sprites are actually drawn here. MAES: Optimized. No longer needed to
     * pass x1 and x2 parameters (useless) +2 fps on nuts.wad timedemo.
     */
    @SuppressWarnings("unchecked")
    protected void DrawVisSprite(vissprite_t<V> vis) {
        column_t column;
        int texturecolumn;
        int frac; // fixed_t
        patch_t patch;

        // At this point, the view angle (and patch) has already been
        // chosen. Go back.
        patch = W.CachePatchNum(vis.patch + SM.getFirstSpriteLump());

        maskedcvars.dc_colormap = vis.colormap;
        // colfunc=glasscolfunc;
        if (maskedcvars.dc_colormap == null) {
            // NULL colormap = shadow draw
            colfunc = colfuncs.fuzz;
        } else if ((vis.mobjflags & MF_TRANSLATION) != 0) {
            colfunc = colfuncs.trans;
            maskedcvars.dc_translation = (T) colormaps.getTranslationTable(vis.mobjflags);
        }

        maskedcvars.dc_iscale = Math.abs(vis.xiscale) >> view.detailshift;
        maskedcvars.dc_texturemid = vis.texturemid;
        frac = vis.startfrac;
        spryscale = vis.scale;
        sprtopscreen =
            view.centeryfrac
                    - FixedMul(maskedcvars.dc_texturemid, spryscale);

        // A texture height of 0 means "not tiling" and holds for
        // all sprite/masked renders.
        maskedcvars.dc_texheight = 0;

        for (maskedcvars.dc_x = vis.x1; maskedcvars.dc_x <= vis.x2; maskedcvars.dc_x++, frac +=
            vis.xiscale) {
            texturecolumn = frac >> FRACBITS;
            if (RANGECHECK) {
                if (texturecolumn < 0 || texturecolumn >= patch.width)
                    I.Error("R_DrawSpriteRange: bad texturecolumn");
            }
            column = patch.columns[texturecolumn];
            DrawMaskedColumn(column);
        }

        colfunc = colfuncs.masked;
    }

    /**
     * R_RenderMaskedSegRange
     * 
     * @param ds
     * @param x1
     * @param x2
     */
    protected void RenderMaskedSegRange(drawseg_t ds, int x1, int x2) {
        int index;

        int lightnum;
        int texnum;

        // System.out.printf("RenderMaskedSegRange from %d to %d\n",x1,x2);

        // Calculate light table.
        // Use different light tables
        // for horizontal / vertical / diagonal. Diagonal?
        // OPTIMIZE: get rid of LIGHTSEGSHIFT globally
        MyBSP.curline = ds.curline;
        frontsector = MyBSP.curline.frontsector;
        backsector = MyBSP.curline.backsector;
        texnum = TexMan.getTextureTranslation(MyBSP.curline.sidedef.midtexture);
        // System.out.print(" for texture "+textures[texnum].name+"\n:");
        lightnum =
            (frontsector.lightlevel >> colormaps.lightSegShift()) + colormaps.extralight;

        if (MyBSP.curline.v1y == MyBSP.curline.v2y)
            lightnum--;
        else if (MyBSP.curline.v1x == MyBSP.curline.v2x)
            lightnum++;

        // Killough code.
        colormaps.walllights =
            lightnum >= colormaps.lightLevels() ? colormaps.scalelight[colormaps.lightLevels() - 1]
                    : lightnum < 0 ? colormaps.scalelight[0]
                            : colormaps.scalelight[lightnum];

        // Get the list
        maskedtexturecol = ds.getMaskedTextureColList();
        // And this is the pointer.
        pmaskedtexturecol = ds.getMaskedTextureColPointer();

        rw_scalestep = ds.scalestep;
        spryscale = ds.scale1 + (x1 - ds.x1) * rw_scalestep;

        // HACK to get "pointers" inside clipping lists
        mfloorclip = ds.getSprBottomClipList();
        p_mfloorclip = ds.getSprBottomClipPointer();
        mceilingclip = ds.getSprTopClipList();
        p_mceilingclip = ds.getSprTopClipPointer();
        // find positioning
        if ((MyBSP.curline.linedef.flags & ML_DONTPEGBOTTOM) != 0) {
            maskedcvars.dc_texturemid =
                frontsector.floorheight > backsector.floorheight ? frontsector.floorheight
                        : backsector.floorheight;
            maskedcvars.dc_texturemid =
                maskedcvars.dc_texturemid + TexMan.getTextureheight(texnum)
                        - view.z;
        } else {
            maskedcvars.dc_texturemid =
                frontsector.ceilingheight < backsector.ceilingheight ? frontsector.ceilingheight
                        : backsector.ceilingheight;
            maskedcvars.dc_texturemid = maskedcvars.dc_texturemid - view.z;
        }
        maskedcvars.dc_texturemid += MyBSP.curline.sidedef.rowoffset;

        if (colormaps.fixedcolormap != null)
            maskedcvars.dc_colormap = colormaps.fixedcolormap;

        // Texture height must be set at this point. This will trigger
        // tiling. For sprites, it should be set to 0.
        maskedcvars.dc_texheight =
            TexMan.getTextureheight(texnum) >> FRACBITS;

        // draw the columns
        for (maskedcvars.dc_x = x1; maskedcvars.dc_x <= x2; maskedcvars.dc_x++) {
            // calculate lighting
            if (maskedtexturecol[pmaskedtexturecol + maskedcvars.dc_x] != Short.MAX_VALUE) {
                if (colormaps.fixedcolormap == null) {
                    index = spryscale >>> colormaps.lightScaleShift();

                    if (index >= colormaps.maxLightScale())
                        index = colormaps.maxLightScale() - 1;

                    maskedcvars.dc_colormap = colormaps.walllights[index];
                }

                sprtopscreen =
                    view.centeryfrac
                            - FixedMul(maskedcvars.dc_texturemid, spryscale);
                maskedcvars.dc_iscale = (int) (0xffffffffL / spryscale);

                // draw the texture
                column_t data = TexMan.GetColumnStruct(texnum,
                    (int) maskedtexturecol[pmaskedtexturecol
                            + maskedcvars.dc_x]);// -3);

                DrawMaskedColumn(data);

                maskedtexturecol[pmaskedtexturecol + maskedcvars.dc_x] =
                    Short.MAX_VALUE;
            }
            spryscale += rw_scalestep;
        }

    }

    /**
     * R_DrawPSprite Draws a "player sprite" with slighly different rules
     * than normal sprites. This is actually a PITA, at best :-/
     */

    protected void DrawPSprite(pspdef_t psp) {

        int tx;
        int x1;
        int x2;
        spritedef_t sprdef;
        spriteframe_t sprframe;
        vissprite_t<V> vis;
        int lump;
        boolean flip;

        // decide which patch to use (in terms of angle?)
        if (RANGECHECK) {
            if (psp.state.sprite.ordinal() >= SM.getNumSprites())
                I.Error("R_ProjectSprite: invalid sprite number %d ",
                    psp.state.sprite);
        }

        sprdef = SM.getSprite(psp.state.sprite.ordinal());

        if (RANGECHECK) {
            if ((psp.state.frame & FF_FRAMEMASK) >= sprdef.numframes)
                I.Error("R_ProjectSprite: invalid sprite frame %d : %d ",
                    psp.state.sprite, psp.state.frame);
        }

        sprframe = sprdef.spriteframes[psp.state.frame & FF_FRAMEMASK];

        // Base frame for "angle 0" aka viewed from dead-front.
        lump = sprframe.lump[0];
        // Q: where can this be set? A: at sprite loadtime.
        flip = (boolean) (sprframe.flip[0] != 0);

        // calculate edges of the shape. tx is expressed in "view units".
        tx = (int) (FixedMul(psp.sx, view.BOBADJUST) - view.WEAPONADJUST);

        tx -= spriteoffset[lump];

        // So...centerxfrac is the center of the screen (pixel coords in
        // fixed point).
        x1 = (view.centerxfrac + FixedMul(tx, pspritescale)) >> FRACBITS;

        // off the right side
        if (x1 > view.width)
            return;

        tx += spritewidth[lump];
        x2 =
            ((view.centerxfrac + FixedMul(tx, pspritescale)) >> FRACBITS) - 1;

        // off the left side
        if (x2 < 0)
            return;

        // store information in a vissprite ?
        vis = avis;
        vis.mobjflags = 0;
        vis.texturemid =
            ((BASEYCENTER + view.lookdir) << FRACBITS) + FRACUNIT / 2
                    - (psp.sy - spritetopoffset[lump]);
        vis.x1 = x1 < 0 ? 0 : x1;
        vis.x2 = x2 >= view.width ? view.width - 1 : x2;
        vis.scale = (pspritescale) << view.detailshift;

        if (flip) {
            vis.xiscale = -pspriteiscale;
            vis.startfrac = spritewidth[lump] - 1;
        } else {
            vis.xiscale = pspriteiscale;
            vis.startfrac = 0;
        }

        if (vis.x1 > x1)
            vis.startfrac += vis.xiscale * (vis.x1 - x1);

        vis.patch = lump;

        if ((view.player.powers[pw_invisibility] > 4 * 32)
                || (view.player.powers[pw_invisibility] & 8) != 0) {
            // shadow draw
            vis.colormap = null;

        } else if (colormaps.fixedcolormap != null) {
            // fixed color
            vis.colormap = colormaps.fixedcolormap;
            // vis.pcolormap=0;
        } else if ((psp.state.frame & FF_FULLBRIGHT) != 0) {
            // full bright
            vis.colormap = colormaps.colormaps[Palettes.COLORMAP_FIXED];
            // vis.pcolormap=0;
        } else {
            // local light
            vis.colormap = colormaps.spritelights[colormaps.maxLightScale() - 1];
        }

        // System.out.println("Weapon draw "+vis);
        DrawVisSprite(vis);
    }

    protected int PSpriteSY[] = { 0, // staff
            5 * FRACUNIT, // goldwand
            15 * FRACUNIT, // crossbow
            15 * FRACUNIT, // blaster
            15 * FRACUNIT, // skullrod
            15 * FRACUNIT, // phoenix rod
            15 * FRACUNIT, // mace
            15 * FRACUNIT, // gauntlets
            15 * FRACUNIT // beak
        };

    /**
     * R_DrawPlayerSprites This is where stuff like guns is drawn...right?
     */

    protected final void DrawPlayerSprites() {
        int i;
        int lightnum;
        pspdef_t psp;

        // get light level
        lightnum =
            (view.player.mo.subsector.sector.lightlevel >> colormaps.lightSegShift())
                    + colormaps.extralight;

        if (lightnum < 0)
            colormaps.spritelights = colormaps.scalelight[0];
        else if (lightnum >= colormaps.lightLevels())
            colormaps.spritelights = colormaps.scalelight[colormaps.lightLevels() - 1];
        else
            colormaps.spritelights = colormaps.scalelight[lightnum];

        // clip to screen bounds
        mfloorclip = view.screenheightarray;
        p_mfloorclip = 0;
        mceilingclip = view.negonearray;
        p_mceilingclip = 0;

        // add all active psprites
        // MAES 25/5/2011 Fixed another stupid bug that prevented
        // PSP from actually being updated. This in turn uncovered
        // other bugs in the way psp and state were treated, and the way
        // flash states were set. It should be OK now.
        for (i = 0; i < NUMPSPRITES; i++) {
            psp = view.player.psprites[i];
            if (psp.state != null && psp.state.id != 0) {
                DrawPSprite(psp);
            }
        }
    }

    // MAES: Scale to vs.getScreenWidth()
    protected short[] clipbot;

    protected short[] cliptop;

    /**
     * R_DrawSprite
     */

    protected final void DrawSprite(vissprite_t<V> spr) {
        int ds;
        drawseg_t dss;

        int x;
        int r1;
        int r2;
        int scale; // fixed
        int lowscale; // fixed
        int silhouette;

        for (x = spr.x1; x <= spr.x2; x++)
            clipbot[x] = cliptop[x] = -2;

        // Scan drawsegs from end to start for obscuring segs.
        // The first drawseg that has a greater scale
        // is the clip seg.
        for (ds = seg_vars.ds_p - 1; ds >= 0; ds--) {
            // determine if the drawseg obscures the sprite
            // System.out.println("Drawseg "+ds+"of "+(ds_p-1));
            dss = seg_vars.drawsegs[ds];
            if (dss.x1 > spr.x2
                    || dss.x2 < spr.x1
                    || ((dss.silhouette == 0) && (dss
                            .nullMaskedTextureCol()))) {
                // does not cover sprite
                continue;
            }

            r1 = dss.x1 < spr.x1 ? spr.x1 : dss.x1;
            r2 = dss.x2 > spr.x2 ? spr.x2 : dss.x2;

            if (dss.scale1 > dss.scale2) {
                lowscale = dss.scale2;
                scale = dss.scale1;
            } else {
                lowscale = dss.scale1;
                scale = dss.scale2;
            }

            if (scale < spr.scale
                    || (lowscale < spr.scale && (dss.curline
                            .PointOnSegSide(spr.gx, spr.gy) == 0))) {
                // masked mid texture?
                if (!dss.nullMaskedTextureCol())
                    RenderMaskedSegRange(dss, r1, r2);
                // seg is behind sprite
                continue;
            }

            // clip this piece of the sprite
            silhouette = dss.silhouette;

            if (spr.gz >= dss.bsilheight)
                silhouette &= ~SIL_BOTTOM;

            if (spr.gzt <= dss.tsilheight)
                silhouette &= ~SIL_TOP;

            // BOTTOM clipping
            if (silhouette == 1) {
                // bottom sil
                for (x = r1; x <= r2; x++)
                    if (clipbot[x] == -2)
                        clipbot[x] = dss.getSprBottomClip(x);

            } else if (silhouette == 2) {
                // top sil
                for (x = r1; x <= r2; x++)
                    if (cliptop[x] == -2)
                        cliptop[x] = dss.getSprTopClip(x);
            } else if (silhouette == 3) {
                // both
                for (x = r1; x <= r2; x++) {
                    if (clipbot[x] == -2)
                        clipbot[x] = dss.getSprBottomClip(x);
                    if (cliptop[x] == -2)
                        cliptop[x] = dss.getSprTopClip(x);
                }
            }

        }

        // all clipping has been performed, so draw the sprite

        // check for unclipped columns
        for (x = spr.x1; x <= spr.x2; x++) {
            if (clipbot[x] == -2)
                clipbot[x] = (short) view.height;
            // ?? What's this bullshit?
            if (cliptop[x] == -2)
                cliptop[x] = -1;
        }

        mfloorclip = clipbot;
        p_mfloorclip = 0;
        mceilingclip = cliptop;
        p_mceilingclip = 0;
        DrawVisSprite(spr);
    }

    /**
     * R_DrawMasked Sorts and draws vissprites (room for optimization in
     * sorting func.) Draws masked textures. Draws player weapons and
     * overlays (psprites). Sorting function can be swapped for almost
     * anything, and it will work better, in-place and be simpler to draw,
     * too.
     */

    @Override
    public void DrawMasked() {
        // vissprite_t spr;
        int ds;
        drawseg_t dss;

        // Well, it sorts visspite objects.
        // It actually IS faster to sort with comparators, but you need to
        // go into NUTS.WAD-like wads.
        // numbers. The built-in sort if about as good as it gets. In fact,
        // it's hardly slower
        // to draw sprites without sorting them when using the built-in
        // modified mergesort, while
        // the original algorithm is so dreadful it actually does slow
        // things down.

        VIS.SortVisSprites();

        // If you are feeling adventurous, try these ones. They *might*
        // perform
        // better in very extreme situations where all sprites are always on
        // one side
        // of your view, but I hardly see any benefits in that. They are
        // both
        // much better than the original anyway.

        // combSort(vissprites,vissprite_p);
        // shellsort(vissprites,vissprite_p);

        // pQuickSprite.sort(vissprites);

        // The original sort. It's incredibly bad on so many levels (uses a
        // separate
        // linked list for the sorted sequence, which is pointless since the
        // vissprite_t
        // array is gonna be changed all over in the next frame anyway, it's
        // not like
        // it helps preseving or anything. It does work in Java too, but I'd
        // say to Keep Away. No srsly.

        /*
         * SortVisSprites (); // Sprite "0" not visible? /*if (vissprite_p >
         * 0) { // draw all vissprites back to front for (spr =
         * vsprsortedhead.next ; spr != vsprsortedhead ; spr=spr.next) {
         * DrawSprite (spr); } }
         */

        // After using in-place sorts, sprites can be drawn as simply as
        // that.

        colfunc = colfuncs.masked; // Sprites use fully-masked capable
                                 // function.

        final vissprite_t<V>[] vissprites = VIS.getVisSprites();
        final int numvissprites = VIS.getNumVisSprites();

        for (int i = 0; i < numvissprites; i++) {
            DrawSprite(vissprites[i]);
        }

        // render any remaining masked mid textures
        for (ds = seg_vars.ds_p - 1; ds >= 0; ds--) {
            dss = seg_vars.drawsegs[ds];
            if (!dss.nullMaskedTextureCol())
                RenderMaskedSegRange(dss, dss.x1, dss.x2);
        }
        // draw the psprites on top of everything
        // but does not draw on side views
        // if (viewangleoffset==0)

        colfunc = colfuncs.player;
        DrawPlayerSprites();
        colfunc = colfuncs.masked;
    }

    /**
     * R_DrawMaskedColumn Used for sprites and masked mid textures. Masked
     * means: partly transparent, i.e. stored in posts/runs of opaque
     * pixels. NOTE: this version accepts raw bytes, in case you know what
     * you're doing.
     */

   /* protected final void DrawMaskedColumn(T column) {
        int topscreen;
        int bottomscreen;
        int basetexturemid; // fixed_t
        int topdelta;
        int length;

        basetexturemid = maskedcvars.dc_texturemid;
        // That's true for the whole column.
        maskedcvars.dc_source = (T) column;
        int pointer = 0;

        // for each post...
        while ((topdelta = 0xFF & column[pointer]) != 0xFF) {
            // calculate unclipped screen coordinates
            // for post
            topscreen = sprtopscreen + spryscale * topdelta;
            length = 0xff & column[pointer + 1];
            bottomscreen = topscreen + spryscale * length;

            maskedcvars.dc_yl = (topscreen + FRACUNIT - 1) >> FRACBITS;
            maskedcvars.dc_yh = (bottomscreen - 1) >> FRACBITS;

            if (maskedcvars.dc_yh >= mfloorclip[p_mfloorclip
                    + maskedcvars.dc_x])
                maskedcvars.dc_yh =
                    mfloorclip[p_mfloorclip + maskedcvars.dc_x] - 1;

            if (maskedcvars.dc_yl <= mceilingclip[p_mceilingclip
                    + maskedcvars.dc_x])
                maskedcvars.dc_yl =
                    mceilingclip[p_mceilingclip + maskedcvars.dc_x] + 1;

            // killough 3/2/98, 3/27/98: Failsafe against overflow/crash:
            if (maskedcvars.dc_yl <= maskedcvars.dc_yh
                    && maskedcvars.dc_yh < view.height) {
                // Set pointer inside column to current post's data
                // Rremember, it goes {postlen}{postdelta}{pad}[data]{pad}
                maskedcvars.dc_source_ofs = pointer + 3;
                maskedcvars.dc_texturemid =
                    basetexturemid - (topdelta << FRACBITS);

                // Drawn by either R_DrawColumn
                // or (SHADOW) R_DrawFuzzColumn.
                maskedcvars.dc_texheight = 0; // Killough

                completeColumn();
            }
            pointer += length + 4;
        }

        maskedcvars.dc_texturemid = basetexturemid;
    }
    */


    /**
     * R_DrawMaskedColumn Used for sprites and masked mid textures. Masked
     * means: partly transparent, i.e. stored in posts/runs of opaque
     * pixels. FIXME: while it does work with "raw columns", if the initial
     * post is drawn outside of the screen the rest appear screwed up.
     * SOLUTION: use the version taking raw byte[] arguments.
     */

    @SuppressWarnings("unchecked")
    protected final void DrawMaskedColumn(column_t column) {
        int topscreen;
        int bottomscreen;
        int basetexturemid; // fixed_t

        basetexturemid = maskedcvars.dc_texturemid;
        // That's true for the whole column.
        maskedcvars.dc_source = (T) column.data;
        // dc_source_ofs=0;

        // for each post...
        for (int i = 0; i < column.posts; i++) {
            maskedcvars.dc_source_ofs = column.postofs[i];
            // calculate unclipped screen coordinates
            // for post
            topscreen = sprtopscreen + spryscale * column.postdeltas[i];
            bottomscreen = topscreen + spryscale * column.postlen[i];

            maskedcvars.dc_yl = (topscreen + FRACUNIT - 1) >> FRACBITS;
            maskedcvars.dc_yh = (bottomscreen - 1) >> FRACBITS;

            if (maskedcvars.dc_yh >= mfloorclip[p_mfloorclip
                    + maskedcvars.dc_x])
                maskedcvars.dc_yh =
                    mfloorclip[p_mfloorclip + maskedcvars.dc_x] - 1;
            if (maskedcvars.dc_yl <= mceilingclip[p_mceilingclip
                    + maskedcvars.dc_x])
                maskedcvars.dc_yl =
                    mceilingclip[p_mceilingclip + maskedcvars.dc_x] + 1;

            // killough 3/2/98, 3/27/98: Failsafe against overflow/crash:
            if (maskedcvars.dc_yl <= maskedcvars.dc_yh
                    && maskedcvars.dc_yh < maskedcvars.viewheight) {

                // Set pointer inside column to current post's data
                // Remember, it goes {postlen}{postdelta}{pad}[data]{pad}

                maskedcvars.dc_texturemid =
                    basetexturemid - (column.postdeltas[i] << FRACBITS);

                // Drawn by either R_DrawColumn or (SHADOW)
                // R_DrawFuzzColumn.
                // MAES: when something goes bad here, it means that the
                // following:
                //
                // fracstep = dc_iscale;
                // frac = dc_texturemid + (dc_yl - centery) * fracstep;
                //
                // results in a negative initial frac number.

                // Drawn by either R_DrawColumn
                //  or (SHADOW) R_DrawFuzzColumn.
                
                // FUN FACT: this was missing and fucked my shit up.
                maskedcvars.dc_texheight=0; // Killough
                
                completeColumn();
                 
            }
        }

        maskedcvars.dc_texturemid = basetexturemid;
    }
    
    /*
     * R_DrawMaskedColumn
     * Used for sprites and masked mid textures.
     * Masked means: partly transparent, i.e. stored
     *  in posts/runs of opaque pixels.
     *  
     *  NOTE: this version accepts raw bytes, in case you  know what you're doing.
     *  NOTE: this is a legacy function. Do not reactivate unless
     *  REALLY needed.
     *
     */
/*
    protected final  void DrawMaskedColumn (byte[] column)
    {
        int topscreen;
        int bottomscreen;
        int basetexturemid; // fixed_t
        int topdelta;
        int length;
        
        basetexturemid = dc_texturemid;
        // That's true for the whole column.
        dc_source = column;
        int pointer=0;
        
        // for each post...
        while((topdelta=0xFF&column[pointer])!=0xFF)
        {
        // calculate unclipped screen coordinates
        //  for post
        topscreen = sprtopscreen + spryscale*topdelta;
        length=0xff&column[pointer+1];
        bottomscreen = topscreen + spryscale*length;

        dc_yl = (topscreen+FRACUNIT-1)>>FRACBITS;
        dc_yh = (bottomscreen-1)>>FRACBITS;
            
        if (dc_yh >= mfloorclip[p_mfloorclip+dc_x])
            dc_yh = mfloorclip[p_mfloorclip+dc_x]-1;
        
        if (dc_yl <= mceilingclip[p_mceilingclip+dc_x])
            dc_yl = mceilingclip[p_mceilingclip+dc_x]+1;

        // killough 3/2/98, 3/27/98: Failsafe against overflow/crash:
        if (dc_yl <= dc_yh && dc_yh < viewheight)
        {
            // Set pointer inside column to current post's data
            // Rremember, it goes {postlen}{postdelta}{pad}[data]{pad} 
            dc_source_ofs = pointer+3;
            dc_texturemid = basetexturemid - (topdelta<<FRACBITS);

            // Drawn by either R_DrawColumn
            //  or (SHADOW) R_DrawFuzzColumn.
            dc_texheight=0; // Killough
                
            maskedcolfunc.invoke();
        }
        pointer+=length + 4;
        }
        
        dc_texturemid = basetexturemid;
    }
      */

    @Override
    public void setPspriteIscale(int i) {
        pspriteiscale = i;

    }

    @Override
    public void setPspriteScale(int i) {
        pspritescale = i;
    }

    @Override
    public void setDetail(int detailshift){
        switch (detailshift){
        case HIGH_DETAIL:
            colfuncs=colfuncshi;
            break;
        case LOW_DETAIL:
            colfuncs=colfuncslow;
            break;
        }
    }
    
}

package org.bleachhack.util.doom.rr;

import org.bleachhack.util.doom.data.Defines;
import static org.bleachhack.util.doom.data.Defines.ANGLETOSKYSHIFT;
import static org.bleachhack.util.doom.data.Defines.NF_SUBSECTOR;
import static org.bleachhack.util.doom.data.Defines.PU_CACHE;
import static org.bleachhack.util.doom.data.Defines.SIL_BOTH;
import static org.bleachhack.util.doom.data.Defines.SIL_BOTTOM;
import static org.bleachhack.util.doom.data.Defines.SIL_TOP;
import static org.bleachhack.util.doom.data.Limits.MAXHEIGHT;
import static org.bleachhack.util.doom.data.Limits.MAXSEGS;
import static org.bleachhack.util.doom.data.Limits.MAXWIDTH;
import org.bleachhack.util.doom.data.Tables;
import static org.bleachhack.util.doom.data.Tables.ANG180;
import static org.bleachhack.util.doom.data.Tables.ANG270;
import static org.bleachhack.util.doom.data.Tables.ANG90;
import static org.bleachhack.util.doom.data.Tables.ANGLETOFINESHIFT;
import static org.bleachhack.util.doom.data.Tables.BITS32;
import static org.bleachhack.util.doom.data.Tables.DBITS;
import static org.bleachhack.util.doom.data.Tables.FINEANGLES;
import static org.bleachhack.util.doom.data.Tables.QUARTERMARK;
import static org.bleachhack.util.doom.data.Tables.SlopeDiv;
import static org.bleachhack.util.doom.data.Tables.addAngles;
import static org.bleachhack.util.doom.data.Tables.finecosine;
import static org.bleachhack.util.doom.data.Tables.finesine;
import static org.bleachhack.util.doom.data.Tables.finetangent;
import static org.bleachhack.util.doom.data.Tables.tantoangle;
import org.bleachhack.util.doom.doom.CommandVariable;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.SourceCode.R_Draw;
import static org.bleachhack.util.doom.doom.SourceCode.R_Draw.R_FillBackScreen;
import org.bleachhack.util.doom.doom.player_t;
import org.bleachhack.util.doom.doom.thinker_t;
import org.bleachhack.util.doom.i.IDoomSystem;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.bleachhack.util.doom.m.BBox.BOXBOTTOM;
import static org.bleachhack.util.doom.m.BBox.BOXLEFT;
import static org.bleachhack.util.doom.m.BBox.BOXRIGHT;
import static org.bleachhack.util.doom.m.BBox.BOXTOP;
import org.bleachhack.util.doom.m.IDoomMenu;
import org.bleachhack.util.doom.m.MenuMisc;
import org.bleachhack.util.doom.m.Settings;
import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;
import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import static org.bleachhack.util.doom.m.fixed_t.FixedDiv;
import static org.bleachhack.util.doom.m.fixed_t.FixedMul;
import org.bleachhack.util.doom.mochadoom.Engine;
import static org.bleachhack.util.doom.p.ActiveStates.P_MobjThinker;
import org.bleachhack.util.doom.p.mobj_t;
import org.bleachhack.util.doom.rr.drawfuns.ColFuncs;
import org.bleachhack.util.doom.rr.drawfuns.ColVars;
import org.bleachhack.util.doom.rr.drawfuns.DoomColumnFunction;
import org.bleachhack.util.doom.rr.drawfuns.DoomSpanFunction;
import org.bleachhack.util.doom.rr.drawfuns.SpanVars;
import static org.bleachhack.util.doom.rr.line_t.*;
import org.bleachhack.util.doom.utils.C2JUtils;
import static org.bleachhack.util.doom.utils.GenericCopy.malloc;
import static org.bleachhack.util.doom.v.DoomGraphicSystem.V_NOSCALEOFFSET;
import static org.bleachhack.util.doom.v.DoomGraphicSystem.V_NOSCALEPATCH;
import static org.bleachhack.util.doom.v.DoomGraphicSystem.V_NOSCALESTART;
import org.bleachhack.util.doom.v.graphics.Palettes;
import org.bleachhack.util.doom.v.renderers.DoomScreen;
import static org.bleachhack.util.doom.v.renderers.DoomScreen.*;
import org.bleachhack.util.doom.v.tables.BlurryTable;
import org.bleachhack.util.doom.v.tables.LightsAndColors;
import org.bleachhack.util.doom.wad.IWadLoader;

/**
 * Most shared -essential- status information, methods and classes related to
 * the software rendering subsystem are found here, shared between the various
 * implementations of the Doom's renderer. Not the cleanest or more OO way
 * possible, but still a good way to avoid duplicating common code. Some stuff
 * like Texture, Flat and Sprite management are also found -or at least
 * implemented temporarily- here, until a cleaner split can be made. This is a
 * kind of "Jack of all trades" class, but hopefully not for long.
 *
 * @author velktron
 */
public abstract class RendererState<T, V> implements SceneRenderer<T, V>, ILimitResettable {

    protected static final boolean DEBUG = false;
    protected static final boolean DEBUG2 = false;

    // ///////////////////// STATUS ////////////////////////
    protected DoomMain<T, V> DOOM;
    protected ISegDrawer MySegs;
    protected IDoomMenu Menu;
    protected BSP MyBSP;
    protected PlaneDrawer<T, V> MyPlanes;
    protected IMaskedDrawer<T, V> MyThings;
    public IVisSpriteManagement<V> VIS;
    protected TextureManager<T> TexMan;
    public ViewVars view;
    public LightsAndColors<V> colormaps;
    public SegVars seg_vars;
    public Visplanes vp_vars;

    // Rendering subsystems that are detailshift-aware
    protected List<IDetailAware> detailaware;

    // The only reason to query scaledviewwidth from outside the renderer, is
    // this.
    @Override
    public boolean isFullHeight() {
        return (view.height == DOOM.vs.getScreenHeight());
    }

    public boolean isFullWidth() {
        return (view.scaledwidth == DOOM.vs.getScreenWidth());
    }

    @Override
    public boolean isFullScreen() {
        return isFullWidth() && isFullHeight();
    }

    /**
     * Increment every time a check is made For some reason, this needs to be
     * visible even by enemies thinking :-S
     */
    protected int validcount = 1;

    /**
     * Who can set this? A: The Menu.
     */
    protected boolean setsizeneeded;
    protected int setblocks;
    protected int setdetail;

    // private BSPVars bspvars;
    /**
     * R_SetViewSize Do not really change anything here, because it might be in
     * the middle of a refresh. The change will take effect next refresh.
     *
     * @param blocks
     * 11 is full screen, 9 default.
     * @param detail
     * 0= high, 1 =low.
     */
    @Override
    public void SetViewSize(int blocks, int detail) {
        // System.out.println("SetViewSize");
        setsizeneeded = true;
        setblocks = blocks;
        setdetail = detail;

        detailaware.forEach((d) -> {
            d.setDetail(setdetail);
        });
    }

    /**
     * R_SetupFrame
     */
    public void SetupFrame(player_t player) {
        view.player = player;
        view.x = player.mo.x;
        view.y = player.mo.y;
        // viewangle = addAngles(player.mo.angle , viewangleoffset);
        view.angle = player.mo.angle & BITS32;
        // With 32 colormaps, a bump of 1 or 2 is normal.
        // With more than 32, it should be obviously higher.

        int bumplight = Math.max(colormaps.lightBits() - 5, 0);
        // Be a bit more generous, otherwise the effect is not
        // as evident with truecolor maps.
        bumplight += (bumplight > 0) ? 1 : 0;

        colormaps.extralight = player.extralight << bumplight;

        view.z = player.viewz;
        view.lookdir = player.lookdir;
        int tempCentery;

        // MAES: hacks based on Heretic. Weapon movement needs to be compensated
        if (setblocks == 11) {
            tempCentery = (view.height / 2) + (int) (view.lookdir * DOOM.vs.getScreenMul() * setblocks) / 11;
        } else {
            tempCentery = (view.height / 2) + (int) (view.lookdir * DOOM.vs.getScreenMul() * setblocks) / 10;
        }

        if (view.centery != tempCentery) {
            view.centery = tempCentery;
            view.centeryfrac = view.centery << FRACBITS;
            int yslope[] = vp_vars.yslope;
            for (int i = 0; i < view.height; i++) {
                yslope[i] = FixedDiv(
                    (view.width << view.detailshift) / 2 * FRACUNIT,
                    Math.abs(((i - view.centery) << FRACBITS) + FRACUNIT / 2)
                );
            }

            skydcvars.centery = maskedcvars.centery = dcvars.centery = view.centery;
        }

        view.sin = Tables.finesine(view.angle);
        view.cos = Tables.finecosine(view.angle);

        sscount = 0;

        if (player.fixedcolormap != Palettes.COLORMAP_FIXED) {
            colormaps.fixedcolormap = colormaps.getFixedColormap(player);
            // Offset by fixedcolomap
            // pfixedcolormap =player.fixedcolormap*256;

            colormaps.walllights = colormaps.scalelightfixed;

            for (int i = 0; i < colormaps.maxLightScale(); i++) {
                colormaps.scalelightfixed[i] = colormaps.fixedcolormap;
            }
        } else {
            colormaps.fixedcolormap = null;
        }

        framecount++;
        validcount++;
    }

    /**
     * R_SetupFrame for a particular actor.
     */
    public void SetupFrame(mobj_t actor) {

        // viewplayer = player;
        view.x = actor.x;
        view.y = actor.y;
        // viewangle = addAngles(player.mo.angle , viewangleoffset);
        view.angle = actor.angle & BITS32;
        // extralight = actor.extralight;

        view.z = actor.z + actor.height;

        view.sin = finesine(view.angle);
        view.cos = finecosine(view.angle);

        sscount = 0;

        framecount++;
        validcount++;
    }

    public RendererState(DoomMain<T, V> DOOM) {
        this.DOOM = DOOM;

        // These don't change between implementations, yet.
        this.MyBSP = new BSP();

        this.view = new ViewVars(DOOM.vs);
        this.seg_vars = new SegVars();
        this.dcvars = new ColVars<>();
        this.dsvars = new SpanVars<>();
        this.maskedcvars = new ColVars<>();
        this.skydcvars = new ColVars<>();
        this.colfunclow = new ColFuncs<>();
        this.colfunchi = new ColFuncs<>();

        this.detailaware = new ArrayList<>();
        this.colormaps = new LightsAndColors<>(DOOM);
        // It's better to construct this here
        @SuppressWarnings("unchecked")
        final TextureManager<T> tm = (TextureManager<T>) new SimpleTextureManager(DOOM);
        this.TexMan = tm;

        // Visplane variables
        this.vp_vars = new Visplanes(DOOM.vs, view, TexMan);

        // Set rendering functions only after screen sizes
        // and stuff have been set.
        this.MyPlanes = new Planes(DOOM, this);
        this.VIS = new VisSprites<>(this);
        this.MyThings = new SimpleThings<>(DOOM.vs, this);
    }

    // ////////////////////////////// THINGS ////////////////////////////////
    protected final class BSP extends BSPVars {

        /**
         * newend is one past the last valid seg (cliprange_t)
         */
        int newend;

        cliprange_t[] solidsegs;

        BSP() {
            solidsegs = malloc(cliprange_t::new, cliprange_t[]::new, MAXSEGS + 1);
        }

        /**
         * R_ClipSolidWallSegment Does handle solid walls, single sided LineDefs
         * (middle texture) that entirely block the view VERTICALLY. Handles
         * "clipranges" for a solid wall, aka where it blocks the view.
         *
         * @param first
         * starting y coord?
         * @param last
         * ending y coord?
         */
        private void ClipSolidWallSegment(int first, int last) {

            int next;
            int start;
            // int maxlast=Integer.MIN_VALUE;

            start = 0; // within solidsegs

            // Find the first cliprange that touches the range.
            // Actually, the first one not completely hiding it (its last must
            // be lower than first.
            while (solidsegs[start].last < first - 1) {
                start++;
            }

            // If the post begins above the lastly found cliprange...
            if (first < solidsegs[start].first) {
                // ..and ends above it, too (no overlapping)
                if (last < solidsegs[start].first - 1) {
                    // ... then the post is entirely visible (above start),
                    // so insert a new clippost. Calling this function
                    // tells the renderer that there is an obstruction.
                    MySegs.StoreWallRange(first, last);

                    // Newend should have a value of 2 if we are at the
                    // beginning of a new frame.
                    next = newend;
                    newend++;

                    if (next >= solidsegs.length) {
                        ResizeSolidSegs();
                    }
                    while (next != start) {
                        // *next=*(next-1);
                        /*
                         * MAES: I think this is supposed to copy the structs
                         * solidsegs[next] = solidsegs[next-1].clone(); OK, so
                         * basically the last solidseg copies its previous, and
                         * so on until we reach the start. This means that at
                         * some point, the value of the start solidseg is
                         * duplicated.
                         */

                        solidsegs[next].copy(solidsegs[next - 1]);

                        next--;
                    }

                    // At this point, next points at start.
                    // Therefore, start
                    solidsegs[next].first = first;
                    solidsegs[next].last = last;
                    return;
                }

                // There is a fragment above *start. This can occur if it a
                // post does start before another, but its lower edge overlaps
                // (partial, upper occlusion)
                MySegs.StoreWallRange(first, solidsegs[start].first - 1);
                // Now adjust the clip size.
                solidsegs[start].first = first;
            }

            // We can reach this only if a post starts AFTER another
            // Bottom contained in start? Obviously it won't be visible.
            if (last <= solidsegs[start].last) {
                return;
            }

            next = start;
            while (last >= solidsegs[(next + 1)].first - 1) {
                // There is a fragment between two posts.
                MySegs.StoreWallRange(solidsegs[next].last + 1,
                    solidsegs[next + 1].first - 1);
                next++;

                if (last <= solidsegs[next].last) {
                    // Bottom is contained in next.
                    // Adjust the clip size.
                    solidsegs[start].last = solidsegs[next].last;
                    // goto crunch;

                    { // crunch code
                        if (next == start) {
                            // Post just extended past the bottom of one post.
                            return;
                        }

                        while (next++ != newend) {
                            // Remove a post.
                            // MAES: this is a struct copy.
                            if (next >= solidsegs.length) {
                                ResizeSolidSegs();
                            }
                            solidsegs[++start].copy(solidsegs[next]);
                        }

                        newend = start + 1;
                        return;
                    }
                }
            }

            // There is a fragment after *next.
            MySegs.StoreWallRange(solidsegs[next].last + 1, last);
            // Adjust the clip size.
            solidsegs[start].last = last;

            // Remove start+1 to next from the clip list,
            // because start now covers their area.
            { // crunch code
                if (next == start) {
                    // Post just extended past the bottom of one post.
                    return;
                }

                while (next++ != newend) {
                    // Remove a post.
                    // MAES: this is a struct copy.
                    // MAES: this can overflow, breaking e.g. MAP30 of Final
                    // Doom.
                    if (next >= solidsegs.length) {
                        ResizeSolidSegs();
                    }
                    solidsegs[++start].copy(solidsegs[next]);
                }

                newend = start + 1;
            }
        }

        void ResizeSolidSegs() {
            solidsegs = C2JUtils.resize(solidsegs, solidsegs.length * 2);
        }

        //
        // R_ClipPassWallSegment
        // Clips the given range of columns,
        // but does not includes it in the clip list.
        // Does handle windows,
        // e.g. LineDefs with upper and lower texture.
        //
        private void ClipPassWallSegment(int first, int last) {

            // Find the first range that touches the range
            // (adjacent pixels are touching).
            int start = 0;

            while (solidsegs[start].last < first - 1) {
                start++;
            }

            if (first < solidsegs[start].first) {
                if (last < solidsegs[start].first - 1) {
                    // Post is entirely visible (above start).
                    MySegs.StoreWallRange(first, last);
                    return;
                }

                // There is a fragment above *start.
                MySegs.StoreWallRange(first, solidsegs[start].first - 1);
            }

            // Bottom contained in start?
            if (last <= solidsegs[start].last) {
                return;
            }

            // MAES: Java absolutely can't do without a sanity check here.
            // if (startptr>=MAXSEGS-2) return;
            while (last >= solidsegs[start + 1].first - 1) {
                // There is a fragment between two posts.
                MySegs.StoreWallRange(solidsegs[start].last + 1,
                    solidsegs[start + 1].first - 1);
                start++;
                // if (startptr>=MAXSEGS-2) return;
                // start=solidsegs[startptr];

                if (last <= solidsegs[start].last) {
                    return;
                }
            }

            // There is a fragment after *next.
            MySegs.StoreWallRange(solidsegs[start].last + 1, last);
        }

        /**
         * R_ClearClipSegs Clears the clipping segs list. The list is actually
         * fixed size for efficiency reasons, so it just tells Doom to use the
         * first two solidsegs, which are "neutered". It's interesting to note
         * how the solidsegs begin and end just "outside" the visible borders of
         * the screen.
         */
        public void ClearClipSegs() {
            solidsegs[0].first = -0x7fffffff;
            solidsegs[0].last = -1;
            solidsegs[1].first = view.width;
            solidsegs[1].last = 0x7fffffff;
            newend = 2; // point so solidsegs[2];
        }

        /**
         * R_AddLine Called after a SubSector BSP trasversal ends up in a
         * "final" subsector. Clips the given segment and adds any visible
         * pieces to the line list. It also determines what kind of boundary
         * (line) visplane clipping should be performed. E.g. window, final
         * 1-sided line, closed door etc.) CAREFUL: was the source of much
         * frustration with visplanes...
         */
        private void AddLine(seg_t line) {
            if (DEBUG) {
                System.out.println("Entered AddLine for " + line);
            }
            int x1;
            int x2;
            long angle1;
            long angle2;
            long span;
            long tspan;

            curline = line;

            // OPTIMIZE: quickly reject orthogonal back sides.
            angle1 = view.PointToAngle(line.v1x, line.v1y);
            angle2 = view.PointToAngle(line.v2x, line.v2y);

            // Clip to view edges.
            // OPTIMIZE: make constant out of 2*clipangle (FIELDOFVIEW).
            span = addAngles(angle1, -angle2);

            // Back side? I.e. backface culling?
            if (span >= ANG180) {
                return;
            }

            // Global angle needed by segcalc.
            MySegs.setGlobalAngle(angle1);
            angle1 -= view.angle;
            angle2 -= view.angle;

            angle1 &= BITS32;
            angle2 &= BITS32;

            tspan = addAngles(angle1, clipangle);

            if (tspan > CLIPANGLE2) {
                tspan -= CLIPANGLE2;
                tspan &= BITS32;

                // Totally off the left edge?
                if (tspan >= span) {
                    return;
                }

                angle1 = clipangle;
            }
            tspan = addAngles(clipangle, -angle2);

            if (tspan > CLIPANGLE2) {
                tspan -= CLIPANGLE2;
                tspan &= BITS32;

                // Totally off the left edge?
                if (tspan >= span) {
                    return;
                }
                angle2 = -clipangle;
                angle2 &= BITS32;
            }

            // The seg is in the view range,
            // but not necessarily visible.
            angle1 = ((angle1 + ANG90) & BITS32) >>> ANGLETOFINESHIFT;
            angle2 = ((angle2 + ANG90) & BITS32) >>> ANGLETOFINESHIFT;
            x1 = viewangletox[(int) angle1];
            x2 = viewangletox[(int) angle2];

            // Does not cross a pixel?
            if (x1 == x2) {
                return;
            }

            backsector = line.backsector;

            // Single sided line?
            if (backsector == null) {
                if (DEBUG) {
                    System.out .println("Entering ClipSolidWallSegment SS with params " + x1 + " " + (x2 - 1));
                }
                ClipSolidWallSegment(x1, x2 - 1); // to clipsolid
                if (DEBUG) {
                    System.out.println("Exiting ClipSolidWallSegment");
                }
                return;
            }

            // Closed door.
            if (backsector.ceilingheight <= frontsector.floorheight
                || backsector.floorheight >= frontsector.ceilingheight) {
                if (DEBUG) {
                    System.out.println("Entering ClipSolidWallSegment Closed door with params " + x1 + " " + (x2 - 1));
                }
                ClipSolidWallSegment(x1, x2 - 1);
                // to clipsolid
                return;
            }

            // Window. This includes same-level floors with different textures
            if (backsector.ceilingheight != frontsector.ceilingheight
                || backsector.floorheight != frontsector.floorheight) {
                if (DEBUG) {
                    System.out.println("Entering ClipSolidWallSegment window with params " + x1 + " " + (x2 - 1));
                }
                ClipPassWallSegment(x1, x2 - 1); // to clippass
                return;
            }

            // Reject empty lines used for triggers
            // and special events.
            // Identical floor and ceiling on both sides,
            // identical light levels on both sides,
            // and no middle texture.
            if (backsector.ceilingpic == frontsector.ceilingpic
                && backsector.floorpic == frontsector.floorpic
                && backsector.lightlevel == frontsector.lightlevel
                && curline.sidedef.midtexture == 0) {
                return;
            }

            // If nothing of the previous holds, then we are
            // treating the case of same-level, differently
            // textured floors. ACHTUNG, this caused the "bleeding floor"
            // bug, which is now fixed.
            // Fucking GOTOs....
            ClipPassWallSegment(x1, x2 - 1); // to clippass
            if (DEBUG) {
                System.out.println("Exiting AddLine for " + line);
            }
        }

        //
        // R_CheckBBox
        // Checks BSP node/subtree bounding box.
        // Returns true
        // if some part of the bbox might be visible.
        //
        private final int[][] checkcoord = {
            {3, 0, 2, 1},
            {3, 0, 2, 0},
            {3, 1, 2, 0},
            {0},
            {2, 0, 2, 1},
            {0, 0, 0, 0},
            {3, 1, 3, 0},
            {0},
            {2, 0, 3, 1},
            {2, 1, 3, 1},
            {2, 1, 3, 0}
        };

        /**
         * @param bspcoord
         * (fixed_t* as bbox)
         * @return
         */
        public boolean CheckBBox(int[] bspcoord) {
            int boxx;
            int boxy;
            int boxpos;

            // fixed_t
            int x1;
            int y1;
            int x2;
            int y2;

            // angle_t
            long angle1;
            long angle2;
            long span;
            long tspan;

            cliprange_t start;

            int sx1;
            int sx2;

            // Find the corners of the box
            // that define the edges from current viewpoint.
            if (view.x <= bspcoord[BOXLEFT]) {
                boxx = 0;
            } else if (view.x < bspcoord[BOXRIGHT]) {
                boxx = 1;
            } else {
                boxx = 2;
            }

            if (view.y >= bspcoord[BOXTOP]) {
                boxy = 0;
            } else if (view.y > bspcoord[BOXBOTTOM]) {
                boxy = 1;
            } else {
                boxy = 2;
            }

            boxpos = (boxy << 2) + boxx;
            if (boxpos == 5) {
                return true;
            }

            x1 = bspcoord[checkcoord[boxpos][0]];
            y1 = bspcoord[checkcoord[boxpos][1]];
            x2 = bspcoord[checkcoord[boxpos][2]];
            y2 = bspcoord[checkcoord[boxpos][3]];

            // check clip list for an open space
            angle1 = view.PointToAngle(x1, y1) - view.angle;
            angle2 = view.PointToAngle(x2, y2) - view.angle;

            angle1 &= BITS32;
            angle2 &= BITS32;

            span = angle1 - angle2;

            span &= BITS32;

            // Sitting on a line?
            if (span >= ANG180) {
                return true;
            }

            tspan = angle1 + clipangle;
            tspan &= BITS32;

            if (tspan > CLIPANGLE2) {
                tspan -= CLIPANGLE2;
                tspan &= BITS32;
                // Totally off the left edge?
                if (tspan >= span) {
                    return false;
                }

                angle1 = clipangle;
            }
            tspan = (clipangle - angle2) & BITS32;
            if (tspan > CLIPANGLE2) {
                tspan -= CLIPANGLE2;
                tspan &= BITS32;

                // Totally off the left edge?
                if (tspan >= span) {
                    return false;
                }

                angle2 = -clipangle;
                angle2 &= BITS32;
            }

            // Find the first clippost
            // that touches the source post
            // (adjacent pixels are touching).
            angle1 = ((angle1 + ANG90) & BITS32) >>> ANGLETOFINESHIFT;
            angle2 = ((angle2 + ANG90) & BITS32) >>> ANGLETOFINESHIFT;
            sx1 = viewangletox[(int) angle1];
            sx2 = viewangletox[(int) angle2];

            // Does not cross a pixel.
            if (sx1 == sx2) {
                return false;
            }
            sx2--;

            int pstart = 0;
            start = solidsegs[pstart];
            // FIXME: possible solidseg overflow here overflows
            while (start.last < sx2 && pstart < MAXSEGS) {
                start = solidsegs[pstart++];
            }

            return !(sx1 >= start.first && sx2 <= start.last);
        }

        /**
         * R_Subsector Determine floor/ceiling planes. Add sprites of things in
         * sector. Draw one or more line segments. It also alters the visplane
         * list!
         *
         * @param num
         * Subsector from subsector_t list in Lever Loader.
         */
        private void Subsector(int num) {
            if (DEBUG) {
                System.out.println("\t\tSubSector " + num + " to render");
            }
            int count;
            int line; // pointer into a list of segs instead of seg_t
            subsector_t sub;

            if (RANGECHECK) {
                if (num >= DOOM.levelLoader.numsubsectors) {
                    DOOM.doomSystem.Error("R_Subsector: ss %d with numss = %d", num,
                        DOOM.levelLoader.numsubsectors);
                }
            }

            sscount++;
            sub = DOOM.levelLoader.subsectors[num];

            frontsector = sub.sector;
            if (DEBUG) {
                System.out.println("Frontsector to render :" + frontsector);
            }
            count = sub.numlines;
            // line = LL.segs[sub.firstline];
            line = sub.firstline;

            if (DEBUG) {
                System.out.println("Trying to find an existing FLOOR visplane...");
            }
            if (frontsector.floorheight < view.z) {
                vp_vars.floorplane
                    = vp_vars.FindPlane(frontsector.floorheight,
                        frontsector.floorpic, frontsector.lightlevel);
            } else {
                // FIXME: unclear what would happen with a null visplane used
                // It's never checked explicitly for either condition, just
                // called straight.
                vp_vars.floorplane = -1; // in lieu of NULL
            }

            // System.out.println("Trying to find an existing CEILING visplane...");
            if (frontsector.ceilingheight > view.z
                || frontsector.ceilingpic == TexMan.getSkyFlatNum()) {
                vp_vars.ceilingplane
                    = vp_vars.FindPlane(frontsector.ceilingheight,
                        frontsector.ceilingpic, frontsector.lightlevel);
            } else {
                vp_vars.ceilingplane = -1; // In lieu of NULL. Will bomb if
                // actually
                // used.
            }

            VIS.AddSprites(frontsector);

            if (DEBUG) {
                System.out.println("Enter Addline for SubSector " + num + " count " + count);
            }
            while (count-- > 0) {
                AddLine(DOOM.levelLoader.segs[line]);
                line++;
            }
            if (DEBUG) {
                System.out.println("Exit Addline for SubSector " + num);
            }
        }

        /**
         * RenderBSPNode Renders all subsectors below a given node, traversing
         * subtree recursively. Just call with BSP root.
         */
        public void RenderBSPNode(int bspnum) {
            if (DEBUG) {
                System.out.println("Processing BSP Node " + bspnum);
            }

            node_t bsp;
            int side;

            // Found a subsector? Then further decisions are taken, in, well,
            // SubSector.
            if (C2JUtils.flags(bspnum, NF_SUBSECTOR)) {
                if (DEBUG) {
                    System.out.println("Subsector found.");
                }
                if (bspnum == -1) {
                    Subsector(0);
                } else {
                    Subsector(bspnum & (~NF_SUBSECTOR));
                }
                return;
            }

            bsp = DOOM.levelLoader.nodes[bspnum];

            // Decide which side the view point is on.
            side = bsp.PointOnSide(view.x, view.y);
            if (DEBUG) {
                System.out.println("\tView side: " + side);
            }

            // Recursively divide front space.
            if (DEBUG) {
                System.out.println("\tEnter Front space of " + bspnum);
            }
            RenderBSPNode(bsp.children[side]);
            if (DEBUG) {
                System.out.println("\tReturn Front space of " + bspnum);
            }

            // Possibly divide back space.
            if (CheckBBox(bsp.bbox[side ^ 1].bbox)) {
                if (DEBUG) {
                    System.out.println("\tEnter Back space of " + bspnum);
                }
                RenderBSPNode(bsp.children[side ^ 1]);
                if (DEBUG) {
                    System.out.println("\tReturn Back space of " + bspnum);
                }
            }
        }

    }

    protected abstract class SegDrawer implements ISegDrawer {

        protected static final int HEIGHTBITS = 12;
        protected static final int HEIGHTUNIT = (1 << HEIGHTBITS);
        protected final Visplanes vp_vars;
        protected final SegVars seg_vars;

        // Fast blanking buffers.
        protected short[] BLANKFLOORCLIP;
        protected short[] BLANKCEILINGCLIP;

        @Override
        public short[] getBLANKFLOORCLIP() {
            return BLANKFLOORCLIP;
        }

        @Override
        public short[] getBLANKCEILINGCLIP() {
            return BLANKCEILINGCLIP;
        }

        /**
         * fixed_t
         */
        protected int pixhigh, pixlow, pixhighstep, pixlowstep, topfrac, topstep, bottomfrac, bottomstep;
        protected int worldtop, worldbottom, worldhigh, worldlow;

        /**
         * True if any of the segs textures might be visible.
         */
        protected boolean segtextured;

        /**
         * Clip values are the solid pixel bounding the range. floorclip starts
         * out vs.getScreenHeight() ceilingclip starts out -1
         */
        protected short[] floorclip, ceilingclip;

        @Override
        public final short[] getFloorClip() {
            return floorclip;
        }

        @Override
        public short[] getCeilingClip() {
            return ceilingclip;
        }

        /**
         * False if the back side is the same plane.
         */
        protected boolean markfloor, markceiling;

        protected boolean maskedtexture;

        protected int toptexture;

        protected int bottomtexture;

        protected int midtexture;

        /**
         * angle_t, used after adding ANG90 in StoreWallRange
         */
        protected long rw_normalangle;

        /**
         * angle to line origin
         */
        protected long rw_angle1;

        //
        // regular wall
        //
        protected int rw_x;

        protected int rw_stopx;

        protected long rw_centerangle; // angle_t

        /**
         * fixed_t
         */
        protected int rw_offset, rw_distance, rw_scale, rw_scalestep,
            rw_midtexturemid, rw_toptexturemid, rw_bottomtexturemid;

        @Override
        public void resetLimits() {
            drawseg_t[] tmp = new drawseg_t[seg_vars.MAXDRAWSEGS];
            System.arraycopy(seg_vars.drawsegs, 0, tmp, 0, seg_vars.MAXDRAWSEGS);

            // Now, that was quite a haircut!.
            seg_vars.drawsegs = tmp;

            // System.out.println("Drawseg buffer cut back to original limit of "+MAXDRAWSEGS);
        }

        @Override
        public void sync() {
            // Nothing required if serial.
        }

        /**
         * R_StoreWallRange A wall segment will be drawn between start and stop
         * pixels (inclusive). This is the only place where
         * markceiling/markfloor can be set. Can only be called from
         * ClipSolidWallSegment and ClipPassWallSegment.
         *
         * @throws IOException
         */
        @Override
        public void StoreWallRange(int start, int stop) {

            if (DEBUG2) {
                System.out.println("\t\t\t\tStorewallrange called between " + start + " and " + stop);
            }

            int hyp; // fixed_t
            int sineval; // fixed_t
            int distangle;
            long offsetangle; // angle_t
            int vtop; // fixed_t
            int lightnum;
            drawseg_t seg;

            // don't overflow and crash
            if (seg_vars.ds_p == seg_vars.drawsegs.length) {
                seg_vars.ResizeDrawsegs();
            }

            if (RANGECHECK) {
                if (start >= view.width || start > stop) {
                    DOOM.doomSystem.Error("Bad R_RenderWallRange: %d to %d", start, stop);
                }
            }

            seg = seg_vars.drawsegs[seg_vars.ds_p];

            MyBSP.sidedef = MyBSP.curline.sidedef;
            MyBSP.linedef = MyBSP.curline.linedef;

            // mark the segment as visible for auto map
            MyBSP.linedef.flags |= ML_MAPPED;

            // calculate rw_distance for scale calculation
            rw_normalangle = addAngles(MyBSP.curline.angle, ANG90);

            /*
             * MAES: ok, this is a tricky spot. angle_t's are supposed to be
             * always positive 32-bit unsigned integers, so a subtraction should
             * be always positive by definition, right? WRONG: this fucking spot
             * caused "blind spots" at certain angles because ONLY HERE angles
             * are supposed to be treated as SIGNED and result in differences
             * <180 degrees -_- The only way to coerce this behavior is to cast
             * both as signed ints.
             */
            offsetangle = Math.abs((int) rw_normalangle - (int) rw_angle1);

            if (offsetangle > ANG90) {
                offsetangle = ANG90;
            }

            // It should fit even in a signed int, by now.
            distangle = (int) (ANG90 - offsetangle);
            hyp = PointToDist(MyBSP.curline.v1x, MyBSP.curline.v1y);
            sineval = finesine(distangle);
            rw_distance = FixedMul(hyp, sineval);

            seg.x1 = rw_x = start;
            seg.x2 = stop;
            seg.curline = MyBSP.curline;
            /*
             * This is the only place it's ever explicitly assigned. Therefore
             * it always starts at stop+1.
             */
            rw_stopx = stop + 1;

            // calculate scale at both ends and step
            // this is the ONLY place where rw_scale is set.
            seg.scale1
                = rw_scale
                = ScaleFromGlobalAngle((view.angle + view.xtoviewangle[start]));

            if (stop > start) {
                seg.scale2
                    = ScaleFromGlobalAngle(view.angle + view.xtoviewangle[stop]);
                seg.scalestep
                    = rw_scalestep = (seg.scale2 - rw_scale) / (stop - start);
            } else {
                // UNUSED: try to fix the stretched line bug
                /*
                 * #if 0 if (rw_distance < FRACUNIT/2) { fixed_t trx,try;
                 * fixed_t gxt,gyt; trx = curline.v1.x - viewx; try =
                 * curline.v1.y - viewy; gxt = FixedMul(trx,viewcos); gyt =
                 * -FixedMul(try,viewsin); seg.scale1 = FixedDiv(projection,
                 * gxt-gyt)<<detailshift; } #endif
                 */
                seg.scale2 = seg.scale1;
            }

            // calculate texture boundaries
            // and decide if floor / ceiling marks are needed
            worldtop = MyBSP.frontsector.ceilingheight - view.z;
            worldbottom = MyBSP.frontsector.floorheight - view.z;

            midtexture = toptexture = bottomtexture = 0;
            maskedtexture = false;
            seg.setMaskedTextureCol(null, 0);
            // seg.maskedtexturecol = null;

            if (MyBSP.backsector == null) {
                // single sided line
                midtexture
                    = TexMan.getTextureTranslation(MyBSP.sidedef.midtexture);
                // a single sided line is terminal, so it must mark ends
                markfloor = markceiling = true;
                if ((MyBSP.linedef.flags & ML_DONTPEGBOTTOM) != 0) {
                    vtop
                        = MyBSP.frontsector.floorheight
                        + TexMan.getTextureheight(MyBSP.sidedef.midtexture);
                    // bottom of texture at bottom
                    rw_midtexturemid = vtop - view.z;
                } else {
                    // top of texture at top
                    rw_midtexturemid = worldtop;
                }
                rw_midtexturemid += MyBSP.sidedef.rowoffset;

                seg.silhouette = SIL_BOTH;
                seg.setSprTopClip(view.screenheightarray, 0);
                seg.setSprBottomClip(view.negonearray, 0);
                seg.bsilheight = Integer.MAX_VALUE;
                seg.tsilheight = Integer.MIN_VALUE;
            } else {
                // two sided line
                seg.setSprTopClip(null, 0);
                seg.setSprBottomClip(null, 0);
                seg.silhouette = 0;

                if (MyBSP.frontsector.floorheight > MyBSP.backsector.floorheight) {
                    seg.silhouette = SIL_BOTTOM;
                    seg.bsilheight = MyBSP.frontsector.floorheight;
                } else if (MyBSP.backsector.floorheight > view.z) {
                    seg.silhouette = SIL_BOTTOM;
                    seg.bsilheight = Integer.MAX_VALUE;
                    // seg.sprbottomclip = negonearray;
                }

                if (MyBSP.frontsector.ceilingheight < MyBSP.backsector.ceilingheight) {
                    seg.silhouette |= SIL_TOP;
                    seg.tsilheight = MyBSP.frontsector.ceilingheight;
                } else if (MyBSP.backsector.ceilingheight < view.z) {
                    seg.silhouette |= SIL_TOP;
                    seg.tsilheight = Integer.MIN_VALUE;
                    // seg.sprtopclip = screenheightarray;
                }

                if (MyBSP.backsector.ceilingheight <= MyBSP.frontsector.floorheight) {
                    seg.setSprBottomClip(view.negonearray, 0);
                    seg.bsilheight = Integer.MAX_VALUE;
                    seg.silhouette |= SIL_BOTTOM;
                }

                if (MyBSP.backsector.floorheight >= MyBSP.frontsector.ceilingheight) {
                    seg.setSprTopClip(view.screenheightarray, 0);
                    seg.tsilheight = Integer.MIN_VALUE;
                    seg.silhouette |= SIL_TOP;
                }

                worldhigh = MyBSP.backsector.ceilingheight - view.z;
                worldlow = MyBSP.backsector.floorheight - view.z;

                // hack to allow height changes in outdoor areas
                if (MyBSP.frontsector.ceilingpic == TexMan.getSkyFlatNum()
                    && MyBSP.backsector.ceilingpic == TexMan
                        .getSkyFlatNum()) {
                    worldtop = worldhigh;
                }

                markfloor = worldlow != worldbottom
                    || MyBSP.backsector.floorpic != MyBSP.frontsector.floorpic
                    || MyBSP.backsector.lightlevel != MyBSP.frontsector.lightlevel; // same plane on both sides
                markceiling = worldhigh != worldtop
                    || MyBSP.backsector.ceilingpic != MyBSP.frontsector.ceilingpic
                    || MyBSP.backsector.lightlevel != MyBSP.frontsector.lightlevel; // same plane on both sides

                if (MyBSP.backsector.ceilingheight <= MyBSP.frontsector.floorheight
                    || MyBSP.backsector.floorheight >= MyBSP.frontsector.ceilingheight) {
                    // closed door
                    markceiling = markfloor = true;
                }

                if (worldhigh < worldtop) {
                    // top texture
                    toptexture
                        = TexMan.getTextureTranslation(MyBSP.sidedef.toptexture);
                    if ((MyBSP.linedef.flags & ML_DONTPEGTOP) != 0) {
                        // top of texture at top
                        rw_toptexturemid = worldtop;
                    } else {
                        vtop
                            = MyBSP.backsector.ceilingheight
                            + TexMan.getTextureheight(MyBSP.sidedef.toptexture);

                        // bottom of texture
                        rw_toptexturemid = vtop - view.z;
                    }
                }
                if (worldlow > worldbottom) {
                    // bottom texture
                    bottomtexture
                        = TexMan.getTextureTranslation(MyBSP.sidedef.bottomtexture);

                    if ((MyBSP.linedef.flags & ML_DONTPEGBOTTOM) != 0) {
                        // bottom of texture at bottom
                        // top of texture at top
                        rw_bottomtexturemid = worldtop;
                    } else {
                        // top of texture at top
                        rw_bottomtexturemid = worldlow;
                    }
                }
                rw_toptexturemid += MyBSP.sidedef.rowoffset;
                rw_bottomtexturemid += MyBSP.sidedef.rowoffset;

                // allocate space for masked texture tables
                if (MyBSP.sidedef.midtexture != 0) {
                    // masked midtexture
                    maskedtexture = true;
                    seg_vars.maskedtexturecol = vp_vars.openings;
                    seg_vars.pmaskedtexturecol = vp_vars.lastopening - rw_x;
                    seg.setMaskedTextureCol(seg_vars.maskedtexturecol,
                        seg_vars.pmaskedtexturecol);
                    vp_vars.lastopening += rw_stopx - rw_x;
                }
            }

            // calculate rw_offset (only needed for textured lines)
            segtextured
                = (((midtexture | toptexture | bottomtexture) != 0) | maskedtexture);

            if (segtextured) {
                offsetangle = addAngles(rw_normalangle, -rw_angle1);

                // Another "tricky spot": negative of an unsigned number?
                if (offsetangle > ANG180) {
                    offsetangle = (-(int) offsetangle) & BITS32;
                }

                if (offsetangle > ANG90) {
                    offsetangle = ANG90;
                }

                sineval = finesine(offsetangle);
                rw_offset = FixedMul(hyp, sineval);

                // Another bug: we CAN'T assume that the result won't wrap
                // around.
                // If that assumption is made, then texture alignment issues
                // appear
                if (((rw_normalangle - rw_angle1) & BITS32) < ANG180) {
                    rw_offset = -rw_offset;
                }

                rw_offset += MyBSP.sidedef.textureoffset + MyBSP.curline.offset;
                // This is OK, however: we can add as much shit as we want,
                // as long as we trim it to the 32 LSB. Proof as to why
                // this is always true is left as an exercise to the reader.
                rw_centerangle = (ANG90 + view.angle - rw_normalangle) & BITS32;

                // calculate light table
                // use different light tables
                // for horizontal / vertical / diagonal
                // OPTIMIZE: get rid of LIGHTSEGSHIFT globally
                if (colormaps.fixedcolormap == null) {
                    lightnum
                        = (MyBSP.frontsector.lightlevel >> colormaps.lightSegShift())
                        + colormaps.extralight;

                    if (MyBSP.curline.v1y == MyBSP.curline.v2y) {
                        lightnum--;
                    } else if (MyBSP.curline.v1x == MyBSP.curline.v2x) {
                        lightnum++;
                    }

                    if (lightnum < 0) {
                        colormaps.walllights = colormaps.scalelight[0];
                    } else if (lightnum >= colormaps.lightLevels()) {
                        colormaps.walllights
                            = colormaps.scalelight[colormaps.lightLevels() - 1];
                    } else {
                        colormaps.walllights = colormaps.scalelight[lightnum];
                    }
                }
            }

            // if a floor / ceiling plane is on the wrong side
            // of the view plane, it is definitely invisible
            // and doesn't need to be marked.
            if (MyBSP.frontsector.floorheight >= view.z) {
                // above view plane
                markfloor = false;
            }

            if (MyBSP.frontsector.ceilingheight <= view.z
                && MyBSP.frontsector.ceilingpic != TexMan.getSkyFlatNum()) {
                // below view plane
                markceiling = false;
            }

            // calculate incremental stepping values for texture edges
            worldtop >>= 4;
            worldbottom >>= 4;

            topstep = -FixedMul(rw_scalestep, worldtop);
            topfrac = (view.centeryfrac >> 4) - FixedMul(worldtop, rw_scale);

            bottomstep = -FixedMul(rw_scalestep, worldbottom);
            bottomfrac
                = (view.centeryfrac >> 4) - FixedMul(worldbottom, rw_scale);

            if (MyBSP.backsector != null) {
                worldhigh >>= 4;
                worldlow >>= 4;

                if (worldhigh < worldtop) {
                    pixhigh
                        = (view.centeryfrac >> 4) - FixedMul(worldhigh, rw_scale);
                    pixhighstep = -FixedMul(rw_scalestep, worldhigh);
                }

                if (worldlow > worldbottom) {
                    pixlow
                        = (view.centeryfrac >> 4) - FixedMul(worldlow, rw_scale);
                    pixlowstep = -FixedMul(rw_scalestep, worldlow);
                }
            }

            // render it
            if (markceiling) {
                // System.out.println("Markceiling");
                vp_vars.ceilingplane
                    = vp_vars.CheckPlane(vp_vars.ceilingplane, rw_x, rw_stopx - 1);
            }

            if (markfloor) {
                // System.out.println("Markfloor");
                vp_vars.floorplane
                    = vp_vars.CheckPlane(vp_vars.floorplane, rw_x, rw_stopx - 1);
            }

            RenderSegLoop();

            // After rendering is actually performed, clipping is set.
            // save sprite clipping info ... no top clipping?
            if ((C2JUtils.flags(seg.silhouette, SIL_TOP) || maskedtexture)
                && seg.nullSprTopClip()) {

                // memcpy (lastopening, ceilingclip+start, 2*(rw_stopx-start));
                System.arraycopy(ceilingclip, start, vp_vars.openings,
                    vp_vars.lastopening, rw_stopx - start);

                seg.setSprTopClip(vp_vars.openings, vp_vars.lastopening - start);
                // seg.setSprTopClipPointer();
                vp_vars.lastopening += rw_stopx - start;
            }
            // no floor clipping?
            if ((C2JUtils.flags(seg.silhouette, SIL_BOTTOM) || maskedtexture)
                && seg.nullSprBottomClip()) {
                // memcpy (lastopening, floorclip+start, 2*(rw_stopx-start));
                System.arraycopy(floorclip, start, vp_vars.openings,
                    vp_vars.lastopening, rw_stopx - start);
                seg.setSprBottomClip(vp_vars.openings, vp_vars.lastopening
                    - start);
                vp_vars.lastopening += rw_stopx - start;
            }

            if (maskedtexture && C2JUtils.flags(seg.silhouette, SIL_TOP)) {
                seg.silhouette |= SIL_TOP;
                seg.tsilheight = Integer.MIN_VALUE;
            }
            if (maskedtexture && (seg.silhouette & SIL_BOTTOM) == 0) {
                seg.silhouette |= SIL_BOTTOM;
                seg.bsilheight = Integer.MAX_VALUE;
            }
            seg_vars.ds_p++;
        }

        /**
         * R_RenderSegLoop Draws zero, one, or two textures (and possibly a
         * masked texture) for walls. Can draw or mark the starting pixel of
         * floor and ceiling textures. Also sets the actual sprite clipping info
         * (where sprites should be cut) Since rw_x ranges are non-overlapping,
         * rendering all walls means completing the clipping list as well. The
         * only difference between the parallel and the non-parallel version is
         * that the parallel doesn't draw immediately but rather, generates
         * RWIs. This can surely be unified to avoid replicating code. CALLED:
         * CORE LOOPING ROUTINE.
         */
        protected void RenderSegLoop() {
            int angle; // angle_t
            int index;
            int yl; // low
            int yh; // hight
            int mid;
            int texturecolumn = 0; // fixed_t
            int top;
            int bottom;

            for (; rw_x < rw_stopx; rw_x++) {
                // mark floor / ceiling areas
                yl = (topfrac + HEIGHTUNIT - 1) >> HEIGHTBITS;

                // no space above wall?
                if (yl < ceilingclip[rw_x] + 1) {
                    yl = ceilingclip[rw_x] + 1;
                }

                if (markceiling) {
                    top = ceilingclip[rw_x] + 1;
                    bottom = yl - 1;

                    if (bottom >= floorclip[rw_x]) {
                        bottom = floorclip[rw_x] - 1;
                    }

                    if (top <= bottom) {
                        vp_vars.visplanes[vp_vars.ceilingplane].setTop(rw_x,
                            (char) top);
                        vp_vars.visplanes[vp_vars.ceilingplane].setBottom(rw_x,
                            (char) bottom);
                    }
                }

                yh = bottomfrac >> HEIGHTBITS;

                if (yh >= floorclip[rw_x]) {
                    yh = floorclip[rw_x] - 1;
                }

                // A particular seg has been identified as a floor marker.
                if (markfloor) {
                    top = yh + 1;
                    bottom = floorclip[rw_x] - 1;
                    if (top <= ceilingclip[rw_x]) {
                        top = ceilingclip[rw_x] + 1;
                    }
                    if (top <= bottom) {
                        vp_vars.visplanes[vp_vars.floorplane].setTop(rw_x,
                            (char) top);
                        vp_vars.visplanes[vp_vars.floorplane].setBottom(rw_x,
                            (char) bottom);
                    }
                }

                // texturecolumn and lighting are independent of wall tiers
                if (segtextured) {
                    // calculate texture offset

                    // CAREFUL: a VERY anomalous point in the code. Their sum is
                    // supposed
                    // to give an angle not exceeding 45 degrees (or an index of
                    // 0x0FFF after
                    // shifting). If added with pure unsigned rules, this
                    // doesn't hold anymore,
                    // not even if accounting for overflow.
                    angle
                        = Tables.toBAMIndex(rw_centerangle
                            + (int) view.xtoviewangle[rw_x]);

                    // FIXME: We are accessing finetangent here, the code seems
                    // pretty confident in that angle won't exceed 4K no matter
                    // what.
                    // But xtoviewangle alone can yield 8K when shifted.
                    // This usually only overflows if we idclip and look at
                    // certain directions (probably angles get fucked up),
                    // however it seems rare
                    // enough to just "swallow" the exception. You can eliminate
                    // it by anding
                    // with 0x1FFF if you're so inclined.
                    // FIXED by allowing overflow. See Tables for details.
                    texturecolumn
                        = rw_offset - FixedMul(finetangent[angle], rw_distance);
                    texturecolumn >>= FRACBITS;
                    // calculate lighting
                    index = rw_scale >> colormaps.lightScaleShift();

                    if (index >= colormaps.maxLightScale()) {
                        index = colormaps.maxLightScale() - 1;
                    }

                    dcvars.dc_colormap = colormaps.walllights[index];
                    dcvars.dc_x = rw_x;
                    dcvars.dc_iscale = (int) (0xffffffffL / rw_scale);
                }

                // draw the wall tiers
                if (midtexture != 0) {
                    // single sided line
                    dcvars.dc_yl = yl;
                    dcvars.dc_yh = yh;
                    dcvars.dc_texheight
                        = TexMan.getTextureheight(midtexture) >> FRACBITS; // killough
                    dcvars.dc_texturemid = rw_midtexturemid;
                    dcvars.dc_source_ofs = 0;
                    dcvars.dc_source
                        = TexMan.GetCachedColumn(midtexture, texturecolumn);
                    CompleteColumn();
                    ceilingclip[rw_x] = (short) view.height;
                    floorclip[rw_x] = -1;
                } else {
                    // two sided line
                    if (toptexture != 0) {
                        // top wall
                        mid = pixhigh >> HEIGHTBITS;
                        pixhigh += pixhighstep;

                        if (mid >= floorclip[rw_x]) {
                            mid = floorclip[rw_x] - 1;
                        }

                        if (mid >= yl) {
                            dcvars.dc_yl = yl;
                            dcvars.dc_yh = mid;
                            dcvars.dc_texturemid = rw_toptexturemid;
                            dcvars.dc_texheight = TexMan.getTextureheight(toptexture) >> FRACBITS;
                            dcvars.dc_source = TexMan.GetCachedColumn(toptexture, texturecolumn);
                            dcvars.dc_source_ofs = 0;
                            if (dcvars.dc_colormap == null) {
                                System.out.println("Two-sided");
                            }
                            CompleteColumn();
                            ceilingclip[rw_x] = (short) mid;
                        } else {
                            ceilingclip[rw_x] = (short) (yl - 1);
                        }
                    } else {
                        // no top wall
                        if (markceiling) {
                            ceilingclip[rw_x] = (short) (yl - 1);
                        }
                    }

                    if (bottomtexture != 0) {
                        // bottom wall
                        mid = (pixlow + HEIGHTUNIT - 1) >> HEIGHTBITS;
                        pixlow += pixlowstep;

                        // no space above wall?
                        if (mid <= ceilingclip[rw_x]) {
                            mid = ceilingclip[rw_x] + 1;
                        }

                        if (mid <= yh) {
                            dcvars.dc_yl = mid;
                            dcvars.dc_yh = yh;
                            dcvars.dc_texturemid = rw_bottomtexturemid;
                            dcvars.dc_texheight = TexMan.getTextureheight(bottomtexture) >> FRACBITS;
                            dcvars.dc_source = TexMan.GetCachedColumn(bottomtexture, texturecolumn);
                            dcvars.dc_source_ofs = 0;
                            CompleteColumn();

                            floorclip[rw_x] = (short) mid;
                        } else {
                            floorclip[rw_x] = (short) (yh + 1);
                        }
                    } else {
                        // no bottom wall
                        if (markfloor) {
                            floorclip[rw_x] = (short) (yh + 1);
                        }
                    }

                    if (maskedtexture) {
                        // save texturecol
                        // for backdrawing of masked mid texture
                        seg_vars.maskedtexturecol[seg_vars.pmaskedtexturecol + rw_x] = (short) texturecolumn;
                    }
                }

                rw_scale += rw_scalestep;
                topfrac += topstep;
                bottomfrac += bottomstep;
            }
        }

        @Override
        public void ClearClips() {
            System.arraycopy(BLANKFLOORCLIP, 0, floorclip, 0, view.width);
            System.arraycopy(BLANKCEILINGCLIP, 0, ceilingclip, 0, view.width);
        }

        /**
         * Called from RenderSegLoop. This should either invoke the column
         * function, or store a wall rendering instruction in the parallel
         * version. It's the only difference between the parallel and serial
         * renderer, BTW. So override and implement accordingly.
         */
        protected abstract void CompleteColumn();

        @Override
        public void ExecuteSetViewSize(int viewwidth) {
            for (int i = 0; i < viewwidth; i++) {
                BLANKFLOORCLIP[i] = (short) view.height;
                BLANKCEILINGCLIP[i] = -1;
            }
        }

        @Override
        public void CompleteRendering() {
            // Nothing to do for serial. 
        }

        protected column_t col;

        public SegDrawer(SceneRenderer<?, ?> R) {
            this.vp_vars = R.getVPVars();
            this.seg_vars = R.getSegVars();
            col = new column_t();
            seg_vars.drawsegs = malloc(drawseg_t::new, drawseg_t[]::new, seg_vars.MAXDRAWSEGS);
            this.floorclip = new short[DOOM.vs.getScreenWidth()];
            this.ceilingclip = new short[DOOM.vs.getScreenWidth()];
            BLANKFLOORCLIP = new short[DOOM.vs.getScreenWidth()];
            BLANKCEILINGCLIP = new short[DOOM.vs.getScreenWidth()];
        }

        /**
         * R_ScaleFromGlobalAngle Returns the texture mapping scale for the
         * current line (horizontal span) at the given angle. rw_distance must
         * be calculated first.
         */
        protected int ScaleFromGlobalAngle(long visangle) {
            int scale; // fixed_t
            long anglea;
            long angleb;
            int sinea;
            int sineb;
            int num; // fixed_t
            int den;

            // UNUSED
            /*
             * { fixed_t dist; fixed_t z; fixed_t sinv; fixed_t cosv; sinv =
             * finesine[(visangle-rw_normalangle)>>ANGLETOFINESHIFT]; dist =
             * FixedDiv (rw_distance, sinv); cosv =
             * finecosine[(viewangle-visangle)>>ANGLETOFINESHIFT]; z =
             * abs(FixedMul (dist, cosv)); scale = FixedDiv(projection, z);
             * return scale; }
             */
            anglea = (ANG90 + visangle - view.angle) & BITS32;
            angleb = (ANG90 + visangle - rw_normalangle) & BITS32;

            // both sines are allways positive
            sinea = finesine(anglea);
            sineb = finesine(angleb);
            num = FixedMul(view.projection, sineb) << view.detailshift;
            den = FixedMul(rw_distance, sinea);

            if (den > num >> 16) {
                scale = FixedDiv(num, den);

                if (scale > 64 * FRACUNIT) {
                    scale = 64 * FRACUNIT;
                } else if (scale < 256) {
                    scale = 256;
                }
            } else {
                scale = 64 * FRACUNIT;
            }

            return scale;
        }

        @Override
        public void setGlobalAngle(long angle) {
            this.rw_angle1 = angle;
        }
    }

    protected interface IPlaneDrawer {
        void InitPlanes();
        void MapPlane(int y, int x1, int x2);
        void DrawPlanes();
        int[] getDistScale();

        /**
         * Sync up in case there's concurrent planes/walls rendering
         */
        void sync();
    }

    protected interface ISegDrawer extends ILimitResettable {
        void ClearClips();
        short[] getBLANKCEILINGCLIP();
        short[] getBLANKFLOORCLIP();
        short[] getFloorClip();
        short[] getCeilingClip();
        void ExecuteSetViewSize(int viewwidth);
        void setGlobalAngle(long angle1);
        void StoreWallRange(int first, int last);

        /**
         * If there is anything to do beyond the BPS traversal,
         * e.g. parallel rendering
         */
        void CompleteRendering();

        /**
         * Sync up in case there's concurrent planes/walls rendering
         */
        void sync();
    }

    protected class Planes extends PlaneDrawer<T, V> {

        Planes(DoomMain<T, V> DOOM, RendererState<T, V> R) {
            super(DOOM, R);
        }

        /**
         * R_DrawPlanes At the end of each frame. This also means that visplanes
         * must have been set BEFORE we called this function. Therefore, look
         * for errors behind.
         *
         * @throws IOException
         */
        @Override
        public void DrawPlanes() {
            if (DEBUG) {
                System.out.println(" >>>>>>>>>>>>>>>>>>>>>   DrawPlanes: " + vp_vars.lastvisplane);
            }
            visplane_t pln; // visplane_t
            int light;
            int x;
            int stop;
            int angle;

            if (RANGECHECK) {
                rangeCheckErrors();
            }

            for (int pl = 0; pl < vp_vars.lastvisplane; pl++) {
                pln = vp_vars.visplanes[pl];
                if (DEBUG2) {
                    System.out.println(pln);
                }

                if (pln.minx > pln.maxx) {
                    continue;
                }
                // sky flat
                if (pln.picnum == TexMan.getSkyFlatNum()) {
                    // Cache skytexture stuff here. They aren't going to change
                    // while
                    // being drawn, after all, are they?
                    int skytexture = TexMan.getSkyTexture();
                    skydcvars.dc_texheight
                        = TexMan.getTextureheight(skytexture) >> FRACBITS;
                    skydcvars.dc_iscale
                        = vpvars.getSkyScale() >> view.detailshift;

                    /**
                     * Sky is allways drawn full bright, i.e. colormaps[0] is
                     * used. Because of this hack, sky is not affected by INVUL
                     * inverse mapping.
                     * Settings.fixskypalette handles the fix
                     */
                    if (DOOM.CM.equals(Settings.fix_sky_palette, Boolean.TRUE) && colormap.fixedcolormap != null) {
                        skydcvars.dc_colormap = colormap.fixedcolormap;
                    } else {
                        skydcvars.dc_colormap = colormap.colormaps[Palettes.COLORMAP_FIXED];
                    }
                    skydcvars.dc_texturemid = TexMan.getSkyTextureMid();
                    for (x = pln.minx; x <= pln.maxx; x++) {

                        skydcvars.dc_yl = pln.getTop(x);
                        skydcvars.dc_yh = pln.getBottom(x);

                        if (skydcvars.dc_yl <= skydcvars.dc_yh) {
                            angle
                                = (int) (addAngles(view.angle, view.xtoviewangle[x]) >>> ANGLETOSKYSHIFT);
                            skydcvars.dc_x = x;
                            // Optimized: texheight is going to be the same
                            // during normal skies drawing...right?
                            skydcvars.dc_source
                                = TexMan.GetCachedColumn(skytexture, angle);
                            colfunc.sky.invoke();
                        }
                    }
                    continue;
                }

                // regular flat
                dsvars.ds_source = TexMan.getSafeFlat(pln.picnum);

                planeheight = Math.abs(pln.height - view.z);
                light = (pln.lightlevel >> colormap.lightSegShift()) + colormap.extralight;

                if (light >= colormap.lightLevels()) {
                    light = colormap.lightLevels() - 1;
                }

                if (light < 0) {
                    light = 0;
                }

                planezlight = colormap.zlight[light];

                // We set those values at the border of a plane's top to a
                // "sentinel" value...ok.
                pln.setTop(pln.maxx + 1, visplane_t.SENTINEL);
                pln.setTop(pln.minx - 1, visplane_t.SENTINEL);

                stop = pln.maxx + 1;

                for (x = pln.minx; x <= stop; x++) {
                    MakeSpans(x, pln.getTop(x - 1), pln.getBottom(x - 1), pln.getTop(x), pln.getBottom(x));
                }

                // Z_ChangeTag (ds_source, PU_CACHE);
            }
        }

    } // End Plane class

    // /////////////////////// LIGHTS, POINTERS, COLORMAPS ETC. ////////////////
    // /// FROM R_DATA, R_MAIN , R_DRAW //////////
    /**
     * OK< this is supposed to "peg" into screen buffer 0. It will work AS LONG
     * AS SOMEONE FUCKING ACTUALLY SETS IT !!!!
     */
    protected V screen;

    protected static final boolean RANGECHECK = false;

    /**
     * These are actually offsets inside screen 0 (or any screen). Therefore
     * anything using them should "draw" inside screen 0
     */
    protected int[] ylookup = new int[MAXHEIGHT];

    /**
     * Columns offset to set where?!
     */
    protected int[] columnofs = new int[MAXWIDTH];

    /**
     * General purpose. Used for solid walls and as an intermediary for
     * threading
     */
    protected ColVars<T, V> dcvars;

    /**
     * Used for spans
     */
    protected SpanVars<T, V> dsvars;

    // Used for sky drawer, to avoid clashing with shared dcvars
    protected ColVars<T, V> skydcvars;

    /**
     * Masked drawing functions get "pegged" to this set of dcvars, passed upon
     * initialization. However, multi-threaded vars are better off carrying each
     * their own ones.
     */
    protected ColVars<T, V> maskedcvars;

    /**
     * e6y: wide-res Borrowed from PrBoom+;
     */

    /*
     * protected int wide_centerx, wide_ratio, wide_offsetx, wide_offset2x,
     * wide_offsety, wide_offset2y; protected final base_ratio_t[]
     * BaseRatioSizes = { new base_ratio_t(960, 600, 0, 48, 1.333333f), // 4:3
     * new base_ratio_t(1280, 450, 0, 48 * 3 / 4, 1.777777f), // 16:9 new
     * base_ratio_t(1152, 500, 0, 48 * 5 / 6, 1.6f), // 16:10 new
     * base_ratio_t(960, 600, 0, 48, 1.333333f), new base_ratio_t(960, 640,
     * (int) (6.5 * FRACUNIT), 48 * 15 / 16, 1.25f) // 5:4 };
     */
    /**
     * just for profiling purposes
     */
    protected int framecount;
    protected int sscount;
    protected int linecount;
    protected int loopcount;

    //
    // precalculated math tables
    //
    protected long clipangle;

    // Set to 2*clipangle later.
    protected long CLIPANGLE2;

    // The viewangletox[viewangle + FINEANGLES/4] lookup
    // maps the visible view angles to screen X coordinates,
    // flattening the arc to a flat projection plane.
    // There will be many angles mapped to the same X.
    protected final int[] viewangletox = new int[FINEANGLES / 2];

    /**
     * The xtoviewangle[] table maps a screen pixel to the lowest viewangle that
     * maps back to x ranges from clipangle to -clipangle.
     *
     * @see view.xtoviewangle
     */
    //protected long[] view.xtoviewangle;// MAES: to resize
    // UNUSED.
    // The finetangentgent[angle+FINEANGLES/4] table
    // holds the fixed_t tangent values for view angles,
    // ranging from MININT to 0 to MAXINT.
    // fixed_t finetangent[FINEANGLES/2];
    // fixed_t finesine[5*FINEANGLES/4];
    // MAES: uh oh. So now all these ints must become finesines? fuck that.
    // Also wtf @ this hack....this points to approx 1/4th of the finesine
    // table, but what happens if I read past it?
    // int[] finecosine = finesine[FINEANGLES/4];

    /*
     * MAES: what's going on with light tables here. OK...so these should be
     * "unsigned bytes", since, after all, they'll be used as pointers inside an
     * array to finally pick a color, so they should be expanded to shorts.
     */
    // //////////// SOME UTILITY METHODS /////////////
    /**
     * Assigns a point of view before calling PointToAngle CAREFUL: this isn't a
     * pure function, as it alters the renderer's state!
     */
    @Override
    public final long PointToAngle2(int x1, int y1, int x2, int y2) {
        // Careful with assignments...
        view.x = x1;
        view.y = y1;

        return view.PointToAngle(x2, y2);
    }

    //
    // R_InitPointToAngle
    //
    /*
     * protected final void InitPointToAngle () { // UNUSED - now getting from
     * tables.c if (false){ int i; long t; float f; // // slope (tangent) to
     * angle lookup // for (i=0 ; i<=SLOPERANGE ; i++) { f = (float) Math.atan(
     * (double)(i/SLOPERANGE )/(3.141592657*2)); t = (long) (0xffffffffL*f);
     * tantoangle[i] = (int) t; } } }
     */
    /**
     * Public, static, stateless version of PointToAngle2. Call this one when
     * "renderless" use of PointToAngle2 is required.
     */
    public static long PointToAngle(int viewx, int viewy, int x, int y) {
        // MAES: note how we don't use &BITS32 here. That is because
        // we know that the maximum possible value of tantoangle is angle
        // This way, we are actually working with vectors emanating
        // from our current position.
        x -= viewx;
        y -= viewy;

        if ((x == 0) && (y == 0)) {
            return 0;
        }

        if (x >= 0) {
            // x >=0
            if (y >= 0) {
                // y>= 0

                if (x > y) {
                    // octant 0
                    return tantoangle[SlopeDiv(y, x)];
                } else {
                    // octant 1
                    return (ANG90 - 1 - tantoangle[SlopeDiv(x, y)]);
                }
            } else {
                // y<0
                y = -y;

                if (x > y) {
                    // octant 8
                    return (-tantoangle[SlopeDiv(y, x)]);
                } else {
                    // octant 7
                    return (ANG270 + tantoangle[SlopeDiv(x, y)]);
                }
            }
        } else {
            // x<0
            x = -x;

            if (y >= 0) {
                // y>= 0
                if (x > y) {
                    // octant 3
                    return (ANG180 - 1 - tantoangle[SlopeDiv(y, x)]);
                } else {
                    // octant 2
                    return (ANG90 + tantoangle[SlopeDiv(x, y)]);
                }
            } else {
                // y<0
                y = -y;

                if (x > y) {
                    // octant 4
                    return (ANG180 + tantoangle[SlopeDiv(y, x)]);
                } else {
                    // octant 5
                    return (ANG270 - 1 - tantoangle[SlopeDiv(x, y)]);
                }
            }
        }
        // This is actually unreachable.
        // return 0;
    }

    //
    // R_InitTables
    //
    protected void InitTables() {
        // UNUSED: now getting from tables.c
        /*
         * int i; float a; float fv; int t; // viewangle tangent table for (i=0
         * ; i<FINEANGLES/2 ; i++) { a = (i-FINEANGLES/4+0.5)*PI*2/FINEANGLES;
         * fv = FRACUNIT*tan (a); t = fv; finetangent[i] = t; } // finesine
         * table for (i=0 ; i<5*FINEANGLES/4 ; i++) { // OPTIMIZE: mirro.. a =
         * (i+0.5)*PI*2/FINEANGLES; t = FRACUNIT*sin (a); finesine[i] = t; }
         */

    }

    /**
     * R_PointToDist
     *
     * @param x
     * fixed_t
     * @param y
     * fixed_t
     * @return
     */
    protected int PointToDist(int x, int y) {
        int angle;
        int dx;
        int dy;
        int temp;
        int dist;

        dx = Math.abs(x - view.x);
        dy = Math.abs(y - view.y);

        // If something is farther north/south than west/east, it gets swapped.
        // Probably as a crude way to avoid divisions by zero. This divides
        // the field into octants, rather than quadrants, where the biggest
        // angle to
        // consider is 45...right? So dy/dx can never exceed 1.0, in theory.
        if (dy > dx) {
            temp = dx;
            dx = dy;
            dy = temp;
        }

        // If one or both of the distances are *exactly* zero at this point,
        // then this means that the wall is in your face anyway, plus we want to
        // avoid a division by zero. So you get zero.
        if (dx == 0) {
            return 0;
        }

        /*
         * If dx is zero, this is going to bomb. Fixeddiv will return MAXINT aka
         * 7FFFFFFF, >> DBITS will make it 3FFFFFF, which is more than enough to
         * break tantoangle[]. In the original C code, this probably didn't
         * matter: there would probably be garbage orientations thrown all
         * around. However this is unacceptable in Java. OK, so the safeguard
         * above prevents that. Still... this method is only called once per
         * visible wall per frame, so one check more or less at this point won't
         * change much. It's better to be safe than sorry.
         */
        // This effectively limits the angle to
        // angle = Math.max(FixedDiv(dy, dx), 2048) >> DBITS;
        angle = (FixedDiv(dy, dx) & 0x1FFFF) >> DBITS;

        // Since the division will be 0xFFFF at most, DBITS will restrict
        // the maximum angle index to 7FF, about 45, so adding ANG90 with
        // no other safeguards is OK.
        angle = (int) ((tantoangle[angle] + ANG90) >> ANGLETOFINESHIFT);

        // use as cosine
        dist = FixedDiv(dx, finesine[angle]);

        return dist;
    }

    // //////////// COMMON RENDERING GLOBALS ////////////////
    // //////////////// COLUMN AND SPAN FUNCTIONS //////////////
    protected ColFuncs<T, V> colfunc;

    // Keep two sets of functions.
    protected ColFuncs<T, V> colfunchi;

    protected ColFuncs<T, V> colfunclow;

    protected void setHiColFuns() {
        colfunchi.main = colfunchi.base = DrawColumn;
        colfunchi.masked = DrawColumnMasked;
        colfunchi.fuzz = DrawFuzzColumn;
        colfunchi.trans = DrawTranslatedColumn;
        colfunchi.glass = DrawTLColumn;
        colfunchi.player = DrawColumnPlayer;
        colfunchi.sky = DrawColumnSkies;
    }

    protected void setLowColFuns() {
        colfunclow.main = colfunclow.base = DrawColumnLow;
        colfunclow.masked = DrawColumnMaskedLow;
        colfunclow.fuzz = DrawFuzzColumnLow;
        colfunclow.trans = DrawTranslatedColumnLow;
        colfunclow.glass = DrawTLColumn;
        colfunclow.player = DrawColumnMaskedLow;
        colfunclow.sky = DrawColumnSkiesLow;
    }

    @Override
    public ColFuncs<T, V> getColFuncsHi() {
        return this.colfunchi;
    }

    @Override
    public ColFuncs<T, V> getColFuncsLow() {
        return this.colfunclow;
    }

    @Override
    public ColVars<T, V> getMaskedDCVars() {
        return this.maskedcvars;
    }

    // These column functions are "fixed" for a given renderer, and are
    // not used directly, but only after passing them to colfuncs
    protected DoomColumnFunction<T, V> DrawTranslatedColumn;
    protected DoomColumnFunction<T, V> DrawTranslatedColumnLow;
    protected DoomColumnFunction<T, V> DrawColumnPlayer;
    protected DoomColumnFunction<T, V> DrawColumnSkies;
    protected DoomColumnFunction<T, V> DrawColumnSkiesLow;
    protected DoomColumnFunction<T, V> DrawFuzzColumn;
    protected DoomColumnFunction<T, V> DrawFuzzColumnLow;
    protected DoomColumnFunction<T, V> DrawColumn;
    protected DoomColumnFunction<T, V> DrawColumnLow;
    protected DoomColumnFunction<T, V> DrawColumnMasked;
    protected DoomColumnFunction<T, V> DrawColumnMaskedLow;
    protected DoomColumnFunction<T, V> DrawTLColumn;

    /**
     * to be set in UnifiedRenderer
     */
    protected DoomSpanFunction<T, V> DrawSpan, DrawSpanLow;

    // ////////////// r_draw methods //////////////
    /**
     * R_DrawViewBorder Draws the border around the view for different size windows
     * Made use of CopyRect there
     * - Good Sign 2017/04/06
     */
    @Override
    public void DrawViewBorder() {
        if (view.scaledwidth == DOOM.vs.getScreenWidth()) {
            return;
        }

        final int top = ((DOOM.vs.getScreenHeight() - DOOM.statusBar.getHeight()) - view.height) / 2;
        final int side = (DOOM.vs.getScreenWidth() - view.scaledwidth) / 2;
        final Rectangle rect;
        // copy top
        rect = new Rectangle(0, 0, DOOM.vs.getScreenWidth(), top);
        DOOM.graphicSystem.CopyRect(BG, rect, FG);
        // copy left side
        rect.setBounds(0, top, side, view.height);
        DOOM.graphicSystem.CopyRect(BG, rect, FG);
        // copy right side
        rect.x = side + view.scaledwidth;
        DOOM.graphicSystem.CopyRect(BG, rect, FG);
        // copy bottom
        rect.setBounds(0, top + view.height, DOOM.vs.getScreenWidth(), top);
        DOOM.graphicSystem.CopyRect(BG, rect, FG);
    }

    @Override
    public void ExecuteSetViewSize() {
        int cosadj;
        int dy;
        int level;
        int startmap;

        setsizeneeded = false;

        // 11 Blocks means "full screen"
        if (setblocks == 11) {
            view.scaledwidth = DOOM.vs.getScreenWidth();
            view.height = DOOM.vs.getScreenHeight();
        } else if (DOOM.CM.equals(Settings.scale_screen_tiles, Boolean.TRUE)) {
            /**
             * Make it exactly as in vanilla DOOM
             *  - Good Sign 2017/05/08
             */
            view.scaledwidth = (setblocks * 32) * DOOM.vs.getScalingX();
            view.height = ((setblocks * 168 / 10) & ~7) * DOOM.vs.getScalingY();
        } else { // Mocha Doom formula looks better for non-scaled tiles
            view.scaledwidth = setblocks * (DOOM.vs.getScreenWidth() / 10);
            // Height can only be a multiple of 8.
            view.height = (short) ((setblocks * (DOOM.vs.getScreenHeight() - DOOM.statusBar.getHeight()) / 10) & ~7);
        }

        skydcvars.viewheight
            = maskedcvars.viewheight = dcvars.viewheight = view.height;

        view.detailshift = setdetail;
        view.width = view.scaledwidth >> view.detailshift;

        view.centery = view.height / 2;
        view.centerx = view.width / 2;
        view.centerxfrac = (view.centerx << FRACBITS);
        view.centeryfrac = (view.centery << FRACBITS);
        view.projection = view.centerxfrac;

        skydcvars.centery = maskedcvars.centery = dcvars.centery = view.centery;

        // High detail
        if (view.detailshift == 0) {

            colfunc = colfunchi;
            dsvars.spanfunc = DrawSpan;
        } else {
            // Low detail
            colfunc = colfunclow;
            dsvars.spanfunc = DrawSpanLow;

        }

        InitBuffer(view.scaledwidth, view.height);

        InitTextureMapping();

        // psprite scales
        // pspritescale = FRACUNIT*viewwidth/vs.getScreenWidth();
        // pspriteiscale = FRACUNIT*vs.getScreenWidth()/viewwidth;
        MyThings.setPspriteScale((int) (FRACUNIT * (DOOM.vs.getScreenMul() * view.width) / DOOM.vs.getScreenWidth()));
        MyThings.setPspriteIscale((int) (FRACUNIT * (DOOM.vs.getScreenWidth() / (view.width * DOOM.vs.getScreenMul()))));
        vp_vars.setSkyScale((int) (FRACUNIT * (DOOM.vs.getScreenWidth() / (view.width * DOOM.vs.getScreenMul()))));

        view.BOBADJUST = this.DOOM.vs.getSafeScaling() << 15;
        view.WEAPONADJUST = (int) ((DOOM.vs.getScreenWidth() / (2 * DOOM.vs.getScreenMul())) * FRACUNIT);

        // thing clipping
        for (int i = 0; i < view.width; i++) {
            view.screenheightarray[i] = (short) view.height;
        }

        // planes
        for (int i = 0; i < view.height; i++) {
            dy = ((i - view.height / 2) << FRACBITS) + FRACUNIT / 2;
            dy = Math.abs(dy);
            vp_vars.yslope[i] = FixedDiv((view.width << view.detailshift) / 2 * FRACUNIT, dy);
            // MyPlanes.yslopef[i] = ((viewwidth<<detailshift)/2)/ dy;
        }

        // double cosadjf;
        for (int i = 0; i < view.width; i++) {
            // MAES: In this spot we must interpet it as SIGNED, else it's
            // pointless, right?
            // MAES: this spot caused the "warped floor bug", now fixed. Don't
            // forget xtoviewangle[i]!
            cosadj = Math.abs(finecosine(view.xtoviewangle[i]));
            // cosadjf =
            // Math.abs(Math.cos((double)xtoviewangle[i]/(double)0xFFFFFFFFL));
            MyPlanes.getDistScale()[i] = FixedDiv(FRACUNIT, cosadj);
            // MyPlanes.distscalef[i] = (float) (1.0/cosadjf);
        }

        // Calculate the light levels to use
        // for each level / scale combination.
        for (int i = 0; i < colormaps.lightLevels(); i++) {
            startmap = ((colormaps.lightLevels() - colormaps.lightBright() - i) * 2) * colormaps.numColorMaps() / colormaps.lightLevels();
            for (int j = 0; j < colormaps.maxLightScale(); j++) {
                level
                    = startmap - j / DISTMAP;
                if (level < 0) {
                    level = 0;
                }
                if (level >= colormaps.numColorMaps()) {
                    level = colormaps.numColorMaps() - 1;
                }
                colormaps.scalelight[i][j] = colormaps.colormaps[level];
            }
        }

        MySegs.ExecuteSetViewSize(view.width);
    }
    
    private final Rectangle backScreenRect = new Rectangle();
    private final Rectangle tilePatchRect = new Rectangle();

    /**
     * R_FillBackScreen Fills the back screen with a pattern for variable screen
     * sizes Also draws a beveled edge. This is actually stored in screen 1, and
     * is only OCCASIONALLY written to screen 0 (the visible one) by calling
     * R_VideoErase.
     */
    @Override
    @R_Draw.C(R_FillBackScreen)
    public void FillBackScreen() {
        final boolean scaleSetting = Engine.getConfig().equals(Settings.scale_screen_tiles, Boolean.TRUE);
        flat_t src;
        DoomScreen dest;
        int x;
        int y;
        patch_t patch;

        // DOOM border patch.
        String name1 = "FLOOR7_2";

        // DOOM II border patch.
        String name2 = "GRNROCK";

        String name;

        if (view.scaledwidth == DOOM.vs.getScreenWidth()) {
            return;
        }

        if (DOOM.isCommercial()) {
            name = name2;
        } else {
            name = name1;
        }

        /* This is a flat we're reading here */
        src = DOOM.wadLoader.CacheLumpName(name, PU_CACHE, flat_t.class);
        dest = BG;
        
        /**
         * TODO: cache it?
         * This part actually draws the border itself, without bevels
         * 
         * MAES:
         * improved drawing routine for extended bit-depth compatibility.
         *
         * Now supports configurable vanilla-like scaling of tiles
         * - Good Sign 2017/04/09
         * 
         * @SourceCode.Compatible
         */
        Tiling: {
            this.backScreenRect.setBounds(0, 0, DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight() - DOOM.statusBar.getHeight());
            this.tilePatchRect.setBounds(0, 0, 64, 64);
            V block = DOOM.graphicSystem.convertPalettedBlock(src.data);
            if (scaleSetting) {
                block = DOOM.graphicSystem.ScaleBlock(block, DOOM.vs, tilePatchRect.width, tilePatchRect.height);
                this.tilePatchRect.width *= DOOM.graphicSystem.getScalingX();
                this.tilePatchRect.height *= DOOM.graphicSystem.getScalingY();
            }
            DOOM.graphicSystem.TileScreenArea(dest, backScreenRect, block, tilePatchRect);
        }
        
        final int scaleFlags = V_NOSCALESTART | (scaleSetting ? 0 : V_NOSCALEOFFSET | V_NOSCALEPATCH);
        final int stepX = scaleSetting ? DOOM.graphicSystem.getScalingX() << 3 : 8;
        final int stepY = scaleSetting ? DOOM.graphicSystem.getScalingY() << 3 : 8;

        patch = DOOM.wadLoader.CachePatchName("BRDR_T", PU_CACHE);
        for (x = 0; x < view.scaledwidth; x += stepX) {
            DOOM.graphicSystem.DrawPatchScaled(BG, patch, DOOM.vs, view.windowx + x, view.windowy - stepY, scaleFlags);
        }

        patch = DOOM.wadLoader.CachePatchName("BRDR_B", PU_CACHE);
        for (x = 0; x < view.scaledwidth; x += stepX) {
            DOOM.graphicSystem.DrawPatchScaled(BG, patch, DOOM.vs, view.windowx + x, view.windowy + view.height, scaleFlags);
        }

        patch = DOOM.wadLoader.CachePatchName("BRDR_L", PU_CACHE);
        for (y = 0; y < view.height; y += stepY) {
            DOOM.graphicSystem.DrawPatchScaled(BG, patch, DOOM.vs, view.windowx - stepX, view.windowy + y, scaleFlags);
        }

        patch = DOOM.wadLoader.CachePatchName("BRDR_R", PU_CACHE);
        for (y = 0; y < view.height; y += stepY) {
            DOOM.graphicSystem.DrawPatchScaled(BG, patch, DOOM.vs, view.windowx + view.scaledwidth, view.windowy + y, scaleFlags);
        }

        // Draw beveled edge. Top-left
        patch = DOOM.wadLoader.CachePatchName("BRDR_TL", PU_CACHE);
        DOOM.graphicSystem.DrawPatchScaled(BG, patch, DOOM.vs, view.windowx - stepX, view.windowy - stepY, scaleFlags);

        // Top-right.
        patch = DOOM.wadLoader.CachePatchName("BRDR_TR", PU_CACHE);
        DOOM.graphicSystem.DrawPatchScaled(BG, patch, DOOM.vs, view.windowx + view.scaledwidth, view.windowy - stepY, scaleFlags);

        // Bottom-left
        patch = DOOM.wadLoader.CachePatchName("BRDR_BL", PU_CACHE);
        DOOM.graphicSystem.DrawPatchScaled(BG, patch, DOOM.vs, view.windowx - stepX, view.windowy + view.height, scaleFlags);
        
        // Bottom-right.
        patch = DOOM.wadLoader.CachePatchName("BRDR_BR", PU_CACHE);
        DOOM.graphicSystem.DrawPatchScaled(BG, patch, DOOM.vs, view.windowx + view.width, view.windowy + view.height, scaleFlags);
    }

    /**
     * R_Init
     */
    @Override
    public void Init() {
        // Any good reason for this to be here?
        // drawsegs=new drawseg_t[MAXDRAWSEGS];
        // C2JUtils.initArrayOfObjects(drawsegs);

        // DON'T FORGET ABOUT MEEEEEE!!!11!!!
        this.screen = this.DOOM.graphicSystem.getScreen(FG);

        System.out.print("\nR_InitData");
        InitData();
        // InitPointToAngle ();
        System.out.print("\nR_InitPointToAngle");

        // ds.DM.viewwidth / ds.viewheight / detailLevel are set by the defaults
        System.out.print("\nR_InitTables");
        InitTables();

        SetViewSize(DOOM.menu.getScreenBlocks(), DOOM.menu.getDetailLevel());

        System.out.print("\nR_InitPlanes");
        MyPlanes.InitPlanes();

        System.out.print("\nR_InitLightTables");
        InitLightTables();

        System.out.print("\nR_InitSkyMap: " + TexMan.InitSkyMap());

        System.out.print("\nR_InitTranslationsTables");
        InitTranslationTables();

        System.out.print("\nR_InitTranMap: ");
        R_InitTranMap(0);

        System.out.print("\nR_InitDrawingFunctions: ");
        R_InitDrawingFunctions();

        framecount = 0;
    }

    /**
     * R_InitBuffer Creates lookup tables that avoid multiplies and other
     * hazzles for getting the framebuffer address of a pixel to draw. MAES:
     * this is "pinned" to screen[0] of a Video Renderer. We will handle this
     * differently elsewhere...
     */
    protected void InitBuffer(int width, int height) {
        int i;

        // Handle resize,
        // e.g. smaller view windows
        // with border and/or status bar.
        view.windowx = (DOOM.vs.getScreenWidth() - width) >> 1;

        // Column offset. For windows.
        for (i = 0; i < width; i++) {
            columnofs[i] = view.windowx + i;
        }

        // SamE with base row offset.
        if (width == DOOM.vs.getScreenWidth()) {
            view.windowy = 0;
        } else {
            view.windowy = (DOOM.vs.getScreenHeight() - DOOM.statusBar.getHeight() - height) >> 1;
        }

        // Preclaculate all row offsets.
        for (i = 0; i < height; i++) {
            ylookup[i] = /* screens[0] + */ (i + view.windowy) * DOOM.vs.getScreenWidth();
        }
    }

    /**
     * R_InitTextureMapping Not moved into the TextureManager because it's
     * tighly coupled to the visuals, rather than textures. Perhaps the name is
     * not the most appropriate.
     */
    protected void InitTextureMapping() {
        int i, x, t;
        int focallength; // fixed_t
        int fov = FIELDOFVIEW;

        // For widescreen displays, increase the FOV so that the middle part of
        // the
        // screen that would be visible on a 4:3 display has the requested FOV.
        /*
         * UNUSED if (wide_centerx != centerx) { // wide_centerx is what centerx
         * would be // if the display was not widescreen fov = (int)
         * (Math.atan((double) centerx Math.tan((double) fov * Math.PI /
         * FINEANGLES) / (double) wide_centerx) FINEANGLES / Math.PI); if (fov >
         * 130 * FINEANGLES / 360) fov = 130 * FINEANGLES / 360; }
         */
        // Use tangent table to generate viewangletox:
        // viewangletox will give the next greatest x
        // after the view angle.
        //
        // Calc focallength
        // so FIELDOFVIEW angles covers vs.getScreenWidth().
        focallength
            = FixedDiv(view.centerxfrac, finetangent[QUARTERMARK + FIELDOFVIEW
                / 2]);

        for (i = 0; i < FINEANGLES / 2; i++) {
            if (finetangent[i] > FRACUNIT * 2) {
                t = -1;
            } else if (finetangent[i] < -FRACUNIT * 2) {
                t = view.width + 1;
            } else {
                t = FixedMul(finetangent[i], focallength);
                t = (view.centerxfrac - t + FRACUNIT - 1) >> FRACBITS;

                if (t < -1) {
                    t = -1;
                } else if (t > view.width + 1) {
                    t = view.width + 1;
                }
            }
            viewangletox[i] = t;
        }

        // Scan viewangletox[] to generate xtoviewangle[]:
        // xtoviewangle will give the smallest view angle
        // that maps to x.
        for (x = 0; x <= view.width; x++) {
            i = 0;
            while (viewangletox[i] > x) {
                i++;
            }
            view.xtoviewangle[x] = addAngles((i << ANGLETOFINESHIFT), -ANG90);
        }

        // Take out the fencepost cases from viewangletox.
        for (i = 0; i < FINEANGLES / 2; i++) {
            t = FixedMul(finetangent[i], focallength);
            t = view.centerx - t;

            if (viewangletox[i] == -1) {
                viewangletox[i] = 0;
            } else if (viewangletox[i] == view.width + 1) {
                viewangletox[i] = view.width;
            }
        }

        clipangle = view.xtoviewangle[0];
        // OPTIMIZE: assign constant for optimization.
        CLIPANGLE2 = (2 * clipangle) & BITS32;
    }

    //
    // R_InitLightTables
    // Only inits the zlight table,
    // because the scalelight table changes with view size.
    //
    protected final static int DISTMAP = 2;

    protected void InitLightTables() {
        int i;
        int j;
        int startmap;
        int scale;

        // Calculate the light levels to use
        // for each level / distance combination.
        for (i = 0; i < colormaps.lightLevels(); i++) {
            startmap = ((colormaps.lightLevels() - colormaps.lightBright() - i) * 2) * colormaps.numColorMaps() / colormaps.lightLevels();
            for (j = 0; j < colormaps.maxLightZ(); j++) {
                // CPhipps - use 320 here instead of vs.getScreenWidth(), otherwise hires is
                //           brighter than normal res

                scale = FixedDiv((320 / 2 * FRACUNIT), (j + 1) << colormaps.lightZShift());
                int t, level = startmap - (scale >>= colormaps.lightScaleShift()) / DISTMAP;

                if (level < 0) {
                    level = 0;
                }

                if (level >= colormaps.numColorMaps()) {
                    level = colormaps.numColorMaps() - 1;
                }

                // zlight[i][j] = colormaps + level*256;
                colormaps.zlight[i][j] = colormaps.colormaps[level];
            }
        }
    }

    protected static final int TSC = 12;
    
    /**
     * number of fixed point digits in
     * filter percent
     */
    byte[] main_tranmap;

    /**
     * A faster implementation of the tranmap calculations. Almost 10x faster
     * than the old one!
     *
     * @param progress
     */
    protected void R_InitTranMap(int progress) {
        int lump = DOOM.wadLoader.CheckNumForName("TRANMAP");

        long ta = System.nanoTime();

        // PRIORITY: a map file has been specified from commandline. Try to read
        // it. If OK, this trumps even those specified in lumps.
        DOOM.cVarManager.with(CommandVariable.TRANMAP, 0, (String tranmap) -> {
            if (C2JUtils.testReadAccess(tranmap)) {
                System.out.printf("Translucency map file %s specified in -tranmap arg. Attempting to use...\n", tranmap);
                main_tranmap = new byte[256 * 256]; // killough 4/11/98
                int result = MenuMisc.ReadFile(tranmap, main_tranmap);
                if (result > 0) {
                    return;
                }
                System.out.print("...failure.\n");
            }
        });

        // Next, if a tranlucency filter map lump is present, use it
        if (lump != -1) { // Set a pointer to the translucency filter maps.
            System.out.print("Translucency map found in lump. Attempting to use...");
            // main_tranmap=new byte[256*256]; // killough 4/11/98
            main_tranmap = DOOM.wadLoader.CacheLumpNumAsRawBytes(lump, Defines.PU_STATIC); // killough
            // 4/11/98
            // Tolerate 64K or more.
            if (main_tranmap.length >= 0x10000) {
                return;
            }
            System.out.print("...failure.\n"); // Not good, try something else.
        }

        // A default map file already exists. Try to read it.
        if (C2JUtils.testReadAccess("tranmap.dat")) {
            System.out.print("Translucency map found in default tranmap.dat file. Attempting to use...");
            main_tranmap = new byte[256 * 256]; // killough 4/11/98
            int result = MenuMisc.ReadFile("tranmap.dat", main_tranmap);
            if (result > 0) {
                return; // Something went wrong, so fuck that.
            }
        }

        // Nothing to do, so we must synthesize it from scratch. And, boy, is it
        // slooow.
        { // Compose a default transparent filter map based on PLAYPAL.
            System.out.print("Computing translucency map from scratch...that's gonna be SLOW...");
            byte[] playpal = DOOM.wadLoader.CacheLumpNameAsRawBytes("PLAYPAL", Defines.PU_STATIC);
            main_tranmap = new byte[256 * 256]; // killough 4/11/98
            int[] basepal = new int[3 * 256];
            int[] mixedpal = new int[3 * 256 * 256];

            main_tranmap = new byte[256 * 256];

            // Init array of base colors.
            for (int i = 0; i < 256; i++) {
                basepal[3 * i] = 0Xff & playpal[i * 3];
                basepal[1 + 3 * i] = 0Xff & playpal[1 + i * 3];
                basepal[2 + 3 * i] = 0Xff & playpal[2 + i * 3];
            }

            // Init array of mixed colors. These are true RGB.
            // The diagonal of this array will be the original colors.
            for (int i = 0; i < 256 * 3; i += 3) {
                for (int j = 0; j < 256 * 3; j += 3) {
                    mixColors(basepal, basepal, mixedpal, i, j, j * 256 + i);
                }
            }

            // Init distance map. Every original palette colour has a
            // certain distance from all the others. The diagonal is zero.
            // The interpretation is that e.g. the mixture of color 2 and 8 will
            // have a RGB value, which is closest to euclidean distance to
            // e.g. original color 9. Therefore we should put "9" in the (2,8)
            // and (8,2) cells of the tranmap.
            final float[] tmpdist = new float[256];

            for (int a = 0; a < 256; a++) {
                for (int b = a; b < 256; b++) {
                    // We evaluate the mixture of a and b
                    // Construct distance table vs all of the ORIGINAL colors.
                    for (int k = 0; k < 256; k++) {
                        tmpdist[k] = colorDistance(mixedpal, basepal, 3 * (a + b * 256), k * 3);
                    }

                    main_tranmap[(a << 8) | b] = (byte) findMin(tmpdist);
                    main_tranmap[(b << 8) | a] = main_tranmap[(a << 8) | b];
                }
            }
            System.out.print("...done\n");
            if (MenuMisc.WriteFile("tranmap.dat", main_tranmap,
                main_tranmap.length)) {
                System.out.print("TRANMAP.DAT saved to disk for your convenience! Next time will be faster.\n");
            }
        }

        long b = System.nanoTime();
        System.out.printf("Tranmap %d\n", (b - ta) / 1000000);
    }

    /**
     * Mixes two RGB colors. Nuff said
     */
    protected void mixColors(int[] a, int[] b, int[] c, int pa, int pb,
        int pc) {
        c[pc] = (a[pa] + b[pb]) / 2;
        c[pc + 1] = (a[pa + 1] + b[pb + 1]) / 2;
        c[pc + 2] = (a[pa + 2] + b[pb + 2]) / 2;

    }

    /**
     * Returns the euclidean distance of two RGB colors. Nuff said
     */
    protected float colorDistance(int[] a, int[] b, int pa, int pb) {
        return (float) Math.sqrt((a[pa] - b[pb]) * (a[pa] - b[pb])
            + (a[pa + 1] - b[pb + 1]) * (a[pa + 1] - b[pb + 1])
            + (a[pa + 2] - b[pb + 2]) * (a[pa + 2] - b[pb + 2]));
    }

    /**
     * Stuff that is trivially initializable, even with generics,
     * but is only safe to do after all constructors have completed.
     */
    protected void completeInit() {
        this.detailaware.add(MyThings);
    }

    protected int findMin(float[] a) {
        int minindex = 0;
        float min = Float.POSITIVE_INFINITY;

        for (int i = 0; i < a.length; i++) {
            if (a[i] < min) {
                min = a[i];
                minindex = i;
            }
        }

        return minindex;

    }

    /**
     * R_DrawMaskedColumnSinglePost. Used to handle some special cases where
     * cached columns get used as "masked" middle textures. Will be treated as a
     * single-run post of capped length.
     */

    /*
     * protected final void DrawCompositeColumnPost(byte[] column) { int
     * topscreen; int bottomscreen; int basetexturemid; // fixed_t int
     * topdelta=0; // Fixed value int length; basetexturemid = dc_texturemid; //
     * That's true for the whole column. dc_source = column; // for each post...
     * while (topdelta==0) { // calculate unclipped screen coordinates // for
     * post topscreen = sprtopscreen + spryscale * 0; length = column.length;
     * bottomscreen = topscreen + spryscale * length; dc_yl = (topscreen +
     * FRACUNIT - 1) >> FRACBITS; dc_yh = (bottomscreen - 1) >> FRACBITS; if
     * (dc_yh >= mfloorclip[p_mfloorclip + dc_x]) dc_yh =
     * mfloorclip[p_mfloorclip + dc_x] - 1; if (dc_yl <=
     * mceilingclip[p_mceilingclip + dc_x]) dc_yl = mceilingclip[p_mceilingclip
     * + dc_x] + 1; // killough 3/2/98, 3/27/98: Failsafe against
     * overflow/crash: if (dc_yl <= dc_yh && dc_yh < viewheight) { // Set
     * pointer inside column to current post's data // Rremember, it goes
     * {postlen}{postdelta}{pad}[data]{pad} dc_source_ofs = 0; // pointer + 3;
     * dc_texturemid = basetexturemid - (topdelta << FRACBITS); // Drawn by
     * either R_DrawColumn // or (SHADOW) R_DrawFuzzColumn. dc_texheight=0; //
     * Killough try { maskedcolfunc.invoke(); } catch (Exception e){
     * System.err.printf("Error rendering %d %d %d\n", dc_yl,dc_yh,dc_yh-dc_yl);
     * } } topdelta--; } dc_texturemid = basetexturemid; }
     */
    protected abstract void InitColormaps() throws IOException;

    // Only used by Fuzz effect
    protected BlurryTable BLURRY_MAP;

    /**
     * R_InitData Locates all the lumps that will be used by all views Must be
     * called after W_Init.
     */
    public void InitData() {
        try {
            System.out.print("\nInit Texture and Flat Manager");
            TexMan = this.DOOM.textureManager;
            System.out.print("\nInitTextures");
            TexMan.InitTextures();
            System.out.print("\nInitFlats");
            TexMan.InitFlats();
            System.out.print("\nInitSprites");
            DOOM.spriteManager.InitSpriteLumps();
            MyThings.cacheSpriteManager(DOOM.spriteManager);
            VIS.cacheSpriteManager(DOOM.spriteManager);
            System.out.print("\nInitColormaps\t\t");
            InitColormaps();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected int spritememory;

    /**
     * To be called right after PrecacheLevel from SetupLevel in LevelLoader.
     * It's an ugly hack, in that it must communicate with the "Game map" class
     * and determine what kinds of monsters are actually in the level and
     * whether it should load their graphics or not. Whenever we implement it,
     * it's going to be ugly and not neatly separated anyway.
     *
     * @return
     */
    @Override
    public void PreCacheThinkers() {

        boolean[] spritepresent;
        thinker_t th;
        spriteframe_t sf;
        int lump;

        final spritedef_t[] sprites = DOOM.spriteManager.getSprites();
        final int numsprites = DOOM.spriteManager.getNumSprites();

        spritepresent = new boolean[numsprites];

        for (th = DOOM.actions.getThinkerCap().next; th != DOOM.actions.getThinkerCap(); th = th.next) {
            if (th.thinkerFunction == P_MobjThinker) {
                spritepresent[((mobj_t) th).mobj_sprite.ordinal()] = true;
            }
        }

        spritememory = 0;
        for (int i = 0; i < numsprites; i++) {
            if (!spritepresent[i]) {
                continue;
            }

            for (int j = 0; j < sprites[i].numframes; j++) {
                sf = sprites[i].spriteframes[j];
                for (int k = 0; k < 8; k++) {
                    lump = DOOM.spriteManager.getFirstSpriteLump() + sf.lump[k];
                    spritememory += DOOM.wadLoader.GetLumpInfo(lump).size;
                    DOOM.wadLoader.CacheLumpNum(lump, PU_CACHE, patch_t.class);
                }
            }
        }
    }

    /**
     * R_InitTranslationTables Creates the translation tables to map the green
     * color ramp to gray, brown, red. Assumes a given structure of the PLAYPAL.
     * Could be read from a lump instead.
     */
    protected void InitTranslationTables() {
        int i;

        final int TR_COLORS = 28;

        // translationtables = Z_Malloc (256*3+255, PU_STATIC, 0);
        // translationtables = (byte *)(( (int)translationtables + 255 )& ~255);
        byte[][] translationtables
            = colormaps.translationtables = new byte[TR_COLORS][256];

        // translate just the 16 green colors
        for (i = 0; i < 256; i++) {
            translationtables[0][i] = (byte) i;

            if (i >= 0x70 && i <= 0x7f) {
                // Remap green range to other ranges.
                translationtables[1][i] = (byte) (0x60 + (i & 0xf)); // gray
                translationtables[2][i] = (byte) (0x40 + (i & 0xf)); // brown
                translationtables[3][i] = (byte) (0x20 + (i & 0xf)); // red
                translationtables[4][i] = (byte) (0x10 + (i & 0xf)); // pink
                translationtables[5][i] = (byte) (0x30 + (i & 0xf)); // skin
                translationtables[6][i] = (byte) (0x50 + (i & 0xf)); // metal
                translationtables[7][i] = (byte) (0x80 + (i & 0xf)); // copper
                translationtables[8][i] = (byte) (0xB0 + (i & 0xf)); // b.red
                translationtables[9][i] = (byte) (0xC0 + (i & 0xf)); // electric
                // blue
                translationtables[10][i] = (byte) (0xD0 + (i & 0xf)); // guantanamo
                // "Halfhue" colors for which there are only 8 distinct hues
                translationtables[11][i] = (byte) (0x90 + (i & 0xf) / 2); // brown2
                translationtables[12][i] = (byte) (0x98 + (i & 0xf) / 2); // gray2
                translationtables[13][i] = (byte) (0xA0 + (i & 0xf) / 2); // piss
                translationtables[14][i] = (byte) (0xA8 + (i & 0xf) / 2); // gay
                translationtables[15][i] = (byte) (0xE0 + (i & 0xf) / 2); // yellow
                translationtables[16][i] = (byte) (0xE8 + (i & 0xf) / 2); // turd
                translationtables[17][i] = (byte) (0xF0 + (i & 0xf) / 2); // compblue
                translationtables[18][i] = (byte) (0xF8 + (i & 0xf) / 2); // whore
                translationtables[19][i] = (byte) (0x05 + (i & 0xf) / 2); // nigga
                // "Pimped up" colors, using mixed hues.
                translationtables[20][i] = (byte) (0x90 + (i & 0xf)); // soldier
                translationtables[21][i] = (byte) (0xA0 + (i & 0xf)); // drag
                // queen
                translationtables[22][i] = (byte) (0xE0 + (i & 0xf)); // shit &
                // piss
                translationtables[23][i] = (byte) (0xF0 + (i & 0xf)); // raver
                translationtables[24][i] = (byte) (0x70 + (0xf - i & 0xf)); // inv.marine
                translationtables[25][i] = (byte) (0xF0 + (0xf - i & 0xf)); // inv.raver
                translationtables[26][i] = (byte) (0xE0 + (0xf - i & 0xf)); // piss
                // &
                // shit
                translationtables[27][i] = (byte) (0xA0 + (i & 0xf)); // shitty
                // gay
            } else {
                for (int j = 1; j < TR_COLORS; j++) {
                    // Keep all other colors as is.
                    translationtables[j][i] = (byte) i;
                }
            }
        }
    }

    // ///////////////// Generic rendering methods /////////////////////
    public IMaskedDrawer<T, V> getThings() {
        return this.MyThings;
    }

    /**
     * e6y: this is a precalculated value for more precise flats drawing (see
     * R_MapPlane) "Borrowed" from PrBoom+
     */
    protected float viewfocratio;

    protected int projectiony;

    // Some more isolation methods....
    @Override
    public int getValidCount() {
        return validcount;
    }

    @Override
    public void increaseValidCount(int amount) {
        validcount += amount;
    }

    @Override
    public boolean getSetSizeNeeded() {
        return setsizeneeded;
    }

    @Override
    public TextureManager<T> getTextureManager() {
        return TexMan;
    }

    @Override
    public PlaneDrawer<T, V> getPlaneDrawer() {
        return this.MyPlanes;
    }

    @Override
    public ViewVars getView() {
        return this.view;
    }

    @Override
    public SpanVars<T, V> getDSVars() {
        return this.dsvars;
    }

    @Override
    public LightsAndColors<V> getColorMap() {
        return this.colormaps;
    }

    @Override
    public IDoomSystem getDoomSystem() {
        return this.DOOM.doomSystem;
    }

    @Override
    public Visplanes getVPVars() {
        return this.vp_vars;
    }

    @Override
    public SegVars getSegVars() {
        return this.seg_vars;
    }

    @Override
    public IWadLoader getWadLoader() {
        return this.DOOM.wadLoader;
    }

    @Override
    public ISpriteManager getSpriteManager() {
        return this.DOOM.spriteManager;
    }

    @Override
    public BSPVars getBSPVars() {
        return this.MyBSP;
    }

    @Override
    public IVisSpriteManagement<V> getVisSpriteManager() {
        return this.VIS;
    }

    /**
     * Initializes the various drawing functions. They are all "pegged" to the
     * same dcvars/dsvars object. Any initializations of e.g. parallel renderers
     * and their supporting subsystems should occur here.
     */
    protected void R_InitDrawingFunctions() {
        this.setHiColFuns();
        this.setLowColFuns();
    }

    // //////////////////////////// LIMIT RESETTING //////////////////
    @Override
    public void resetLimits() {
        // Call it only at the beginning of new levels.
        VIS.resetLimits();
        MySegs.resetLimits();
    }

    /**
     * R_RenderView As you can guess, this renders the player view of a
     * particular player object. In practice, it could render the view of any
     * mobj too, provided you adapt the SetupFrame method (where the viewing
     * variables are set). This is the "vanilla" implementation which just works
     * for most cases.
     */
    @Override
    public void RenderPlayerView(player_t player) {

        // Viewing variables are set according to the player's mobj. Interesting
        // hacks like
        // free cameras or monster views can be done.
        SetupFrame(player);

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

        // Check for new console commands.
        DOOM.gameNetworking.NetUpdate();

        // FIXME: "Warped floor" fixed, now to fix same-height visplane
        // bleeding.
        MyPlanes.DrawPlanes();

        // Check for new console commands.
        DOOM.gameNetworking.NetUpdate();

        MyThings.DrawMasked();

        colfunc.main = colfunc.base;

        // Check for new console commands.
        DOOM.gameNetworking.NetUpdate();
    }
}

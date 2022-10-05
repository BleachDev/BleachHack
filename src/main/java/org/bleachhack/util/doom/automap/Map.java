package org.bleachhack.util.doom.automap;

// Emacs style mode select -*- C++ -*-
// -----------------------------------------------------------------------------
//
// $Id: Map.java,v 1.37 2012/09/24 22:36:28 velktron Exp $
//
// Copyright (C) 1993-1996 by id Software, Inc.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
//
//
// $Log: Map.java,v $
// Revision 1.37  2012/09/24 22:36:28  velktron
// Map get color
//
// Revision 1.36  2012/09/24 17:16:23  velktron
// Massive merge between HiColor and HEAD. There's no difference from now on, and development continues on HEAD.
//
// Revision 1.34.2.4  2012/09/24 16:58:06  velktron
// TrueColor, Generics.
//
// Revision 1.34.2.3  2012/09/20 14:06:43  velktron
// Generic automap
//
// Revision 1.34.2.2 2011/11/27 18:19:19 velktron
// Configurable colors, more parametrizable.
//
// Revision 1.34.2.1 2011/11/14 00:27:11 velktron
// A barely functional HiColor branch. Most stuff broken. DO NOT USE
//
// Revision 1.34 2011/11/03 18:11:14 velktron
// Fixed long-standing issue with 0-rot vector being reduced to pixels. Fixed
// broken map panning functionality after keymap change.
//
// Revision 1.33 2011/11/01 23:48:43 velktron
// Using FillRect
//
// Revision 1.32 2011/11/01 19:03:10 velktron
// Using screen number constants
//
// Revision 1.31 2011/10/23 18:10:32 velktron
// Generic compliance for DoomVideoInterface
//
// Revision 1.30 2011/10/07 16:08:23 velktron
// Now using g.Keys and line_t
//
// Revision 1.29 2011/09/29 13:25:09 velktron
// Eliminated "intermediate" AbstractAutoMap. Map implements IAutoMap directly.
//
// Revision 1.28 2011/07/28 16:35:03 velktron
// Well, we don't need to know that anymore.
//
// Revision 1.27 2011/06/18 23:16:34 velktron
// Added extreme scale safeguarding (e.g. for Europe.wad).
//
// Revision 1.26 2011/05/30 15:45:44 velktron
// AbstractAutoMap and IAutoMap
//
// Revision 1.25 2011/05/24 11:31:47 velktron
// Adapted to IDoomStatusBar
//
// Revision 1.24 2011/05/23 16:57:39 velktron
// Migrated to VideoScaleInfo.
//
// Revision 1.23 2011/05/17 16:50:02 velktron
// Switched to DoomStatus
//
// Revision 1.22 2011/05/10 10:39:18 velktron
// Semi-playable Techdemo v1.3 milestone
//
// Revision 1.21 2010/12/14 17:55:59 velktron
// Fixed weapon bobbing, added translucent column drawing, separated rendering
// commons.
//
// Revision 1.20 2010/12/12 19:06:18 velktron
// Tech Demo v1.1 release.
//
// Revision 1.19 2010/11/17 23:55:06 velktron
// Kind of playable/controllable.
//
// Revision 1.18 2010/11/12 13:37:25 velktron
// Rationalized the LUT system - now it's 100% procedurally generated.
//
// Revision 1.17 2010/10/01 16:47:51 velktron
// Fixed tab interception.
//
// Revision 1.16 2010/09/27 15:07:44 velktron
// meh
//
// Revision 1.15 2010/09/27 02:27:29 velktron
// BEASTLY update
//
// Revision 1.14 2010/09/23 07:31:11 velktron
// fuck
//
// Revision 1.13 2010/09/13 15:39:17 velktron
// Moving towards an unified gameplay approach...
//
// Revision 1.12 2010/09/08 21:09:01 velktron
// Better display "driver".
//
// Revision 1.11 2010/09/08 15:22:18 velktron
// x,y coords in some structs as value semantics. Possible speed increase?
//
// Revision 1.10 2010/09/06 16:02:59 velktron
// Implementation of palettes.
//
// Revision 1.9 2010/09/02 15:56:54 velktron
// Bulk of unified renderer copyediting done.
//
// Some changes like e.g. global separate limits class and instance methods for
// seg_t and node_t introduced.
//
// Revision 1.8 2010/09/01 15:53:42 velktron
// Graphics data loader implemented....still need to figure out how column
// caching works, though.
//
// Revision 1.7 2010/08/27 23:46:57 velktron
// Introduced Buffered renderer, which makes tapping directly into byte[] screen
// buffers mapped to BufferedImages possible.
//
// Revision 1.6 2010/08/26 16:43:42 velktron
// Automap functional, biatch.
//
// Revision 1.5 2010/08/25 00:50:59 velktron
// Some more work...
//
// Revision 1.4 2010/08/22 18:04:21 velktron
// Automap
//
// Revision 1.3 2010/08/19 23:14:49 velktron
// Automap
//
// Revision 1.2 2010/08/10 16:41:57 velktron
// Threw some work into map loading.
//
// Revision 1.1 2010/07/20 15:52:56 velktron
// LOTS of changes, Automap almost complete. Use of fixed_t inside methods
// severely limited.
//
// Revision 1.1 2010/06/30 08:58:51 velktron
// Let's see if this stuff will finally commit....
//
//
// Most stuff is still being worked on. For a good place to start and get an
// idea of what is being done, I suggest checking out the "testers" package.
//
// Revision 1.1 2010/06/29 11:07:34 velktron
// Release often, release early they say...
//
// Commiting ALL stuff done so far. A lot of stuff is still broken/incomplete,
// and there's still mixed C code in there. I suggest you load everything up in
// Eclpise and see what gives from there.
//
// A good place to start is the testers/ directory, where you can get an idea of
// how a few of the implemented stuff works.
//
//
// DESCRIPTION: the automap code
//
// -----------------------------------------------------------------------------

import static org.bleachhack.util.doom.data.Defines.*;
import static org.bleachhack.util.doom.data.Limits.*;
import static org.bleachhack.util.doom.data.Tables.*;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.SourceCode.AM_Map;
import static org.bleachhack.util.doom.doom.SourceCode.AM_Map.AM_Responder;
import static org.bleachhack.util.doom.doom.englsh.*;
import org.bleachhack.util.doom.doom.event_t;
import org.bleachhack.util.doom.doom.evtype_t;
import org.bleachhack.util.doom.doom.player_t;
import org.bleachhack.util.doom.g.Signals.ScanCode;
import static org.bleachhack.util.doom.g.Signals.ScanCode.*;
import java.awt.Rectangle;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import org.bleachhack.util.doom.m.cheatseq_t;
import static org.bleachhack.util.doom.m.fixed_t.*;
import org.bleachhack.util.doom.p.mobj_t;
import static org.bleachhack.util.doom.rr.line_t.*;
import org.bleachhack.util.doom.rr.patch_t;
import static org.bleachhack.util.doom.utils.GenericCopy.*;
import static org.bleachhack.util.doom.v.DoomGraphicSystem.*;
import org.bleachhack.util.doom.v.graphics.Plotter;
import static org.bleachhack.util.doom.v.renderers.DoomScreen.*;

public class Map<T, V> implements IAutoMap<T, V> {

    /////////////////// Status objects ///////////////////
    final DoomMain<T, V> DOOM;

    /**
     * Configurable colors - now an enum
     *  - Good Sign 2017/04/05
     * 
     * Use colormap-specific colors to support extended modes.
     * Moved hardcoding in here. Potentially configurable.
     */
    enum Color {
        CLOSE_TO_BLACK(1, (byte) 246),
        REDS(16, (byte) 176 /*(256 - 5 * 16)*/),
        BLUES(8, (byte) 200 /*(256 - 4 * 16 + 8)*/),
        GREENS(16, (byte) 112 /*(7 * 16)*/),
        GRAYS(16, (byte) 96 /*(6 * 16)*/),
        BROWNS(16, (byte) 64 /*(4 * 16)*/),
        YELLOWS(8, (byte) 160 /*(256 - 32)*/),
        BLACK(1, (byte) 0),
        WHITE(1, (byte) 4),
        GRAYS_DARKER_25(13, (byte)(GRAYS.value + 4)),
        DARK_GREYS(8, (byte)(GRAYS.value + GRAYS.range / 2)),
        DARK_REDS(8, (byte)(REDS.value + REDS.range / 2));

        final static int NUM_LITES = 8;
        final static int[] LITE_LEVELS_FULL_RANGE = { 0, 4, 7, 10, 12, 14, 15, 15 };
        final static int[] LITE_LEVELS_HALF_RANGE = { 0, 2, 3, 5, 6, 6, 7, 7 };
        final byte[] liteBlock;
        final byte value;
        final int range;

        Color(int range, byte value) {
            this.range = range;
            this.value = value;
            if (range >= NUM_LITES) {
                this.liteBlock = new byte[NUM_LITES];
            } else {
                this.liteBlock = null;
            }
        }
        
        static {
            for (Color c: values()) {
                switch(c.range) {
                    case 16:
                        for (int i = 0; i < NUM_LITES; ++i) {
                            c.liteBlock[i] = (byte) (c.value + LITE_LEVELS_FULL_RANGE[i]);
                        }
                        break;
                    case 8:
                        for (int i = 0; i < LITE_LEVELS_HALF_RANGE.length; ++i) {
                            c.liteBlock[i] = (byte) (c.value + LITE_LEVELS_HALF_RANGE[i]);
                        }
                }
            }
        }
    }
    // For use if I do walls with outsides/insides

    // Automap colors
    final static Color 
        BACKGROUND = Color.BLACK,
        YOURCOLORS = Color.WHITE,
        WALLCOLORS = Color.REDS,
        TELECOLORS = Color.DARK_REDS,
        TSWALLCOLORS = Color.GRAYS,
        FDWALLCOLORS = Color.BROWNS,
        CDWALLCOLORS = Color.YELLOWS,
        THINGCOLORS = Color.GREENS,
        SECRETWALLCOLORS = Color.REDS,
        GRIDCOLORS = Color.DARK_GREYS,
        MAPPOWERUPSHOWNCOLORS = Color.GRAYS,
        CROSSHAIRCOLORS = Color.GRAYS;

    final static EnumSet<Color> GENERATE_LITE_LEVELS_FOR = EnumSet.of(
        TELECOLORS,
        WALLCOLORS,
        FDWALLCOLORS,
        CDWALLCOLORS,
        TSWALLCOLORS,
        SECRETWALLCOLORS,
        MAPPOWERUPSHOWNCOLORS,
        THINGCOLORS
    );
    
    final static Color THEIR_COLORS[] = {
        Color.GREENS,
        Color.GRAYS,
        Color.BROWNS,
        Color.REDS
    };
    
    // drawing stuff
    public static final ScanCode AM_PANDOWNKEY = SC_DOWN;
    public static final ScanCode AM_PANUPKEY = SC_UP;
    public static final ScanCode AM_PANRIGHTKEY = SC_RIGHT;
    public static final ScanCode AM_PANLEFTKEY = SC_LEFT;
    public static final ScanCode AM_ZOOMINKEY = SC_EQUALS;
    public static final ScanCode AM_ZOOMOUTKEY = SC_MINUS;
    public static final ScanCode AM_STARTKEY = SC_TAB;
    public static final ScanCode AM_ENDKEY = SC_TAB;
    public static final ScanCode AM_GOBIGKEY = SC_0;
    public static final ScanCode AM_FOLLOWKEY = SC_F;
    public static final ScanCode AM_GRIDKEY = SC_G;
    public static final ScanCode AM_MARKKEY = SC_M;
    public static final ScanCode AM_CLEARMARKKEY = SC_C;
    public static final int AM_NUMMARKPOINTS = 10;

    // (fixed_t) scale on entry
    public static final int INITSCALEMTOF = (int) (.2 * FRACUNIT);

    // how much the automap moves window per tic in frame-buffer coordinates
    // moves 140 pixels in 1 second
    public static final int F_PANINC = 4;

    // how much zoom-in per tic
    // goes to 2x in 1 second
    public static final int M_ZOOMIN = ((int) (1.02 * FRACUNIT));

    // how much zoom-out per tic
    // pulls out to 0.5x in 1 second
    public static final int M_ZOOMOUT = ((int) (FRACUNIT / 1.02));
    
    final EnumMap<Color, V> fixedColorSources = new EnumMap<>(Color.class);
    final EnumMap<Color, V> litedColorSources = new EnumMap<>(Color.class);

    public Map(final DoomMain<T, V> DOOM) {
        // Some initializing...
        this.DOOM = DOOM;
        this.markpoints = malloc(mpoint_t::new, mpoint_t[]::new, AM_NUMMARKPOINTS);

        f_oldloc = new mpoint_t();
        m_paninc = new mpoint_t();

        this.plotter = DOOM.graphicSystem.createPlotter(FG);
        this.plr = DOOM.players[DOOM.displayplayer];
        Repalette();
        // Pre-scale stuff.
        finit_width = DOOM.vs.getScreenWidth();
        finit_height = DOOM.vs.getScreenHeight() - 32 * DOOM.vs.getSafeScaling();
    }
    
    @Override
    public final void Repalette() {
        GENERATE_LITE_LEVELS_FOR.stream()
            .forEach((c) -> {
                if (c.liteBlock != null) {
                    litedColorSources.put(c, DOOM.graphicSystem.convertPalettedBlock(c.liteBlock));
                }
            });
        
        Arrays.stream(Color.values())
            .forEach((c) -> {
                V converted = DOOM.graphicSystem.convertPalettedBlock(c.value);
                @SuppressWarnings("unchecked")
                V extended = (V) Array.newInstance(converted.getClass().getComponentType(), Color.NUM_LITES);
                memset(extended, 0, Color.NUM_LITES, converted, 0, 1);
                fixedColorSources.put(c, extended);
            });
    }

    /** translates between frame-buffer and map distances */
    private int FTOM(int x) {
        return FixedMul(((x) << 16), scale_ftom);
    }

    /** translates between frame-buffer and map distances */
    private int MTOF(int x) {
        return FixedMul((x), scale_mtof) >> 16;
    }

    /** translates between frame-buffer and map coordinates */
    private int CXMTOF(int x) {
        return (f_x + MTOF((x) - m_x));
    }

    /** translates between frame-buffer and map coordinates */
    private int CYMTOF(int y) {
        return (f_y + (f_h - MTOF((y) - m_y)));
    }
    
    // the following is crap
    public static final short LINE_NEVERSEE = ML_DONTDRAW;

    // This seems to be the minimum viable scale before things start breaking
    // up.
    private static final int MINIMUM_SCALE = (int) (0.7 * FRACUNIT);

    // This seems to be the limit for some maps like europe.wad
    private static final int MINIMUM_VIABLE_SCALE = FRACUNIT >> 5;

    //
    // The vector graphics for the automap.
    /**
     * A line drawing of the player pointing right, starting from the middle.
     */
    protected mline_t[] player_arrow;

    protected int NUMPLYRLINES;

    protected mline_t[] cheat_player_arrow;

    protected int NUMCHEATPLYRLINES;

    protected mline_t[] triangle_guy;

    protected int NUMTRIANGLEGUYLINES;

    protected mline_t[] thintriangle_guy;

    protected int NUMTHINTRIANGLEGUYLINES;

    protected void initVectorGraphics() {

        int R = ((8 * PLAYERRADIUS) / 7);
        player_arrow =
            new mline_t[] {
                    new mline_t(-R + R / 8, 0, R, 0), // -----
                    new mline_t(R, 0, R - R / 2, R / 4), // ----
                    new mline_t(R, 0, R - R / 2, -R / 4),
                    new mline_t(-R + R / 8, 0, -R - R / 8, R / 4), // >---
                    new mline_t(-R + R / 8, 0, -R - R / 8, -R / 4),
                    new mline_t(-R + 3 * R / 8, 0, -R + R / 8, R / 4), // >>--
                    new mline_t(-R + 3 * R / 8, 0, -R + R / 8, -R / 4) };

        NUMPLYRLINES = player_arrow.length;

        cheat_player_arrow =
            new mline_t[] {
                    new mline_t(-R + R / 8, 0, R, 0), // -----
                    new mline_t(R, 0, R - R / 2, R / 6), // ----
                    new mline_t(R, 0, R - R / 2, -R / 6),
                    new mline_t(-R + R / 8, 0, -R - R / 8, R / 6), // >----
                    new mline_t(-R + R / 8, 0, -R - R / 8, -R / 6),
                    new mline_t(-R + 3 * R / 8, 0, -R + R / 8, R / 6), // >>----
                    new mline_t(-R + 3 * R / 8, 0, -R + R / 8, -R / 6),
                    new mline_t(-R / 2, 0, -R / 2, -R / 6), // >>-d--
                    new mline_t(-R / 2, -R / 6, -R / 2 + R / 6, -R / 6),
                    new mline_t(-R / 2 + R / 6, -R / 6, -R / 2 + R / 6, R / 4),
                    new mline_t(-R / 6, 0, -R / 6, -R / 6), // >>-dd-
                    new mline_t(-R / 6, -R / 6, 0, -R / 6),
                    new mline_t(0, -R / 6, 0, R / 4),
                    new mline_t(R / 6, R / 4, R / 6, -R / 7), // >>-ddt
                    new mline_t(R / 6, -R / 7, R / 6 + R / 32, -R / 7 - R / 32),
                    new mline_t(R / 6 + R / 32, -R / 7 - R / 32,
                            R / 6 + R / 10, -R / 7) };

        NUMCHEATPLYRLINES = cheat_player_arrow.length;

        R = (FRACUNIT);
        triangle_guy =
            new mline_t[] { new mline_t(-.867 * R, -.5 * R, .867 * R, -.5 * R),
                    new mline_t(.867 * R, -.5 * R, 0, R),
                    new mline_t(0, R, -.867 * R, -.5 * R) };

        NUMTRIANGLEGUYLINES = triangle_guy.length;

        thintriangle_guy =
            new mline_t[] { new mline_t(-.5 * R, -.7 * R, R, 0),
                    new mline_t(R, 0, -.5 * R, .7 * R),
                    new mline_t(-.5 * R, .7 * R, -.5 * R, -.7 * R) };

        NUMTHINTRIANGLEGUYLINES = thintriangle_guy.length;
    }

    /** Planned overlay mode */
    protected int overlay = 0;

    protected int cheating = 0;

    protected boolean grid = false;

    protected int leveljuststarted = 1; // kluge until AM_LevelInit() is called

    protected int finit_width;

    protected int finit_height;

    // location of window on screen
    protected int f_x;

    protected int f_y;

    // size of window on screen
    protected int f_w;

    protected int f_h;
    
    protected Rectangle f_rect;

    /** used for funky strobing effect */
    protected int lightlev;

    /** pseudo-frame buffer */
    //protected V fb;
    
    /**
     * I've made this awesome change to draw map lines on the renderer
     *  - Good Sign 2017/04/05
     */
    protected final Plotter<V> plotter;

    protected int amclock;

    /** (fixed_t) how far the window pans each tic (map coords) */
    protected mpoint_t m_paninc;

    /** (fixed_t) how far the window zooms in each tic (map coords) */
    protected int mtof_zoommul;

    /** (fixed_t) how far the window zooms in each tic (fb coords) */
    protected int ftom_zoommul;

    /** (fixed_t) LL x,y where the window is on the map (map coords) */
    protected int m_x, m_y;

    /** (fixed_t) UR x,y where the window is on the map (map coords) */
    protected int m_x2, m_y2;

    /** (fixed_t) width/height of window on map (map coords) */
    protected int m_w, m_h;

    /** (fixed_t) based on level size */
    protected int min_x, min_y, max_x, max_y;

    /** (fixed_t) max_x-min_x */
    protected int max_w; //

    /** (fixed_t) max_y-min_y */
    protected int max_h;

    /** (fixed_t) based on player size */
    protected int min_w, min_h;

    /** (fixed_t) used to tell when to stop zooming out */
    protected int min_scale_mtof;

    /** (fixed_t) used to tell when to stop zooming in */
    protected int max_scale_mtof;

    /** (fixed_t) old stuff for recovery later */
    protected int old_m_w, old_m_h, old_m_x, old_m_y;

    /** old location used by the Follower routine */
    protected mpoint_t f_oldloc;

    /** (fixed_t) used by MTOF to scale from map-to-frame-buffer coords */
    protected int scale_mtof = INITSCALEMTOF;

    /** used by FTOM to scale from frame-buffer-to-map coords (=1/scale_mtof) */
    protected int scale_ftom;

    /** the player represented by an arrow */
    protected player_t plr;

    /** numbers used for marking by the automap */
    private final patch_t[] marknums = new patch_t[10];

    /** where the points are */
    private final mpoint_t[] markpoints;

    /** next point to be assigned */
    private int markpointnum = 0;

    /** specifies whether to follow the player around */
    protected boolean followplayer = true;

    protected char[] cheat_amap_seq = { 0xb2, 0x26, 0x26, 0x2e, 0xff }; // iddt

    protected cheatseq_t cheat_amap = new cheatseq_t(cheat_amap_seq, 0);

    // MAES: STROBE cheat. It's not even cheating, strictly speaking.

    private final char cheat_strobe_seq[] = { 0x6e, 0xa6, 0xea, 0x2e, 0x6a, 0xf6,
            0x62, 0xa6, 0xff // vestrobe
        };

    private final cheatseq_t cheat_strobe = new cheatseq_t(cheat_strobe_seq, 0);

    private boolean stopped = true;

    // extern boolean viewactive;
    // extern byte screens[][DOOM.vs.getScreenWidth()*DOOM.vs.getScreenHeight()];

    /**
     * Calculates the slope and slope according to the x-axis of a line segment
     * in map coordinates (with the upright y-axis n' all) so that it can be
     * used with the brain-dead drawing stuff.
     * 
     * @param ml
     * @param is
     */

    public final void getIslope(mline_t ml, islope_t is) {
        int dx, dy;

        dy = ml.ay - ml.by;
        dx = ml.bx - ml.ax;
        if (dy == 0)
            is.islp = (dx < 0 ? -MAXINT : MAXINT);
        else
            is.islp = FixedDiv(dx, dy);
        if (dx == 0)
            is.slp = (dy < 0 ? -MAXINT : MAXINT);
        else
            is.slp = FixedDiv(dy, dx);

    }

    //
    //
    //
    public final void activateNewScale() {
        m_x += m_w / 2;
        m_y += m_h / 2;
        m_w = FTOM(f_w);
        m_h = FTOM(f_h);
        m_x -= m_w / 2;
        m_y -= m_h / 2;
        m_x2 = m_x + m_w;
        m_y2 = m_y + m_h;
        
        plotter.setThickness(
            Math.min(MTOF(FRACUNIT), DOOM.graphicSystem.getScalingX()),
            Math.min(MTOF(FRACUNIT), DOOM.graphicSystem.getScalingY())
        );
    }

    //
    //
    //
    public final void saveScaleAndLoc() {
        old_m_x = m_x;
        old_m_y = m_y;
        old_m_w = m_w;
        old_m_h = m_h;
    }

    private void restoreScaleAndLoc() {

        m_w = old_m_w;
        m_h = old_m_h;
        if (!followplayer) {
            m_x = old_m_x;
            m_y = old_m_y;
        } else {
            m_x = plr.mo.x - m_w / 2;
            m_y = plr.mo.y - m_h / 2;
        }
        m_x2 = m_x + m_w;
        m_y2 = m_y + m_h;

        // Change the scaling multipliers
        scale_mtof = FixedDiv(f_w << FRACBITS, m_w);
        scale_ftom = FixedDiv(FRACUNIT, scale_mtof);

        plotter.setThickness(
            Math.min(MTOF(FRACUNIT), Color.NUM_LITES),
            Math.min(MTOF(FRACUNIT), Color.NUM_LITES)
        );
    }

    /**
     * adds a marker at the current location
     */

    public final void addMark() {
        markpoints[markpointnum].x = m_x + m_w / 2;
        markpoints[markpointnum].y = m_y + m_h / 2;
        markpointnum = (markpointnum + 1) % AM_NUMMARKPOINTS;

    }

    /**
     * Determines bounding box of all vertices, sets global variables
     * controlling zoom range.
     */

    public final void findMinMaxBoundaries() {
        int a; // fixed_t
        int b;

        min_x = min_y = MAXINT;
        max_x = max_y = -MAXINT;

        for (int i = 0; i < DOOM.levelLoader.numvertexes; i++) {
            if (DOOM.levelLoader.vertexes[i].x < min_x)
                min_x = DOOM.levelLoader.vertexes[i].x;
            else if (DOOM.levelLoader.vertexes[i].x > max_x)
                max_x = DOOM.levelLoader.vertexes[i].x;

            if (DOOM.levelLoader.vertexes[i].y < min_y)
                min_y = DOOM.levelLoader.vertexes[i].y;
            else if (DOOM.levelLoader.vertexes[i].y > max_y)
                max_y = DOOM.levelLoader.vertexes[i].y;
        }

        max_w = max_x - min_x;
        max_h = max_y - min_y;

        min_w = 2 * PLAYERRADIUS; // const? never changed?
        min_h = 2 * PLAYERRADIUS;

        a = FixedDiv(f_w << FRACBITS, max_w);
        b = FixedDiv(f_h << FRACBITS, max_h);

        min_scale_mtof = a < b ? a : b;
        if (min_scale_mtof < 0) {
            // MAES: safeguard against negative scaling e.g. in Europe.wad
            // This seems to be the limit.
            min_scale_mtof = MINIMUM_VIABLE_SCALE;
        }
        max_scale_mtof = FixedDiv(f_h << FRACBITS, 2 * PLAYERRADIUS);

    }

    public final void changeWindowLoc() {
        if (m_paninc.x != 0 || m_paninc.y != 0) {
            followplayer = false;
            f_oldloc.x = MAXINT;
        }

        m_x += m_paninc.x;
        m_y += m_paninc.y;

        if (m_x + m_w / 2 > max_x)
            m_x = max_x - m_w / 2;
        else if (m_x + m_w / 2 < min_x)
            m_x = min_x - m_w / 2;

        if (m_y + m_h / 2 > max_y)
            m_y = max_y - m_h / 2;
        else if (m_y + m_h / 2 < min_y)
            m_y = min_y - m_h / 2;

        m_x2 = m_x + m_w;
        m_y2 = m_y + m_h;
    }

    public final void initVariables() {
        int pnum;

        DOOM.automapactive = true;
        f_oldloc.x = MAXINT;
        amclock = 0;
        lightlev = 0;

        m_paninc.x = m_paninc.y = 0;
        ftom_zoommul = FRACUNIT;
        mtof_zoommul = FRACUNIT;

        m_w = FTOM(f_w);
        m_h = FTOM(f_h);

        // find player to center on initially
        if (!DOOM.playeringame[pnum = DOOM.consoleplayer])
            for (pnum = 0; pnum < MAXPLAYERS; pnum++) {
                System.out.println(pnum);
                if (DOOM.playeringame[pnum])
                    break;
            }
        plr = DOOM.players[pnum];
        m_x = plr.mo.x - m_w / 2;
        m_y = plr.mo.y - m_h / 2;
        this.changeWindowLoc();

        // for saving & restoring
        old_m_x = m_x;
        old_m_y = m_y;
        old_m_w = m_w;
        old_m_h = m_h;

        // inform the status bar of the change
        DOOM.statusBar.NotifyAMEnter();
    }

    //
    //
    //
    public final void loadPics() {
        int i;
        String namebuf;

        for (i = 0; i < 10; i++) {
            namebuf = ("AMMNUM" + i);
            marknums[i] = DOOM.wadLoader.CachePatchName(namebuf);
        }

    }

    public final void unloadPics() {
        int i;

        for (i = 0; i < 10; i++) {
            DOOM.wadLoader.UnlockLumpNum(marknums[i]);
        }
    }

    public final void clearMarks() {
        int i;

        for (i = 0; i < AM_NUMMARKPOINTS; i++)
            markpoints[i].x = -1; // means empty
        markpointnum = 0;
    }

    /**
     * should be called at the start of every level right now, i figure it out
     * myself
     */
    public final void LevelInit() {
        leveljuststarted = 0;

        f_x = f_y = 0;
        f_w = finit_width;
        f_h = finit_height;
        f_rect = new Rectangle(0, 0, f_w, f_h);

        // scanline=new byte[f_h*f_w];

        this.clearMarks();

        this.findMinMaxBoundaries();
        scale_mtof = FixedDiv(min_scale_mtof, MINIMUM_SCALE);
        if (scale_mtof > max_scale_mtof)
            scale_mtof = min_scale_mtof;
        scale_ftom = FixedDiv(FRACUNIT, scale_mtof);
        
        plotter.setThickness(
            Math.min(MTOF(FRACUNIT), DOOM.graphicSystem.getScalingX()),
            Math.min(MTOF(FRACUNIT), DOOM.graphicSystem.getScalingY())
        );
    }

    @Override
    public final void Stop() {
        this.unloadPics();
        DOOM.automapactive = false;
        // This is the only way to notify the status bar responder that we're
        // exiting the automap.
        DOOM.statusBar.NotifyAMExit();
        stopped = true;
    }

    // More "static" stuff.
    protected int lastlevel = -1, lastepisode = -1;

    @Override
    public final void Start() {
        if (!stopped)
            Stop();

        stopped = false;
        if (lastlevel != DOOM.gamemap || lastepisode != DOOM.gameepisode) {
            this.LevelInit();
            lastlevel = DOOM.gamemap;
            lastepisode = DOOM.gameepisode;
        }
        this.initVectorGraphics();
        this.LevelInit();
        this.initVariables();
        this.loadPics();
    }

    /**
     * set the window scale to the maximum size
     */
    public final void minOutWindowScale() {
        scale_mtof = min_scale_mtof;
        scale_ftom = FixedDiv(FRACUNIT, scale_mtof);
        plotter.setThickness(DOOM.graphicSystem.getScalingX(), DOOM.graphicSystem.getScalingY());
        this.activateNewScale();
    }

    /**
     * set the window scale to the minimum size
     */

    public final void maxOutWindowScale() {
        scale_mtof = max_scale_mtof;
        scale_ftom = FixedDiv(FRACUNIT, scale_mtof);
        plotter.setThickness(0, 0);
        this.activateNewScale();
    }

    /** These belong to AM_Responder */
    protected boolean cheatstate = false, bigstate = false;

    /** static char buffer[20] in AM_Responder */
    protected String buffer;

    /**
     * Handle events (user inputs) in automap mode
     */

    @Override
    @AM_Map.C(AM_Responder)
    public final boolean Responder(event_t ev) {
        boolean rc;
        rc = false;

        // System.out.println(ev.data1==AM_STARTKEY);
        if (!DOOM.automapactive) {
            if (ev.isKey(AM_STARTKEY, evtype_t.ev_keyup)) {
                this.Start();
                DOOM.viewactive = false;
                rc = true;
            }
        } else if (ev.isType(evtype_t.ev_keydown)) {
            rc = true;
            if (ev.isKey(AM_PANRIGHTKEY)) { // pan right
                if (!followplayer)
                    m_paninc.x = FTOM(F_PANINC);
                else
                    rc = false;
            } else if (ev.isKey(AM_PANLEFTKEY)) { // pan left
                if (!followplayer)
                    m_paninc.x = -FTOM(F_PANINC);
                else
                    rc = false;
            } else if (ev.isKey(AM_PANUPKEY)) { // pan up
                if (!followplayer)
                    m_paninc.y = FTOM(F_PANINC);
                else
                    rc = false;
            } else if (ev.isKey(AM_PANDOWNKEY)) { // pan down
                if (!followplayer)
                    m_paninc.y = -FTOM(F_PANINC);
                else
                    rc = false;
            } else if (ev.isKey(AM_ZOOMOUTKEY)) { // zoom out
                mtof_zoommul = M_ZOOMOUT;
                ftom_zoommul = M_ZOOMIN;
            } else if (ev.isKey(AM_ZOOMINKEY)) { // zoom in
                mtof_zoommul = M_ZOOMIN;
                ftom_zoommul = M_ZOOMOUT;
            } else if (ev.isKey(AM_GOBIGKEY)) {
                bigstate = !bigstate;
                if (bigstate) {
                    this.saveScaleAndLoc();
                    this.minOutWindowScale();
                } else
                    this.restoreScaleAndLoc();
            } else if (ev.isKey(AM_FOLLOWKEY)) {
                followplayer = !followplayer;
                f_oldloc.x = MAXINT;
                plr.message = followplayer ? AMSTR_FOLLOWON : AMSTR_FOLLOWOFF;
            } else if (ev.isKey(AM_GRIDKEY)) {
                grid = !grid;
                plr.message = grid ? AMSTR_GRIDON : AMSTR_GRIDOFF;
            } else if (ev.isKey(AM_MARKKEY)) {
                buffer = (AMSTR_MARKEDSPOT + " " + markpointnum);
                plr.message = buffer;
                this.addMark();
            } else if (ev.isKey(AM_CLEARMARKKEY)) {
                this.clearMarks();
                plr.message = AMSTR_MARKSCLEARED;
            } else {
                cheatstate = false;
                rc = false;
            }
            
            if (!DOOM.deathmatch && ev.ifKeyAsciiChar(cheat_amap::CheckCheat)) {
                rc = false;
                cheating = (cheating + 1) % 3;
            }
            
            /** 
             * MAES: brought back strobe effect
             * Good Sign: setting can be saved/loaded from config
             */
            if (ev.ifKeyAsciiChar(cheat_strobe::CheckCheat)) {
                DOOM.mapstrobe = !DOOM.mapstrobe;
            }
        } else if (ev.isType(evtype_t.ev_keyup)) {
            rc = false;
            if (ev.isKey(AM_PANRIGHTKEY)) {
                if (!followplayer)
                    m_paninc.x = 0;
            } else if (ev.isKey(AM_PANLEFTKEY)) {
                if (!followplayer)
                    m_paninc.x = 0;
            } else if (ev.isKey(AM_PANUPKEY)) {
                if (!followplayer)
                    m_paninc.y = 0;
            } else if (ev.isKey(AM_PANDOWNKEY)) {
                if (!followplayer)
                    m_paninc.y = 0;
            } else if (ev.isKey(AM_ZOOMOUTKEY) || ev.isKey(AM_ZOOMINKEY)) {
                mtof_zoommul = FRACUNIT;
                ftom_zoommul = FRACUNIT;
            } else if (ev.isKey(AM_ENDKEY)) {
                bigstate = false;
                DOOM.viewactive = true;
                this.Stop();
            }
        }

        return rc;

    }

    /**
     * Zooming
     */
    private void changeWindowScale() {

        // Change the scaling multipliers
        scale_mtof = FixedMul(scale_mtof, mtof_zoommul);
        scale_ftom = FixedDiv(FRACUNIT, scale_mtof);

        if (scale_mtof < min_scale_mtof)
            this.minOutWindowScale();
        else if (scale_mtof > max_scale_mtof)
            this.maxOutWindowScale();
        else
            this.activateNewScale();

    }

    //
    //
    //
    private void doFollowPlayer() {

        if (f_oldloc.x != plr.mo.x || f_oldloc.y != plr.mo.y) {
            m_x = FTOM(MTOF(plr.mo.x)) - m_w / 2;
            m_y = FTOM(MTOF(plr.mo.y)) - m_h / 2;
            m_x2 = m_x + m_w;
            m_y2 = m_y + m_h;
            f_oldloc.x = plr.mo.x;
            f_oldloc.y = plr.mo.y;

            // m_x = FTOM(MTOF(plr.mo.x - m_w/2));
            // m_y = FTOM(MTOF(plr.mo.y - m_h/2));
            // m_x = plr.mo.x - m_w/2;
            // m_y = plr.mo.y - m_h/2;

        }

    }

    private void updateLightLev() {
        // Change light level
        // no more buggy nexttic - Good Sign 2017/04/01
        // no more additional lightlevelcnt - Good Sign 2017/04/05
        // no more even lightlev and changed to array access - Good Sign 2017/04/08
        if (amclock % 6 == 0) {
            final int sourceLength = Color.NUM_LITES;
            final V intermeditate = DOOM.graphicSystem.convertPalettedBlock((byte) 0);
            litedColorSources.forEach((c, source) -> {
                memcpy(source, sourceLength - 1, intermeditate, 0, 1);
                memcpy(source, 0, source, 1, sourceLength - 1);
                memcpy(intermeditate, 0, source, 0, 1);
            });
        }
    }

    /**
     * Updates on Game Tick
     */
    @Override
    public final void Ticker() {
        if (!DOOM.automapactive || DOOM.menuactive)
            return;

        amclock++;

        if (followplayer)
            this.doFollowPlayer();

        // Change the zoom if necessary
        if (ftom_zoommul != FRACUNIT)
            this.changeWindowScale();

        // Change x,y location
        if ((m_paninc.x | m_paninc.y) != 0)
            this.changeWindowLoc();

        // Update light level
        if (DOOM.mapstrobe)
            updateLightLev();
    }

    // private static int BUFFERSIZE=f_h*f_w;

    /**
     * Automap clipping of lines. Based on Cohen-Sutherland clipping algorithm
     * but with a slightly faster reject and precalculated slopes. If the speed
     * is needed, use a hash algorithm to handle the common cases.
     */
    private int tmpx, tmpy;// =new fpoint_t();

    private boolean clipMline(mline_t ml, fline_t fl) {

        // System.out.print("Asked to clip from "+FixedFloat.toFloat(ml.a.x)+","+FixedFloat.toFloat(ml.a.y));
        // System.out.print(" to clip "+FixedFloat.toFloat(ml.b.x)+","+FixedFloat.toFloat(ml.b.y)+"\n");
        // These were supposed to be "registers", so they exhibit by-ref
        // properties.
        int outcode1 = 0;
        int outcode2 = 0;
        int outside;

        int dx;
        int dy;
        /*
         * fl.a.x=0; fl.a.y=0; fl.b.x=0; fl.b.y=0;
         */

        // do trivial rejects and outcodes
        if (ml.ay > m_y2)
            outcode1 = TOP;
        else if (ml.ay < m_y)
            outcode1 = BOTTOM;

        if (ml.by > m_y2)
            outcode2 = TOP;
        else if (ml.by < m_y)
            outcode2 = BOTTOM;

        if ((outcode1 & outcode2) != 0)
            return false; // trivially outside

        if (ml.ax < m_x)
            outcode1 |= LEFT;
        else if (ml.ax > m_x2)
            outcode1 |= RIGHT;

        if (ml.bx < m_x)
            outcode2 |= LEFT;
        else if (ml.bx > m_x2)
            outcode2 |= RIGHT;

        if ((outcode1 & outcode2) != 0)
            return false; // trivially outside

        // transform to frame-buffer coordinates.
        fl.ax = CXMTOF(ml.ax);
        fl.ay = CYMTOF(ml.ay);
        fl.bx = CXMTOF(ml.bx);
        fl.by = CYMTOF(ml.by);

        // System.out.println(">>>>>> ("+fl.a.x+" , "+fl.a.y+" ),("+fl.b.x+" , "+fl.b.y+" )");
        outcode1 = DOOUTCODE(fl.ax, fl.ay);
        outcode2 = DOOUTCODE(fl.bx, fl.by);

        if ((outcode1 & outcode2) != 0)
            return false;

        while ((outcode1 | outcode2) != 0) {
            // may be partially inside box
            // find an outside point
            if (outcode1 != 0)
                outside = outcode1;
            else
                outside = outcode2;

            // clip to each side
            if ((outside & TOP) != 0) {
                dy = fl.ay - fl.by;
                dx = fl.bx - fl.ax;
                tmpx = fl.ax + (dx * (fl.ay)) / dy;
                tmpy = 0;
            } else if ((outside & BOTTOM) != 0) {
                dy = fl.ay - fl.by;
                dx = fl.bx - fl.ax;
                tmpx = fl.ax + (dx * (fl.ay - f_h)) / dy;
                tmpy = f_h - 1;
            } else if ((outside & RIGHT) != 0) {
                dy = fl.by - fl.ay;
                dx = fl.bx - fl.ax;
                tmpy = fl.ay + (dy * (f_w - 1 - fl.ax)) / dx;
                tmpx = f_w - 1;
            } else if ((outside & LEFT) != 0) {
                dy = fl.by - fl.ay;
                dx = fl.bx - fl.ax;
                tmpy = fl.ay + (dy * (-fl.ax)) / dx;
                tmpx = 0;
            }

            if (outside == outcode1) {
                fl.ax = tmpx;
                fl.ay = tmpy;
                outcode1 = DOOUTCODE(fl.ax, fl.ay);
            } else {
                fl.bx = tmpx;
                fl.by = tmpy;
                outcode2 = DOOUTCODE(fl.bx, fl.by);
            }

            if ((outcode1 & outcode2) != 0)
                return false; // trivially outside
        }

        return true;
    }

    protected static int LEFT = 1, RIGHT = 2, BOTTOM = 4, TOP = 8;

    /**
     * MAES: the result was supposed to be passed in an "oc" parameter by
     * reference. Not convenient, so I made some changes...
     * 
     * @param mx
     * @param my
     */

    private int DOOUTCODE(int mx, int my) {
        int oc = 0;
        if ((my) < 0)
            (oc) |= TOP;
        else if ((my) >= f_h)
            (oc) |= BOTTOM;
        if ((mx) < 0)
            (oc) |= LEFT;
        else if ((mx) >= f_w)
            (oc) |= RIGHT;
        return oc;
    }

    /** Not my idea ;-) */
    protected int fuck = 0;

    /**
     * Clip lines, draw visible parts of lines.
     */
    protected int singlepixel = 0;

    private void drawMline(mline_t ml, V colorSource) {
        // fl.reset();
        if (this.clipMline(ml, fl)) {
            // if ((fl.a.x==fl.b.x)&&(fl.a.y==fl.b.y)) singlepixel++;
            // draws the line using coords
            DOOM.graphicSystem
                .drawLine(plotter
                    .setColorSource(colorSource, 0)
                    .setPosition(fl.ax, fl.ay),
                fl.bx, fl.by);
        }
    }

    private fline_t fl = new fline_t();

    private mline_t ml = new mline_t();

    /**
     * Draws flat (floor/ceiling tile) aligned grid lines.
     */
    private void drawGrid(V colorSource) {
        int x, y; // fixed_t
        int start, end; // fixed_t

        // Figure out start of vertical gridlines
        start = m_x;
        if (((start - DOOM.levelLoader.bmaporgx) % (MAPBLOCKUNITS << FRACBITS)) != 0)
            start +=
                (MAPBLOCKUNITS << FRACBITS)
                        - ((start - DOOM.levelLoader.bmaporgx) % (MAPBLOCKUNITS << FRACBITS));
        end = m_x + m_w;

        // draw vertical gridlines
        ml.ay = m_y;
        ml.by = m_y + m_h;
        for (x = start; x < end; x += (MAPBLOCKUNITS << FRACBITS)) {
            ml.ax = x;
            ml.bx = x;
            drawMline(ml, colorSource);
        }

        // Figure out start of horizontal gridlines
        start = m_y;
        if (((start - DOOM.levelLoader.bmaporgy) % (MAPBLOCKUNITS << FRACBITS)) != 0)
            start +=
                (MAPBLOCKUNITS << FRACBITS)
                        - ((start - DOOM.levelLoader.bmaporgy) % (MAPBLOCKUNITS << FRACBITS));
        end = m_y + m_h;

        // draw horizontal gridlines
        ml.ax = m_x;
        ml.bx = m_x + m_w;
        for (y = start; y < end; y += (MAPBLOCKUNITS << FRACBITS)) {
            ml.ay = y;
            ml.by = y;
            drawMline(ml, colorSource);
        }

    }

    protected mline_t l = new mline_t();

    /**
     * Determines visible lines, draws them. This is LineDef based, not LineSeg
     * based.
     */

    private void drawWalls() {

        final V teleColorSource = litedColorSources.get(TELECOLORS);
        final V wallColorSource = litedColorSources.get(WALLCOLORS);
        final V fdWallColorSource = litedColorSources.get(FDWALLCOLORS);
        final V cdWallColorSource = litedColorSources.get(CDWALLCOLORS);
        final V tsWallColorSource = litedColorSources.get(TSWALLCOLORS);
        final V secretWallColorSource = litedColorSources.get(SECRETWALLCOLORS);

        for (int i = 0; i < DOOM.levelLoader.numlines; i++) {
            l.ax = DOOM.levelLoader.lines[i].v1x;
            l.ay = DOOM.levelLoader.lines[i].v1y;
            l.bx = DOOM.levelLoader.lines[i].v2x;
            l.by = DOOM.levelLoader.lines[i].v2y;
            if ((cheating | (DOOM.levelLoader.lines[i].flags & ML_MAPPED)) != 0) {
                if (((DOOM.levelLoader.lines[i].flags & LINE_NEVERSEE) & ~cheating) != 0)
                    continue;
                if (DOOM.levelLoader.lines[i].backsector == null) {
                    drawMline(l, wallColorSource);
                } else {
                    if (DOOM.levelLoader.lines[i].special == 39) { // teleporters
                        drawMline(l, teleColorSource);
                    } else if ((DOOM.levelLoader.lines[i].flags & ML_SECRET) != 0) // secret
                                                                     // door
                    {
                        if (cheating != 0)
                            drawMline(l, secretWallColorSource);
                        else
                            drawMline(l, wallColorSource);
                    } else if (DOOM.levelLoader.lines[i].backsector.floorheight != DOOM.levelLoader.lines[i].frontsector.floorheight) {
                        drawMline(l, fdWallColorSource); // floor level change
                    } else if (DOOM.levelLoader.lines[i].backsector.ceilingheight != DOOM.levelLoader.lines[i].frontsector.ceilingheight) {
                        drawMline(l, cdWallColorSource); // ceiling level change
                    } else if (cheating != 0) {
                        drawMline(l, tsWallColorSource);
                    }
                }
            }
            // If we have allmap...
            else if (plr.powers[pw_allmap] != 0) {
                // Some are never seen even with that!
                if ((DOOM.levelLoader.lines[i].flags & LINE_NEVERSEE) == 0)
                    drawMline(l, litedColorSources.get(MAPPOWERUPSHOWNCOLORS));
            }
        }

        // System.out.println("Single pixel draws: "+singlepixel+" out of "+P.lines.length);
        // singlepixel=0;
    }

    //
    // Rotation in 2D.
    // Used to rotate player arrow line character.
    //
    private int rotx, roty;

    /**
     * Rotation in 2D. Used to rotate player arrow line character.
     * 
     * @param x
     *        fixed_t
     * @param y
     *        fixed_t
     * @param a
     *        angle_t -> this should be a LUT-ready BAM.
     */

    private void rotate(int x, int y, int a) {
        // int tmpx;

        rotx = FixedMul(x, finecosine[a]) - FixedMul(y, finesine[a]);

        roty = FixedMul(x, finesine[a]) + FixedMul(y, finecosine[a]);

        // rotx.val = tmpx;
    }

    private void drawLineCharacter(mline_t[] lineguy, int lineguylines,
            int scale, // fixed_t
            int angle, // This should be a LUT-ready angle.
            V colorSource,
            int x, // fixed_t
            int y // fixed_t
    ) {
        int i;
        final boolean rotate = (angle != 0);
        mline_t l = new mline_t();

        for (i = 0; i < lineguylines; i++) {
            l.ax = lineguy[i].ax;
            l.ay = lineguy[i].ay;

            if (scale != 0) {
                l.ax = FixedMul(scale, l.ax);
                l.ay = FixedMul(scale, l.ay);
            }

            if (rotate) {
                rotate(l.ax, l.ay, angle);
                // MAES: assign rotations
                l.ax = rotx;
                l.ay = roty;
            }

            l.ax += x;
            l.ay += y;

            l.bx = lineguy[i].bx;
            l.by = lineguy[i].by;

            if (scale != 0) {
                l.bx = FixedMul(scale, l.bx);
                l.by = FixedMul(scale, l.by);
            }

            if (rotate) {
                rotate(l.bx, l.by, angle);
                // MAES: assign rotations
                l.bx = rotx;
                l.by = roty;
            }

            l.bx += x;
            l.by += y;

            drawMline(l, colorSource);
        }
    }

    public final void drawPlayers() {
        player_t p;

        int their_color = -1;
        V colorSource;

        // System.out.println(Long.toHexString(plr.mo.angle));

        if (!DOOM.netgame) {
            if (cheating != 0)
                drawLineCharacter(cheat_player_arrow, NUMCHEATPLYRLINES, 0,
                    toBAMIndex(plr.mo.angle), fixedColorSources.get(Color.WHITE), plr.mo.x,
                    plr.mo.y);
            else
                drawLineCharacter(player_arrow, NUMPLYRLINES, 0,
                    toBAMIndex(plr.mo.angle), fixedColorSources.get(Color.WHITE), plr.mo.x,
                    plr.mo.y);
            return;
        }

        for (int i = 0; i < MAXPLAYERS; i++) {
            their_color++;
            p = DOOM.players[i];

            if ((DOOM.deathmatch && !DOOM.singledemo) && p != plr)
                continue;

            if (!DOOM.playeringame[i])
                continue;

            if (p.powers[pw_invisibility] != 0)
                colorSource = fixedColorSources.get(Color.CLOSE_TO_BLACK);
            else
                colorSource = fixedColorSources.get(THEIR_COLORS[their_color]);

            drawLineCharacter(player_arrow, NUMPLYRLINES, 0, (int) p.mo.angle, colorSource, p.mo.x, p.mo.y);
        }

    }

    final void drawThings(Color colors, int colorrange) {
        mobj_t t;
        V colorSource = litedColorSources.get(colors); // Ain't gonna change

        for (int i = 0; i < DOOM.levelLoader.numsectors; i++) {
            // MAES: get first on the list.
            t = DOOM.levelLoader.sectors[i].thinglist;
            while (t != null) {
                drawLineCharacter(thintriangle_guy, NUMTHINTRIANGLEGUYLINES,
                    16 << FRACBITS, toBAMIndex(t.angle), colorSource, t.x, t.y);
                t = (mobj_t) t.snext;
            }
        }
    }

    public final void drawMarks() {
        int i, fx, fy, w, h;

        for (i = 0; i < AM_NUMMARKPOINTS; i++) {
            if (markpoints[i].x != -1) {
                w = marknums[i].width;
                h = marknums[i].height;
                // Nothing wrong with v1.9 IWADs, but I wouldn't put my hand on
                // the fire for older ones.
                // w = 5; // because something's wrong with the wad, i guess
                // h = 6; // because something's wrong with the wad, i guess
                fx = CXMTOF(markpoints[i].x);
                fy = CYMTOF(markpoints[i].y);
                if (fx >= f_x && fx <= f_w - w && fy >= f_y && fy <= f_h - h)
                    DOOM.graphicSystem.DrawPatchScaled(FG, marknums[i], DOOM.vs, fx, fy, V_NOSCALESTART);
            }
        }

    }

    private void drawCrosshair(V colorSource) {
        /*plotter.setPosition(
                DOOM.videoRenderer.getScreenWidth() / 2,
                DOOM.videoRenderer.getScreenHeight()/ 2
            ).setColorSource(colorSource, 0)
            .plot();*/
        //fb[(f_w * (f_h + 1)) / 2] = (short) color; // single point for now
    }

    @Override
    public final void Drawer() {
        if (!DOOM.automapactive)
            return;
        // System.out.println("Drawing map");
        if (overlay < 1)
            DOOM.graphicSystem.FillRect(FG, f_rect, BACKGROUND.value); // BACKGROUND
        
        if (grid)
            drawGrid(fixedColorSources.get(GRIDCOLORS));
        
        drawWalls();
        drawPlayers();
        if (cheating == 2)
            drawThings(THINGCOLORS, THINGRANGE);
        drawCrosshair(fixedColorSources.get(CROSSHAIRCOLORS));

        drawMarks();

        //DOOM.videoRenderer.MarkRect(f_x, f_y, f_w, f_h);

    }
}
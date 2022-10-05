package org.bleachhack.util.doom.f;

/* Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: EndLevel.java,v 1.11 2012/09/24 17:16:23 velktron Exp $
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// $Log: EndLevel.java,v $
// Revision 1.11  2012/09/24 17:16:23  velktron
// Massive merge between HiColor and HEAD. There's no difference from now on, and development continues on HEAD.
//
// Revision 1.8.2.2  2012/09/24 16:57:43  velktron
// Addressed generics warnings.
//
// Revision 1.8.2.1  2011/11/27 18:18:34  velktron
// Use cacheClear() on deactivation.
//
// Revision 1.8  2011/11/01 19:02:57  velktron
// Using screen number constants
//
// Revision 1.7  2011/10/23 18:11:32  velktron
// Generic compliance for DoomVideoInterface
//
// Revision 1.6  2011/08/23 16:13:53  velktron
// Got rid of Z remnants.
//
// Revision 1.5  2011/07/31 21:49:38  velktron
// Changed endlevel drawer's behavior to be closer to prBoom+'s. Allows using 1994TU.WAD while backwards compatible.
//
// Revision 1.4  2011/06/02 14:56:48  velktron
// imports
//
// Revision 1.3  2011/06/02 14:53:21  velktron
// Moved Endlevel constants to AbstractEndLevel
//
// Revision 1.2  2011/06/02 14:14:28  velktron
// Implemented endlevel unloading of graphics, changed state enum.
//
// Revision 1.1  2011/06/02 14:00:48  velktron
// Moved Endlevel stuff  to f, where it makes more sense.
//
// Revision 1.18  2011/05/31 12:25:14  velktron
// Endlevel -mostly- scaled correctly.
//
// Revision 1.17  2011/05/29 22:15:32  velktron
// Introduced IRandom interface.
//
// Revision 1.16  2011/05/24 17:54:02  velktron
// Defaults tester
//
// Revision 1.15  2011/05/23 17:00:39  velktron
// Got rid of verbosity
//
// Revision 1.14  2011/05/21 16:53:24  velktron
// Adapted to use new gamemode system.
//
// Revision 1.13  2011/05/18 16:58:04  velktron
// Changed to DoomStatus
//
// Revision 1.12  2011/05/17 16:52:19  velktron
// Switched to DoomStatus
//
// Revision 1.11  2011/05/11 14:12:08  velktron
// Interfaced with DoomGame
//
// Revision 1.10  2011/05/10 10:39:18  velktron
// Semi-playable Techdemo v1.3 milestone
//
// Revision 1.9  2011/05/06 14:00:54  velktron
// More of _D_'s changes committed.
//
// Revision 1.8  2011/02/11 00:11:13  velktron
// A MUCH needed update to v1.3.
//
// Revision 1.7  2010/12/20 17:15:08  velktron
// Made the renderer more OO -> TextureManager and other changes as well.
//
// Revision 1.6  2010/11/12 13:37:25  velktron
// Rationalized the LUT system - now it's 100% procedurally generated.
//
// Revision 1.5  2010/09/23 07:31:11  velktron
// fuck
//
// Revision 1.4  2010/09/02 15:56:54  velktron
// Bulk of unified renderer copyediting done.
//
// Some changes like e.g. global separate limits class and instance methods for seg_t and node_t introduced.
//
// Revision 1.3  2010/08/23 14:36:08  velktron
// Menu mostly working, implemented Killough's fast hash-based GetNumForName, although it can probably be finetuned even more.
//
// Revision 1.2  2010/08/13 14:06:36  velktron
// Endlevel screen fully functional!
//
// Revision 1.1  2010/07/06 16:32:38  velktron
// Threw some work in WI, now EndLevel. YEAH THERE'S GONNA BE A SEPARATE EndLevel OBJECT THAT'S HOW PIMP THE PROJECT IS!!!!11!!!
//
// Revision 1.1  2010/06/30 08:58:51  velktron
// Let's see if this stuff will finally commit....
//
//
// Most stuff is still  being worked on. For a good place to start and get an idea of what is being done, I suggest checking out the "testers" package.
//
// Revision 1.1  2010/06/29 11:07:34  velktron
// Release often, release early they say...
//
// Commiting ALL stuff done so far. A lot of stuff is still broken/incomplete, and there's still mixed C code in there. I suggest you load everything up in Eclpise and see what gives from there.
//
// A good place to start is the testers/ directory, where you  can get an idea of how a few of the implemented stuff works.
//
//
// DESCRIPTION:
//	Intermission screens.
//
//-----------------------------------------------------------------------------*/
import static org.bleachhack.util.doom.data.Defines.*;
import static org.bleachhack.util.doom.data.Limits.*;
import org.bleachhack.util.doom.data.sounds.musicenum_t;
import org.bleachhack.util.doom.data.sounds.sfxenum_t;
import org.bleachhack.util.doom.defines.*;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.SourceCode;
import org.bleachhack.util.doom.doom.SourceCode.CauseOfDesyncProbability;
import org.bleachhack.util.doom.doom.SourceCode.WI_Stuff;
import static org.bleachhack.util.doom.doom.SourceCode.WI_Stuff.WI_Start;
import static org.bleachhack.util.doom.doom.SourceCode.WI_Stuff.WI_initAnimatedBack;
import static org.bleachhack.util.doom.doom.SourceCode.WI_Stuff.WI_initDeathmatchStats;
import static org.bleachhack.util.doom.doom.SourceCode.WI_Stuff.WI_initNetgameStats;
import static org.bleachhack.util.doom.doom.SourceCode.WI_Stuff.WI_initStats;
import static org.bleachhack.util.doom.doom.SourceCode.WI_Stuff.WI_initVariables;
import static org.bleachhack.util.doom.doom.SourceCode.WI_Stuff.WI_loadData;
import org.bleachhack.util.doom.doom.event_t;
import org.bleachhack.util.doom.doom.player_t;
import org.bleachhack.util.doom.doom.wbplayerstruct_t;
import org.bleachhack.util.doom.doom.wbstartstruct_t;
import org.bleachhack.util.doom.rr.*;
import static org.bleachhack.util.doom.v.DoomGraphicSystem.*;
import static org.bleachhack.util.doom.v.renderers.DoomScreen.*;

/**
 * This class (stuff.c) seems to implement the endlevel screens.
 *
 * @author Maes
 *
 */
public class EndLevel<T, V> extends AbstractEndLevel {

    ////////////////// STATUS ///////////////////
    private final DoomMain<T, V> DOOM;

    private static final int COUNT_KILLS = 2;
    private static final int COUNT_ITEMS = 4;
    private static final int COUNT_SECRETS = 6;
    private static final int COUNT_TIME = 8;
    private static final int COUNT_DONE = 10;

    static enum endlevel_state {
        NoState,
        StatCount,
        ShowNextLoc,
        JustShutOff
    }

    //GLOBAL LOCATIONS
    private static final int WI_TITLEY = 2;
    private static final int WI_SPACINGY = 3;

    //
    // GENERAL DATA
    //
    //
    // Locally used stuff.
    //
    private static final boolean RANGECHECKING = true;

    // Where to draw some stuff. To be scaled up, so they
    // are not final.
    public static int SP_STATSX;
    public static int SP_STATSY;

    public static int SP_TIMEX;
    public static int SP_TIMEY;

    // States for single-player
    protected static int SP_KILLS = 0;
    protected static int SP_ITEMS = 2;
    protected static int SP_SECRET = 4;
    protected static int SP_FRAGS = 6;
    protected static int SP_TIME = 8;
    protected static int SP_PAR = SP_TIME;

    protected int SP_PAUSE = 1;

    // in seconds
    protected int SHOWNEXTLOCDELAY = 4;
    protected int SHOWLASTLOCDELAY = SHOWNEXTLOCDELAY;

    // used to accelerate or skip a stage
    int acceleratestage;

    // wbs->pnum
    int me;

    // specifies current state )
    endlevel_state state;

    // contains information passed into intermission
    public wbstartstruct_t wbs;

    wbplayerstruct_t[] plrs;  // wbs->plyr[]

    // used for general timing
    int cnt;

    // used for timing of background animation
    int bcnt;

    // signals to refresh everything for one frame
    int firstrefresh;

    int[] cnt_kills = new int[MAXPLAYERS];
    int[] cnt_items = new int[MAXPLAYERS];
    int[] cnt_secret = new int[MAXPLAYERS];
    int cnt_time;
    int cnt_par;
    int cnt_pause;

    // # of commercial levels
    int NUMCMAPS;

    //
    //	GRAPHICS
    //
    // background (map of levels).
    patch_t bg;

    // You Are Here graphic
    patch_t[] yah = new patch_t[3];

    // splat
    patch_t[] splat;

    /**
     * %, : graphics
     */
    patch_t percent, colon;

    /**
     * 0-9 graphic
     */
    patch_t[] num = new patch_t[10];

    /**
     * minus sign
     */
    patch_t wiminus;

    // "Finished!" graphics
    patch_t finished;

    // "Entering" graphic
    patch_t entering;

    // "secret"
    patch_t sp_secret;

    /**
     * "Kills", "Scrt", "Items", "Frags"
     */
    patch_t kills, secret, items, frags;

    /**
     * Time sucks.
     */
    patch_t time, par, sucks;

    /**
     * "killers", "victims"
     */
    patch_t killers, victims;

    /**
     * "Total", your face, your dead face
     */
    patch_t total, star, bstar;

    /**
     * "red P[1..MAXPLAYERS]"
     */
    patch_t[] p = new patch_t[MAXPLAYERS];

    /**
     * "gray P[1..MAXPLAYERS]"
     */
    patch_t[] bp = new patch_t[MAXPLAYERS];

    /**
     * Name graphics of each level (centered)
     */
    patch_t[] lnames;

    //
    // CODE
    //
    // slam background
    // UNUSED  unsigned char *background=0;
    public EndLevel(DoomMain<T, V> DOOM) {
        this.DOOM = DOOM;

        // Pre-scale stuff.
        SP_STATSX = 50 * DOOM.vs.getSafeScaling();
        SP_STATSY = 50 * DOOM.vs.getSafeScaling();

        SP_TIMEX = 16 * DOOM.vs.getSafeScaling();
        SP_TIMEY = (DOOM.vs.getScreenHeight() - DOOM.statusBar.getHeight());
        // _D_: commented this, otherwise something didn't work
        //this.Start(DS.wminfo);
    }

    protected void slamBackground() {
        //    memcpy(screens[0], screens[1], DOOM.vs.getScreenWidth() * DOOM.vs.getScreenHeight());
        // Remember, the second arg is the source!
        DOOM.graphicSystem.screenCopy(BG, FG);
        //System.arraycopy(V.getScreen(SCREEN_BG), 0 ,V.getScreen(SCREEN_FG),0, DOOM.vs.getScreenWidth() * DOOM.vs.getScreenHeight());
        //V.MarkRect (0, 0, DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight());
    }

// The ticker is used to detect keys
//  because of timing issues in netgames.
    public boolean Responder(event_t ev) {
        return false;
    }

    /**
     * Draws "<Levelname> Finished!"
     */
    protected void drawLF() {
        int y = WI_TITLEY;

        // draw <LevelName> 
        DOOM.graphicSystem.DrawPatchScaled(FG, lnames[wbs.last], DOOM.vs, (320 - lnames[wbs.last].width) / 2, y);

        // draw "Finished!"
        y += (5 * lnames[wbs.last].height) / 4;

        DOOM.graphicSystem.DrawPatchScaled(FG, finished, DOOM.vs, (320 - finished.width) / 2, y);
    }

    /**
     * Draws "Entering <LevelName>"
     */
    protected void drawEL() {
        int y = WI_TITLEY; // This is in 320 x 200 coords!

        // draw "Entering"
        DOOM.graphicSystem.DrawPatchScaled(FG, entering, DOOM.vs, (320 - entering.width) / 2, y);

        // HACK: if lnames[wbs.next] DOES have a defined nonzero topoffset, use it.
        // implicitly in DrawScaledPatch, and trump the normal behavior.
        // FIXME: this is only useful in a handful of prBoom+ maps, which use
        // a modified endlevel screen. The reason it works there is the behavior of the 
        // unified patch drawing function, which is approximated with this hack.
        if (lnames[wbs.next].topoffset == 0) {
            y += (5 * lnames[wbs.next].height) / 4;
        }
        // draw level.

        DOOM.graphicSystem.DrawPatchScaled(FG, lnames[wbs.next], DOOM.vs, (320 - lnames[wbs.next].width) / 2, y);

    }

    /**
     * Fixed the issue with splat patch_t[] - a crash caused by null in array - by importing fix from prboom-plus. The
     * issue was: developers intended to be able to pass one patch_t or two at once, when they pass one, they use a
     * pointer expecting an array but without a real array, producing UB. At first, I've bring back an array by redoing
     * splat as patch_t[] instead of single patch_t. Secondly, I've emulated UB by allowing null to be found in splat
     * array Finally, I've 'fixed' this imaginary UB by testing against null, as it is done in prboom-plus.
     *
     * So at the moment it should work exactly as in vanilla if it would not crash. However, additional testing may
     * apply to revert this fix. - Good Sign 2017/04/04
     *
     * For whatever fucked-up reason, it expects c to be an array of patches, and may choose to draw from alternative
     * ones...which however are never loaded, or are supposed to be "next" in memory or whatever. I kept this behavior,
     * however in Java it will NOT work as intended, if ever.
     *
     * @param n
     * @param c
     */
    protected void
            drawOnLnode(int n,
                    patch_t[] c) {

        int i;
        int left;
        int top;
        int right;
        int bottom;
        boolean fits = false;

        i = 0;
        do {
            left = lnodes[wbs.epsd][n].x - c[i].leftoffset;
            top = lnodes[wbs.epsd][n].y - c[i].topoffset;
            right = left + c[i].width;
            bottom = top + c[i].height;

            if (left >= 0
                    && right < DOOM.vs.getScreenWidth()
                    && top >= 0
                    && bottom < DOOM.vs.getScreenHeight()) {
                fits = true;
            } else {
                i++;
            }
        } while (!fits && i != 2 && c[i] != null);

        if (fits && i < 2) {
            //V.DrawPatch(lnodes[wbs.epsd][n].x, lnodes[wbs.epsd][n].y,
            //	    FB, c[i]);
            DOOM.graphicSystem.DrawPatchScaled(FG, c[i], DOOM.vs, lnodes[wbs.epsd][n].x, lnodes[wbs.epsd][n].y);
        } else {
            // DEBUG
            System.out.println("Could not place patch on level " + n + 1);
        }
    }

    @SourceCode.Exact
    @WI_Stuff.C(WI_initAnimatedBack)
    protected void initAnimatedBack() {
        anim_t a;

        if (DOOM.isCommercial()) {
            return;
        }

        if (wbs.epsd > 2) {
            return;
        }

        for (int i = 0; i < NUMANIMS[wbs.epsd]; i++) {
            a = anims[wbs.epsd][i];

            // init variables
            a.ctr = -1;

            if (null != a.type) // specify the next time to draw it
            switch (a.type) {
                case ANIM_ALWAYS:
                    a.nexttic = bcnt + 1 + (DOOM.random.M_Random() % a.period);
                    break;
                case ANIM_RANDOM:
                    a.nexttic = bcnt + 1 + a.data2 + (DOOM.random.M_Random() % a.data1);
                    break;
                case ANIM_LEVEL:
                    a.nexttic = bcnt + 1;
                    break;
                default:
                    break;
            }
        }

    }

    protected void updateAnimatedBack() {
        int i;
        anim_t a;

        if (DOOM.isCommercial()) {
            return;
        }

        if (wbs.epsd > 2) {
            return;
        }

        int aaptr = wbs.epsd;

        for (i = 0; i < NUMANIMS[wbs.epsd]; i++) {
            a = anims[aaptr][i];

            if (bcnt == a.nexttic) {
                switch (a.type) {
                    case ANIM_ALWAYS:
                        if (++anims[aaptr][i].ctr >= a.nanims) {
                            a.ctr = 0;
                        }
                        a.nexttic = bcnt + a.period;
                        break;

                    case ANIM_RANDOM:
                        a.ctr++;
                        if (a.ctr == a.nanims) {
                            a.ctr = -1;
                            a.nexttic = bcnt + a.data2 + (DOOM.random.M_Random() % a.data1);
                        } else {
                            a.nexttic = bcnt + a.period;
                        }
                        break;

                    case ANIM_LEVEL:
                        // gawd-awful hack for level anims
                        if (!(state == endlevel_state.StatCount && i == 7)
                                && wbs.next == a.data1) {
                            a.ctr++;
                            if (a.ctr == a.nanims) {
                                a.ctr--;
                            }
                            a.nexttic = bcnt + a.period;
                        }
                        break;
                }
            }

        }

    }

    protected void drawAnimatedBack() {
        int i;
        anim_t a;

        if (DOOM.isCommercial()) {
            return;
        }

        if (wbs.epsd > 2) {
            return;
        }

        for (i = 0; i < NUMANIMS[wbs.epsd]; i++) {
            a = anims[wbs.epsd][i];

            if (a.ctr >= 0) {
                DOOM.graphicSystem.DrawPatchScaled(FG, a.p[a.ctr], DOOM.vs, a.loc.x, a.loc.y);
            }
        }

    }

    /**
     * Draws a number. If digits > 0, then use that many digits minimum, otherwise only use as many as necessary.
     * Returns new x position.
     */
    protected int drawNum(int x, int y, int n, int digits) {

        int fontwidth = num[0].width;
        boolean neg;
        int temp;

        if (digits < 0) {
            if (n == 0) {
                // make variable-length zeros 1 digit long
                digits = 1;
            } else {
                // figure out # of digits in #
                digits = 0;
                temp = n;

                while (temp != 0) {
                    temp /= 10;
                    digits++;
                }
            }
        }

        neg = (n < 0);
        if (neg) {
            n = -n;
        }

        // if non-number, do not draw it
        if (n == 1994) {
            return 0;
        }

        // draw the new number
        while ((digits--) != 0) {
            x -= fontwidth * DOOM.vs.getScalingX();
            DOOM.graphicSystem.DrawPatchScaled(FG, num[n % 10], DOOM.vs, x, y, V_NOSCALESTART);
            n /= 10;
        }

        // draw a minus sign if necessary
        if (neg) {
            DOOM.graphicSystem.DrawPatchScaled(FG, wiminus, DOOM.vs, x -= 8 * DOOM.vs.getScalingX(), y, V_NOSCALESTART);
        }

        return x;

    }

    protected void drawPercent(int x, int y, int p) {
        if (p < 0) {
            return;
        }

        DOOM.graphicSystem.DrawPatchScaled(FG, percent, DOOM.vs, x, y, V_NOSCALESTART);
        drawNum(x, y, p, -1);
    }

//
// Display level completion time and par,
//  or "sucks" message if overflow.
//
    protected void drawTime(int x,
            int y,
            int t) {

        int div;
        int n;

        if (t < 0) {
            return;
        }

        if (t <= 61 * 59) {
            div = 1;

            do {
                n = (t / div) % 60;
                x = drawNum(x, y, n, 2) - colon.width * DOOM.vs.getScalingX();
                div *= 60;

                // draw
                if ((div == 60) || (t / div) > 0) {
                    DOOM.graphicSystem.DrawPatchScaled(FG, colon, DOOM.vs, x, y, V_NOSCALESTART);
                }

            } while ((t / div) > 0);
        } else {
            // "sucks"
            DOOM.graphicSystem.DrawPatchScaled(FG, sucks, DOOM.vs, x - sucks.width * DOOM.vs.getScalingX(), y, V_NOSCALESTART);
        }
    }

    protected void End() {
        state = endlevel_state.JustShutOff;
        DOOM.graphicSystem.forcePalette();
        unloadData();
    }

    protected void unloadData() {
        int i;
        int j;

        DOOM.wadLoader.UnlockLumpNum(wiminus);
        wiminus = null;

        for (i = 0; i < 10; i++) {
            DOOM.wadLoader.UnlockLumpNum(num[i]);
            num[i] = null;
        }

        if (DOOM.isCommercial()) {
            for (i = 0; i < NUMCMAPS; i++) {
                DOOM.wadLoader.UnlockLumpNum(lnames[i]);
                lnames[i] = null;
            }
        } else {
            DOOM.wadLoader.UnlockLumpNum(yah[0]);
            yah[0] = null;
            DOOM.wadLoader.UnlockLumpNum(yah[1]);
            yah[1] = null;

            DOOM.wadLoader.UnlockLumpNum(splat[0]);
            splat[0] = null;

            for (i = 0; i < NUMMAPS; i++) {
                DOOM.wadLoader.UnlockLumpNum(lnames[i]);
                lnames[i] = null;

            }
            if (wbs.epsd < 3) {
                for (j = 0; j < NUMANIMS[wbs.epsd]; j++) {
                    if (wbs.epsd != 1 || j != 8) {
                        for (i = 0; i < anims[wbs.epsd][j].nanims; i++) {
                            DOOM.wadLoader.UnlockLumpNum(anims[wbs.epsd][j].p[i]);
                            anims[wbs.epsd][j].p[i] = null;
                        }
                    }
                }
            }
        }
        DOOM.wadLoader.UnlockLumpNum(percent);
        percent = null;
        DOOM.wadLoader.UnlockLumpNum(colon);
        colon = null;
        DOOM.wadLoader.UnlockLumpNum(finished);
        finished = null;
        DOOM.wadLoader.UnlockLumpNum(entering);
        entering = null;
        DOOM.wadLoader.UnlockLumpNum(kills);
        kills = null;
        DOOM.wadLoader.UnlockLumpNum(secret);
        secret = null;
        DOOM.wadLoader.UnlockLumpNum(sp_secret);
        sp_secret = null;
        DOOM.wadLoader.UnlockLumpNum(items);
        items = null;
        DOOM.wadLoader.UnlockLumpNum(frags);
        frags = null;
        DOOM.wadLoader.UnlockLumpNum(time);
        time = null;
        DOOM.wadLoader.UnlockLumpNum(sucks);
        sucks = null;
        DOOM.wadLoader.UnlockLumpNum(par);
        par = null;
        DOOM.wadLoader.UnlockLumpNum(victims);
        victims = null;
        DOOM.wadLoader.UnlockLumpNum(killers);
        killers = null;
        DOOM.wadLoader.UnlockLumpNum(total);
        total = null;
        for (i = 0; i < MAXPLAYERS; i++) {
            DOOM.wadLoader.UnlockLumpNum(p[i]);
            DOOM.wadLoader.UnlockLumpNum(bp[i]);
            p[i] = null;
            bp[i] = null;
        }
    }

    protected void initNoState() {
        state = endlevel_state.NoState;
        acceleratestage = 0;
        cnt = 10;
    }

    protected void updateNoState() {

        updateAnimatedBack();

        if (--cnt == 00) {
            End();
            DOOM.WorldDone();
        }

    }

    boolean snl_pointeron = false;

    protected void initShowNextLoc() {
        state = endlevel_state.ShowNextLoc;
        acceleratestage = 0;
        cnt = SHOWNEXTLOCDELAY * TICRATE;

        initAnimatedBack();
    }

    protected void updateShowNextLoc() {
        updateAnimatedBack();

        if ((--cnt == 0) || (acceleratestage != 0)) {
            initNoState();
        } else {
            snl_pointeron = (cnt & 31) < 20;
        }
    }

    protected void drawShowNextLoc() {

        int i;
        int last;

        slamBackground();

        // draw animated background
        drawAnimatedBack();

        if (!DOOM.isCommercial()) {
            if (wbs.epsd > 2) {
                drawEL();
                return;
            }

            last = (wbs.last == 8) ? wbs.next - 1 : wbs.last;

            // draw a splat on taken cities.
            for (i = 0; i <= last; i++) {
                drawOnLnode(i, splat);
            }

            // splat the secret level?
            if (wbs.didsecret) {
                drawOnLnode(8, splat);
            }

            // draw flashing ptr
            if (snl_pointeron) {
                drawOnLnode(wbs.next, yah);
            }
        }

        // draws which level you are entering..
        if ((!DOOM.isCommercial())
                || wbs.next != 30) {
            drawEL();
        }

    }

    protected void drawNoState() {
        snl_pointeron = true;
        drawShowNextLoc();
    }

    protected int fragSum(int playernum) {
        int i;
        int frags = 0;

        for (i = 0; i < MAXPLAYERS; i++) {
            if (DOOM.playeringame[i]
                    && i != playernum) {
                frags += plrs[playernum].frags[i];
            }
        }

        // JDC hack - negative frags.
        frags -= plrs[playernum].frags[playernum];
        // UNUSED if (frags < 0)
        // 	frags = 0;

        return frags;
    }

    int dm_state;
    int[][] dm_frags = new int[MAXPLAYERS][MAXPLAYERS];
    int[] dm_totals = new int[MAXPLAYERS];

    @SourceCode.Exact
    @WI_Stuff.C(WI_initDeathmatchStats)
    protected void initDeathmatchStats() {
        state = endlevel_state.StatCount;
        acceleratestage = 0;
        dm_state = 1;

        cnt_pause = TICRATE;

        for (int i = 0; i < MAXPLAYERS; i++) {
            if (DOOM.playeringame[i]) {
                for (int j = 0; j < MAXPLAYERS; j++) {
                    if (DOOM.playeringame[j]) {
                        dm_frags[i][j] = 0;
                    }
                }

                dm_totals[i] = 0;
            }
        }

        WI_initAnimatedBack: {
            initAnimatedBack();
        }
    }

    protected void updateDeathmatchStats() {

        int i;
        int j;

        boolean stillticking;

        updateAnimatedBack();

        if ((acceleratestage != 0) && (dm_state != 4)) {
            acceleratestage = 0;

            for (i = 0; i < MAXPLAYERS; i++) {
                if (DOOM.playeringame[i]) {
                    for (j = 0; j < MAXPLAYERS; j++) {
                        if (DOOM.playeringame[j]) {
                            dm_frags[i][j] = plrs[i].frags[j];
                        }
                    }

                    dm_totals[i] = fragSum(i);
                }
            }

            DOOM.doomSound.StartSound(null, sfxenum_t.sfx_barexp);
            dm_state = 4;
        }

        if (dm_state == 2) {
            if ((bcnt & 3) == 0) {
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_pistol);
            }

            stillticking = false;

            for (i = 0; i < MAXPLAYERS; i++) {
                if (DOOM.playeringame[i]) {
                    for (j = 0; j < MAXPLAYERS; j++) {
                        if (DOOM.playeringame[j]
                                && dm_frags[i][j] != plrs[i].frags[j]) {
                            if (plrs[i].frags[j] < 0) {
                                dm_frags[i][j]--;
                            } else {
                                dm_frags[i][j]++;
                            }

                            if (dm_frags[i][j] > 99) {
                                dm_frags[i][j] = 99;
                            }

                            if (dm_frags[i][j] < -99) {
                                dm_frags[i][j] = -99;
                            }

                            stillticking = true;
                        }
                    }
                    dm_totals[i] = fragSum(i);

                    if (dm_totals[i] > 99) {
                        dm_totals[i] = 99;
                    }

                    if (dm_totals[i] < -99) {
                        dm_totals[i] = -99;
                    }
                }

            }
            if (!stillticking) {
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_barexp);
                dm_state++;
            }

        } else if (dm_state == 4) {
            if (acceleratestage != 0) {
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_slop);

                if (DOOM.isCommercial()) {
                    initNoState();
                } else {
                    initShowNextLoc();
                }
            }
        } else if ((dm_state & 1) != 0) {
            if (--cnt_pause == 0) {
                dm_state++;
                cnt_pause = TICRATE;
            }
        }
    }

    protected void drawDeathmatchStats() {

        int i;
        int j;
        int x;
        int y;
        int w;

        int lh = WI_SPACINGY; // line height

        slamBackground();

        // draw animated background
        drawAnimatedBack();
        drawLF();

        // draw stat titles (top line)
        DOOM.graphicSystem.DrawPatch(FG, total, DM_TOTALSX - total.width / 2, DM_MATRIXY - WI_SPACINGY + 10);
        DOOM.graphicSystem.DrawPatch(FG, killers, DM_KILLERSX, DM_KILLERSY);
        DOOM.graphicSystem.DrawPatch(FG, victims, DM_VICTIMSX, DM_VICTIMSY);

        // draw P?
        x = DM_MATRIXX + DM_SPACINGX;
        y = DM_MATRIXY;

        for (i = 0; i < MAXPLAYERS; i++) {
            if (DOOM.playeringame[i]) {
                DOOM.graphicSystem.DrawPatch(FG, p[i], x - p[i].width / 2, DM_MATRIXY - WI_SPACINGY);
                DOOM.graphicSystem.DrawPatch(FG, p[i], DM_MATRIXX - p[i].width / 2, y);

                if (i == me) {
                    DOOM.graphicSystem.DrawPatch(FG, bstar, x - p[i].width / 2, DM_MATRIXY - WI_SPACINGY);
                    DOOM.graphicSystem.DrawPatch(FG, star, DM_MATRIXX - p[i].width / 2, y);
                }
            } else {
                // V_DrawPatch(x-SHORT(bp[i].width)/2,
                //   DM_MATRIXY - WI_SPACINGY, FB, bp[i]);
                // V_DrawPatch(DM_MATRIXX-SHORT(bp[i].width)/2,
                //   y, FB, bp[i]);
            }
            x += DM_SPACINGX;
            y += WI_SPACINGY;
        }

        // draw stats
        y = DM_MATRIXY + 10;
        w = num[0].width;

        for (i = 0; i < MAXPLAYERS; i++) {
            x = DM_MATRIXX + DM_SPACINGX;

            if (DOOM.playeringame[i]) {
                for (j = 0; j < MAXPLAYERS; j++) {
                    if (DOOM.playeringame[j]) {
                        drawNum(x + w, y, dm_frags[i][j], 2);
                    }

                    x += DM_SPACINGX;
                }
                drawNum(DM_TOTALSX + w, y, dm_totals[i], 2);
            }
            y += WI_SPACINGY;
        }
    }

    int[] cnt_frags = new int[MAXPLAYERS];
    int dofrags;
    int ng_state;

    @SourceCode.Suspicious(CauseOfDesyncProbability.LOW)
    @WI_Stuff.C(WI_initNetgameStats)
    protected void initNetgameStats() {
        state = endlevel_state.StatCount;
        acceleratestage = 0;
        ng_state = 1;

        cnt_pause = TICRATE;

        for (int i = 0; i < MAXPLAYERS; i++) {
            if (!DOOM.playeringame[i]) {
                continue;
            }

            cnt_kills[i] = cnt_items[i] = cnt_secret[i] = cnt_frags[i] = 0;
            
            dofrags += fragSum(i);
        }

        //Suspicious - Good Sign 2017/05/08
        dofrags = ~ ~dofrags;

        WI_initAnimatedBack: {
            initAnimatedBack();
        }
    }

    protected void updateNetgameStats() {

        int i;
        int fsum;

        boolean stillticking;

        updateAnimatedBack();

        if (acceleratestage != 0 && ng_state != 10) {
            acceleratestage = 0;

            for (i = 0; i < MAXPLAYERS; i++) {
                if (!DOOM.playeringame[i]) {
                    continue;
                }

                cnt_kills[i] = (plrs[i].skills * 100) / wbs.maxkills;
                cnt_items[i] = (plrs[i].sitems * 100) / wbs.maxitems;
                cnt_secret[i] = (plrs[i].ssecret * 100) / wbs.maxsecret;

                if (dofrags != 0) {
                    cnt_frags[i] = fragSum(i);
                }
            }
            DOOM.doomSound.StartSound(null, sfxenum_t.sfx_barexp);
            ng_state = 10;
        }

        if (ng_state == 2) {
            if ((bcnt & 3) == 0) {
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_pistol);
            }

            stillticking = false;

            for (i = 0; i < MAXPLAYERS; i++) {
                if (!DOOM.playeringame[i]) {
                    continue;
                }

                cnt_kills[i] += 2;

                if (cnt_kills[i] >= (plrs[i].skills * 100) / wbs.maxkills) {
                    cnt_kills[i] = (plrs[i].skills * 100) / wbs.maxkills;
                } else {
                    stillticking = true;
                }
            }

            if (!stillticking) {
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_barexp);
                ng_state++;
            }
        } else if (ng_state == 4) {
            if ((bcnt & 3) == 0) {
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_pistol);
            }

            stillticking = false;

            for (i = 0; i < MAXPLAYERS; i++) {
                if (!DOOM.playeringame[i]) {
                    continue;
                }

                cnt_items[i] += 2;
                if (cnt_items[i] >= (plrs[i].sitems * 100) / wbs.maxitems) {
                    cnt_items[i] = (plrs[i].sitems * 100) / wbs.maxitems;
                } else {
                    stillticking = true;
                }
            }
            if (!stillticking) {
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_barexp);
                ng_state++;
            }
        } else if (ng_state == 6) {
            if ((bcnt & 3) == 0) {
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_pistol);
            }

            stillticking = false;

            for (i = 0; i < MAXPLAYERS; i++) {
                if (!DOOM.playeringame[i]) {
                    continue;
                }

                cnt_secret[i] += 2;

                if (cnt_secret[i] >= (plrs[i].ssecret * 100) / wbs.maxsecret) {
                    cnt_secret[i] = (plrs[i].ssecret * 100) / wbs.maxsecret;
                } else {
                    stillticking = true;
                }
            }

            if (!stillticking) {
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_barexp);
                ng_state += 1 + 2 * ~dofrags;
            }
        } else if (ng_state == 8) {
            if ((bcnt & 3) == 0) {
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_pistol);
            }

            stillticking = false;

            for (i = 0; i < MAXPLAYERS; i++) {
                if (!DOOM.playeringame[i]) {
                    continue;
                }

                cnt_frags[i] += 1;

                if (cnt_frags[i] >= (fsum = fragSum(i))) {
                    cnt_frags[i] = fsum;
                } else {
                    stillticking = true;
                }
            }

            if (!stillticking) {
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_pldeth);
                ng_state++;
            }
        } else if (ng_state == 10) {
            if (acceleratestage != 0) {
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_sgcock);
                if (DOOM.isCommercial()) {
                    initNoState();
                } else {
                    initShowNextLoc();
                }
            }
        } else if ((ng_state & 1) != 0) {
            if (--cnt_pause == 0) {
                ng_state++;
                cnt_pause = TICRATE;
            }
        }
    }

    protected void drawNetgameStats() {
        int i;
        int x;
        int y;
        int pwidth = percent.width;

        slamBackground();

        // draw animated background
        drawAnimatedBack();

        drawLF();

        // draw stat titles (top line)
        DOOM.graphicSystem.DrawPatchScaled(FG, kills, DOOM.vs, NG_STATSX() + NG_SPACINGX - kills.width, NG_STATSY);
        DOOM.graphicSystem.DrawPatchScaled(FG, items, DOOM.vs, NG_STATSX() + 2 * NG_SPACINGX - items.width, NG_STATSY);
        DOOM.graphicSystem.DrawPatchScaled(FG, secret, DOOM.vs, NG_STATSX() + 3 * NG_SPACINGX - secret.width, NG_STATSY);

        if (dofrags != 0) {
            DOOM.graphicSystem.DrawPatchScaled(FG, frags, DOOM.vs, NG_STATSX() + 4 * NG_SPACINGX - frags.width, NG_STATSY);
        }

        // draw stats
        y = NG_STATSY + kills.height;

        for (i = 0; i < MAXPLAYERS; i++) {
            if (!DOOM.playeringame[i]) {
                continue;
            }

            x = NG_STATSX();
            DOOM.graphicSystem.DrawPatchScaled(FG, p[i], DOOM.vs, x - p[i].width, y);

            if (i == me) {
                DOOM.graphicSystem.DrawPatchScaled(FG, star, DOOM.vs, x - p[i].width, y);
            }

            x += NG_SPACINGX;
            drawPercent((x - pwidth) * DOOM.vs.getScalingX(), (y + 10) * DOOM.vs.getScalingY(), cnt_kills[i]);
            x += NG_SPACINGX;
            drawPercent((x - pwidth) * DOOM.vs.getScalingX(), (y + 10) * DOOM.vs.getScalingY(), cnt_items[i]);
            x += NG_SPACINGX;
            drawPercent((x - pwidth) * DOOM.vs.getScalingX(), (y + 10) * DOOM.vs.getScalingY(), cnt_secret[i]);
            x += NG_SPACINGX;

            if (dofrags != 0) {
                drawNum(x * DOOM.vs.getScalingX(), (y + 10) * DOOM.vs.getScalingY(), cnt_frags[i], -1);
            }

            y += WI_SPACINGY;
        }

    }

    int sp_state;

    @SourceCode.Exact
    @WI_Stuff.C(WI_initStats)
    protected void initStats() {
        state = endlevel_state.StatCount;
        acceleratestage = 0;
        sp_state = 1;
        cnt_kills[0] = cnt_items[0] = cnt_secret[0] = -1;
        cnt_time = cnt_par = -1;
        cnt_pause = TICRATE;

        WI_initAnimatedBack: {
            initAnimatedBack();
        }
    }

    protected void updateStats() {

        updateAnimatedBack();

        //System.out.println("SP_State "+sp_state);
        if ((acceleratestage != 0) && sp_state != COUNT_DONE) {
            acceleratestage = 0;
            cnt_kills[0] = (plrs[me].skills * 100) / wbs.maxkills;
            cnt_items[0] = (plrs[me].sitems * 100) / wbs.maxitems;
            cnt_secret[0] = (plrs[me].ssecret * 100) / wbs.maxsecret;
            cnt_time = plrs[me].stime / TICRATE;
            cnt_par = wbs.partime / TICRATE;
            DOOM.doomSound.StartSound(null, sfxenum_t.sfx_barexp);
            sp_state = 10;
        }

        if (sp_state == COUNT_KILLS) {
            cnt_kills[0] += 2;

            if ((bcnt & 3) == 0) {
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_pistol);
            }

            if (cnt_kills[0] >= (plrs[me].skills * 100) / wbs.maxkills) {
                cnt_kills[0] = (plrs[me].skills * 100) / wbs.maxkills;
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_barexp);
                sp_state++;
            }
        } else if (sp_state == COUNT_ITEMS) {
            cnt_items[0] += 2;

            if ((bcnt & 3) == 0) {
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_pistol);
            }

            if (cnt_items[0] >= (plrs[me].sitems * 100) / wbs.maxitems) {
                cnt_items[0] = (plrs[me].sitems * 100) / wbs.maxitems;
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_barexp);
                sp_state++;
            }
        } else if (sp_state == COUNT_SECRETS) {
            cnt_secret[0] += 2;

            if ((bcnt & 3) == 0) {
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_pistol);
            }

            if (cnt_secret[0] >= (plrs[me].ssecret * 100) / wbs.maxsecret) {
                cnt_secret[0] = (plrs[me].ssecret * 100) / wbs.maxsecret;
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_barexp);
                sp_state++;
            }
        } else if (sp_state == COUNT_TIME) {
            if ((bcnt & 3) == 0) {
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_pistol);
            }

            cnt_time += 3;

            if (cnt_time >= plrs[me].stime / TICRATE) {
                cnt_time = plrs[me].stime / TICRATE;
            }

            cnt_par += 3;

            if (cnt_par >= wbs.partime / TICRATE) {
                cnt_par = wbs.partime / TICRATE;

                if (cnt_time >= plrs[me].stime / TICRATE) {
                    DOOM.doomSound.StartSound(null, sfxenum_t.sfx_barexp);
                    sp_state++;
                }
            }
        } else if (sp_state == COUNT_DONE) {
            if (acceleratestage != 0) {
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_sgcock);

                if (DOOM.isCommercial()) {
                    initNoState();
                } else {
                    initShowNextLoc();
                }
            }
        } // Non-drawing, pausing state. Any odd value introduces a 35 tic pause.
        else if ((sp_state & 1) > 0) {
            if (--cnt_pause == 0) {
                sp_state++;
                cnt_pause = TICRATE;
            }
        }

    }

    protected void drawStats() {
        // line height
        int lh;

        lh = (3 * num[0].height * DOOM.vs.getScalingY()) / 2;

        slamBackground();

        // draw animated background
        drawAnimatedBack();

        drawLF();

        DOOM.graphicSystem.DrawPatchScaled(FG, kills, DOOM.vs, SP_STATSX, SP_STATSY, V_NOSCALESTART);
        drawPercent(DOOM.vs.getScreenWidth() - SP_STATSX, SP_STATSY, cnt_kills[0]);

        DOOM.graphicSystem.DrawPatchScaled(FG, items, DOOM.vs, SP_STATSX, SP_STATSY + lh, V_NOSCALESTART);
        drawPercent(DOOM.vs.getScreenWidth() - SP_STATSX, SP_STATSY + lh, cnt_items[0]);

        DOOM.graphicSystem.DrawPatchScaled(FG, sp_secret, DOOM.vs, SP_STATSX, SP_STATSY + 2 * lh, V_NOSCALESTART);
        drawPercent(DOOM.vs.getScreenWidth() - SP_STATSX, SP_STATSY + 2 * lh, cnt_secret[0]);

        DOOM.graphicSystem.DrawPatchScaled(FG, time, DOOM.vs, SP_TIMEX, SP_TIMEY, V_NOSCALESTART);
        drawTime(DOOM.vs.getScreenWidth() / 2 - SP_TIMEX, SP_TIMEY, cnt_time);

        if (wbs.epsd < 3) {
            DOOM.graphicSystem.DrawPatchScaled(FG, par, DOOM.vs, DOOM.vs.getScreenWidth() / 2 + SP_TIMEX, SP_TIMEY, V_NOSCALESTART);
            drawTime(DOOM.vs.getScreenWidth() - SP_TIMEX, SP_TIMEY, cnt_par);
        }

    }

    protected void checkForAccelerate() {

        // check for button presses to skip delays
        for (int i = 0; i < MAXPLAYERS; i++) {
            player_t player = DOOM.players[i];
            if (DOOM.playeringame[i]) {
                if ((player.cmd.buttons & BT_ATTACK) != 0) {
                    if (!player.attackdown) {
                        acceleratestage = 1;
                    }
                    player.attackdown = true;
                } else {
                    player.attackdown = false;
                }
                if ((player.cmd.buttons & BT_USE) != 0) {
                    if (!player.usedown) {
                        acceleratestage = 1;
                    }
                    player.usedown = true;
                } else {
                    player.usedown = false;
                }
            }
        }
    }

    /**
     * Updates stuff each tick
     */
    public void Ticker() {
        // counter for general background animation
        bcnt++;

        if (bcnt == 1) {
            // intermission music
            if (DOOM.isCommercial()) {
                DOOM.doomSound.ChangeMusic(musicenum_t.mus_dm2int.ordinal(), true);
            } else {
                DOOM.doomSound.ChangeMusic(musicenum_t.mus_inter.ordinal(), true);
            }
        }

        checkForAccelerate();
//System.out.println("State "+state);

        switch (state) {
            case StatCount:
                if (DOOM.deathmatch) {
                    updateDeathmatchStats();
                } else if (DOOM.netgame) {
                    updateNetgameStats();
                } else {
                    updateStats();
                }
                break;

            case ShowNextLoc:
                updateShowNextLoc();
                break;

            case NoState:
                updateNoState();
                break;
            case JustShutOff:
                // We just finished, and graphics have been unloaded.
                // If we don't consume a tick in this way, Doom
                // will try to draw unloaded graphics.
                state = endlevel_state.NoState;
                break;
        }

    }

    @SourceCode.Compatible
    @WI_Stuff.C(WI_loadData)
    protected void loadData() {
        String name;
        anim_t a;

        if (DOOM.isCommercial()) {
            name = "INTERPIC";
        } else { //sprintf(name, "WIMAP%d", wbs.epsd);
            name = ("WIMAP" + Integer.toString(wbs.epsd));
        }

        // MAES: For Ultimate Doom
        if (DOOM.isRetail()) {
            if (wbs.epsd == 3) {
                name = "INTERPIC";
            }
        }

        // background - draw it to screen 1 for quick redraw.
        bg = DOOM.wadLoader.CacheLumpName(name, PU_CACHE, patch_t.class);
        DOOM.graphicSystem.DrawPatchScaled(BG, bg, DOOM.vs, 0, 0, V_SAFESCALE);

        // UNUSED unsigned char *pic = screens[1];
        // if (gamemode == commercial)
        // {
        // darken the background image
        // while (pic != screens[1] + DOOM.vs.getScreenHeight()*DOOM.vs.getScreenWidth())
        // {
        //   *pic = colormaps[256*25 + *pic];
        //   pic++;
        // }
        //}
        if (DOOM.isCommercial()) {
            NUMCMAPS = 32;

            lnames = new patch_t[NUMCMAPS];
            String xxx = "CWILV%02d";
            //String buffer;
            for (int i = 0; i < NUMCMAPS; i++) {
                name = String.format(xxx, i);
                lnames[i] = DOOM.wadLoader.CacheLumpName(name, PU_STATIC, patch_t.class);
            }
        } else {
            lnames = new patch_t[NUMMAPS];
            String xxx = "WILV%d%d";

            for (int i = 0; i < NUMMAPS; i++) {
                name = String.format(xxx, wbs.epsd, i);
                lnames[i] = DOOM.wadLoader.CacheLumpName(name, PU_STATIC, patch_t.class);
            }

            // you are here
            yah[0] = DOOM.wadLoader.CacheLumpName("WIURH0", PU_STATIC, patch_t.class);

            // you are here (alt.)
            yah[1] = DOOM.wadLoader.CacheLumpName("WIURH1", PU_STATIC, patch_t.class);

            yah[2] = null;

            // splat
            splat = new patch_t[]{DOOM.wadLoader.CacheLumpName("WISPLAT", PU_STATIC, patch_t.class), null};

            if (wbs.epsd < 3) {
                xxx = "WIA%d%02d%02d";
                //xxx=new PrintfFormat("WIA%d%.2d%.2d");
                for (int j = 0; j < NUMANIMS[wbs.epsd]; j++) {
                    a = anims[wbs.epsd][j];
                    for (int i = 0; i < a.nanims; i++) {
                        // MONDO HACK!
                        if (wbs.epsd != 1 || j != 8) {
                            // animations
                            name = String.format(xxx, wbs.epsd, j, i);
                            a.p[i] = DOOM.wadLoader.CacheLumpName(name, PU_STATIC, patch_t.class);
                        } else {
                            // HACK ALERT!
                            a.p[i] = anims[1][4].p[i];
                        }
                    }
                }
            }
        }

        // More hacks on minus sign.
        wiminus = DOOM.wadLoader.CacheLumpName("WIMINUS", PU_STATIC, patch_t.class);

        String xxx = "WINUM%d";
        for (int i = 0; i < 10; i++) {
            // numbers 0-9
            name = String.format(xxx, i);
            num[i] = DOOM.wadLoader.CacheLumpName(name, PU_STATIC, patch_t.class);
        }

        // percent sign
        percent = DOOM.wadLoader.CacheLumpName("WIPCNT", PU_STATIC, patch_t.class);

        // "finished"
        finished = DOOM.wadLoader.CacheLumpName("WIF", PU_STATIC, patch_t.class);

        // "entering"
        entering = DOOM.wadLoader.CacheLumpName("WIENTER", PU_STATIC, patch_t.class);

        // "kills"
        kills = DOOM.wadLoader.CacheLumpName("WIOSTK", PU_STATIC, patch_t.class);

        // "scrt"
        secret = DOOM.wadLoader.CacheLumpName("WIOSTS", PU_STATIC, patch_t.class);

        // "secret"
        sp_secret = DOOM.wadLoader.CacheLumpName("WISCRT2", PU_STATIC, patch_t.class);

        // Yuck. 
        if (DOOM.language == Language_t.french) {
            // "items"
            if (DOOM.netgame && !DOOM.deathmatch) {
                items = DOOM.wadLoader.CacheLumpName("WIOBJ", PU_STATIC, patch_t.class);
            } else {
                items = DOOM.wadLoader.CacheLumpName("WIOSTI", PU_STATIC, patch_t.class);
            }
        } else {
            items = DOOM.wadLoader.CacheLumpName("WIOSTI", PU_STATIC, patch_t.class);
        }

        // "frgs"
        frags = DOOM.wadLoader.CacheLumpName("WIFRGS", PU_STATIC, patch_t.class);

        // ":"
        colon = DOOM.wadLoader.CacheLumpName("WICOLON", PU_STATIC, patch_t.class);

        // "time"
        time = DOOM.wadLoader.CacheLumpName("WITIME", PU_STATIC, patch_t.class);

        // "sucks"
        sucks = DOOM.wadLoader.CacheLumpName("WISUCKS", PU_STATIC, patch_t.class);

        // "par"
        par = DOOM.wadLoader.CacheLumpName("WIPAR", PU_STATIC, patch_t.class);

        // "killers" (vertical)
        killers = DOOM.wadLoader.CacheLumpName("WIKILRS", PU_STATIC, patch_t.class);

        // "victims" (horiz)
        victims = DOOM.wadLoader.CacheLumpName("WIVCTMS", PU_STATIC, patch_t.class);

        // "total"
        total = DOOM.wadLoader.CacheLumpName("WIMSTT", PU_STATIC, patch_t.class);

        // your face
        star = DOOM.wadLoader.CacheLumpName("STFST01", PU_STATIC, patch_t.class);

        // dead face
        bstar = DOOM.wadLoader.CacheLumpName("STFDEAD0", PU_STATIC, patch_t.class);

        String xx1 = "STPB%d";
        String xx2 = "WIBP%d";
        for (int i = 0; i < MAXPLAYERS; i++) {
            // "1,2,3,4"
            name = String.format(xx1, i);
            p[i] = DOOM.wadLoader.CacheLumpName(name, PU_STATIC, patch_t.class);

            // "1,2,3,4"
            name = String.format(xx2, i + 1);
            bp[i] = DOOM.wadLoader.CacheLumpName(name, PU_STATIC, patch_t.class);
        }

    }

    /*

public void WI_unloadData()
{
    int		i;
    int		j;

    W.UnlockLumpNum(wiminus, PU_CACHE);

    for (i=0 ; i<10 ; i++)
	W.UnlockLumpNum(num[i], PU_CACHE);
    
    if (gamemode == commercial)
    {
  	for (i=0 ; i<NUMCMAPS ; i++)
	    W.UnlockLumpNum(lnames[i], PU_CACHE);
    }
    else
    {
	W.UnlockLumpNum(yah[0], PU_CACHE);
	W.UnlockLumpNum(yah[1], PU_CACHE);

	W.UnlockLumpNum(splat, PU_CACHE);

	for (i=0 ; i<NUMMAPS ; i++)
	    W.UnlockLumpNum(lnames[i], PU_CACHE);
	
	if (wbs.epsd < 3)
	{
	    for (j=0;j<NUMANIMS[wbs.epsd];j++)
	    {
		if (wbs.epsd != 1 || j != 8)
		    for (i=0;i<anims[wbs.epsd][j].nanims;i++)
			W.UnlockLumpNum(anims[wbs.epsd][j].p[i], PU_CACHE);
	    }
	}
    }
    
    Z_Free(lnames);

    W.UnlockLumpNum(percent, PU_CACHE);
    W.UnlockLumpNum(colon, PU_CACHE);
    W.UnlockLumpNum(finished, PU_CACHE);
    W.UnlockLumpNum(entering, PU_CACHE);
    W.UnlockLumpNum(kills, PU_CACHE);
    W.UnlockLumpNum(secret, PU_CACHE);
    W.UnlockLumpNum(sp_secret, PU_CACHE);
    W.UnlockLumpNum(items, PU_CACHE);
    W.UnlockLumpNum(frags, PU_CACHE);
    W.UnlockLumpNum(time, PU_CACHE);
    W.UnlockLumpNum(sucks, PU_CACHE);
    W.UnlockLumpNum(par, PU_CACHE);

    W.UnlockLumpNum(victims, PU_CACHE);
    W.UnlockLumpNum(killers, PU_CACHE);
    W.UnlockLumpNum(total, PU_CACHE);
    //  W.UnlockLumpNum(star, PU_CACHE);
    //  W.UnlockLumpNum(bstar, PU_CACHE);
    
    for (i=0 ; i<MAXPLAYERS ; i++)
	W.UnlockLumpNum(p[i], PU_CACHE);

    for (i=0 ; i<MAXPLAYERS ; i++)
	W.UnlockLumpNum(bp[i], PU_CACHE);
}
     */
    public void Drawer() {
        switch (state) {
            case StatCount:
                if (DOOM.deathmatch) {
                    drawDeathmatchStats();
                } else if (DOOM.netgame) {
                    drawNetgameStats();
                } else {
                    drawStats();
                }
                break;

            case ShowNextLoc:
                drawShowNextLoc();
                break;

            case NoState:
                drawNoState();
                break;
                
            default:
            	break;
        }
    }

    @SourceCode.Compatible
    @WI_Stuff.C(WI_initVariables)
    protected void initVariables(wbstartstruct_t wbstartstruct) {
        wbs = wbstartstruct.clone();

        if (RANGECHECKING) {
            if (!DOOM.isCommercial()) {
                if (DOOM.isRetail()) {
                    RNGCHECK(wbs.epsd, 0, 3);
                } else {
                    RNGCHECK(wbs.epsd, 0, 2);
                }
            } else {
                RNGCHECK(wbs.last, 0, 8);
                RNGCHECK(wbs.next, 0, 8);
            }
            RNGCHECK(wbs.pnum, 0, MAXPLAYERS);
            RNGCHECK(wbs.pnum, 0, MAXPLAYERS);
        }

        acceleratestage = 0;
        cnt = bcnt = 0;
        firstrefresh = 1;
        me = wbs.pnum;
        plrs = wbs.plyr.clone();

        if (wbs.maxkills == 0) {
            wbs.maxkills = 1;
        }

        if (wbs.maxitems == 0) {
            wbs.maxitems = 1;
        }

        if (wbs.maxsecret == 0) {
            wbs.maxsecret = 1;
        }

        // Sanity check for Ultimate.
        if (!DOOM.isRetail()) {
            if (wbs.epsd > 2) {
                wbs.epsd -= 3;
            }
        }
    }

    @SourceCode.Exact
    @WI_Stuff.C(WI_Start)
    public void Start(wbstartstruct_t wbstartstruct) {
        WI_initVariables: {
            initVariables(wbstartstruct);
        }
        WI_loadData: {
            loadData();
        }

        if (DOOM.deathmatch) {
            WI_initDeathmatchStats: {
                initDeathmatchStats();
            }
        } else if (DOOM.netgame) {
            WI_initNetgameStats: {
                initNetgameStats();
            }
        } else {
            WI_initStats: {
                initStats();
            }
        }
    }

    protected int NG_STATSX() {
        return 32 + star.width / 2 + 32 * (!(dofrags > 0) ? 1 : 0);
    }

    protected static boolean RNGCHECK(int what, int min, int max) {
        return (what >= min && what <= max);
    }
}

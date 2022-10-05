/*
 * Copyright (C) 1993-1996 by id Software, Inc.
 * Copyright (C) 2017 Good Sign
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bleachhack.util.doom.p.Actions;

import org.bleachhack.util.doom.automap.IAutoMap;
import static org.bleachhack.util.doom.data.Limits.MAXRADIUS;
import static org.bleachhack.util.doom.data.Limits.MAXSPECIALCROSS;
import org.bleachhack.util.doom.data.sounds;
import org.bleachhack.util.doom.defines.skill_t;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.SourceCode;
import org.bleachhack.util.doom.doom.SourceCode.P_Map;
import static org.bleachhack.util.doom.doom.SourceCode.P_Map.PIT_CheckLine;
import static org.bleachhack.util.doom.doom.SourceCode.P_Map.P_CheckPosition;
import org.bleachhack.util.doom.doom.SourceCode.P_MapUtl;
import static org.bleachhack.util.doom.doom.SourceCode.P_MapUtl.P_BlockLinesIterator;
import static org.bleachhack.util.doom.doom.SourceCode.P_MapUtl.P_BlockThingsIterator;
import org.bleachhack.util.doom.doom.SourceCode.fixed_t;
import org.bleachhack.util.doom.doom.player_t;
import org.bleachhack.util.doom.hu.IHeadsUp;
import org.bleachhack.util.doom.i.IDoomSystem;
import java.util.function.Predicate;
import static org.bleachhack.util.doom.m.BBox.BOXBOTTOM;
import static org.bleachhack.util.doom.m.BBox.BOXLEFT;
import static org.bleachhack.util.doom.m.BBox.BOXRIGHT;
import static org.bleachhack.util.doom.m.BBox.BOXTOP;
import org.bleachhack.util.doom.p.AbstractLevelLoader;
import static org.bleachhack.util.doom.p.AbstractLevelLoader.FIX_BLOCKMAP_512;
import org.bleachhack.util.doom.p.ThinkerList;
import org.bleachhack.util.doom.p.UnifiedGameMap;
import org.bleachhack.util.doom.p.intercept_t;
import org.bleachhack.util.doom.p.mobj_t;
import static org.bleachhack.util.doom.p.mobj_t.MF_MISSILE;
import static org.bleachhack.util.doom.p.mobj_t.MF_NOCLIP;
import org.bleachhack.util.doom.rr.SceneRenderer;
import org.bleachhack.util.doom.rr.line_t;
import static org.bleachhack.util.doom.rr.line_t.ML_BLOCKING;
import static org.bleachhack.util.doom.rr.line_t.ML_BLOCKMONSTERS;
import org.bleachhack.util.doom.rr.sector_t;
import org.bleachhack.util.doom.rr.subsector_t;
import org.bleachhack.util.doom.s.ISoundOrigin;
import org.bleachhack.util.doom.st.IDoomStatusBar;
import org.bleachhack.util.doom.utils.C2JUtils;
import static org.bleachhack.util.doom.utils.C2JUtils.eval;
import org.bleachhack.util.doom.utils.TraitFactory;
import org.bleachhack.util.doom.utils.TraitFactory.ContextKey;
import org.bleachhack.util.doom.utils.TraitFactory.Trait;

public interface ActionTrait extends Trait, ThinkerList {
    TraitFactory.KeyChain ACTION_KEY_CHAIN = new TraitFactory.KeyChain();

    ContextKey<SlideMove> KEY_SLIDEMOVE = ACTION_KEY_CHAIN.newKey(ActionTrait.class, SlideMove::new);
    ContextKey<Spechits> KEY_SPECHITS = ACTION_KEY_CHAIN.newKey(ActionTrait.class, Spechits::new);
    ContextKey<Movement> KEY_MOVEMENT = ACTION_KEY_CHAIN.newKey(ActionTrait.class, Movement::new);
    
    AbstractLevelLoader levelLoader();
    IHeadsUp headsUp();
    IDoomSystem doomSystem();
    IDoomStatusBar statusBar();
    IAutoMap<?, ?> autoMap();
    SceneRenderer<?, ?> sceneRenderer();

    UnifiedGameMap.Specials getSpecials();
    UnifiedGameMap.Switches getSwitches();
    ActionsThinkers getThinkers();
    ActionsEnemies getEnemies();
    ActionsAttacks getAttacks();

    void StopSound(ISoundOrigin origin); // DOOM.doomSound.StopSound
    void StartSound(ISoundOrigin origin, sounds.sfxenum_t s); // DOOM.doomSound.StartSound
    void StartSound(ISoundOrigin origin, int s); // DOOM.doomSound.StartSound

    player_t getPlayer(int number); //DOOM.players[]
    skill_t getGameSkill(); // DOOM.gameskill
    mobj_t createMobj(); // mobj_t.from(DOOM);

    int LevelTime(); // DOOM.leveltime
    int P_Random();
    int ConsolePlayerNumber(); // DOOM.consoleplayer
    int MapNumber(); // DOOM.gamemap
    boolean PlayerInGame(int number); // DOOM.palyeringame
    boolean IsFastParm(); // DOOM.fastparm
    boolean IsPaused(); // DOOM.paused
    boolean IsNetGame(); // DOOM.netgame
    boolean IsDemoPlayback(); // DOOM.demoplayback
    boolean IsDeathMatch(); // DOOM.deathmatch
    boolean IsAutoMapActive(); // DOOM.automapactive
    boolean IsMenuActive(); // DOOM.menuactive
    boolean CheckThing(mobj_t m);
    boolean StompThing(mobj_t m);
        
    default void SetThingPosition(mobj_t mobj) {
        levelLoader().SetThingPosition(mobj);
    }

    /**
     * Try to avoid.
     */
    DoomMain<?, ?> DOOM();
    
    final class SlideMove {
        //
        // SLIDE MOVE
        // Allows the player to slide along any angled walls.
        //
        mobj_t slidemo;
        
        @fixed_t
        int bestslidefrac, secondslidefrac;
        
        line_t bestslideline, secondslideline;
        
        @fixed_t
        int tmxmove, tmymove;
    }
    
    final class Spechits {
        line_t[] spechit = new line_t[MAXSPECIALCROSS];
        int numspechit;
        
        //
        // USE LINES
        //
        mobj_t usething;
    }
    
    ///////////////// MOVEMENT'S ACTIONS ////////////////////////
    final class Movement {
        /**
         * If "floatok" true, move would be ok if within "tmfloorz - tmceilingz".
         */
        public boolean floatok;
        
        @fixed_t
        public int tmfloorz,
                   tmceilingz,
                   tmdropoffz;
        
        // keep track of the line that lowers the ceiling,
        // so missiles don't explode against sky hack walls
        public line_t ceilingline;
        @fixed_t
        int[] tmbbox = new int[4];
        
        mobj_t tmthing;
        
        long tmflags;
        
        @fixed_t
        int tmx, tmy;
        
        ////////////////////// FROM p_maputl.c ////////////////////
        @fixed_t
        int opentop, openbottom, openrange, lowfloor;
    }
    
    /**
     * P_LineOpening Sets opentop and openbottom to the window through a two
     * sided line. OPTIMIZE: keep this precalculated
     */

    default void LineOpening(line_t linedef) {
        final Movement ma = contextRequire(KEY_MOVEMENT);
        sector_t front;
        sector_t back;

        if (linedef.sidenum[1] == line_t.NO_INDEX) {
            // single sided line
            ma.openrange = 0;
            return;
        }

        front = linedef.frontsector;
        back = linedef.backsector;

        if (front.ceilingheight < back.ceilingheight) {
            ma.opentop = front.ceilingheight;
        } else {
            ma.opentop = back.ceilingheight;
        }

        if (front.floorheight > back.floorheight) {
            ma.openbottom = front.floorheight;
            ma.lowfloor = back.floorheight;
        } else {
            ma.openbottom = back.floorheight;
            ma.lowfloor = front.floorheight;
        }

        ma.openrange = ma.opentop - ma.openbottom;
    }

    //
    //P_BlockThingsIterator
    //
    @SourceCode.Exact
    @P_MapUtl.C(P_BlockThingsIterator)
    default boolean BlockThingsIterator(int x, int y, Predicate<mobj_t> func) {
        final AbstractLevelLoader ll = levelLoader();
        mobj_t mobj;

        if (x < 0 || y < 0 || x >= ll.bmapwidth || y >= ll.bmapheight) {
            return true;
        }

        for (mobj = ll.blocklinks[y * ll.bmapwidth + x]; mobj != null; mobj = (mobj_t) mobj.bnext) {
            if (!func.test(mobj)) {
                return false;
            }
        }
        return true;
    }

    //
    // SECTOR HEIGHT CHANGING
    // After modifying a sectors floor or ceiling height,
    // call this routine to adjust the positions
    // of all things that touch the sector.
    //
    // If anything doesn't fit anymore, true will be returned.
    // If crunch is true, they will take damage
    //  as they are being crushed.
    // If Crunch is false, you should set the sector height back
    //  the way it was and call P_ChangeSector again
    //  to undo the changes.
    //

    /**
     * P_BlockLinesIterator The validcount flags are used to avoid checking lines that are marked in multiple mapblocks,
     * so increment validcount before the first call to P_BlockLinesIterator, then make one or more calls to it.
     */
    @P_MapUtl.C(P_BlockLinesIterator)
    default boolean BlockLinesIterator(int x, int y, Predicate<line_t> func) {
        final AbstractLevelLoader ll = levelLoader();
        final SceneRenderer<?, ?> sr = sceneRenderer();
        int offset;
        int lineinblock;
        line_t ld;

        if (x < 0 || y < 0 || x >= ll.bmapwidth || y >= ll.bmapheight) {
            return true;
        }

        // This gives us the index to look up (in blockmap)
        offset = y * ll.bmapwidth + x;

        // The index contains yet another offset, but this time 
        offset = ll.blockmap[offset];

        // MAES: blockmap terminating marker is always -1
        @SourceCode.Compatible("validcount")
        final int validcount = sr.getValidCount();

        // [SYNC ISSUE]: don't skip offset+1 :-/
        for (
            @SourceCode.Compatible("list = blockmaplump+offset ; *list != -1 ; list++")
            int list = offset; (lineinblock = ll.blockmap[list]) != -1; list++
        ) {
            ld = ll.lines[lineinblock];
            //System.out.println(ld);
            if (ld.validcount == validcount) {
                continue;   // line has already been checked
            }
            ld.validcount = validcount;
            if (!func.test(ld)) {
                return false;
            }
        }
        return true;    // everything was checked
    }

    // keep track of the line that lowers the ceiling,
    // so missiles don't explode against sky hack walls
    default void ResizeSpechits() {
        final Spechits spechits = contextRequire(KEY_SPECHITS);
        spechits.spechit = C2JUtils.resize(spechits.spechit[0], spechits.spechit, spechits.spechit.length * 2);
    }
    
    /**
     * PIT_CheckLine Adjusts tmfloorz and tmceilingz as lines are contacted
     *
     */
    @P_Map.C(PIT_CheckLine) default boolean CheckLine(line_t ld) {
        final Spechits spechits = contextRequire(KEY_SPECHITS);
        final Movement ma = contextRequire(KEY_MOVEMENT);
        
        if (ma.tmbbox[BOXRIGHT] <= ld.bbox[BOXLEFT]
        || ma.tmbbox[BOXLEFT] >= ld.bbox[BOXRIGHT]
        || ma.tmbbox[BOXTOP] <= ld.bbox[BOXBOTTOM]
        || ma.tmbbox[BOXBOTTOM] >= ld.bbox[BOXTOP])
        {
            return true;
        }

        if (ld.BoxOnLineSide(ma.tmbbox) != -1) {
            return true;
        }

        // A line has been hit
        // The moving thing's destination position will cross
        // the given line.
        // If this should not be allowed, return false.
        // If the line is special, keep track of it
        // to process later if the move is proven ok.
        // NOTE: specials are NOT sorted by order,
        // so two special lines that are only 8 pixels apart
        // could be crossed in either order.
        if (ld.backsector == null) {
            return false;       // one sided line
        }
        if (!eval(ma.tmthing.flags & MF_MISSILE)) {
            if (eval(ld.flags & ML_BLOCKING)) {
                return false;   // explicitly blocking everything
            }
            if ((ma.tmthing.player == null) && eval(ld.flags & ML_BLOCKMONSTERS)) {
                return false;   // block monsters only
            }
        }

        // set openrange, opentop, openbottom
        LineOpening(ld);

        // adjust floor / ceiling heights
        if (ma.opentop < ma.tmceilingz) {
            ma.tmceilingz = ma.opentop;
            ma.ceilingline = ld;
        }

        if (ma.openbottom > ma.tmfloorz) {
            ma.tmfloorz = ma.openbottom;
        }

        if (ma.lowfloor < ma.tmdropoffz) {
            ma.tmdropoffz = ma.lowfloor;
        }

        // if contacted a special line, add it to the list
        if (ld.special != 0) {
            spechits.spechit[spechits.numspechit] = ld;
            spechits.numspechit++;
            // Let's be proactive about this.
            if (spechits.numspechit >= spechits.spechit.length) {
                this.ResizeSpechits();
            }
        }

        return true;
    };

    //
    // MOVEMENT CLIPPING
    //
    /**
     * P_CheckPosition This is purely informative, nothing is modified (except things picked up).
     *
     * in: a mobj_t (can be valid or invalid) a position to be checked (doesn't need to be related to the mobj_t.x,y)
     *
     * during: special things are touched if MF_PICKUP early out on solid lines?
     *
     * out: newsubsec floorz ceilingz tmdropoffz the lowest point contacted (monsters won't move to a dropoff)
     * speciallines[] numspeciallines
     *
     * @param thing
     * @param x fixed_t
     * @param y fixed_t
     */
    @SourceCode.Compatible
    @P_Map.C(P_CheckPosition)
    default boolean CheckPosition(mobj_t thing, @fixed_t int x, @fixed_t int y) {
        final AbstractLevelLoader ll = levelLoader();
        final Spechits spechits = contextRequire(KEY_SPECHITS);
        final Movement ma = contextRequire(KEY_MOVEMENT);
        int xl;
        int xh;
        int yl;
        int yh;
        int bx;
        int by;
        subsector_t newsubsec;

        ma.tmthing = thing;
        ma.tmflags = thing.flags;

        ma.tmx = x;
        ma.tmy = y;

        ma.tmbbox[BOXTOP] = y + ma.tmthing.radius;
        ma.tmbbox[BOXBOTTOM] = y - ma.tmthing.radius;
        ma.tmbbox[BOXRIGHT] = x + ma.tmthing.radius;
        ma.tmbbox[BOXLEFT] = x - ma.tmthing.radius;

        R_PointInSubsector: {
            newsubsec = levelLoader().PointInSubsector(x, y);
        }
        ma.ceilingline = null;

        // The base floor / ceiling is from the subsector
        // that contains the point.
        // Any contacted lines the step closer together
        // will adjust them.
        ma.tmfloorz = ma.tmdropoffz = newsubsec.sector.floorheight;
        ma.tmceilingz = newsubsec.sector.ceilingheight;

        sceneRenderer().increaseValidCount(1);
        spechits.numspechit = 0;

        if (eval(ma.tmflags & MF_NOCLIP)) {
            return true;
        }

        // Check things first, possibly picking things up.
        // The bounding box is extended by MAXRADIUS
        // because mobj_ts are grouped into mapblocks
        // based on their origin point, and can overlap
        // into adjacent blocks by up to MAXRADIUS units.
        xl = ll.getSafeBlockX(ma.tmbbox[BOXLEFT] - ll.bmaporgx - MAXRADIUS);
        xh = ll.getSafeBlockX(ma.tmbbox[BOXRIGHT] - ll.bmaporgx + MAXRADIUS);
        yl = ll.getSafeBlockY(ma.tmbbox[BOXBOTTOM] - ll.bmaporgy - MAXRADIUS);
        yh = ll.getSafeBlockY(ma.tmbbox[BOXTOP] - ll.bmaporgy + MAXRADIUS);

        for (bx = xl; bx <= xh; bx++) {
            for (by = yl; by <= yh; by++) {
                P_BlockThingsIterator: {
                    if (!BlockThingsIterator(bx, by, this::CheckThing)) {
                        return false;
                    }
                }
            }
        }

        // check lines
        xl = ll.getSafeBlockX(ma.tmbbox[BOXLEFT] - ll.bmaporgx);
        xh = ll.getSafeBlockX(ma.tmbbox[BOXRIGHT] - ll.bmaporgx);
        yl = ll.getSafeBlockY(ma.tmbbox[BOXBOTTOM] - ll.bmaporgy);
        yh = ll.getSafeBlockY(ma.tmbbox[BOXTOP] - ll.bmaporgy);

        if (FIX_BLOCKMAP_512) {
            // Maes's quick and dirty blockmap extension hack
            // E.g. for an extension of 511 blocks, max negative is -1.
            // A full 512x512 blockmap doesn't have negative indexes.
            if (xl <= ll.blockmapxneg) {
                xl = 0x1FF & xl;         // Broke width boundary
            }
            if (xh <= ll.blockmapxneg) {
                xh = 0x1FF & xh;    // Broke width boundary
            }
            if (yl <= ll.blockmapyneg) {
                yl = 0x1FF & yl;        // Broke height boundary
            }
            if (yh <= ll.blockmapyneg) {
                yh = 0x1FF & yh;   // Broke height boundary     
            }
        }
        for (bx = xl; bx <= xh; bx++) {
            for (by = yl; by <= yh; by++) {
                P_BlockLinesIterator: {
                    if (!this.BlockLinesIterator(bx, by, this::CheckLine)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
    
    //
    // P_ThingHeightClip
    // Takes a valid thing and adjusts the thing.floorz,
    // thing.ceilingz, and possibly thing.z.
    // This is called for all nearby monsters
    // whenever a sector changes height.
    // If the thing doesn't fit,
    // the z will be set to the lowest value
    // and false will be returned.
    //
    default boolean ThingHeightClip(mobj_t thing) {
        final Movement ma = contextRequire(KEY_MOVEMENT);
        boolean onfloor;

        onfloor = (thing.z == thing.floorz);

        this.CheckPosition(thing, thing.x, thing.y);
        // what about stranding a monster partially off an edge?

        thing.floorz = ma.tmfloorz;
        thing.ceilingz = ma.tmceilingz;

        if (onfloor) {
            // walking monsters rise and fall with the floor
            thing.z = thing.floorz;
        } else {
            // don't adjust a floating monster unless forced to
            if (thing.z + thing.height > thing.ceilingz) {
                thing.z = thing.ceilingz - thing.height;
            }
        }

        return thing.ceilingz - thing.floorz >= thing.height;
    }
    
    default boolean isblocking(intercept_t in, line_t li) {
        final SlideMove slideMove = contextRequire(KEY_SLIDEMOVE);
        // the line does block movement,
        // see if it is closer than best so far

        if (in.frac < slideMove.bestslidefrac) {
            slideMove.secondslidefrac = slideMove.bestslidefrac;
            slideMove.secondslideline = slideMove.bestslideline;
            slideMove.bestslidefrac = in.frac;
            slideMove.bestslideline = li;
        }

        return false;   // stop
    }
}

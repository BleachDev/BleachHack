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

import static org.bleachhack.util.doom.data.Defines.NF_SUBSECTOR;
import static org.bleachhack.util.doom.data.Defines.RANGECHECK;
import org.bleachhack.util.doom.doom.SourceCode.fixed_t;
import static org.bleachhack.util.doom.m.fixed_t.FixedDiv;
import org.bleachhack.util.doom.p.AbstractLevelLoader;
import org.bleachhack.util.doom.p.MapUtils;
import org.bleachhack.util.doom.p.divline_t;
import org.bleachhack.util.doom.p.mobj_t;
import org.bleachhack.util.doom.rr.SceneRenderer;
import org.bleachhack.util.doom.rr.line_t;
import static org.bleachhack.util.doom.rr.line_t.ML_TWOSIDED;
import org.bleachhack.util.doom.rr.node_t;
import org.bleachhack.util.doom.rr.sector_t;
import org.bleachhack.util.doom.rr.subsector_t;
import static org.bleachhack.util.doom.utils.C2JUtils.eval;
import static org.bleachhack.util.doom.utils.C2JUtils.flags;
import org.bleachhack.util.doom.utils.TraitFactory.ContextKey;

public interface ActionsSight extends ActionsSectors {

    ContextKey<Sight> KEY_SIGHT = ACTION_KEY_CHAIN.newKey(ActionsSight.class, Sight::new);

    class Sight {

        int sightzstart; // eye z of looker
        divline_t strace = new divline_t();
        ; // from t1 to t2
        int t2x, t2y;
        int[] sightcounts = new int[2];
    }

    /**
     * P_CheckSight Returns true if a straight line between t1 and t2 is
     * unobstructed. Uses REJECT.
     */
    default boolean CheckSight(mobj_t t1, mobj_t t2) {
        final AbstractLevelLoader ll = levelLoader();
        final Sight sight = contextRequire(KEY_SIGHT);
        final Spawn spawn = contextRequire(KEY_SPAWN);

        int s1;
        int s2;
        int pnum;
        int bytenum;
        int bitnum;

        // First check for trivial rejection.
        // Determine subsector entries in REJECT table.
        s1 = t1.subsector.sector.id; // (t1.subsector.sector - sectors);
        s2 = t2.subsector.sector.id;// - sectors);
        pnum = s1 * ll.numsectors + s2;
        bytenum = pnum >> 3;
        bitnum = 1 << (pnum & 7);

        // Check in REJECT table.
        if (eval(ll.rejectmatrix[bytenum] & bitnum)) {
            sight.sightcounts[0]++;

            // can't possibly be connected
            return false;
        }

        // An unobstructed LOS is possible.
        // Now look from eyes of t1 to any part of t2.
        sight.sightcounts[1]++;

        sceneRenderer().increaseValidCount(1);

        sight.sightzstart = t1.z + t1.height - (t1.height >> 2);
        spawn.topslope = (t2.z + t2.height) - sight.sightzstart;
        spawn.bottomslope = (t2.z) - sight.sightzstart;

        sight.strace.x = t1.x;
        sight.strace.y = t1.y;
        sight.t2x = t2.x;
        sight.t2y = t2.y;
        sight.strace.dx = t2.x - t1.x;
        sight.strace.dy = t2.y - t1.y;

        // the head node is the last node output
        return CrossBSPNode(ll.numnodes - 1);
    }

    /**
     * P_CrossSubsector Returns true if strace crosses the given subsector
     * successfully.
     */
    default boolean CrossSubsector(int num) {
        final SceneRenderer<?, ?> sr = sceneRenderer();
        final AbstractLevelLoader ll = levelLoader();
        final Spawn spawn = contextRequire(KEY_SPAWN);
        final Sight sight = contextRequire(KEY_SIGHT);

        int seg; // pointer inside segs
        line_t line;
        int s1;
        int s2;
        int count;
        subsector_t sub;
        sector_t front;
        sector_t back;
        @fixed_t
        int opentop;
        int openbottom;
        divline_t divl = new divline_t();
        //vertex_t v1;
        //vertex_t v2;
        @fixed_t
        int frac;
        int slope;

        if (RANGECHECK) {
            if (num >= ll.numsubsectors) {
                doomSystem().Error("P_CrossSubsector: ss %d with numss = %d", num, ll.numsubsectors);
            }
        }

        sub = ll.subsectors[num];

        // check lines
        count = sub.numlines;
        seg = sub.firstline;// LL.segs[sub.firstline];

        for (; count > 0; seg++, count--) {
            line = ll.segs[seg].linedef;

            // allready checked other side?
            if (line.validcount == sr.getValidCount()) {
                continue;
            }

            line.validcount = sr.getValidCount();

            //v1 = line.v1;
            //v2 = line.v2;
            s1 = sight.strace.DivlineSide(line.v1x, line.v1y);
            s2 = sight.strace.DivlineSide(line.v2x, line.v2y);

            // line isn't crossed?
            if (s1 == s2) {
                continue;
            }

            divl.x = line.v1x;
            divl.y = line.v1y;
            divl.dx = line.v2x - line.v1x;
            divl.dy = line.v2y - line.v1y;
            s1 = divl.DivlineSide(sight.strace.x, sight.strace.y);
            s2 = divl.DivlineSide(sight.t2x, sight.t2y);

            // line isn't crossed?
            if (s1 == s2) {
                continue;
            }

            // stop because it is not two sided anyway
            // might do this after updating validcount?
            if (!flags(line.flags, ML_TWOSIDED)) {
                return false;
            }

            // crosses a two sided line
            front = ll.segs[seg].frontsector;
            back = ll.segs[seg].backsector;

            // no wall to block sight with?
            if (front.floorheight == back.floorheight
                && front.ceilingheight == back.ceilingheight) {
                continue;
            }

            // possible occluder
            // because of ceiling height differences
            if (front.ceilingheight < back.ceilingheight) {
                opentop = front.ceilingheight;
            } else {
                opentop = back.ceilingheight;
            }

            // because of ceiling height differences
            if (front.floorheight > back.floorheight) {
                openbottom = front.floorheight;
            } else {
                openbottom = back.floorheight;
            }

            // quick test for totally closed doors
            if (openbottom >= opentop) {
                return false; // stop
            }

            frac = MapUtils.P_InterceptVector(sight.strace, divl);

            if (front.floorheight != back.floorheight) {
                slope = FixedDiv(openbottom - sight.sightzstart, frac);
                if (slope > spawn.bottomslope) {
                    spawn.bottomslope = slope;
                }
            }

            if (front.ceilingheight != back.ceilingheight) {
                slope = FixedDiv(opentop - sight.sightzstart, frac);
                if (slope < spawn.topslope) {
                    spawn.topslope = slope;
                }
            }

            if (spawn.topslope <= spawn.bottomslope) {
                return false; // stop
            }
        }
        // passed the subsector ok
        return true;
    }

    /**
     * P_CrossBSPNode Returns true if strace crosses the given node
     * successfully.
     */
    default boolean CrossBSPNode(int bspnum) {
        final AbstractLevelLoader ll = levelLoader();
        final Sight sight = contextRequire(KEY_SIGHT);

        node_t bsp;
        int side;

        if (eval(bspnum & NF_SUBSECTOR)) {
            if (bspnum == -1) {
                return CrossSubsector(0);
            } else {
                return CrossSubsector(bspnum & (~NF_SUBSECTOR));
            }
        }

        bsp = ll.nodes[bspnum];

        // decide which side the start point is on
        side = bsp.DivlineSide(sight.strace.x, sight.strace.y);
        if (side == 2) {
            side = 0; // an "on" should cross both sides
        }

        // cross the starting side
        if (!CrossBSPNode(bsp.children[side])) {
            return false;
        }

        // the partition plane is crossed here
        if (side == bsp.DivlineSide(sight.t2x, sight.t2y)) {
            // the line doesn't touch the other side
            return true;
        }

        // cross the ending side
        return CrossBSPNode(bsp.children[side ^ 1]);
    }
}

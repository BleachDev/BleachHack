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

import static org.bleachhack.util.doom.data.Limits.MAXRADIUS;
import org.bleachhack.util.doom.data.Tables;
import static org.bleachhack.util.doom.data.Tables.finecosine;
import static org.bleachhack.util.doom.data.Tables.finesine;
import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.data.sounds;
import org.bleachhack.util.doom.doom.SourceCode.fixed_t;
import org.bleachhack.util.doom.doom.thinker_t;
import static org.bleachhack.util.doom.m.BBox.BOXBOTTOM;
import static org.bleachhack.util.doom.m.BBox.BOXLEFT;
import static org.bleachhack.util.doom.m.BBox.BOXRIGHT;
import static org.bleachhack.util.doom.m.BBox.BOXTOP;
import org.bleachhack.util.doom.p.AbstractLevelLoader;
import org.bleachhack.util.doom.p.ActiveStates;
import org.bleachhack.util.doom.p.mobj_t;
import static org.bleachhack.util.doom.p.mobj_t.MF_MISSILE;
import org.bleachhack.util.doom.rr.line_t;
import org.bleachhack.util.doom.rr.sector_t;
import org.bleachhack.util.doom.rr.subsector_t;

public interface ActionsTeleportation extends ActionsSectors {

    void UnsetThingPosition(mobj_t mobj);

    //
    // TELEPORTATION
    //
    @Override
    default int Teleport(line_t line, int side, mobj_t thing) {
        int i;
        int tag;
        mobj_t m;
        mobj_t fog;
        int an;
        thinker_t thinker;
        sector_t sector;
        @fixed_t
        int oldx, oldy, oldz;

        // don't teleport missiles
        if ((thing.flags & MF_MISSILE) != 0) {
            return 0;
        }

        // Don't teleport if hit back of line,
        //  so you can get out of teleporter.
        if (side == 1) {
            return 0;
        }

        tag = line.tag;
        for (i = 0; i < levelLoader().numsectors; i++) {
            if (levelLoader().sectors[i].tag == tag) {
                //thinker = thinkercap.next;
                for (thinker = getThinkerCap().next; thinker != getThinkerCap(); thinker = thinker.next) {
                    // not a mobj
                    if (thinker.thinkerFunction != ActiveStates.P_MobjThinker) {
                        continue;
                    }

                    m = (mobj_t) thinker;

                    // not a teleportman
                    if (m.type != mobjtype_t.MT_TELEPORTMAN) {
                        continue;
                    }

                    sector = m.subsector.sector;
                    // wrong sector
                    if (sector.id != i) {
                        continue;
                    }

                    oldx = thing.x;
                    oldy = thing.y;
                    oldz = thing.z;

                    if (!TeleportMove(thing, m.x, m.y)) {
                        return 0;
                    }

                    thing.z = thing.floorz;  //fixme: not needed?
                    if (thing.player != null) {
                        thing.player.viewz = thing.z + thing.player.viewheight;
                        thing.player.lookdir = 0; // Reset lookdir
                    }

                    // spawn teleport fog at source and destination
                    fog = SpawnMobj(oldx, oldy, oldz, mobjtype_t.MT_TFOG);
                    StartSound(fog, sounds.sfxenum_t.sfx_telept);
                    an = Tables.toBAMIndex(m.angle);
                    fog = SpawnMobj(m.x + 20 * finecosine[an], m.y + 20 * finesine[an], thing.z, mobjtype_t.MT_TFOG);

                    // emit sound, where?
                    StartSound(fog, sounds.sfxenum_t.sfx_telept);

                    // don't move for a bit
                    if (thing.player != null) {
                        thing.reactiontime = 18;
                    }

                    thing.angle = m.angle;
                    thing.momx = thing.momy = thing.momz = 0;
                    return 1;
                }
            }
        }
        return 0;
    }

    //
    // TELEPORT MOVE
    // 
    //
    // P_TeleportMove
    //
    default boolean TeleportMove(mobj_t thing, int x, /*fixed*/ int y) {
        final Spechits spechits = contextRequire(KEY_SPECHITS);
        final AbstractLevelLoader ll = levelLoader();
        final Movement ma = contextRequire(KEY_MOVEMENT);
        int xl;
        int xh;
        int yl;
        int yh;
        int bx;
        int by;

        subsector_t newsubsec;

        // kill anything occupying the position
        ma.tmthing = thing;
        ma.tmflags = thing.flags;

        ma.tmx = x;
        ma.tmy = y;

        ma.tmbbox[BOXTOP] = y + ma.tmthing.radius;
        ma.tmbbox[BOXBOTTOM] = y - ma.tmthing.radius;
        ma.tmbbox[BOXRIGHT] = x + ma.tmthing.radius;
        ma.tmbbox[BOXLEFT] = x - ma.tmthing.radius;

        newsubsec = ll.PointInSubsector(x, y);
        ma.ceilingline = null;

        // The base floor/ceiling is from the subsector
        // that contains the point.
        // Any contacted lines the step closer together
        // will adjust them.
        ma.tmfloorz = ma.tmdropoffz = newsubsec.sector.floorheight;
        ma.tmceilingz = newsubsec.sector.ceilingheight;

        sceneRenderer().increaseValidCount(1); // This is r_main's ?
        spechits.numspechit = 0;

        // stomp on any things contacted
        xl = ll.getSafeBlockX(ma.tmbbox[BOXLEFT] - ll.bmaporgx - MAXRADIUS);
        xh = ll.getSafeBlockX(ma.tmbbox[BOXRIGHT] - ll.bmaporgx + MAXRADIUS);
        yl = ll.getSafeBlockY(ma.tmbbox[BOXBOTTOM] - ll.bmaporgy - MAXRADIUS);
        yh = ll.getSafeBlockY(ma.tmbbox[BOXTOP] - ll.bmaporgy + MAXRADIUS);

        for (bx = xl; bx <= xh; bx++) {
            for (by = yl; by <= yh; by++) {
                if (!BlockThingsIterator(bx, by, this::StompThing)) {
                    return false;
                }
            }
        }

        // the move is ok,
        // so link the thing into its new position
        UnsetThingPosition(thing);

        thing.floorz = ma.tmfloorz;
        thing.ceilingz = ma.tmceilingz;
        thing.x = x;
        thing.y = y;

        ll.SetThingPosition(thing);

        return true;
    }
}

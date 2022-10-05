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

import static org.bleachhack.util.doom.data.Defines.MISSILERANGE;
import static org.bleachhack.util.doom.data.Defines.PT_ADDLINES;
import static org.bleachhack.util.doom.data.Defines.PT_ADDTHINGS;
import static org.bleachhack.util.doom.data.Limits.MAXRADIUS;
import static org.bleachhack.util.doom.data.Tables.finecosine;
import static org.bleachhack.util.doom.data.Tables.finesine;
import static org.bleachhack.util.doom.data.info.mobjinfo;
import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.defines.statenum_t;
import org.bleachhack.util.doom.doom.SourceCode.P_Enemy;
import static org.bleachhack.util.doom.doom.SourceCode.P_Enemy.PIT_VileCheck;
import org.bleachhack.util.doom.doom.SourceCode.P_Map;
import static org.bleachhack.util.doom.doom.SourceCode.P_Map.PIT_RadiusAttack;
import static org.bleachhack.util.doom.doom.SourceCode.P_Map.PTR_ShootTraverse;
import org.bleachhack.util.doom.doom.SourceCode.angle_t;
import org.bleachhack.util.doom.doom.SourceCode.fixed_t;
import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;
import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import static org.bleachhack.util.doom.m.fixed_t.FixedDiv;
import static org.bleachhack.util.doom.m.fixed_t.FixedMul;
import org.bleachhack.util.doom.p.AbstractLevelLoader;
import org.bleachhack.util.doom.p.intercept_t;
import org.bleachhack.util.doom.p.mobj_t;
import static org.bleachhack.util.doom.p.mobj_t.MF_CORPSE;
import static org.bleachhack.util.doom.p.mobj_t.MF_NOBLOOD;
import static org.bleachhack.util.doom.p.mobj_t.MF_SHOOTABLE;
import org.bleachhack.util.doom.rr.line_t;
import static org.bleachhack.util.doom.rr.line_t.ML_TWOSIDED;
import static org.bleachhack.util.doom.utils.C2JUtils.eval;
import org.bleachhack.util.doom.utils.TraitFactory.ContextKey;

public interface ActionsAttacks extends ActionsAim, ActionsMobj, ActionsSight, ActionsShootEvents {

    ContextKey<Attacks> KEY_ATTACKS = ACTION_KEY_CHAIN.newKey(ActionsAttacks.class, Attacks::new);

    final class Attacks {

        //
        // RADIUS ATTACK
        //
        public mobj_t bombsource;
        public mobj_t bombspot;
        public int bombdamage;
        ///////////////////// PIT AND PTR FUNCTIONS //////////////////
        /**
         * PIT_VileCheck Detect a corpse that could be raised.
         */
        public mobj_t vileCorpseHit;
        public mobj_t vileObj;
        public int vileTryX;
        public int vileTryY;
    }

    //
    // P_GunShot
    //
    default void P_GunShot(mobj_t mo, boolean accurate) {
        final Spawn targ = contextRequire(KEY_SPAWN);
        long angle;
        int damage;

        damage = 5 * (P_Random() % 3 + 1);
        angle = mo.angle;

        if (!accurate) {
            angle += (P_Random() - P_Random()) << 18;
        }

        this.LineAttack(mo, angle, MISSILERANGE, targ.bulletslope, damage);
    }

    /**
     * P_LineAttack If damage == 0, it is just a test trace that will leave linetarget set.
     *
     * @param t1
     * @param angle angle_t
     * @param distance fixed_t
     * @param slope fixed_t
     * @param damage
     */
    default void LineAttack(mobj_t t1, @angle_t long angle, @fixed_t int distance, @fixed_t int slope, int damage) {
        final Spawn targ = contextRequire(KEY_SPAWN);
        int x2, y2;

        targ.shootthing = t1;
        targ.la_damage = damage;
        x2 = t1.x + (distance >> FRACBITS) * finecosine(angle);
        y2 = t1.y + (distance >> FRACBITS) * finesine(angle);
        targ.shootz = t1.z + (t1.height >> 1) + 8 * FRACUNIT;
        targ.attackrange = distance;
        targ.aimslope = slope;

        PathTraverse(t1.x, t1.y, x2, y2, PT_ADDLINES | PT_ADDTHINGS, this::ShootTraverse);
    }

    //
    // RADIUS ATTACK
    //
    /**
     * P_RadiusAttack Source is the creature that caused the explosion at spot.
     */
    default void RadiusAttack(mobj_t spot, mobj_t source, int damage) {
        final AbstractLevelLoader ll = levelLoader();
        final Attacks att = contextRequire(KEY_ATTACKS);

        int x;
        int y;

        int xl;
        int xh;
        int yl;
        int yh;

        @fixed_t
        int dist;

        dist = (damage + MAXRADIUS) << FRACBITS;
        yh = ll.getSafeBlockY(spot.y + dist - ll.bmaporgy);
        yl = ll.getSafeBlockY(spot.y - dist - ll.bmaporgy);
        xh = ll.getSafeBlockX(spot.x + dist - ll.bmaporgx);
        xl = ll.getSafeBlockX(spot.x - dist - ll.bmaporgx);
        att.bombspot = spot;
        att.bombsource = source;
        att.bombdamage = damage;

        for (y = yl; y <= yh; y++) {
            for (x = xl; x <= xh; x++) {
                BlockThingsIterator(x, y, this::RadiusAttack);
            }
        }
    }

    ///////////////////// PIT AND PTR FUNCTIONS //////////////////
    /**
     * PIT_VileCheck Detect a corpse that could be raised.
     */
    @P_Enemy.C(PIT_VileCheck)
    default boolean VileCheck(mobj_t thing) {
        final Attacks att = contextRequire(KEY_ATTACKS);

        int maxdist;
        boolean check;

        if (!eval(thing.flags & MF_CORPSE)) {
            return true;    // not a monster
        }
        if (thing.mobj_tics != -1) {
            return true;    // not lying still yet
        }
        if (thing.info.raisestate == statenum_t.S_NULL) {
            return true;    // monster doesn't have a raise state
        }
        maxdist = thing.info.radius + mobjinfo[mobjtype_t.MT_VILE.ordinal()].radius;

        if (Math.abs(thing.x - att.vileTryX) > maxdist
            || Math.abs(thing.y - att.vileTryY) > maxdist) {
            return true;        // not actually touching
        }

        att.vileCorpseHit = thing;
        att.vileCorpseHit.momx = att.vileCorpseHit.momy = 0;
        att.vileCorpseHit.height <<= 2;
        check = CheckPosition(att.vileCorpseHit, att.vileCorpseHit.x, att.vileCorpseHit.y);
        att.vileCorpseHit.height >>= 2;

        // check it doesn't fit here, or stop checking
        return !check;
    }

    /**
     * PIT_RadiusAttack "bombsource" is the creature that caused the explosion at "bombspot".
     */
    @P_Map.C(PIT_RadiusAttack)
    default boolean RadiusAttack(mobj_t thing) {
        final Attacks att = contextRequire(KEY_ATTACKS);
        @fixed_t
        int dx, dy, dist;

        if (!eval(thing.flags & MF_SHOOTABLE)) {
            return true;
        }

        // Boss spider and cyborg
        // take no damage from concussion.
        if (thing.type == mobjtype_t.MT_CYBORG || thing.type == mobjtype_t.MT_SPIDER) {
            return true;
        }

        dx = Math.abs(thing.x - att.bombspot.x);
        dy = Math.abs(thing.y - att.bombspot.y);

        dist = dx > dy ? dx : dy;
        dist = (dist - thing.radius) >> FRACBITS;

        if (dist < 0) {
            dist = 0;
        }

        if (dist >= att.bombdamage) {
            return true;    // out of range
        }
        if (CheckSight(thing, att.bombspot)) {
            // must be in direct path
            DamageMobj(thing, att.bombspot, att.bombsource, att.bombdamage - dist);
        }

        return true;
    }

    ;

    /**
     * PTR_ShootTraverse
     *
     * 9/5/2011: Accepted _D_'s fix
     */
    @P_Map.C(PTR_ShootTraverse)
    default boolean ShootTraverse(intercept_t in) {
        final Spawn targ = contextRequire(KEY_SPAWN);
        final Movement mov = contextRequire(KEY_MOVEMENT);
        @fixed_t
        int x, y, z, frac;
        line_t li;
        mobj_t th;

        @fixed_t
        int slope, dist, thingtopslope, thingbottomslope;

        if (in.isaline) {
            li = (line_t) in.d();

            if (li.special != 0) {
                ShootSpecialLine(targ.shootthing, li);
            }

            if (!eval(li.flags & ML_TWOSIDED)) {
                return gotoHitLine(in, li);
            }

            // crosses a two sided line
            LineOpening(li);

            dist = FixedMul(targ.attackrange, in.frac);

            if (li.frontsector.floorheight != li.backsector.floorheight) {
                slope = FixedDiv(mov.openbottom - targ.shootz, dist);
                if (slope > targ.aimslope) {
                    return gotoHitLine(in, li);
                }
            }

            if (li.frontsector.ceilingheight != li.backsector.ceilingheight) {
                slope = FixedDiv(mov.opentop - targ.shootz, dist);
                if (slope < targ.aimslope) {
                    return gotoHitLine(in, li);
                }
            }

            // shot continues
            return true;

        }

        // shoot a thing
        th = (mobj_t) in.d();
        if (th == targ.shootthing) {
            return true;        // can't shoot self
        }
        if (!eval(th.flags & MF_SHOOTABLE)) {
            return true;        // corpse or something
        }
        // check angles to see if the thing can be aimed at
        dist = FixedMul(targ.attackrange, in.frac);
        thingtopslope = FixedDiv(th.z + th.height - targ.shootz, dist);

        if (thingtopslope < targ.aimslope) {
            return true;        // shot over the thing
        }
        thingbottomslope = FixedDiv(th.z - targ.shootz, dist);

        if (thingbottomslope > targ.aimslope) {
            return true;        // shot under the thing
        }

        // hit thing
        // position a bit closer
        frac = in.frac - FixedDiv(10 * FRACUNIT, targ.attackrange);

        x = targ.trace.x + FixedMul(targ.trace.dx, frac);
        y = targ.trace.y + FixedMul(targ.trace.dy, frac);
        z = targ.shootz + FixedMul(targ.aimslope, FixedMul(frac, targ.attackrange));

        // Spawn bullet puffs or blod spots,
        // depending on target type.
        if (eval(((mobj_t) in.d()).flags & MF_NOBLOOD)) {
            SpawnPuff(x, y, z);
        } else {
            SpawnBlood(x, y, z, targ.la_damage);
        }

        if (targ.la_damage != 0) {
            DamageMobj(th, targ.shootthing, targ.shootthing, targ.la_damage);
        }

        // don't go any farther
        return false;
    }
}

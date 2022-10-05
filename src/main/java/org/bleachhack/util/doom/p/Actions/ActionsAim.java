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

import static org.bleachhack.util.doom.data.Defines.PT_ADDLINES;
import static org.bleachhack.util.doom.data.Defines.PT_ADDTHINGS;
import static org.bleachhack.util.doom.data.Tables.BITS32;
import static org.bleachhack.util.doom.data.Tables.finecosine;
import static org.bleachhack.util.doom.data.Tables.finesine;
import org.bleachhack.util.doom.doom.SourceCode.P_Map;
import static org.bleachhack.util.doom.doom.SourceCode.P_Map.PTR_AimTraverse;
import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;
import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import static org.bleachhack.util.doom.m.fixed_t.FixedDiv;
import static org.bleachhack.util.doom.m.fixed_t.FixedMul;
import org.bleachhack.util.doom.p.intercept_t;
import org.bleachhack.util.doom.p.mobj_t;
import static org.bleachhack.util.doom.p.mobj_t.MF_SHOOTABLE;
import org.bleachhack.util.doom.rr.line_t;
import static org.bleachhack.util.doom.rr.line_t.ML_TWOSIDED;
import static org.bleachhack.util.doom.utils.C2JUtils.eval;

public interface ActionsAim extends ActionsMissiles {

    /**
     * P_AimLineAttack
     *
     * @param t1
     * @param angle long
     * @param distance int
     */
    @Override
    default int AimLineAttack(mobj_t t1, long angle, int distance) {
        final Spawn targ = contextRequire(KEY_SPAWN);
        int x2, y2;
        targ.shootthing = t1;

        x2 = t1.x + (distance >> FRACBITS) * finecosine(angle);
        y2 = t1.y + (distance >> FRACBITS) * finesine(angle);
        targ.shootz = t1.z + (t1.height >> 1) + 8 * FRACUNIT;

        // can't shoot outside view angles
        targ.topslope = 100 * FRACUNIT / 160;
        targ.bottomslope = -100 * FRACUNIT / 160;

        targ.attackrange = distance;
        targ.linetarget = null;

        PathTraverse(t1.x, t1.y, x2, y2, PT_ADDLINES | PT_ADDTHINGS, this::AimTraverse);

        if (targ.linetarget != null) {
            return targ.aimslope;
        }

        return 0;
    }

    //
    // P_BulletSlope
    // Sets a slope so a near miss is at aproximately
    // the height of the intended target
    //
    default void P_BulletSlope(mobj_t mo) {
        final Spawn targ = contextRequire(KEY_SPAWN);
        long an;

        // see which target is to be aimed at
        // FIXME: angle can already be negative here.
        // Not a problem if it's just moving about (accumulation will work)
        // but it needs to be sanitized before being used in any function.
        an = mo.angle;
        //_D_: &BITS32 will be used later in this function, by fine(co)sine()
        targ.bulletslope = AimLineAttack(mo, an/*&BITS32*/, 16 * 64 * FRACUNIT);

        if (!eval(targ.linetarget)) {
            an += 1 << 26;
            targ.bulletslope = AimLineAttack(mo, an/*&BITS32*/, 16 * 64 * FRACUNIT);
            if (!eval(targ.linetarget)) {
                an -= 2 << 26;
                targ.bulletslope = AimLineAttack(mo, an/*&BITS32*/, 16 * 64 * FRACUNIT);
            }

            // Give it one more try, with freelook
            if (mo.player.lookdir != 0 && !eval(targ.linetarget)) {
                an += 2 << 26;
                an &= BITS32;
                targ.bulletslope = (mo.player.lookdir << FRACBITS) / 173;
            }
        }
    }

    ////////////////// PTR Traverse Interception Functions ///////////////////////
    // Height if not aiming up or down
    // ???: use slope for monsters?
    @P_Map.C(PTR_AimTraverse)
    default boolean AimTraverse(intercept_t in) {
        final Movement mov = contextRequire(KEY_MOVEMENT);
        final Spawn targ = contextRequire(KEY_SPAWN);

        line_t li;
        mobj_t th;
        int slope;
        int thingtopslope;
        int thingbottomslope;
        int dist;

        if (in.isaline) {
            li = (line_t) in.d();

            if (!eval(li.flags & ML_TWOSIDED)) {
                return false;       // stop
            }
            // Crosses a two sided line.
            // A two sided line will restrict
            // the possible target ranges.
            LineOpening(li);

            if (mov.openbottom >= mov.opentop) {
                return false;       // stop
            }
            dist = FixedMul(targ.attackrange, in.frac);

            if (li.frontsector.floorheight != li.backsector.floorheight) {
                slope = FixedDiv(mov.openbottom - targ.shootz, dist);
                if (slope > targ.bottomslope) {
                    targ.bottomslope = slope;
                }
            }

            if (li.frontsector.ceilingheight != li.backsector.ceilingheight) {
                slope = FixedDiv(mov.opentop - targ.shootz, dist);
                if (slope < targ.topslope) {
                    targ.topslope = slope;
                }
            }

            // determine whether shot continues
            return targ.topslope > targ.bottomslope;
        }

        // shoot a thing
        th = (mobj_t) in.d();
        if (th == targ.shootthing) {
            return true;            // can't shoot self
        }
        if (!eval(th.flags & MF_SHOOTABLE)) {
            return true;            // corpse or something
        }
        // check angles to see if the thing can be aimed at
        dist = FixedMul(targ.attackrange, in.frac);
        thingtopslope = FixedDiv(th.z + th.height - targ.shootz, dist);

        if (thingtopslope < targ.bottomslope) {
            return true;            // shot over the thing
        }
        thingbottomslope = FixedDiv(th.z - targ.shootz, dist);

        if (thingbottomslope > targ.topslope) {
            return true;            // shot under the thing
        }
        // this thing can be hit!
        if (thingtopslope > targ.topslope) {
            thingtopslope = targ.topslope;
        }

        if (thingbottomslope < targ.bottomslope) {
            thingbottomslope = targ.bottomslope;
        }

        targ.aimslope = (thingtopslope + thingbottomslope) / 2;
        targ.linetarget = th;

        return false;           // don't go any farther
    }

}

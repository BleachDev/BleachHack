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
package org.bleachhack.util.doom.p.Actions.ActiveStates.MonsterStates;

import org.bleachhack.util.doom.data.Tables;
import static org.bleachhack.util.doom.data.Tables.ANG180;
import static org.bleachhack.util.doom.data.Tables.BITS32;
import static org.bleachhack.util.doom.data.Tables.finecosine;
import static org.bleachhack.util.doom.data.Tables.finesine;
import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.data.sounds;
import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import static org.bleachhack.util.doom.m.fixed_t.FixedMul;
import static org.bleachhack.util.doom.m.fixed_t.MAPFRACUNIT;
import org.bleachhack.util.doom.p.Actions.ActionTrait;
import static org.bleachhack.util.doom.p.MapUtils.AproxDistance;
import org.bleachhack.util.doom.p.mobj_t;
import static org.bleachhack.util.doom.utils.C2JUtils.eval;

public interface Skels extends ActionTrait {
    int TRACEANGLE = 0xC_00_00_00;
    
    //
    // A_SkelMissile
    //
    default void A_SkelMissile(mobj_t actor) {
        mobj_t mo;

        if (actor.target == null) {
            return;
        }

        A_FaceTarget(actor);
        actor.z += 16 * FRACUNIT;    // so missile spawns higher
        mo = getAttacks().SpawnMissile(actor, actor.target, mobjtype_t.MT_TRACER);
        actor.z -= 16 * FRACUNIT;    // back to normal

        mo.x += mo.momx;
        mo.y += mo.momy;
        mo.tracer = actor.target;
    }

    default void A_SkelWhoosh(mobj_t actor) {
        if (actor.target == null) {
            return;
        }
        A_FaceTarget(actor);
        StartSound(actor, sounds.sfxenum_t.sfx_skeswg);
    }

    default void A_SkelFist(mobj_t actor) {
        int damage;

        if (actor.target == null) {
            return;
        }

        A_FaceTarget(actor);

        if (getEnemies().CheckMeleeRange(actor)) {
            damage = ((P_Random() % 10) + 1) * 6;
            StartSound(actor, sounds.sfxenum_t.sfx_skepch);
            getAttacks().DamageMobj(actor.target, actor, actor, damage);
        }
    }
    
    default void A_Tracer(mobj_t actor) {
        long exact; //angle_t
        int dist, slope; // fixed
        mobj_t dest;
        mobj_t th;
        if (eval(DOOM().gametic & 3)) {
            return;
        }
        // spawn a puff of smoke behind the rocket
        getAttacks().SpawnPuff(actor.x, actor.y, actor.z);
        th = getEnemies().SpawnMobj(actor.x - actor.momx, actor.y - actor.momy, actor.z, mobjtype_t.MT_SMOKE);
        th.momz = MAPFRACUNIT;
        th.mobj_tics -= P_Random() & 3;
        if (th.mobj_tics < 1) {
            th.mobj_tics = 1;
        }
        
        // adjust direction
        dest = actor.tracer;
        if (dest == null || dest.health <= 0) {
            return;
        }
        
        // change angle
        exact = sceneRenderer().PointToAngle2(actor.x, actor.y, dest.x, dest.y) & BITS32;
        
        // MAES: let's analyze the logic here...
        // So exact is the angle between the missile and its target.
        if (exact != actor.angle) { // missile is already headed there dead-on.
            if (exact - actor.angle > ANG180) {
                actor.angle -= TRACEANGLE;
                actor.angle &= BITS32;
                if (((exact - actor.angle) & BITS32) < ANG180) {
                    actor.angle = exact;
                }
            } else {
                actor.angle += TRACEANGLE;
                actor.angle &= BITS32;
                if (((exact - actor.angle) & BITS32) > ANG180) {
                    actor.angle = exact;
                }
            }
        }
        // MAES: fixed and sped up.
        int exact2 = Tables.toBAMIndex(actor.angle);
        actor.momx = FixedMul(actor.info.speed, finecosine[exact2]);
        actor.momy = FixedMul(actor.info.speed, finesine[exact2]);
        // change slope
        dist = AproxDistance(dest.x - actor.x, dest.y - actor.y);
        dist /= actor.info.speed;
        if (dist < 1) {
            dist = 1;
        }
        slope = (dest.z + 40 * FRACUNIT - actor.z) / dist;
        if (slope < actor.momz) {
            actor.momz -= FRACUNIT / 8;
        } else {
            actor.momz += FRACUNIT / 8;
        }
    }

    public void A_FaceTarget(mobj_t actor);

}

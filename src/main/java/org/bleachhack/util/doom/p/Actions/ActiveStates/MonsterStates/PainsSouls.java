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

import static org.bleachhack.util.doom.data.Limits.MAXSKULLS;
import org.bleachhack.util.doom.data.Tables;
import static org.bleachhack.util.doom.data.Tables.ANG180;
import static org.bleachhack.util.doom.data.Tables.ANG270;
import static org.bleachhack.util.doom.data.Tables.ANG90;
import static org.bleachhack.util.doom.data.Tables.finecosine;
import static org.bleachhack.util.doom.data.Tables.finesine;
import static org.bleachhack.util.doom.data.info.mobjinfo;
import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.doom.SourceCode.angle_t;
import org.bleachhack.util.doom.doom.SourceCode.fixed_t;
import org.bleachhack.util.doom.doom.thinker_t;
import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import static org.bleachhack.util.doom.m.fixed_t.FixedMul;
import org.bleachhack.util.doom.p.Actions.ActionTrait;
import org.bleachhack.util.doom.p.ActiveStates;
import static org.bleachhack.util.doom.p.MapUtils.AproxDistance;
import org.bleachhack.util.doom.p.mobj_t;
import static org.bleachhack.util.doom.p.mobj_t.MF_SKULLFLY;

public interface PainsSouls extends ActionTrait {
    static final int SKULLSPEED = 20 * org.bleachhack.util.doom.m.fixed_t.MAPFRACUNIT;
    
    void A_FaceTarget(mobj_t actor);
    void A_Fall(mobj_t actor);
    
    /**
     * SkullAttack
     * Fly at the player like a missile.
     */
    default void A_SkullAttack(mobj_t actor) {
        mobj_t dest;
        int an;
        int dist;

        if (actor.target == null) {
            return;
        }

        dest = actor.target;
        actor.flags |= MF_SKULLFLY;

        StartSound(actor, actor.info.attacksound);
        A_FaceTarget(actor);
        an = Tables.toBAMIndex(actor.angle);
        actor.momx = FixedMul(SKULLSPEED, finecosine[an]);
        actor.momy = FixedMul(SKULLSPEED, finesine[an]);
        dist = AproxDistance(dest.x - actor.x, dest.y - actor.y);
        dist /= SKULLSPEED;

        if (dist < 1) {
            dist = 1;
        }
        actor.momz = (dest.z + (dest.height >> 1) - actor.z) / dist;
    }

    /**
     * A_PainShootSkull
     * Spawn a lost soul and launch it at the target
     * It's not a valid callback like the others, actually.
     * No idea if some DEH patch does use it to cause
     * mayhem though.
     *
     */
    default void A_PainShootSkull(mobj_t actor, Long angle) {
        @fixed_t int x, y, z;

        mobj_t newmobj;
        @angle_t int an;
        int prestep;
        int count;
        thinker_t currentthinker;

        // count total number of skull currently on the level
        count = 0;

        currentthinker = getThinkerCap().next;
        while (currentthinker != getThinkerCap()) {
            if ((currentthinker.thinkerFunction == ActiveStates.P_MobjThinker)
                && ((mobj_t) currentthinker).type == mobjtype_t.MT_SKULL) {
                count++;
            }
            currentthinker = currentthinker.next;
        }

        // if there are allready 20 skulls on the level,
        // don't spit another one
        if (count > MAXSKULLS) {
            return;
        }

        // okay, there's playe for another one
        an = Tables.toBAMIndex(angle);

        prestep
            = 4 * FRACUNIT
            + 3 * (actor.info.radius + mobjinfo[mobjtype_t.MT_SKULL.ordinal()].radius) / 2;

        x = actor.x + FixedMul(prestep, finecosine[an]);
        y = actor.y + FixedMul(prestep, finesine[an]);
        z = actor.z + 8 * FRACUNIT;

        newmobj = getAttacks().SpawnMobj(x, y, z, mobjtype_t.MT_SKULL);

        // Check for movements.
        if (!getAttacks().TryMove(newmobj, newmobj.x, newmobj.y)) {
            // kill it immediately
            getAttacks().DamageMobj(newmobj, actor, actor, 10000);
            return;
        }

        newmobj.target = actor.target;
        A_SkullAttack(newmobj);
    }

    //
    // A_PainAttack
    // Spawn a lost soul and launch it at the target
    // 
    default void A_PainAttack(mobj_t actor) {
        if (actor.target == null) {
            return;
        }

        A_FaceTarget(actor);
        A_PainShootSkull(actor, actor.angle);
    }

    default void A_PainDie(mobj_t actor) {
        A_Fall(actor);
        A_PainShootSkull(actor, actor.angle + ANG90);
        A_PainShootSkull(actor, actor.angle + ANG180);
        A_PainShootSkull(actor, actor.angle + ANG270);
    }

}

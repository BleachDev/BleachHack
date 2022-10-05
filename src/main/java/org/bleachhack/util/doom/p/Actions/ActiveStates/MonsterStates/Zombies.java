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

import static org.bleachhack.util.doom.data.Defines.MISSILERANGE;
import org.bleachhack.util.doom.data.sounds;
import org.bleachhack.util.doom.p.Actions.ActionTrait;
import org.bleachhack.util.doom.p.mobj_t;

public interface Zombies extends ActionTrait {
    void A_FaceTarget(mobj_t actor);

    //
    // A_PosAttack
    //
    default void A_PosAttack(mobj_t actor) {
        int angle;
        int damage;
        int slope;

        if (actor.target == null) {
            return;
        }
        A_FaceTarget(actor);
        angle = (int) actor.angle;
        slope = getAttacks().AimLineAttack(actor, angle, MISSILERANGE);

        StartSound(actor, sounds.sfxenum_t.sfx_pistol);
        angle += (P_Random() - P_Random()) << 20;
        damage = ((P_Random() % 5) + 1) * 3;
        getAttacks().LineAttack(actor, angle, MISSILERANGE, slope, damage);
    }

    default void A_SPosAttack(mobj_t actor) {
        int i;
        long angle;
        long bangle;
        int damage;
        int slope;

        if (actor.target == null) {
            return;
        }

        StartSound(actor, sounds.sfxenum_t.sfx_shotgn);
        A_FaceTarget(actor);
        bangle = actor.angle;
        slope = getAttacks().AimLineAttack(actor, bangle, MISSILERANGE);

        for (i = 0; i < 3; i++) {
            angle = bangle + ((P_Random() - P_Random()) << 20);
            damage = ((P_Random() % 5) + 1) * 3;
            getAttacks().LineAttack(actor, angle, MISSILERANGE, slope, damage);
        }
    }

    default void A_CPosAttack(mobj_t actor) {
        long angle;
        long bangle;
        int damage;
        int slope;

        if (actor.target == null) {
            return;
        }

        StartSound(actor, sounds.sfxenum_t.sfx_shotgn);
        A_FaceTarget(actor);
        bangle = actor.angle;
        slope = getAttacks().AimLineAttack(actor, bangle, MISSILERANGE);

        angle = bangle + ((P_Random() - P_Random()) << 20);
        damage = ((P_Random() % 5) + 1) * 3;
        getAttacks().LineAttack(actor, angle, MISSILERANGE, slope, damage);
    }

    default void A_CPosRefire(mobj_t actor) {
        // keep firing unless target got out of sight
        A_FaceTarget(actor);

        if (P_Random() < 40) {
            return;
        }

        if (actor.target == null || actor.target.health <= 0 || !getEnemies().CheckSight(actor, actor.target)) {
            actor.SetMobjState(actor.info.seestate);
        }
    }

}

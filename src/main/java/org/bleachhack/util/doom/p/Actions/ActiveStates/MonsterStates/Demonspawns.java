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

import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.data.sounds;
import org.bleachhack.util.doom.p.Actions.ActionTrait;
import org.bleachhack.util.doom.p.mobj_t;

public interface Demonspawns extends ActionTrait {
    void A_FaceTarget(mobj_t actor);

    //
    // A_TroopAttack
    //
    default void A_TroopAttack(mobj_t actor) {
        int damage;

        if (actor.target == null) {
            return;
        }

        A_FaceTarget(actor);
        if (getEnemies().CheckMeleeRange(actor)) {
            StartSound(actor, sounds.sfxenum_t.sfx_claw);
            damage = (P_Random() % 8 + 1) * 3;
            getAttacks().DamageMobj(actor.target, actor, actor, damage);
            return;
        }

        // launch a missile
        getAttacks().SpawnMissile(actor, actor.target, mobjtype_t.MT_TROOPSHOT);
    }

    default void A_SargAttack(mobj_t actor) {
        int damage;

        if (actor.target == null) {
            return;
        }

        A_FaceTarget(actor);
        if (getEnemies().CheckMeleeRange(actor)) {
            damage = ((P_Random() % 10) + 1) * 4;
            getAttacks().DamageMobj(actor.target, actor, actor, damage);
        }
    }

    default void A_HeadAttack(mobj_t actor) {
        int damage;

        if (actor.target == null) {
            return;
        }

        A_FaceTarget(actor);
        if (getEnemies().CheckMeleeRange(actor)) {
            damage = (P_Random() % 6 + 1) * 10;
            getAttacks().DamageMobj(actor.target, actor, actor, damage);
            return;
        }

        // launch a missile
        getAttacks().SpawnMissile(actor, actor.target, mobjtype_t.MT_HEADSHOT);
    }

    default void A_CyberAttack(mobj_t actor) {
        if (actor.target == null) {
            return;
        }

        A_FaceTarget(actor);
        getAttacks().SpawnMissile(actor, actor.target, mobjtype_t.MT_ROCKET);
    }

    default void A_BruisAttack(mobj_t actor) {
        int damage;

        if (actor.target == null) {
            return;
        }

        if (getEnemies().CheckMeleeRange(actor)) {
            StartSound(actor, sounds.sfxenum_t.sfx_claw);
            damage = (P_Random() % 8 + 1) * 10;
            getAttacks().DamageMobj(actor.target, actor, actor, damage);
            return;
        }

        // launch a missile
        getAttacks().SpawnMissile(actor, actor.target, mobjtype_t.MT_BRUISERSHOT);
    }

}

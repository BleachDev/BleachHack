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
import org.bleachhack.util.doom.p.Actions.ActionTrait;
import org.bleachhack.util.doom.p.mobj_t;

public interface Spiders extends ActionTrait {
    void A_FaceTarget(mobj_t actor);
    
    default void A_SpidRefire(mobj_t actor) {
        // keep firing unless target got out of sight
        A_FaceTarget(actor);

        if (P_Random() < 10) {
            return;
        }

        if (actor.target == null || actor.target.health <= 0 || !getEnemies().CheckSight(actor, actor.target)) {
            actor.SetMobjState(actor.info.seestate);
        }
    }

    default void A_BspiAttack(mobj_t actor) {
        if (actor.target == null) {
            return;
        }

        A_FaceTarget(actor);

        // launch a missile
        getAttacks().SpawnMissile(actor, actor.target, mobjtype_t.MT_ARACHPLAZ);
    }
}

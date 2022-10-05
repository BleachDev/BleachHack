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
import static org.bleachhack.util.doom.data.Tables.finecosine;
import static org.bleachhack.util.doom.data.Tables.finesine;
import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.data.sounds;
import static org.bleachhack.util.doom.m.fixed_t.FixedMul;
import org.bleachhack.util.doom.p.Actions.ActionTrait;
import org.bleachhack.util.doom.p.mobj_t;

public interface Mancubi extends ActionTrait {
    static final long FATSPREAD = Tables.ANG90 / 8;
    
    void A_FaceTarget(mobj_t actor);

    //
    // Mancubus attack,
    // firing three missiles (bruisers)
    // in three different directions?
    // Doesn't look like it. 
    //
    default void A_FatRaise(mobj_t actor) {
        A_FaceTarget(actor);
        StartSound(actor, sounds.sfxenum_t.sfx_manatk);
    }

    default void A_FatAttack1(mobj_t actor) {
        mobj_t mo;
        int an;

        A_FaceTarget(actor);
        // Change direction  to ...
        actor.angle += FATSPREAD;
        getAttacks().SpawnMissile(actor, actor.target, mobjtype_t.MT_FATSHOT);

        mo = getAttacks().SpawnMissile(actor, actor.target, mobjtype_t.MT_FATSHOT);
        mo.angle += FATSPREAD;
        an = Tables.toBAMIndex(mo.angle);
        mo.momx = FixedMul(mo.info.speed, finecosine[an]);
        mo.momy = FixedMul(mo.info.speed, finesine[an]);
    }

    default void A_FatAttack2(mobj_t actor) {
        mobj_t mo;
        int an;

        A_FaceTarget(actor);
        // Now here choose opposite deviation.
        actor.angle -= FATSPREAD;
        getAttacks().SpawnMissile(actor, actor.target, mobjtype_t.MT_FATSHOT);

        mo = getAttacks().SpawnMissile(actor, actor.target, mobjtype_t.MT_FATSHOT);
        mo.angle -= FATSPREAD * 2;
        an = Tables.toBAMIndex(mo.angle);
        mo.momx = FixedMul(mo.info.speed, finecosine[an]);
        mo.momy = FixedMul(mo.info.speed, finesine[an]);
    }

    default void A_FatAttack3(mobj_t actor) {
        mobj_t mo;
        int an;

        A_FaceTarget(actor);

        mo = getAttacks().SpawnMissile(actor, actor.target, mobjtype_t.MT_FATSHOT);
        mo.angle -= FATSPREAD / 2;
        an = Tables.toBAMIndex(mo.angle);
        mo.momx = FixedMul(mo.info.speed, finecosine[an]);
        mo.momy = FixedMul(mo.info.speed, finesine[an]);

        mo = getAttacks().SpawnMissile(actor, actor.target, mobjtype_t.MT_FATSHOT);
        mo.angle += FATSPREAD / 2;
        an = Tables.toBAMIndex(mo.angle);
        mo.momx = FixedMul(mo.info.speed, finecosine[an]);
        mo.momy = FixedMul(mo.info.speed, finesine[an]);
    }

}

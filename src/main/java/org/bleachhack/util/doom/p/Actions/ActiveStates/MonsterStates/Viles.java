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

import static org.bleachhack.util.doom.data.Limits.MAXRADIUS;
import static org.bleachhack.util.doom.data.Tables.finecosine;
import static org.bleachhack.util.doom.data.Tables.finesine;
import org.bleachhack.util.doom.data.mobjinfo_t;
import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.data.sounds;
import org.bleachhack.util.doom.defines.statenum_t;
import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import static org.bleachhack.util.doom.m.fixed_t.FixedMul;
import static org.bleachhack.util.doom.m.fixed_t.MAPFRACUNIT;
import org.bleachhack.util.doom.p.AbstractLevelLoader;
import org.bleachhack.util.doom.p.Actions.ActionTrait;
import org.bleachhack.util.doom.p.Actions.ActionsAttacks;
import org.bleachhack.util.doom.p.Actions.ActionsAttacks.Attacks;
import static org.bleachhack.util.doom.p.Actions.ActionsAttacks.KEY_ATTACKS;
import static org.bleachhack.util.doom.p.ChaseDirections.DI_NODIR;
import static org.bleachhack.util.doom.p.ChaseDirections.xspeed;
import static org.bleachhack.util.doom.p.ChaseDirections.yspeed;
import org.bleachhack.util.doom.p.mobj_t;

public interface Viles extends ActionTrait {
    void A_FaceTarget(mobj_t actor);
    void A_Chase(mobj_t actor);

    //
    // A_VileChase
    // Check for ressurecting a body
    //
    default void A_VileChase(mobj_t actor) {
        final AbstractLevelLoader ll = levelLoader();
        final ActionsAttacks actionsAttacks = getAttacks();
        final Attacks att = actionsAttacks.contextRequire(KEY_ATTACKS);
        
        int xl;
        int xh;
        int yl;
        int yh;

        int bx;
        int by;

        mobjinfo_t info;
        mobj_t temp;

        if (actor.movedir != DI_NODIR) {
            // check for corpses to raise
            att.vileTryX = actor.x + actor.info.speed * xspeed[actor.movedir];
            att.vileTryY = actor.y + actor.info.speed * yspeed[actor.movedir];

            xl = ll.getSafeBlockX(att.vileTryX - ll.bmaporgx - MAXRADIUS * 2);
            xh = ll.getSafeBlockX(att.vileTryX - ll.bmaporgx + MAXRADIUS * 2);
            yl = ll.getSafeBlockY(att.vileTryY - ll.bmaporgy - MAXRADIUS * 2);
            yh = ll.getSafeBlockY(att.vileTryY - ll.bmaporgy + MAXRADIUS * 2);

            att.vileObj = actor;
            for (bx = xl; bx <= xh; bx++) {
                for (by = yl; by <= yh; by++) {
                    // Call PIT_VileCheck to check
                    // whether object is a corpse
                    // that can be raised.
                    if (!BlockThingsIterator(bx, by, actionsAttacks::VileCheck)) {
                        // got one!
                        temp = actor.target;
                        actor.target = att.vileCorpseHit;
                        A_FaceTarget(actor);
                        actor.target = temp;

                        actor.SetMobjState(statenum_t.S_VILE_HEAL1);
                        StartSound(att.vileCorpseHit, sounds.sfxenum_t.sfx_slop);
                        info = att.vileCorpseHit.info;

                        att.vileCorpseHit.SetMobjState(info.raisestate);
                        att.vileCorpseHit.height <<= 2;
                        att.vileCorpseHit.flags = info.flags;
                        att.vileCorpseHit.health = info.spawnhealth;
                        att.vileCorpseHit.target = null;

                        return;
                    }
                }
            }
        }

        // Return to normal attack.
        A_Chase(actor);
    }

    //
    // A_VileStart
    //
    default void A_VileStart(mobj_t actor) {
        StartSound(actor, sounds.sfxenum_t.sfx_vilatk);
    }
    
    //
    // A_Fire
    // Keep fire in front of player unless out of sight
    //
    default void A_StartFire(mobj_t actor) {
        StartSound(actor, sounds.sfxenum_t.sfx_flamst);
        A_Fire(actor);
    }

    default void A_FireCrackle(mobj_t actor) {
        StartSound(actor, sounds.sfxenum_t.sfx_flame);
        A_Fire(actor);
    }

    default void A_Fire(mobj_t actor) {
        mobj_t dest;
        //long    an;

        dest = actor.tracer;
        if (dest == null) {
            return;
        }

        // don't move it if the vile lost sight
        if (!getEnemies().CheckSight(actor.target, dest)) {
            return;
        }

        // an = dest.angle >>> ANGLETOFINESHIFT;
        getAttacks().UnsetThingPosition(actor);
        actor.x = dest.x + FixedMul(24 * FRACUNIT, finecosine(dest.angle));
        actor.y = dest.y + FixedMul(24 * FRACUNIT, finesine(dest.angle));
        actor.z = dest.z;
        SetThingPosition(actor);
    }
    
    //
    // A_VileTarget
    // Spawn the hellfire
    //
    default void A_VileTarget(mobj_t actor) {
        mobj_t fog;

        if (actor.target == null) {
            return;
        }

        A_FaceTarget(actor);

        fog = getEnemies().SpawnMobj(actor.target.x, actor.target.y, actor.target.z, mobjtype_t.MT_FIRE);

        actor.tracer = fog;
        fog.target = actor;
        fog.tracer = actor.target;
        A_Fire(fog);
    }

    //
    // A_VileAttack
    //
    default void A_VileAttack(mobj_t actor) {
        mobj_t fire;
        //int     an;

        if (actor.target == null) {
            return;
        }

        A_FaceTarget(actor);

        if (!getEnemies().CheckSight(actor, actor.target)) {
            return;
        }

        StartSound(actor, sounds.sfxenum_t.sfx_barexp);
        getAttacks().DamageMobj(actor.target, actor, actor, 20);
        actor.target.momz = 1000 * MAPFRACUNIT / actor.target.info.mass;

        // an = actor.angle >> ANGLETOFINESHIFT;
        fire = actor.tracer;

        if (fire == null) {
            return;
        }

        // move the fire between the vile and the player
        fire.x = actor.target.x - FixedMul(24 * FRACUNIT, finecosine(actor.angle));
        fire.y = actor.target.y - FixedMul(24 * FRACUNIT, finesine(actor.angle));
        getAttacks().RadiusAttack(fire, actor, 70);
    }
}

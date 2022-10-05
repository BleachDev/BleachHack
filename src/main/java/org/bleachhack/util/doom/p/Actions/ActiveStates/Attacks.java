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
package org.bleachhack.util.doom.p.Actions.ActiveStates;

import static org.bleachhack.util.doom.data.Defines.MELEERANGE;
import static org.bleachhack.util.doom.data.Defines.MISSILERANGE;
import static org.bleachhack.util.doom.data.Defines.pw_strength;
import static org.bleachhack.util.doom.data.Tables.ANG180;
import static org.bleachhack.util.doom.data.Tables.ANG90;
import static org.bleachhack.util.doom.data.Tables.BITS32;
import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.data.sounds;
import org.bleachhack.util.doom.defines.statenum_t;
import org.bleachhack.util.doom.doom.SourceCode.angle_t;
import static org.bleachhack.util.doom.doom.items.weaponinfo;
import org.bleachhack.util.doom.doom.player_t;
import static org.bleachhack.util.doom.doom.player_t.ps_flash;
import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import static org.bleachhack.util.doom.p.Actions.ActionsSectors.KEY_SPAWN;
import org.bleachhack.util.doom.p.Actions.ActionsSectors.Spawn;
import org.bleachhack.util.doom.p.mobj_t;
import static org.bleachhack.util.doom.p.mobj_t.MF_JUSTATTACKED;
import org.bleachhack.util.doom.p.pspdef_t;
import static org.bleachhack.util.doom.utils.C2JUtils.eval;

public interface Attacks extends Monsters {
    // plasma cells for a bfg attack
    // IDEA: make action functions partially parametrizable?
    int BFGCELLS = 40;
    
    //
    // A_FirePistol
    //

    default void A_FirePistol(player_t player, pspdef_t psp) {
        StartSound(player.mo, sounds.sfxenum_t.sfx_pistol);

        player.mo.SetMobjState(statenum_t.S_PLAY_ATK2);
        player.ammo[weaponinfo[player.readyweapon.ordinal()].ammo.ordinal()]--;

        player.SetPsprite(
            ps_flash,
            weaponinfo[player.readyweapon.ordinal()].flashstate);

        getAttacks().P_BulletSlope(player.mo);
        getAttacks().P_GunShot(player.mo, !eval(player.refire));
    }

    //
    // A_FireShotgun
    //
    default void A_FireShotgun(player_t player, pspdef_t psp) {
        int i;

        StartSound(player.mo, sounds.sfxenum_t.sfx_shotgn);
        player.mo.SetMobjState(statenum_t.S_PLAY_ATK2);

        player.ammo[weaponinfo[player.readyweapon.ordinal()].ammo.ordinal()]--;

        player.SetPsprite(
            ps_flash,
            weaponinfo[player.readyweapon.ordinal()].flashstate);

        getAttacks().P_BulletSlope(player.mo);

        for (i = 0; i < 7; i++) {
            getAttacks().P_GunShot(player.mo, false);
        }
    }

    /**
     * A_FireShotgun2
     */
    default void A_FireShotgun2(player_t player, pspdef_t psp) {
        final Spawn sp = getEnemies().contextRequire(KEY_SPAWN);
        long angle;
        int damage;

        StartSound(player.mo, sounds.sfxenum_t.sfx_dshtgn);
        player.mo.SetMobjState(statenum_t.S_PLAY_ATK2);

        player.ammo[weaponinfo[player.readyweapon.ordinal()].ammo.ordinal()] -= 2;

        player.SetPsprite(
            ps_flash,
            weaponinfo[player.readyweapon.ordinal()].flashstate);

        getAttacks().P_BulletSlope(player.mo);

        for (int i = 0; i < 20; i++) {
            damage = 5 * (P_Random() % 3 + 1);
            angle = player.mo.angle;
            angle += (P_Random() - P_Random()) << 19;
            getAttacks().LineAttack(player.mo, angle, MISSILERANGE, sp.bulletslope + ((P_Random() - P_Random()) << 5), damage);
        }
    }

    //
    // A_Punch
    //
    default void A_Punch(player_t player, pspdef_t psp) {
        final Spawn sp = contextRequire(KEY_SPAWN);
        @angle_t long angle;
        int damage;
        int slope;

        damage = (P_Random() % 10 + 1) << 1;

        if (eval(player.powers[pw_strength])) {
            damage *= 10;
        }

        angle = player.mo.angle;
        //angle = (angle+(RND.P_Random()-RND.P_Random())<<18)/*&BITS32*/;
        // _D_: for some reason, punch didnt work until I change this
        // I think it's because of "+" VS "<<" prioritys...
        angle += (P_Random() - P_Random()) << 18;
        slope = getAttacks().AimLineAttack(player.mo, angle, MELEERANGE);
        getAttacks().LineAttack(player.mo, angle, MELEERANGE, slope, damage);

        // turn to face target
        if (eval(sp.linetarget)) {
            StartSound(player.mo, sounds.sfxenum_t.sfx_punch);
            player.mo.angle = sceneRenderer().PointToAngle2(
                player.mo.x,
                player.mo.y,
                sp.linetarget.x,
                sp.linetarget.y
            ) & BITS32;
        }
    }

    //
    // A_Saw
    //
    default void A_Saw(player_t player, pspdef_t psp) {
        final Spawn sp = contextRequire(KEY_SPAWN);
        @angle_t long angle;
        int damage;
        int slope;

        damage = 2 * (P_Random() % 10 + 1);
        angle = player.mo.angle;
        angle += (P_Random() - P_Random()) << 18;
        angle &= BITS32;

        // use meleerange + 1 se the puff doesn't skip the flash
        slope = getAttacks().AimLineAttack(player.mo, angle, MELEERANGE + 1);
        getAttacks().LineAttack(player.mo, angle, MELEERANGE + 1, slope, damage);

        if (!eval(sp.linetarget)) {
            StartSound(player.mo, sounds.sfxenum_t.sfx_sawful);
            return;
        }
        StartSound(player.mo, sounds.sfxenum_t.sfx_sawhit);

        // turn to face target
        angle = sceneRenderer().PointToAngle2(player.mo.x, player.mo.y,
            sp.linetarget.x, sp.linetarget.y) & BITS32;
        /* FIXME: this comparison is going to fail.... or not?
            If e.g. angle = 359 degrees (which will be mapped to a small negative number),
            and player.mo.angle = 160 degrees (a large, positive value), the result will be a
            large negative value, which will still be "greater" than ANG180.
            
            It seems that *differences* between angles will always compare correctly, but
            not direct inequalities.
            
         */

        // Yet another screwy place where unsigned BAM angles are used as SIGNED comparisons.
        long dangle = (angle - player.mo.angle);
        dangle &= BITS32;
        if (dangle > ANG180) {
            if ((int) dangle < -ANG90 / 20) {
                player.mo.angle = angle + ANG90 / 21;
            } else {
                player.mo.angle -= ANG90 / 20;
            }
        } else {
            if (dangle > ANG90 / 20) {
                player.mo.angle = angle - ANG90 / 21;
            } else {
                player.mo.angle += ANG90 / 20;
            }
        }
        player.mo.angle &= BITS32;
        player.mo.flags |= MF_JUSTATTACKED;
    }

    //
    // A_FireMissile
    //
    default void A_FireMissile(player_t player, pspdef_t psp) {
        player.ammo[weaponinfo[player.readyweapon.ordinal()].ammo.ordinal()]--;
        getAttacks().SpawnPlayerMissile(player.mo, mobjtype_t.MT_ROCKET);
    }

    //
    // A_FireBFG
    //
    default void A_FireBFG(player_t player, pspdef_t psp) {
        player.ammo[weaponinfo[player.readyweapon.ordinal()].ammo.ordinal()] -= BFGCELLS;
        getAttacks().SpawnPlayerMissile(player.mo, mobjtype_t.MT_BFG);
    }

    //
    // A_FireCGun
    //
    default void A_FireCGun(player_t player, pspdef_t psp) {
        // For convenience.
        int readyweap = player.readyweapon.ordinal();
        int flashstate = weaponinfo[readyweap].flashstate.ordinal();
        int current_state = psp.state.id;

        StartSound(player.mo, sounds.sfxenum_t.sfx_pistol);
        if (!eval(player.ammo[weaponinfo[readyweap].ammo.ordinal()])) {
            return;
        }

        player.mo.SetMobjState(statenum_t.S_PLAY_ATK2);
        player.ammo[weaponinfo[readyweap].ammo.ordinal()]--;

        // MAES: Code to alternate between two different gun flashes
        // needed a clear rewrite, as it was way too messy.
        // We know that the flash states are a certain amount away from
        // the firing states. This amount is two frames.
        player.SetPsprite(ps_flash, statenum_t.values()[flashstate + current_state - statenum_t.S_CHAIN1.ordinal()]
        );

        getAttacks().P_BulletSlope(player.mo);
        getAttacks().P_GunShot(player.mo, !eval(player.refire));
    }

    //
    // A_FirePlasma
    //
    default void A_FirePlasma(player_t player, pspdef_t psp) {
        player.ammo[weaponinfo[player.readyweapon.ordinal()].ammo.ordinal()]--;

        player.SetPsprite(
            ps_flash,
            weaponinfo[player.readyweapon.ordinal()].flashstate);

        getAttacks().SpawnPlayerMissile(player.mo, mobjtype_t.MT_PLASMA);
    }

    default void A_XScream(mobj_t actor) {
        StartSound(actor, sounds.sfxenum_t.sfx_slop);
    }

    default void A_Pain(mobj_t actor) {
        if (actor.info.painsound != null) {
            StartSound(actor, actor.info.painsound);
        }
    }

    //
    // A_Explode
    //
    default void A_Explode(mobj_t thingy) {
        getAttacks().RadiusAttack(thingy, thingy.target, 128);
    }
    
    //
    // A_BFGSpray
    // Spawn a BFG explosion on every monster in view
    //
    default void A_BFGSpray(mobj_t mo) {
        final Spawn sp = contextRequire(KEY_SPAWN);

        int damage;
        long an; // angle_t

        // offset angles from its attack angle
        for (int i = 0; i < 40; i++) {
            an = (mo.angle - ANG90 / 2 + ANG90 / 40 * i) & BITS32;

            // mo.target is the originator (player)
            //  of the missile
            getAttacks().AimLineAttack(mo.target, an, 16 * 64 * FRACUNIT);

            if (!eval(sp.linetarget)) {
                continue;
            }

            getEnemies().SpawnMobj(sp.linetarget.x, sp.linetarget.y, sp.linetarget.z + (sp.linetarget.height >> 2), mobjtype_t.MT_EXTRABFG);

            damage = 0;
            for (int j = 0; j < 15; j++) {
                damage += (P_Random() & 7) + 1;
            }

            getEnemies().DamageMobj(sp.linetarget, mo.target, mo.target, damage);
        }
    }

}

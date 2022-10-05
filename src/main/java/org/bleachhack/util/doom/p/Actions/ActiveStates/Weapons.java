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

import static org.bleachhack.util.doom.data.Defines.BT_ATTACK;
import static org.bleachhack.util.doom.data.Defines.PST_DEAD;
import static org.bleachhack.util.doom.data.Tables.FINEANGLES;
import static org.bleachhack.util.doom.data.Tables.FINEMASK;
import static org.bleachhack.util.doom.data.Tables.finecosine;
import static org.bleachhack.util.doom.data.Tables.finesine;
import static org.bleachhack.util.doom.data.info.states;
import org.bleachhack.util.doom.data.sounds;
import org.bleachhack.util.doom.defines.statenum_t;
import static org.bleachhack.util.doom.doom.items.weaponinfo;
import org.bleachhack.util.doom.doom.player_t;
import static org.bleachhack.util.doom.doom.player_t.LOWERSPEED;
import static org.bleachhack.util.doom.doom.player_t.RAISESPEED;
import static org.bleachhack.util.doom.doom.player_t.WEAPONBOTTOM;
import static org.bleachhack.util.doom.doom.player_t.WEAPONTOP;
import static org.bleachhack.util.doom.doom.player_t.ps_flash;
import static org.bleachhack.util.doom.doom.player_t.ps_weapon;
import org.bleachhack.util.doom.doom.weapontype_t;
import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import static org.bleachhack.util.doom.m.fixed_t.FixedMul;
import org.bleachhack.util.doom.p.pspdef_t;
import static org.bleachhack.util.doom.utils.C2JUtils.eval;

public interface Weapons extends Sounds {
    /**
     * A_WeaponReady
     * The player can fire the weapon
     * or change to another weapon at this time.
     * Follows after getting weapon up,
     * or after previous attack/fire sequence.
     */
    default void A_WeaponReady(player_t player, pspdef_t psp) {
        statenum_t newstate;
        int angle;

        // get out of attack state
        if (player.mo.mobj_state == states[statenum_t.S_PLAY_ATK1.ordinal()]
            || player.mo.mobj_state == states[statenum_t.S_PLAY_ATK2.ordinal()]) {
            player.mo.SetMobjState(statenum_t.S_PLAY);
        }

        if (player.readyweapon == weapontype_t.wp_chainsaw
         && psp.state == states[statenum_t.S_SAW.ordinal()])
        {
            StartSound(player.mo, sounds.sfxenum_t.sfx_sawidl);
        }

        // check for change
        //  if player is dead, put the weapon away
        if (player.pendingweapon != weapontype_t.wp_nochange || !eval(player.health[0])) {
            // change weapon
            //  (pending weapon should allready be validated)
            newstate = weaponinfo[player.readyweapon.ordinal()].downstate;
            player.SetPsprite(player_t.ps_weapon, newstate);
            return;
        }

        // check for fire
        //  the missile launcher and bfg do not auto fire
        if (eval(player.cmd.buttons & BT_ATTACK)) {
            if (!player.attackdown
             || (player.readyweapon != weapontype_t.wp_missile
             && player.readyweapon != weapontype_t.wp_bfg))
            {
                player.attackdown = true;
                getEnemies().FireWeapon(player);
                return;
            }
        } else {
            player.attackdown = false;
        }

        // bob the weapon based on movement speed
        angle = (128 * LevelTime()) & FINEMASK;
        psp.sx = FRACUNIT + FixedMul(player.bob, finecosine[angle]);
        angle &= FINEANGLES / 2 - 1;
        psp.sy = player_t.WEAPONTOP + FixedMul(player.bob, finesine[angle]);
    }

    //
    // A_Raise
    //
    default void A_Raise(player_t player, pspdef_t psp) {
        statenum_t newstate;

        //System.out.println("Trying to raise weapon");
        //System.out.println(player.readyweapon + " height: "+psp.sy);
        psp.sy -= RAISESPEED;

        if (psp.sy > WEAPONTOP) {
            //System.out.println("Not on top yet, exit and repeat.");
            return;
        }

        psp.sy = WEAPONTOP;

        // The weapon has been raised all the way,
        //  so change to the ready state.
        newstate = weaponinfo[player.readyweapon.ordinal()].readystate;
        //System.out.println("Weapon raised, setting new state.");

        player.SetPsprite(ps_weapon, newstate);
    }

    //
    // A_ReFire
    // The player can re-fire the weapon
    // without lowering it entirely.
    //
    @Override
    default void A_ReFire(player_t player, pspdef_t psp) {
        // check for fire
        //  (if a weaponchange is pending, let it go through instead)
        if (eval(player.cmd.buttons & BT_ATTACK)
            && player.pendingweapon == weapontype_t.wp_nochange
            && eval(player.health[0])) {
            player.refire++;
            getEnemies().FireWeapon(player);
        } else {
            player.refire = 0;
            player.CheckAmmo();
        }
    }

    //
    // A_GunFlash
    //
    default void A_GunFlash(player_t player, pspdef_t psp) {
        player.mo.SetMobjState(statenum_t.S_PLAY_ATK2);
        player.SetPsprite(ps_flash, weaponinfo[player.readyweapon.ordinal()].flashstate);
    }
    
    //
    // ?
    //
    default void A_Light0(player_t player, pspdef_t psp) {
        player.extralight = 0;
    }

    default void A_Light1(player_t player, pspdef_t psp) {
        player.extralight = 1;
    }

    default void A_Light2(player_t player, pspdef_t psp) {
        player.extralight = 2;
    }

    //
    // A_Lower
    // Lowers current weapon,
    //  and changes weapon at bottom.
    //
    default void A_Lower(player_t player, pspdef_t psp) {
        psp.sy += LOWERSPEED;

        // Is already down.
        if (psp.sy < WEAPONBOTTOM) {
            return;
        }

        // Player is dead.
        if (player.playerstate == PST_DEAD) {
            psp.sy = WEAPONBOTTOM;

            // don't bring weapon back up
            return;
        }

        // The old weapon has been lowered off the screen,
        // so change the weapon and start raising it
        if (!eval(player.health[0])) {
            // Player is dead, so keep the weapon off screen.
            player.SetPsprite(ps_weapon, statenum_t.S_NULL);
            return;
        }

        player.readyweapon = player.pendingweapon;

        player.BringUpWeapon();
    }

    default void A_CheckReload(player_t player, pspdef_t psp) {
        player.CheckAmmo();
        /*
        if (player.ammo[am_shell]<2)
        P_SetPsprite (player, ps_weapon, S_DSNR1);
         */
    }

}

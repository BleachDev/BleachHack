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

import static org.bleachhack.util.doom.data.Defines.*;
import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.data.sounds.sfxenum_t;
import org.bleachhack.util.doom.defines.ammotype_t;
import org.bleachhack.util.doom.defines.card_t;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.SourceCode.P_Map;
import static org.bleachhack.util.doom.doom.SourceCode.P_Map.PIT_CheckThing;
import static org.bleachhack.util.doom.doom.SourceCode.P_Map.PIT_StompThing;
import org.bleachhack.util.doom.doom.SourceCode.fixed_t;
import static org.bleachhack.util.doom.doom.englsh.*;
import org.bleachhack.util.doom.doom.player_t;
import org.bleachhack.util.doom.doom.weapontype_t;
import org.bleachhack.util.doom.m.Settings;
import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import org.bleachhack.util.doom.p.mobj_t;
import static org.bleachhack.util.doom.p.mobj_t.*;
import static org.bleachhack.util.doom.utils.C2JUtils.eval;

public interface ActionsThings extends ActionTrait {

    void DamageMobj(mobj_t thing, mobj_t tmthing, mobj_t tmthing0, int damage);
    void RemoveMobj(mobj_t special);

    /**
     * PIT_CheckThing
     */
    @Override
    @P_Map.C(PIT_CheckThing)
    default boolean CheckThing(mobj_t thing) {
        final Movement movm = contextRequire(KEY_MOVEMENT);
        @fixed_t
        int blockdist;
        boolean solid;
        int damage;

        if ((thing.flags & (MF_SOLID | MF_SPECIAL | MF_SHOOTABLE)) == 0) {
            return true;
        }

        blockdist = thing.radius + movm.tmthing.radius;

        if (Math.abs(thing.x - movm.tmx) >= blockdist
            || Math.abs(thing.y - movm.tmy) >= blockdist) {
            // didn't hit it
            return true;
        }

        // don't clip against self
        if (thing == movm.tmthing) {
            return true;
        }

        // check for skulls slamming into things
        if ((movm.tmthing.flags & MF_SKULLFLY) != 0) {
            damage = ((P_Random() % 8) + 1) * movm.tmthing.info.damage;

            DamageMobj(thing, movm.tmthing, movm.tmthing, damage);

            movm.tmthing.flags &= ~MF_SKULLFLY;
            movm.tmthing.momx = movm.tmthing.momy = movm.tmthing.momz = 0;

            movm.tmthing.SetMobjState(movm.tmthing.info.spawnstate);

            return false;       // stop moving
        }

        // missiles can hit other things
        if (eval(movm.tmthing.flags & MF_MISSILE)) {
            // see if it went over / under
            if (movm.tmthing.z > thing.z + thing.height) {
                return true;        // overhead
            }
            if (movm.tmthing.z + movm.tmthing.height < thing.z) {
                return true;        // underneath
            }
            if (movm.tmthing.target != null && (movm.tmthing.target.type == thing.type
                || (movm.tmthing.target.type == mobjtype_t.MT_KNIGHT && thing.type == mobjtype_t.MT_BRUISER)
                || (movm.tmthing.target.type == mobjtype_t.MT_BRUISER && thing.type == mobjtype_t.MT_KNIGHT))) {
                // Don't hit same species as originator.
                if (thing == movm.tmthing.target) {
                    return true;
                }

                if (thing.type != mobjtype_t.MT_PLAYER) {
                    // Explode, but do no damage.
                    // Let players missile other players.
                    return false;
                }
            }

            if (!eval(thing.flags & MF_SHOOTABLE)) {
                // didn't do any damage
                return !eval(thing.flags & MF_SOLID);
            }

            // damage / explode
            damage = ((P_Random() % 8) + 1) * movm.tmthing.info.damage;
            DamageMobj(thing, movm.tmthing, movm.tmthing.target, damage);

            // don't traverse any more
            return false;
        }

        // check for special pickup
        if (eval(thing.flags & MF_SPECIAL)) {
            solid = eval(thing.flags & MF_SOLID);
            if (eval(movm.tmflags & MF_PICKUP)) {
                // can remove thing
                TouchSpecialThing(thing, movm.tmthing);
            }
            return !solid;
        }

        return !eval(thing.flags & MF_SOLID);
    }

    ;

    /**
     * P_TouchSpecialThing LIKE ROMERO's ASS!!!
     */
    default void TouchSpecialThing(mobj_t special, mobj_t toucher) {
        final DoomMain<?, ?> DOOM = DOOM();
        player_t player;
        int i;
        @fixed_t
        int delta;
        sfxenum_t sound;

        delta = special.z - toucher.z;

        if (delta > toucher.height || delta < -8 * FRACUNIT) {
            // out of reach
            return;
        }

        sound = sfxenum_t.sfx_itemup;
        player = toucher.player;

        // Dead thing touching.
        // Can happen with a sliding player corpse.
        if (toucher.health <= 0) {
            return;
        }

        // Identify by sprite.
        switch (special.mobj_sprite) {
            // armor
            case SPR_ARM1:
                if (!player.GiveArmor(1)) {
                    return;
                }
                player.message = GOTARMOR;
                break;

            case SPR_ARM2:
                if (!player.GiveArmor(2)) {
                    return;
                }
                player.message = GOTMEGA;
                break;

            // bonus items
            case SPR_BON1:
                player.health[0]++; // can go over 100%
                if (player.health[0] > 200) {
                    player.health[0] = 200;
                }
                player.mo.health = player.health[0];
                player.message = GOTHTHBONUS;
                break;

            case SPR_BON2:
                player.armorpoints[0]++; // can go over 100%
                if (player.armorpoints[0] > 200) {
                    player.armorpoints[0] = 200;
                }
                if (player.armortype == 0) {
                    player.armortype = 1;
                }
                player.message = GOTARMBONUS;
                break;

            case SPR_SOUL:
                player.health[0] += 100;
                if (player.health[0] > 200) {
                    player.health[0] = 200;
                }
                player.mo.health = player.health[0];
                player.message = GOTSUPER;
                sound = sfxenum_t.sfx_getpow;
                break;

            case SPR_MEGA:
                if (!DOOM.isCommercial()) {
                    return;
                }
                player.health[0] = 200;
                player.mo.health = player.health[0];
                player.GiveArmor(2);
                player.message = GOTMSPHERE;
                sound = sfxenum_t.sfx_getpow;
                break;

            // cards
            // leave cards for everyone
            case SPR_BKEY:
                if (!player.cards[card_t.it_bluecard.ordinal()]) {
                    player.message = GOTBLUECARD;
                }
                player.GiveCard(card_t.it_bluecard);
                if (!DOOM.netgame) {
                    break;
                }
                return;

            case SPR_YKEY:
                if (!player.cards[card_t.it_yellowcard.ordinal()]) {
                    player.message = GOTYELWCARD;
                }
                player.GiveCard(card_t.it_yellowcard);
                if (!DOOM.netgame) {
                    break;
                }
                return;

            case SPR_RKEY:
                if (!player.cards[card_t.it_redcard.ordinal()]) {
                    player.message = GOTREDCARD;
                }
                player.GiveCard(card_t.it_redcard);
                if (!DOOM.netgame) {
                    break;
                }
                return;

            case SPR_BSKU:
                if (!player.cards[card_t.it_blueskull.ordinal()]) {
                    player.message = GOTBLUESKUL;
                }
                player.GiveCard(card_t.it_blueskull);
                if (!DOOM.netgame) {
                    break;
                }
                return;

            case SPR_YSKU:
                if (!player.cards[card_t.it_yellowskull.ordinal()]) {
                    player.message = GOTYELWSKUL;
                }
                player.GiveCard(card_t.it_yellowskull);
                if (!DOOM.netgame) {
                    break;
                }
                return;

            case SPR_RSKU:
                if (!player.cards[card_t.it_redskull.ordinal()]) {
                    player.message = GOTREDSKULL;
                }
                player.GiveCard(card_t.it_redskull);
                if (!DOOM.netgame) {
                    break;
                }
                return;

            // medikits, heals
            case SPR_STIM:
                if (!player.GiveBody(10)) {
                    return;
                }
                player.message = GOTSTIM;
                break;

            case SPR_MEDI:
                /**
                 * Another fix with switchable option to enable
                 * - Good Sign 2017/04/03
                 */
                boolean need = player.health[0] < 25;

                if (!player.GiveBody(25)) {
                    return;
                }

                if (DOOM.CM.equals(Settings.fix_medi_need, Boolean.FALSE)) // default behavior - with bug
                {
                    player.message = player.health[0] < 25 ? GOTMEDINEED : GOTMEDIKIT;
                } else //proper behavior
                {
                    player.message = need ? GOTMEDINEED : GOTMEDIKIT;
                }

                break;

            // power ups
            case SPR_PINV:
                if (!player.GivePower(pw_invulnerability)) {
                    return;
                }
                player.message = GOTINVUL;
                sound = sfxenum_t.sfx_getpow;
                break;

            case SPR_PSTR:
                if (!player.GivePower(pw_strength)) {
                    return;
                }
                player.message = GOTBERSERK;
                if (player.readyweapon != weapontype_t.wp_fist) {
                    player.pendingweapon = weapontype_t.wp_fist;
                }
                sound = sfxenum_t.sfx_getpow;
                break;

            case SPR_PINS:
                if (!player.GivePower(pw_invisibility)) {
                    return;
                }
                player.message = GOTINVIS;
                sound = sfxenum_t.sfx_getpow;
                break;

            case SPR_SUIT:
                if (!player.GivePower(pw_ironfeet)) {
                    return;
                }
                player.message = GOTSUIT;
                sound = sfxenum_t.sfx_getpow;
                break;

            case SPR_PMAP:
                if (!player.GivePower(pw_allmap)) {
                    return;
                }
                player.message = GOTMAP;
                sound = sfxenum_t.sfx_getpow;
                break;

            case SPR_PVIS:
                if (!player.GivePower(pw_infrared)) {
                    return;
                }
                player.message = GOTVISOR;
                sound = sfxenum_t.sfx_getpow;
                break;

            // ammo
            case SPR_CLIP:
                if ((special.flags & MF_DROPPED) != 0) {
                    if (!player.GiveAmmo(ammotype_t.am_clip, 0)) {
                        return;
                    }
                } else {
                    if (!player.GiveAmmo(ammotype_t.am_clip, 1)) {
                        return;
                    }
                }
                player.message = GOTCLIP;
                break;

            case SPR_AMMO:
                if (!player.GiveAmmo(ammotype_t.am_clip, 5)) {
                    return;
                }
                player.message = GOTCLIPBOX;
                break;

            case SPR_ROCK:
                if (!player.GiveAmmo(ammotype_t.am_misl, 1)) {
                    return;
                }
                player.message = GOTROCKET;
                break;

            case SPR_BROK:
                if (!player.GiveAmmo(ammotype_t.am_misl, 5)) {
                    return;
                }
                player.message = GOTROCKBOX;
                break;

            case SPR_CELL:
                if (!player.GiveAmmo(ammotype_t.am_cell, 1)) {
                    return;
                }
                player.message = GOTCELL;
                break;

            case SPR_CELP:
                if (!player.GiveAmmo(ammotype_t.am_cell, 5)) {
                    return;
                }
                player.message = GOTCELLBOX;
                break;

            case SPR_SHEL:
                if (!player.GiveAmmo(ammotype_t.am_shell, 1)) {
                    return;
                }
                player.message = GOTSHELLS;
                break;

            case SPR_SBOX:
                if (!player.GiveAmmo(ammotype_t.am_shell, 5)) {
                    return;
                }
                player.message = GOTSHELLBOX;
                break;

            case SPR_BPAK:
                if (!player.backpack) {
                    for (i = 0; i < NUMAMMO; i++) {
                        player.maxammo[i] *= 2;
                    }
                    player.backpack = true;
                }
                for (i = 0; i < NUMAMMO; i++) {
                    player.GiveAmmo(ammotype_t.values()[i], 1);
                }
                player.message = GOTBACKPACK;
                break;

            // weapons
            case SPR_BFUG:
                if (!player.GiveWeapon(weapontype_t.wp_bfg, false)) {
                    return;
                }
                player.message = GOTBFG9000;
                sound = sfxenum_t.sfx_wpnup;
                break;

            case SPR_MGUN:
                if (!player.GiveWeapon(weapontype_t.wp_chaingun,
                    (special.flags & MF_DROPPED) != 0)) {
                    return;
                }
                player.message = GOTCHAINGUN;
                sound = sfxenum_t.sfx_wpnup;
                break;

            case SPR_CSAW:
                if (!player.GiveWeapon(weapontype_t.wp_chainsaw, false)) {
                    return;
                }
                player.message = GOTCHAINSAW;
                sound = sfxenum_t.sfx_wpnup;
                break;

            case SPR_LAUN:
                if (!player.GiveWeapon(weapontype_t.wp_missile, false)) {
                    return;
                }
                player.message = GOTLAUNCHER;
                sound = sfxenum_t.sfx_wpnup;
                break;

            case SPR_PLAS:
                if (!player.GiveWeapon(weapontype_t.wp_plasma, false)) {
                    return;
                }
                player.message = GOTPLASMA;
                sound = sfxenum_t.sfx_wpnup;
                break;

            case SPR_SHOT:
                if (!player.GiveWeapon(weapontype_t.wp_shotgun,
                    (special.flags & MF_DROPPED) != 0)) {
                    return;
                }
                player.message = GOTSHOTGUN;
                sound = sfxenum_t.sfx_wpnup;
                break;

            case SPR_SGN2:
                if (!player.GiveWeapon(weapontype_t.wp_supershotgun,
                    (special.flags & MF_DROPPED) != 0)) {
                    return;
                }
                player.message = GOTSHOTGUN2;
                sound = sfxenum_t.sfx_wpnup;
                break;

            default:
                DOOM.doomSystem.Error("P_SpecialThing: Unknown gettable thing");
        }

        if ((special.flags & MF_COUNTITEM) != 0) {
            player.itemcount++;
        }
        RemoveMobj(special);
        player.bonuscount += player_t.BONUSADD;
        if (player == DOOM.players[DOOM.consoleplayer]) {
            DOOM.doomSound.StartSound(null, sound);
        }
    }

    /**
     * PIT_StompThing
     */
    @Override
    @P_Map.C(PIT_StompThing)
    default boolean StompThing(mobj_t thing) {
        final Movement mov = contextRequire(KEY_MOVEMENT);
        @fixed_t
        int blockdist;

        if ((thing.flags & MF_SHOOTABLE) == 0) {
            return true;
        }

        blockdist = thing.radius + mov.tmthing.radius;

        if (Math.abs(thing.x - mov.tmx) >= blockdist || Math.abs(thing.y - mov.tmy) >= blockdist) {
            // didn't hit it
            return true;
        }

        // don't clip against self
        if (thing == mov.tmthing) {
            return true;
        }

        // monsters don't stomp things except on boss level
        if ((mov.tmthing.player == null) && (MapNumber() != 30)) {
            return false;
        }

        DamageMobj(thing, mov.tmthing, mov.tmthing, 10000); // in interaction
        return true;
    }
;
}

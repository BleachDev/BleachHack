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

import static org.bleachhack.util.doom.data.Defines.BASETHRESHOLD;
import static org.bleachhack.util.doom.data.Defines.ITEMQUESIZE;
import static org.bleachhack.util.doom.data.Defines.ONFLOORZ;
import static org.bleachhack.util.doom.data.Defines.PST_DEAD;
import static org.bleachhack.util.doom.data.Defines.pw_invulnerability;
import static org.bleachhack.util.doom.data.Tables.ANG180;
import static org.bleachhack.util.doom.data.Tables.BITS32;
import static org.bleachhack.util.doom.data.Tables.finecosine;
import static org.bleachhack.util.doom.data.Tables.finesine;
import static org.bleachhack.util.doom.data.info.states;
import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.defines.skill_t;
import org.bleachhack.util.doom.defines.statenum_t;
import org.bleachhack.util.doom.doom.SourceCode;
import org.bleachhack.util.doom.doom.SourceCode.P_MapUtl;
import static org.bleachhack.util.doom.doom.SourceCode.P_MapUtl.P_UnsetThingPosition;
import static org.bleachhack.util.doom.doom.SourceCode.P_Mobj.P_RemoveMobj;
import org.bleachhack.util.doom.doom.player_t;
import org.bleachhack.util.doom.doom.weapontype_t;
import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import static org.bleachhack.util.doom.m.fixed_t.FixedMul;
import static org.bleachhack.util.doom.m.fixed_t.MAPFRACUNIT;
import org.bleachhack.util.doom.p.AbstractLevelLoader;
import static org.bleachhack.util.doom.p.MobjFlags.MF_DROPPED;
import static org.bleachhack.util.doom.p.MobjFlags.MF_NOBLOCKMAP;
import static org.bleachhack.util.doom.p.MobjFlags.MF_NOSECTOR;
import static org.bleachhack.util.doom.p.MobjFlags.MF_SPECIAL;
import org.bleachhack.util.doom.p.mobj_t;
import static org.bleachhack.util.doom.p.mobj_t.MF_CORPSE;
import static org.bleachhack.util.doom.p.mobj_t.MF_COUNTKILL;
import static org.bleachhack.util.doom.p.mobj_t.MF_DROPOFF;
import static org.bleachhack.util.doom.p.mobj_t.MF_FLOAT;
import static org.bleachhack.util.doom.p.mobj_t.MF_JUSTHIT;
import static org.bleachhack.util.doom.p.mobj_t.MF_NOCLIP;
import static org.bleachhack.util.doom.p.mobj_t.MF_NOGRAVITY;
import static org.bleachhack.util.doom.p.mobj_t.MF_SHOOTABLE;
import static org.bleachhack.util.doom.p.mobj_t.MF_SKULLFLY;
import static org.bleachhack.util.doom.p.mobj_t.MF_SOLID;
import static org.bleachhack.util.doom.utils.C2JUtils.eval;

public interface ActionsMobj extends ActionsThings, ActionsMovement, ActionsTeleportation {

    //
    // P_DamageMobj
    // Damages both enemies and players
    // "inflictor" is the thing that caused the damage
    //  creature or missile, can be NULL (slime, etc)
    // "source" is the thing to target after taking damage
    //  creature or NULL
    // Source and inflictor are the same for melee attacks.
    // Source can be NULL for slime, barrel explosions
    // and other environmental stuff.
    //
    @Override
    default void DamageMobj(mobj_t target, mobj_t inflictor, mobj_t source, int damage) {
        long ang; // unsigned
        int saved;
        player_t player;
        @SourceCode.fixed_t
        int thrust;
        int temp;

        if (!eval(target.flags & MF_SHOOTABLE)) {
            return; // shouldn't happen...
        }
        if (target.health <= 0) {
            return;
        }

        if (eval(target.flags & MF_SKULLFLY)) {
            target.momx = target.momy = target.momz = 0;
        }

        player = target.player;
        if ((player != null) && getGameSkill() == skill_t.sk_baby) {
            damage >>= 1;   // take half damage in trainer mode
        }

        // Some close combat weapons should not
        // inflict thrust and push the victim out of reach,
        // thus kick away unless using the chainsaw.
        if ((inflictor != null)
            && !eval(target.flags & MF_NOCLIP)
            && (source == null
            || source.player == null
            || source.player.readyweapon != weapontype_t.wp_chainsaw)) {
            ang = sceneRenderer().PointToAngle2(inflictor.x,
                inflictor.y,
                target.x,
                target.y) & BITS32;

            thrust = damage * (MAPFRACUNIT >> 3) * 100 / target.info.mass;

            // make fall forwards sometimes
            if ((damage < 40)
                && (damage > target.health)
                && (target.z - inflictor.z > 64 * FRACUNIT)
                && eval(P_Random() & 1)) {
                ang += ANG180;
                thrust *= 4;
            }

            //ang >>= ANGLETOFINESHIFT;
            target.momx += FixedMul(thrust, finecosine(ang));
            target.momy += FixedMul(thrust, finesine(ang));
        }

        // player specific
        if (player != null) {
            // end of game hell hack
            if (target.subsector.sector.special == 11
                && damage >= target.health) {
                damage = target.health - 1;
            }

            // Below certain threshold,
            // ignore damage in GOD mode, or with INVUL power.
            if (damage < 1000
                && (eval(player.cheats & player_t.CF_GODMODE))
                || player.powers[pw_invulnerability] != 0) {
                return;
            }

            if (player.armortype != 0) {
                if (player.armortype == 1) {
                    saved = damage / 3;
                } else {
                    saved = damage / 2;
                }

                if (player.armorpoints[0] <= saved) {
                    // armor is used up
                    saved = player.armorpoints[0];
                    player.armortype = 0;
                }
                player.armorpoints[0] -= saved;
                damage -= saved;
            }
            player.health[0] -= damage;   // mirror mobj health here for Dave
            if (player.health[0] < 0) {
                player.health[0] = 0;
            }

            player.attacker = source;
            player.damagecount += damage;  // add damage after armor / invuln

            if (player.damagecount > 100) {
                player.damagecount = 100;  // teleport stomp does 10k points...
            }
            temp = damage < 100 ? damage : 100;

            if (player == getPlayer(ConsolePlayerNumber())) {
                doomSystem().Tactile(40, 10, 40 + temp * 2);
            }
        }

        // do the damage    
        target.health -= damage;
        if (target.health <= 0) {
            this.KillMobj(source, target);
            return;
        }

        if ((P_Random() < target.info.painchance)
            && !eval(target.flags & MF_SKULLFLY)) {
            target.flags |= MF_JUSTHIT;    // fight back!

            target.SetMobjState(target.info.painstate);
        }

        target.reactiontime = 0;       // we're awake now...   

        if (((target.threshold == 0) || (target.type == mobjtype_t.MT_VILE))
            && (source != null) && (source != target)
            && (source.type != mobjtype_t.MT_VILE)) {
            // if not intent on another player,
            // chase after this one
            target.target = source;
            target.threshold = BASETHRESHOLD;
            if (target.mobj_state == states[target.info.spawnstate.ordinal()]
                && target.info.seestate != statenum_t.S_NULL) {
                target.SetMobjState(target.info.seestate);
            }
        }

    }

    //
    // KillMobj
    //
    default void KillMobj(mobj_t source, mobj_t target) {
        mobjtype_t item;
        mobj_t mo;

        // Maes: this seems necessary in order for barrel damage
        // to propagate inflictors.
        target.target = source;

        target.flags &= ~(MF_SHOOTABLE | MF_FLOAT | MF_SKULLFLY);

        if (target.type != mobjtype_t.MT_SKULL) {
            target.flags &= ~MF_NOGRAVITY;
        }

        target.flags |= MF_CORPSE | MF_DROPOFF;
        target.height >>= 2;

        if (source != null && source.player != null) {
            // count for intermission
            if ((target.flags & MF_COUNTKILL) != 0) {
                source.player.killcount++;
            }

            if (target.player != null) //; <-- _D_: that semicolon caused a bug!
            {
                source.player.frags[target.player.identify()]++;
            }
            // It's probably intended to increment the frags of source player vs target player. Lookup? 
        } else if (!IsNetGame() && ((target.flags & MF_COUNTKILL) != 0)) {
            // count all monster deaths,
            // even those caused by other monsters
            getPlayer(0).killcount++;
        }

        if (target.player != null) {
            // count environment kills against you
            if (source == null) // TODO: some way to indentify which one of the 
            // four possiblelayers is the current player
            {
                target.player.frags[target.player.identify()]++;
            }

            target.flags &= ~MF_SOLID;
            target.player.playerstate = PST_DEAD;
            target.player.DropWeapon(); // in PSPR

            if (target.player == getPlayer(ConsolePlayerNumber()) && IsAutoMapActive()) {
                // don't die in auto map,
                // switch view prior to dying
                autoMap().Stop();
            }

        }

        if (target.health < -target.info.spawnhealth && target.info.xdeathstate != statenum_t.S_NULL) {
            target.SetMobjState(target.info.xdeathstate);
        } else {
            target.SetMobjState(target.info.deathstate);
        }
        target.mobj_tics -= P_Random() & 3;

        if (target.mobj_tics < 1) {
            target.mobj_tics = 1;
        }

        //  I_StartSound (&actor.r, actor.info.deathsound);
        // Drop stuff.
        // This determines the kind of object spawned
        // during the death frame of a thing.
        switch (target.type) {
            case MT_WOLFSS:
            case MT_POSSESSED:
                item = mobjtype_t.MT_CLIP;
                break;

            case MT_SHOTGUY:
                item = mobjtype_t.MT_SHOTGUN;
                break;

            case MT_CHAINGUY:
                item = mobjtype_t.MT_CHAINGUN;
                break;

            default:
                return;
        }

        mo = SpawnMobj(target.x, target.y, ONFLOORZ, item);
        mo.flags |= MF_DROPPED;    // special versions of items
    }

    @Override
    @SourceCode.Exact
    @SourceCode.P_Mobj.C(P_RemoveMobj)
    default void RemoveMobj(mobj_t mobj) {
        if (eval(mobj.flags & MF_SPECIAL)
            && !eval(mobj.flags & MF_DROPPED)
            && (mobj.type != mobjtype_t.MT_INV)
            && (mobj.type != mobjtype_t.MT_INS)) {
            final RespawnQueue resp = contextRequire(KEY_RESP_QUEUE);
            resp.itemrespawnque[resp.iquehead] = mobj.spawnpoint;
            resp.itemrespawntime[resp.iquehead] = LevelTime();
            resp.iquehead = (resp.iquehead + 1) & (ITEMQUESIZE - 1);

            // lose one off the end?
            if (resp.iquehead == resp.iquetail) {
                resp.iquetail = (resp.iquetail + 1) & (ITEMQUESIZE - 1);
            }
        }

        // unlink from sector and block lists
        P_UnsetThingPosition:
        {
            UnsetThingPosition(mobj);
        }

        // stop any playing sound
        S_StopSound:
        {
            StopSound(mobj);
        }

        // free block
        P_RemoveThinker:
        {
            RemoveThinker(mobj);
        }
    }

    /**
     * P_UnsetThingPosition Unlinks a thing from block map and sectors. On each
     * position change, BLOCKMAP and other lookups maintaining lists ot things
     * inside these structures need to be updated.
     */
    @Override
    @SourceCode.Exact
    @P_MapUtl.C(P_UnsetThingPosition)
    default void UnsetThingPosition(mobj_t thing) {
        final AbstractLevelLoader ll = levelLoader();
        final int blockx;
        final int blocky;

        if (!eval(thing.flags & MF_NOSECTOR)) {
            // inert things don't need to be in blockmap?
            // unlink from subsector
            if (thing.snext != null) {
                ((mobj_t) thing.snext).sprev = thing.sprev;
            }

            if (thing.sprev != null) {
                ((mobj_t) thing.sprev).snext = thing.snext;
            } else {
                thing.subsector.sector.thinglist = (mobj_t) thing.snext;
            }
        }

        if (!eval(thing.flags & MF_NOBLOCKMAP)) {
            // inert things don't need to be in blockmap
            // unlink from block map
            if (thing.bnext != null) {
                ((mobj_t) thing.bnext).bprev = thing.bprev;
            }

            if (thing.bprev != null) {
                ((mobj_t) thing.bprev).bnext = thing.bnext;
            } else {
                blockx = ll.getSafeBlockX(thing.x - ll.bmaporgx);
                blocky = ll.getSafeBlockY(thing.y - ll.bmaporgy);

                if (blockx >= 0 && blockx < ll.bmapwidth
                    && blocky >= 0 && blocky < ll.bmapheight) {
                    ll.blocklinks[blocky * ll.bmapwidth + blockx] = (mobj_t) thing.bnext;
                }
            }
        }
    }
}

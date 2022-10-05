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

import static org.bleachhack.util.doom.data.Defines.ITEMQUESIZE;
import static org.bleachhack.util.doom.data.Defines.ONCEILINGZ;
import static org.bleachhack.util.doom.data.Defines.ONFLOORZ;
import static org.bleachhack.util.doom.data.Limits.MAXPLAYERS;
import static org.bleachhack.util.doom.data.Tables.ANG45;
import static org.bleachhack.util.doom.data.info.mobjinfo;
import org.bleachhack.util.doom.data.mapthing_t;
import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.data.sounds;
import org.bleachhack.util.doom.doom.CommandVariable;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.SourceCode;
import org.bleachhack.util.doom.doom.SourceCode.CauseOfDesyncProbability;
import org.bleachhack.util.doom.doom.SourceCode.P_Spec;
import static org.bleachhack.util.doom.doom.SourceCode.P_Spec.P_SpawnSpecials;
import static org.bleachhack.util.doom.doom.SourceCode.P_Tick.P_RemoveThinker;
import org.bleachhack.util.doom.doom.thinker_t;
import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;

import org.bleachhack.util.doom.p.*;
import org.bleachhack.util.doom.p.ActiveStates.MobjConsumer;
import static org.bleachhack.util.doom.p.ActiveStates.NOP;
import org.bleachhack.util.doom.p.ActiveStates.ThinkerConsumer;
import static org.bleachhack.util.doom.p.DoorDefines.FASTDARK;
import static org.bleachhack.util.doom.p.DoorDefines.SLOWDARK;
import static org.bleachhack.util.doom.p.mobj_t.MF_SPAWNCEILING;
import org.bleachhack.util.doom.rr.sector_t;
import org.bleachhack.util.doom.rr.subsector_t;
import static org.bleachhack.util.doom.utils.C2JUtils.eval;

public interface ActionsThinkers extends ActionsSectors, ThinkerList {

    //
    // P_RemoveThinker
    // Deallocation is lazy -- it will not actually be freed
    // until its thinking turn comes up.
    //
    //
    // killough 4/25/98:
    //
    // Instead of marking the function with -1 value cast to a function pointer,
    // set the function to P_RemoveThinkerDelayed(), so that later, it will be
    // removed automatically as part of the thinker process.
    //
    @Override
    @SourceCode.Compatible("thinker->function.acv = (actionf_v)(-1)")
    @SourceCode.P_Tick.C(P_RemoveThinker)
    default void RemoveThinker(thinker_t thinker) {
        thinker.thinkerFunction = RemoveState.REMOVE;
    }

    /**
     * P_SpawnSpecials After the map has been loaded, scan for specials that spawn thinkers
     */
    @SourceCode.Suspicious(CauseOfDesyncProbability.LOW)
    @P_Spec.C(P_SpawnSpecials)
    default void SpawnSpecials() {
        final DoomMain<?, ?> D = DOOM();
        final AbstractLevelLoader ll = levelLoader();
        final UnifiedGameMap.Specials sp = getSpecials();
        sector_t sector;

        /*int     episode;

        episode = 1;
        if (W.CheckNumForName("texture2") >= 0)
        episode = 2;
         */
        // See if -TIMER needs to be used.
        sp.levelTimer = false;

        if (D.cVarManager.bool(CommandVariable.AVG) && IsDeathMatch()) {
            sp.levelTimer = true;
            sp.levelTimeCount = 20 * 60 * 35;
        }

        if (IsDeathMatch()) {
            D.cVarManager.with(CommandVariable.TIMER, 0, (Integer i) -> {
                sp.levelTimer = true;
                sp.levelTimeCount = i * 60 * 35;
            });
        }

        //  Init special SECTORs.
        //sector = LL.sectors;
        for (int i = 0; i < ll.numsectors; i++) {
            sector = ll.sectors[i];
            if (!eval(sector.special)) {
                continue;
            }

            switch (sector.special) {
                case 1:
                    // FLICKERING LIGHTS
                    P_SpawnLightFlash: {
                        SpawnLightFlash(sector);
                    }
                    break;

                case 2:
                    // STROBE FAST
                    P_SpawnStrobeFlash: {
                        SpawnStrobeFlash(sector, FASTDARK, 0);
                    }
                    break;

                case 3:
                    // STROBE SLOW
                    P_SpawnStrobeFlash: {
                        SpawnStrobeFlash(sector, SLOWDARK, 0);
                    }
                    break;

                case 4:
                    // STROBE FAST/DEATH SLIME
                    P_SpawnStrobeFlash: {
                        SpawnStrobeFlash(sector, FASTDARK, 0);
                    }
                    sector.special = 4;
                    break;

                case 8:
                    // GLOWING LIGHT
                    P_SpawnGlowingLight: {
                        SpawnGlowingLight(sector);
                    }
                    break;
                case 9:
                    // SECRET SECTOR
                    D.totalsecret++;
                    break;

                case 10:
                    // DOOR CLOSE IN 30 SECONDS
                    SpawnDoorCloseIn30: {
                        SpawnDoorCloseIn30(sector);
                    }
                    break;

                case 12:
                    // SYNC STROBE SLOW
                    P_SpawnStrobeFlash: {
                        SpawnStrobeFlash(sector, SLOWDARK, 1);
                    }
                    break;

                case 13:
                    // SYNC STROBE FAST
                    P_SpawnStrobeFlash: {
                        SpawnStrobeFlash(sector, FASTDARK, 1);
                    }
                    break;

                case 14:
                    // DOOR RAISE IN 5 MINUTES
                    P_SpawnDoorRaiseIn5Mins: {
                        SpawnDoorRaiseIn5Mins(sector, i);
                    }
                    break;

                case 17:
                    P_SpawnFireFlicker: {
                        SpawnFireFlicker(sector);
                    }
                    break;
            }
        }

        //  Init line EFFECTs
        sp.numlinespecials = 0;
        for (int i = 0; i < ll.numlines; i++) {
            switch (ll.lines[i].special) {
                case 48:
                    // EFFECT FIRSTCOL SCROLL+
                    // Maes 6/4/2012: removed length limit.
                    if (sp.numlinespecials == sp.linespeciallist.length) {
                        sp.resizeLinesSpecialList();
                    }
                    sp.linespeciallist[sp.numlinespecials] = ll.lines[i];
                    sp.numlinespecials++;
                    break;
            }
        }

        //  Init other misc stuff
        for (int i = 0; i < this.getMaxCeilings(); i++) {
            this.getActiveCeilings()[i] = null;
        }
        
        getSwitches().initButtonList();

        // UNUSED: no horizonal sliders.
        // if (SL!=null) {
        // SL.updateStatus(DM);
        //  SL.P_InitSlidingDoorFrames();
        //}
    }

    /**
     * P_RespawnSpecials
     */
    default void RespawnSpecials() {
        final RespawnQueue resp = contextRequire(KEY_RESP_QUEUE);
        int x, y, z; // fixed

        subsector_t ss;
        mobj_t mo;
        mapthing_t mthing;

        int i;

        // only respawn items in deathmatch (deathmatch!=2)
        if (!DOOM().altdeath) {
            return; // 
        }
        // nothing left to respawn?
        if (resp.iquehead == resp.iquetail) {
            return;
        }

        // wait at least 30 seconds
        if (LevelTime() - resp.itemrespawntime[resp.iquetail] < 30 * 35) {
            return;
        }

        mthing = resp.itemrespawnque[resp.iquetail];

        x = mthing.x << FRACBITS;
        y = mthing.y << FRACBITS;

        // spawn a teleport fog at the new spot
        ss = levelLoader().PointInSubsector(x, y);
        mo = SpawnMobj(x, y, ss.sector.floorheight, mobjtype_t.MT_IFOG);
        StartSound(mo, sounds.sfxenum_t.sfx_itmbk);

        // find which type to spawn
        for (i = 0; i < mobjtype_t.NUMMOBJTYPES.ordinal(); i++) {
            if (mthing.type == mobjinfo[i].doomednum) {
                break;
            }
        }

        // spawn it
        if (eval(mobjinfo[i].flags & MF_SPAWNCEILING)) {
            z = ONCEILINGZ;
        } else {
            z = ONFLOORZ;
        }

        mo = SpawnMobj(x, y, z, mobjtype_t.values()[i]);
        mo.spawnpoint = mthing;
        mo.angle = ANG45 * (mthing.angle / 45);

        // pull it from the que
        resp.iquetail = (resp.iquetail + 1) & (ITEMQUESIZE - 1);
    }

    //
    // P_AllocateThinker
    // Allocates memory and adds a new thinker at the end of the list.
    //
    //public void AllocateThinker(thinker_t thinker) {;
    // UNUSED
    //}
    //
    // P_RunThinkers
    //
    default void RunThinkers() {
        thinker_t thinker = getThinkerCap().next;
        while (thinker != getThinkerCap()) {
            if (thinker.thinkerFunction == RemoveState.REMOVE) {
                // time to remove it
                thinker.next.prev = thinker.prev;
                thinker.prev.next = thinker.next;
                // Z_Free (currentthinker);
            } else {
                ActiveStates thinkerFunction = (ActiveStates)thinker.thinkerFunction;
                if (thinkerFunction.isParamType(MobjConsumer.class)) {
                    thinkerFunction.fun(MobjConsumer.class).accept(DOOM().actions, (mobj_t) thinker);
                } else if (thinkerFunction.isParamType(ThinkerConsumer.class)) {
                    thinkerFunction.fun(ThinkerConsumer.class).accept(DOOM().actions, thinker);
                }
            }
            thinker = thinker.next;
        }
    }

    //
    //P_Ticker
    //
    default void Ticker() {
        // run the tic
        if (IsPaused()) {
            return;
        }

        // pause if in menu and at least one tic has been run
        if (!IsNetGame() && IsMenuActive() && !IsDemoPlayback() && getPlayer(ConsolePlayerNumber()).viewz != 1) {
            return;
        }

        for (int i = 0; i < MAXPLAYERS; i++) {
            if (PlayerInGame(i)) {
                getPlayer(i).PlayerThink();
            }
        }

        RunThinkers();
        getSpecials().UpdateSpecials(); // In specials. Merge?
        RespawnSpecials();

        // for par times
        DOOM().leveltime++;
    }
}

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

import static org.bleachhack.util.doom.data.Limits.CEILSPEED;
import static org.bleachhack.util.doom.data.Limits.MAXCEILINGS;
import org.bleachhack.util.doom.data.sounds;
import org.bleachhack.util.doom.doom.SourceCode.P_Ceiling;
import static org.bleachhack.util.doom.doom.SourceCode.P_Ceiling.EV_DoCeiling;
import org.bleachhack.util.doom.doom.thinker_t;
import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import org.bleachhack.util.doom.p.ActiveStates;
import org.bleachhack.util.doom.p.ceiling_e;
import org.bleachhack.util.doom.p.ceiling_t;
import org.bleachhack.util.doom.p.result_e;
import org.bleachhack.util.doom.rr.line_t;
import org.bleachhack.util.doom.rr.sector_t;
import org.bleachhack.util.doom.utils.C2JUtils;
import static org.bleachhack.util.doom.utils.C2JUtils.eval;
import org.bleachhack.util.doom.utils.TraitFactory.ContextKey;

public interface ActionsCeilings extends ActionsMoveEvents, ActionsUseEvents {

    ContextKey<Ceilings> KEY_CEILINGS = ACTION_KEY_CHAIN.newKey(ActionsCeilings.class, Ceilings::new);

    void RemoveThinker(thinker_t activeCeiling);
    result_e MovePlane(sector_t sector, int speed, int bottomheight, boolean crush, int i, int direction);
    int FindSectorFromLineTag(line_t line, int secnum);

    final class Ceilings {

        ceiling_t[] activeceilings = new ceiling_t[MAXCEILINGS];
    }

    /**
     * This needs to be called before loading, otherwise crushers won't be able to be restarted.
     */
    default void ClearCeilingsBeforeLoading() {
        contextRequire(KEY_CEILINGS).activeceilings = new ceiling_t[MAXCEILINGS];
    }

    /**
     * T_MoveCeiling
     */
    default void MoveCeiling(ceiling_t ceiling) {
        result_e res;

        switch (ceiling.direction) {
            case 0:
                // IN STASIS
                break;
            case 1:
                // UP
                res = MovePlane(ceiling.sector, ceiling.speed, ceiling.topheight, false, 1, ceiling.direction);

                if (!eval(LevelTime() & 7)) {
                    switch (ceiling.type) {
                        case silentCrushAndRaise:
                            break;
                        default:
                            StartSound(ceiling.sector.soundorg, sounds.sfxenum_t.sfx_stnmov);
                    }
                }

                if (res == result_e.pastdest) {
                    switch (ceiling.type) {
                        case raiseToHighest:
                            this.RemoveActiveCeiling(ceiling);
                            break;
                        case silentCrushAndRaise:
                            StartSound(ceiling.sector.soundorg, sounds.sfxenum_t.sfx_pstop);
                        case fastCrushAndRaise:
                        case crushAndRaise:
                            ceiling.direction = -1;
                        default:
                        	break;
                    }
                }
                break;

            case -1:
                // DOWN
                res = MovePlane(ceiling.sector, ceiling.speed, ceiling.bottomheight, ceiling.crush, 1, ceiling.direction);

                if (!eval(LevelTime() & 7)) {
                    switch (ceiling.type) {
                        case silentCrushAndRaise:
                            break;
                        default:
                            StartSound(ceiling.sector.soundorg, sounds.sfxenum_t.sfx_stnmov);
                    }
                }

                if (res == result_e.pastdest) {
                    switch (ceiling.type) {
                        case silentCrushAndRaise:
                            StartSound(ceiling.sector.soundorg, sounds.sfxenum_t.sfx_pstop);
                        case crushAndRaise:
                            ceiling.speed = CEILSPEED;
                        case fastCrushAndRaise:
                            ceiling.direction = 1;
                            break;
                        case lowerAndCrush:
                        case lowerToFloor:
                            RemoveActiveCeiling(ceiling);
                            break;
                        default:
                            break;
                    }
                } else { // ( res != result_e.pastdest )
                    if (res == result_e.crushed) {
                        switch (ceiling.type) {
                            case silentCrushAndRaise:
                            case crushAndRaise:
                            case lowerAndCrush:
                                ceiling.speed = CEILSPEED / 8;
                                break;
                            default:
                                break;
                        }
                    }
                }
        }
    }

    //
    // EV.DoCeiling
    // Move a ceiling up/down and all around!
    //
    @Override
    @P_Ceiling.C(EV_DoCeiling)
    default boolean DoCeiling(line_t line, ceiling_e type) {
        int secnum = -1;
        boolean rtn = false;
        sector_t sec;
        ceiling_t ceiling;

        //  Reactivate in-stasis ceilings...for certain types.
        switch (type) {
            case fastCrushAndRaise:
            case silentCrushAndRaise:
            case crushAndRaise:
                ActivateInStasisCeiling(line);
            default:
                break;
        }

        while ((secnum = FindSectorFromLineTag(line, secnum)) >= 0) {
            sec = levelLoader().sectors[secnum];
            if (sec.specialdata != null) {
                continue;
            }

            // new door thinker
            rtn = true;
            ceiling = new ceiling_t();
            sec.specialdata = ceiling;
            ceiling.thinkerFunction = ActiveStates.T_MoveCeiling;
            AddThinker(ceiling);
            ceiling.sector = sec;
            ceiling.crush = false;

            switch (type) {
                case fastCrushAndRaise:
                    ceiling.crush = true;
                    ceiling.topheight = sec.ceilingheight;
                    ceiling.bottomheight = sec.floorheight + (8 * FRACUNIT);
                    ceiling.direction = -1;
                    ceiling.speed = CEILSPEED * 2;
                    break;

                case silentCrushAndRaise:
                case crushAndRaise:
                    ceiling.crush = true;
                    ceiling.topheight = sec.ceilingheight;
                case lowerAndCrush:
                case lowerToFloor:
                    ceiling.bottomheight = sec.floorheight;
                    if (type != ceiling_e.lowerToFloor) {
                        ceiling.bottomheight += 8 * FRACUNIT;
                    }
                    ceiling.direction = -1;
                    ceiling.speed = CEILSPEED;
                    break;

                case raiseToHighest:
                    ceiling.topheight = sec.FindHighestCeilingSurrounding();
                    ceiling.direction = 1;
                    ceiling.speed = CEILSPEED;
                    break;
            }

            ceiling.tag = sec.tag;
            ceiling.type = type;
            AddActiveCeiling(ceiling);
        }
        return rtn;
    }

    //
    // Add an active ceiling
    //
    default void AddActiveCeiling(ceiling_t c) {
        final ceiling_t[] activeCeilings = getActiveCeilings();
        for (int i = 0; i < activeCeilings.length; ++i) {
            if (activeCeilings[i] == null) {
                activeCeilings[i] = c;
                return;
            }
        }
        // Needs rezising
        setActiveceilings(C2JUtils.resize(c, activeCeilings, 2 * activeCeilings.length));
    }

    //
    // Remove a ceiling's thinker
    //
    default void RemoveActiveCeiling(ceiling_t c) {
        final ceiling_t[] activeCeilings = getActiveCeilings();
        for (int i = 0; i < activeCeilings.length; ++i) {
            if (activeCeilings[i] == c) {
                activeCeilings[i].sector.specialdata = null;
                RemoveThinker(activeCeilings[i]);
                activeCeilings[i] = null;
                break;
            }
        }
    }

    //
    // Restart a ceiling that's in-stasis
    //
    default void ActivateInStasisCeiling(line_t line) {
        final ceiling_t[] activeCeilings = getActiveCeilings();
        for (int i = 0; i < activeCeilings.length; ++i) {
            if (activeCeilings[i] != null
                && (activeCeilings[i].tag == line.tag)
                && (activeCeilings[i].direction == 0)) {
                activeCeilings[i].direction = activeCeilings[i].olddirection;
                activeCeilings[i].thinkerFunction = ActiveStates.T_MoveCeiling;
            }
        }
    }

    //
    // EV_CeilingCrushStop
    // Stop a ceiling from crushing!
    //
    @Override
    default int CeilingCrushStop(line_t line) {
        int i;
        int rtn;

        rtn = 0;
        final ceiling_t[] activeCeilings = getActiveCeilings();
        for (i = 0; i < activeCeilings.length; ++i) {
            if (activeCeilings[i] != null
                && (activeCeilings[i].tag == line.tag)
                && (activeCeilings[i].direction != 0)) {
                activeCeilings[i].olddirection = activeCeilings[i].direction;
                activeCeilings[i].thinkerFunction = ActiveStates.NOP;
                activeCeilings[i].direction = 0;       // in-stasis
                rtn = 1;
            }
        }

        return rtn;
    }

    default void setActiveceilings(ceiling_t[] activeceilings) {
        contextRequire(KEY_CEILINGS).activeceilings = activeceilings;
    }

    default ceiling_t[] getActiveCeilings() {
        return contextRequire(KEY_CEILINGS).activeceilings;
    }

    default int getMaxCeilings() {
        return contextRequire(KEY_CEILINGS).activeceilings.length;
    }
}

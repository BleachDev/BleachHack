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

import static org.bleachhack.util.doom.data.Limits.MAXPLAYERS;
import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.thinker_t;
import org.bleachhack.util.doom.p.Actions.ActionTrait;
import org.bleachhack.util.doom.p.ActiveStates;
import org.bleachhack.util.doom.p.floor_e;
import org.bleachhack.util.doom.p.mobj_t;
import org.bleachhack.util.doom.p.vldoor_e;
import org.bleachhack.util.doom.rr.line_t;

public interface Bosses extends ActionTrait {
    void A_Fall(mobj_t mo);
    
    /**
     * A_BossDeath
     * Possibly trigger special effects
     * if on first boss level
     *
     * TODO: find out how Plutonia/TNT does cope with this.
     * Special clauses?
     *
     */
    default void A_BossDeath(mobj_t mo) {
        final DoomMain<?, ?> D = DOOM();
        thinker_t th;
        mobj_t mo2;
        line_t junk = new line_t();
        int i;

        if (D.isCommercial()) {
            if (D.gamemap != 7) {
                return;
            }

            if ((mo.type != mobjtype_t.MT_FATSO)
                && (mo.type != mobjtype_t.MT_BABY)) {
                return;
            }
        } else {
            switch (D.gameepisode) {
                case 1:
                    if (D.gamemap != 8) {
                        return;
                    }

                    if (mo.type != mobjtype_t.MT_BRUISER) {
                        return;
                    }
                    break;

                case 2:
                    if (D.gamemap != 8) {
                        return;
                    }

                    if (mo.type != mobjtype_t.MT_CYBORG) {
                        return;
                    }
                    break;

                case 3:
                    if (D.gamemap != 8) {
                        return;
                    }

                    if (mo.type != mobjtype_t.MT_SPIDER) {
                        return;
                    }

                    break;

                case 4:
                    switch (D.gamemap) {
                        case 6:
                            if (mo.type != mobjtype_t.MT_CYBORG) {
                                return;
                            }
                            break;

                        case 8:
                            if (mo.type != mobjtype_t.MT_SPIDER) {
                                return;
                            }
                            break;

                        default:
                            return;
                    }
                    break;

                default:
                    if (D.gamemap != 8) {
                        return;
                    }
                    break;
            }

        }

        // make sure there is a player alive for victory
        for (i = 0; i < MAXPLAYERS; i++) {
            if (D.playeringame[i] && D.players[i].health[0] > 0) {
                break;
            }
        }

        if (i == MAXPLAYERS) {
            return; // no one left alive, so do not end game
        }
        // scan the remaining thinkers to see
        // if all bosses are dead
        for (th = getThinkerCap().next; th != getThinkerCap(); th = th.next) {
            if (th.thinkerFunction != ActiveStates.P_MobjThinker) {
                continue;
            }

            mo2 = (mobj_t) th;
            if (mo2 != mo
                && mo2.type == mo.type
                && mo2.health > 0) {
                // other boss not dead
                return;
            }
        }

        // victory!
        if (D.isCommercial()) {
            if (D.gamemap == 7) {
                if (mo.type == mobjtype_t.MT_FATSO) {
                    junk.tag = 666;
                    getThinkers().DoFloor(junk, floor_e.lowerFloorToLowest);
                    return;
                }

                if (mo.type == mobjtype_t.MT_BABY) {
                    junk.tag = 667;
                    getThinkers().DoFloor(junk, floor_e.raiseToTexture);
                    return;
                }
            }
        } else {
            switch (D.gameepisode) {
                case 1:
                    junk.tag = 666;
                    getThinkers().DoFloor(junk, floor_e.lowerFloorToLowest);
                    return;

                case 4:
                    switch (D.gamemap) {
                        case 6:
                            junk.tag = 666;
                            getThinkers().DoDoor(junk, vldoor_e.blazeOpen);
                            return;

                        case 8:
                            junk.tag = 666;
                            getThinkers().DoFloor(junk, floor_e.lowerFloorToLowest);
                            return;
                    }
            }
        }

        D.ExitLevel();
    }
    
    default void A_KeenDie(mobj_t mo) {
        thinker_t th;
        mobj_t mo2;
        line_t junk = new line_t(); // MAES: fixed null 21/5/2011

        A_Fall(mo);

        // scan the remaining thinkers
        // to see if all Keens are dead
        for (th = getThinkerCap().next; th != getThinkerCap(); th = th.next) {
            if (th.thinkerFunction != ActiveStates.P_MobjThinker) {
                continue;
            }

            mo2 = (mobj_t) th;
            if (mo2 != mo
                && mo2.type == mo.type
                && mo2.health > 0) {
                // other Keen not dead
                return;
            }
        }

        junk.tag = 666;
        getThinkers().DoDoor(junk, vldoor_e.open);
    }

}

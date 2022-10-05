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

import org.bleachhack.util.doom.data.sounds;
import org.bleachhack.util.doom.defines.card_t;
import org.bleachhack.util.doom.doom.SourceCode;
import org.bleachhack.util.doom.doom.SourceCode.P_Doors;
import static org.bleachhack.util.doom.doom.SourceCode.P_Doors.P_SpawnDoorCloseIn30;
import static org.bleachhack.util.doom.doom.SourceCode.P_Doors.P_SpawnDoorRaiseIn5Mins;
import static org.bleachhack.util.doom.doom.englsh.PD_BLUEK;
import static org.bleachhack.util.doom.doom.englsh.PD_BLUEO;
import static org.bleachhack.util.doom.doom.englsh.PD_REDK;
import static org.bleachhack.util.doom.doom.englsh.PD_REDO;
import static org.bleachhack.util.doom.doom.englsh.PD_YELLOWK;
import static org.bleachhack.util.doom.doom.englsh.PD_YELLOWO;
import org.bleachhack.util.doom.doom.player_t;
import org.bleachhack.util.doom.doom.thinker_t;
import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import org.bleachhack.util.doom.p.ActiveStates;
import static org.bleachhack.util.doom.p.ActiveStates.T_VerticalDoor;
import static org.bleachhack.util.doom.p.DoorDefines.VDOORSPEED;
import static org.bleachhack.util.doom.p.DoorDefines.VDOORWAIT;
import org.bleachhack.util.doom.p.mobj_t;
import org.bleachhack.util.doom.p.plat_t;
import org.bleachhack.util.doom.p.result_e;
import org.bleachhack.util.doom.p.vldoor_e;
import org.bleachhack.util.doom.p.vldoor_t;
import org.bleachhack.util.doom.rr.line_t;
import org.bleachhack.util.doom.rr.sector_t;
import static org.bleachhack.util.doom.utils.C2JUtils.eval;

public interface ActionsDoors extends ActionsMoveEvents, ActionsUseEvents {

    result_e MovePlane(sector_t sector, int speed, int floorheight, boolean b, int i, int direction);
    void RemoveThinker(thinker_t door);
    int FindSectorFromLineTag(line_t line, int secnum);

    //
    // VERTICAL DOORS
    //
    /**
     * T_VerticalDoor
     */
    default void VerticalDoor(vldoor_t door) {
        switch (door.direction) {
            case 0:
                // WAITING
                if (!eval(--door.topcountdown)) {
                    switch (door.type) {
                        case blazeRaise:
                            door.direction = -1; // time to go back down
                            StartSound(door.sector.soundorg, sounds.sfxenum_t.sfx_bdcls);
                            break;
                        case normal:
                            door.direction = -1; // time to go back down
                            StartSound(door.sector.soundorg, sounds.sfxenum_t.sfx_dorcls);
                            break;
                        case close30ThenOpen:
                            door.direction = 1;
                            StartSound(door.sector.soundorg, sounds.sfxenum_t.sfx_doropn);
                            break;
                        default:
                        	break;
                    }
                }
                break;

            case 2:
                //  INITIAL WAIT
                if (!eval(--door.topcountdown)) {
                    switch (door.type) {
                        case raiseIn5Mins:
                            door.direction = 1;
                            door.type = vldoor_e.normal;
                            StartSound(door.sector.soundorg, sounds.sfxenum_t.sfx_doropn);
                            break;
                        default:
                        	break;
                    }
                }
                break;

            case -1: {
                // DOWN
                final result_e res = MovePlane(door.sector, door.speed, door.sector.floorheight, false, 1, door.direction);
                if (res == result_e.pastdest) {
                    switch (door.type) {
                        case blazeRaise:
                        case blazeClose:
                            door.sector.specialdata = null;
                            RemoveThinker(door);  // unlink and free
                            StartSound(door.sector.soundorg, sounds.sfxenum_t.sfx_bdcls);
                            break;
                        case normal:
                        case close:
                            door.sector.specialdata = null;
                            RemoveThinker(door);  // unlink and free
                            break;
                        case close30ThenOpen:
                            door.direction = 0;
                            door.topcountdown = 35 * 30;
                            break;
                        default:
                        	break;
                    }
                } else if (res == result_e.crushed) {
                    switch (door.type) {
                        case blazeClose:
                        case close: // DO NOT GO BACK UP!
                            break;
                        default:
                            door.direction = 1;
                            StartSound(door.sector.soundorg, sounds.sfxenum_t.sfx_doropn);
                    }
                }
                break;
            }
            case 1: {
                // UP
                final result_e res = this.MovePlane(door.sector, door.speed, door.topheight, false, 1, door.direction);

                if (res == result_e.pastdest) {
                    switch (door.type) {
                        case blazeRaise:
                        case normal:
                            door.direction = 0; // wait at top
                            door.topcountdown = door.topwait;
                            break;
                        case close30ThenOpen:
                        case blazeOpen:
                        case open:
                            door.sector.specialdata = null;
                            RemoveThinker(door);  // unlink and free
                            break;
                        default:
                        	break;
                    }
                }
                break;
            }
        }
    }

    /**
     * EV_DoLockedDoor Move a locked door up/down
     */
    @Override
    default boolean DoLockedDoor(line_t line, vldoor_e type, mobj_t thing) {
        player_t p;

        p = thing.player;

        if (p == null) {
            return false;
        }

        switch (line.special) {
            case 99: // Blue Lock
            case 133:
                /*         if ( p==null )
             return false; */
                if (!p.cards[card_t.it_bluecard.ordinal()] && !p.cards[card_t.it_blueskull.ordinal()]) {
                    p.message = PD_BLUEO;
                    StartSound(null, sounds.sfxenum_t.sfx_oof);
                    return false;
                }
                break;

            case 134: // Red Lock
            case 135:
                /*        if ( p==null )
             return false; */
                if (!p.cards[card_t.it_redcard.ordinal()] && !p.cards[card_t.it_redskull.ordinal()]) {
                    p.message = PD_REDO;
                    StartSound(null, sounds.sfxenum_t.sfx_oof);
                    return false;
                }
                break;

            case 136:    // Yellow Lock
            case 137:
                /*        if ( p==null )
             return false; */
                if (!p.cards[card_t.it_yellowcard.ordinal()]
                    && !p.cards[card_t.it_yellowskull.ordinal()]) {
                    p.message = PD_YELLOWO;
                    StartSound(null, sounds.sfxenum_t.sfx_oof);
                    return false;
                }
                break;
        }

        return DoDoor(line, type);
    }

    @Override
    default boolean DoDoor(line_t line, vldoor_e type) {
        int secnum;
        boolean rtn = false;
        sector_t sec;
        vldoor_t door;

        secnum = -1;

        while ((secnum = FindSectorFromLineTag(line, secnum)) >= 0) {
            sec = levelLoader().sectors[secnum];
            if (sec.specialdata != null) {
                continue;
            }

            // new door thinker
            rtn = true;
            door = new vldoor_t();
            sec.specialdata = door;
            door.thinkerFunction = ActiveStates.T_VerticalDoor;
            AddThinker(door);
            door.sector = sec;
            door.type = type;
            door.topwait = VDOORWAIT;
            door.speed = VDOORSPEED;

            switch (type) {
                case blazeClose:
                    door.topheight = sec.FindLowestCeilingSurrounding();
                    door.topheight -= 4 * FRACUNIT;
                    door.direction = -1;
                    door.speed = VDOORSPEED * 4;
                    StartSound(door.sector.soundorg, sounds.sfxenum_t.sfx_bdcls);
                    break;
                case close:
                    door.topheight = sec.FindLowestCeilingSurrounding();
                    door.topheight -= 4 * FRACUNIT;
                    door.direction = -1;
                    StartSound(door.sector.soundorg, sounds.sfxenum_t.sfx_dorcls);
                    break;
                case close30ThenOpen:
                    door.topheight = sec.ceilingheight;
                    door.direction = -1;
                    StartSound(door.sector.soundorg, sounds.sfxenum_t.sfx_dorcls);
                    break;
                case blazeRaise:
                case blazeOpen:
                    door.direction = 1;
                    door.topheight = sec.FindLowestCeilingSurrounding();
                    door.topheight -= 4 * FRACUNIT;
                    door.speed = VDOORSPEED * 4;
                    if (door.topheight != sec.ceilingheight) {
                        StartSound(door.sector.soundorg, sounds.sfxenum_t.sfx_bdopn);
                    }
                    break;
                case normal:
                case open:
                    door.direction = 1;
                    door.topheight = sec.FindLowestCeilingSurrounding();
                    door.topheight -= 4 * FRACUNIT;
                    if (door.topheight != sec.ceilingheight) {
                        StartSound(door.sector.soundorg, sounds.sfxenum_t.sfx_doropn);
                    }
                default:
                	break;
            }

        }
        return rtn;
    }

    /**
     * EV_VerticalDoor : open a door manually, no tag value
     */
    @Override
    default void VerticalDoor(line_t line, mobj_t thing) {
        player_t player;
        //int      secnum;
        sector_t sec;
        vldoor_t door;
        int side;

        side = 0;  // only front sides can be used

        // Check for locks
        player = thing.player;

        switch (line.special) {
            case 26: // Blue Lock
            case 32:
                if (player == null) {
                    return;
                }

                if (!player.cards[card_t.it_bluecard.ordinal()] && !player.cards[card_t.it_blueskull.ordinal()]) {
                    player.message = PD_BLUEK;
                    StartSound(null, sounds.sfxenum_t.sfx_oof);
                    return;
                }
                break;

            case 27: // Yellow Lock
            case 34:
                if (player == null) {
                    return;
                }

                if (!player.cards[card_t.it_yellowcard.ordinal()] && !player.cards[card_t.it_yellowskull.ordinal()]) {
                    player.message = PD_YELLOWK;
                    StartSound(null, sounds.sfxenum_t.sfx_oof);
                    return;
                }
                break;

            case 28: // Red Lock
            case 33:
                if (player == null) {
                    return;
                }

                if (!player.cards[card_t.it_redcard.ordinal()] && !player.cards[card_t.it_redskull.ordinal()]) {
                    player.message = PD_REDK;
                    StartSound(null, sounds.sfxenum_t.sfx_oof);
                    return;
                }
                break;
        }

        // if the sector has an active thinker, use it
        sec = levelLoader().sides[line.sidenum[side ^ 1]].sector;
        // secnum = sec.id;

        if (sec.specialdata != null) {
            if (sec.specialdata instanceof plat_t) {
                /**
                 * [MAES]: demo sync for e1nm0646: emulates active plat_t interpreted
                 * as door. TODO: add our own overflow handling class.
                 */
                door = ((plat_t) sec.specialdata).asVlDoor(levelLoader().sectors);
            } else {
                door = (vldoor_t) sec.specialdata;
            }
            switch (line.special) {
                case 1: // ONLY FOR "RAISE" DOORS, NOT "OPEN"s
                case 26:
                case 27:
                case 28:
                case 117:
                    if (door.direction == -1) {
                        door.direction = 1; // go back up
                    } else {
                        if (thing.player == null) {
                            return;     // JDC: bad guys never close doors
                        }
                        door.direction = -1;    // start going down immediately
                    }
                    return;
            }
        }

        // for proper sound
        switch (line.special) {
            case 117:    // BLAZING DOOR RAISE
            case 118: // BLAZING DOOR OPEN
                StartSound(sec.soundorg, sounds.sfxenum_t.sfx_bdopn);
                break;

            case 1:  // NORMAL DOOR SOUND
            case 31:
                StartSound(sec.soundorg, sounds.sfxenum_t.sfx_doropn);
                break;

            default: // LOCKED DOOR SOUND
                StartSound(sec.soundorg, sounds.sfxenum_t.sfx_doropn);
                break;
        }

        // new door thinker
        door = new vldoor_t();
        sec.specialdata = door;
        door.thinkerFunction = ActiveStates.T_VerticalDoor;
        AddThinker(door);
        door.sector = sec;
        door.direction = 1;
        door.speed = VDOORSPEED;
        door.topwait = VDOORWAIT;

        switch (line.special) {
            case 1:
            case 26:
            case 27:
            case 28:
                door.type = vldoor_e.normal;
                break;
            case 31:
            case 32:
            case 33:
            case 34:
                door.type = vldoor_e.open;
                line.special = 0;
                break;
            case 117: // blazing door raise
                door.type = vldoor_e.blazeRaise;
                door.speed = VDOORSPEED * 4;
                break;
            case 118: // blazing door open
                door.type = vldoor_e.blazeOpen;
                line.special = 0;
                door.speed = VDOORSPEED * 4;
        }

        // find the top and bottom of the movement range
        door.topheight = sec.FindLowestCeilingSurrounding();
        door.topheight -= 4 * FRACUNIT;
    }
    
    //
    // Spawn a door that closes after 30 seconds
    //
    @SourceCode.Exact
    @P_Doors.C(P_SpawnDoorCloseIn30)
    default void SpawnDoorCloseIn30(sector_t sector) {
        vldoor_t door;

        Z_Malloc: {
            door = new vldoor_t();
        }

        P_AddThinker: {
            AddThinker(door);
        }

        sector.specialdata = door;
        sector.special = 0;

        door.thinkerFunction = T_VerticalDoor;
        door.sector = sector;
        door.direction = 0;
        door.type = vldoor_e.normal;
        door.speed = VDOORSPEED;
        door.topcountdown = 30 * 35;
    }

    /**
     * Spawn a door that opens after 5 minutes
     */
    @SourceCode.Exact
    @P_Doors.C(P_SpawnDoorRaiseIn5Mins)
    default void SpawnDoorRaiseIn5Mins(sector_t sector, int secnum) {
        vldoor_t door;

        Z_Malloc: {
            door = new vldoor_t();
        }

        P_AddThinker: {
            AddThinker(door);
        }
        
        sector.specialdata = door;
        sector.special = 0;
        
        door.thinkerFunction = T_VerticalDoor;
        door.sector = sector;
        door.direction = 2;
        door.type = vldoor_e.raiseIn5Mins;
        door.speed = VDOORSPEED;
        P_FindLowestCeilingSurrounding: {
            door.topheight = sector.FindLowestCeilingSurrounding();
        }
        door.topheight -= 4 * FRACUNIT;
        door.topwait = VDOORWAIT;
        door.topcountdown = 5 * 60 * 35;
    }
}

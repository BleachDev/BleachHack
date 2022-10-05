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

import static org.bleachhack.util.doom.data.Defines.PT_ADDLINES;
import static org.bleachhack.util.doom.data.Defines.USERANGE;
import org.bleachhack.util.doom.data.Tables;
import static org.bleachhack.util.doom.data.Tables.finecosine;
import static org.bleachhack.util.doom.data.Tables.finesine;
import org.bleachhack.util.doom.data.sounds;
import org.bleachhack.util.doom.doom.SourceCode.P_Map;
import static org.bleachhack.util.doom.doom.SourceCode.P_Map.PTR_UseTraverse;
import org.bleachhack.util.doom.doom.player_t;
import java.util.function.Predicate;
import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;
import org.bleachhack.util.doom.p.ceiling_e;
import org.bleachhack.util.doom.p.floor_e;
import org.bleachhack.util.doom.p.intercept_t;
import org.bleachhack.util.doom.p.mobj_t;
import org.bleachhack.util.doom.p.plattype_e;
import org.bleachhack.util.doom.p.stair_e;
import org.bleachhack.util.doom.p.vldoor_e;
import org.bleachhack.util.doom.rr.line_t;
import static org.bleachhack.util.doom.rr.line_t.ML_SECRET;
import static org.bleachhack.util.doom.utils.C2JUtils.eval;

public interface ActionsUseEvents extends ActionTrait {

    void VerticalDoor(line_t line, mobj_t thing);
    void LightTurnOn(line_t line, int i);
    boolean BuildStairs(line_t line, stair_e stair_e);
    boolean DoDonut(line_t line);
    boolean DoFloor(line_t line, floor_e floor_e);
    boolean DoDoor(line_t line, vldoor_e vldoor_e);
    boolean DoPlat(line_t line, plattype_e plattype_e, int i);
    boolean DoCeiling(line_t line, ceiling_e ceiling_e);
    boolean DoLockedDoor(line_t line, vldoor_e vldoor_e, mobj_t thing);
    boolean PathTraverse(int x1, int y1, int x2, int y2, int flags, Predicate<intercept_t> trav);

    /**
     * P_UseSpecialLine Called when a thing uses a special line. Only the front sides of lines are usable.
     */
    default boolean UseSpecialLine(mobj_t thing, line_t line, boolean side) {
        // Err...
        // Use the back sides of VERY SPECIAL lines...
        if (side) {
            switch (line.special) {
                case 124:
                    // Sliding door open&close
                    // SL.EV_SlidingDoor(line, thing);
                    break;

                default:
                    return false;
                //break;
            }
        }

        // Switches that other things can activate.
        //_D_: little bug fixed here, see linuxdoom source
        if (thing.player ==/*!=*/ null) {
            // never open secret doors
            if (eval(line.flags & ML_SECRET)) {
                return false;
            }

            switch (line.special) {
                case 1:   // MANUAL DOOR RAISE
                case 32:  // MANUAL BLUE
                case 33:  // MANUAL RED
                case 34: // MANUAL YELLOW
                    break;

                default:
                    return false;
                //break;
            }
        }

        // do something  
        switch (line.special) {
            // MANUALS
            case 1:      // Vertical Door
            case 26:     // Blue Door/Locked
            case 27:     // Yellow Door /Locked
            case 28:     // Red Door /Locked

            case 31:     // Manual door open
            case 32:     // Blue locked door open
            case 33:     // Red locked door open
            case 34:     // Yellow locked door open

            case 117:        // Blazing door raise
            case 118: // Blazing door open
                VerticalDoor(line, thing);
                break;

            //UNUSED - Door Slide Open&Close
            case 124:
                // NOTE: clashes with secret level exit.
                //SL.EV_SlidingDoor (line, thing);
                break;

            // SWITCHES
            case 7:
                // Build Stairs
                if (BuildStairs(line, stair_e.build8)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 9:
                // Change Donut
                if (DoDonut(line)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 11:
                // Exit level
                getSwitches().ChangeSwitchTexture(line, false);
                DOOM().ExitLevel();
                break;

            case 14:
                // Raise Floor 32 and change texture
                if (DoPlat(line, plattype_e.raiseAndChange, 32)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 15:
                // Raise Floor 24 and change texture
                if (DoPlat(line, plattype_e.raiseAndChange, 24)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 18:
                // Raise Floor to next highest floor
                if (DoFloor(line, floor_e.raiseFloorToNearest)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 20:
                // Raise Plat next highest floor and change texture
                if (DoPlat(line, plattype_e.raiseToNearestAndChange, 0)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 21:
                // PlatDownWaitUpStay
                if (DoPlat(line, plattype_e.downWaitUpStay, 0)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 23:
                // Lower Floor to Lowest
                if (DoFloor(line, floor_e.lowerFloorToLowest)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 29:
                // Raise Door
                if (DoDoor(line, vldoor_e.normal)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 41:
                // Lower Ceiling to Floor
                if (DoCeiling(line, ceiling_e.lowerToFloor)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 71:
                // Turbo Lower Floor
                if (DoFloor(line, floor_e.turboLower)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 49:
                // Ceiling Crush And Raise
                if (DoCeiling(line, ceiling_e.crushAndRaise)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 50:
                // Close Door
                if (DoDoor(line, vldoor_e.close)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 51:
                // Secret EXIT
                getSwitches().ChangeSwitchTexture(line, false);
                DOOM().SecretExitLevel();
                break;

            case 55:
                // Raise Floor Crush
                if (DoFloor(line, floor_e.raiseFloorCrush)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 101:
                // Raise Floor
                if (DoFloor(line, floor_e.raiseFloor)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 102:
                // Lower Floor to Surrounding floor height
                if (DoFloor(line, floor_e.lowerFloor)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 103:
                // Open Door
                if (DoDoor(line, vldoor_e.open)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 111:
                // Blazing Door Raise (faster than TURBO!)
                if (DoDoor(line, vldoor_e.blazeRaise)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 112:
                // Blazing Door Open (faster than TURBO!)
                if (DoDoor(line, vldoor_e.blazeOpen)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 113:
                // Blazing Door Close (faster than TURBO!)
                if (DoDoor(line, vldoor_e.blazeClose)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 122:
                // Blazing PlatDownWaitUpStay
                if (DoPlat(line, plattype_e.blazeDWUS, 0)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 127:
                // Build Stairs Turbo 16
                if (this.BuildStairs(line, stair_e.turbo16)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 131:
                // Raise Floor Turbo
                if (DoFloor(line, floor_e.raiseFloorTurbo)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 133:
            // BlzOpenDoor BLUE
            case 135:
            // BlzOpenDoor RED
            case 137:
                // BlzOpenDoor YELLOW
                if (DoLockedDoor(line, vldoor_e.blazeOpen, thing)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            case 140:
                // Raise Floor 512
                if (DoFloor(line, floor_e.raiseFloor512)) {
                    getSwitches().ChangeSwitchTexture(line, false);
                }
                break;

            // BUTTONS
            case 42:
                // Close Door
                if (DoDoor(line, vldoor_e.close)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 43:
                // Lower Ceiling to Floor
                if (this.DoCeiling(line, ceiling_e.lowerToFloor)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 45:
                // Lower Floor to Surrounding floor height
                if (DoFloor(line, floor_e.lowerFloor)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 60:
                // Lower Floor to Lowest
                if (DoFloor(line, floor_e.lowerFloorToLowest)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 61:
                // Open Door
                if (DoDoor(line, vldoor_e.open)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 62:
                // PlatDownWaitUpStay
                if (DoPlat(line, plattype_e.downWaitUpStay, 1)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 63:
                // Raise Door
                if (DoDoor(line, vldoor_e.normal)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 64:
                // Raise Floor to ceiling
                if (DoFloor(line, floor_e.raiseFloor)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 66:
                // Raise Floor 24 and change texture
                if (DoPlat(line, plattype_e.raiseAndChange, 24)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 67:
                // Raise Floor 32 and change texture
                if (DoPlat(line, plattype_e.raiseAndChange, 32)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 65:
                // Raise Floor Crush
                if (DoFloor(line, floor_e.raiseFloorCrush)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 68:
                // Raise Plat to next highest floor and change texture
                if (DoPlat(line, plattype_e.raiseToNearestAndChange, 0)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 69:
                // Raise Floor to next highest floor
                if (DoFloor(line, floor_e.raiseFloorToNearest)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 70:
                // Turbo Lower Floor
                if (DoFloor(line, floor_e.turboLower)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 114:
                // Blazing Door Raise (faster than TURBO!)
                if (DoDoor(line, vldoor_e.blazeRaise)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 115:
                // Blazing Door Open (faster than TURBO!)
                if (DoDoor(line, vldoor_e.blazeOpen)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 116:
                // Blazing Door Close (faster than TURBO!)
                if (DoDoor(line, vldoor_e.blazeClose)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 123:
                // Blazing PlatDownWaitUpStay
                if (DoPlat(line, plattype_e.blazeDWUS, 0)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 132:
                // Raise Floor Turbo
                if (DoFloor(line, floor_e.raiseFloorTurbo)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 99:
            // BlzOpenDoor BLUE
            case 134:
            // BlzOpenDoor RED
            case 136:
                // BlzOpenDoor YELLOW
                if (this.DoLockedDoor(line, vldoor_e.blazeOpen, thing)) {
                    getSwitches().ChangeSwitchTexture(line, true);
                }
                break;

            case 138:
                // Light Turn On
                LightTurnOn(line, 255);
                getSwitches().ChangeSwitchTexture(line, true);
                break;

            case 139:
                // Light Turn Off
                LightTurnOn(line, 35);
                getSwitches().ChangeSwitchTexture(line, true);
                break;

        }

        return true;
    }

    /**
     * P_UseLines Looks for special lines in front of the player to activate.
     */
    default void UseLines(player_t player) {
        final Spechits sp = contextRequire(KEY_SPECHITS);
        int angle;
        int x1, y1, x2, y2;
        //System.out.println("Uselines");
        sp.usething = player.mo;

        // Normally this shouldn't cause problems?
        angle = Tables.toBAMIndex(player.mo.angle);

        x1 = player.mo.x;
        y1 = player.mo.y;
        x2 = x1 + (USERANGE >> FRACBITS) * finecosine[angle];
        y2 = y1 + (USERANGE >> FRACBITS) * finesine[angle];

        PathTraverse(x1, y1, x2, y2, PT_ADDLINES, this::UseTraverse);
    }

    //
    // USE LINES
    //
    @P_Map.C(PTR_UseTraverse)
    default boolean UseTraverse(intercept_t in) {
        final Movement mov = contextRequire(KEY_MOVEMENT);
        final Spechits sp = contextRequire(KEY_SPECHITS);

        boolean side;
        // FIXME: some sanity check here?
        line_t line = (line_t) in.d();

        if (line.special == 0) {
            LineOpening(line);
            if (mov.openrange <= 0) {
                StartSound(sp.usething, sounds.sfxenum_t.sfx_noway);

                // can't use through a wall
                return false;
            }
            // not a special line, but keep checking
            return true;
        }

        side = false;
        if (line.PointOnLineSide(sp.usething.x, sp.usething.y)) {
            side = true;
        }

        //  return false;       // don't use back side
        UseSpecialLine(sp.usething, line, side);

        // can't use for than one special line in a row
        return false;
    }
;
}

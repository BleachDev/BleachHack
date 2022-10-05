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

import org.bleachhack.util.doom.p.ceiling_e;
import org.bleachhack.util.doom.p.floor_e;
import org.bleachhack.util.doom.p.mobj_t;
import org.bleachhack.util.doom.p.plattype_e;
import org.bleachhack.util.doom.p.stair_e;
import org.bleachhack.util.doom.p.vldoor_e;
import org.bleachhack.util.doom.rr.line_t;

public interface ActionsMoveEvents extends ActionTrait {

    boolean DoDoor(line_t line, vldoor_e type);
    boolean DoFloor(line_t line, floor_e floor_e);
    boolean DoPlat(line_t line, plattype_e plattype_e, int i);
    boolean BuildStairs(line_t line, stair_e stair_e);
    boolean DoCeiling(line_t line, ceiling_e ceiling_e);
    void StopPlat(line_t line);
    void LightTurnOn(line_t line, int i);
    void StartLightStrobing(line_t line);
    void TurnTagLightsOff(line_t line);
    int Teleport(line_t line, int side, mobj_t thing);
    int CeilingCrushStop(line_t line);

    //
    //EVENTS
    //Events are operations triggered by using, crossing,
    //or shooting special lines, or by timed thinkers.
    //
    /**
     * P_CrossSpecialLine - TRIGGER Called every time a thing origin is about to cross a line with a non 0 special.
     */
    default void CrossSpecialLine(line_t line, int side, mobj_t thing) {
        //line_t line;
        boolean ok;

        //line = LL.lines[linenum];
        //  Triggers that other things can activate
        if (thing.player == null) {
            // Things that should NOT trigger specials...
            switch (thing.type) {
                case MT_ROCKET:
                case MT_PLASMA:
                case MT_BFG:
                case MT_TROOPSHOT:
                case MT_HEADSHOT:
                case MT_BRUISERSHOT:
                    return;
                // break;

                default:
                    break;
            }

            ok = false;
            switch (line.special) {
                case 39:  // TELEPORT TRIGGER
                case 97:  // TELEPORT RETRIGGER
                case 125: // TELEPORT MONSTERONLY TRIGGER
                case 126: // TELEPORT MONSTERONLY RETRIGGER
                case 4:   // RAISE DOOR
                case 10:  // PLAT DOWN-WAIT-UP-STAY TRIGGER
                case 88: // PLAT DOWN-WAIT-UP-STAY RETRIGGER
                    ok = true;
                    break;
            }
            if (!ok) {
                return;
            }
        }

        // TODO: enum!
        // Note: could use some const's here.
        switch (line.special) {
            // TRIGGERS.
            // All from here to RETRIGGERS.
            case 2:
                // Open Door
                DoDoor(line, vldoor_e.open);
                line.special = 0;
                break;

            case 3:
                // Close Door
                DoDoor(line, vldoor_e.close);
                line.special = 0;
                break;

            case 4:
                // Raise Door
                DoDoor(line, vldoor_e.normal);
                line.special = 0;
                break;

            case 5:
                // Raise Floor
                DoFloor(line, floor_e.raiseFloor);
                line.special = 0;
                break;

            case 6:
                // Fast Ceiling Crush & Raise
                DoCeiling(line, ceiling_e.fastCrushAndRaise);
                line.special = 0;
                break;

            case 8:
                // Build Stairs
                BuildStairs(line, stair_e.build8);
                line.special = 0;
                break;

            case 10:
                // PlatDownWaitUp
                DoPlat(line, plattype_e.downWaitUpStay, 0);
                line.special = 0;
                break;

            case 12:
                // Light Turn On - brightest near
                LightTurnOn(line, 0);
                line.special = 0;
                break;

            case 13:
                // Light Turn On 255
                LightTurnOn(line, 255);
                line.special = 0;
                break;

            case 16:
                // Close Door 30
                DoDoor(line, vldoor_e.close30ThenOpen);
                line.special = 0;
                break;

            case 17:
                // Start Light Strobing
                StartLightStrobing(line);
                line.special = 0;
                break;

            case 19:
                // Lower Floor
                DoFloor(line, floor_e.lowerFloor);
                line.special = 0;
                break;

            case 22:
                // Raise floor to nearest height and change texture
                DoPlat(line, plattype_e.raiseToNearestAndChange, 0);
                line.special = 0;
                break;

            case 25:
                // Ceiling Crush and Raise
                DoCeiling(line, ceiling_e.crushAndRaise);
                line.special = 0;
                break;

            case 30:
                // Raise floor to shortest texture height
                //  on either side of lines.
                DoFloor(line, floor_e.raiseToTexture);
                line.special = 0;
                break;

            case 35:
                // Lights Very Dark
                LightTurnOn(line, 35);
                line.special = 0;
                break;

            case 36:
                // Lower Floor (TURBO)
                DoFloor(line, floor_e.turboLower);
                line.special = 0;
                break;

            case 37:
                // LowerAndChange
                DoFloor(line, floor_e.lowerAndChange);
                line.special = 0;
                break;

            case 38:
                // Lower Floor To Lowest
                DoFloor(line, floor_e.lowerFloorToLowest);
                line.special = 0;
                break;

            case 39:
                // TELEPORT!
                Teleport(line, side, thing);
                line.special = 0;
                break;

            case 40:
                // RaiseCeilingLowerFloor
                DoCeiling(line, ceiling_e.raiseToHighest);
                DoFloor(line, floor_e.lowerFloorToLowest);
                line.special = 0;
                break;

            case 44:
                // Ceiling Crush
                DoCeiling(line, ceiling_e.lowerAndCrush);
                line.special = 0;
                break;

            case 52:
                // EXIT!
                DOOM().ExitLevel();
                break;

            case 53:
                // Perpetual Platform Raise
                DoPlat(line, plattype_e.perpetualRaise, 0);
                line.special = 0;
                break;

            case 54:
                // Platform Stop
                StopPlat(line);
                line.special = 0;
                break;

            case 56:
                // Raise Floor Crush
                DoFloor(line, floor_e.raiseFloorCrush);
                line.special = 0;
                break;

            case 57:
                // Ceiling Crush Stop
                CeilingCrushStop(line);
                line.special = 0;
                break;

            case 58:
                // Raise Floor 24
                DoFloor(line, floor_e.raiseFloor24);
                line.special = 0;
                break;

            case 59:
                // Raise Floor 24 And Change
                DoFloor(line, floor_e.raiseFloor24AndChange);
                line.special = 0;
                break;

            case 104:
                // Turn lights off in sector(tag)
                TurnTagLightsOff(line);
                line.special = 0;
                break;

            case 108:
                // Blazing Door Raise (faster than TURBO!)
                DoDoor(line, vldoor_e.blazeRaise);
                line.special = 0;
                break;

            case 109:
                // Blazing Door Open (faster than TURBO!)
                DoDoor(line, vldoor_e.blazeOpen);
                line.special = 0;
                break;

            case 100:
                // Build Stairs Turbo 16
                BuildStairs(line, stair_e.turbo16);
                line.special = 0;
                break;

            case 110:
                // Blazing Door Close (faster than TURBO!)
                DoDoor(line, vldoor_e.blazeClose);
                line.special = 0;
                break;

            case 119:
                // Raise floor to nearest surr. floor
                DoFloor(line, floor_e.raiseFloorToNearest);
                line.special = 0;
                break;

            case 121:
                // Blazing PlatDownWaitUpStay
                DoPlat(line, plattype_e.blazeDWUS, 0);
                line.special = 0;
                break;

            case 124:
                // Secret EXIT
                DOOM().SecretExitLevel();
                break;

            case 125:
                // TELEPORT MonsterONLY
                if (thing.player == null) {
                    Teleport(line, side, thing);
                    line.special = 0;
                }
                break;

            case 130:
                // Raise Floor Turbo
                DoFloor(line, floor_e.raiseFloorTurbo);
                line.special = 0;
                break;

            case 141:
                // Silent Ceiling Crush & Raise
                DoCeiling(line, ceiling_e.silentCrushAndRaise);
                line.special = 0;
                break;

            // RETRIGGERS.  All from here till end.
            case 72:
                // Ceiling Crush
                DoCeiling(line, ceiling_e.lowerAndCrush);
                break;

            case 73:
                // Ceiling Crush and Raise
                DoCeiling(line, ceiling_e.crushAndRaise);
                break;

            case 74:
                // Ceiling Crush Stop
                CeilingCrushStop(line);
                break;

            case 75:
                // Close Door
                DoDoor(line, vldoor_e.close);
                break;

            case 76:
                // Close Door 30
                DoDoor(line, vldoor_e.close30ThenOpen);
                break;

            case 77:
                // Fast Ceiling Crush & Raise
                DoCeiling(line, ceiling_e.fastCrushAndRaise);
                break;

            case 79:
                // Lights Very Dark
                LightTurnOn(line, 35);
                break;

            case 80:
                // Light Turn On - brightest near
                LightTurnOn(line, 0);
                break;

            case 81:
                // Light Turn On 255
                LightTurnOn(line, 255);
                break;

            case 82:
                // Lower Floor To Lowest
                DoFloor(line, floor_e.lowerFloorToLowest);
                break;

            case 83:
                // Lower Floor
                DoFloor(line, floor_e.lowerFloor);
                break;

            case 84:
                // LowerAndChange
                DoFloor(line, floor_e.lowerAndChange);
                break;

            case 86:
                // Open Door
                DoDoor(line, vldoor_e.open);
                break;

            case 87:
                // Perpetual Platform Raise
                DoPlat(line, plattype_e.perpetualRaise, 0);
                break;

            case 88:
                // PlatDownWaitUp
                DoPlat(line, plattype_e.downWaitUpStay, 0);
                break;

            case 89:
                // Platform Stop
                StopPlat(line);
                break;

            case 90:
                // Raise Door
                DoDoor(line, vldoor_e.normal);
                break;

            case 91:
                // Raise Floor
                DoFloor(line, floor_e.raiseFloor);
                break;

            case 92:
                // Raise Floor 24
                DoFloor(line, floor_e.raiseFloor24);
                break;

            case 93:
                // Raise Floor 24 And Change
                DoFloor(line, floor_e.raiseFloor24AndChange);
                break;

            case 94:
                // Raise Floor Crush
                DoFloor(line, floor_e.raiseFloorCrush);
                break;

            case 95:
                // Raise floor to nearest height
                // and change texture.
                DoPlat(line, plattype_e.raiseToNearestAndChange, 0);
                break;

            case 96:
                // Raise floor to shortest texture height
                // on either side of lines.
                DoFloor(line, floor_e.raiseToTexture);
                break;

            case 97:
                // TELEPORT!
                Teleport(line, side, thing);
                break;

            case 98:
                // Lower Floor (TURBO)
                DoFloor(line, floor_e.turboLower);
                break;

            case 105:
                // Blazing Door Raise (faster than TURBO!)
                DoDoor(line, vldoor_e.blazeRaise);
                break;

            case 106:
                // Blazing Door Open (faster than TURBO!)
                DoDoor(line, vldoor_e.blazeOpen);
                break;

            case 107:
                // Blazing Door Close (faster than TURBO!)
                DoDoor(line, vldoor_e.blazeClose);
                break;

            case 120:
                // Blazing PlatDownWaitUpStay.
                DoPlat(line, plattype_e.blazeDWUS, 0);
                break;

            case 126:
                // TELEPORT MonsterONLY.
                if (thing.player == null) {
                    Teleport(line, side, thing);
                }
                break;

            case 128:
                // Raise To Nearest Floor
                DoFloor(line, floor_e.raiseFloorToNearest);
                break;

            case 129:
                // Raise Floor Turbo
                DoFloor(line, floor_e.raiseFloorTurbo);
                break;
        }
    }

}

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

import org.bleachhack.util.doom.doom.SourceCode;
import org.bleachhack.util.doom.doom.SourceCode.P_Lights;
import static org.bleachhack.util.doom.doom.SourceCode.P_Lights.T_FireFlicker;
import static org.bleachhack.util.doom.doom.SourceCode.P_Lights.T_Glow;
import static org.bleachhack.util.doom.doom.SourceCode.P_Lights.T_LightFlash;
import org.bleachhack.util.doom.doom.thinker_t;
import org.bleachhack.util.doom.p.Actions.ActionTrait;
import org.bleachhack.util.doom.p.Actions.ActionsLights.fireflicker_t;
import org.bleachhack.util.doom.p.Actions.ActionsLights.glow_t;
import org.bleachhack.util.doom.p.Actions.ActionsLights.lightflash_t;
import static org.bleachhack.util.doom.p.DoorDefines.GLOWSPEED;
import org.bleachhack.util.doom.p.ceiling_t;
import org.bleachhack.util.doom.p.floormove_t;
import org.bleachhack.util.doom.p.plat_t;
import org.bleachhack.util.doom.p.slidedoor_t;
import org.bleachhack.util.doom.p.strobe_t;
import org.bleachhack.util.doom.p.vldoor_t;

public interface Thinkers extends ActionTrait {
    //
    // T_FireFlicker
    //
    @SourceCode.Exact
    @P_Lights.C(T_FireFlicker)
    default void T_FireFlicker(thinker_t f) {
        final fireflicker_t flick = (fireflicker_t) f;
        int amount;

        if (--flick.count != 0) {
            return;
        }

        amount = (P_Random() & 3) * 16;

        if (flick.sector.lightlevel - amount < flick.minlight) {
            flick.sector.lightlevel = (short) flick.minlight;
        } else {
            flick.sector.lightlevel = (short) (flick.maxlight - amount);
        }

        flick.count = 4;
    }
    
    /**
     * T_LightFlash
     * Do flashing lights.
     */
    @SourceCode.Exact
    @P_Lights.C(T_LightFlash)
    default void T_LightFlash(thinker_t l) {
        final lightflash_t flash = (lightflash_t) l;
        if (--flash.count != 0) {
            return;
        }

        if (flash.sector.lightlevel == flash.maxlight) {
            flash.sector.lightlevel = (short) flash.minlight;
            flash.count = (P_Random() & flash.mintime) + 1;
        } else {
            flash.sector.lightlevel = (short) flash.maxlight;
            flash.count = (P_Random() & flash.maxtime) + 1;
        }
    }

    default void T_StrobeFlash(thinker_t s) {
        ((strobe_t) s).StrobeFlash();
    }

    //
    // Spawn glowing light
    //
    @SourceCode.Exact
    @P_Lights.C(T_Glow)
    default void T_Glow(thinker_t t) {
        glow_t g = (glow_t) t;
        switch (g.direction) {
            case -1:
                // DOWN
                g.sector.lightlevel -= GLOWSPEED;
                if (g.sector.lightlevel <= g.minlight) {
                    g.sector.lightlevel += GLOWSPEED;
                    g.direction = 1;
                }
                break;

            case 1:
                // UP
                g.sector.lightlevel += GLOWSPEED;
                if (g.sector.lightlevel >= g.maxlight) {
                    g.sector.lightlevel -= GLOWSPEED;
                    g.direction = -1;
                }
                break;
                
            default:
                break;
        }
    }

    default void T_MoveCeiling(thinker_t c) {
        getThinkers().MoveCeiling((ceiling_t) c);
    }

    default void T_MoveFloor(thinker_t f) {
        getThinkers().MoveFloor((floormove_t) f);
    }

    default void T_VerticalDoor(thinker_t v) {
        getThinkers().VerticalDoor((vldoor_t) v);
    }
    
    default void T_SlidingDoor(thinker_t door) {
        getThinkers().SlidingDoor((slidedoor_t) door);
    }
    
    default void T_PlatRaise(thinker_t p) {
        getThinkers().PlatRaise((plat_t) p);
    }
}

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

import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import static org.bleachhack.util.doom.m.fixed_t.FixedDiv;
import static org.bleachhack.util.doom.m.fixed_t.FixedMul;
import org.bleachhack.util.doom.p.UnifiedGameMap.Switches;
import org.bleachhack.util.doom.p.floor_e;
import org.bleachhack.util.doom.p.intercept_t;
import org.bleachhack.util.doom.p.mobj_t;
import org.bleachhack.util.doom.p.plattype_e;
import org.bleachhack.util.doom.p.vldoor_e;
import org.bleachhack.util.doom.rr.line_t;

public interface ActionsShootEvents extends ActionsSpawns {

    /**
     * P_ShootSpecialLine - IMPACT SPECIALS Called when a thing shoots a special line.
     */
    default void ShootSpecialLine(mobj_t thing, line_t line) {
        final Switches sw = getSwitches();
        boolean ok;

        //  Impacts that other things can activate.
        if (thing.player == null) {
            ok = false;
            switch (line.special) {
                case 46:
                    // OPEN DOOR IMPACT
                    ok = true;
                    break;
            }
            if (!ok) {
                return;
            }
        }

        switch (line.special) {
            case 24:
                // RAISE FLOOR
                DoFloor(line, floor_e.raiseFloor);
                sw.ChangeSwitchTexture(line, false);
                break;

            case 46:
                // OPEN DOOR
                DoDoor(line, vldoor_e.open);
                sw.ChangeSwitchTexture(line, true);
                break;

            case 47:
                // RAISE FLOOR NEAR AND CHANGE
                DoPlat(line, plattype_e.raiseToNearestAndChange, 0);
                sw.ChangeSwitchTexture(line, false);
                break;
        }
    }

    //_D_: NOTE: this function was added, because replacing a goto by a boolean flag caused a bug if shooting a single sided line
    default boolean gotoHitLine(intercept_t in, line_t li) {
        final Spawn targ = contextRequire(KEY_SPAWN);
        int x, y, z, frac;

        // position a bit closer
        frac = in.frac - FixedDiv(4 * FRACUNIT, targ.attackrange);
        x = targ.trace.x + FixedMul(targ.trace.dx, frac);
        y = targ.trace.y + FixedMul(targ.trace.dy, frac);
        z = targ.shootz + FixedMul(targ.aimslope, FixedMul(frac, targ.attackrange));

        if (li.frontsector.ceilingpic == DOOM().textureManager.getSkyFlatNum()) {
            // don't shoot the sky!
            if (z > li.frontsector.ceilingheight) {
                return false;
            }

            // it's a sky hack wall
            if (li.backsector != null && li.backsector.ceilingpic == DOOM().textureManager.getSkyFlatNum()) {
                return false;
            }
        }

        // Spawn bullet puffs.
        this.SpawnPuff(x, y, z);

        // don't go any farther
        return false;
    }
}

/**
 * Copyright (C) 1993-1996 Id Software, Inc.
 * from f_wipe.c
 * 
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

package org.bleachhack.util.doom.v.graphics;

import java.lang.reflect.Array;
import static org.bleachhack.util.doom.utils.GenericCopy.*;

public interface ColorTransform {
    
    default boolean initTransform(Wipers.WiperImpl<?, ?> wiper) {
        memcpy(wiper.wipeStartScr, 0, wiper.wipeEndScr, 0, Array.getLength(wiper.wipeEndScr));
        return false;
    }
    
    default boolean colorTransformB(Wipers.WiperImpl<byte[], ?> wiper) {
        byte[] w = wiper.wipeStartScr, e = wiper.wipeEndScr;
        boolean changed = false;
        for (int i = 0, newval; i < w.length; ++i) {
            if (w[i] != e[i]) {
                w[i] = w[i] > e[i]
                    ? (newval = w[i] - wiper.ticks) < e[i] ? e[i] : (byte) newval
                    : (newval = w[i] + wiper.ticks) > e[i] ? e[i] : (byte) newval;
                changed = true;
            }
        }
        return !changed;
    }

    default boolean colorTransformS(Wipers.WiperImpl<short[], ?> wiper) {
        short[] w = wiper.wipeStartScr, e = wiper.wipeEndScr;
        boolean changed = false;
        for (int i = 0, newval; i < w.length; ++i) {
            if (w[i] != e[i]) {
                w[i] = w[i] > e[i]
                    ? (newval = w[i] - wiper.ticks) < e[i] ? e[i] : (byte) newval
                    : (newval = w[i] + wiper.ticks) > e[i] ? e[i] : (byte) newval;
                changed = true;
            }
        }
        return !changed;
    }

    default boolean colorTransformI(Wipers.WiperImpl<int[], ?> wiper) {
        int[] w = wiper.wipeStartScr, e = wiper.wipeEndScr;
        boolean changed = false;
        for (int i = 0, newval; i < w.length; ++i) {
            if (w[i] != e[i]) {
                w[i] = w[i] > e[i]
                    ? (newval = w[i] - wiper.ticks) < e[i] ? e[i] : (byte) newval
                    : (newval = w[i] + wiper.ticks) > e[i] ? e[i] : (byte) newval;
                changed = true;
            }
        }
        return !changed;
    }
}

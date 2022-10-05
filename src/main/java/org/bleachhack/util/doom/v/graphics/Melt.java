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

import static org.bleachhack.util.doom.utils.C2JUtils.*;

public interface Melt extends ColorTransform {
    /**
     * No more fucking column-major transpose!
     * A funny fast thing for 1993, but able to make Intel i7 think hard in 2017
     * (well, at least, in energy saving mode :p)
     *  - Good Sign, 2017/04/10
     */
    default boolean initMeltScaled(Wipers.WiperImpl<?, ?> wiper) { return initMelt(wiper, true); }
    default boolean initMelt(Wipers.WiperImpl<?, ?> wiper) { return initMelt(wiper, false); }
    default boolean initMelt(Wipers.WiperImpl<?, ?> wiper, boolean scaled) {
        // copy start screen to main screen
        memcpy(wiper.wipeStartScr, wiper.wipeScr, wiper.screenWidth * wiper.screenHeight);
        setupColumnPositions(wiper, scaled);
        return false;
    }

    /**
     * setup initial column positions
     * (y<0 => not ready to scroll yet)
     */
    default void setupColumnPositions(Wipers.WiperImpl<?, ?> wiper, boolean scaled) {
        final int lim = scaled ? wiper.screenWidth / wiper.dupy : wiper.screenWidth;
        wiper.y = new int[lim];
        wiper.y[0] = -(wiper.random.M_Random() % 16);
        for (int i = 1; i < lim; i++) {
            final int r = (wiper.random.M_Random() % 3) - 1;
            wiper.y[i] = wiper.y[i - 1] + r;

            if (wiper.y[i] > 0) {
                wiper.y[i] = 0;
            } else if (wiper.y[i] == -16) {
                wiper.y[i] = -15;
            }
        }
    }
    
    /**
     * The only place where we cannot have generic code, because it is 1 pixel copy operation
     * which to be called tens thousands times and will cause overhead on just literally any more intermediate function
     * The "same object" comparison is actually comparison of two integers - pointers in memory, - so it is instant
     * and branching is predictable, so a good cache will negate the class checks completely
     *  - Good Sign 2017/04/10
     */
    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    default void toScreen(Class<?> bufType, Object src, Object dest, int width, int dy, int ps, int pd) {
        if (bufType == int[].class) {
            final int[] to = (int[]) src, from = (int[]) dest;
            for (int i = 0; i < dy; ++i) {
                final int iWidth = width * i;
                to[pd + iWidth] = from[ps + iWidth];
            }
        } else if (bufType == short[].class) {
            final short[] to = (short[]) src, from = (short[]) dest;
            for (int i = 0; i < dy; ++i) {
                final int iWidth = width * i;
                to[pd + iWidth] = from[ps + iWidth];
            }
        } else if (bufType == byte[].class) {
            final byte[] to = (byte[]) src, from = (byte[]) dest;
            for (int i = 0; i < dy; ++i) {
                final int iWidth = width * i;
                to[pd + iWidth] = from[ps + iWidth];
            }
        } else throw new UnsupportedOperationException("Do not have support for: " + bufType);
    }
    
    /**
     * Completely opposite of the previous method. Only performant when scaling is on.
     * Stick to System.arraycopy since there is certainly several pixels to get and set.
     * Also, it doesn't even need to check and cast to classes
     *  - Good Sign 2017/04/10
     */
    @SuppressWarnings("SuspiciousSystemArraycopy")
    default void toScreenScaled(Wipers.WiperImpl<?, ?> wiper, Object from, int dy, int ps, int pd) {
        for (int i = 0; i < dy; ++i) {
            final int iWidth = wiper.screenWidth * i;
            System.arraycopy(from, ps + iWidth, wiper.wipeScr, pd + iWidth, wiper.dupy);
        }
    }
    
    /**
     * Scrolls down columns ready for scroll and those who aren't makes a bit more ready
     * Finally no more shitty transpose!
     *  - Good Sign 2017/04/10
     */
    default boolean doMeltScaled(Wipers.WiperImpl<?, ?> wiper) { return doMelt(wiper, true); }
    default boolean doMelt(Wipers.WiperImpl<?, ?> wiper) { return doMelt(wiper, false); }
    default boolean doMelt(Wipers.WiperImpl<?, ?> wiper, boolean scaled) {
        final int lim = scaled ? wiper.screenWidth / wiper.dupy : wiper.screenWidth;
        boolean done = true;
        
        while (wiper.ticks-- > 0) {
            for (int i = 0; i < lim; i++) {
                // Column won't start yet.
                if (wiper.y[i] < 0) {
                    wiper.y[i]++;
                    done = false;
                } else if (wiper.y[i] < wiper.screenHeight) {
                    int dy = (wiper.y[i] < wiper.scaled_16) ? wiper.y[i] + (scaled ? wiper.dupy : 1) : wiper.scaled_8;
                    if (wiper.y[i] + dy >= wiper.screenHeight) dy = wiper.screenHeight - wiper.y[i];
                    int pd = wiper.y[i] * wiper.screenWidth + (scaled ? i * wiper.dupx : i);

                    // MAES: this part should draw the END SCREEN "behind" the melt.
                    if (scaled)
                        toScreenScaled(wiper, wiper.wipeEndScr, dy, pd, pd);
                    else
                        toScreen(wiper.bufferType, wiper.wipeScr, wiper.wipeEndScr, wiper.screenWidth, dy, pd, pd);
                    
                    wiper.y[i] += dy;
                    pd += dy * wiper.screenWidth;

                    // This draws a column shifted by y[i]
                    if (scaled)
                        toScreenScaled(wiper, wiper.wipeStartScr, wiper.screenHeight - wiper.y[i], i * wiper.dupy, pd);
                    else
                        toScreen(wiper.bufferType, wiper.wipeScr, wiper.wipeStartScr, wiper.screenWidth, wiper.screenHeight - wiper.y[i], i, pd);
                    
                    done = false;
                }
            }
        }

        return done;
    }

    default boolean exitMelt(Wipers.WiperImpl<?, ?> wiper) {
        wiper.y = null; //Z_Free(y);
        wiper.ticks = 0;
        return false;
    }
}

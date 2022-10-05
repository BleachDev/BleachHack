/*
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

import org.bleachhack.util.doom.rr.patch_t;

/**
 *
 * @author Good Sign
 */
public interface Points<V, E extends Enum<E>> extends Screens<V, E> {
    default void doRangeCheck(int x, int y, int width, int height) throws BadRangeException {
        if (x >= 0 && y >= 0) {
            final int scrWidth = this.getScreenWidth();
            final int scrHeight = this.getScreenHeight();
            if (x + width > scrWidth || y + height > scrWidth) {
                throw new BadRangeException(String.format(
                    "Coordinates overflow screen space: (%d, %d, %d, %d) on screen %dx%d",
                    x, y, x + width, y + height, scrWidth, scrHeight)
                );
            }
        } else {
            throw new IllegalArgumentException(String.format("Invalid coordinates: (%d, %d)", x, y));
        }
    }

    default void doRangeCheck(int x, int y, patch_t patch) throws BadRangeException {
        doRangeCheck(x, y, patch.width, patch.height);
    }

    default void doRangeCheck(int x, int y, patch_t patch, int dupx, int dupy) throws BadRangeException {
        doRangeCheck(x, y, patch.width * dupx, patch.height * dupy);
    }
    
    default int point(int x, int y) {
        return y * getScreenWidth() + x;
    }
    
    default int point(int x, int y, int width) {
        return y * width + x;
    }
}

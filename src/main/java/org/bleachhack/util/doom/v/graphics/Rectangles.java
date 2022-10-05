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

import java.awt.Rectangle;

/**
 * Rectangles fill and copy
 * 
 * TODO: range checks on Fill & Copy
 * 
 * @author Good Sign
 */
public interface Rectangles<V, E extends Enum<E>> extends Blocks<V, E>, Points<V, E> {
    /**
     * Computes a Horizontal with a row from the Rectangle at heightIndex
     * @param rect
     * @param heightIndex
     * @return 
     */
    default Horizontal GetRectRow(Rectangle rect, int heightIndex) {
        if (heightIndex < 0 || heightIndex > rect.height) {
            throw new IndexOutOfBoundsException("Bad row index: " + heightIndex);
        }
        
        return new Horizontal(point(rect.x, rect.y) + heightIndex * getScreenWidth(), rect.width);
    }
    
    /**
     * V_CopyRect
     */
    
    default void CopyRect(E srcScreenType, Rectangle rectangle, E dstScreenType) {
        final V srcScreen = getScreen(srcScreenType);
        final V dstScreen = getScreen(dstScreenType);
        final int screenWidth = getScreenWidth();
        final int point = point(rectangle.x, rectangle.y);
        final Relocation rel = new Relocation(point, point, rectangle.width);
        for (int h = rectangle.height; h > 0; --h, rel.shift(screenWidth)) {
            screenCopy(srcScreen, dstScreen, rel);
        }
    }
    
    default void CopyRect(E srcScreenType, Rectangle rectangle, E dstScreenType, int dstPoint) {
        final V srcScreen = getScreen(srcScreenType);
        final V dstScreen = getScreen(dstScreenType);
        final int screenWidth = getScreenWidth();
        final Relocation rel = new Relocation(point(rectangle.x, rectangle.y), dstPoint, rectangle.width);
        for (int h = rectangle.height; h > 0; --h, rel.shift(screenWidth)) {
            screenCopy(srcScreen, dstScreen, rel);
        }
    }
    
    /**
     * V_FillRect
     */

    default void FillRect(E screenType, Rectangle rectangle, V patternSrc, Horizontal pattern) {
        final V screen = getScreen(screenType);
        if (rectangle.height > 0) {
            final Horizontal row = GetRectRow(rectangle, 0);
            // Fill first line of rect
            screenSet(patternSrc, pattern, screen, row);
            // Fill the rest of the rect
            RepeatRow(screen, row, rectangle.height - 1);
        }
    }

    default void FillRect(E screenType, Rectangle rectangle, V patternSrc, int point) {
        final V screen = getScreen(screenType);
        if (rectangle.height > 0) {
            final Horizontal row = GetRectRow(rectangle, 0);
            // Fill first line of rect
            screenSet(patternSrc, point, screen, row);
            // Fill the rest of the rect
            RepeatRow(screen, row, rectangle.height - 1);
        }
    }
    
    default void FillRect(E screenType, Rectangle rectangle, int color) {FillRect(screenType, rectangle, (byte) color);}
    default void FillRect(E screenType, Rectangle rectangle, byte color) {
        final V screen = getScreen(screenType);
        if (rectangle.height > 0) {
            final V filler = convertPalettedBlock(color);
            final Horizontal row = GetRectRow(rectangle, 0);
            // Fill first line of rect
            screenSet(filler, 0, screen, row);
            // Fill the rest of the rect
            RepeatRow(screen, row, rectangle.height - 1);
        }
    }
}

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

import org.bleachhack.util.doom.f.Wiper;
import java.lang.reflect.Array;
import org.bleachhack.util.doom.m.IRandom;
import static org.bleachhack.util.doom.utils.GenericCopy.*;
import org.bleachhack.util.doom.v.renderers.DoomScreen;

/**
 * Screen surface library
 *
 * @author Good Sign
 */
public interface Screens<V, E extends Enum<E>> {
    final int SCREENS_COUNT = DoomScreen.values().length;
    
    V getScreen(E screenType);
    int getScalingX();
    int getScalingY();
    int getScreenWidth();
    int getScreenHeight();
    Wiper createWiper(IRandom random);
    
    /**
     * memset-like methods for screen surfaces
     */
    
    /**
     * Will fill destPortion on the screen with color of the specified point on it
     * The point argument IS NOT a color to fill, only a POINTER to the pixel on the screen
     */
    default void screenSet(V screen, int point, Horizontal destination) {
        memset(screen, destination.start, destination.length, screen, point, 1);
    }
    
    /**
     * Will fill destPortion on the dstScreen by scrPortion pattern from srcScreen
     */
    default void screenSet(V srcScreen, Horizontal pattern, V dstScreen, Horizontal destination) {
        memset(dstScreen, destination.start, destination.length, srcScreen, pattern.start, pattern.length);
    }

    /**
     * Will fill destPortion on the dstScreen with color of the specified point on the srcScreen
     * The point argument IS NOT a color to fill, only a POINTER to the pixel on the screen
     */
    default void screenSet(V srcScreen, int point, V dstScreen, Horizontal destination) {
        memset(dstScreen, destination.start, destination.length, srcScreen, point, 1);
    }

    /**
     * Will fill destPortion on the screen with srcPortion pattern from the same screen
     */
    default void screenSet(V screen, Horizontal pattern, Horizontal destination) {
        memset(screen, destination.start, destination.length, screen, pattern.start, pattern.length);
    }

    /**
     * memcpy-like method for screen surfaces
     */
    default void screenCopy(V srcScreen, V dstScreen, Relocation relocation) {
        memcpy(srcScreen, relocation.source, dstScreen, relocation.destination, relocation.length);
    }
    
    default void screenCopy(E srcScreen, E dstScreen) {
        final Object dstScreenObj = getScreen(dstScreen);
        memcpy(getScreen(srcScreen), 0, dstScreenObj, 0, Array.getLength(dstScreenObj));
    }
    
    default Plotter<V> createPlotter(E screen) {
        return new Plotter.Thin<>(getScreen(screen), getScreenWidth());
    }
    
    class BadRangeException extends Exception {
		private static final long serialVersionUID = 2903441181162189295L;
		public BadRangeException(String m) { super(m);}
        public BadRangeException() {}
    }
}

/**
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

package org.bleachhack.util.doom.v.scale;

/**
 * Interface for an object that conveys screen resolution/scaling information, meant to replace the static declarations
 * in Defines.
 *
 * @author velktron
 *
 */
public interface VideoScale {

    //It is educational but futile to change this
    //scaling e.g. to 2. Drawing of status bar,
    //menues etc. is tied to the scale implied
    //by the graphics.
    public static double INV_ASPECT_RATIO = 0.625; // 0.75, ideally

    //
    // For resize of screen, at start of game.
    // It will not work dynamically, see visplanes.
    //
    public static final int BASE_WIDTH = 320;
    public static final int BASE_HEIGHT = (int) (INV_ASPECT_RATIO * 320); // 200

    int getScreenWidth();
    int getScreenHeight();
    int getScalingX();
    int getScalingY();

    /**
     * Safest global scaling for fixed stuff like menus, titlepic etc
     */
    int getSafeScaling();

    /**
     * Get floating point screen multiplier. Not recommended, as it causes visual glitches. Replace with safe scale,
     * whenever possible
     */
    float getScreenMul();

    /**
     * Future, should signal aware objects that they should refresh their resolution-dependent state, structures,
     * variables etc.
     *
     * @return
     */
    boolean changed();
}

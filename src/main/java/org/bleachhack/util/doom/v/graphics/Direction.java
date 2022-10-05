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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Good Sign
 */
public enum Direction {
    LEFT_UP,      UP,      RIGHT_UP,
    /*  \         ||         /   */
    /*    \       ||       /     */
    /*      \     ||     /       */
    /*        \   ||   /         */
    LEFT,/*===*/CENTER,/*===*/RIGHT,
    /*        /   ||   \         */
    /*      /     ||     \       */
    /*    /       ||       \     */
    /*  /         ||         \   */
    LEFT_DOWN,   DOWN,   RIGHT_DOWN;
    
    public static final List<Direction> directions = Collections.unmodifiableList(Arrays.asList(values()));

    /**
     * Categorization constants
     */
    
    // LEFT_UP, UP, RIGHT_UP
    public final boolean hasTop = ordinal() < 3;
    // LEFT_UP, LEFT, LEFT_DOWN
    public final boolean hasLeft = ordinal() % 3 == 0;
    // RIGHT_UP, RIGHT_ RIGHT_DOWN
    public final boolean hasRight = ordinal() % 3 == 2;
    // LEFT_DOWN, DOWN, RIGHT_DOWN
    public final boolean hasBottom = ordinal() > 5;
    // UP, LEFT, RIGHT, DOWN
    public final boolean straight = ordinal() % 2 != 0; 

    public boolean isAdjacent(Direction dir) {
        return this.straight ^ dir.straight;
    }
    
    /**
     * Conversions
     */

    public Direction next() {
        if (this == RIGHT_DOWN)
            return LEFT_UP;
        
        return directions.get(ordinal() + 1);
    }
    
    public Direction opposite() {
        switch(this) {
            case LEFT_UP:
                return RIGHT_DOWN;
            case UP:
                return DOWN;
            case RIGHT_UP:
                return LEFT_DOWN;
            case LEFT:
                return RIGHT;
            default: // CENTER
                return this;
            case RIGHT:
                return LEFT;
            case LEFT_DOWN:
                return RIGHT_UP;
            case DOWN:
                return UP;
            case RIGHT_DOWN:
                return LEFT_UP;
        }
    }
    
    public Direction rotationHor(int sign) {
        if (sign == 0)
            return this;
        
        switch(this) {
            case LEFT_UP:
                return sign > 0 ? UP : LEFT;
            case UP:
                return sign > 0 ? RIGHT_UP : LEFT_UP;
            case RIGHT_UP:
                return sign > 0 ? RIGHT : UP;
            case LEFT:
                return sign > 0 ? CENTER : this;
            default: // CENTER
                return sign > 0 ? RIGHT : LEFT;
            case RIGHT:
                return sign > 0 ? CENTER : this;
            case LEFT_DOWN:
                return sign > 0 ? DOWN : LEFT;
            case DOWN:
                return sign > 0 ? RIGHT_DOWN : LEFT_DOWN;
            case RIGHT_DOWN:
                return sign > 0 ? RIGHT : DOWN;
        }
    }
    
    public Direction rotationVert(int sign) {
        if (sign == 0)
            return this;
        
        switch(this) {
            case LEFT_UP:
                return sign > 0 ? LEFT : UP;
            case UP:
                return sign > 0 ? CENTER : this;
            case RIGHT_UP:
                return sign > 0 ? RIGHT : UP;
            case LEFT:
                return sign > 0 ? LEFT_DOWN : LEFT_UP;
            default: // CENTER
                return sign > 0 ? DOWN : UP;
            case RIGHT:
                return sign > 0 ? RIGHT_DOWN : RIGHT_UP;
            case LEFT_DOWN:
                return sign > 0 ? DOWN : LEFT;
            case DOWN:
                return sign < 0 ? CENTER : this;
            case RIGHT_DOWN:
                return sign > 0 ? DOWN : RIGHT;
        }
    }
    
    public Direction rotation(int signX, int signY) {
        final Direction rotX = rotationHor(signX), rotY = rotationHor(signY);
        
        if (rotX.isAdjacent(rotY)) {
            if (signX > 0 && signY > 0)
                return RIGHT_DOWN;
            else if (signX > 0 && signY < 0)
                return RIGHT_UP;
            else if (signX < 0 && signY > 0)
                return LEFT_DOWN;
            else if (signX < 0 && signY < 0)
                return LEFT_UP;
        }
        
        // otherwise, 2nd takes precedence
        return rotY;
    }
}

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

/**
 * Horizontal represents a range from a screen buffer (byte or short or int array) 
 * 
 * @author Good Sign
 */
public class Horizontal {
    
    public int start;
    public int length;

    public Horizontal() {
    }

    public Horizontal(int start, int length) {
        this.start = start;
        this.length = length;
    }

    public Relocation relocate(int amount) {
        return new Relocation(start, start + amount, length);
    }

    public void shift(int amount) {
        this.start += amount;
    }
}

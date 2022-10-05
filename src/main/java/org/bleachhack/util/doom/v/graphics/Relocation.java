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
 * Relocation represents a move of a fixed length of bytes/shorts/ints
 * from one range in screen buffer to another range of the same size
 * 
 * @author Good Sign
 */
public final class Relocation {
    
    public int source;
    public int destination;
    public int length;

    public Relocation() {
    }

    public Relocation(int source, int destination, int length) {
        this.source = source;
        this.destination = destination;
        this.length = length;
    }

    public Relocation shift(int amount) {
        this.source += amount;
        this.destination += amount;
        return this;
    }
    
    public Relocation retarget(int source, int destination) {
        this.source = source;
        this.destination = destination;
        return this;
    }
}

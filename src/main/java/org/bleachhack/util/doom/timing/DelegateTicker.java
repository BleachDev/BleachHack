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
package org.bleachhack.util.doom.timing;

/**
 *
 * @author Good Sign
 */
public class DelegateTicker implements ITicker {
    private final FastTicker ft = new FastTicker();
    private final MilliTicker mt = new MilliTicker();
    private final NanoTicker nt = new NanoTicker();
    private ITicker currentTicker = ft;

    @Override
    public int GetTime() {
        return currentTicker.GetTime();
    }
    
    public void changeTicker() {
        if (currentTicker == nt) {
            currentTicker = mt;
            ((MilliTicker) currentTicker).basetime = 0;
            ((MilliTicker) currentTicker).oldtics = 0;
        } else if (currentTicker == mt) {
            currentTicker = ft;
            ((FastTicker) currentTicker).fasttic = 0;
        } else {
            currentTicker = nt;
            ((NanoTicker) currentTicker).basetime = 0;
            ((NanoTicker) currentTicker).oldtics = 0;
        }
    }
}

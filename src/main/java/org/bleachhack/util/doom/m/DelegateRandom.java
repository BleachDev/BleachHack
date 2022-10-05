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
package org.bleachhack.util.doom.m;

import org.bleachhack.util.doom.data.Defines;
import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.doom.SourceCode.M_Random;
import static org.bleachhack.util.doom.doom.SourceCode.M_Random.*;
import org.bleachhack.util.doom.p.ActiveStates;
import org.bleachhack.util.doom.utils.C2JUtils;

/**
 * A "IRandom" that delegates its function to one of the two available IRandom implementations
 * By default, MochaDoom now uses JavaRandom, however it switches
 * to DoomRandom (supposedly Vanilla DOOM v1.9 compatible, tested only in Chocolate DOOM)
 * whenever you start recording or playing demo. When you start then new game, MochaDoom restores new JavaRandom.
 * 
 * However, if you start MochaDoom with -javarandom command line argument and -record demo,
 * then MochaDoom will record the demo using JavaRandom. Such demo will be neither compatible
 * with Vanilla DOOM v1.9, nor with another source port.
 * 
 * Only MochaDoom can play JavaRandom demos.
 *  - Good Sign 2017/04/10
 * 
 * @author Good Sign
 */
public class DelegateRandom implements IRandom {
    
    private IRandom random;
    private IRandom altRandom;

    public DelegateRandom() {
        this.random = new JavaRandom();
    }

    public void requireRandom(final int version) {
        if (C2JUtils.flags(version, Defines.JAVARANDOM_MASK) && this.random instanceof DoomRandom) {
            switchRandom(true);
        } else if (!C2JUtils.flags(version, Defines.JAVARANDOM_MASK) && !(this.random instanceof DoomRandom)) {
            switchRandom(false);
        }
    }

    private void switchRandom(boolean which) {
        IRandom arandom = altRandom;
        if (arandom != null && ((!which && arandom instanceof DoomRandom) || (which && arandom instanceof JavaRandom))) {
            this.altRandom = random;
            this.random = arandom;
            System.out.print(String.format("M_Random: Switching to %s\n", random.getClass().getSimpleName()));
        } else {
            this.altRandom = random;
            this.random = which ? new JavaRandom() : new DoomRandom();
            System.out.print(String.format("M_Random: Switching to %s (new instance)\n", random.getClass().getSimpleName()));
        }
        //random.ClearRandom();
    }

    @Override
    @M_Random.C(P_Random)
    public int P_Random() {
        return random.P_Random();
    }

    @Override
    @M_Random.C(M_Random)
    public int M_Random() {
        return random.M_Random();
    }

    @Override
    @M_Random.C(M_ClearRandom)
    public void ClearRandom() {
        random.ClearRandom();
    }

    @Override
    public int getIndex() {
        return random.getIndex();
    }

    @Override
    public int P_Random(int caller) {
        return random.P_Random(caller);
    }

    @Override
    public int P_Random(String message) {
        return random.P_Random(message);
    }

    @Override
    public int P_Random(ActiveStates caller, int sequence) {
        return random.P_Random(caller, sequence);
    }

    @Override
    public int P_Random(ActiveStates caller, mobjtype_t type, int sequence) {
        return random.P_Random(caller, type, sequence);
    }
    
}

package org.bleachhack.util.doom.m;

import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.p.ActiveStates;

// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: DoomRandom.java,v 1.4 2013/06/04 11:29:25 velktron Exp $
//
// Copyright (C) 1993-1996 by id Software, Inc.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// $Log: DoomRandom.java,v $
// Revision 1.4  2013/06/04 11:29:25  velktron
// Dummy implementations
//
// Revision 1.3  2013/06/03 10:53:29  velktron
// Implements the new IRandom.
//
// Revision 1.2.10.3  2013/01/09 19:38:26  velktron
// Printing arbitrary messages
//
// Revision 1.2.10.2  2012/11/20 15:59:20  velktron
// More tooling functions.
//
// Revision 1.2.10.1  2012/11/19 22:11:36  velktron
// Added demo sync tooling.
//
// Revision 1.2  2011/05/30 02:24:30  velktron
// *** empty log message ***
//
// Revision 1.1  2011/05/29 22:15:32  velktron
// Introduced IRandom interface.
//
// Revision 1.4  2010/09/22 16:40:02  velktron
// MASSIVE changes in the status passing model.
// DoomMain and DoomGame unified.
// Doomstat merged into DoomMain (now status and game functions are one).
//
// Most of DoomMain implemented. Possible to attempt a "classic type" start but will stop when reading sprites.
//
// Revision 1.3  2010/09/10 17:35:49  velktron
// DoomGame, Menu, renderers
//
// Revision 1.2  2010/07/06 16:32:38  velktron
// Threw some work in WI, now EndLevel. YEAH THERE'S GONNA BE A SEPARATE EndLevel OBJECT THAT'S HOW PIMP THE PROJECT IS!!!!11!!!
//
// Revision 1.1  2010/06/30 08:58:50  velktron
// Let's see if this stuff will finally commit....
//
//
// Most stuff is still  being worked on. For a good place to start and get an idea of what is being done, I suggest checking out the "testers" package.
//
// Revision 1.1  2010/06/29 11:07:34  velktron
// Release often, release early they say...
//
// Commiting ALL stuff done so far. A lot of stuff is still broken/incomplete, and there's still mixed C code in there. I suggest you load everything up in Eclpise and see what gives from there.
//
// A good place to start is the testers/ directory, where you  can get an idea of how a few of the implemented stuff works.
//
//
// DESCRIPTION:
//	Random number LUT.
//
//-----------------------------------------------------------------------------

class DoomRandom implements IRandom{

    /**
     * M_Random
     * Returns a 0-255 number. Made into shorts for Java, because of their nature.
     */
    public static short rndtable[] = {
        0,   8, 109, 220, 222, 241, 149, 107,  75, 248, 254, 140,  16,  66 ,
        74,  21, 211,  47,  80, 242, 154,  27, 205, 128, 161,  89,  77,  36 ,
        95, 110,  85,  48, 212, 140, 211, 249,  22,  79, 200,  50,  28, 188 ,
        52, 140, 202, 120,  68, 145,  62,  70, 184, 190,  91, 197, 152, 224 ,
        149, 104,  25, 178, 252, 182, 202, 182, 141, 197,   4,  81, 181, 242 ,
        145,  42,  39, 227, 156, 198, 225, 193, 219,  93, 122, 175, 249,   0 ,
        175, 143,  70, 239,  46, 246, 163,  53, 163, 109, 168, 135,   2, 235 ,
        25,  92,  20, 145, 138,  77,  69, 166,  78, 176, 173, 212, 166, 113 ,
        94, 161,  41,  50, 239,  49, 111, 164,  70,  60,   2,  37, 171,  75 ,
        136, 156,  11,  56,  42, 146, 138, 229,  73, 146,  77,  61,  98, 196 ,
        135, 106,  63, 197, 195,  86,  96, 203, 113, 101, 170, 247, 181, 113 ,
        80, 250, 108,   7, 255, 237, 129, 226,  79, 107, 112, 166, 103, 241 ,
        24, 223, 239, 120, 198,  58,  60,  82, 128,   3, 184,  66, 143, 224 ,
        145, 224,  81, 206, 163,  45,  63,  90, 168, 114,  59,  33, 159,  95 ,
        28, 139, 123,  98, 125, 196,  15,  70, 194, 253,  54,  14, 109, 226 ,
        71,  17, 161,  93, 186,  87, 244, 138,  20,  52, 123, 251,  26,  36 ,
        17,  46,  52, 231, 232,  76,  31, 221,  84,  37, 216, 165, 212, 106 ,
        197, 242,  98,  43,  39, 175, 254, 145, 190,  84, 118, 222, 187, 136 ,
        120, 163, 236, 249
    };

    protected int rndindex = 0;
    protected int prndindex = 0;

    // Which one is deterministic?
    @Override
    public int P_Random() {
        prndindex = (prndindex + 1) & 0xff;
        return rndtable[prndindex];
    }

    /**
     * [Maes] I'd rather dispatch the call here, than making IRandom aware of DoomStatus. Replace RND.P_Random calls
     * with DM.P_Random(callerid) etc.
     *
     * Fixme: this could be made into a proper enum
     *
     * @param caller
     */
    @Override
    public int P_Random(int caller) {
        int value = P_Random();
        SLY.sync("PR #%d [%d]=%d\n", caller, prndindex, value);
        return value;
    }

    @Override
    public int P_Random(String message) {
        int value = P_Random();
        SLY.sync("PR %s [%d]=%d\n", message,
                prndindex, value);
        return value;
    }

    @Override
    public int P_Random(ActiveStates caller, int sequence) {
        int value = P_Random();
        /*
	SLY.sync("PR #%d %s_%d [%d]=%d\n", caller.ordinal(),caller,sequence,
			prndindex, value);*/
        return value;
    }

    @Override
    public int P_Random(ActiveStates caller, mobjtype_t type, int sequence) {
        int value = P_Random();
        /*
    SLY.sync("PR #%d %s_%d %s [%d]=%d\n", caller.ordinal(),caller,sequence,
        type, prndindex, value);*/
        return value;
    }

    @Override
    public int M_Random() {
        rndindex = (rndindex + 1) & 0xff;
        return rndtable[rndindex];
    }

    @Override
    public void ClearRandom() {
        rndindex = prndindex = 0;
    }

    DoomRandom() {
        SLY = null;
    }

    @Override
    public int getIndex() {
        return prndindex;
    }

    DoomRandom(ISyncLogger SLY) {
        this.SLY = SLY;
    }

    private final ISyncLogger SLY;

}

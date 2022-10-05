package org.bleachhack.util.doom.doom;
// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: items.java,v 1.3 2010/12/20 17:15:08 velktron Exp $
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
// $Log: items.java,v $
// Revision 1.3  2010/12/20 17:15:08  velktron
// Made the renderer more OO -> TextureManager and other changes as well.
//
// Revision 1.2  2010/08/19 23:14:49  velktron
// Automap
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
//
//-----------------------------------------------------------------------------

import org.bleachhack.util.doom.defines.*;

public class items{

public static weaponinfo_t[]	weaponinfo=
{
    new weaponinfo_t(
	// fist
	ammotype_t.am_noammo,
	statenum_t.S_PUNCHUP,
	statenum_t.S_PUNCHDOWN,
	statenum_t.S_PUNCH,
	statenum_t.S_PUNCH1,
	statenum_t.S_NULL
    ),	
    new weaponinfo_t(
	// pistol
    ammotype_t.am_clip,
	statenum_t.S_PISTOLUP,
	statenum_t.S_PISTOLDOWN,
	statenum_t.S_PISTOL,
	statenum_t.S_PISTOL1,
	statenum_t.S_PISTOLFLASH
	), new weaponinfo_t(
	// shotgun
    ammotype_t.am_shell,
    statenum_t.S_SGUNUP,
    statenum_t.S_SGUNDOWN,
    statenum_t.S_SGUN,
    statenum_t.S_SGUN1,
    statenum_t.S_SGUNFLASH1
    ),
    new weaponinfo_t(
	// chaingun
    ammotype_t.am_clip,
	statenum_t.S_CHAINUP,
	statenum_t.S_CHAINDOWN,
	statenum_t.S_CHAIN,
	statenum_t.S_CHAIN1,
	statenum_t.S_CHAINFLASH1
    ),
    new weaponinfo_t(
	// missile launcher
        ammotype_t.am_misl,
	statenum_t.S_MISSILEUP,
	statenum_t.S_MISSILEDOWN,
	statenum_t.S_MISSILE,
	statenum_t.S_MISSILE1,
	statenum_t.S_MISSILEFLASH1
    ),
    new weaponinfo_t(
	// plasma rifle
        ammotype_t.am_cell,
	statenum_t.S_PLASMAUP,
	statenum_t.S_PLASMADOWN,
	statenum_t.S_PLASMA,
	statenum_t.S_PLASMA1,
	statenum_t.S_PLASMAFLASH1
    ),
    new weaponinfo_t(
	// bfg 9000
        ammotype_t.am_cell,
	statenum_t.S_BFGUP,
	statenum_t.S_BFGDOWN,
	statenum_t.S_BFG,
	statenum_t.S_BFG1,
	statenum_t.S_BFGFLASH1
    ),
    new weaponinfo_t(
	// chainsaw
        ammotype_t.am_noammo,
	statenum_t.S_SAWUP,
	statenum_t.S_SAWDOWN,
	statenum_t.S_SAW,
	statenum_t.S_SAW1,
	statenum_t.S_NULL
    ),
    new weaponinfo_t(
	// super shotgun
    ammotype_t.am_shell,
    statenum_t.S_DSGUNUP,
	statenum_t.S_DSGUNDOWN,
	statenum_t.S_DSGUN,
	statenum_t.S_DSGUN1,
	statenum_t.S_DSGUNFLASH1
    )
    };
}









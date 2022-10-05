package org.bleachhack.util.doom.m;

// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: Swap.java,v 1.2 2011/07/27 20:48:20 velktron Exp $
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
// DESCRIPTION:
//	Endianess handling, swapping 16bit and 32bit.
//  It's role is much less important than in C-based ports (because of stream
//  built-in endianness settings), but they are still used occasionally.
//
//-----------------------------------------------------------------------------

public final class Swap{


// Swap 16bit, that is, MSB and LSB byte.
public final static short SHORT(short x)
{
    // No masking with 0xFF should be necessary. 
    // MAES: necessary with java due to sign trailing.
    
    return (short) ((short) ((x>>>8)&0xFF) | (x<<8));
}

//Swap 16bit, that is, MSB and LSB byte.
public final static short SHORT(char x)
{
    // No masking with 0xFF should be necessary. 
    // MAES: necessary with java due to sign trailing.
    
    return (short) ((short) ((x>>>8)&0xFF) | (x<<8));
}

//Swap 16bit, that is, MSB and LSB byte.
public final static char USHORT(char x)
{
    // No masking with 0xFF should be necessary. 
    // MAES: necessary with java due to sign trailing.
    
    return (char) ((char) ((x>>>8)&0xFF) | (x<<8));
}


// Swapping 32bit.
// Maes: the "long" here is really 32-bit.
public final static int LONG( int x)
{
    return
	(x>>>24)
	| ((x>>>8) & 0xff00)
	| ((x<<8) & 0xff0000)
	| (x<<24);
}
}

//$Log: Swap.java,v $
//Revision 1.2  2011/07/27 20:48:20  velktron
//Proper commenting, cleanup.
//
//Revision 1.1  2010/06/30 08:58:50  velktron
//Let's see if this stuff will finally commit....

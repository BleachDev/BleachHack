package org.bleachhack.util.doom.data;
// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: doomtype.java,v 1.3 2011/02/11 00:11:13 velktron Exp $
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
//	Simple basic typedefs, isolated here to make it easier
//	 separating modules.
//    
//-----------------------------------------------------------------------------


public class doomtype {

// C's "chars" are actually Java signed bytes.
public static byte MAXCHAR =((byte)0x7f);
public static short MAXSHORT=	((short)0x7fff);

// Max pos 32-bit int.
public static int MAXINT=((int)0x7fffffff);	
public static long MAXLONG=((long)0x7fffffff);
public static byte MINCHAR=((byte)0x80);
public static short MINSHORT=((short)0x8000);

// Max negative 32-bit integer.
public static int MININT=((int)0x80000000);
public static long MINLONG=((long)0x80000000);
}
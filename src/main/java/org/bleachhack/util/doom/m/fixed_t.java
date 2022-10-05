package org.bleachhack.util.doom.m;

import org.bleachhack.util.doom.data.Defines;
// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: fixed_t.java,v 1.14 2011/10/25 19:52:13 velktron Exp $
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
//
// DESCRIPTION:
//	Fixed point implementation.
//
//-----------------------------------------------------------------------------

//
// Fixed point, 32bit as 16.16.
//
// Most functionality of C-based ports is preserved, EXCEPT that there's
// no typedef of ints into fixed_t, and that there's no actual object fixed_t
// type that is actually instantiated in the current codebase, for performance reasons.
// There are still remnants of a full OO implementation that still do work, 
// and the usual FixedMul/FixedDiv etc. methods are still used throughout the codebase,
// but operate on int operants (signed, 32-bit integers).

public class fixed_t implements Comparable<fixed_t>{

    public static final int FRACBITS =       16;
    public static final int FRACUNIT =       (1<<FRACBITS);
    public static final int MAPFRACUNIT =    FRACUNIT/Defines.TIC_MUL;
    public int val;
    
    public fixed_t(){
        this.set(0);
    }
    
    public int get(){
        return val;
    }
    
    public void set(int val){
        this.val=val;
    }

    public void copy(fixed_t a){
        this.set(a.get());
    }

    
    public boolean equals(fixed_t a){
        return (this.get()==a.get())?true:false;
    }

    public static boolean equals(fixed_t a, fixed_t b){
        return (a.get()==b.get())?true:false;
    }    
    
    public fixed_t(int val){
        this.val=val;
    }
    
public fixed_t(fixed_t x) {
        this.val=x.val;
    }

public static final String rcsid = "$Id: fixed_t.java,v 1.14 2011/10/25 19:52:13 velktron Exp $";

/** Creates a new fixed_t object for the result a*b
 * 
 * @param a
 * @param b
 * @return
 */

public static int FixedMul
( fixed_t	a,
  fixed_t	b )
{
    return (int)(((long) a.val * (long ) b.val) >>> FRACBITS);
}

public static int FixedMul
( int   a,
  fixed_t   b )
{
    return (int)(((long) a * (long ) b.val) >>> FRACBITS);
}

public static final int FixedMul
( int   a,
  int   b )
{
    return (int)(((long) a * (long ) b) >>> FRACBITS);
}


/** Returns result straight as an int..
 * 
 * @param a
 * @param b
 * @return
 */

public static int FixedMulInt
( fixed_t   a,
        fixed_t   b )
{
    return (int)(((long) a.val * (long ) b.val) >> FRACBITS);
}

/** In-place c=a*b
 * 
 * @param a
 * @param b
 * @param c
 */

public final static void FixedMul
( fixed_t   a,
        fixed_t   b,
        fixed_t c)
{
    c.set((int)(((long) a.val * (long ) b.val) >> FRACBITS));
}


/** In-place this=this*a
 * 
 * @param a
 * @param b
 * @param c
 */

public final void FixedMul
( fixed_t   a)
{
    this.set((int)(((long) a.val * (long ) this.val) >> FRACBITS));
}


public final static int
FixedDiv
( int   a,
  int   b )
{
	  if ((Math.abs(a) >> 14) >= Math.abs(b))
	    {
		return (a^b) < 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
	    }
	    else
	    {
		long result;

		result = ((long) a << 16) / b;

		return (int) result;
	    }
}


public final static int
FixedDiv2
( int   a,
  int   b )
{

    
    int c;
    c = (int)(((long)a<<16) / (long)b);
    return c;
    
    /*
    double c;

    c = ((double)a) / ((double)b) * FRACUNIT;

  if (c >= 2147483648.0 || c < -2147483648.0)
      throw new ArithmeticException("FixedDiv: divide by zero");
 
 return (int)c;*/
}

@Override
public int compareTo(fixed_t o) {
    if (o.getClass()!=fixed_t.class) return -1;
    if (this.val==((fixed_t)(o)).val) return 0;
    if (this.val>((fixed_t)(o)).val) return 1;
    else return -1;
    }

public int compareTo(int o) {
    if (this.val==o) return 0;
    if (this.val>o) return 1;
    else return -1;
    }

public void add(fixed_t a){
    this.val+=a.val;
}

public void sub(fixed_t a){
    this.val-=a.val;
}

public void add(int a){
    this.val+=a;
}

public void sub(int a){
    this.val-=a;
}

/** a+b
 * 
 * @param a
 * @param b
 * @return
 */

public static int add(fixed_t a,fixed_t b){
    return a.val+b.val;
}

/** a-b
 * 
 * @param a
 * @param b
 * @return
 */

public static int sub(fixed_t a,fixed_t b){
    return a.val-b.val;
}

/** c=a+b
 * 
 * @param c
 * @param a
 * @param b
 */

public static void add(fixed_t c, fixed_t a,fixed_t b){
    c.val= a.val+b.val;
}

/** c=a-b
 * 
 * @param c
 * @param a
 * @param b
 */

public static void sub(fixed_t c,fixed_t a,fixed_t b){
    c.val= a.val-b.val;
}


/** Equals Zero
 * 
 * @return
 */

public boolean isEZ() {
    return (this.val==0);
    }

/** Greater than Zero
 * 
 * @return
 */

public boolean isGZ() {
    return (this.val>0);
    }

/** Less than Zero
 * 
 * @return
 */
public boolean isLZ() {
    return (this.val<0);
    }

// These are here to make easier handling all those methods in R 
// that return "1" or "0" based on one result.

public int oneEZ(){
    return (this.val==0)?1:0;
}

public int oneGZ(){
    return (this.val>0)?1:0;
}

public int oneLZ(){
    return (this.val<0)?1:0;
}


}
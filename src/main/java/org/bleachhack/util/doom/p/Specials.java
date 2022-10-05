package org.bleachhack.util.doom.p;

import org.bleachhack.util.doom.doom.player_t;
import org.bleachhack.util.doom.m.fixed_t;
import static org.bleachhack.util.doom.m.fixed_t.*;
import org.bleachhack.util.doom.p.Actions.ActionsLights.glow_t;
import org.bleachhack.util.doom.p.Actions.ActionsLights.lightflash_t;
import org.bleachhack.util.doom.rr.line_t;
import org.bleachhack.util.doom.rr.sector_t;
import org.bleachhack.util.doom.rr.side_t;

// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: Specials.java,v 1.7 2011/06/01 00:09:08 velktron Exp $
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
// DESCRIPTION:  none
//	Implements special effects:
//	Texture animation, height or lighting changes
//	 according to adjacent sectors, respective
//	 utility functions, etc.
//
//-----------------------------------------------------------------------------

public interface Specials {

//
// End-level timer (-TIMER option)
//
//extern	boolean levelTimer;
//extern	int	levelTimeCount;


//      Define values for map objects
public static final int MO_TELEPORTMAN          =14;


// at game start
public void    P_InitPicAnims ();

// at map load
public void    P_SpawnSpecials ();

// every tic
public void    P_UpdateSpecials ();

// when needed
public boolean
P_UseSpecialLine
( mobj_t	thing,
  line_t	line,
  int		side );

public void
P_ShootSpecialLine
( mobj_t	thing,
  line_t	line );

public 
void
P_CrossSpecialLine
( int		linenum,
  int		side,
  mobj_t	thing );

public void    P_PlayerInSpecialSector (player_t player);

public int
twoSided
( int		sector,
  int		line );

public sector_t
getSector
( int		currentSector,
  int		line,
  int		side );

side_t
getSide
( int		currentSector,
  int		line,
  int		side );

public fixed_t P_FindLowestFloorSurrounding(sector_t sec);
public fixed_t P_FindHighestFloorSurrounding(sector_t sec);

public fixed_t
P_FindNextHighestFloor
( sector_t	sec,
  int		currentheight );

public fixed_t P_FindLowestCeilingSurrounding(sector_t sec);
public fixed_t P_FindHighestCeilingSurrounding(sector_t sec);

public int
P_FindSectorFromLineTag
( line_t	line,
  int		start );

public int
P_FindMinSurroundingLight
( sector_t	sector,
  int		max );

public sector_t
getNextSector
( line_t	line,
  sector_t	sec );


//
// SPECIAL
//
int EV_DoDonut(line_t line);


public static final int GLOWSPEED	=		8;
public static final int STROBEBRIGHT=		5;
public static final int FASTDARK	=		15;
public static final int SLOWDARK	=		35;


public void    P_SpawnFireFlicker (sector_t sector);
public void    T_LightFlash (lightflash_t flash);
public void    P_SpawnLightFlash (sector_t sector);
public void    T_StrobeFlash (strobe_t flash);

public void
P_SpawnStrobeFlash
( sector_t	sector,
  int		fastOrSlow,
  int		inSync );

public void    EV_StartLightStrobing(line_t line);
public void    EV_TurnTagLightsOff(line_t line);

public void
EV_LightTurnOn
( line_t	line,
  int		bright );

public void    T_Glow(glow_t g);
public void    P_SpawnGlowingLight(sector_t sector);

 // max # of wall switches in a level
public static final int MAXSWITCHES	=	50;

 // 4 players, 4 buttons each at once, max.
public static final int MAXBUTTONS		=16;

 // 1 second, in ticks. 
public static final int BUTTONTIME   =   35         ;    

//extern button_t	buttonlist[MAXBUTTONS]; 

public void
P_ChangeSwitchTexture
( line_t	line,
  int		useAgain );

public void P_InitSwitchList();

public static final int PLATWAIT    =	3;
public static final int PLATSPEED	=	FRACUNIT;
public static final int MAXPLATS	=	30;


//extern plat_t*	activeplats[MAXPLATS];

public void    T_PlatRaise(plat_t	plat);

public int
EV_DoPlat
( line_t	line,
  plattype_e	type,
  int		amount );

void    P_AddActivePlat(plat_t plat);
void    P_RemoveActivePlat(plat_t plat);
void    EV_StopPlat(line_t line);
void    P_ActivateInStasis(int tag);

public static final int VDOORSPEED	=	FRACUNIT*2;
public static final int VDOORWAIT   =		150;

void
EV_VerticalDoor
( line_t	line,
  mobj_t	thing );

int
EV_DoDoor
( line_t	line,
  vldoor_e	type );

int
EV_DoLockedDoor
( line_t	line,
  vldoor_e	type,
  mobj_t	thing );

public void    T_VerticalDoor (vldoor_t door);
public void    P_SpawnDoorCloseIn30 (sector_t sec);

void
P_SpawnDoorRaiseIn5Mins
( sector_t	sec,
  int		secnum );

}


// UNUSED
//
//      Sliding doors...
//

/*
typedef enum
{
    sd_opening,
    sd_waiting,
    sd_closing

} sd_e;



typedef enum
{
    sdt_openOnly,
    sdt_closeOnly,
    sdt_openAndClose

} sdt_e;
*/

/*

typedef struct
{
    thinker_t	thinker;
    sdt_e	type;
    line_t*	line;
    int		frame;
    int		whichDoorIndex;
    int		timer;
    sector_t*	frontsector;
    sector_t*	backsector;
    sd_e	 status;

} slidedoor_t;
*/

/*

typedef struct
{
    char	frontFrame1[9];
    char	frontFrame2[9];
    char	frontFrame3[9];
    char	frontFrame4[9];
    char	backFrame1[9];
    char	backFrame2[9];
    char	backFrame3[9];
    char	backFrame4[9];
    
} slidename_t;
*/

/*

typedef struct
{
    int             frontFrames[4];
    int             backFrames[4];

} slideframe_t;

*/

/*
// how many frames of animation
#define SNUMFRAMES		4

#define SDOORWAIT		35*3
#define SWAITTICS		4

// how many diff. types of anims
#define MAXSLIDEDOORS	5                            

void P_InitSlidingDoorFrames(void);

void
EV_SlidingDoor
( line_t*	line,
  mobj_t*	thing );
#endif

#define CEILSPEED		FRACUNIT
#define CEILWAIT		150
#define MAXCEILINGS		30

extern ceiling_t*	activeceilings[MAXCEILINGS];

int
EV_DoCeiling
( line_t*	line,
  ceiling_e	type );

void    T_MoveCeiling (ceiling_t* ceiling);
void    P_AddActiveCeiling(ceiling_t* c);
void    P_RemoveActiveCeiling(ceiling_t* c);
int	EV_CeilingCrushStop(line_t* line);
void    P_ActivateInStasisCeiling(line_t* line);

#define FLOORSPEED		FRACUNIT

result_e
T_MovePlane
( sector_t*	sector,
  fixed_t	speed,
  fixed_t	dest,
  boolean	crush,
  int		floorOrCeiling,
  int		direction );

int
EV_BuildStairs
( line_t*	line,
  stair_e	type );

int
EV_DoFloor
( line_t*	line,
  floor_e	floortype );

void T_MoveFloor( floormove_t* floor);

//
// P_TELEPT
//
int
EV_Teleport
( line_t*	line,
  int		side,
  mobj_t*	thing );
*/
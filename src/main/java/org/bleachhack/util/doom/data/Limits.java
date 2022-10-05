package org.bleachhack.util.doom.data;

import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import static org.bleachhack.util.doom.m.fixed_t.MAPFRACUNIT;

/** Everything that constitutes a removable limit should go here */

public final class Limits {

	// Obvious rendering limits
	public static final int MAXVISPLANES = 128;
	public static final int MAXSEGS = 32;
	public static final int MAXVISSPRITES = 128;
	public static final int MAXDRAWSEGS = 256;
	// MAES: Moved MAXOPENINGS to renderer state, it's scale dependant.
	public static final int CEILSPEED = MAPFRACUNIT;
	public static final int CEILWAIT = 150;
	public static final int MAXCEILINGS = 30;

	public static final int MAXANIMS = 32;

	/** Animating line specials */
	public static final int MAXLINEANIMS = 64;

	// These are only used in the renderer, effectively putting
	// a limit to the size of lookup tables for screen buffers.
	public static final int MAXWIDTH = 1600;
	public static final int MAXHEIGHT = 1200;

	// Command line/file limits
	public static final int MAXWADFILES = 20;
	public static final int MAXARGVS = 100;

	// The maximum number of players, multiplayer/networking.
	// Max computers/players in a game. AFFECTS SAVEGAMES.
	public static final int MAXPLAYERS = 4;
	public final static int MAXNETNODES = 8;

	/** Some quirky engine limits */
	public static final int MAXEVENTS = 64;
	
	/** max # of wall switch TYPES in a level */
	public static final int MAXSWITCHES = 50;

	/** 20 adjoining sectors max! */
	public static final int MAX_ADJOINING_SECTORS = 20;

	// 4 players, 4 buttons each at once, max.
	public static final int MAXBUTTONS = 16;

	// 1 second, in ticks.
	public static final int BUTTONTIME = 35;

	/**
	 * keep track of special lines as they are hit, but don't process them until
	 * the move is proven valid
	 */
	public static final int MAXSPECIALCROSS = 8;
	public static final int MAXHEALTH = 100;

	/**
	 * MAXRADIUS is for precalculated sector block boxes the spider demon is
	 * larger, but we do not have any moving sectors nearby
	 */
	public static final int MAXRADIUS = 32 * FRACUNIT;

	public static final int MAXINTERCEPTS = 128;
	public static final int MAXMOVE = (30 * MAPFRACUNIT);

	/** Player spawn spots for deathmatch. */
	public static final int MAX_DM_STARTS = 10;

	// C's "chars" are actually Java signed bytes.
	public static final byte MAXCHAR = ((byte) 0x7f);
	public static final byte MINCHAR = ((byte) 0x80);

	// 16-bit integers...
	public static final short MAXSHORT = ((short) 0x7fff);
	public static final short MINSHORT = ((short) 0x8000);

	// Max pos 32-bit int.
	public static final int MAXINT = ((int) 0x7fffffff);
	public static final long MAXLONG = ((long) 0x7fffffff);

	// Max negative 32-bit integer. These are considered to be the same.
	public static final int MININT = ((int) 0x80000000);
	public static final long MINLONG = ((long) 0x80000000);

	// Buffering/memory limits.
	public static final int SAVEGAMESIZE = 0x2c000;

	public static final int SAVESTRINGSIZE = 24;
	public static final int VERSIONSIZE = 16;

	public static final int PLATWAIT = 3;
	public static final int PLATSPEED = MAPFRACUNIT;
	public static final int MAXPLATS = 30;
	public static final int MAXSKULLS = 20;
	public static final int NUMBRAINTARGETS=32;
	public static final int NUMMOBJTYPES=mobjtype_t.NUMMOBJTYPES.ordinal();
	
}

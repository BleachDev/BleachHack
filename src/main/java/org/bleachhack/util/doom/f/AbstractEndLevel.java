package org.bleachhack.util.doom.f;

import static org.bleachhack.util.doom.data.Defines.TICRATE;
import org.bleachhack.util.doom.wad.animenum_t;

public abstract class AbstractEndLevel {
	
	//NET GAME STUFF

    public static final int NG_STATSY = 50;
    public static final int NG_SPACINGX = 64;

    //DEATHMATCH STUFF
    public static final int DM_MATRIXX = 42;
    public static final int DM_MATRIXY = 68;

    public static final int DM_SPACINGX = 40;

    public static final int DM_TOTALSX = 269;

    public static final int DM_KILLERSX = 10;
    public static final int DM_KILLERSY = 100;
    public static final int DM_VICTIMSX = 5;
    public static final int DM_VICTIMSY = 50;

	// static point_t lnodes[NUMEPISODES][NUMMAPS] 
	final static public point_t[][] lnodes =
	{
	    // Episode 0 World Map
	    {
	    new point_t( 185, 164 ),   // location of level 0 (CJ)
	    new point_t( 148, 143 ),   // location of level 1 new point_t(CJ)
	    new point_t( 69, 122 ),    // location of level 2 new point_t(CJ)
	    new point_t( 209, 102 ),   // location of level 3 new point_t(CJ)
	    new point_t( 116, 89 ),    // location of level 4 new point_t(CJ)
	    new point_t( 166, 55 ),    // location of level 5 new point_t(CJ)
	    new point_t( 71, 56 ), // location of level 6 new point_t(CJ)
	    new point_t( 135, 29 ),    // location of level 7 new point_t(CJ)
	    new point_t( 71, 24 )  // location of level 8 new point_t(CJ)
	    },

	    // Episode 1 World Map should go here
	    {
	    new point_t( 254, 25 ),    // location of level 0 new point_t(CJ)
	    new point_t( 97, 50 ), // location of level 1 new point_t(CJ)
	    new point_t( 188, 64 ),    // location of level 2 new point_t(CJ)
	    new point_t( 128, 78 ),    // location of level 3 new point_t(CJ)
	    new point_t( 214, 92 ),    // location of level 4 new point_t(CJ)
	    new point_t( 133, 130 ),   // location of level 5 new point_t(CJ)
	    new point_t( 208, 136 ),   // location of level 6 new point_t(CJ)
	    new point_t( 148, 140 ),   // location of level 7 new point_t(CJ)
	    new point_t( 235, 158 )    // location of level 8 new point_t(CJ)
	    },

	    // Episode 2 World Map should go here
	    {
	    new point_t( 156, 168 ),   // location of level 0 new point_t(CJ)
	    new point_t( 48, 154 ),    // location of level 1 new point_t(CJ)
	    new point_t( 174, 95 ),    // location of level 2 new point_t(CJ)
	    new point_t( 265, 75 ),    // location of level 3 new point_t(CJ)
	    new point_t( 130, 48 ),    // location of level 4 new point_t(CJ)
	    new point_t( 279, 23 ),    // location of level 5 new point_t(CJ)
	    new point_t( 198, 48 ),    // location of level 6 new point_t(CJ)
	    new point_t( 140, 25 ),    // location of level 7 new point_t(CJ)
	    new point_t( 281, 136 )    // location of level 8 new point_t(CJ)
	    }

	};

	//
	//Animation locations for episode 0 (1).
	//Using patches saves a lot of space,
	//as they replace 320x200 full screen frames.
	//
	
	public static  final anim_t[] epsd0animinfo =
	{
	 new anim_t(animenum_t.ANIM_ALWAYS, TICRATE/3, 3, new point_t( 224, 104 ) ),
	  new anim_t( animenum_t.ANIM_ALWAYS, TICRATE/3, 3,  new point_t( 184, 160 ) ),
	  new anim_t( animenum_t.ANIM_ALWAYS, TICRATE/3, 3,  new point_t( 112, 136 ) ),
	  new anim_t( animenum_t.ANIM_ALWAYS, TICRATE/3, 3,  new point_t( 72, 112 ) ),
	  new anim_t( animenum_t.ANIM_ALWAYS, TICRATE/3, 3, new point_t( 88, 96 ) ),
	  new anim_t( animenum_t.ANIM_ALWAYS, TICRATE/3, 3,  new point_t( 64, 48 ) ),
	  new anim_t( animenum_t.ANIM_ALWAYS, TICRATE/3, 3,  new point_t( 192, 40 ) ),
	  new anim_t(animenum_t.ANIM_ALWAYS, TICRATE/3, 3,  new point_t( 136, 16 ) ),
	  new anim_t(animenum_t. ANIM_ALWAYS, TICRATE/3, 3,  new point_t( 80, 16 ) ),
	  new anim_t( animenum_t.ANIM_ALWAYS, TICRATE/3, 3,  new point_t( 64, 24 ) )
	};

	public static final anim_t[] epsd1animinfo =
	{
	  new anim_t( animenum_t.ANIM_LEVEL, TICRATE/3, 1,  new point_t( 128, 136 ), 1 ),
	  new anim_t( animenum_t.ANIM_LEVEL, TICRATE/3, 1,  new point_t( 128, 136 ), 2 ),
	  new anim_t( animenum_t.ANIM_LEVEL, TICRATE/3, 1,  new point_t( 128, 136 ), 3 ),
	  new anim_t( animenum_t.ANIM_LEVEL, TICRATE/3, 1, new point_t( 128, 136 ), 4 ),
	  new anim_t( animenum_t.ANIM_LEVEL, TICRATE/3, 1,  new point_t(128, 136 ), 5 ),
	  new anim_t( animenum_t.ANIM_LEVEL, TICRATE/3, 1,  new point_t( 128, 136 ), 6 ),
	  new anim_t( animenum_t.ANIM_LEVEL, TICRATE/3, 1,  new point_t( 128, 136 ), 7 ),
	  new anim_t( animenum_t.ANIM_LEVEL, TICRATE/3, 3, new point_t( 192, 144 ), 8 ),
	  new anim_t( animenum_t.ANIM_LEVEL, TICRATE/3, 1,  new point_t( 128, 136 ), 8 )
	};

	public static final anim_t[] epsd2animinfo =
	{
	  new anim_t( animenum_t.ANIM_ALWAYS, TICRATE/3, 3,  new point_t( 104, 168 ) ),
	  new anim_t( animenum_t.ANIM_ALWAYS, TICRATE/3, 3,  new point_t( 40, 136 ) ),
	  new anim_t( animenum_t.ANIM_ALWAYS, TICRATE/3, 3,  new point_t( 160, 96 ) ),
	  new anim_t( animenum_t.ANIM_ALWAYS, TICRATE/3, 3,  new point_t(104, 80 ) ),
	  new anim_t( animenum_t.ANIM_ALWAYS, TICRATE/3, 3, new point_t( 120, 32 ) ),
	  new anim_t( animenum_t.ANIM_ALWAYS, TICRATE/4, 3,  new point_t( 40, 0 ) )
	};

	/*static int NUMANIMS[NUMEPISODES] =
	{
	 sizeof(epsd0animinfo)/sizeof(anim_t),
	 sizeof(epsd1animinfo)/sizeof(anim_t),
	 sizeof(epsd2animinfo)/sizeof(anim_t)
	};*/

	// MAES: cute, but we can do it in a more Java-friendly way :-p

    public static final int[] NUMANIMS = {epsd0animinfo.length, epsd1animinfo.length, epsd2animinfo.length};

	/** ATTENTION: there's a difference between these "anims" and those used in p_spec.c */

	public static final anim_t[][] anims =
	{
	 epsd0animinfo,
	 epsd1animinfo,
	 epsd2animinfo
	};

}

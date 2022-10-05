package org.bleachhack.util.doom.p;

/** YEAH, I'M USING THE CONSTANTS INTERFACE PATTERN. DEAL WITH IT */

public interface MobjFlags {
 // // MF_ flags for mobjs.

    // Call P_SpecialThing when touched.
    public static final long MF_SPECIAL = 1;
    // Blocks.
    public static final long MF_SOLID = 2;
    // Can be hit.
    public static final long MF_SHOOTABLE = 4;
    // Don't use the sector links (invisible but touchable).
    public static final long MF_NOSECTOR = 8;
    // Don't use the blocklinks (inert but displayable)
    public static final long MF_NOBLOCKMAP = 16;

    // Not to be activated by sound, deaf monster.
    public static final long MF_AMBUSH = 32;
    // Will try to attack right back.
    public static final long MF_JUSTHIT = 64;
    // Will take at least one step before attacking.
    public static final long MF_JUSTATTACKED = 128;
    // On level spawning (initial position),
    // hang from ceiling instead of stand on floor.
    public static final long MF_SPAWNCEILING = 256;
    // Don't apply gravity (every tic),
    // that is, object will float, keeping current height
    // or changing it actively.
    public static final long MF_NOGRAVITY = 512;

    // Movement flags.
    // This allows jumps from high places.
    public static final long MF_DROPOFF = 0x400;
    // For players, will pick up items.
    public static final long MF_PICKUP = 0x800;
    // Player cheat. ???
    public static final int MF_NOCLIP = 0x1000;
    // Player: keep info about sliding along walls.
    public static final int MF_SLIDE = 0x2000;
    // Allow moves to any height, no gravity.
    // For active floaters, e.g. cacodemons, pain elementals.
    public static final int MF_FLOAT = 0x4000;
    // Don't cross lines
    // ??? or look at heights on teleport.
    public static final int MF_TELEPORT = 0x8000;
    // Don't hit same species, explode on block.
    // Player missiles as well as fireballs of various kinds.
    public static final int MF_MISSILE = 0x10000;
    // Dropped by a demon, not level spawned.
    // E.g. ammo clips dropped by dying former humans.
    public static final int MF_DROPPED = 0x20000;
    // Use fuzzy draw (shadow demons or spectres),
    // temporary player invisibility powerup.
    public static final int MF_SHADOW = 0x40000;
    // Flag: don't bleed when shot (use puff),
    // barrels and shootable furniture shall not bleed.
    public static final long MF_NOBLOOD = 0x80000;
    // Don't stop moving halfway off a step,
    // that is, have dead bodies slide down all the way.
    public static final long MF_CORPSE = 0x100000;
    // Floating to a height for a move, ???
    // don't auto float to target's height.
    public static final long MF_INFLOAT = 0x200000;

    // On kill, count this enemy object
    // towards intermission kill total.
    // Happy gathering.
    public static final long MF_COUNTKILL = 0x400000;

    // On picking up, count this item object
    // towards intermission item total.
    public static final long MF_COUNTITEM = 0x800000;

    // Special handling: skull in flight.
    // Neither a cacodemon nor a missile.
    public static final long MF_SKULLFLY = 0x1000000;

    // Don't spawn this object
    // in death match mode (e.g. key cards).
    public static final long MF_NOTDMATCH = 0x2000000;

    // Player sprites in multiplayer modes are modified
    // using an internal color lookup table for re-indexing.
    // If 0x4 0x8 or 0xc,
    // use a translation table for player colormaps
    public static final long MF_TRANSLATION = 0xc000000;
    // Hmm ???.
    public static final long MF_TRANSSHIFT = 26;

    public static final long  MF_UNUSED2      =(0x0000000010000000);
    public static final long  MF_UNUSED3      =(0x0000000020000000);

        // Translucent sprite?                                          // phares
    public static final long  MF_TRANSLUCENT  =(0x0000000040000000);

    // this is free            LONGLONG(0x0000000100000000)

    // these are greater than an int. That's why the flags below are now uint_64_t

    public static final long  MF_TOUCHY = (0x0000000100000000L);
    public static final long  MF_BOUNCES =(0x0000000200000000L);
    public static final long  MF_FRIEND = (0x0000000400000000L);

    public static final long  MF_RESSURECTED =(0x0000001000000000L);
    public static final long  MF_NO_DEPTH_TEST =(0x0000002000000000L);
    public static final long  MF_FOREGROUND = (0x0000004000000000L);
    
}

package org.bleachhack.util.doom.p;

import static org.bleachhack.util.doom.data.Defines.FLOATSPEED;
import static org.bleachhack.util.doom.data.Defines.GRAVITY;
import static org.bleachhack.util.doom.data.Defines.VIEWHEIGHT;
import org.bleachhack.util.doom.data.Tables;
import static org.bleachhack.util.doom.data.info.states;
import org.bleachhack.util.doom.data.mapthing_t;
import org.bleachhack.util.doom.data.mobjinfo_t;
import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.data.sounds.sfxenum_t;
import org.bleachhack.util.doom.data.spritenum_t;
import org.bleachhack.util.doom.data.state_t;
import org.bleachhack.util.doom.defines.*;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.SourceCode.fixed_t;
import org.bleachhack.util.doom.doom.player_t;
import org.bleachhack.util.doom.doom.thinker_t;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.bleachhack.util.doom.p.ActiveStates.MobjConsumer;
import static org.bleachhack.util.doom.p.MapUtils.AproxDistance;
import org.bleachhack.util.doom.rr.subsector_t;
import org.bleachhack.util.doom.s.ISoundOrigin;
import static org.bleachhack.util.doom.utils.C2JUtils.eval;
import static org.bleachhack.util.doom.utils.C2JUtils.pointer;
import org.bleachhack.util.doom.wad.IPackableDoomObject;
import org.bleachhack.util.doom.wad.IReadableDoomObject;
import org.bleachhack.util.doom.wad.IWritableDoomObject;

/**
 * 
 * NOTES: mobj_t
 * 
 * mobj_ts are used to tell the refresh where to draw an image, tell the world
 * simulation when objects are contacted, and tell the sound driver how to
 * position a sound.
 * 
 * The refresh uses the next and prev links to follow lists of things in sectors
 * as they are being drawn. The sprite, frame, and angle elements determine
 * which patch_t is used to draw the sprite if it is visible. The sprite and
 * frame values are allmost allways set from state_t structures. The
 * statescr.exe utility generates the states.h and states.c files that contain
 * the sprite/frame numbers from the statescr.txt source file. The xyz origin
 * point represents a point at the bottom middle of the sprite (between the feet
 * of a biped). This is the default origin position for patch_ts grabbed with
 * lumpy.exe. A walking creature will have its z equal to the floor it is
 * standing on.
 * 
 * The sound code uses the x,y, and subsector fields to do stereo positioning of
 * any sound effited by the mobj_t.
 * 
 * The play simulation uses the blocklinks, x,y,z, radius, height to determine
 * when mobj_ts are touching each other, touching lines in the map, or hit by
 * trace lines (gunshots, lines of sight, etc). The mobj_t->flags element has
 * various bit flags used by the simulation.
 * 
 * Every mobj_t is linked into a single sector based on its origin coordinates.
 * The subsector_t is found with R_PointInSubsector(x,y), and the sector_t can
 * be found with subsector->sector. The sector links are only used by the
 * rendering code, the play simulation does not care about them at all.
 * 
 * Any mobj_t that needs to be acted upon by something else in the play world
 * (block movement, be shot, etc) will also need to be linked into the blockmap.
 * If the thing has the MF_NOBLOCK flag set, it will not use the block links. It
 * can still interact with other things, but only as the instigator (missiles
 * will run into other things, but nothing can run into a missile). Each block
 * in the grid is 128*128 units, and knows about every line_t that it contains a
 * piece of, and every interactable mobj_t that has its origin contained.
 * 
 * A valid mobj_t is a mobj_t that has the proper subsector_t filled in for its
 * xy coordinates and is linked into the sector from which the subsector was
 * made, or has the MF_NOSECTOR flag set (the subsector_t needs to be valid even
 * if MF_NOSECTOR is set), and is linked into a blockmap block or has the
 * MF_NOBLOCKMAP flag set. Links should only be modified by the
 * P_[Un]SetThingPosition() functions. Do not change the MF_NO? flags while a
 * thing is valid.
 * 
 * Any questions?
 * 
 * @author admin
 * 
 */

public class mobj_t extends thinker_t implements ISoundOrigin, Interceptable,
		IWritableDoomObject, IPackableDoomObject, IReadableDoomObject {

	public final ActionFunctions A;
    
    public static mobj_t createOn(final DoomMain<?, ?> context) {
        if (eval(context.actions)) {
            return new mobj_t(context.actions);
        }
        
        return new mobj_t();
    }
    
    private mobj_t() {
		this.spawnpoint = new mapthing_t();
		this.A = null;
    }

	private mobj_t(final ActionFunctions A) {
		this.spawnpoint = new mapthing_t();
		this.A = A;
		// A mobj_t is ALSO a thinker, as it always contains the struct.
		// Don't fall for C's trickery ;-)
		// this.thinker=new thinker_t();
	}

	/* List: thinker links. */
	// public thinker_t thinker;

	/** Info for drawing: position. */
	@fixed_t public int x, y, z;

	/** More list: links in sector (if needed) */
	public thinker_t snext, sprev;

	// More drawing info: to determine current sprite.
	/**
	 * orientation. This needs to be long or else certain checks will fail...but
	 * I need to see it working in order to confirm
	 */
	public long angle;

	/** used to find patch_t and flip value */
	public spritenum_t mobj_sprite;
	/** might be ORed with FF_FULLBRIGHT */
	public int mobj_frame;

	/** Interaction info, by BLOCKMAP. Links in blocks (if needed). */
	public thinker_t bnext, bprev;

	/** MAES: was actually a pointer to a struct subsector_s */
	public subsector_t subsector;

	/** The closest interval over all contacted Sectors. */
	@fixed_t public int floorz, ceilingz;

	/** For movement checking. */
	@fixed_t public int radius, height;

	/** Momentums, used to update position. */
	@fixed_t public int momx, momy, momz;

	/** If == validcount, already checked. */
	public int validcount;

	public mobjtype_t type;
	// MAES: was a pointer
	public mobjinfo_t info; // &mobjinfo[mobj.type]

	public long mobj_tics; // state tic counter
	// MAES: was a pointer
	public state_t mobj_state;
	public long flags;
	public int health;

	/** Movement direction, movement generation (zig-zagging). */
	public int movedir; // 0-7
	public int movecount; // when 0, select a new dir

	/**
	 * Thing being chased/attacked (or NULL), also the originator for missiles.
	 * MAES: was a pointer
	 */
	public mobj_t target;
	public int p_target; // for savegames

	/**
	 * Reaction time: if non 0, don't attack yet. Used by player to freeze a bit
	 * after teleporting.
	 */
	public int reactiontime;

	/**
	 * If >0, the target will be chased no matter what (even if shot)
	 */
	public int threshold;

	/**
	 * Additional info record for player avatars only. Only valid if type ==
	 * MT_PLAYER struct player_s* player;
	 */

	public player_t player;

	/** Player number last looked for. */
	public int lastlook;

	/** For nightmare respawn. */
	public mapthing_t spawnpoint; // struct

	/** Thing being chased/attacked for tracers. */

	public mobj_t tracer; // MAES: was a pointer

	// // MF_ flags for mobjs.

	// Call P_SpecialThing when touched.
	public static final int MF_SPECIAL = 1;
	// Blocks.
	public static final int MF_SOLID = 2;
	// Can be hit.
	public static final int MF_SHOOTABLE = 4;
	// Don't use the sector links (invisible but touchable).
	public static final int MF_NOSECTOR = 8;
	// Don't use the blocklinks (inert but displayable)
	public static final int MF_NOBLOCKMAP = 16;

	// Not to be activated by sound, deaf monster.
	public static final int MF_AMBUSH = 32;
	// Will try to attack right back.
	public static final int MF_JUSTHIT = 64;
	// Will take at least one step before attacking.
	public static final int MF_JUSTATTACKED = 128;
	// On level spawning (initial position),
	// hang from ceiling instead of stand on floor.
	public static final int MF_SPAWNCEILING = 256;
	// Don't apply gravity (every tic),
	// that is, object will float, keeping current height
	// or changing it actively.
	public static final int MF_NOGRAVITY = 512;

	// Movement flags.
	// This allows jumps from high places.
	public static final int MF_DROPOFF = 0x400;
	// For players, will pick up items.
	public static final int MF_PICKUP = 0x800;
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
	public static final int MF_NOBLOOD = 0x80000;
	// Don't stop moving halfway off a step,
	// that is, have dead bodies slide down all the way.
	public static final int MF_CORPSE = 0x100000;
	// Floating to a height for a move, ???
	// don't auto float to target's height.
	public static final int MF_INFLOAT = 0x200000;

	// On kill, count this enemy object
	// towards intermission kill total.
	// Happy gathering.
	public static final int MF_COUNTKILL = 0x400000;

	// On picking up, count this item object
	// towards intermission item total.
	public static final int MF_COUNTITEM = 0x800000;

	// Special handling: skull in flight.
	// Neither a cacodemon nor a missile.
	public static final int MF_SKULLFLY = 0x1000000;

	// Don't spawn this object
	// in death match mode (e.g. key cards).
	public static final int MF_NOTDMATCH = 0x2000000;

	// Player sprites in multiplayer modes are modified
	// using an internal color lookup table for re-indexing.
	// If 0x4 0x8 or 0xc,
	// use a translation table for player colormaps
	public static final int MF_TRANSLATION = 0xc000000;
	// Hmm ???.
	public static final int MF_TRANSSHIFT = 26;

	/*
	 * The following methods were for the most part "contextless" and
	 * instance-specific, so they were implemented here rather that being
	 * scattered all over the package.
	 */

	/**
	 * P_SetMobjState Returns true if the mobj is still present.
	 */

	public boolean SetMobjState(statenum_t state) {
		state_t st;

		do {
			if (state == statenum_t.S_NULL) {
                mobj_state = null;
				// MAES/_D_: uncommented this as it should work by now (?).
				A.RemoveMobj(this);
				return false;
			}

			st = states[state.ordinal()];
			mobj_state = st;
			mobj_tics = st.tics;
			mobj_sprite = st.sprite;
			mobj_frame = st.frame;

			// Modified handling.
			// Call action functions when the state is set
            // TODO: try find a bug
            if (st.action.isParamType(MobjConsumer.class)) {
                st.action.fun(MobjConsumer.class).accept(A, this);
            }

			state = st.nextstate;
		} while (!eval(mobj_tics));

		return true;
	}

	/**
	 * P_ZMovement
	 */

	public void ZMovement() {
		@fixed_t int dist, delta;

		// check for smooth step up
		if ((player != null) && z < floorz) {
			player.viewheight -= floorz - z;

			player.deltaviewheight = (VIEWHEIGHT - player.viewheight) >> 3;
		}

		// adjust height
		z += momz;

		if (((flags & MF_FLOAT) != 0) && target != null) {
			// float down towards target if too close
			if ((flags & MF_SKULLFLY) == 0 && (flags & MF_INFLOAT) == 0) {
				dist = AproxDistance(x - target.x, y - target.y);

				delta = (target.z + (height >> 1)) - z;

				if (delta < 0 && dist < -(delta * 3))
					z -= FLOATSPEED;
				else if (delta > 0 && dist < (delta * 3))
					z += FLOATSPEED;
			}

		}

		// clip movement
		if (z <= floorz) {
			// hit the floor

			// Note (id):
			// somebody left this after the setting momz to 0,
			// kinda useless there.
			if ((flags & MF_SKULLFLY) != 0) {
				// the skull slammed into something
				momz = -momz;
			}

			if (momz < 0) {
				if (player != null && (momz < -GRAVITY * 8)) {
					// Squat down.
					// Decrease viewheight for a moment
					// after hitting the ground (hard),
					// and utter appropriate sound.
					player.deltaviewheight = momz >> 3;
					A.DOOM.doomSound.StartSound(this, sfxenum_t.sfx_oof);
				}
				momz = 0;
			}
			z = floorz;

			if ((flags & MF_MISSILE) != 0 && (flags & MF_NOCLIP) == 0) {
				A.ExplodeMissile(this);
				return;
			}
		} else if ((flags & MF_NOGRAVITY) == 0) {
			if (momz == 0)
				momz = -GRAVITY * 2;
			else
				momz -= GRAVITY;
		}

		if (z + height > ceilingz) {
			// hit the ceiling
			if (momz > 0)
				momz = 0;
			{
				z = ceilingz - height;
			}

			if ((flags & MF_SKULLFLY) != 0) { // the skull slammed into
												// something
				momz = -momz;
			}

			if ((flags & MF_MISSILE) != 0 && (flags & MF_NOCLIP) == 0) {
				A.ExplodeMissile(this);
			}
		}
	}

	public int eflags; // DOOM LEGACY

	// Fields used only during DSG unmarshalling
	public int stateid;
	public int playerid;
	public int p_tracer;

	/** Unique thing id, used during sync debugging */
    public int thingnum;

	public void clear() {
		fastclear.rewind();
		try {
			this.unpack(mobj_t.fastclear);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// _D_: to permit this object to save/load
    @Override
	public void read(DataInputStream f) throws IOException {
		// More efficient, avoids duplicating code and
		// handles little endian better.
		buffer.position(0);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		f.read(buffer.array());
		this.unpack(buffer);
	}

	@Override
	public void write(DataOutputStream f) throws IOException {

		// More efficient, avoids duplicating code and
		// handles little endian better.
		buffer.position(0);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		this.pack(buffer);
		f.write(buffer.array());

	}

    @Override
	public void pack(ByteBuffer b) throws IOException {
		b.order(ByteOrder.LITTLE_ENDIAN);
		super.pack(b); // Pack the head thinker.
		b.putInt(x);
		b.putInt(y);
		b.putInt(z);
		b.putInt(pointer(snext));
		b.putInt(pointer(sprev));
		b.putInt((int) (this.angle & Tables.BITS32));
		b.putInt(this.mobj_sprite.ordinal());
		b.putInt(this.mobj_frame);
		b.putInt(pointer(bnext));
		b.putInt(pointer(bprev));
		b.putInt(pointer(subsector));
		b.putInt(floorz);
		b.putInt(ceilingz);
		b.putInt(radius);
		b.putInt(height);
		b.putInt(momx);
		b.putInt(momy);
		b.putInt(momz);
		b.putInt(validcount);
		b.putInt(type.ordinal());
		b.putInt(pointer(info)); // TODO: mobjinfo
		b.putInt((int) (this.mobj_tics & Tables.BITS32));
		b.putInt(this.mobj_state.id); // TODO: state OK?
		b.putInt((int) this.flags); // truncate
		b.putInt(this.health);
		b.putInt(this.movedir);
		b.putInt(this.movecount);
		b.putInt(pointer(target)); // TODO: p_target?
		b.putInt(this.reactiontime);
		b.putInt(this.threshold);
		// Check for player.
		if (this.player != null) {
			b.putInt(1 + this.player.identify());

			// System.out.printf("Mobj with hashcode %d is player %d",pointer(this),1+this.player.identify());
		} else
			b.putInt(0);
		b.putInt(lastlook);
		spawnpoint.pack(b);
		b.putInt(pointer(tracer)); // tracer pointer stored.

	}

    @Override
	public void unpack(ByteBuffer b) throws IOException {
		b.order(ByteOrder.LITTLE_ENDIAN);
		super.unpack(b); // 12 Read the head thinker.
		this.x = b.getInt(); // 16
		this.y = b.getInt(); // 20
		this.z = b.getInt(); // 24
		b.getLong(); // TODO: snext, sprev. When are those set? 32
		this.angle = Tables.BITS32 & b.getInt(); // 36
		this.mobj_sprite = spritenum_t.values()[b.getInt()]; // 40
		this.mobj_frame = b.getInt(); // 44
		b.getLong(); // TODO: bnext, bprev. When are those set? 52
		b.getInt(); // TODO: subsector 56
		this.floorz = b.getInt(); // 60
		this.ceilingz = b.getInt(); // 64
		this.radius = b.getInt(); // 68
		this.height = b.getInt(); // 72
		this.momx = b.getInt(); // 76
		this.momy = b.getInt(); // 80
		this.momz = b.getInt(); // 84
		this.validcount = b.getInt(); // 88
		this.type = mobjtype_t.values()[b.getInt()]; // 92
		b.getInt(); // TODO: mobjinfo (deduced from type) //96
		this.mobj_tics = Tables.BITS32 & b.getInt(); // 100
		// System.out.println("State"+f.readLEInt());
		this.stateid = b.getInt(); // TODO: state OK?
		this.flags = b.getInt()&Tables.BITS32; // Only 32-bit flags can be restored
		this.health = b.getInt();
		this.movedir = b.getInt();
		this.movecount = b.getInt();
		this.p_target = b.getInt();
		this.reactiontime = b.getInt();
		this.threshold = b.getInt();
		this.playerid = b.getInt(); // TODO: player. Non null should mean that
									// it IS a player.
		this.lastlook = b.getInt();
		spawnpoint.unpack(b);
		this.p_tracer = b.getInt(); // TODO: tracer
	}

	private static ByteBuffer buffer = ByteBuffer.allocate(154);
	private static ByteBuffer fastclear = ByteBuffer.allocate(154);

	/*
	 * @Override protected void finalize(){ count++; if (count%100==0)
	 * System.err
	 * .printf("Total %d Mobj %s@%d finalized free memory: %d\n",count,
	 * this.type.name(),this.hashCode(),Runtime.getRuntime().freeMemory()); }
	 */
	protected static int count = 0;

	// TODO: a linked list of sectors where this object appears
	// public msecnode_t touching_sectorlist;

	// Sound origin stuff
	
	@Override
	public final int getX() {
		return x;
	}

	@Override
	public final int getY() {
		return y;
	}

	@Override
	public final int getZ() {
		return z;
	}
	
    @Override
	public String toString(){
	    return String.format("%s %d",this.type,this.thingnum);
	}

}

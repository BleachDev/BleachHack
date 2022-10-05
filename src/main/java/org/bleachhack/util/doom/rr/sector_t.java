package org.bleachhack.util.doom.rr;

import static org.bleachhack.util.doom.data.Limits.MAXINT;
import static org.bleachhack.util.doom.data.Limits.MAX_ADJOINING_SECTORS;
import org.bleachhack.util.doom.doom.SourceCode;
import org.bleachhack.util.doom.doom.SourceCode.P_Spec;
import static org.bleachhack.util.doom.doom.SourceCode.P_Spec.P_FindLowestCeilingSurrounding;
import org.bleachhack.util.doom.doom.SourceCode.fixed_t;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import org.bleachhack.util.doom.m.IRandom;
import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;
import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import org.bleachhack.util.doom.mochadoom.Loggers;
import org.bleachhack.util.doom.p.Resettable;
import org.bleachhack.util.doom.p.ThinkerList;
import org.bleachhack.util.doom.p.mobj_t;
import org.bleachhack.util.doom.s.degenmobj_t;
import static org.bleachhack.util.doom.utils.C2JUtils.memset;
import org.bleachhack.util.doom.wad.DoomIO;
import org.bleachhack.util.doom.wad.IPackableDoomObject;
import org.bleachhack.util.doom.wad.IReadableDoomObject;

/**
 * The SECTORS record, at runtime. Stores things/mobjs. Can be
 * archived/unarchived during savegames.
 * 
 * @author Maes
 */
public class sector_t implements IReadableDoomObject, IPackableDoomObject, Resettable {

    public ThinkerList TL;

    public IRandom RND;

    public sector_t() {
        blockbox = new int[4];
        id = -1;
    }

    /** (fixed_t) */
    public int floorheight, ceilingheight;

    public short floorpic;

    public short ceilingpic;

    public short lightlevel;

    public short special;

    public short tag;

    /** 0 = untraversed, 1,2 = sndlines -1 */
    public int soundtraversed;

    /** thing that made a sound (or null) (MAES: single pointer) */
    public mobj_t soundtarget;

    /** mapblock bounding box for height changes */
    public int[] blockbox;

    /**
     * origin for any sounds played by the sector. Used to be degenmobj_t, but
     * that's really a futile distinction.
     */
    public degenmobj_t soundorg;

    /** if == validcount, already checked */
    public int validcount;

    /** list of mobjs in sector (MAES: it's used as a linked list) */
    public mobj_t thinglist;

    /**
     * thinker_t for reversable actions. This actually was a void*, and in
     * practice it could store doors, plats, floors and ceiling objects.
     */
    public SectorAction specialdata;

    public int linecount;

    // struct line_s** lines; // [linecount] size
    // MAES: make this line_t[] for now?
    public line_t[] lines;

    /** Use for internal identification */
    public int id;

    /** killough 1/30/98: improves searches for tags. */
    public int nexttag,firsttag;  

    @Override
    public String toString() {
        String str =
            String.format("Sector: %d %x %x %d %d %d %d %d", id, floorheight,
                ceilingheight, floorpic, ceilingpic, lightlevel, special, // needed?
                tag); // needed?

        return str;
    }


    //
    // P_FindLowestFloorSurrounding()
    // FIND LOWEST FLOOR HEIGHT IN SURROUNDING SECTORS
    //
    public int FindLowestFloorSurrounding() {
        int i;
        line_t check;
        sector_t other;
        int floor = this.floorheight;

        for (i = 0; i < this.linecount; i++) {
            check = this.lines[i];
            other = check.getNextSector(this);

            if (other == null)
                continue;

            if (other.floorheight < floor)
                floor = other.floorheight;
        }
        return floor;
    }

    /**
     * P_FindHighestFloorSurrounding() FIND HIGHEST FLOOR HEIGHT IN SURROUNDING
     * SECTORS Compatibility problem: apparently this is hardcoded for vanilla
     * compatibility (instead of Integer.MIN_VALUE), but it will cause some
     * "semi-Boom" maps not to work, since it won't be able to lower stuff below
     * -500 units. The correct fix here would be to allow for -compatlevel style
     * options. Maybe later.
     * 
     * @param sec
     */

    public int FindHighestFloorSurrounding() {
        int i;
        line_t check;
        sector_t other;

        int floor = -500 * FRACUNIT;

        for (i = 0; i < this.linecount; i++) {
            check = this.lines[i];
            other = check.getNextSector(this);

            // The compiler nagged about this being unreachable, with
            // some older 1.6 JDKs, but that's obviously not true.
            if (other == null)
                continue;

            if (other.floorheight > floor)
                floor = other.floorheight;
        }
        return floor;
    }

    /**
     * P_FindNextHighestFloor FIND NEXT HIGHEST FLOOR IN SURROUNDING SECTORS
     * Note: this should be doable w/o a fixed array.
     * 
     * @param sec
     * @param currentheight
     * @return fixed
     */

    public int FindNextHighestFloor(int currentheight) {
        int i;
        int h;
        int min;
        line_t check;
        sector_t other;
        int height = currentheight;

        int heightlist[] = new int[MAX_ADJOINING_SECTORS];

        for (i = 0, h = 0; i < this.linecount; i++) {
            check = this.lines[i];
            other = check.getNextSector(this);

            if (other == null)
                continue;

            if (other.floorheight > height)
                heightlist[h++] = other.floorheight;

            // Check for overflow. Exit.
            if (h >= MAX_ADJOINING_SECTORS) {
                Loggers.getLogger(sector_t.class.getName()).log(Level.WARNING,
                    "Sector with more than 20 adjoining sectors\n");
                break;
            }
        }

        // Find lowest height in list
        if (h == 0)
            return currentheight;

        min = heightlist[0];

        // Range checking?
        for (i = 1; i < h; i++)
            if (heightlist[i] < min)
                min = heightlist[i];

        return min;
    }

    //
    // FIND LOWEST CEILING IN THE SURROUNDING SECTORS
    //
    @SourceCode.Exact
    @P_Spec.C(P_FindLowestCeilingSurrounding)
    public @fixed_t int FindLowestCeilingSurrounding() {
        line_t check;
        sector_t other;
        int height = MAXINT;

        for (int i = 0; i < this.linecount; i++) {
            check = this.lines[i];
            getNextSector: {
                other = check.getNextSector(this);
            }

            if (other == null) {
                continue;
            }

            if (other.ceilingheight < height) {
                height = other.ceilingheight;
            }
        }
        return height;
    }

    //
    // FIND HIGHEST CEILING IN THE SURROUNDING SECTORS
    //
    public int FindHighestCeilingSurrounding() {
        int i;
        line_t check;
        sector_t other;
        int height = 0;

        for (i = 0; i < this.linecount; i++) {
            check = this.lines[i];
            other = check.getNextSector(this);

            if (other == null)
                continue;

            if (other.ceilingheight > height)
                height = other.ceilingheight;
        }
        return height;
    }

    @Override
    public void read(DataInputStream f)
            throws IOException {

        // ACHTUNG: the only situation where we'd
        // like to read memory-format sector_t's is from
        // savegames, and in vanilla savegames, not all info
        // is saved (or read) from disk.

        this.floorheight = DoomIO.readLEShort(f) << FRACBITS;
        this.ceilingheight = DoomIO.readLEShort(f) << FRACBITS;
        // MAES: it may be necessary to apply a hack in order to
        // read vanilla savegames.
        this.floorpic = DoomIO.readLEShort(f);
        this.ceilingpic = DoomIO.readLEShort(f);
        // f.skipBytes(4);
        this.lightlevel = DoomIO.readLEShort(f);
        this.special = DoomIO.readLEShort(f); // needed?
        this.tag = DoomIO.readLEShort(f); // needed?
    }

    @Override
    public void pack(ByteBuffer b) {

        b.putShort((short) (floorheight >> FRACBITS));
        b.putShort((short) (ceilingheight >> FRACBITS));
        // MAES: it may be necessary to apply a hack in order to
        // read vanilla savegames.
        b.putShort(floorpic);
        b.putShort(ceilingpic);
        // f.skipBytes(4);
        b.putShort(lightlevel);
        b.putShort(special);
        b.putShort(tag);
    }

    @Override
    public void reset() {
        floorheight = 0;
        ceilingheight = 0;
        floorpic = 0;
        ceilingpic = 0;
        lightlevel = 0;
        special = 0;
        tag = 0;
        soundtraversed = 0;
        soundtarget = null;
        memset(blockbox, 0, blockbox.length);
        soundorg = null;
        validcount = 0;
        thinglist = null;
        specialdata = null;
        linecount = 0;
        lines = null;
        id = -1;

    }
}

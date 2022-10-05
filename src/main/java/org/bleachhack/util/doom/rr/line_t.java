package org.bleachhack.util.doom.rr;

import org.bleachhack.util.doom.defines.slopetype_t;
import org.bleachhack.util.doom.doom.SourceCode;
import org.bleachhack.util.doom.doom.SourceCode.P_Spec;
import static org.bleachhack.util.doom.doom.SourceCode.P_Spec.getNextSector;
import org.bleachhack.util.doom.doom.thinker_t;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import static org.bleachhack.util.doom.m.BBox.BOXBOTTOM;
import static org.bleachhack.util.doom.m.BBox.BOXLEFT;
import static org.bleachhack.util.doom.m.BBox.BOXRIGHT;
import static org.bleachhack.util.doom.m.BBox.BOXTOP;
import static org.bleachhack.util.doom.m.fixed_t.*;
import org.bleachhack.util.doom.p.Interceptable;
import org.bleachhack.util.doom.p.Resettable;
import org.bleachhack.util.doom.s.degenmobj_t;
import static org.bleachhack.util.doom.utils.C2JUtils.eval;
import static org.bleachhack.util.doom.utils.C2JUtils.memset;
import org.bleachhack.util.doom.wad.DoomIO;
import org.bleachhack.util.doom.wad.IPackableDoomObject;
import org.bleachhack.util.doom.wad.IReadableDoomObject;

/** This is the actual linedef */

public class line_t
        implements Interceptable, IReadableDoomObject, IPackableDoomObject,
        Resettable {

    public static final char NO_INDEX=0xFFFF;
    
    public line_t() {
        sidenum = new char[2];
        bbox = new int[4];
        slopetype = slopetype_t.ST_HORIZONTAL;
    }

    /**
     * Vertices, from v1 to v2. NOTE: these are almost never passed as-such, nor
     * linked to Maybe we can get rid of them and only use the value semantics?
     */
    public vertex_t v1, v2;

    /** remapped vertex coords, for quick lookup with value semantics */
    public int v1x, v1y, v2x, v2y;

    /** (fixed_t) Precalculated v2 - v1 for side checking. */
    public int dx, dy;

    /** Animation related. */
    public short flags, special, tag;

    /**
     * Visual appearance: SideDefs. sidenum[1] will be 0xFFFF if one sided
     */
    public char[] sidenum;

    /**
     * Neat. Another bounding box, for the extent of the LineDef. MAES: make
     * this a proper bbox? fixed_t bbox[4];
     */
    public int[] bbox;

    /** To aid move clipping. */
    public slopetype_t slopetype;

    /**
     * Front and back sector. Note: redundant? Can be retrieved from SideDefs.
     * MAES: pointers
     */
    public sector_t frontsector, backsector;

    public int frontsectorid, backsectorid;

    /** if == validcount, already checked */
    public int validcount;

    /** thinker_t for reversable actions MAES: (void*) */
    public thinker_t specialdata;

    public int specialdataid;

    public degenmobj_t soundorg;

    // From Boom
    public int tranlump;
    
    public int id;
    
    /** killough 4/17/98: improves searches for tags. */
    public int firsttag,nexttag;    

    /** For Boom stuff, interprets sidenum specially */
    public int getSpecialSidenum() {
        return (sidenum[0] << 16) & (0x0000ffff & sidenum[1]);
    }

    public void assignVertexValues() {
        this.v1x = v1.x;
        this.v1y = v1.y;
        this.v2x = v2.x;
        this.v2y = v2.y;

    }

    /**
     * P_PointOnLineSide
     * 
     * @param x
     *        fixed_t
     * @param y
     *        fixed_t
     * @return 0 or 1 (false, true) - (front, back)
     */
    public boolean PointOnLineSide(int x, int y)

    {
    	

    	  return
    			    (dx==0) ? x <= this.v1x ? this.dy > 0 : this.dy < 0 :
    			    (dy==0) ? y <= this.v1y ? this.dx < 0 : this.dx > 0 :
    			    FixedMul(y-this.v1y, this.dx>>FRACBITS) >=
    			    FixedMul(this.dy>>FRACBITS, x-this.v1x);
    	/*
        int dx, dy, left, right;
        if (this.dx == 0) {
            if (x <= this.v1x)
                return this.dy > 0;

            return this.dy < 0;
        }
        if (this.dy == 0) {
            if (y <= this.v1y)
                return this.dx < 0;

            return this.dx > 0;
        }

        dx = (x - this.v1x);
        dy = (y - this.v1y);

        left = FixedMul(this.dy >> FRACBITS, dx);
        right = FixedMul(dy, this.dx >> FRACBITS);

        if (right < left)
            return false; // front side
        return true; // back side*/
    }

    /**
     * P_BoxOnLineSide Considers the line to be infinite Returns side 0 or 1, -1
     * if box crosses the line. Doubles as a convenient check for whether a
     * bounding box crosses a line at all
     * 
     * @param tmbox
     *        fixed_t[]
     */
    public int BoxOnLineSide(int[] tmbox) {
        boolean p1 = false;
        boolean p2 = false;

        switch (this.slopetype) {
        // Line perfectly horizontal, box floating "north" of line
        case ST_HORIZONTAL:
            p1 = tmbox[BOXTOP] > v1y;
            p2 = tmbox[BOXBOTTOM] > v1y;
            if (dx < 0) {
                p1 ^= true;
                p2 ^= true;
            }
            break;

        // Line perfectly vertical, box floating "west" of line
        case ST_VERTICAL:

            p1 = tmbox[BOXRIGHT] < v1x;
            p2 = tmbox[BOXLEFT] < v1x;
            if (dy < 0) {
                p1 ^= true;
                p2 ^= true;
            }
            break;

        case ST_POSITIVE:
            // Positive slope, both points on one side.
            p1 = PointOnLineSide(tmbox[BOXLEFT], tmbox[BOXTOP]);
            p2 = PointOnLineSide(tmbox[BOXRIGHT], tmbox[BOXBOTTOM]);
            break;

        case ST_NEGATIVE:
            // Negative slope, both points (mirrored horizontally) on one side.
            p1 = PointOnLineSide(tmbox[BOXRIGHT], tmbox[BOXTOP]);
            p2 = PointOnLineSide(tmbox[BOXLEFT], tmbox[BOXBOTTOM]);
            break;
        }

        if (p1 == p2)
            return p1 ? 1 : 0;
        // Any other result means non-inclusive crossing.
        return -1;
    }

    /**
     * Variant of P_BoxOnLineSide. Uses inclusive checks, so that even lines on
     * the border of a box will be considered crossing. This is more useful for
     * building blockmaps.
     * 
     * @param tmbox
     *        fixed_t[]
     */
    public int BoxOnLineSideInclusive(int[] tmbox) {
        boolean p1 = false;
        boolean p2 = false;

        switch (this.slopetype) {
        // Line perfectly horizontal, box floating "north" of line
        case ST_HORIZONTAL:
            p1 = tmbox[BOXTOP] >= v1y;
            p2 = tmbox[BOXBOTTOM] >= v1y;
            if (dx < 0) {
                p1 ^= true;
                p2 ^= true;
            }
            break;

        // Line perfectly vertical, box floating "west" of line
        case ST_VERTICAL:

            p1 = tmbox[BOXRIGHT] <= v1x;
            p2 = tmbox[BOXLEFT] <= v1x;
            if (dy < 0) {
                p1 ^= true;
                p2 ^= true;
            }
            break;

        case ST_POSITIVE:
            // Positive slope, both points on one side.
            p1 = PointOnLineSide(tmbox[BOXLEFT], tmbox[BOXTOP]);
            p2 = PointOnLineSide(tmbox[BOXRIGHT], tmbox[BOXBOTTOM]);
            break;

        case ST_NEGATIVE:
            // Negative slope, both points (mirrored horizontally) on one side.
            p1 = PointOnLineSide(tmbox[BOXRIGHT], tmbox[BOXTOP]);
            p2 = PointOnLineSide(tmbox[BOXLEFT], tmbox[BOXBOTTOM]);
            break;
        }

        if (p1 == p2)
            return p1 ? 1 : 0;
        // Any other result means non-inclusive crossing.
        return -1;
    }

    /**
     * getNextSector() Return sector_t * of sector next to current. NULL if not
     * two-sided line
     */
    @SourceCode.Compatible("getNextSector(line_t line, sector_t sec)")
    @P_Spec.C(getNextSector)
    public sector_t getNextSector(sector_t sec) {
        if (!eval(flags & ML_TWOSIDED)) {
            return null;
        }

        if (frontsector == sec) {
            return backsector;
        }

        return frontsector;
    }

    public String toString() {
        return (String.format("Line %d Flags: %x Special %d Tag: %d ", this.id,this.flags,
            this.special, this.tag));
    }

    @Override
    public void read(DataInputStream f)
            throws IOException {

        // For histerical reasons, these are the only parts of line_t that
        // are archived in vanilla savegames. Go figure.
        this.flags = DoomIO.readLEShort(f);
        this.special = DoomIO.readLEShort(f);
        this.tag = DoomIO.readLEShort(f);
    }

    @Override
    public void pack(ByteBuffer buffer) {
        buffer.putShort(flags);
        buffer.putShort(special);
        buffer.putShort(tag);
        // buffer.putShort((short) 0XDEAD);
        // buffer.putShort((short) 0XBABE);
        // buffer.putShort((short) 0XBEEF);
    }

    @Override
    public void reset() {
        v1 = v2 = null;
        v1x = v1y = v2x = v2y = 0;
        dx = dy = 0;
        flags = special = tag = 0;
        memset(sidenum, (char) 0, sidenum.length);
        Arrays.fill(bbox, 0);
        slopetype = slopetype_t.ST_HORIZONTAL;
        frontsector = backsector = null;
        frontsectorid = backsectorid = 0;
        validcount = 0;
        specialdata = null;
        specialdataid = 0;
        soundorg = null;
        tranlump = 0;
    }
    
    /**
     * LUT, motion clipping, walls/grid element // // LineDef attributes. // /**
     * Solid, is an obstacle.
     */
    public static final int ML_BLOCKING = 1;

    /** Blocks monsters only. */
    public static final int ML_BLOCKMONSTERS = 2;

    /** Backside will not be present at all if not two sided. */
    public static final int ML_TWOSIDED = 4;

    // If a texture is pegged, the texture will have
    // the end exposed to air held constant at the
    // top or bottom of the texture (stairs or pulled
    // down things) and will move with a height change
    // of one of the neighbor sectors.
    // Unpegged textures allways have the first row of
    // the texture at the top pixel of the line for both
    // top and bottom textures (use next to windows).

    /** upper texture unpegged */
    public static final int ML_DONTPEGTOP = 8;

    /** lower texture unpegged */
    public static final int ML_DONTPEGBOTTOM = 16;

    /** In AutoMap: don't map as two sided: IT'S A SECRET! */
    public static final int ML_SECRET = 32;

    /** Sound rendering: don't let sound cross two of these. */
    public static final int ML_SOUNDBLOCK = 64;

    /** Don't draw on the automap at all. */
    public static final int ML_DONTDRAW = 128;

    /** Set if already seen, thus drawn in automap. */
    public static final int ML_MAPPED = 256;

}

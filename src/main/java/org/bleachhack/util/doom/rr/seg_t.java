package org.bleachhack.util.doom.rr;

import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;
import static org.bleachhack.util.doom.m.fixed_t.FixedMul;

import org.bleachhack.util.doom.p.Resettable;

/**
 * The LineSeg. Must be built from on-disk mapsegs_t, which are much simpler.
 * 
 * @author Maes
 */

public class seg_t
        implements Resettable {

    /** To be used as references */
    public vertex_t v1, v2;

    /** Local caching. Spares us using one extra reference level */
    public int v1x, v1y, v2x, v2y;

    /** (fixed_t) */
    public int offset;

    /** (angle_t) */
    public long angle;

    // MAES: all were single pointers.

    public side_t sidedef;

    public line_t linedef;

    /**
     * Sector references. Could be retrieved from linedef, too. backsector is
     * NULL for one sided lines
     */
    public sector_t frontsector, backsector;

    // Boom stuff
    public boolean miniseg;

    public float length;

    /** proff 11/05/2000: needed for OpenGL */
    public int iSegID;

    public void assignVertexValues() {
        this.v1x = v1.x;
        this.v1y = v1.y;
        this.v2x = v2.x;
        this.v2y = v2.y;

    }

    /**
     * R_PointOnSegSide
     * 
     * @param x
     * @param y
     * @param line
     * @return
     */
    public static int PointOnSegSide(int x, int y, seg_t line) {
        int lx;
        int ly;
        int ldx;
        int ldy;
        int dx;
        int dy;
        int left;
        int right;

        lx = line.v1x;
        ly = line.v1y;

        ldx = line.v2x - lx;
        ldy = line.v2y - ly;

        if (ldx == 0) {
            if (x <= lx)
                return (ldy > 0) ? 1 : 0;

            return (ldy < 0) ? 1 : 0;
        }
        if (ldy == 0) {
            if (y <= ly)
                return (ldx < 0) ? 1 : 0;

            return (ldx > 0) ? 1 : 0;
        }

        dx = x - lx;
        dy = y - ly;

        // Try to quickly decide by looking at sign bits.
        if (((ldy ^ ldx ^ dx ^ dy) & 0x80000000) != 0) {
            if (((ldy ^ dx) & 0x80000000) != 0) {
                // (left is negative)
                return 1;
            }
            return 0;
        }

        left = FixedMul(ldy >> FRACBITS, dx);
        right = FixedMul(dy, ldx >> FRACBITS);

        if (right < left) {
            // front side
            return 0;
        }
        // back side
        return 1;
    }

    /**
     * R_PointOnSegSide
     * 
     * @param x
     * @param y
     * @param line
     * @return
     */
    public int PointOnSegSide(int x, int y) {
        int lx;
        int ly;
        int ldx;
        int ldy;
        int dx;
        int dy;
        int left;
        int right;

        lx = this.v1x;
        ly = this.v1y;

        ldx = this.v2x - lx;
        ldy = this.v2y - ly;

        if (ldx == 0) {
            if (x <= lx)
                return (ldy > 0) ? 1 : 0;

            return (ldy < 0) ? 1 : 0;
        }
        if (ldy == 0) {
            if (y <= ly)
                return (ldx < 0) ? 1 : 0;

            return (ldx > 0) ? 1 : 0;
        }

        dx = x - lx;
        dy = y - ly;

        // Try to quickly decide by looking at sign bits.
        if (((ldy ^ ldx ^ dx ^ dy) & 0x80000000) != 0) {
            if (((ldy ^ dx) & 0x80000000) != 0) {
                // (left is negative)
                return 1;
            }
            return 0;
        }

        left = FixedMul(ldy >> FRACBITS, dx);
        right = FixedMul(dy, ldx >> FRACBITS);

        if (right < left) {
            // front side
            return 0;
        }
        // back side
        return 1;
    }

    public String toString() {
        return String
                .format(
                    "Seg %d\n\tFrontsector: %s\n\tBacksector: %s\n\tVertexes: %x %x %x %x",
                    iSegID, frontsector, backsector, v1x, v1y, v2x, v2y);
    }

    @Override
    public void reset() {
        v1 = v2 = null;
        v1x = v1y = v2x = v2y = 0;
        angle = 0;
        frontsector = backsector = null;
        iSegID = 0;
        linedef = null;
        miniseg = false;
        offset = 0;
        length = 0;
    }

}

package org.bleachhack.util.doom.rr;

import org.bleachhack.util.doom.doom.SourceCode;
import org.bleachhack.util.doom.doom.SourceCode.R_Main;
import static org.bleachhack.util.doom.doom.SourceCode.R_Main.*;
import org.bleachhack.util.doom.doom.SourceCode.fixed_t;
import org.bleachhack.util.doom.m.BBox;
import org.bleachhack.util.doom.m.ISyncLogger;
import org.bleachhack.util.doom.m.Settings;
import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;
import static org.bleachhack.util.doom.m.fixed_t.FixedMul;
import org.bleachhack.util.doom.mochadoom.Engine;
import org.bleachhack.util.doom.p.Resettable;
import static org.bleachhack.util.doom.utils.C2JUtils.eval;
import static org.bleachhack.util.doom.utils.C2JUtils.memset;

/**
 * BSP node.
 *
 * @author Maes
 */
public class node_t implements Resettable {

    public node_t() {
        bbox = new BBox[] {new BBox(), new BBox()};
        children = new int[2];
    }

    public node_t(int x, int y, int dx, int dy, BBox[] bbox,
        int[] children) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.bbox = bbox;
        this.children = children;
    }

    /**
     * Partition line.
     */
    @fixed_t public int x, y, dx, dy;

    /**
     * Bounding box for each child.
     */
    // Maes: make this into two proper bboxes?
    @fixed_t public BBox[] bbox;

    /**
     * If NF_SUBSECTOR its a subsector.
     *
     * e6y: support for extented nodes
     */
    public int[] children;

    /**
     * R_PointOnSide
     * Traverse BSP (sub) tree,
     * check point against partition plane.
     * Returns side 0 (front) or 1 (back).
     *
     * @param x fixed
     * @param y fixed
     * @param node
     */
    @R_Main.C(R_PointOnSide)
    public static int PointOnSide(@fixed_t int x, @fixed_t int y, node_t node) {
        // MAES: These are used mainly as ints, no need to use fixed_t internally.
        // fixed_t will only be used as a "pass type", but calculations will be done with ints, preferably.
        @fixed_t int dx, dy, left, right;

        if (node.dx == 0) {
            if (x <= node.x) {
                return (node.dy > 0) ? 1 : 0;
            }

            return (node.dy < 0) ? 1 : 0;
        }
        if (node.dy == 0) {
            if (y <= node.y) {
                return (node.dx < 0) ? 1 : 0;
            }

            return (node.dx > 0) ? 1 : 0;
        }

        dx = (x - node.x);
        dy = (y - node.y);

        // Try to quickly decide by looking at sign bits.
        if (((node.dy ^ node.dx ^ dx ^ dy) & 0x80000000) != 0) {
            if (((node.dy ^ dx) & 0x80000000) != 0) {
                // (left is negative)
                return 1;
            }
            return 0;
        }

        left = FixedMul(node.dy >> FRACBITS, dx);
        right = FixedMul(dy, node.dx >> FRACBITS);

        if (right < left) {
            // front side
            return 0;
        }
        // back side
        return 1;
    }

    /**
     * Since no context is needed, this is perfect for an instance method
     *
     * @param x fixed
     * @param y fixed
     * @return
     */
    @SourceCode.Exact
    @R_Main.C(R_PointOnSide)
    public int PointOnSide(@fixed_t int x, @fixed_t int y) {
        // MAES: These are used mainly as ints, no need to use fixed_t internally.
        // fixed_t will only be used as a "pass type", but calculations will be done with ints, preferably.
        @fixed_t int lDx, lDy, left, right;

        if (this.dx == 0) {
            if (x <= this.x) {
                return (this.dy > 0) ? 1 : 0;
            }

            return (this.dy < 0) ? 1 : 0;
        }
        if (this.dy == 0) {
            if (y <= this.y) {
                return (this.dx < 0) ? 1 : 0;
            }

            return (this.dx > 0) ? 1 : 0;
        }

        lDx = (x - this.x);
        lDy = (y - this.y);

        // Try to quickly decide by looking at sign bits.
        if (((this.dy ^ this.dx ^ lDx ^ lDy) & 0x80000000) != 0) {
            if (((this.dy ^ lDx) & 0x80000000) != 0) {
                // (left is negative)
                return 1;
            }
            return 0;
        }

        left = FixedMul(this.dy >> FRACBITS, lDx);
        right = FixedMul(lDy, this.dx >> FRACBITS);

        if (right < left) {
            // front side
            return 0;
        }
        // back side
        return 1;
    }

    /**
     * P_DivlineSide
     * Returns side 0 (front), 1 (back), or 2 (on).
     * Clone of divline_t's method. Same contract, but working on node_t's to avoid casts.
     * Boom-style code. Da fack.
     * [Maes]: using it leads to very different DEMO4 UD behavior.
     */
    public int DivlineSide(int x, int y) {
        int left, right;
        return (this.dx == 0) ? x == this.x ? 2 : x <= this.x ? eval(this.dy > 0) : eval(this.dy < 0) : (this.dy == 0)
            ? (OLDDEMO ? x : y) == this.y ? 2 : y <= this.y ? eval(this.dx < 0) : eval(this.dx > 0) : (this.dy == 0)
            ? y == this.y ? 2 : y <= this.y ? eval(this.dx < 0) : eval(this.dx > 0)
            : (right = ((y - this.y) >> FRACBITS) * (this.dx >> FRACBITS))
            < (left = ((x - this.x) >> FRACBITS) * (this.dy >> FRACBITS)) ? 0 : right == left ? 2 : 1;
    }

    private static final boolean OLDDEMO = Engine.getConfig().equals(Settings.line_of_sight, Settings.LOS.Vanilla);

    public int DivlineSide(int x, int y, ISyncLogger SL, boolean sync) {
        int result = DivlineSide(x, y);

        if (sync) {
            SL.sync("DLS %d\n", result);
        }

        return result;
    }

    @Override
    public void reset() {
        x = y = dx = dy = 0;

        for (int i = 0; i < 2; i++) {
            bbox[i].ClearBox();
        }

        memset(children, 0, children.length);
    }

}

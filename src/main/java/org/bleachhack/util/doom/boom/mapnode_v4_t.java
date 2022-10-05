package org.bleachhack.util.doom.boom;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.bleachhack.util.doom.wad.CacheableDoomObject;
import org.bleachhack.util.doom.wad.DoomBuffer;

/** BSP Node structure on-disk */
public class mapnode_v4_t
        implements CacheableDoomObject {

    public mapnode_v4_t() {
        this.bbox = new short[2][4];
        this.children = new int[2];
    }

    /** Partition line from (x,y) to x+dx,y+dy) */
    public short x, y, dx, dy;

    /** Bounding box for each child, clip against view frustum. */
    public short[][] bbox;

    /** If NF_SUBSECTOR its a subsector, else it's a node of another subtree. 
     *  In simpler words: if the first bit is set, strip it and use the rest
     *  as a subtree index. Else it's a node index.
     * */
    public int[] children = new int[2];

    public static final int sizeOf() {
        return (8 + 16 + 8);
    }

    @Override
    public void unpack(ByteBuffer buf)
            throws IOException {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        this.x = buf.getShort();
        this.y = buf.getShort();
        this.dx = buf.getShort();
        this.dy = buf.getShort();
        DoomBuffer.readShortArray(buf, this.bbox[0], 4);
        DoomBuffer.readShortArray(buf, this.bbox[1], 4);
        DoomBuffer.readIntArray(buf, this.children, 2);
    }

}

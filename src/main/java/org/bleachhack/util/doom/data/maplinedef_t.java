package org.bleachhack.util.doom.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.bleachhack.util.doom.wad.CacheableDoomObject;
import org.bleachhack.util.doom.wad.DoomBuffer;

/**
 * A LineDef, as used for editing, and as input to the BSP builder.
 */
public class maplinedef_t implements CacheableDoomObject{

    public maplinedef_t() {
        this.sidenum = new char[2];
    }

    public char v1;

    public char v2;

    public short flags;

    public short special;

    public short tag;

    /** sidenum[1] will be 0xFFFF if one sided */
    public char[] sidenum;

    public static int sizeOf() {
        return 14;
    }

    @Override
    public void unpack(ByteBuffer buf)
            throws IOException {
    buf.order(ByteOrder.LITTLE_ENDIAN);
    this.v1 = buf.getChar();
    this.v2 = buf.getChar();
    this.flags = buf.getShort();
    this.special = buf.getShort();
    this.tag = buf.getShort();
    DoomBuffer.readCharArray(buf, this.sidenum, 2);
    }
}

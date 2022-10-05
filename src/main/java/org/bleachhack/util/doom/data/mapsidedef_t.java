package org.bleachhack.util.doom.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.bleachhack.util.doom.wad.CacheableDoomObject;
import org.bleachhack.util.doom.wad.DoomBuffer;

/**
 * A SideDef, defining the visual appearance of a wall, by setting textures and
 * offsets. ON-DISK.
 */

public class mapsidedef_t implements CacheableDoomObject{

    public mapsidedef_t() {

    }

    public short textureoffset;

    public short rowoffset;

    // 8-char strings.
    public String toptexture;

    public String bottomtexture;

    public String midtexture;

    /** Front sector, towards viewer. */
    public short sector;

    public static int sizeOf() {
        return 30;
    }

    @Override
    public void unpack(ByteBuffer buf)
            throws IOException {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        this.textureoffset = buf.getShort();
        this.rowoffset = buf.getShort();
        this.toptexture=DoomBuffer.getNullTerminatedString(buf,8).toUpperCase();
        this.bottomtexture=DoomBuffer.getNullTerminatedString(buf,8).toUpperCase();
        this.midtexture=DoomBuffer.getNullTerminatedString(buf,8).toUpperCase();
        this.sector = buf.getShort();
        
    }
}

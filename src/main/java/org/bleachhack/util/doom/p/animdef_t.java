package org.bleachhack.util.doom.p;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.bleachhack.util.doom.wad.CacheableDoomObject;
import org.bleachhack.util.doom.wad.DoomBuffer;

/**
 * Source animation definition. Made readable for compatibility with Boom's
 * SWANTBLS system. 
 * 
 * @author velktron
 */
public class animdef_t
        implements CacheableDoomObject {

    public animdef_t() {

    }

    public animdef_t(boolean istexture, String endname, String startname,
            int speed) {
        super();
        this.istexture = istexture;
        this.endname = endname;
        this.startname = startname;
        this.speed = speed;
    }

    
    /** if false, it is a flat, and will NOT be used as a texture. Unless you
     *  use "flats on walls functionality of course. */
    public boolean istexture;

    /** The END name and START name of a texture, given in this order when reading a lump 
     *  The animation system is agnostic to the actual names of of the "in-between" 
     *  frames, it's purely pointer based, and only the start/end are constant. It only
     *  counts the actual number of existing textures during initialization time.
     * 
     */
    
    public String endname,startname;

    public int speed;

    public String toString() {
        return String.format("%s %s %s %d", istexture, startname, endname,
            speed);
    }

    @Override
    public void unpack(ByteBuffer buf)
            throws IOException {
        // Like most Doom structs...
        buf.order(ByteOrder.LITTLE_ENDIAN);
        this.istexture = (buf.get() != 0);
        this.startname = DoomBuffer.getNullTerminatedString(buf, 9);
        this.endname = DoomBuffer.getNullTerminatedString(buf, 9);
        this.speed = buf.getInt();
    }

    public static int size() {
        return 23;
    }

}

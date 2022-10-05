package org.bleachhack.util.doom.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.bleachhack.util.doom.wad.CacheableDoomObject;
import org.bleachhack.util.doom.wad.IPackableDoomObject;
import org.bleachhack.util.doom.wad.IWritableDoomObject;

/** mapthing_t ... same on disk AND in memory, wow?! */

public class mapthing_t implements CacheableDoomObject,IPackableDoomObject,IWritableDoomObject,Cloneable{
    public short x;

    public short y;

    public short angle;

    public short type;

    public short options;

    public mapthing_t() {
    }

    public mapthing_t(mapthing_t source) {
        this.copyFrom(source);
    }

    public static int sizeOf() {
        return 10;
    }

    @Override
    public void unpack(ByteBuffer buf)
            throws IOException {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        this.x = buf.getShort();
        this.y = buf.getShort();
        this.angle = buf.getShort();
        this.type = buf.getShort();
        this.options = buf.getShort();
        
    }
    
    public void copyFrom(mapthing_t source){

        this.x=source.x;
        this.y=source.y;
        this.angle=source.angle;
        this.options=source.options;
        this.type=source.type;
    }

    @Override
    public void write(DataOutputStream f)
            throws IOException {
        
        // More efficient, avoids duplicating code and
        // handles little endian better.
        iobuffer.position(0);
        iobuffer.order(ByteOrder.LITTLE_ENDIAN);
        this.pack(iobuffer);
        f.write(iobuffer.array());
        
    }

    public void pack(ByteBuffer b) {
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.putShort(x);
        b.putShort(y);
        b.putShort(angle);
        b.putShort(type);
        b.putShort(options);
    }
    
    private static ByteBuffer iobuffer=ByteBuffer.allocate(10);
}
package org.bleachhack.util.doom.boom;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.bleachhack.util.doom.wad.CacheableDoomObject;

/** fixed 32 bit gl_vert format v2.0+ (glBsp 1.91) */

public class mapglvertex_t implements CacheableDoomObject{
    public int x, y; // fixed_t

    public static int sizeOf() {
        return 8;
    }

    @Override
    public void unpack(ByteBuffer buf)
            throws IOException {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        x=buf.getInt();
        y=buf.getInt();
    }
}
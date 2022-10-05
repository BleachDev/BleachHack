package org.bleachhack.util.doom.rr;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.bleachhack.util.doom.wad.CacheableDoomObject;

/**
 * Texture definition.
 * Each texture is composed of one or more patches,
 * with patches being lumps stored in the WAD.
 * The lumps are referenced by number, and patched
 * into the rectangular texture space using origin
 * and possibly other attributes. 
 */

public class mappatch_t implements CacheableDoomObject {
     public short   originx;
     public short   originy;
     public short   patch;
     public short   stepdir;
     public short   colormap;
     
    @Override
    public void unpack(ByteBuffer buf)
            throws IOException {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        originx=buf.getShort();
        originy=buf.getShort();
        patch=buf.getShort();
        stepdir=buf.getShort();
        colormap=buf.getShort();        
    }

    public static final int size() {
        return 10;
    }
     
     
 };

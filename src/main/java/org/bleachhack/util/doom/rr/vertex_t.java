package org.bleachhack.util.doom.rr;

import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.bleachhack.util.doom.p.Resettable;

import org.bleachhack.util.doom.wad.CacheableDoomObject;

/** This is the vertex structure used IN MEMORY with fixed-point arithmetic.
 *  It's DIFFERENT than the one used on disk, which has 16-bit signed shorts.
 *  However, it must be parsed. 
 *
 */

public class vertex_t  implements CacheableDoomObject, Resettable{

    public vertex_t(){
        
    }
    /** treat as (fixed_t) */
    public  int x,y;
    
    
    /** Notice how we auto-expand to fixed_t */
    @Override
    public void unpack(ByteBuffer buf)
            throws IOException {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        this.x=buf.getShort()<<FRACBITS;
        this.y=buf.getShort()<<FRACBITS;
        
    }

    @Override
    public void reset() {
        x=0; y=0;        
    }


    public static int sizeOf() {
        return 4;
    }
    
}
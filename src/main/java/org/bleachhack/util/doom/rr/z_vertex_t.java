package org.bleachhack.util.doom.rr;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class z_vertex_t
        extends vertex_t {

    public z_vertex_t(){
        super();
    }
    
    /** Notice how we auto-expand to fixed_t */
    @Override
    public void unpack(ByteBuffer buf)
            throws IOException {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        this.x=buf.getInt();
        this.y=buf.getInt();
        
    }
    
    public final static int sizeOf() {
        return 8;
    }
    
}

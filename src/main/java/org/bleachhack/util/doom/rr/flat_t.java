package org.bleachhack.util.doom.rr;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.bleachhack.util.doom.wad.CacheableDoomObject;

public class flat_t
        implements CacheableDoomObject {

    public static final int FLAT_SIZE=4096;
    
    public byte[] data;

    public flat_t(){
        this.data=new byte[FLAT_SIZE];
    }

    public flat_t(int size){
        this.data=new byte[size];
    }

    
    @Override
    public void unpack(ByteBuffer buf)
            throws IOException {
        
            //buf.get(this.data);
            this.data=buf.array();

    }

}

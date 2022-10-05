package org.bleachhack.util.doom.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.bleachhack.util.doom.wad.CacheableDoomObject;
import org.bleachhack.util.doom.wad.DoomBuffer;

/** Sector definition, from editing. */ 
public class mapsector_t implements CacheableDoomObject {
    
        public mapsector_t(){

        }
    
      public short     floorheight;
      public  short     ceilingheight;
      public  String floorpic;
      public  String        ceilingpic;
      public  short     lightlevel;
      public  short     special;
      public  short     tag;
    @Override
    public void unpack(ByteBuffer buf)
            throws IOException {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        this.floorheight = buf.getShort();
        this.ceilingheight = buf.getShort();
        this.floorpic=DoomBuffer.getNullTerminatedString(buf,8).toUpperCase();
        this.ceilingpic=DoomBuffer.getNullTerminatedString(buf,8).toUpperCase();
        this.lightlevel= buf.getShort();
        this.special= buf.getShort();
        this.tag= buf.getShort();
    }
    
    public static int sizeOf() {
        return 26;
    }
      
}

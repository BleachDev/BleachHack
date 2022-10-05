package org.bleachhack.util.doom.p;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.bleachhack.util.doom.rr.SectorAction;
import org.bleachhack.util.doom.wad.DoomIO;
import org.bleachhack.util.doom.wad.IReadableDoomObject;

public class vldoor_t extends SectorAction implements IReadableDoomObject{
        
        public vldoor_e    type;
        /** fixed_t */
        public int topheight, speed;

        /** 1 = up, 0 = waiting at top, -1 = down */
        public int direction;
        
        /** tics to wait at the top */
        public int             topwait;
        
        /**(keep in case a door going down is reset)
           when it reaches 0, start going down */        
        public int             topcountdown;
        
        @Override
        public void read(DataInputStream f) throws IOException{

            super.read(f); // Call thinker reader first            
            type=vldoor_e.values()[DoomIO.readLEInt(f)];
            super.sectorid=DoomIO.readLEInt(f); // Sector index (or pointer?)
            topheight=DoomIO.readLEInt(f);
            speed=DoomIO.readLEInt(f);
            direction=DoomIO.readLEInt(f);
            topwait=DoomIO.readLEInt(f);
            topcountdown=DoomIO.readLEInt(f);            
            }
        
        @Override
        public void pack(ByteBuffer b) throws IOException{
            super.pack(b); //12            
            b.putInt(type.ordinal()); // 16
            b.putInt(super.sectorid); // 20
            b.putInt(topheight); // 24
            b.putInt(speed); //28
            b.putInt(direction); // 32
            b.putInt(topwait); //36
            b.putInt(topcountdown); //40
        }
        
    }
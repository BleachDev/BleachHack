package org.bleachhack.util.doom.p;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.bleachhack.util.doom.rr.SectorAction;
import org.bleachhack.util.doom.wad.DoomIO;
import org.bleachhack.util.doom.wad.IReadableDoomObject;

public class floormove_t extends SectorAction implements IReadableDoomObject{
    
    public floormove_t(){
        // MAES HACK: floors are implied to be at least of "lowerFloor" type
        // unless set otherwise, due to implicit zero-enum value.
        this.type=floor_e.lowerFloor;
    }
    
    public floor_e type;
    public boolean crush;

    public int     direction;
    public int     newspecial;
    public short   texture;
    /** fixed_t */
    public int floordestheight;
    /** fixed_t */
    public int speed;

    @Override
    public void read(DataInputStream f) throws IOException{

        super.read(f); // Call thinker reader first            
        type=floor_e.values()[DoomIO.readLEInt(f)];
        crush=DoomIO.readIntBoolean(f);
        super.sectorid=DoomIO.readLEInt(f); // Sector index (or pointer?)
        direction=DoomIO.readLEInt(f);
        newspecial=DoomIO.readLEInt(f);
        texture=DoomIO.readLEShort(f);
        floordestheight=DoomIO.readLEInt(f);
        speed=DoomIO.readLEInt(f);        
        }
   
    @Override
    public void pack(ByteBuffer b) throws IOException{
        super.pack(b); //12            
        b.putInt(type.ordinal()); // 16
        b.putInt(crush?1:0); //20
        b.putInt(super.sectorid); // 24
        b.putInt(direction); // 28
        b.putInt(newspecial); // 32
        b.putShort(texture); // 34
        b.putInt(floordestheight); // 38
        b.putInt(speed); // 42
    }
    
}
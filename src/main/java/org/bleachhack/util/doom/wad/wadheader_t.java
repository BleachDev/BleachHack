package org.bleachhack.util.doom.wad;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class wadheader_t implements IReadableDoomObject, IWritableDoomObject {
    public String type;
    public int numentries;
    public int tablepos;
    
    public boolean big_endian=false;
    
    public void read(DataInputStream f) throws IOException{

        type=DoomIO.readNullTerminatedString(f,4);
        
        if (!big_endian){
        numentries=(int) DoomIO.readUnsignedLEInt(f);
        tablepos=(int) DoomIO.readUnsignedLEInt(f);

        } else {
            numentries=f.readInt();
            tablepos=f.readInt();
        }
        
    }

    public static int sizeof(){
        return 16;
    }

    @Override
    public void write(DataOutputStream dos)
            throws IOException {
        DoomIO.writeString(dos, type, 4);
        
        if (!big_endian){
            DoomIO.writeLEInt(dos, (int) numentries);
            DoomIO.writeLEInt(dos, (int) tablepos);
        } else {
                dos.writeInt((int) numentries);
                dos.writeInt((int) tablepos);
        }
        
        
    }

}

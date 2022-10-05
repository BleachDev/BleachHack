package org.bleachhack.util.doom.wad;

import java.io.DataInputStream;
import java.io.IOException;

public class wadinfo_t implements IReadableDoomObject{
        // Should be "IWAD" or "PWAD".
        String       identification;      
        long         numlumps;
        long         infotableofs;
             
        /** Reads the wadinfo_t from the file.*/
        public void read(DataInputStream f) throws IOException {
            identification = DoomIO.readString(f,4);
            numlumps=DoomIO.readUnsignedLEInt(f);
            infotableofs=DoomIO.readUnsignedLEInt(f);
        }
        
    }
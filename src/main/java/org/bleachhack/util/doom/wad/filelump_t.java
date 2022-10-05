package org.bleachhack.util.doom.wad;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** filelumps are on-disk structures. lumpinfos are almost the same, but are memory only.
 * 
 * @author Maes
 *
 */

public class filelump_t  implements IReadableDoomObject, IWritableDoomObject {
    public long         filepos;
    public long         size; // Is INT 32-bit in file!
    public String        name; // Whatever appears inside the wadfile
    public String actualname; // Sanitized name, e.g. after compression markers
        
    public boolean big_endian=false; // E.g. Jaguar
    public boolean compressed=false; // Compressed lump
    
    public void read(DataInputStream f) throws IOException{
        // MAES: Byte Buffers actually make it convenient changing byte order on-the-fly.
        // But RandomAccessFiles (and inputsteams) don't :-S

        if (!big_endian){
        filepos=DoomIO.readUnsignedLEInt(f);
        size=DoomIO.readUnsignedLEInt(f);

        } else {
            filepos=f.readInt();
            size=f.readInt();

        }
        
        // Names used in the reading subsystem should be upper case,
        // but check for compressed status first
        name=DoomIO.readNullTerminatedString(f,8);
        
       
        char[] stuff= name.toCharArray();
        
        // It's a compressed lump
        if (stuff[0] > 0x7F) {
            this.compressed=true;
            stuff[0]&=0x7F; 
        }
        
        actualname=new String(stuff).toUpperCase();
        
        
    }

    public static int sizeof(){
        return (4+4+8);
    }

    @Override
    public void write(DataOutputStream dos)
            throws IOException {
        if (!big_endian){
            DoomIO.writeLEInt(dos, (int) filepos);
            DoomIO.writeLEInt(dos, (int) size);
        } else {
                dos.writeInt((int) filepos);
                dos.writeInt((int) size);
        }
        DoomIO.writeString(dos, name, 8);
        
    }
    
}
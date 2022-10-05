package org.bleachhack.util.doom.demo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.bleachhack.util.doom.doom.ticcmd_t;

import org.bleachhack.util.doom.utils.C2JUtils;
import org.bleachhack.util.doom.wad.CacheableDoomObject;
import org.bleachhack.util.doom.wad.IWritableDoomObject;

/** A more lightweight version of ticcmd_t, which contains
 *  too much crap and redundant data. In order to keep demo
 *  loading and recording easier, this class contains only the
 *  necessary stuff to read/write from/to disk during VANILLA
 *  demos. It can be converted from/to ticcmd_t, if needed.
 * 
 * @author admin
 *
 */
public class VanillaTiccmd implements CacheableDoomObject, IDemoTicCmd,IWritableDoomObject{

    /** *2048 for move */
    public byte forwardmove;

    /** *2048 for move */
    public byte sidemove;

    /** <<16 for angle delta */
    public byte angleturn;
    public byte buttons; 

    /** Special note: the only occasion where we'd ever be interested
     *  in reading ticcmd_t's from a lump is when playing back demos.
     *  Therefore, we use this specialized reading method which does NOT,
     *  I repeat, DOES NOT set all fields and some are read differently.
     *  NOT 1:1 intercangeable with the Datagram methods!  
     * 
     */
    @Override
    public void unpack(ByteBuffer f)
            throws IOException {
        
        // MAES: the original ID code for reference.
        // demo_p++ is a pointer inside a raw byte buffer.
        
     //cmd->forwardmove = ((signed char)*demo_p++); 
     //cmd->sidemove = ((signed char)*demo_p++); 
     //cmd->angleturn = ((unsigned char)*demo_p++)<<8; 
     //cmd->buttons = (unsigned char)*demo_p++; 
        
        forwardmove=f.get();
        sidemove=   f.get();        
        // Even if they use the "unsigned char" syntax, angleturn is signed.
        angleturn=f.get();
        buttons=f.get();
        
    }
    
    /** Ditto, we only pack some of the fields.
     * 
     * @param f
     * @throws IOException
     */
    public void pack(ByteBuffer f)
            throws IOException {
        
        f.put(forwardmove);
        f.put(sidemove);
        f.put(angleturn);
        f.put(buttons);        
    }
    
    private static StringBuilder sb=new StringBuilder();
    
    public String toString(){
        sb.setLength(0);
        sb.append(" forwardmove ");
        sb.append(this.forwardmove); 
        sb.append(" sidemove ");
        sb.append(this.sidemove); 
        sb.append(" angleturn ");
        sb.append(this.angleturn); 
        sb.append(" buttons ");
        sb.append(Integer.toHexString(this.buttons));
        return sb.toString();
    }

    @Override
    public void decode(ticcmd_t dest) {
        // Decode
        dest.forwardmove=this.forwardmove;
        dest.sidemove=this.sidemove;
        dest.angleturn=(short) (this.angleturn<<8);
        dest.buttons=(char) (C2JUtils.toUnsignedByte(this.buttons));
        }

    @Override
    public void encode(ticcmd_t source) {
        // Note: we can get away with a simple copy because
        // Demoticcmds have already been "decoded".
        this.forwardmove=source.forwardmove;
        this.sidemove=source.sidemove;
        // OUCH!!! NASTY PRECISION REDUCTION... but it's the
        // One True Vanilla way.
        this.angleturn=(byte) (source.angleturn>>>8);
        this.buttons=(byte) (source.buttons&0x00FF);
    }

    @Override
    public void write(DataOutputStream f)
            throws IOException {
        f.writeByte(forwardmove);
        f.writeByte(sidemove);
        f.writeByte(angleturn);
        f.writeByte(buttons); 
    }

}

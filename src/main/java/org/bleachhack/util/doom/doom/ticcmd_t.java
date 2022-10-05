package org.bleachhack.util.doom.doom;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.bleachhack.util.doom.wad.CacheableDoomObject;
import org.bleachhack.util.doom.wad.IReadableDoomObject;

public class ticcmd_t implements IDatagramSerializable, IReadableDoomObject,CacheableDoomObject{
    
    // The length datagrams are supposed to have, for full compatibility.
    
    public static final int TICCMDLEN=8;
    
    // Initializes ticcmd buffer, too.
    
    public ticcmd_t(){
        this.buffer=new byte[TICCMDLEN];
    }
    
    /** *2048 for move */
    public byte forwardmove;

    /** *2048 for move */
    public byte sidemove;

    /** <<16 for angle delta */
    public short angleturn;

    /** checks for net game */
    public short consistancy;
    
    /** MAES: these are unsigned bytes :-( 
     *  However over networks, if we wish for vanilla compatibility,
     *  these must be reduced to 8-bit "chars"
     * */

    public char chatchar, buttons; 
    
    /** HERETIC look/fly up/down/centering */    
    public char		lookfly; 
    
    // TODO: will be ignored during vanilla demos. Consider using specialized
    // per-demotype readers instead of Cacheable/Unpackage.

    /** replaces G_CmdChecksum (ticcmd_t cmd) */

    /////////////////////////////////////////////
    
    // For datagram serialization
    private byte[] buffer;
    
    public int getChecksum(ticcmd_t cmd) {
        int sum = 0;
        sum += forwardmove;
        sum += sidemove;
        sum += angleturn;
        sum += consistancy;
        sum += chatchar;
        sum += buttons;
        return sum;
        }

    /** because Cloneable is bullshit */
    
     public void copyTo(ticcmd_t dest){
         dest.forwardmove = forwardmove;
         dest.sidemove = sidemove;
         dest.angleturn= angleturn;
         dest.consistancy= consistancy;
         dest.chatchar= chatchar;
         dest.buttons= buttons;
         dest.lookfly=lookfly;
     }
     
     private static StringBuilder sb=new StringBuilder();
     
     public String toString(){
         sb.setLength(0);
         sb.append(" forwardmove ");
         sb.append(Integer.toHexString(this.forwardmove)); 
         sb.append(" sidemove ");
         sb.append(Integer.toHexString(this.sidemove)); 
         sb.append(" angleturn ");
         sb.append(Integer.toHexString(this.angleturn)); 
         sb.append(" consistancy ");
         sb.append(Integer.toHexString(this.consistancy));      
         sb.append(" chatchar ");
         sb.append(chatchar);         
        sb.append(" buttons ");
         sb.append(Integer.toHexString(this.buttons));
         return sb.toString();
     }

    @Override
    public byte[] pack() {
        buffer[0]=forwardmove;
        buffer[1]=sidemove;        
        buffer[2]=(byte) (angleturn>>>8);
        buffer[3]=(byte) (angleturn&0x00FF);
        buffer[4]=(byte) (consistancy>>>8);
        buffer[5]=(byte) (consistancy&0x00FF);

        // We only send 8 bytes because the original length was 8 bytes.
        buffer[6]=(byte) (chatchar&0x00FF);
        buffer[7]=(byte) (buttons&0x00FF);
        
        return buffer;
    }

    @Override
    public void pack(byte[] buf, int offset) {
        buf[0+offset]=forwardmove;
        buf[1+offset]=sidemove;        
        buf[2+offset]=(byte) (angleturn>>>8);
        buf[3+offset]=(byte) (angleturn&0x00FF);
        buf[4+offset]=(byte) (consistancy>>>8);
        buf[5+offset]=(byte) (consistancy&0x00FF);

        // We only send 8 bytes because the original length was 8 bytes.
        buf[6+offset]=(byte) (chatchar&0x00FF);
        buf[7+offset]=(byte) (buttons&0x00FF);
        
    }

    @Override
    public void unpack(byte[] buf) {
        unpack(buf,0);        
        }

    @Override
    public void unpack(byte[] buf, int offset) {
        forwardmove=buf[0+offset];
        sidemove=   buf[1+offset];        
        angleturn=(short)(buf[2+offset]<<8|buf[3+offset]);
        consistancy=(short)(buf[4+offset]<<8|buf[5+offset]);
        // We blow these up to full chars.
        chatchar=(char)(0x00FF& buf[6+offset]);
        buttons=(char) (0x00FF& buf[7+offset]);
        
    }

    @Override
    public byte[] cached() {
        return this.buffer;
    }

    @Override
    public void read(DataInputStream f)
            throws IOException {
    	iobuffer.position(0);
    	iobuffer.order(ByteOrder.LITTLE_ENDIAN);
    	f.read(iobuffer.array());
    	unpack(iobuffer);
    }
    
    /** This is useful only when loading/saving players from savegames.
     *  It's NOT interchangeable with datagram methods, because it
     *  does not use the network byte order.
     */

    @Override
    public void unpack(ByteBuffer f)
            throws IOException {
        f.order(ByteOrder.LITTLE_ENDIAN);
        forwardmove=f.get();
        sidemove=   f.get();        
        // Even if they use the "unsigned char" syntax, angleturn is signed.
        angleturn=f.getShort();
        consistancy=f.getShort();
        // We blow these up to full chars.
        chatchar=(char) f.get();
        buttons=(char) f.get();
        
    }
    
    /** Ditto, we only pack some of the fields.
     * 
     * @param f
     * @throws IOException
     */
    public void pack(ByteBuffer f)
            throws IOException {
        f.order(ByteOrder.LITTLE_ENDIAN);
        f.put(forwardmove);
        f.put(sidemove);        
        // LE order on disk for vanilla compatibility.
        f.putShort(angleturn);
        f.putShort(consistancy);
        // We crimp these to bytes :-(
        f.put((byte) chatchar);
        f.put((byte) buttons);     
    }
    
    private static ByteBuffer iobuffer=ByteBuffer.allocate(8);
    
};

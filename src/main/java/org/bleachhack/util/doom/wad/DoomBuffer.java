package org.bleachhack.util.doom.wad;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Very similar to the concept of ReadableDoomObjects
 *  but made to work with byte buffers instead. 
 *  
 *  This is normally NOT used to pass data around: I am 
 *  using it as a workaround to store raw byte buffers
 *  into a "CacheableDoomObject" array, as Java
 *  doesn't seem to like storing both ByteBuffers and
 *  CacheableDoomObjects in the same array. WTF...
 * 
 * @author admin
 *
 */

public class DoomBuffer implements CacheableDoomObject  {

    public DoomBuffer(){

    }
    
    public DoomBuffer(ByteBuffer b){
        this.buffer=b;
    }

    private ByteBuffer buffer;

    public static void readObjectArray(ByteBuffer buf,CacheableDoomObject[] s,int len) throws IOException {
        if ((s == null) || (len == 0)) {
            return;
        }

        for (int i = 0; i < Math.min(len, s.length); i++) {
            s[i].unpack(buf);
        }
    }
    
    public static void readIntArray(ByteBuffer buf, int[] s, int len) throws IOException {
        if ((s == null) || (len == 0)) {
            return;
        }

        for (int i = 0; i < Math.min(len, s.length); i++) {
            s[i] = buf.getInt();
        }
    }
    
    public static void putIntArray(ByteBuffer buf,int[] s,int len,ByteOrder bo) throws IOException {
        buf.order(bo);
        
        if ((s==null)||(len==0)) return;
        
        for (int i=0;i<Math.min(len,s.length);i++){           
            buf.putInt(s[i]);
        }
    }
    
    public static void putBooleanIntArray(ByteBuffer buf,boolean[] s,int len,ByteOrder bo) throws IOException {
        buf.order(bo);
        
        if ((s==null)||(len==0)) return;
        
        for (int i=0;i<Math.min(len,s.length);i++){           
            buf.putInt(s[i]?1:0);
        }
    }
    
    public static void putBooleanInt(ByteBuffer buf,boolean s,ByteOrder bo) throws IOException {
        buf.order(bo);        
            buf.putInt(s?1:0);
    }
    
    public static void readCharArray(ByteBuffer buf,char[] s,int len) throws IOException {

        if ((s==null)||(len==0)) return;
        
        for (int i=0;i<Math.min(len,s.length);i++){           
            s[i]=buf.getChar();
        }
    }
    
    public static void readShortArray(ByteBuffer buf,short[] s,int len) throws IOException {

        if ((s==null)||(len==0)) return;
        
        for (int i=0;i<Math.min(len,s.length);i++){           
            s[i]=buf.getShort();
        }
    }

    public void readShortArray(short[] s,int len) throws IOException {

        if ((s==null)||(len==0)) return;
        
        for (int i=0;i<Math.min(len,s.length);i++){           
            s[i]=this.buffer.getShort();
            
        }
    }
    
    public void readCharArray(char[] s,int len) throws IOException {

        if ((s==null)||(len==0)) return;
        
        for (int i=0;i<Math.min(len,s.length);i++){           
            s[i]=this.buffer.getChar();
            
        }
    }
    
    public void readCharArray(int[] s,int len) throws IOException {

        if ((s==null)||(len==0)) return;
        
        for (int i=0;i<Math.min(len,s.length);i++){           
            s[i]=this.buffer.getChar();
            
        }
    }
    
    
    /** Reads a length specified string from a buffer. */
    public static String readString(ByteBuffer buf) throws IOException {
        int len = buf.getInt();

        if (len == -1)
            return null;

        if (len == 0)
            return "";

        byte bb[] = new byte[len];

        buf.get(bb, 0, len);

        return new String(bb, 0, len);
    }

 /** MAES: Reads a specified number of bytes from a buffer into a new String.
  *  With many lengths being implicit, we need to actually take the loader by the hand.
  * 
  * @param buf
  * @param len
  * @return
  * @throws IOException
  */
    
    public static String getString(ByteBuffer buf, int len) throws IOException {

        if (len == -1)
            return null;

        if (len == 0)
            return "";

        byte bb[] = new byte[len];

        buf.get(bb, 0, len);

        return new String(bb, 0, len);
    }
    
    /** MAES: Reads a maximum specified number of bytes from a buffer into a new String,
     * considering the bytes as representing a null-terminated, C-style string.
     * 
     * @param buf
     * @param len
     * @return
     * @throws IOException
     */
       
       public static String getNullTerminatedString(ByteBuffer buf, int len) throws IOException {

           if (len == -1)
               return null;

           if (len == 0)
               return "";

           byte bb[] = new byte[len];
           
           buf.get(bb, 0, len);
           // Detect null-termination.
           for (int i=0;i<len;i++){
               if (bb[i]==0x00){
                   len=i;
                   break;
               }
           }
           
           return new String(bb, 0, len);
       }
    
    /** MAES: Reads a specified number of bytes from a buffer into a new String.
     *  With many lengths being implicit, we need to actually take the loader by the hand.
     * 
     * @param buf
     * @param len
     * @return
     * @throws IOException
     */
       
       public static char[] getCharSeq(ByteBuffer buf, int len) throws IOException {
           return (getString(buf,len)).toCharArray();
       }

    

    @Override
    public void unpack(ByteBuffer buf)
            throws IOException {
        this.buffer=buf;
        
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
    
    public void setOrder(ByteOrder bo){
        this.buffer.order(bo);
    }

    public void rewind() {
        this.buffer.rewind();
    }

    public static int getBEInt(byte b3,byte b2, byte b1,byte b0) {        
        return (b3<<24|b2<<16|b1<<8|b0);
    }
    
    public static int getBEInt(byte[] buf,int offset) {        
        return (buf[offset]<<24|buf[offset+1]<<16|buf[offset+2]<<8|buf[offset+3]);
    }
    
    public static int getBEInt(byte[] buf) {        
        return (buf[0]<<24|buf[1]<<16|buf[2]<<8|buf[3]);
    }
    
    public static int getLEInt(byte b0,byte b1, byte b2,byte b3) {        
        return (b3<<24|b2<<16|b1<<8|b0);
    }
    
    public static int getLEInt(byte[] buf) {        
        return (buf[3]<<24|buf[2]<<16|buf[1]<<24|buf[0]);
    }
    
    public static short getBEShort(byte[] buf) {        
        return (short) (buf[0]<<8|buf[1]);
    }
    
    public static short getLEShort(byte[] buf) {        
        return (short) (buf[0]<<8|buf[1]);
    }
    
}

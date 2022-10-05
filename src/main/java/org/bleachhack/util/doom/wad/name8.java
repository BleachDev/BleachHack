package org.bleachhack.util.doom.wad;

public class name8 {
    private byte[]  s;
    static byte[] ss=new byte[9];
    public int[]    x;
    public long hash;
    
    public name8(String name){
        s=new byte[9];
        x=new int[2];
        // in case the name was a full 8 chars
        this.s[8] = 0;
        
        byte[] tmp=name.getBytes();
        System.arraycopy(tmp, 0, this.s, 0, Math.min(8,tmp.length));
        this.x[0]=byteArrayToInt(s,0);
        this.x[1]=byteArrayToInt(s,4);
        this.hash=byteArrayToLong(s,0);
    }
    
    /** Returns a 64-bit number that maps directly to the ASCII
     *  8-bit representation of a fixed-length 8 char string.
     *  It's for all effects and purposes a unique 64-bit hash, and can be used to
     *  speed up comparisons.
     *  
     * @param name
     * @return
     */
    
    public static long getLongHash(String name){
        // in case the name was a full 8 chars
        for (int i=0;i<ss.length;i++){
            ss[i]=0;
        }
        
        byte[] tmp=name.getBytes();
        // We must effectively limit hashes to 31 bits to be able to use them.
        System.arraycopy(tmp, 0, ss, 0, Math.min(8,tmp.length));
        return byteArrayToLong(ss,0);
    }
    
    public static int getIntName(String name){
        // in case the name was a full 8 chars
        for (int i=0;i<ss.length;i++){
            ss[i]=0;
        }
        
        byte[] tmp=name.getBytes();
        System.arraycopy(tmp, 0, ss, 0, Math.min(4,tmp.length));
        return byteArrayToInt(ss,0);
    }
    
    public static int byteArrayToInt(byte[] src, int ofs){
        return (src[ofs]<<24)|(src[ofs+1]<<16)|(src[ofs+2]<<8)|src[ofs+3];
    }
    
    public static long byteArrayToLong(byte[] src, int ofs){
        return (((long)byteArrayToInt(src, 0)<<32)|byteArrayToInt(src, 4));
    }
    
    
    /** Probably has horrible performance...
     * 
     * @param src
     * @param ofs
     * @return
     */
    
    public static int stringToInt(String src, int ofs){
         byte[]  s=new byte[9];
         for (int i=0;i<src.length();i++){
             s[i]=(byte) src.charAt(i);
         }
        
            return (s[ofs]<<24)|(s[ofs+1]<<16)|(s[ofs+2]<<8)|s[ofs+3];
    }
        
    }

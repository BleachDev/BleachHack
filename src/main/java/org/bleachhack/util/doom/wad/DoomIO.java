package org.bleachhack.util.doom.wad;

/*
Copyright (C) 1997-2001 Id Software, Inc.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

//Created on 24.07.2004 by RST.

//$Id: DoomIO.java,v 1.3 2013/06/03 10:30:20 velktron Exp $

import java.io.*;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.List;

import org.bleachhack.util.doom.m.Swap;

/**
* An extension of RandomAccessFile, which handles readString/WriteString specially 
* and offers several Doom related (and cross-OS) helper functions for reading/writing
* arrays of multiple objects or fixed-length strings from/to disk.
* 
* TO DEVELOPERS: this is the preferrered method of I/O for anything implemented.
* In addition, Doomfiles can be passed to objects implementing the IReadableDoomObject 
* and IWritableDoomObject interfaces, which will "autoread" or "autowrite" themselves
* to the implied stream.
* 
* TODO: in the light of greater future portabililty and compatibility in certain
* environments, PERHAPS this should have been implemented using Streams. Perhaps
* it's possible to change the underlying implementation without (?) changing too
* much of the exposed interface, but it's not a priority for me right now. 
* 
*/
public class DoomIO  {

	private DoomIO(){
		
	}

   /** Writes a Vector to a RandomAccessFile. */
   public static void writeVector(DataOutputStream dos,float v[]) throws IOException {
       for (int n = 0; n < 3; n++)
           dos.writeFloat(v[n]);
   }

   /** Writes a Vector to a RandomAccessFile. */
   public static float[] readVector(DataInputStream dis) throws IOException {
       float res[] = { 0, 0, 0 };
       for (int n = 0; n < 3; n++)
           res[n] = dis.readFloat();

       return res;
   }

   /** Reads a length specified string from a file. */
   public static final String readString(DataInputStream dis) throws IOException {
       int len = dis.readInt();

       if (len == -1)
           return null;

       if (len == 0)
           return "";

       byte bb[] = new byte[len];

       dis.read(bb, 0, len);

       return new String(bb, 0, len,Charset.forName("ISO-8859-1"));
   }

/** MAES: Reads a specified number of bytes from a file into a new String.
 *  With many lengths being implicit, we need to actually take the loader by the hand.
 *  
 * @param len
 * @return
 * @throws IOException
 */
   
   public final static String readString(DataInputStream dis,int len) throws IOException {

       if (len == -1)
           return null;

       if (len == 0)
           return "";

       byte bb[] = new byte[len];

       dis.read(bb, 0, len);

       return new String(bb, 0, len);
   }
   
   public static String readString(InputStream f,int len) throws IOException {

       if (len == -1)
           return null;

       if (len == 0)
           return "";

       byte bb[] = new byte[len];

       f.read(bb, 0, len);

       return new String(bb, 0, len,Charset.forName("ISO-8859-1"));
   }
   
   /** MAES: Reads a specified number of bytes from a file into a new, NULL TERMINATED String.
    *  With many lengths being implicit, we need to actually take the loader by the hand.
    *  
    * @param len
    * @return
    * @throws IOException
    */
   
      public static final String readNullTerminatedString(InputStream dis,int len) throws IOException {

          if (len == -1)
              return null;

          if (len == 0)
              return "";

          byte bb[] = new byte[len];
          int terminator=len;

          dis.read(bb, 0, len);
          
          for (int i=0;i<bb.length;i++){
              if (bb[i]==0) {
                  terminator=i;
                  break; // stop on first null
              }
              
          }
          
          // This is the One True Encoding for Doom.
          return new String(bb, 0, terminator,Charset.forName("ISO-8859-1"));
      }
   
   /** MAES: Reads multiple strings with a specified number of bytes from a file.
    * If the array is not large enough, only partial reads will occur.
    *  
    * @param len
    * @return
    * @throws IOException
    */
      
      public static final String[] readMultipleFixedLengthStrings(DataInputStream dis,String[] dest, int num, int len) throws IOException {

    	  // Some sanity checks...
          if (num<=0 || len < 0)
              return null;

          if (len == 0) {
        	  for (int i=0;i<dest.length;i++){
        		  dest[i]=new String("");
        	  }
        	  return dest;
          }        	  
          
          for (int i=0;i<num;i++){
        	  dest[i]=readString(dis,len);
          }
          return dest;
      }

   
   /** Writes a length specified string (Pascal style) to a file. 
    * 
    * */
   public static void writeString(DataOutputStream dos,String s) {
       try {
       if (s == null) {
           dos.writeInt(-1);
           return;
       }

       dos.writeInt(s.length());
       if (s.length() != 0)
           dos.writeBytes(s);
       } catch (Exception e){
           System.err.println("writeString "+s+" to DoomFile failed!");
       }
   }

   /** Writes a String with a specified len to a file.
    *  This is useful for fixed-size String fields in 
    *  files. Any leftover space will be filled with 0x00s. 
    * 
    * @param s
    * @param len
    * @throws IOException
    */
    
   public static void writeString(DataOutputStream dos,String s,int len) throws IOException {

       if (s==null) return;
       
       if (s.length() != 0){
           byte[] dest=s.getBytes("ISO-8859-1");
           dos.write(dest,0,Math.min(len,dest.length));
           // Fill in with 0s if something's left.
           if (dest.length<len){
               for (int i=0;i<len-dest.length;i++){
                   dos.write((byte)0x00);
               }
           }
       }
   }

   public static void readObjectArray(DataInputStream dis,IReadableDoomObject[] s,int len) throws IOException {

       if ((s==null)||(len==0)) return;
       
       for (int i=0;i<Math.min(len,s.length);i++){           
           s[i].read(dis);
       }
   }

   public static void readObjectArrayWithReflection(DataInputStream dis,IReadableDoomObject[] s,int len) throws Exception {

       if (len==0) return;
       Class<?> c=s.getClass().getComponentType();
       
       for (int i=0;i<Math.min(len,s.length);i++){
           if (s[i]==null) s[i]=(IReadableDoomObject) c.newInstance();
           s[i].read(dis);
       }
   }
   
   public static void readObjectArray(DataInputStream dis,IReadableDoomObject[] s,int len, Class<?> c) throws Exception {

       if ((s==null)||(len==0)) return;
       
       for (int i=0;i<Math.min(len,s.length);i++){
           if (s[i]==null) {
               s[i]=(IReadableDoomObject) c.newInstance();
           }
           s[i].read(dis);
       }
   }
   
   public static final void readIntArray(DataInputStream dis,int[] s,int len, ByteOrder bo) throws IOException {

       if ((s==null)||(len==0)) return;
       
       for (int i=0;i<Math.min(len,s.length);i++){           
           s[i]=dis.readInt();
           if (bo==ByteOrder.LITTLE_ENDIAN){
               s[i]=Swap.LONG(s[i]);
           }
       }
   }
   
   public static final void readShortArray(DataInputStream dis,short[] s,int len, ByteOrder bo) throws IOException {

       if ((s==null)||(len==0)) return;
       
       for (int i=0;i<Math.min(len,s.length);i++){           
           s[i]=dis.readShort();
           if (bo==ByteOrder.LITTLE_ENDIAN){
               s[i]=Swap.SHORT(s[i]);
           }
       }
   }
   
   public static final void readIntArray(DataInputStream dis,int[] s,ByteOrder bo) throws IOException {
       readIntArray(dis,s,s.length,bo);
   }
   
   public static final void readShortArray(DataInputStream dis,short[] s,ByteOrder bo) throws IOException {
       readShortArray(dis,s,s.length,bo);
   }
   
   public static void readBooleanArray(DataInputStream dis,boolean[] s,int len) throws IOException {

       if ((s==null)||(len==0)) return;
       
       for (int i=0;i<Math.min(len,s.length);i++){
           s[i]=dis.readBoolean();
           }
   }
   
   
   
   /** Reads an array of "int booleans" into an array or
    * proper booleans. 4 bytes per boolean are used!
    * 
    * @param s
    * @param len
    * @throws IOException
    */
   
   public final static void readBooleanIntArray(DataInputStream dis,boolean[] s,int len) throws IOException {

       if ((s==null)||(len==0)) return;
       
       for (int i=0;i<Math.min(len,s.length);i++){
           s[i]=readIntBoolean(dis);
           }
   }
   
   public static final void readBooleanIntArray(DataInputStream dis,boolean[] s) throws IOException {
       readBooleanIntArray(dis,s,s.length);
   }
   
   public static final void writeBoolean(DataOutputStream dos,boolean[] s,int len) throws IOException {

       if ((s==null)||(len==0)) return;
       
       for (int i=0;i<Math.min(len,s.length);i++){
           dos.writeBoolean(s[i]);
           }
   }
   
   public static final void writeObjectArray(DataOutputStream dos,IWritableDoomObject[] s,int len) throws IOException {

       if ((s==null)||(len==0)) return;
       
       for (int i=0;i<Math.min(len,s.length);i++){           
           s[i].write(dos);
       }
   }
   
   public static final void writeListOfObjects(DataOutputStream dos,List<IWritableDoomObject> s,int len) throws IOException {

       if ((s==null)||(len==0)) return;
       
       for (int i=0;i<Math.min(len,s.size());i++){           
           s.get(i).write(dos);
       }
   }
   
   public final static void readBooleanArray(DataInputStream dis,boolean[] s) throws IOException {
       readBooleanArray(dis,s,s.length);
       }
   
   public final static void readIntBooleanArray(DataInputStream dis,boolean[] s) throws IOException {
       readBooleanIntArray(dis,s,s.length);
       }
  
   public static final void writeCharArray(DataOutputStream dos,char[] charr,int len) throws IOException {

       if ((charr==null)||(len==0)) return;
       
       for (int i=0;i<Math.min(len,charr.length);i++){           
           dos.writeChar(charr[i]);
       }
   }
   
   /** Will read an array of proper Unicode chars.
    * 
    * @param charr
    * @param len
    * @throws IOException
    */
   
   public static final void readCharArray(DataInputStream dis,char[] charr,int len) throws IOException {

       if ((charr==null)||(len==0)) return;
       
       for (int i=0;i<Math.min(len,charr.length);i++){           
           charr[i]=dis.readChar();
       }
   }
   
   /** Will read a bunch of non-unicode chars into a char array.
    *  Useful when dealing with legacy text files.
    * 
    * @param charr
    * @param len
    * @throws IOException
    */
   
   public static final void readNonUnicodeCharArray(DataInputStream dis,char[] charr,int len) throws IOException {

       if ((charr==null)||(len==0)) return;
       
       for (int i=0;i<Math.min(len,charr.length);i++){           
           charr[i]=(char) dis.readUnsignedByte();
       }
   }
   
   /** Writes an item reference. 
   public void writeItem(gitem_t item) throws IOException {
       if (item == null)
           writeInt(-1);
       else
           writeInt(item.index);
   }
*/
   /** Reads the item index and returns the game item. 
   public gitem_t readItem() throws IOException {
       int ndx = readInt();
       if (ndx == -1)
           return null;
       else
           return GameItemList.itemlist[ndx];
   }
 * @throws IOException 
*/
   
   public static final long readUnsignedLEInt(DataInputStream dis) throws IOException{
       int tmp=dis.readInt();
       return 0xFFFFFFFFL&Swap.LONG(tmp);
   }
   
   public static final int readLEInt(DataInputStream dis) throws IOException{
       int tmp=dis.readInt();
       return Swap.LONG(tmp);
   }
   
   public static final int readLEInt(InputStream dis) throws IOException{	   
       int tmp=new DataInputStream(dis).readInt();
       return Swap.LONG(tmp);
   }
   
   public static final void writeLEInt(DataOutputStream dos,int value) throws IOException{       
	   dos.writeInt(Swap.LONG(value));
   }
   
// 2-byte number
   public static int SHORT_little_endian_TO_big_endian(int i)
   {
       return ((i>>8)&0xff)+((i << 8)&0xff00);
   }

   // 4-byte number
   public static int INT_little_endian_TO_big_endian(int i)
   {
       return((i&0xff)<<24)+((i&0xff00)<<8)+((i&0xff0000)>>8)+((i>>24)&0xff);
   }

public static final short readLEShort(DataInputStream dis) throws IOException {
    short tmp=dis.readShort();
    return Swap.SHORT(tmp);
}

/** Reads a "big boolean" using 4 bytes.
 * 
 * @return
 * @throws IOException
 */
public static final boolean readIntBoolean(DataInputStream dis) throws IOException {
    return (dis.readInt()!=0);

}

   
}

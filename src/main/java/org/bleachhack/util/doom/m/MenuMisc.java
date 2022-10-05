package org.bleachhack.util.doom.m;

import org.bleachhack.util.doom.i.DoomSystem;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.bleachhack.util.doom.wad.IWritableDoomObject;

// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: MenuMisc.java,v 1.29 2012/09/24 17:16:22 velktron Exp $
//
// Copyright (C) 1993-1996 by id Software, Inc.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
//
// DESCRIPTION:
//	Main loop menu stuff.
//	Default Config File.
//	PCX Screenshots.
//
//-----------------------------------------------------------------------------

public abstract class MenuMisc{

    public static final String rcsid = "$Id: MenuMisc.java,v 1.29 2012/09/24 17:16:22 velktron Exp $";
  
    //
    // SCREEN SHOTS
    //
  
    public static boolean WriteFile(String name, byte[] source, int length) {
        OutputStream handle;
        try {
            handle = new  FileOutputStream(name);
            handle.write(source, 0, length);
            handle.close();
        } catch (Exception e) {
            DoomSystem.MiscError("Couldn't write file %s (%s)", name, e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean WriteFile(String name, IWritableDoomObject source) {
        DataOutputStream handle;
        try {
            handle = new DataOutputStream(new FileOutputStream(name));
            source.write(handle);
            handle.close();
        } catch (Exception e) {
            DoomSystem.MiscError("Couldn't write file %s (%s)", name, e.getMessage());
            return false;
        }

        return true;
    }


    /** M_ReadFile 
     *  This version returns a variable-size ByteBuffer, so
     *  we don't need to know a-priori how much stuff to read.
     * 
     */
    public static ByteBuffer ReadFile(String name) {
        BufferedInputStream handle;
        int length;
        // struct stat fileinfo;
        ByteBuffer buf;
        try {
            handle = new BufferedInputStream(new FileInputStream(name));
            length = (int) handle.available();
            buf = ByteBuffer.allocate(length);
            handle.read(buf.array());
            handle.close();
        } catch (Exception e) {
            DoomSystem.MiscError("Couldn't read file %s (%s)", name, e.getMessage());
            return null;
        }

        return buf;
    }

    /** M_ReadFile */
    public static int ReadFile(String name, byte[] buffer) {
    	BufferedInputStream handle;
        int count, length;
        // struct stat fileinfo;
        byte[] buf;
        try {
            handle = new BufferedInputStream(new FileInputStream(name));
            length = (int) handle.available();
            buf = new byte[length];
            count = handle.read(buf);
            handle.close();

            if (count < length)
                throw new Exception("Read only " + count + " bytes out of "
                    + length);

        } catch (Exception e) {
            DoomSystem.MiscError("Couldn't read file %s (%s)", name, e.getMessage());
            return -1;
        }
        System.arraycopy(buf, 0, buffer, 0, Math.min(count,buffer.length));
        return length;
    }

    //
 // WritePCXfile
 //
 public static void
 WritePCXfile
 ( String        filename,
   byte[]     data,
   int       width,
   int       height,
   byte[]     palette )
 {
     int     length;
     pcx_t  pcx;
     byte[]   pack;
     
     pcx = new pcx_t();
     pack=new byte[width*height*2]; // allocate that much data, just in case.

     pcx.manufacturer = 0x0a;       // PCX id
     pcx.version = 5;           // 256 color
     pcx.encoding = 1;          // uncompressed
     pcx.bits_per_pixel = 8;        // 256 color
     pcx.xmin = 0;
     pcx.ymin = 0;
     pcx.xmax = (char) (width-1);
     pcx.ymax = (char) (height-1);
     pcx.hres = (char) width;
     pcx.vres = (char) height;
     // memset (pcx->palette,0,sizeof(pcx->palette));
     pcx.color_planes = 1;      // chunky image
     pcx.bytes_per_line = (char) width;
     pcx.palette_type = 2;   // not a grey scale
     //memset (pcx->filler,0,sizeof(pcx->filler));


     // pack the image
     //pack = &pcx->data;
     int p_pack=0;
     
     for (int i=0 ; i<width*height ; i++)
     {
     if ( (data[i] & 0xc0) != 0xc0)
         pack[p_pack++] = data[i];
     else
     {
         pack[p_pack++] = (byte) 0xc1;
         pack[p_pack++] = data[i];
     }
     }
     
     // write the palette
     pack[p_pack++] = 0x0c; // palette ID byte
     for (int i=0 ; i<768 ; i++)
         pack[p_pack++] = palette[i];
     
     // write output file
     length = p_pack;
     pcx.data=Arrays.copyOf(pack, length);
     
     DataOutputStream f=null;
    try {
        f = new DataOutputStream(new FileOutputStream(filename));
        
    } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
     
    try {
        //f.setLength(0);
        pcx.write(f);
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    
     }
    
    public abstract boolean getShowMessages();

    public abstract void setShowMessages(boolean val);

    public static void WritePNGfile(String imagename, short[] linear, int width, int height) {
        BufferedImage buf = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_555_RGB);
        DataBufferUShort sh = (DataBufferUShort) buf.getRaster().getDataBuffer();
        short[] shd = sh.getData();
        System.arraycopy(linear, 0, shd, 0, Math.min(linear.length, shd.length));
        try {
            ImageIO.write(buf, "PNG", new File(imagename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void WritePNGfile(String imagename, int[] linear, int width, int height) {
        BufferedImage buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        DataBufferInt sh = (DataBufferInt) buf.getRaster().getDataBuffer();
        int[] shd = sh.getData();
        System.arraycopy(linear, 0, shd, 0, Math.min(linear.length, shd.length));
        try {
            ImageIO.write(buf, "PNG", new File(imagename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void WritePNGfile(String imagename, byte[] linear, int width, int height, IndexColorModel icm) {
        BufferedImage buf = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, icm);
        DataBufferByte sh = (DataBufferByte) buf.getRaster().getDataBuffer();
        byte[] shd = sh.getData();
        System.arraycopy(linear, 0, shd, 0, Math.min(linear.length, shd.length));
        try {
            ImageIO.write(buf, "PNG", new File(imagename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// $Log: MenuMisc.java,v $
// Revision 1.29  2012/09/24 17:16:22  velktron
// Massive merge between HiColor and HEAD. There's no difference from now on, and development continues on HEAD.
//
// Revision 1.28.2.4  2012/09/24 16:57:43  velktron
// Addressed generics warnings.
//
// Revision 1.28.2.3  2012/09/17 15:58:58  velktron
// Defaults loading & handling moved out to variables management subsystem
//
// Revision 1.28.2.2  2011/11/18 21:37:59  velktron
// Saves PNGs now.
//
// Revision 1.28.2.1  2011/11/14 00:27:11  velktron
// A barely functional HiColor branch. Most stuff broken. DO NOT USE
//
// Revision 1.28  2011/10/25 19:52:03  velktron
// Using buffered I/O when possible
//
// Revision 1.27  2011/10/24 02:11:27  velktron
// Stream compliancy
//
// Revision 1.26  2011/07/30 22:04:30  velktron
// Removed unused imports (including one that would cause problems compiling with OpenJDK).
//
// Revision 1.25  2011/07/15 13:53:52  velktron
// Implemented WritePCXFile, at last.
//
// Revision 1.24  2011/06/03 16:37:09  velktron
// Readfile will only read at most as much as the buffer allows.
//
// Revision 1.23  2011/05/31 13:33:54  velktron
// -verbosity
//
// Revision 1.22  2011/05/31 09:57:45  velktron
// Fixed broken parsing of unspaced strings. 
// It's never fun having to come up with your own function for string manipulation!
//
// Revision 1.21  2011/05/30 15:46:50  velktron
// AbstractDoomMenu implemented.
//
// Revision 1.20  2011/05/26 17:54:16  velktron
// Removed some Menu verbosity, better defaults functionality.
//
// Revision 1.19  2011/05/26 13:39:15  velktron
// Now using ICommandLineManager
//
// Revision 1.18  2011/05/24 17:46:03  velktron
// Added vanilla default.cfg loading.
//
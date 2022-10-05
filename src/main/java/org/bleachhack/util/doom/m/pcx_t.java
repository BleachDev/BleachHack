package org.bleachhack.util.doom.m;

import java.io.DataOutputStream;
import java.io.IOException;

import org.bleachhack.util.doom.wad.IWritableDoomObject;

/** Yeah, this is actually a PCX header implementation, and Mocha Doom
 *  saved PCX screenshots. Implemented it back just to shot that it can be
 *  done (will switch to PNG ASAP though). 
 *  
 *  @author Maes
 * 
 */

public class pcx_t implements IWritableDoomObject{

	//
	// SCREEN SHOTS
	//

		// char -> byte Bytes.
        /** manufacturer byte, must be 10 decimal */
		public byte		manufacturer;
		
		/** PCX version number */
		byte		version;
		
		/** run length encoding byte, must be 1 */
		byte		encoding;
		
		/** number of bits per pixel per bit plane */
		byte		bits_per_pixel;
		
		/** image limits in pixels: Xmin, Ymin, Xmax, Ymax */
		public char	xmin,ymin,xmax,ymax;
	    
		/** horizontal dots per inch when printed (unreliable) */
		char	hres;
		
		/** vertical dots per inch when printed (unreliable) */
		char	vres;

		/** 16-color palette (16 RGB triples between 0-255) 
		 *  UNUSED in Doom. */
		byte[]	palette=new byte[48];
	    
		/** reserved, must be zero */
	    byte		reserved;
	    
	    /** number of bit planes */
	    byte		color_planes;

	    /** video memory bytes per image row */
	    char	bytes_per_line;
	    
	    /** 16-color palette interpretation (unreliable) 0=color/b&w 1=grayscale */
	    char	palette_type;
	    
	    // Seems off-spec. However it's left all zeroed out.
	    byte[]		filler=new byte[58];

	    //unsigned char	data;
	    byte[] data;
	    
		@Override
		public void write(DataOutputStream f) throws IOException {
			// char -> byte Bytes.

			f.writeByte(manufacturer);
			f.writeByte(version);
			f.writeByte(encoding);
			f.writeByte(bits_per_pixel);
			
			// unsigned short -> char
			f.writeChar(Swap.SHORT(xmin));
			f.writeChar(Swap.SHORT(ymin));
			f.writeChar(Swap.SHORT(xmax));
			f.writeChar(Swap.SHORT(ymax));
		    
			f.writeChar(Swap.SHORT(hres));
			f.writeChar(Swap.SHORT(vres));
			f.write(palette);
		    
		    f.writeByte(reserved);
		    f.writeByte(color_planes);
		 // unsigned short -> char
		    f.writeChar(Swap.SHORT(bytes_per_line));
		    f.writeChar(Swap.SHORT(palette_type));
		    
		    f.write(filler);
		    //unsigned char	data;		// unbounded
		    f.write(data);
		}
	} ;

package org.bleachhack.util.doom.s;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.bleachhack.util.doom.wad.CacheableDoomObject;

/** An object representation of Doom's sound format */

public class DMXSound implements CacheableDoomObject{

    /** ushort, all Doom samples are "type 3". No idea how  */    
    public int type;
    /** ushort, speed in Hz. */
    public int speed;    
    /** uint */
    public int datasize;

    public byte[] data;
    
    @Override
    public void unpack(ByteBuffer buf)
            throws IOException {
       buf.order(ByteOrder.LITTLE_ENDIAN);
       type=buf.getChar();
       speed=buf.getChar();
		try {
			datasize = buf.getInt();
		} catch (BufferUnderflowException e) {
			datasize = buf.capacity() - buf.position();
		}
       data=new byte[Math.min(buf.remaining(),datasize)];
       buf.get(data);
    }

}

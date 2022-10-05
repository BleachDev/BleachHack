package org.bleachhack.util.doom.doom;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.bleachhack.util.doom.p.ActiveStates;
import static org.bleachhack.util.doom.utils.C2JUtils.pointer;

import org.bleachhack.util.doom.p.ThinkerStates;
import org.bleachhack.util.doom.wad.CacheableDoomObject;
import org.bleachhack.util.doom.wad.IPackableDoomObject;
import org.bleachhack.util.doom.wad.IReadableDoomObject;

public class thinker_t implements CacheableDoomObject, IReadableDoomObject, IPackableDoomObject {

    public thinker_t prev;
    public thinker_t next;
    public ThinkerStates thinkerFunction = ActiveStates.NOP;

    /**
     * killough's code for thinkers seems to be totally broken in M.D,
     * so commented it out and will not probably restore, but may invent
     * something new in future
     * - Good Sign 2017/05/1
     * 
     * killough 8/29/98: we maintain thinkers in several equivalence classes,
     * according to various criteria, so as to allow quicker searches.
     */
    /**
     * Next, previous thinkers in same class
     */
    //public thinker_t cnext, cprev;

    /**
     * extra fields, to use when archiving/unarchiving for
     * identification. Also in blocklinks, etc.
     */
    public int id, previd, nextid, functionid;

    @Override
    public void read(DataInputStream f)
        throws IOException {
        readbuffer.position(0);
        readbuffer.order(ByteOrder.LITTLE_ENDIAN);
        f.read(readbuffer.array());
        unpack(readbuffer);
    }

    /**
     * This adds 12 bytes
     */
    @Override
    public void pack(ByteBuffer b)
        throws IOException {
        // It's possible to reconstruct even by hashcodes.
        // As for the function, that should be implied by the mobj_t type.
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.putInt(pointer(prev));
        b.putInt(pointer(next));
        b.putInt(pointer(thinkerFunction.ordinal()));
        //System.out.printf("Packed thinker %d %d %d\n",pointer(prev),pointer(next),pointer(function));
    }

    @Override
    public void unpack(ByteBuffer b)
        throws IOException {
        // We are supposed to archive pointers to other thinkers,
        // but they are rather useless once on disk.
        b.order(ByteOrder.LITTLE_ENDIAN);
        previd = b.getInt();
        nextid = b.getInt();
        functionid = b.getInt();
        //System.out.printf("Unpacked thinker %d %d %d\n",pointer(previd),pointer(nextid),pointer(functionid));
    }

    private static final ByteBuffer readbuffer = ByteBuffer.allocate(12);

}

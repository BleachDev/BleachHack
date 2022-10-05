package org.bleachhack.util.doom.p;

import org.bleachhack.util.doom.doom.SourceCode.fixed_t;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.bleachhack.util.doom.rr.SectorAction;
import org.bleachhack.util.doom.wad.CacheableDoomObject;
import org.bleachhack.util.doom.wad.IPackableDoomObject;
import org.bleachhack.util.doom.wad.IReadableDoomObject;

public class ceiling_t extends SectorAction implements CacheableDoomObject, IReadableDoomObject, IPackableDoomObject {

    public ceiling_e type;
    @fixed_t public int bottomheight;
    @fixed_t public int topheight;
    @fixed_t public int speed;
    public boolean crush;

    // 1 = up, 0 = waiting, -1 = down
    public int direction;

    // ID
    public int tag;
    public int olddirection;

    public ceiling_t() {
        // Set to the smallest ordinal type.
        this.type = ceiling_e.lowerToFloor;
    }

    // HACK for speed.
    public static final ceiling_e[] values = ceiling_e.values();

    @Override
    public void read(DataInputStream f) throws IOException {
        // Read 48 bytes.
        readbuffer.position(0);
        readbuffer.order(ByteOrder.LITTLE_ENDIAN);
        f.read(readbuffer.array(), 0, 48);
        unpack(readbuffer);
    }

    @Override
    public void pack(ByteBuffer b) throws IOException {
        b.order(ByteOrder.LITTLE_ENDIAN);
        super.pack(b); //12            
        b.putInt(type.ordinal()); // 16            
        b.putInt(super.sectorid); // 20
        b.putInt(bottomheight);
        b.putInt(topheight); // 28
        b.putInt(speed);
        b.putInt(crush ? 1 : 0);
        b.putInt(direction); // 40
        b.putInt(tag);
        b.putInt(olddirection); //48
    }

    @Override
    public void unpack(ByteBuffer b) throws IOException {
        b.order(ByteOrder.LITTLE_ENDIAN);
        super.unpack(b); // Call thinker reader first
        type = values[b.getInt()];
        super.sectorid = b.getInt(); // sector pointer.
        bottomheight = b.getInt();
        topheight = b.getInt();
        speed = b.getInt();
        crush = (b.getInt() != 0);
        direction = b.getInt();
        tag = b.getInt();
        olddirection = b.getInt();
    }

    private static final ByteBuffer readbuffer = ByteBuffer.allocate(48);
}

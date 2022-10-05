package org.bleachhack.util.doom.rr;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.bleachhack.util.doom.p.Resettable;

import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;
import org.bleachhack.util.doom.wad.DoomIO;
import org.bleachhack.util.doom.wad.IPackableDoomObject;
import org.bleachhack.util.doom.wad.IReadableDoomObject;

/**
 * The SideDef.
 * 
 * @author admin
 */
public class side_t
        implements IReadableDoomObject, IPackableDoomObject, Resettable {
    /** (fixed_t) add this to the calculated texture column */
    public int textureoffset;

    /** (fixed_t) add this to the calculated texture top */
    public int rowoffset;

    /**
     * Texture indices. We do not maintain names here.
     */
    public short toptexture;

    public short bottomtexture;

    public short midtexture;

    /** Sector the SideDef is facing. MAES: pointer */
    public sector_t sector;

    public int sectorid;

    public int special;

    public side_t() {
    }

    public side_t(int textureoffset, int rowoffset, short toptexture,
            short bottomtexture, short midtexture, sector_t sector) {
        super();
        this.textureoffset = textureoffset;
        this.rowoffset = rowoffset;
        this.toptexture = toptexture;
        this.bottomtexture = bottomtexture;
        this.midtexture = midtexture;
        this.sector = sector;
    }

    @Override
    public void read(DataInputStream f)
            throws IOException {
        this.textureoffset = DoomIO.readLEShort(f) << FRACBITS;
        this.rowoffset = DoomIO.readLEShort(f) << FRACBITS;
        this.toptexture = DoomIO.readLEShort(f);
        this.bottomtexture = DoomIO.readLEShort(f);
        this.midtexture = DoomIO.readLEShort(f);
        // this.sectorid=f.readLEInt();

    }

    @Override
    public void pack(ByteBuffer buffer) {
        buffer.putShort((short) (textureoffset >> FRACBITS));
        buffer.putShort((short) (rowoffset >> FRACBITS));
        buffer.putShort(toptexture);
        buffer.putShort(bottomtexture);
        buffer.putShort(midtexture);
    }

    @Override
    public void reset() {
        textureoffset = 0;
        rowoffset = 0;
        toptexture = 0;
        bottomtexture = 0;
        midtexture = 0;
        sector = null;
        sectorid = 0;
        special = 0;

    }

}

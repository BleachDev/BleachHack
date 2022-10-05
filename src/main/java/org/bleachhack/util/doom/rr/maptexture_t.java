package org.bleachhack.util.doom.rr;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import static org.bleachhack.util.doom.utils.GenericCopy.malloc;
import org.bleachhack.util.doom.wad.CacheableDoomObject;
import org.bleachhack.util.doom.wad.DoomBuffer;

/** Texture definition.
 *  A DOOM wall texture is a list of patches which are to be combined in a predefined order.
 *  This is the ON-DISK structure, to be read from the TEXTURES1 and TEXTURES2 lumps.
 *  In memory, this becomes texture_t.
 *    
 *  @author MAES
 *
 */

public class maptexture_t implements CacheableDoomObject{
    public String        name;
    public  boolean     masked; 
    public short       width; // was signed byte
    public short       height; // was 
    //void**t        columndirectory;  // OBSOLETE (yeah, but we must read a dummy integer here)
    public short       patchcount;
    public mappatch_t[]  patches;
    
    
    @Override
    public void unpack(ByteBuffer buf) throws IOException {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        name = DoomBuffer.getNullTerminatedString(buf, 8);
        masked = (buf.getInt() != 0);
        width = buf.getShort();
        height = buf.getShort();
        buf.getInt(); // read a dummy integer for obsolete columndirectory.
        patchcount = buf.getShort();

        // Simple sanity check. Do not attempt reading more patches than there
        // are left in the TEXTURE lump.
        patchcount = (short) Math.min(patchcount, (buf.capacity() - buf.position()) / mappatch_t.size());

        patches = malloc(mappatch_t::new, mappatch_t[]::new, patchcount);
        DoomBuffer.readObjectArray(buf, patches, patchcount);
    }  
};
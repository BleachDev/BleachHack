package org.bleachhack.util.doom.wad;

import java.io.DataInputStream;
import java.io.IOException;

/** This is for objects that can be read from disk, but cannot
 *  self-determine their own length for some reason.
 * 
 * @author Maes
 *
 */

public interface AidedReadableDoomObject {
    
    public void read(DataInputStream f, int len) throws IOException ;
}

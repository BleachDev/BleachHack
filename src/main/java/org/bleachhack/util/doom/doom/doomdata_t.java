package org.bleachhack.util.doom.doom;

import java.nio.ByteBuffer;
import static org.bleachhack.util.doom.utils.GenericCopy.malloc;
import org.bleachhack.util.doom.wad.DoomBuffer;

public class doomdata_t implements IDatagramSerializable {

	public static final int DOOMDATALEN = 8 + org.bleachhack.util.doom.data.Defines.BACKUPTICS * ticcmd_t.TICCMDLEN;
    
	// High bit is retransmit request.
	/** MAES: was "unsigned" */
	public int checksum;

	/*
	 * CAREFUL!!! Those "bytes" are actually unsigned
	 */

	/** Only valid if NCMD_RETRANSMIT. */
	public byte retransmitfrom;

	public byte starttic;
	public byte player;
	public byte numtics;
	public ticcmd_t[] cmds;
     
	public doomdata_t() {
		cmds = malloc(ticcmd_t::new, ticcmd_t[]::new, org.bleachhack.util.doom.data.Defines.BACKUPTICS);
		// Enough space for its own header + the ticcmds;
		buffer = new byte[DOOMDATALEN];
		// This "pegs" the ByteBuffer to this particular array.
		// Separate updates are not necessary.
		bbuf = ByteBuffer.wrap(buffer);
	}

    // Used for datagram serialization.
    private byte[] buffer;
    private ByteBuffer bbuf;
    
    @Override
    public byte[] pack() {        
        bbuf.rewind();
        
        // Why making it harder?
        bbuf.putInt(checksum);
        bbuf.put(retransmitfrom);
        bbuf.put(starttic);
        bbuf.put(player);
        bbuf.put(numtics);
        
        // FIXME: it's probably more efficient to use System.arraycopy ? 
        // Or are the packets too small anyway? At most we'll be sending "doomdata_t's"
        
		for (int i = 0; i < cmds.length; i++) {
			bbuf.put(cmds[i].pack());
		}
        
        return bbuf.array();
    }

    @Override
    public void pack(byte[] buf, int offset) {
		// No need to make it harder...just pack it and slap it in.
		byte[] tmp = this.pack();
		System.arraycopy(tmp, 0, buf, offset, tmp.length);
    }

    @Override
    public void unpack(byte[] buf) {
        unpack(buf,0);
    }

    @Override
    public void unpack(byte[] buf, int offset) {
		checksum = DoomBuffer.getBEInt(buf);
		offset = +4;
		retransmitfrom = buf[offset++];
		starttic = buf[offset++];
		player = buf[offset++];
		numtics = buf[offset++];

		for (int i = 0; i < cmds.length; i++) {
			cmds[i].unpack(buf, offset);
			offset += ticcmd_t.TICCMDLEN;
		}
    }
    
    public void selfUnpack(){
        unpack(this.buffer);
    }
    
	public void copyFrom(doomdata_t source) {
		this.checksum = source.checksum;
		this.numtics = source.numtics;
		this.player = source.player;
		this.retransmitfrom = source.retransmitfrom;
		this.starttic = source.starttic;

		// MAES: this was buggy as hell, and didn't work at all, which
		// in turn prevented other subsystems such as speed throttling and
		// networking to work.
		//
		// This should be enough to alter the ByteBuffer too.
		// System.arraycopy(source.cached(), 0, this.buffer, 0, DOOMDATALEN);
		// This should set all fields
		// selfUnpack();
	}

	@Override
	public byte[] cached() {
		return this.buffer;
	}
    
	StringBuilder sb = new StringBuilder();

	public String toString() {
		sb.setLength(0);
		sb.append("doomdata_t ");
		sb.append(retransmitfrom);
		sb.append(" starttic ");
		sb.append(starttic);
		sb.append(" player ");
		sb.append(player);
		sb.append(" numtics ");
		sb.append(numtics);
		return sb.toString();
	}

 }

package org.bleachhack.util.doom.rr.drawfuns;

import org.bleachhack.util.doom.i.IDoomSystem;
import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;

public final class
R_DrawColumnLow extends DoomColumnFunction<byte[],short[]> {
    
		public R_DrawColumnLow(int SCREENWIDTH, int SCREENHEIGHT,
	            int[] ylookup, int[] columnofs, ColVars<byte[],short[]> dcvars,
	            short[] screen, IDoomSystem I) {
	        super(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, dcvars, screen, I);
	        this.flags=DcFlags.LOW_DETAIL;
	    }

        public void invoke() {
			int count;
			// MAES: were pointers. Of course...
			int dest, dest2;
			final byte[] dc_source=dcvars.dc_source;
			final short[] dc_colormap=dcvars.dc_colormap;
			final int dc_source_ofs=dcvars.dc_source_ofs;
			// Maes: fixed_t never used as such.
			int frac;
			final int fracstep;

			count = dcvars.dc_yh - dcvars.dc_yl;

			// Zero length.
			if (count < 0)
				return;

			if (RANGECHECK) {
			    performRangeCheck();
			}

			// The idea is to draw more than one pixel at a time.
			dest = blockyDest1();
			dest2 = blockyDest2();

			fracstep = dcvars.dc_iscale;
			frac = dcvars.dc_texturemid + (dcvars.dc_yl - dcvars.centery) * fracstep;
			// int spot=(frac >>> FRACBITS) & 127;
			do {

				// Hack. Does not work correctly.
				// MAES: that's good to know.
				screen[dest] = screen[dest2] = dc_colormap[0x00FF & dc_source[dc_source_ofs
						+ ((frac >>> FRACBITS) & 127)]];

				dest += SCREENWIDTH;
				dest2 += SCREENWIDTH;
				frac += fracstep;
			} while (count-- != 0);
		}
	}
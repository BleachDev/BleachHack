package org.bleachhack.util.doom.rr.drawfuns;
import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;
import org.bleachhack.util.doom.i.IDoomSystem;


public final class R_DrawTLColumn extends DoomColumnFunction<byte[],short[]> {

		public R_DrawTLColumn(int SCREENWIDTH, int SCREENHEIGHT,
	            int[] ylookup, int[] columnofs, ColVars<byte[],short[]> dcvars,
	            short[] screen, IDoomSystem I) {
	        super(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, dcvars, screen, I);
	        this.flags=DcFlags.TRANSPARENT;
	    }

        public void invoke() {
			int count;
			int dest; // killough
			int frac; // killough
			final int fracstep;
			final int dc_source_ofs=dcvars.dc_source_ofs;
			final byte[] tranmap=dcvars.tranmap;

			count = dcvars.dc_yh - dcvars.dc_yl + 1;

			if (count <= 0) // Zero length, column does not exceed a pixel.
				return;

			if (RANGECHECK) {
				performRangeCheck();
			}

			// Framebuffer destination address.
			// Use ylookup LUT to avoid multiply with ScreenWidth.
			// Use columnofs LUT for subwindows?

			dest = computeScreenDest();

			// Determine scaling, which is the only mapping to be done.

			fracstep = dcvars.dc_iscale;
			frac = dcvars.dc_texturemid + (dcvars.dc_yl - dcvars.centery) * fracstep;

			// Inner loop that does the actual texture mapping,
			// e.g. a DDA-lile scaling.
			// This is as fast as it gets. (Yeah, right!!! -- killough)
			//
			// killough 2/1/98: more performance tuning

			{
				final byte[] source = dcvars.dc_source;
				final short[] colormap = dcvars.dc_colormap;
				int heightmask = dcvars.dc_texheight - 1;
				if ((dcvars.dc_texheight & heightmask) != 0) // not a power of 2 --
														// killough
				{
					heightmask++;
					heightmask <<= FRACBITS;

					if (frac < 0)
						while ((frac += heightmask) < 0)
							;
					else
						while (frac >= heightmask)
							frac -= heightmask;

					do {
						// Re-map color indices from wall texture column
						// using a lighting/special effects LUT.
						// heightmask is the Tutti-Frutti fix -- killough

						screen[dest] = tranmap[0xFF00
								& (screen[dest] << 8)
								| (0x00FF & colormap[0x00FF & source[dc_source_ofs
										+ ((frac >> FRACBITS) & heightmask)]])];
						dest += SCREENWIDTH;
						if ((frac += fracstep) >= heightmask)
							frac -= heightmask;
					} while (--count > 0);
				} else {
					while ((count -= 4) >= 0) // texture height is a power of 2
												// -- killough
					{
						// screen[dest] =
						// main_tranmap[0xFF00&(screen[dest]<<8)|(0x00FF&colormap[0x00FF&source[dc_source_ofs+((frac>>FRACBITS)
						// & heightmask)]])];
						screen[dest] = tranmap[0xFF00
								& (screen[dest] << 8)
								| (0x00FF & colormap[0x00FF & source[dc_source_ofs
										+ ((frac >> FRACBITS) & heightmask)]])];
						dest += SCREENWIDTH;
						frac += fracstep;
						screen[dest] = tranmap[0xFF00
								& (screen[dest] << 8)
								| (0x00FF & colormap[0x00FF & source[dc_source_ofs
										+ ((frac >> FRACBITS) & heightmask)]])];
						dest += SCREENWIDTH;
						frac += fracstep;
						screen[dest] = tranmap[0xFF00
								& (screen[dest] << 8)
								| (0x00FF & colormap[0x00FF & source[dc_source_ofs
										+ ((frac >> FRACBITS) & heightmask)]])];
						dest += SCREENWIDTH;
						frac += fracstep;
						screen[dest] = tranmap[0xFF00
								& (screen[dest] << 8)
								| (0x00FF & colormap[0x00FF & source[dc_source_ofs
										+ ((frac >> FRACBITS) & heightmask)]])];
						dest += SCREENWIDTH;
						frac += fracstep;
					}
					if ((count & 1) != 0)
						screen[dest] = tranmap[0xFF00
								& (screen[dest] << 8)
								| (0x00FF & colormap[0x00FF & source[dc_source_ofs
										+ ((frac >> FRACBITS) & heightmask)]])];
				}
			}
		}
	}
/*-----------------------------------------------------------------------------
//
// Copyright (C) 1993-1996 Id Software, Inc.
// Copyright (C) 2017 Good Sign
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// From r_draw.c
//-----------------------------------------------------------------------------*/
package org.bleachhack.util.doom.rr.drawfuns;

import org.bleachhack.util.doom.i.IDoomSystem;
import org.bleachhack.util.doom.v.tables.BlurryTable;

/**
 * fuzzMix was preserved, but moved to its own interface.
 * Implemented by BlurryTable if cfg option fuzz_mix is set
 *  - Good Sign 2017/04/16
 * 
 * Low detail version. Jesus.
 */
public abstract class R_DrawFuzzColumnLow<T, V> extends DoomColumnFunction<T, V> {

	public R_DrawFuzzColumnLow(
        int SCREENWIDTH, int SCREENHEIGHT,
        int[] ylookup, int[] columnofs, ColVars<T, V> dcvars,
        V screen, IDoomSystem I, BlurryTable BLURRY_MAP
    ) {
		this(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, dcvars, screen, I);
		this.blurryTable = BLURRY_MAP;
	}

	public R_DrawFuzzColumnLow(
        int SCREENWIDTH, int SCREENHEIGHT,
        int[] ylookup, int[] columnofs, ColVars<T, V> dcvars,
        V screen, IDoomSystem I
    ) {
		super(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, dcvars, screen, I);
		this.flags = DcFlags.LOW_DETAIL | DcFlags.FUZZY;

		FUZZOFF = SCREENWIDTH;

		// Recompute fuzz table

		fuzzoffset = new int[] { FUZZOFF, -FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF,
				FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF,
				FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, FUZZOFF,
				-FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF, FUZZOFF, -FUZZOFF,
				-FUZZOFF, FUZZOFF, FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF,
				FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF, -FUZZOFF,
				FUZZOFF, FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF,
				FUZZOFF, FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF,
				-FUZZOFF, FUZZOFF };
	}

	protected int fuzzpos;

	//
	// Spectre/Invisibility.
	//

	protected final int FUZZOFF;
	protected final int[] fuzzoffset;

	public static final class Indexed extends R_DrawFuzzColumn<byte[], byte[]> {

		public Indexed(
            int SCREENWIDTH, int SCREENHEIGHT, int[] ylookup,
            int[] columnofs, ColVars<byte[], byte[]> dcvars,
            byte[] screen, IDoomSystem I, BlurryTable BLURRY_MAP
        ) {
			super(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, dcvars, screen, I, BLURRY_MAP);
		}

        @Override
		public void invoke() {
			int count;
			int dest, dest2;

			// Adjust borders. Low...
			if (dcvars.dc_yl == 0)
				dcvars.dc_yl = 1;

			// .. and high.
			if (dcvars.dc_yh == dcvars.viewheight - 1)
				dcvars.dc_yh = dcvars.viewheight - 2;

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

			// Looks like an attempt at dithering,
			// using the colormap #6 (of 0-31, a bit
			// brighter than average).
			if (count > 4) {
				do {
					// Lookup framebuffer, and retrieve
					// a pixel that is either one column
					// left or right of the current one.
					// Add index from colormap to index.
					screen[dest] = blurryTable.computePixel(screen[dest + fuzzoffset[fuzzpos]]);
					screen[dest2] = screen[dest];

					// Ironically, "low detail" fuzziness was not really
					// low-detail,
					// as it normally did full-precision calculations.
					// BLURRY_MAP[0x00FF & screen[dest2+ fuzzoffset[fuzzpos]]];

					// Clamp table lookup index.
					if (++fuzzpos == FUZZTABLE)
						fuzzpos = 0;

					dest += SCREENWIDTH;
					dest2 += SCREENWIDTH;

					screen[dest] = blurryTable.computePixel(screen[dest + fuzzoffset[fuzzpos]]);
					screen[dest2] = screen[dest];
					if (++fuzzpos == FUZZTABLE)
						fuzzpos = 0;
					dest += SCREENWIDTH;
					dest2 += SCREENWIDTH;

					screen[dest] = blurryTable.computePixel(screen[dest + fuzzoffset[fuzzpos]]);
					screen[dest2] = screen[dest];
					if (++fuzzpos == FUZZTABLE)
						fuzzpos = 0;
					dest += SCREENWIDTH;
					dest2 += SCREENWIDTH;

					screen[dest] = blurryTable.computePixel(screen[dest + fuzzoffset[fuzzpos]]);
					screen[dest2] = screen[dest];
					if (++fuzzpos == FUZZTABLE)
						fuzzpos = 0;
					dest += SCREENWIDTH;
					dest2 += SCREENWIDTH;
				} while ((count -= 4) > 4);
            }

			if (count > 0) {
				do {
					screen[dest] = blurryTable.computePixel(screen[dest + fuzzoffset[fuzzpos]]);
					screen[dest2] = screen[dest];

					if (++fuzzpos == FUZZTABLE)
						fuzzpos = 0;

					dest += SCREENWIDTH;
					dest2 += SCREENWIDTH;
				} while (count-- != 0);
            }
		}
	}
	
	public static final class HiColor extends R_DrawFuzzColumn<byte[], short[]> {

    public HiColor(
        int SCREENWIDTH, int SCREENHEIGHT, int[] ylookup,
        int[] columnofs, ColVars<byte[], short[]> dcvars,
        short[] screen, IDoomSystem I, BlurryTable BLURRY_MAP
    ) {
        super(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, dcvars, screen, I, BLURRY_MAP);
    }

    @Override
    public void invoke() {
        int count;
        int dest, dest2;

        // Adjust borders. Low...
        if (dcvars.dc_yl == 0)
            dcvars.dc_yl = 1;

        // .. and high.
        if (dcvars.dc_yh == dcvars.viewheight - 1)
            dcvars.dc_yh = dcvars.viewheight - 2;

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

        // Looks like an attempt at dithering,
        // using the colormap #6 (of 0-31, a bit
        // brighter than average).
        if (count > 4) {
            do {
                // Lookup framebuffer, and retrieve
                // a pixel that is either one column
                // left or right of the current one.
                // Add index from colormap to index.
                screen[dest] = blurryTable.computePixel(screen[dest + fuzzoffset[fuzzpos]]);
                screen[dest2] = screen[dest];

                // Ironically, "low detail" fuzziness was not really
                // low-detail,
                // as it normally did full-precision calculations.
                // BLURRY_MAP[0x00FF & screen[dest2+ fuzzoffset[fuzzpos]]];

                // Clamp table lookup index.
                if (++fuzzpos == FUZZTABLE)
                    fuzzpos = 0;

                dest += SCREENWIDTH;
                dest2 += SCREENWIDTH;

                screen[dest] = blurryTable.computePixel(screen[dest + fuzzoffset[fuzzpos]]);
                screen[dest2] = screen[dest];
                if (++fuzzpos == FUZZTABLE)
                    fuzzpos = 0;
                dest += SCREENWIDTH;
                dest2 += SCREENWIDTH;

                screen[dest] = blurryTable.computePixel(screen[dest + fuzzoffset[fuzzpos]]);
                screen[dest2] = screen[dest];
                if (++fuzzpos == FUZZTABLE)
                    fuzzpos = 0;
                dest += SCREENWIDTH;
                dest2 += SCREENWIDTH;

                screen[dest] = blurryTable.computePixel(screen[dest + fuzzoffset[fuzzpos]]);
                screen[dest2] = screen[dest];
                if (++fuzzpos == FUZZTABLE)
                    fuzzpos = 0;
                dest += SCREENWIDTH;
                dest2 += SCREENWIDTH;
            } while ((count -= 4) > 4);
        }

        if (count > 0)
            do {
                screen[dest] = blurryTable.computePixel(screen[dest + fuzzoffset[fuzzpos]]);
                screen[dest2] = screen[dest];

                if (++fuzzpos == FUZZTABLE)
                    fuzzpos = 0;

                dest += SCREENWIDTH;
                dest2 += SCREENWIDTH;
            } while (count-- != 0);

		}
	}

	public static final class TrueColor extends R_DrawFuzzColumn<byte[], int[]> {

        public TrueColor(
            int SCREENWIDTH, int SCREENHEIGHT, int[] ylookup,
            int[] columnofs, ColVars<byte[], int[]> dcvars,
            int[] screen, IDoomSystem I, BlurryTable BLURRY_MAP
        ) {
            super(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, dcvars, screen, I, BLURRY_MAP);
        }

        @Override
        public void invoke() {
            int count;
            int dest, dest2;

            // Adjust borders. Low...
            if (dcvars.dc_yl == 0)
                dcvars.dc_yl = 1;

            // .. and high.
            if (dcvars.dc_yh == dcvars.viewheight - 1)
                dcvars.dc_yh = dcvars.viewheight - 2;

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

            // Looks like an attempt at dithering,
            // using the colormap #6 (of 0-31, a bit
            // brighter than average).
            if (count > 4)
                do {
                    // Lookup framebuffer, and retrieve
                    // a pixel that is either one column
                    // left or right of the current one.
                    // Add index from colormap to index.
                    screen[dest] = blurryTable.computePixelFast(screen[dest + fuzzoffset[fuzzpos]]);
                    screen[dest2] = screen[dest];

                    // Ironically, "low detail" fuzziness was not really
                    // low-detail,
                    // as it normally did full-precision calculations.
                    // BLURRY_MAP[0x00FF & screen[dest2+ fuzzoffset[fuzzpos]]];

                    // Clamp table lookup index.
                    if (++fuzzpos == FUZZTABLE)
                        fuzzpos = 0;

                    dest += SCREENWIDTH;
                    dest2 += SCREENWIDTH;

                    screen[dest] = blurryTable.computePixelFast(screen[dest + fuzzoffset[fuzzpos]]);
                    screen[dest2] = screen[dest];
                    if (++fuzzpos == FUZZTABLE)
                        fuzzpos = 0;
                    dest += SCREENWIDTH;
                    dest2 += SCREENWIDTH;

                    screen[dest] = blurryTable.computePixelFast(screen[dest + fuzzoffset[fuzzpos]]);
                    screen[dest2] = screen[dest];
                    if (++fuzzpos == FUZZTABLE)
                        fuzzpos = 0;
                    dest += SCREENWIDTH;
                    dest2 += SCREENWIDTH;

                    screen[dest] = blurryTable.computePixelFast(screen[dest + fuzzoffset[fuzzpos]]);
                    screen[dest2] = screen[dest];
                    if (++fuzzpos == FUZZTABLE)
                        fuzzpos = 0;
                    dest += SCREENWIDTH;
                    dest2 += SCREENWIDTH;
                } while ((count -= 4) > 4);

            if (count > 0)
                do {
                    screen[dest] = blurryTable.computePixelFast(screen[dest + fuzzoffset[fuzzpos]]);
                    screen[dest2] = screen[dest];

                    if (++fuzzpos == FUZZTABLE)
                        fuzzpos = 0;

                    dest += SCREENWIDTH;
                    dest2 += SCREENWIDTH;
                } while (count-- != 0);

        }
    }
}
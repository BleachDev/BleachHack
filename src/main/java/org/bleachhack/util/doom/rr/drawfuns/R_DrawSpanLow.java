package org.bleachhack.util.doom.rr.drawfuns;

import org.bleachhack.util.doom.i.IDoomSystem;

public abstract class R_DrawSpanLow<T, V>
        extends DoomSpanFunction<T, V> {

    public R_DrawSpanLow(int SCREENWIDTH, int SCREENHEIGHT, int[] ylookup,
            int[] columnofs, SpanVars<T, V> dsvars, V screen, IDoomSystem I) {
        super(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, dsvars, screen, I);
    }

    public static final class Indexed
    		extends R_DrawSpanLow<byte[], byte[]> {

		public Indexed(int SCREENWIDTH, int SCREENHEIGHT, int[] ylookup,
		        int[] columnofs, SpanVars<byte[], byte[]> dsvars,
		        byte[] screen, IDoomSystem I) {
		    super(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, dsvars,
		            screen, I);
		}
		
		public void invoke() {
		    final byte[] ds_source = dsvars.ds_source;
		    final byte[] ds_colormap = dsvars.ds_colormap;
		    final int ds_xstep = dsvars.ds_xstep;
		    final int ds_ystep = dsvars.ds_ystep;
		    int f_xfrac = dsvars.ds_xfrac;
		    int f_yfrac = dsvars.ds_yfrac;
		    int dest;
		    int count;
		    int spot;
		
		    if (RANGECHECK) {
		        doRangeCheck();
		        // dscount++;
		    }
		
		    // MAES: count must be performed before shifting.
		    count = dsvars.ds_x2 - dsvars.ds_x1;
		
		    // Blocky mode, need to multiply by 2.
		    dsvars.ds_x1 <<= 1;
		    dsvars.ds_x2 <<= 1;
		
		    dest = ylookup[dsvars.ds_y] + columnofs[dsvars.ds_x1];
		
		    do {
		        spot =
		            ((f_yfrac >> (16 - 6)) & (63 * 64))
		                    + ((f_xfrac >> 16) & 63);
		        // Lowres/blocky mode does it twice,
		        // while scale is adjusted appropriately.
		
		        screen[dest++] = ds_colormap[0x00FF & ds_source[spot]];
		        screen[dest++] = ds_colormap[0x00FF & ds_source[spot]];
		
		        f_xfrac += ds_xstep;
		        f_yfrac += ds_ystep;
		
		    } while (count-- > 0);
		
		}
	}
    
    public static final class HiColor
            extends R_DrawSpanLow<byte[], short[]> {

        public HiColor(int SCREENWIDTH, int SCREENHEIGHT, int[] ylookup,
                int[] columnofs, SpanVars<byte[], short[]> dsvars,
                short[] screen, IDoomSystem I) {
            super(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, dsvars,
                    screen, I);
        }

        @Override
        public void invoke() {
            final byte[] ds_source = dsvars.ds_source;
            final short[] ds_colormap = dsvars.ds_colormap;
            final int ds_xstep = dsvars.ds_xstep;
            final int ds_ystep = dsvars.ds_ystep;
            int f_xfrac = dsvars.ds_xfrac;
            int f_yfrac = dsvars.ds_yfrac;
            int dest;
            int count;
            int spot;

            if (RANGECHECK) {
                doRangeCheck();
                // dscount++;
            }

            // MAES: count must be performed before shifting.
            count = dsvars.ds_x2 - dsvars.ds_x1;

            // Blocky mode, need to multiply by 2.
            dsvars.ds_x1 <<= 1;
            dsvars.ds_x2 <<= 1;

            dest = ylookup[dsvars.ds_y] + columnofs[dsvars.ds_x1];

            do {
                spot =
                    ((f_yfrac >> (16 - 6)) & (63 * 64))
                            + ((f_xfrac >> 16) & 63);
                // Lowres/blocky mode does it twice,
                // while scale is adjusted appropriately.

                screen[dest++] = ds_colormap[0x00FF & ds_source[spot]];
                screen[dest++] = ds_colormap[0x00FF & ds_source[spot]];

                f_xfrac += ds_xstep;
                f_yfrac += ds_ystep;

            } while (count-- > 0);

        }
    }

    public static final class TrueColor
            extends R_DrawSpanLow<byte[], int[]> {

        public TrueColor(int SCREENWIDTH, int SCREENHEIGHT, int[] ylookup,
                int[] columnofs, SpanVars<byte[], int[]> dsvars, int[] screen,
                IDoomSystem I) {
            super(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, dsvars,
                    screen, I);
        }

        @Override
        public void invoke() {
            final byte[] ds_source = dsvars.ds_source;
            final int[] ds_colormap = dsvars.ds_colormap;
            final int ds_xstep = dsvars.ds_xstep;
            final int ds_ystep = dsvars.ds_ystep;
            int f_xfrac = dsvars.ds_xfrac;
            int f_yfrac = dsvars.ds_yfrac;
            int dest;
            int count;
            int spot;

            if (RANGECHECK) {
                doRangeCheck();
                // dscount++;
            }

            // MAES: count must be performed before shifting.
            count = dsvars.ds_x2 - dsvars.ds_x1;

            // Blocky mode, need to multiply by 2.
            dsvars.ds_x1 <<= 1;
            dsvars.ds_x2 <<= 1;

            dest = ylookup[dsvars.ds_y] + columnofs[dsvars.ds_x1];

            do {
                spot =
                    ((f_yfrac >> (16 - 6)) & (63 * 64))
                            + ((f_xfrac >> 16) & 63);
                // Lowres/blocky mode does it twice,
                // while scale is adjusted appropriately.

                screen[dest++] = ds_colormap[0x00FF & ds_source[spot]];
                screen[dest++] = ds_colormap[0x00FF & ds_source[spot]];

                f_xfrac += ds_xstep;
                f_yfrac += ds_ystep;

            } while (count-- > 0);

        }
    }
}
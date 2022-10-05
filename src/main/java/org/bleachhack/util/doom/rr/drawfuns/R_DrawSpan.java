package org.bleachhack.util.doom.rr.drawfuns;

import org.bleachhack.util.doom.i.IDoomSystem;

/**
 * Draws the actual span.
 * 
 * ds_frac, ds_yfrac, ds_x2, ds_x1, ds_xstep and ds_ystep must be set.
 * 
 */

public abstract class R_DrawSpan<T, V> extends DoomSpanFunction<T, V> {

	public R_DrawSpan(int sCREENWIDTH, int sCREENHEIGHT, int[] ylookup,
			int[] columnofs, SpanVars<T, V> dsvars, V screen, IDoomSystem I) {
		super(sCREENWIDTH, sCREENHEIGHT, ylookup, columnofs, dsvars, screen, I);
		// TODO Auto-generated constructor stub
	}

	public static final class Indexed extends R_DrawSpan<byte[], byte[]> {

		public Indexed(int SCREENWIDTH, int SCREENHEIGHT, int[] ylookup,
				int[] columnofs, SpanVars<byte[], byte[]> dsvars,
				byte[] screen, IDoomSystem I) {
			super(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, dsvars,
					screen, I);
		}

		public void invoke() {

			int f_xfrac; // fixed_t
			int f_yfrac; // fixed_t
			int dest, count, spot;
			final byte[] ds_colormap = dsvars.ds_colormap;
			final byte[] ds_source = dsvars.ds_source;

			// System.out.println("R_DrawSpan: "+ds_x1+" to "+ds_x2+" at "+
			// ds_y);

			if (RANGECHECK) {
				doRangeCheck();
				// dscount++;
			}

			f_xfrac = dsvars.ds_xfrac;
			f_yfrac = dsvars.ds_yfrac;

			dest = ylookup[dsvars.ds_y] + columnofs[dsvars.ds_x1];

			// We do not check for zero spans here?
			count = dsvars.ds_x2 - dsvars.ds_x1;

			do {
				// Current texture index in u,v.
				spot = ((f_yfrac >> (16 - 6)) & (63 * 64))
						+ ((f_xfrac >> 16) & 63);

				// Lookup pixel from flat texture tile,
				// re-index using light/colormap.
				screen[dest++] = ds_colormap[0x00FF & ds_source[spot]];

				// Next step in u,v.
				f_xfrac += dsvars.ds_xstep;
				f_yfrac += dsvars.ds_ystep;

			} while (count-- > 0);
		}
	}

	public static final class HiColor extends R_DrawSpan<byte[], short[]> {

		public HiColor(int SCREENWIDTH, int SCREENHEIGHT, int[] ylookup,
				int[] columnofs, SpanVars<byte[], short[]> dsvars,
				short[] screen, IDoomSystem I) {
			super(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, dsvars,
					screen, I);
		}

		public void invoke() {

			int f_xfrac; // fixed_t
			int f_yfrac; // fixed_t
			int dest, count, spot;
			final short[] ds_colormap = dsvars.ds_colormap;
			final byte[] ds_source = dsvars.ds_source;

			// System.out.println("R_DrawSpan: "+ds_x1+" to "+ds_x2+" at "+
			// ds_y);

			if (RANGECHECK) {
				doRangeCheck();
				// dscount++;
			}

			f_xfrac = dsvars.ds_xfrac;
			f_yfrac = dsvars.ds_yfrac;

			dest = ylookup[dsvars.ds_y] + columnofs[dsvars.ds_x1];

			// We do not check for zero spans here?
			count = dsvars.ds_x2 - dsvars.ds_x1;

			do {
				// Current texture index in u,v.
				spot = ((f_yfrac >> (16 - 6)) & (63 * 64))
						+ ((f_xfrac >> 16) & 63);

				// Lookup pixel from flat texture tile,
				// re-index using light/colormap.
				screen[dest++] = ds_colormap[0x00FF & ds_source[spot]];

				// Next step in u,v.
				f_xfrac += dsvars.ds_xstep;
				f_yfrac += dsvars.ds_ystep;

			} while (count-- > 0);
		}
	}

	public static final class TrueColor extends R_DrawSpan<byte[], int[]> {

		public TrueColor(int SCREENWIDTH, int SCREENHEIGHT, int[] ylookup,
				int[] columnofs, SpanVars<byte[], int[]> dsvars, int[] screen,
				IDoomSystem I) {
			super(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, dsvars,
					screen, I);
		}

		public void invoke() {

			int f_xfrac; // fixed_t
			int f_yfrac; // fixed_t
			int dest, count, spot;
			final int[] ds_colormap = dsvars.ds_colormap;
			final byte[] ds_source = dsvars.ds_source;

			// System.out.println("R_DrawSpan: "+ds_x1+" to "+ds_x2+" at "+
			// ds_y);

			if (RANGECHECK) {
				doRangeCheck();
				// dscount++;
			}

			f_xfrac = dsvars.ds_xfrac;
			f_yfrac = dsvars.ds_yfrac;

			dest = ylookup[dsvars.ds_y] + columnofs[dsvars.ds_x1];

			// We do not check for zero spans here?
			count = dsvars.ds_x2 - dsvars.ds_x1;

			do {
				// Current texture index in u,v.
				spot = ((f_yfrac >> (16 - 6)) & (63 * 64))
						+ ((f_xfrac >> 16) & 63);

				// Lookup pixel from flat texture tile,
				// re-index using light/colormap.
				screen[dest++] = ds_colormap[0x00FF & ds_source[spot]];

				// Next step in u,v.
				f_xfrac += dsvars.ds_xstep;
				f_yfrac += dsvars.ds_ystep;

			} while (count-- > 0);
		}
	}
}

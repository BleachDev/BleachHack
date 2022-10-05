/*
 * Copyright (C) 2017 Good Sign
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.bleachhack.util.doom.v.graphics;

import java.awt.image.IndexColorModel;
import static org.bleachhack.util.doom.v.tables.GammaTables.LUT;

/**
 * Refactored and included as the module of new software 2d graphics API
 * - Good Sign 2017/04/14
 *
 * Palettes & colormaps library
 * 
 * @author Good Sign
 * @author Maes
 */
public interface Palettes extends Lights {

    /**
     * Maximum number of colors in palette
     */
    static int PAL_NUM_COLORS = 256;
    
    /**
     * There is 256 colors in standard PALYPAL lump, 3 bytes for each color (RGB value)
     * totaling 256 * 3 = 768 bytes
     */
    final int PAL_NUM_STRIDES = 3;

    /**
     * Maximum number of palettes
     * PLAYPAL length / (PAL_NUM_COLORS * PAL_NUM_STRIDES)
     * 
     * TODO: think some way of support for future Hexen, Heretic, Strife palettes
     */
    static int NUM_PALETTES = 14;

    /**
     * Methods to be used by implementor
     */
    
    /** 
     * Perform any action necessary so that palettes get modified according to specified gamma.
     * Consider this a TIME CONSUMING operation, so don't call it unless really necessary.
     * 
     * @param gammalevel
     */
    void setUsegamma(int gammalevel);
    
    /**
     * Getter for gamma level
     * 
     * @return 
     */
    int getUsegamma();
    
    /** 
     * Perform any action necessary so that the screen output uses the specified palette
     * Consider this a TIME CONSUMING operation, so don't call it unless really necessary.
     * 
     * @param palette
     */
    void setPalette(int palette);
    
    /**
     * Getter for palette
     * 
     * @return 
     */
    int getPalette();
    
    /** 
     * Get the value corresponding to a base color (0-255).
     * Depending on the implementation this might be indexed,
     * RGB etc. Use whenever you need "absolute" colors.
     * 
     * @return int
     */
    int getBaseColor(byte color);

    default int getBaseColor(int color) { return getBaseColor((byte) color); }
    
    /**
     * Extracts RGB888 color from an index in the palette
     * @param byte[] pal proper playpal
     * @param int index and index of the color in the palette
     * @return int packed opaque rgb888 pixel
     */
    default int paletteToRGB888(byte[] pal, int index) {
        return toRGB888(pal[index], pal[index + 1], pal[index + 2]);
    }
    
    /**
     * Extracts RGB555 color from an index in the palette
     * @param byte[] pal proper playpal
     * @param int index and index of the color in the palette
     * @return int packed rgb555 pixel
     */
    default short paletteToRGB555(byte[] pal, int index) {
        return rgb888to555(pal[index], pal[index + 1], pal[index + 2]);
    }
    
    /**
     * Extracts RGB888 color components from an index in the palette to the container
     * @param byte[] pal proper playpal
     * @param byte index and index of the color in the palette
     * @param int[] container to hold individual RGB color components
     * @return int[] the populated container
     */
    default int[] getPaletteRGB888(byte[] pal, int index, int[] container) {
        container[0] = pal[index] & 0xFF;
        container[1] = pal[index + 1] & 0xFF;
        container[2] = pal[index + 2] & 0xFF;
        return container;
    }
    
    /**
     * ColorShiftPalette - lifted from dcolors.c Operates on RGB888 palettes in
     * separate bytes. at shift = 0, the colors are normal at shift = steps, the
     * colors are all the given rgb
     */
    default void ColorShiftPalette(byte[] inpal, byte[] outpal, int r, int g, int b, int shift, int steps) {
        int in_p = 0;
        int out_p = 0;
        for (int i = 0; i < PAL_NUM_COLORS; i++) {
            final int dr = r - inpal[in_p + 0];
            final int dg = g - inpal[in_p + 1];
            final int db = b - inpal[in_p + 2];
            outpal[out_p + 0] = (byte) (inpal[in_p + 0] + dr * shift / steps);
            outpal[out_p + 1] = (byte) (inpal[in_p + 1] + dg * shift / steps);
            outpal[out_p + 2] = (byte) (inpal[in_p + 2] + db * shift / steps);
            in_p += 3;
            out_p += 3;
        }
    }

    /**
     * Given raw palette data, returns an array with proper TrueColor data
     * @param byte[] pal proper palette
     * @return int[] 32 bit Truecolor ARGB colormap
     */
    default int[] paletteTrueColor(byte[] pal) {
        final int pal888[] = new int[PAL_NUM_COLORS];
        
        // Initial palette can be neutral or based upon "gamma 0",
        // which is actually a bit biased and distorted
        for (int x = 0; x < PAL_NUM_COLORS; ++x) {
            pal888[x] = paletteToRGB888(pal, x * PAL_NUM_STRIDES);
        }
        
        return pal888;
    }
    
    /**
     * Given raw palette data, returns an array with proper HiColor data
     * @param byte[] pal proper palette
     * @return short[] 16 bit HiColor RGB colormap
     */
    default short[] paletteHiColor(byte[] pal) {
        final short[] pal555 = new short[PAL_NUM_COLORS];

        // Apply gammas a-posteriori, not a-priori.
        // Initial palette can be neutral or based upon "gamma 0",
        // which is actually a bit biased and distorted
        for (int x = 0; x < PAL_NUM_COLORS; ++x) {
            pal555[x] = paletteToRGB555(pal, x * PAL_NUM_STRIDES);
        }
 
        return pal555;
    }

    /**
     * Given an array of certain length and raw palette data fills array
     * with IndexColorModel's for each palette. Gammas are applied a-priori
     * @param IndexColorModel[][] cmaps preallocated array, as it is often reconstructed for gamma, do not reallocate it
     * @param byte[] pal proper palette
     * @return the same araay as input, but all values set to new IndexColorModels
     */    
    default IndexColorModel[][] cmapIndexed(IndexColorModel icms[][], byte[] pal) {
        final int colorsXstride = PAL_NUM_COLORS * PAL_NUM_STRIDES;
        
        // Now we have our palettes.
        for (int i = 0; i < icms[0].length; ++i) {
            //new IndexColorModel(8, PAL_NUM_COLORS, pal, i * colorsXstride, false);
            icms[0][i] = createIndexColorModel(pal, i * colorsXstride);
        }

        // Wire the others according to the gamma table.
        final byte[] tmpcmap = new byte[colorsXstride];

        // For each gamma value...
        for (int j = 1; j < LUT.length; j++) {
            // For each palette
            for (int i = 0; i < NUM_PALETTES; i++) {
                for (int k = 0; k < PAL_NUM_COLORS; ++k) {
                    final int iXcolorsXstride_plus_StrideXk = i * colorsXstride + PAL_NUM_STRIDES * k;
                    tmpcmap[3 * k/**/] = (byte) LUT[j][0xFF & pal[/**/iXcolorsXstride_plus_StrideXk]]; // R
                    tmpcmap[3 * k + 1] = (byte) LUT[j][0xFF & pal[1 + iXcolorsXstride_plus_StrideXk]]; // G
                    tmpcmap[3 * k + 2] = (byte) LUT[j][0xFF & pal[2 + iXcolorsXstride_plus_StrideXk]]; // B
                }

                icms[j][i] = createIndexColorModel(tmpcmap, 0);
            }
        }

        return icms;
    }

    /**
     * @param byte[] cmap a colormap from which to make color model
     * @param int start position in colormap from which to take PAL_NUM_COLORS
     * @return IndexColorModel
     */
    default IndexColorModel createIndexColorModel(byte cmap[], int start) {
        return new IndexColorModel(8, PAL_NUM_COLORS, cmap, start, false);
    }
}

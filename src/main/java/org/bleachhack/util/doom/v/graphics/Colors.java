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
 */
package org.bleachhack.util.doom.v.graphics;

import org.bleachhack.util.doom.v.tables.ColorTint;

/**
 * Package containing individual color modification and transformation methods
 */
public interface Colors {
    /**
     * Get alpha from packed argb long word.
     *
     * @param argb8888
     * @return
     */
    default int getAlpha(int argb8888) {
        return (argb8888 >>> 24) & 0xFF;
    }
    
    /**
     * Get red from packed argb long word.
     *
     * @param rgb888
     * @return
     */
    default int getRed(int rgb888) {
        return (0xFF0000 & rgb888) >> 16;
    }

    /**
     * Get red from packed rgb555
     *
     * @param rgb555
     * @return
     */
    default int getRed5(int rgb555) {
        return (rgb555 >> 10) & 0x1F;
    }

    /**
     * Get green from packed argb long word.
     *
     * @param rgb888
     * @return
     */
    default int getGreen(int rgb888) {
        return (0xFF00 & rgb888) >> 8;
    }

    /**
     * Get green from packed rgb555
     *
     * @param rgb555
     * @return
     */
    default int getGreen5(int rgb555) {
        return (rgb555 >> 5) & 0x1F;
    }

    /**
     * Get blue from packed argb long word.
     *
     * @param rgb888
     * @return
     */
    default int getBlue(int rgb888) {
        return 0xFF & rgb888;
    }
    
    /**
     * Get blue from packed rgb555
     *
     * @param rgb555
     * @return
     */
    default int getBlue5(int rgb555) {
        return rgb555 & 0x1F;
    }

    /**
     * Get all four color channels into an array
     */
    default int[] getARGB8888(int argb8888, int[] container) {
        container[0] = getAlpha(argb8888);
        container[1] = getRed(argb8888);
        container[2] = getGreen(argb8888);
        container[3] = getBlue(argb8888);
        return container;
    }
    
    /**
     * Get all four color channels into an array
     */
    default int[] getRGB888(int rgb888, int[] container) {
        container[0] = getRed(rgb888);
        container[1] = getGreen(rgb888);
        container[2] = getBlue(rgb888);
        return container;
    }
    
    /**
     * Get all three colors into an array
     */
    default int[] getRGB555(int rgb555, int[] container) {
        container[0] = getRed5(rgb555);
        container[1] = getGreen5(rgb555);
        container[2] = getBlue5(rgb555);
        return container;
    }
    
    /**
     * Compose rgb888 color (opaque)
     */
    default int toRGB888(int r, int g, int b) {
        return 0xFF000000 + ((r & 0xFF) << 16) + ((g & 0xFF) << 8) + (b & 0xFF);
    }
    
    /**
     * Compose argb8888 color
     */
    default int toARGB8888(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) + ((r & 0xFF) << 16) + ((g & 0xFF) << 8) + (b & 0xFF);
    }
    
    /**
     * Compose rgb888 color
     */
    default short toRGB555(int r, int g, int b) {
        return (short) (((r & 0x1F) << 10) + ((g & 0x1F) << 5) + (b & 0x1F));
    }
    
    /**
     * Alter rgb888 color by applying a tint to it
     * @param int[] rgbInput an array containing rgb888 color components
     */
    default int[] tintRGB888(final ColorTint tint, final int[] rgbInput, int[] rgbOutput) {
        rgbOutput[0] = tint.tintRed8(rgbInput[0]);
        rgbOutput[1] = tint.tintGreen8(rgbInput[1]);
        rgbOutput[2] = tint.tintBlue8(rgbInput[2]);
        return rgbOutput;
    }

    /**
     * Alter rgb555 color by applying a tint to it
     * @param int[] rgbInput an array containing rgb555 color components
     */
    default int[] tintRGB555(final ColorTint tint, final int[] rgbInput, int[] rgbOutput) {
        rgbOutput[0] = tint.tintRed5(rgbInput[0]);
        rgbOutput[1] = tint.tintGreen5(rgbInput[1]);
        rgbOutput[2] = tint.tintBlue5(rgbInput[2]);
        return rgbOutput;
    }
    
    default double sigmoid(double r) {
        return (1 / (1 + Math.pow(Math.E, (-1 * r))));
    }
    
    default int sigmoidGradient(int component1, int component2, float ratio) {
        return (int) ((ratio * component1) + ((1 - ratio) * component2));
    }

    /**
     * Tells which color is further by comparing distance between two packed rgb888 ints
     */
    default int CompareColors888(int rgb888_1, int rgb888_2) {
        final long distance = ColorDistance888(rgb888_1, rgb888_2);
        return distance > 0 ? 1 : distance < 0 ? -1 : 0;
    }
    
    /**
     * Computes simplified Euclidean color distance (without extracting square root) between two packed rbg888 ints
     */
    default long ColorDistance888(int rgb888_1, int rgb888_2) {
        final int r1 = getRed(rgb888_1),
                g1 = getGreen(rgb888_1),
                b1 = getBlue(rgb888_1),
                r2 = getRed(rgb888_2),
                g2 = getGreen(rgb888_2),
                b2 = getBlue(rgb888_2);
        
        final long dr = r1 - r2, dg = g1 - g2, db = b1 - b2;
        return dr * dr + dg * dg + db * db;
    }

    /**
     * Tells which color is further by comparing hue, saturation, value distance between two packed rgb888 ints
     */
    default int CompareColorsHSV888(int rgb888_1, int rgb888_2) {
        final long distance = ColorDistanceHSV888(rgb888_1, rgb888_2);
        return distance > 0 ? 1 : distance < 0 ? -1 : 0;
    }
    
    /**
     * Computes simplified Euclidean color distance (without extracting square root) between two packed rbg888 ints
     * based on hue, saturation and value
     */
    default long ColorDistanceHSV888(int rgb888_1, int rgb888_2) {
        final int r1 = (int) (0.21 * getRed(rgb888_1)),
                  g1 = (int) (0.72 * getGreen(rgb888_1)),
                  b1 = (int) (0.07 * getBlue(rgb888_1)),
                  r2 = (int) (0.21 * getRed(rgb888_2)),
                  g2 = (int) (0.72 * getGreen(rgb888_2)),
                  b2 = (int) (0.07 * getBlue(rgb888_2));
        
        final long dr = r1 - r2, dg = g1 - g2, db = b1 - b2;
        return dr * dr + dg * dg + db * db;
    }

    /**
     * Tells which color is further by comparing distance between two packed rgb555 shorts
     */
    default int CompareColors555(short rgb555_1, short rgb555_2) {
        final long distance = ColorDistance555(rgb555_1, rgb555_2);
        return distance > 0 ? 1 : distance < 0 ? -1 : 0;
    }
    
    /**
     * Computes simplified Euclidean color distance (without extracting square root) between two packed rbg555 shorts
     */
    default long ColorDistance555(short rgb1, short rgb2) {
        final int r1 = getRed5(rgb1),
                  g1 = getGreen5(rgb1),
                  b1 = getBlue5(rgb1),
                  r2 = getRed5(rgb2),
                  g2 = getGreen5(rgb2),
                  b2 = getBlue5(rgb2);
        
        final long dr = r1 - r2, dg = g1 - g2, db = b1 - b2;
        return dr * dr + dg * dg + db * db;
    }

    /**
     * Tells which color is further by comparing hue, saturation, value distance between two packed rgb555 shorts
     */
    default int CompareColorsHSV555(short rgb555_1, short rgb555_2) {
        final long distance = ColorDistanceHSV555(rgb555_1, rgb555_2);
        return distance > 0 ? 1 : distance < 0 ? -1 : 0;
    }
    
    /**
     * Computes simplified Euclidean color distance (without extracting square root) between two packed rbg888 ints
     * based on hue, saturation and value
     */
    default long ColorDistanceHSV555(short rgb555_1, int rgb555_2) {
        final int r1 = (int) (0.21 * getRed5(rgb555_1)),
                  g1 = (int) (0.72 * getGreen5(rgb555_1)),
                  b1 = (int) (0.07 * getBlue5(rgb555_1)),
                  r2 = (int) (0.21 * getRed5(rgb555_2)),
                  g2 = (int) (0.72 * getGreen5(rgb555_2)),
                  b2 = (int) (0.07 * getBlue5(rgb555_2));
        
        final long dr = r1 - r2, dg = g1 - g2, db = b1 - b2;
        return dr * dr + dg * dg + db * db;
    }

    default float[] ColorRatio(int[] rgb1, int[] rgb2, float[] out) {
        for (int i = 0; i < 3; ++i) {
            out[i] = rgb2[i] > 0 ? rgb1[i] / (float) rgb2[i] : 1.0f;
        }
        return out;
    }
    
    /**
     * Get ARGB_8888 from RGB_555, with proper higher-bit
     * replication.
     *
     * @param rgb555
     * @return rgb888 packed int
     * @author velktron
     */
    default int rgb555to888(short rgb555) {
        // .... .... .... ....
        // 111 11 = 7C00
        // 11 111 = 03E0
        // 1F= 1 1111
        int ri = (0x7C00 & rgb555) >> 7;
        int gi = (0x3E0 & rgb555) >> 2;
        int bi = (0x1F & rgb555) << 3;
        // replicate 3 higher bits
        int bits = (ri & 224) >> 5;
        ri += bits;
        bits = (gi & 224) >> 5;
        gi += bits;
        bits = (bi & 224) >> 5;
        bi += bits;
        // ARGB 8888 packed
        return toRGB888(ri, gi, bi);
    }

    /**
     * Get RGB_555 from packed ARGB_8888.
     *
     * @param argb
     * @return rgb555 packed short
     * @authoor velktron
     */
    default short argb8888to555(int argb8888) {
        int ri = (0xFF010000 & argb8888) >> 19;
        int gi = (0xFF00 & argb8888) >> 11;
        int bi = (0xFF & argb8888) >> 3;
        return toRGB555(ri, gi, bi);
    }

    /**
     * Get packed RGB_555 word from individual 8-bit RGB components.
     *
     *  WARNING: there's no sanity/overflow check for performance reasons.
     *
     * @param r
     * @param g
     * @param b
     * @return rgb888 packed int
     * @author velktron
     */
    default short rgb888to555(int r, int g, int b) {
        return toRGB555(r >> 3, g >> 3, b >> 3);
    }
    
    /**
     * Finds a color in the palette's range from rangel to rangeh closest to specified r, g, b
     * by distortion, the lesst distorted color is the result. Used for rgb555 invulnerability colormap
     */
    default int BestColor(int r, int g, int b, int[] palette, int rangel, int rangeh) {
        /**
         * let any color go to 0 as a last resort
         */
        long bestdistortion = ((long) r * r + (long) g * g + (long) b * b) * 2;
        int bestcolor = 0;
        for (int i = rangel; i <= rangeh; i++) {
            final long dr = r - getRed(palette[i]);
            final long dg = g - getGreen(palette[i]);
            final long db = b - getBlue(palette[i]);
            final long distortion = dr * dr + dg * dg + db * db;
            if (distortion < bestdistortion) {
                if (distortion == 0) {
                    return i; // perfect match
                }
                bestdistortion = distortion;
                bestcolor = i;
            }
        }
        return bestcolor;
    }
}

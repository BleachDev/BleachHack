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
package org.bleachhack.util.doom.v.tables;

import java.util.TreeMap;
import org.bleachhack.util.doom.m.Settings;
import org.bleachhack.util.doom.mochadoom.Engine;
import org.bleachhack.util.doom.v.graphics.Colors;
import static org.bleachhack.util.doom.v.graphics.Lights.COLORMAP_BLURRY;
import static org.bleachhack.util.doom.v.graphics.Lights.COLORMAP_FIXED;
import static org.bleachhack.util.doom.v.graphics.Palettes.*;

/**
 * Colormap-friendly vanilla-like BlurryMap for HiColor && TrueColor modes
 * (though it shares plain "BLURRYMAP" for Indexed too)
 * 
 * DOOM's colormap #6 was deciphered to be actually applying greyscale averaging filter.
 * So, the vanilla effect is something like "n% darker and greyscale", where n% varies
 * I think I've succeeded in replicating it for real color modes
 *  - Good Sign 2017/04/15
 * 
 * Now should be 100%, I've accounted for shift of number of generated lights for 24 bit color
 * 
 * @author Good Sign
 */
public class BlurryTable implements FuzzMix, Colors {
    /**
     * Indexed LUT, e.g. classic "BLURRYMAP" (unaffected)
     */
    private final byte[] LUT_idx;

    private final byte[] LUT_r8;
    private final byte[] LUT_g8;
    private final byte[] LUT_b8;
    private final byte[] LUT_a8;

    private final byte[] LUT_r5;
    private final byte[] LUT_g5;
    private final byte[] LUT_b5;
    
    private final boolean semiTranslucent = Engine.getConfig().equals(Settings.semi_translucent_fuzz, Boolean.TRUE);
    private final boolean fuzzMix = Engine.getConfig().equals(Settings.fuzz_mix, Boolean.TRUE);
    
    /**
     * Only support indexed "BLURRYMAP" with indexed colorMap
     * @param colorMap 
     */
    public BlurryTable(byte[][] colorMap) {
        this.LUT_b5 = null;
        this.LUT_g5 = null;
        this.LUT_r5 = null;
        this.LUT_b8 = null;
        this.LUT_g8 = null;
        this.LUT_r8 = null;
        this.LUT_a8 = null;
        this.LUT_idx = colorMap[COLORMAP_BLURRY];
    }

    /**
     * HiColor BlurryTable will only support int[][] colormap
     * @param liteColorMaps
     */
    public BlurryTable(short[][] liteColorMaps) {
        this.LUT_b5 = new byte[32];
        this.LUT_g5 = new byte[32];
        this.LUT_r5 = new byte[32];
        this.LUT_b8 = null;
        this.LUT_g8 = null;
        this.LUT_r8 = null;
        this.LUT_a8 = null;
        this.LUT_idx = null;

        /**
         * Prepare to sort colors - we will be using the ratio that is next close to apply for current color
         */
        final TreeMap<Short, Float> sortedRatios = new TreeMap<>(this::CompareColors555);
        
        for (int i = 0; i < PAL_NUM_COLORS; ++i) {
            // first get "BLURRYMAP" color components
            final int[] blurryColor = getRGB555(liteColorMaps[COLORMAP_BLURRY][i], new int[3]);
            // then gen color components from unmodified (fixed) palette
            final int[] fixedColor = getRGB555(liteColorMaps[COLORMAP_FIXED][i], new int[3]);
            // make grayscale avegrage (or what you set in cfg) colors out of these components
            final short avgColor = GreyscaleFilter.grey555(blurryColor[0], blurryColor[1], blurryColor[2]);
            final short avgOrig = GreyscaleFilter.grey555(fixedColor[0], fixedColor[1], fixedColor[2]);
            // get grayscale color components
            final int[] blurryAvg = getRGB555(avgColor, new int[3]);
            final int[] fixedAvg = getRGB555(avgOrig, new int[3]);
            
            // now, calculate the ratios
            final float ratioR = fixedAvg[0] > 0 ? blurryAvg[0] / (float) fixedAvg[0] : 0.0f,
                        ratioG = fixedAvg[1] > 0 ? blurryAvg[1] / (float) fixedAvg[1] : 0.0f,
                        ratioB = fixedAvg[2] > 0 ? blurryAvg[2] / (float) fixedAvg[2] : 0.0f;
            
            // best ratio is weighted towards red and blue, but should not be multiplied or it will be too dark
            final float bestRatio = GreyscaleFilter.component(ratioR, ratioG, ratioB);//ratioR * ratioR * ratioG * ratioB * ratioB;
            
            // associate normal color from colormaps avegrage with this ratio
            sortedRatios.put(avgOrig, bestRatio);
        }
        
        // now we have built our sorted maps, time to calculate color component mappings
        for (int i = 0; i <= 0x1F; ++i) {
            final short rgb555 = toRGB555(i, i, i);
            // now the best part - approximation. we just pick the closest grayscale color ratio
            final float ratio = sortedRatios.floorEntry(rgb555).getValue();
            LUT_r5[i] = LUT_g5[i] = LUT_b5[i] = (byte) ((int)(i * ratio) & 0x1F);
        }
        // all done
    }
    
    /**
     * TrueColor BlurryTable will only support int[][] colormap
     * @param liteColorMaps
     */
    public BlurryTable(int[][] liteColorMaps) {
        this.LUT_b5 = null;
        this.LUT_g5 = null;
        this.LUT_r5 = null;
        this.LUT_a8 = new byte[256];
        this.LUT_b8 = new byte[256];
        this.LUT_g8 = new byte[256];
        this.LUT_r8 = new byte[256];
        this.LUT_idx = null;
        
        /**
         * Prepare to sort colors - we will be using the ratio that is next close to apply for current color
         */
        final TreeMap<Integer, Float> sortedRatios = new TreeMap<>(this::CompareColors888);
        
        for (int i = 0; i < PAL_NUM_COLORS; ++i) {
            // first get "BLURRYMAP" color components. 24 bit lighting is richer (256 vs 32) so we need to multiply
            final int[] blurryColor = getRGB888(liteColorMaps[COLORMAP_BLURRY << 3][i], new int[3]);
            // then gen color components from unmodified (fixed) palette
            final int[] fixedColor = getRGB888(liteColorMaps[COLORMAP_FIXED][i], new int[3]);
            // make grayscale avegrage (or what you set in cfg) colors out of these components
            final int avgColor = GreyscaleFilter.grey888(blurryColor[0], blurryColor[1], blurryColor[2]);
            final int avgOrig = GreyscaleFilter.grey888(fixedColor[0], fixedColor[1], fixedColor[2]);
            // get grayscale color components
            final int[] blurryAvg = getRGB888(avgColor, new int[3]);
            final int[] fixedAvg = getRGB888(avgOrig, new int[3]);
            
            // now, calculate the ratios
            final float ratioR = fixedAvg[0] > 0 ? blurryAvg[0] / (float) fixedAvg[0] : 0.0f,
                        ratioG = fixedAvg[1] > 0 ? blurryAvg[1] / (float) fixedAvg[1] : 0.0f,
                        ratioB = fixedAvg[2] > 0 ? blurryAvg[2] / (float) fixedAvg[2] : 0.0f;
            
            // weight ratio towards red and blue and multiply to make darker
            final float bestRatio = GreyscaleFilter.component(ratioR, ratioG, ratioB);//ratioR * ratioR * ratioG * ratioB * ratioB;
            
            // associate normal color from colormaps avegrage with this ratio
            sortedRatios.put(avgOrig, bestRatio);
        }
        
        // now we have built our sorted maps, time to calculate color component mappings
        for (int i = 0; i <= 0xFF; ++i) {
            final int rgb = toRGB888(i, i, i);
            // now the best part - approximation. we just pick the closest grayscale color ratio
            final float ratio = sortedRatios.floorEntry(rgb).getValue();
            LUT_r8[i] = LUT_g8[i] = LUT_b8[i] = (byte) ((int)(i * ratio) & 0xFF);
            // for alpha it is different: we use the same ratio as for greyscale color, but the base alpha is min 50%
            LUT_a8[i] = (byte) (ratio * (Math.max(i, 0x7F) / (float) 0xFF) * 0xFF);
        }
        // all done
    }
    
    /**
     * For indexes
     */
    public byte computePixel(byte pixel) {
        return LUT_idx[pixel & 0xFF];
    }

    /**
     * For HiColor pixels
     */
    public short computePixel(short pixel) {
        if (fuzzMix) { // if blurry feature enabled, everything else does not apply
            return fuzzMixHi(pixel);
        }
        final int rgb[] = getRGB555(pixel, new int[4]);
        return toRGB555(LUT_r5[rgb[0]], LUT_g5[rgb[1]], LUT_b5[rgb[2]]);
    }
    
    /**
     * In high detail mode in AlphaTrueColor color mode will compute special greyscale-to-ratio translucency
     */
    public int computePixel(int pixel) {
        if (fuzzMix) { // if blurry feature enabled, everything else does not apply
            return fuzzMixTrue(pixel);
        }
            
        if (!semiTranslucent) {
            return computePixelFast(pixel);
        }
        final int argb[] = getARGB8888(pixel, new int[4]);
        // the alpha from previous frame would stay until the pixel will not belong to FUZZ holder
        argb[0] = Math.min(argb[0], GreyscaleFilter.component(argb[1], argb[2], argb[3]));
        return toARGB8888(LUT_a8[argb[0]], LUT_r8[argb[1]], LUT_g8[argb[2]], LUT_b8[argb[3]]);
    }
    
    /**
     * For low detail mode, do not compute translucency
     */
    public int computePixelFast(int pixel) {
        if (fuzzMix) { // if blurry feature enabled, everything else does not apply
            return fuzzMixTrueLow(pixel);
        }
            
        final int rgb[] = getRGB888(pixel, new int[3]);
        return 0xFF000000 + (toRGB888(LUT_r8[rgb[0]], LUT_g8[rgb[1]], LUT_b8[rgb[2]]) & 0xFFFFFF);
    }
}

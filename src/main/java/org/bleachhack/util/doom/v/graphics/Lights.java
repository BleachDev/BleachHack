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

import static org.bleachhack.util.doom.v.graphics.Palettes.PAL_NUM_COLORS;
import org.bleachhack.util.doom.v.tables.GreyscaleFilter;

/**
 * This package org.bleachhack.util.doom.provides methods to dynamically generate lightmaps
 * They are intended to be used instead of COLORMAP lump to
 * compute sector brightness
 * 
 * @author Good Sign
 * @author John Carmack
 * @author Velktron
 */
public interface Lights extends Colors {
    /**
     * Light levels. Binded to the colormap subsystem
     */
    final int COLORMAP_LIGHTS_15 = 1 << 5;
    final int COLORMAP_LIGHTS_24 = 1 << 8;
    
    /**
     * Standard lengths for colormaps
     */
    final int COLORMAP_STD_LENGTH_15 = COLORMAP_LIGHTS_15 + 1;
    final int COLORMAP_STD_LENGTH_24 = COLORMAP_LIGHTS_24 + 1;

    /**
     * Default index of inverse colormap. Note that it will be shifted to the actual position
     * in generated lights map by the difference in lights count between 5 and 8 bits lighting.
     * I have discovered, that player_t.fixedcolormap property is *stored* by game when writing files,
     * for example it could be included in savegame or demos.
     * 
     * If we preshift inverse colormap, MochaDoom not in TrueColor bppMode or any Vanilla DOOM would crash
     * when trying to load savegame made when under invulnerabilty in TrueColor bppMode.
     *  - Good Sign 2017/04/15
     */
    final int COLORMAP_INVERSE = 32;
    
    /**
     * An index of of the lighted palette in colormap used for FUZZ effect and partial invisibility
     */
    final int COLORMAP_BLURRY = 6;

    /**
     * An index of of the most lighted palette in colormap
     */
    final int COLORMAP_BULLBRIGHT = 1;

    /**
     * An index of of palette0 in colormap which is not altered
     */
    final int COLORMAP_FIXED = 0;

    /**
     * A difference in percents between color multipliers of two adjacent light levels
     * It took sometime to dig this out, and this could be possibly used to simplify
     * BuildLight functions without decrease in their perfectness
     * 
     * The formula to apply to a color will then be:
     *  float ratio = 1.0f - LIGHT_INCREMENT_RATIO_24 * lightLevel;
     *  color[0] = (int) (color[0] * ratio + 0.5)
     *  color[1] = (int) (color[1] * ratio + 0.5)
     *  color[2] = (int) (color[2] * ratio + 0.5)
     * 
     * However, this one is untested, and existing formula in function AddLight8 does effectively the same,
     * just a little slower.
     * 
     *  - Good Sign 2017/04/17
     */
    final float LIGHT_INCREMENT_RATIO_24 = 1.0f / COLORMAP_LIGHTS_24;
    
    /**
     * Builds TrueColor lights based on standard COLORMAP lump in DOOM format
     * Currently only supports lightmap manipulation, but does not change colors
     * for hacked COLORMAP lumps
     * 
     * Color indexes in colormaps on darker color levels point to less matching
     * colors so only the direction of increase/decrease of lighting is actually
     * used from COLORMAP lump. Everything else is computed based on PLAYPAL
     * 
     * @param int[] palette A packed RGB888 256-entry int palette
     * @param byete[][] colormap read from COLORMAP lump
     * @author Good Sign
     */
    default int[][] BuildLights24(int[] palette, byte[][] colormap) {
        final int[][] targetColormap = new int[
            Math.max(colormap.length, COLORMAP_STD_LENGTH_15) - COLORMAP_LIGHTS_15 + COLORMAP_LIGHTS_24
        ][PAL_NUM_COLORS];
        
        // init operation containers
        final int[] color0 = new int[3], color1 = new int[3], color2 = new int[3];
        final float[] ratio0 = new float[3];
        float weight = 0.0f;
        
        /**
         * Fixed color map - just copy it, only translating palette to real color
         * It is presumably the brightest colormap, but maybe not: we shall check weight of color ratios
         */
        for (int i = 0; i < PAL_NUM_COLORS; ++i) {
            targetColormap[0][i] = palette[colormap[0][i] & 0xFF];
            getRGB888(targetColormap[0][i], color0);
            getRGB888(palette[i], color1);
            // calculate color ratio
            ColorRatio(color0, color1, ratio0);
            // add average ratio to the weight
            weight += GreyscaleFilter.component(ratio0[0], ratio0[1], ratio0[2]);
        }
        
        // initialize ratio to relate weight with number of colors, with default PLAYPAL should always be 1.0f
        float currentLightRatio = Math.min(weight / PAL_NUM_COLORS, 1.0f);
        
        // [1 .. 255]: all colormaps except 1 fixed, 1 inverse and 1 unused
        for (int i = 1; i < COLORMAP_LIGHTS_24; ++i) {
            // [1 .. 31] the index of the colormap to be target for gradations: max 31 of ceiling of i / 8
            final int div = (int) Math.ceil((double) i / 8);
            final int target = Math.min(div, COLORMAP_LIGHTS_15 - 1);
            final int remainder = div < COLORMAP_LIGHTS_15 ? i % 8 : 0;
            final float gradient = 1.0f - remainder * 0.125f;
            
            // calculate weight again for each colormap
            weight = 0.0f;
            for (int j = 0; j < PAL_NUM_COLORS; ++j) {
                // translated indexed color from wad-read colormap i at position j
                getRGB888(palette[colormap[target][j] & 0xFF], color0);
                // translated indexed color from our previous generated colormap at position j
                getRGB888(targetColormap[i - 1][j], color1);
                // calculate color ratio
                ColorRatio(color0, color1, ratio0);
                // add average ratio to the weight
                weight += GreyscaleFilter.component(ratio0[0], ratio0[1], ratio0[2]);
                // to detect which color we will use, get the fixed colormap one
                getRGB888(targetColormap[0][j], color2);
                
                /**
                 * set our color using smooth TrueColor formula: we well use the brighter color as a base
                 * since the brighter color simply have more information not omitted
                 * if we are going up in brightness, not down, it will be compensated by ratio
                 */
                targetColormap[i][j] = toRGB888(
                    sigmoidGradient(color1[0], (int) (Math.max(color2[0], color0[0]) * currentLightRatio + 0.5), gradient),
                    sigmoidGradient(color1[1], (int) (Math.max(color2[1], color0[1]) * currentLightRatio + 0.5), gradient),
                    sigmoidGradient(color1[2], (int) (Math.max(color2[2], color0[2]) * currentLightRatio + 0.5), gradient)
                );
            }
            
            // now detect if we are lightening or darkening
            currentLightRatio += weight > PAL_NUM_COLORS ? LIGHT_INCREMENT_RATIO_24 : -LIGHT_INCREMENT_RATIO_24;
        }
        
        // copy all other parts of colormap
        for (int i = COLORMAP_LIGHTS_24, j = COLORMAP_LIGHTS_15; j < colormap.length; ++i, ++j) {
            CopyMap24(targetColormap[i], palette, colormap[j]);
        }
        
        return targetColormap;
    }

    /**
     * RF_BuildLights lifted from dcolors.c
     *
     * Used to compute extended-color colormaps even in absence of the
     * COLORS15 lump. Must be recomputed if gamma levels change, since
     * they actually modify the RGB envelopes.
     *
     * Variation that produces TrueColor lightmaps
     *
     * @param int[] palette A packed RGB888 256-entry int palette
     */
    default int[][] BuildLights24(int[] palette) {
        final int[][] targetColormap = new int[COLORMAP_STD_LENGTH_24][PAL_NUM_COLORS];
        final int[] palColor = new int[3];
        
        // Don't repeat work more then necessary - loop first over colors, not lights
        for (int c = 0; c < PAL_NUM_COLORS; ++c) {
            getRGB888(palette[c], palColor);
            for (int l = 0; l < COLORMAP_LIGHTS_24; ++l) {
                // Full-quality truecolor.
                targetColormap[l][c] = toRGB888(
                    AddLight8(palColor[0], l), // R
                    AddLight8(palColor[1], l), // G
                    AddLight8(palColor[2], l) // B
                );
            }
        
            // Special map for invulnerability. Do not waste time, build it right now
            BuildSpecials24(targetColormap[COLORMAP_LIGHTS_24], palColor, c);
        }
        
        return targetColormap;
    }

    /**
     * RF_BuildLights lifted from dcolors.c
     *
     * Used to compute extended-color colormaps even in absence of the
     * COLORS15 lump. Must be recomputed if gamma levels change, since
     * they actually modify the RGB envelopes.
     *
     * @param int[] palette A packed RGB888 256-entry int palette
     * @param byte[][] colormap, if supplied it will be used to translate the lights,
     * the inverse colormap will be translated from it and all unused copied.
     *  - Good Sign 2017/04/17
     */
    default short[][] BuildLights15(int[] palette, byte[][] colormaps) {
        final short[][] targetColormap = new short[Math.max(colormaps.length, COLORMAP_STD_LENGTH_15)][PAL_NUM_COLORS];
        
        for (int c = 0; c < colormaps.length; ++c) {
            CopyMap15(targetColormap[c], palette, colormaps[c]);
        }

        return targetColormap;
    }
    
    /**
     * RF_BuildLights lifted from dcolors.c
     *
     * Used to compute extended-color colormaps even in absence of the
     * COLORS15 lump. Must be recomputed if gamma levels change, since
     * they actually modify the RGB envelopes.
     *
     * @param int[] palette A packed RGB888 256-entry int palette
     */
    default short[][] BuildLights15(int[] palette) {
        final short[][] targetColormap = new short[COLORMAP_STD_LENGTH_15][PAL_NUM_COLORS];
        final int[] palColor = new int[3];
        
        // Don't repeat work more then necessary - loop first over colors, not lights
        for (int c = 0; c < PAL_NUM_COLORS; ++c) {
            getRGB888(palette[c], palColor);
            for (int l = 0; l < COLORMAP_LIGHTS_15; ++l) {
                // RGB555 for HiColor, eight times less smooth then TrueColor version
                targetColormap[l][c] = toRGB555(
                    AddLight5(palColor[0], l), // R
                    AddLight5(palColor[1], l), // G
                    AddLight5(palColor[2], l) // B
                );
            }

            // Special map for invulnerability. Do not waste time, build it right now
            BuildSpecials15(targetColormap[COLORMAP_LIGHTS_15], palColor, c);
        }

        return targetColormap;
    }
    
    /**
     * RF_BuildLights lifted from dcolors.c
     *
     * Used to compute extended-color colormaps even in absence of the
     * COLORMAP lump. Must be recomputed if gamma levels change, since
     * they actually modify the RGB envelopes.
     *
     * @param int[] palette A packed RGB888 256-entry int palette
     * @return this concrete one builds Indexed colors. Maybe I would regret it
     *  - Good Sign 2017/04/19
     */
    default byte[][] BuildLightsI(int[] palette) {
        final byte[][] targetColormap = new byte[COLORMAP_STD_LENGTH_15][PAL_NUM_COLORS];
        final int[] palColor = new int[3];
        
        // Don't repeat work more then necessary - loop first over colors, not lights
        for (int c = 0; c < PAL_NUM_COLORS; ++c) {
            getRGB888(palette[c], palColor);
            for (int l = 0; l < COLORMAP_LIGHTS_15; ++l) {
                // RGB555 for HiColor, eight times less smooth then TrueColor version
                targetColormap[l][c] = (byte) BestColor(
                    AddLightI(palColor[0], l), // R
                    AddLightI(palColor[1], l), // G
                    AddLightI(palColor[2], l), // B
                    palette, 0, PAL_NUM_COLORS - 1
                );
            }

            // Special map for invulnerability. Do not waste time, build it right now
            BuildSpecialsI(targetColormap[COLORMAP_LIGHTS_15], palColor, palette, c);
        }

        return targetColormap;
    }

    /**
     * @param c8 one rgb888 color component value
     * @param light light level to add
     * @return one rgb888 component value with added light level
     */
    default int AddLight8(int c8, int light) {
        return (int) (c8 * (1 - (float) light / COLORMAP_LIGHTS_24) + 0.5);
    }

    /**
     * @param c8 one rgb888 color component value (not a mistake - input is rgb888)
     * @param light light level to add
     * @return one rgb555 component value with added light level
     */
    default int AddLight5(int c8, int light) {
        return ((int) (c8 * (1 - (float) light / COLORMAP_LIGHTS_15) + 0.5)) >> 3;
    }

    /**
     * @param c8 one rgb888 color component value (not a mistake - input is rgb888)
     * @param light light level to add
     * @return one rgb555 component value with added light level
     */
    default int AddLightI(int c8, int light) {
        return (int) (c8 * (1 - (float) light / COLORMAP_LIGHTS_15) + 0.5);
    }
    
    /**
     * Decides the size of array for colormap and creates it
     * @param hasColormap whether the array have lump-read colormap
     * @param an array that can possibly have colormap read from COLORMAP lump
     * @return empty array for colormap
     */
    default int[][] AllocateColormap24(final boolean hasColormap, byte[][][] colormap) {
        // if the lump-read COLORMAP is shorter, we must allocate enough
        final int targetLength = hasColormap
            ? COLORMAP_STD_LENGTH_24 + Math.max(0, colormap[0].length - COLORMAP_STD_LENGTH_15)
            : COLORMAP_STD_LENGTH_24;
        
        final int[][] targetColormap = new int[targetLength][PAL_NUM_COLORS];
        return targetColormap;
    }
    
    /**
     * Decides the size of array for colormap and creates it
     * @param hasColormap whether the array have lump-read colormap
     * @param an array that can possibly have colormap read from COLORMAP lump
     * @return empty array for colormap
     */
    default short[][] AllocateColormap15(final boolean hasColormap, byte[][][] colormap) {
        // if the lump-read COLORMAP is shorter, we must allocate enough
        final int targetLength = hasColormap
            ? Math.max(COLORMAP_STD_LENGTH_15, colormap[0].length)
            : COLORMAP_STD_LENGTH_15;
        
        final short[][] targetColormap = new short[targetLength][PAL_NUM_COLORS];
        return targetColormap;
    }
    
    /**
     * Copy selected colormap from COLORMAP lump with respect to palette
     * @param int[] stuff a 256-entry part of target colormap
     * @param int[] palette A packed RGB888 256-entry int palette
     * @param byte[] map a 256-entry part of COLORMAP lump to copy
     */
    default void CopyMap24(int[] targetColormap, int[] palette, byte[] map) {
        for (int c = 0; c < PAL_NUM_COLORS; ++c) {
            targetColormap[c] = palette[map[c] & 0xFF];
        }
    }

    /**
     * Copy selected colormap from COLORMAP lump with respect to palette
     * @param short[] stuff a 256-entry part of target colormap
     * @param int[] palette A packed RGB888 256-entry int palette
     * @param byte[] map a 256-entry part of COLORMAP lump to copy
     */
    default void CopyMap15(short[] targetColormap, int[] palette, byte[] map) {
        final int[] palColor = new int[3];
        for (int c = 0; c < PAL_NUM_COLORS; ++c) {
            getRGB888(palette[map[c] & 0xFF], palColor);
            targetColormap[c] = rgb888to555(palColor[0], palColor[1], palColor[2]);
        }
    }

    /**
     * TrueColor invulnerability specials
     * The key is: get the color, compute its luminance (or other method of grey if set in cfg)
     * and substract it from white
     * 
     * @param int[] stuff target array to set into
     * @param int[] rgb unpacked color components
     * @param index an index of the color int 256-entry int palette
     */
    default void BuildSpecials24(int[] targetColormap, int[] rgb, int index) {
        final float luminance = GreyscaleFilter.component((float) rgb[0], rgb[1], rgb[2]);
        final int grey = (int) (255 * (1.0 - luminance / PAL_NUM_COLORS));
        targetColormap[index] = toRGB888(grey, grey, grey);
    }

    /**
     * HiColor invulnerability specials
     * The key is: get the color, compute its luminance (or other method of grey if set in cfg)
     * and substract it from white
     * 
     * @param short[] stuff target array to set into
     * @param int[] rgb unpacked color components
     * @param index an index of the color int 256-entry int palette
     */
    default void BuildSpecials15(short[] targetColormap, int[] rgb, int index) {
        final float luminance = GreyscaleFilter.component((float) rgb[0], rgb[1], rgb[2]);
        final int grey = (int) (255 * (1.0 - luminance / PAL_NUM_COLORS));
        targetColormap[index] = toRGB555(grey >> 3, grey >> 3, grey >> 3);
    }

    /**
     * Indexed invulnerability specials
     * The key is: get the color, compute its luminance (or other method of grey if set in cfg)
     * and substract it from white
     * 
     * @param byte[] stuff target array to set into
     * @param int[] rgb unpacked color components
     * @param index an index of the color int 256-entry int palette
     */
    default void BuildSpecialsI(byte[] targetColormap, int[] rgb, int[] palette, int index) {
        final float luminance = GreyscaleFilter.component((float) rgb[0], rgb[1], rgb[2]);
        final int grey = (int) (255 * (1.0 - luminance / PAL_NUM_COLORS));
        targetColormap[index] = (byte) BestColor(grey, grey, grey, palette, 0, PAL_NUM_COLORS - 1);
    }
}

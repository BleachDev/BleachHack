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

/**
 * FuzzMix: Unique feature by Maes for HiColor detailed mode
 * This should be preserved, but I've moved it to appropriate place
 * Option to enable the feature in cfg: fuzz_mix
 * 
 * Note: the TrueColor alpha half-brite will only work
 * properly with cfg: color_depth AlphaTrueColor also set
 * 
 * Made it an interface, it is as easy to apply to anything as setting pixel
 *  - Good Sign 2017/04/16
 * 
 * @author velktron
*/
public interface FuzzMix {
    /**
     * Was used by:
     * R_DrawFuzzColumn.HiColor
     * R_DrawFuzzColumnLow.HiColor
     * 
     * Now used by BlurryTable::computePixel
     * only if the option fuzz_mix enabled
     */
    default short fuzzMixHi(short rgb){
        // super-fast half-brite trick
        // 3DEF and >> 1: ok hue, but too dark
        // 7BDE, no shift:  good compromise
        // 739C, no shift: results in too obvious tinting.         
        return (short) (rgb & 0x7BDE);
    }

    /**
     * Was used by:
     * R_DrawFuzzColumn.TrueColor
     * 
     * Now used by BlurryTable::computePixel
     * only if the option fuzz_mix enabled
     * 
     * AX: This is what makes it blurry
     */
    default int fuzzMixTrue(int rgb){
        // Proper half-brite alpha!
        return rgb & 0x10FFFFFF;
    }

    /**
     * Was used by:
     * R_DrawFuzzColumnLow.TrueColor
     * 
     * Now used by BlurryTable::computePixel
     * only if the option fuzz_mix enabled
     * 
     * AX: This is what made it dark and ugly
     */
    default int fuzzMixTrueLow(int rgb) {
        // super-fast half-brite trick
        // 3DEF and >> 1: ok hue, but too dark
        // FF7C7C7C, no shift: good compromise
        // FF707070, no shift: results in too obvious tinting.

        return (rgb >> 1) & 0xFF7F7F7F;
    }
}

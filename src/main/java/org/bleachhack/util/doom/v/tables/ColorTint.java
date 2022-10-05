/**
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Default generated tints for berserk, radsuit, bonus pickup and so on.
 * I think they may be invalid if the game uses custom COLORMAP, so we need an ability
 * to regenerate them when loading such lump.
 * Thus, it is an Enum... but only almost.
 * 
 * Added new LUT's for HiColor and TrueColor renderers
 * They are capable of tinting and gamma correcting full direct colors(not indexed) on the fly
 *  - Good Sign
 */
public class ColorTint {
    public final static ColorTint
        NORMAL = new ColorTint(0, 0, 0, .0f),
        RED_11 = new ColorTint(255, 2, 3, 0.11f),
        RED_22 = new ColorTint(255, 0, 0, 0.22f),
        RED_33 = new ColorTint(255, 0, 0, 0.33f),
        RED_44 = new ColorTint(255, 0, 0, 0.44f),
        RED_55 = new ColorTint(255, 0, 0, 0.55f),
        RED_66 = new ColorTint(255, 0, 0, 0.66f),
        RED_77 = new ColorTint(255, 0, 0, 0.77f),
        RED_88 = new ColorTint(255, 0, 0, 0.88f),
        BERSERK_SLIGHT = new ColorTint(215, 185, 68, 0.12f),
        BERSERK_SOMEWHAT = new ColorTint(215, 185, 68, 0.25f),
        BERSERK_NOTICABLE = new ColorTint(215, 185, 68, 0.375f),
        BERSERK_HEAVY = new ColorTint(215, 185, 68, 0.50f),
        RADSUIT = new ColorTint(3, 253, 3, 0.125f),

        GREY_NORMAL = new ColorTint(NORMAL.mid(), NORMAL.mid5(), NORMAL.purepart),
        GREY_RED_11 = new ColorTint(RED_11.mid(), RED_11.mid5(), RED_11.purepart),
        GREY_RED_22 = new ColorTint(RED_22.mid(), RED_22.mid5(), RED_22.purepart),
        GREY_RED_33 = new ColorTint(RED_33.mid(), RED_33.mid5(), RED_33.purepart),
        GREY_RED_44 = new ColorTint(RED_44.mid(), RED_44.mid5(), RED_44.purepart),
        GREY_RED_55 = new ColorTint(RED_55.mid(), RED_55.mid5(), RED_55.purepart),
        GREY_RED_66 = new ColorTint(RED_66.mid(), RED_66.mid5(), RED_66.purepart),
        GREY_RED_77 = new ColorTint(RED_77.mid(), RED_77.mid5(), RED_77.purepart),
        GREY_RED_88 = new ColorTint(RED_88.mid(), RED_88.mid5(), RED_88.purepart),
        GREY_BERSERK_SLIGHT = new ColorTint(BERSERK_SLIGHT.mid(), BERSERK_SLIGHT.mid5(), BERSERK_SLIGHT.purepart),
        GREY_BERSERK_SOMEWHAT = new ColorTint(BERSERK_SOMEWHAT.mid(), BERSERK_SOMEWHAT.mid5(), BERSERK_SOMEWHAT.purepart),
        GREY_BERSERK_NOTICABLE = new ColorTint(BERSERK_NOTICABLE.mid(), BERSERK_NOTICABLE.mid5(), BERSERK_NOTICABLE.purepart),
        GREY_BERSERK_HEAVY = new ColorTint(BERSERK_HEAVY.mid(), BERSERK_HEAVY.mid5(), BERSERK_HEAVY.purepart),
        GREY_RADSUIT = new ColorTint(RADSUIT.mid(), RADSUIT.mid5(), RADSUIT.purepart);
    
    public static final List<ColorTint> NORMAL_TINTS = Collections.unmodifiableList(Arrays.asList(
        NORMAL,
        RED_11, RED_22, RED_33, RED_44, RED_55, RED_66, RED_77, RED_88,
        BERSERK_SLIGHT, BERSERK_SOMEWHAT, BERSERK_NOTICABLE, BERSERK_HEAVY, RADSUIT
    ));
    
    public static final List<ColorTint> GREY_TINTS = Collections.unmodifiableList(Arrays.asList(
        GREY_NORMAL,
        GREY_RED_11, GREY_RED_22, GREY_RED_33, GREY_RED_44, GREY_RED_55, GREY_RED_66, GREY_RED_77, GREY_RED_88,
        GREY_BERSERK_SLIGHT, GREY_BERSERK_SOMEWHAT, GREY_BERSERK_NOTICABLE, GREY_BERSERK_HEAVY, GREY_RADSUIT
    ));
    
    /*public static List<ColorTint> generateTints(byte cmaps[][]) {
    }*/
    
    ColorTint(int r, int g, int b, float tint) {
        this(r * tint, (r >> 3) * tint, g * tint, (g >> 3) * tint, b * tint, (b >> 3) * tint, 1 - tint);
    }

    ColorTint(float mid8, float mid5, float purepart) {
        this(mid8, mid5, mid8, mid5, mid8, mid5, purepart);
    }
    
    ColorTint(float r, float r5, float g, float g5, float b, float b5, float purepart) {
        this.r = r;
        this.r5 = r5;
        this.g = g;
        this.g5 = g5;
        this.b = b;
        this.b5 = b5;
        this.purepart = purepart;
        for (int j = 0; j < GammaTables.LUT.length; ++j) {
            for (int i = 0; i <= 0xFF; ++i) {
                LUT_r8[j][i] = (byte) GammaTables.LUT[j][tintRed8(i)];
                LUT_g8[j][i] = (byte) GammaTables.LUT[j][tintGreen8(i)];
                LUT_b8[j][i] = (byte) GammaTables.LUT[j][tintBlue8(i)];
                if (i <= 0x1F) {
                    LUT_r5[j][i] = (byte) (GammaTables.LUT[j][tintRed5(i) << 3] >> 3);
                    LUT_g5[j][i] = (byte) (GammaTables.LUT[j][tintGreen5(i) << 3] >> 3);
                    LUT_b5[j][i] = (byte) (GammaTables.LUT[j][tintBlue5(i) << 3] >> 3);
                }
            }
        }
    }

    private final float r, g, b;
    private final float r5, g5, b5;
    private final float purepart;
    public final byte[][] LUT_r8 = new byte[5][0x100];
    public final byte[][] LUT_g8 = new byte[5][0x100];
    public final byte[][] LUT_b8 = new byte[5][0x100];
    public final byte[][] LUT_r5 = new byte[5][0x20];
    public final byte[][] LUT_g5 = new byte[5][0x20];
    public final byte[][] LUT_b5 = new byte[5][0x20];
    
    public float mid() {
        return (r + g + b) / 3;
    }

    public float mid5() {
        return (r5 + g5 + b5) / 3;
    }

    public final int tintGreen8(int green8) {
        return Math.min((int) (green8 * purepart + g), 0xFF);
    }

    public final int tintGreen5(int green5) {
        return Math.min((int) (green5 * purepart + g5), 0x1F);
    }

    public final int tintBlue8(int blue8) {
        return Math.min((int) (blue8 * purepart + b), 0xFF);
    }

    public final int tintBlue5(int blue5) {
        return Math.min((int) (blue5 * purepart + b5), 0x1F);
    }

    public final int tintRed8(int red8) {
        return Math.min((int) (red8 * purepart + r), 0xFF);
    }

    public final int tintRed5(int red5) {
        return Math.min((int) (red5 * purepart + r5), 0x1F);
    }
}
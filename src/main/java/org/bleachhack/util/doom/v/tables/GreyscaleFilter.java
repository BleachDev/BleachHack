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

import org.bleachhack.util.doom.m.Settings;
import org.bleachhack.util.doom.mochadoom.Engine;

/**
 *
 * @author Good Sign
 */
public enum GreyscaleFilter {
    Lightness,
    Average,
    Luminance, // this one is the default for invulnerability map
    Luminosity;
    
    private static GreyscaleFilter FILTER;
    
    public static int component(int r, int g, int b) {
        if (FILTER == null) {
            readSetting();
        }
        return FILTER.getComponent(r, g, b);
    }
    
    public static float component(float r, float g, float b) {
        if (FILTER == null) {
            readSetting();
        }
        return FILTER.getComponent(r, g, b);
    }
    
    public static int grey888(int rgb888) {
        if (FILTER == null) {
            readSetting();
        }
        return FILTER.getGrey888(rgb888);
    }

    public static int grey888(int r8, int g8, int b8) {
        if (FILTER == null) {
            readSetting();
        }
        return FILTER.getGrey888(r8, g8, b8);
    }

    public static short grey555(int r5, int g5, int b5) {
        if (FILTER == null) {
            readSetting();
        }
        return FILTER.getGrey555(r5, g5, b5);
    }

    public static short grey555(short rgb555) {
        if (FILTER == null) {
            readSetting();
        }
        return FILTER.getGrey555(rgb555);
    }

    private static void readSetting() {
        FILTER = Engine.getConfig().getValue(Settings.greyscale_filter, GreyscaleFilter.class);
    }
        
    public int getComponent(int r, int g, int b) {
        switch(this) {
            case Lightness:
                return (Math.max(Math.max(r, g), b) + Math.min(Math.min(r, g), b)) / 2;
            case Average:
                return (r + g + b) / 3;
            case Luminance:
                return (int) (0.299f * r + 0.587f * g + 0.114f * b);
            case Luminosity:
                return (int) (0.2126f * r + 0.7152f * g + 0.0722f * b);
        }
        
        // should not happen
        return 0;
    }
    
    public float getComponent(float r, float g, float b) {
        switch(this) {
            case Lightness:
                return (Math.max(Math.max(r, g), b) + Math.min(Math.min(r, g), b)) / 2;
            case Average:
                return (r + g + b) / 3;
            case Luminance:
                return 0.299f * r + 0.587f * g + 0.114f * b;
            case Luminosity:
                return 0.2126f * r + 0.7152f * g + 0.0722f * b;
        }
        
        // should not happen
        return 0.0f;
    }
    
    public int getGrey888(int r8, int g8, int b8) {
        final int component = getComponent(r8, g8, b8) & 0xFF;
        return 0xFF000000 + (component << 16) + (component << 8) + component;
    }
    
    public short getGrey555(int r5, int g5, int b5){
        final int component = getComponent(r5, g5, b5) & 0x1F;
        return (short) ((component << 10) + (component << 5) + component);
    }
    
    public int getGrey888(int rgb888) {
        return getGrey888((rgb888 >> 16) & 0xFF, (rgb888 >> 8) & 0xFF, rgb888 & 0xFF);
    }
    
    public short getGrey555(short rgb555) {
        return getGrey555((rgb555 >> 10) & 0x1F, (rgb555 >> 5) & 0x1F, rgb555 & 0x1F);
    }
}

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
package org.bleachhack.util.doom.utils;

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class GenericCopy {
    private static final boolean[] BOOL_0 = {false};
    private static final byte[] BYTE_0 = {0};
    private static final short[] SHORT_0 = {0};
    private static final char[] CHAR_0 = {0};
    private static final int[] INT_0 = {0};
    private static final float[] FLOAT_0 = {0};
    private static final long[] LONG_0 = {0};
    private static final double[] DOUBLE_0 = {0};
    
    public static void memset(long[] array, int start, int length, long... value) {
        if (length > 0) {
            if (value.length == 0) {
                value = LONG_0;
            }
            System.arraycopy(value, 0, array, start, value.length);
        
            for (int i = value.length; i < length; i += i) {
                System.arraycopy(array, start, array, start + i, ((length - i) < i) ? (length - i) : i);
            }
        }
    }

    public static void memset(int[] array, int start, int length, int... value) {
        if (length > 0) {
            if (value.length == 0) {
                value = INT_0;
            }
            System.arraycopy(value, 0, array, start, value.length);
        
            for (int i = value.length; i < length; i += i) {
                System.arraycopy(array, start, array, start + i, ((length - i) < i) ? (length - i) : i);
            }
        }
    }

    public static void memset(short[] array, int start, int length, short... value) {
        if (length > 0) {
            if (value.length == 0) {
                value = SHORT_0;
            }
            System.arraycopy(value, 0, array, start, value.length);
        
            for (int i = value.length; i < length; i += i) {
                System.arraycopy(array, start, array, start + i, ((length - i) < i) ? (length - i) : i);
            }
        }
    }

    public static void memset(char[] array, int start, int length, char... value) {
        if (length > 0) {
            if (value.length == 0) {
                value = CHAR_0;
            }
            System.arraycopy(value, 0, array, start, value.length);
        
            for (int i = value.length; i < length; i += i) {
                System.arraycopy(array, start, array, start + i, ((length - i) < i) ? (length - i) : i);
            }
        }
    }

    public static void memset(byte[] array, int start, int length, byte... value) {
        if (length > 0) {
            if (value.length == 0) {
                value = BYTE_0;
            }
            System.arraycopy(value, 0, array, start, value.length);
        
            for (int i = value.length; i < length; i += i) {
                System.arraycopy(array, start, array, start + i, ((length - i) < i) ? (length - i) : i);
            }
        }
    }

    public static void memset(double[] array, int start, int length, double... value) {
        if (length > 0) {
            if (value.length == 0) {
                value = DOUBLE_0;
            }
            System.arraycopy(value, 0, array, start, value.length);
        
            for (int i = value.length; i < length; i += i) {
                System.arraycopy(array, start, array, start + i, ((length - i) < i) ? (length - i) : i);
            }
        }
    }

    public static void memset(float[] array, int start, int length, float... value) {
        if (length > 0) {
            if (value.length == 0) {
                value = FLOAT_0;
            }
            System.arraycopy(value, 0, array, start, value.length);
        
            for (int i = value.length; i < length; i += i) {
                System.arraycopy(array, start, array, start + i, ((length - i) < i) ? (length - i) : i);
            }
        }
    }

    public static void memset(boolean[] array, int start, int length, boolean... value) {
        if (length > 0) {
            if (value.length == 0) {
                value = BOOL_0;
            }
            System.arraycopy(value, 0, array, start, value.length);
        
            for (int i = value.length; i < length; i += i) {
                System.arraycopy(array, start, array, start + i, ((length - i) < i) ? (length - i) : i);
            }
        }
    }

    @SuppressWarnings("SuspiciousSystemArraycopy")
    public static <T> void memset(T array, int start, int length, T value, int valueStart, int valueLength) {
        if (length > 0 && valueLength > 0) {
            System.arraycopy(value, valueStart, array, start, valueLength);
        
            for (int i = valueLength; i < length; i += i) {
                System.arraycopy(array, start, array, start + i, ((length - i) < i) ? (length - i) : i);
            }
        }
    }
    
    @SuppressWarnings("SuspiciousSystemArraycopy")
    public static <T> void memcpy(T srcArray, int srcStart, T dstArray, int dstStart, int length) {
        System.arraycopy(srcArray, srcStart, dstArray, dstStart, length);
    }
    
    public static <T> T[] malloc(final ArraySupplier<T> supplier, final IntFunction<T[]> generator, final int length) {
        final T[] array = generator.apply(length);
        Arrays.setAll(array, supplier::getWithInt);
        return array;
    }
    
    public interface ArraySupplier<T> extends Supplier<T> {
        default T getWithInt(int ignoredInt) {
            return get();
        }
    }
    
    private GenericCopy() {}
}

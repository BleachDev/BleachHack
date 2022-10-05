package org.bleachhack.util.doom.pooling;

import java.util.Arrays;

public class RoguePatchMap2 {
    private static final int DEFAULT_CAPACITY = 16;
    public RoguePatchMap2() {
        lumps = new int[DEFAULT_CAPACITY];
        patches = new byte[DEFAULT_CAPACITY][][];
    }
    boolean containsKey(int lump) {
        return indexOf(lump) >= 0;
    }
    public byte[][] get(int lump) {
        int index = indexOf(lump);
        if (index >= 0) {
            return patches[index];
        } else {
            return null;
        }
    }
    public void put(int lump, byte[][] patch) {
        int index = indexOf(lump);
        if (index >= 0) {
            patches[index] = patch;
        } else {
            ensureCapacity(numEntries + 1);
            int newIndex = ~index;
            int moveCount = numEntries - newIndex;
            if (moveCount > 0) {
                System.arraycopy(lumps, newIndex, lumps, newIndex+1, moveCount);
                System.arraycopy(patches, newIndex, patches, newIndex+1, moveCount);
            }
            lumps[newIndex] = lump;
            patches[newIndex] = patch;
            ++ numEntries;
        }
    }
    private void ensureCapacity(int cap) {
        while (lumps.length <= cap) {
            lumps =
                Arrays.copyOf(lumps, Math.max(lumps.length * 2, DEFAULT_CAPACITY));
        }
        while (patches.length <= cap) {
            patches =
                Arrays.copyOf(patches, Math.max(patches.length * 2, DEFAULT_CAPACITY));
        }
    }
    private int indexOf(int lump) {
        return Arrays.binarySearch(lumps, 0, numEntries, lump);
    }
    private int[] lumps;
    private int numEntries;
    private byte[][][] patches;
}
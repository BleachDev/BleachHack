package org.bleachhack.util.world;

import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.SeedRay;

public class Ore {
    public final Type type;
    public final String dimension;
    public final Map<String, Integer> index;
    public final boolean depthAverage;
    public final Generator generator;
    public final int size;
    public final boolean enabled;
    public final Color color;
    public int step;
    public IntProvider count;
    public int minY;
    public int maxY;
    public float discardOnAir;
    public float chance;

    Ore(Type type, String dimension, Map<String, Integer> index, int step, IntProvider count, float chance, boolean depthAverage, int minY, int maxY, Generator generator, int size, float discardOnAir, boolean enabled, Color color) {
        this.type = type;
        this.dimension = dimension;
        this.index = index;
        this.step = step;
        this.count = count;
        this.depthAverage = depthAverage;
        this.minY = minY;
        this.maxY = maxY;
        this.generator = generator;
        this.size = size;
        this.enabled = enabled;
        this.color = color;
        this.discardOnAir = discardOnAir;
        this.chance = chance;
    }

    Ore(Type type, String dimension, int index, int step, IntProvider count, float chance, boolean depthAverage, int minY, int maxY, Generator generator, int size, float discardOnAir, boolean enabled, Color color) {
        this(type, dimension, indexToMap(index), step, count, chance, depthAverage, minY, maxY, generator, size, discardOnAir, enabled, color);
    }

    Ore(Type type, String dimension, int index, int step, IntProvider count, boolean depthAverage, int minY, int maxY,
        Generator generator, int size, boolean enabled, Color color) {
        this(type, dimension, indexToMap(index), step, count, 1F, depthAverage, minY, maxY, generator, size, 0F, enabled, color);
    }

    Ore(Type type, String dimension, Map<String, Integer> index, int step, IntProvider count, boolean depthAverage, int minY, int maxY,
        Generator generator, int size, boolean enabled, Color color) {
        this(type, dimension, index, step, count, 1F, depthAverage, minY, maxY, generator, size, 0F, enabled, color);
    }

    private static HashMap<String, Integer> indexToMap(int index) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("default", index);
        return map;
    }

    private static java.util.List<Ore> V1_18() {
        java.util.List<Ore> ores = new ArrayList<>();

        HashMap<String, Integer> extraGoldIndex = new HashMap<>();
        extraGoldIndex.put("default", -1);
        String[] extraGoldBiomes = new String[]{"badlands", "eroded_badlands", "wooded_badlands"};
        for (String extraGoldBiome : extraGoldBiomes) {
            extraGoldIndex.put(extraGoldBiome, 27);
        }

        HashMap<String, Integer> emeraldIndex = new HashMap<>();
        emeraldIndex.put("default", -1);
        String[] emeraldBiomes = new String[]{"windswept_hills", "meadow", "grove", "jagged_peaks", "snowy_slopes", "frozen_peaks", "stony_peaks"};
        for (String emeraldBiome : emeraldBiomes) {
            emeraldIndex.put(emeraldBiome, 27);
        }

        SeedRay seedRay = (SeedRay) ModuleManager.getModule("SeedRay");

        ores.add(new Ore(Type.COAL, "overworld", 9, 6, ConstantIntProvider.create(30), false, 136, 320, Generator.DEFAULT, 17, seedRay.getSetting(0).asToggle().getState(), new Color(47, 44, 54)));
        ores.add(new Ore(Type.COAL, "overworld", 10, 6, ConstantIntProvider.create(20), 1F, true, 97, 97, Generator.DEFAULT, 17, 0.5F, seedRay.getSetting(0).asToggle().getState(), new Color(47, 44, 54)));
        ores.add(new Ore(Type.IRON, "overworld", 11, 6, ConstantIntProvider.create(90), true, 233, 153, Generator.DEFAULT, 9, seedRay.getSetting(1).asToggle().getState(), new Color(236, 173, 119)));
        ores.add(new Ore(Type.IRON, "overworld", 12, 6, ConstantIntProvider.create(10), true, 17, 41, Generator.DEFAULT, 9, seedRay.getSetting(1).asToggle().getState(), new Color(236, 173, 119)));
        ores.add(new Ore(Type.IRON, "overworld", 13, 6, ConstantIntProvider.create(10), false, -64, 73, Generator.DEFAULT, 4, seedRay.getSetting(1).asToggle().getState(), new Color(236, 173, 119)));
        ores.add(new Ore(Type.GOLD_EXTRA, "overworld", extraGoldIndex, 6, ConstantIntProvider.create(50), false, 32, 257, Generator.DEFAULT, 9, seedRay.getSetting(2).asToggle().getState(), new Color(247, 229, 30)));
        ores.add(new Ore(Type.GOLD, "overworld", 14, 6, ConstantIntProvider.create(4), 1F, true, -15, 49, Generator.DEFAULT, 9, 0.5F, seedRay.getSetting(2).asToggle().getState(), new Color(247, 229, 30)));
        ores.add(new Ore(Type.GOLD, "overworld", 15, 6, UniformIntProvider.create(0, 1), 1F, false, -64, -47, Generator.DEFAULT, 9, 0.5F, seedRay.getSetting(2).asToggle().getState(), new Color(247, 229, 30)));
        ores.add(new Ore(Type.REDSTONE, "overworld", 16, 6, ConstantIntProvider.create(4), false, -64, 16, Generator.DEFAULT, 8, seedRay.getSetting(3).asToggle().getState(), new Color(245, 7, 23)));
        ores.add(new Ore(Type.REDSTONE, "overworld", 17, 6, ConstantIntProvider.create(8), true, -63, 33, Generator.DEFAULT, 8, seedRay.getSetting(3).asToggle().getState(), new Color(245, 7, 23)));
        ores.add(new Ore(Type.DIAMOND, "overworld", 18, 6, ConstantIntProvider.create(7), 1F, true, -63, 81, Generator.DEFAULT, 4, 0.5F, seedRay.getSetting(4).asToggle().getState(), new Color(33, 244, 255)));
        ores.add(new Ore(Type.DIAMOND, "overworld", 19, 6, ConstantIntProvider.create(1), (1F / 9F), true, -63, 81, Generator.DEFAULT, 12, 0.7F, seedRay.getSetting(4).asToggle().getState(), new Color(33, 244, 255)));
        ores.add(new Ore(Type.DIAMOND, "overworld", 20, 6, ConstantIntProvider.create(4), 1F, true, -63, 81, Generator.DEFAULT, 8, 1F, seedRay.getSetting(4).asToggle().getState(), new Color(33, 244, 255)));
        ores.add(new Ore(Type.LAPIS, "overworld", 21, 6, ConstantIntProvider.create(2), true, 1, 33, Generator.DEFAULT, 7, seedRay.getSetting(5).asToggle().getState(), new Color(8, 26, 189)));
        ores.add(new Ore(Type.LAPIS, "overworld", 22, 6, ConstantIntProvider.create(4), 1F, false, -64, 65, Generator.DEFAULT, 7, 1F, seedRay.getSetting(5).asToggle().getState(), new Color(8, 26, 189)));
        ores.add(new Ore(Type.EMERALD, "overworld", emeraldIndex, 6, ConstantIntProvider.create(100), true, 233, 249, Generator.DEFAULT, 3, seedRay.getSetting(6).asToggle().getState(), new Color(27, 209, 45)));
        ores.add(new Ore(Type.COPPER, "overworld", 24, 6, ConstantIntProvider.create(16), true, 49, 65, Generator.DEFAULT, 10, seedRay.getSetting(7).asToggle().getState(), new Color(239, 151, 0)));
        ores.add(new Ore(Type.GOLD_NETHER, "nether", Map.of("default", 19, "basalt_deltas", -1), 7, ConstantIntProvider.create(10), false, 10, 118, Generator.DEFAULT, 10, seedRay.getSetting(2).asToggle().getState(), new Color(247, 229, 30)));
        ores.add(new Ore(Type.QUARTZ, "nether", Map.of("default", 20, "basalt_deltas", -1), 7, ConstantIntProvider.create(16), false, 10, 118, Generator.DEFAULT, 14, seedRay.getSetting(8).asToggle().getState(), new Color(205, 205, 205)));
        ores.add(new Ore(Type.GOLD_NETHER, "nether", Map.of("default", -1, "basalt_deltas", 13), 7, ConstantIntProvider.create(20), false, 10, 118, Generator.DEFAULT, 10, seedRay.getSetting(2).asToggle().getState(), new Color(247, 229, 30)));
        ores.add(new Ore(Type.QUARTZ, "nether", Map.of("default", -1, "basalt_deltas", 14), 7, ConstantIntProvider.create(32), false, 10, 118, Generator.DEFAULT, 14, seedRay.getSetting(8).asToggle().getState(), new Color(205, 205, 205)));
        ores.add(new Ore(Type.LDEBRIS, "nether", 21, 7, ConstantIntProvider.create(1), true, 17, 9, Generator.NO_SURFACE, 3, seedRay.getSetting(9).asToggle().getState(), new Color(209, 27, 245)));
        ores.add(new Ore(Type.SDEBRIS, "nether", 22, 7, ConstantIntProvider.create(1), false, 8, 120, Generator.NO_SURFACE, 2, seedRay.getSetting(9).asToggle().getState(), new Color(209, 27, 245)));
        return ores;
    }

    public static List<Ore> getConfig() {
        return V1_18();
    }

    protected enum Type {
        DIAMOND, REDSTONE, GOLD, IRON, COAL, EMERALD, SDEBRIS, LDEBRIS, LAPIS, COPPER, QUARTZ, GOLD_NETHER, GOLD_EXTRA
    }

    public enum Generator {
        DEFAULT, NO_SURFACE
    }
}

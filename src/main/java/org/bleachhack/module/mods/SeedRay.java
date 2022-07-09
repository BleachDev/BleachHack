package org.bleachhack.module.mods;

import java.awt.Color;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.setting.option.Option;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.QuadColor;
import org.bleachhack.util.world.Ore;

import com.google.common.eventbus.AllowConcurrentEvents;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tag.TagKey;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.random.ChunkRandom;

//Code by KiwiClient, Implemented by Exterminate
public class SeedRay extends Module {

    private final HashMap<Long, HashMap<Ore, HashSet<Vec3d>>> chunkRenderers = new HashMap<>();
    public Long worldSeed = null;
    public List<Ore> oreConfig;
    private ChunkPos prevOffset = new ChunkPos(0, 0);
	
	public SeedRay() {
		super("SeedRay", KEY_UNBOUND, ModuleCategory.RENDER,
				"Attempts to simulate ore positions given a seed, use " + Option.CHAT_COMMAND_PREFIX.getValue() + "seedray to set seed",
	            new SettingToggle("Coal", false), // 0
	            new SettingToggle("Iron", false), // 1
	            new SettingToggle("Gold", false), // 2
	            new SettingToggle("Redstone", false), // 3
	            new SettingToggle("Diamond", false), // 4
	            new SettingToggle("Lapis", false), // 5
	            new SettingToggle("Emerald", false), // 6
	            new SettingToggle("Copper", false), // 7
	            new SettingToggle("Quartz", false), // 8
	            new SettingToggle("Debris", false), // 9
	            new SettingSlider("Range", 1, 10, 5, 0).withDesc("Chunks to simulate"));
	}
	
    @BleachSubscribe
    @AllowConcurrentEvents
    public void onWorldRender(EventWorldRender.Post event) {
        if (worldSeed != null) {
            int chunkX = mc.player.getChunkPos().x;
            int chunkZ = mc.player.getChunkPos().z;

            int rangeVal = getSetting(10).asSlider().getValueInt();
            for (int range = 0; range <= rangeVal; range++) {
                for (int x = -range + chunkX; x <= range + chunkX; x++) {
                    renderChunk(x, chunkZ + range - rangeVal, event.getMatrices());
                }
                for (int x = (-range) + 1 + chunkX; x < range + chunkX; x++) {
                    renderChunk(x, chunkZ - range + rangeVal + 1, event.getMatrices());
                }
            }
        }
    }
    
    private void renderChunk(int x, int z, MatrixStack ms) {
        long chunkKey = (long) x + ((long) z << 32);

        if (chunkRenderers.containsKey(chunkKey)) {
            for (Ore ore : oreConfig) {
                if (ore.enabled) {
                    if (!chunkRenderers.get(chunkKey).containsKey(ore)) {
                        continue;
                    }

                    for (Vec3d pos : chunkRenderers.get(chunkKey).get(ore)) {
                        Box box = new Box(new BlockPos(pos));
                        Color color = ore.color;
                        Renderer.drawBoxOutline(box, QuadColor.single(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()), 1);
                    }
                }
            }
        }
    }
    
    @BleachSubscribe
    @AllowConcurrentEvents
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        long chunkX = mc.player.getChunkPos().x;
        long chunkZ = mc.player.getChunkPos().z;
        ClientWorld world = mc.world;
        @SuppressWarnings("resource")
		int renderdistance = MinecraftClient.getInstance().options.viewDistance;

        int chunkCounter = 5;

        loop:
        while (true) {
            for (long offsetX = prevOffset.x; offsetX <= renderdistance; offsetX++) {
                for (long offsetZ = prevOffset.z; offsetZ <= renderdistance; offsetZ++) {
                    prevOffset = new ChunkPos((int) offsetX, (int) offsetZ);
                    if (chunkCounter <= 0) {
                        break loop;
                    }
                    long chunkKey = (chunkX + offsetX) + ((chunkZ + offsetZ) << 32);

                    if (chunkRenderers.containsKey(chunkKey)) {
                        chunkRenderers.get(chunkKey).values().forEach(oreSet -> oreSet.removeIf(ore -> !world.getBlockState(new BlockPos((int) ore.x, (int) ore.y, (int) ore.z)).isOpaque()));
                    }
                    chunkCounter--;
                }
                prevOffset = new ChunkPos((int) offsetX, -renderdistance);
            }
            prevOffset = new ChunkPos(-renderdistance, -renderdistance);
        }
    }
    
    @Override
    public void onEnable(boolean inWorld) {
        super.onEnable(inWorld);

        if (worldSeed == null) {
            mc.inGameHud.getChatHud().addMessage(new LiteralText("Please input a seed using " + Option.CHAT_COMMAND_PREFIX.getValue() + "seedray <seed>"));
            this.setEnabled(false);
        }

        oreConfig = Ore.getConfig();
        reload();
    }

    public void reload() {
        chunkRenderers.clear();
        if (mc.world != null) {
            loadVisibleChunks();
        }
    }

    private void loadVisibleChunks() {
        int renderdistance = mc.options.viewDistance;

        if (mc.player == null) {
            return;
        }
        int playerChunkX = mc.player.getChunkPos().x;
        int playerChunkZ = mc.player.getChunkPos().z;

        for (int i = playerChunkX - renderdistance; i < playerChunkX + renderdistance; i++) {
            for (int j = playerChunkZ - renderdistance; j < playerChunkZ + renderdistance; j++) {
                doMathOnChunk(i, j);
            }
        }
    }

    public void doMathOnChunk(int chunkX, int chunkZ) {
        if (worldSeed == null || !this.isEnabled()) {
            this.setEnabled(false);
            return;
        }
        long chunkKey = (long) chunkX + ((long) chunkZ << 32);

        ClientWorld world = mc.world;

        if (chunkRenderers.containsKey(chunkKey) || world == null) {
            return;
        }

        if (world.getChunkManager().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false) == null) {
            return;
        }

        chunkX = chunkX << 4;
        chunkZ = chunkZ << 4;


        ChunkRandom random = new ChunkRandom(ChunkRandom.RandomProvider.XOROSHIRO.create(0));

        HashMap<Ore, HashSet<Vec3d>> h = new HashMap<>();

        long populationSeed = random.setPopulationSeed(worldSeed, chunkX, chunkZ);

        Identifier id = world.getRegistryManager().get(Registry.BIOME_KEY).getId(world.getBiomeAccess().getBiomeForNoiseGen(new BlockPos(chunkX, 0, chunkZ)).value());
        if (id == null) {
            mc.inGameHud.getChatHud().addMessage(new LiteralText("Unable to calculate ore positions, there may be mods affecting world generation"));
            this.setEnabled(false);
            return;
        }
        String biomeName = id.getPath();
        String dimensionName = world.getDimension().getInfiniburnBlocks().id().getPath();

        for (Ore ore : oreConfig) {

            if (!dimensionName.endsWith(ore.dimension)) {
                continue;
            }

            HashSet<Vec3d> ores = new HashSet<>();

            int index;
            if (ore.index.containsKey(biomeName)) {
                index = ore.index.get(biomeName);
            } else {
                index = ore.index.get("default");
            }
            if (index < 0) {
                continue;
            }

            random.setDecoratorSeed(populationSeed, index, ore.step);

            int repeat = ore.count.get(random);

            for (int i = 0; i < repeat; i++) {

                if (ore.chance != 1F && random.nextFloat() >= ore.chance) {
                    continue;
                }

                int x = random.nextInt(16) + chunkX;
                int z = random.nextInt(16) + chunkZ;
                int y = ore.depthAverage ? random.nextInt(ore.maxY) + random.nextInt(ore.maxY) - ore.maxY : random.nextInt(ore.maxY - ore.minY);

                y += ore.minY;

                switch (ore.generator) {
                    case DEFAULT -> ores.addAll(generateNormal(world, random, new BlockPos(x, y, z), ore.size, ore.discardOnAir));
                    case NO_SURFACE -> ores.addAll(generateHidden(world, random, new BlockPos(x, y, z), ore.size));
                    default -> System.out.println("Fatal Error");
                }
            }
            if (!ores.isEmpty()) {
                h.put(ore, ores);
            }
        }
        chunkRenderers.put(chunkKey, h);
    }

    // ====================================
    // Mojang code
    // ====================================

    private ArrayList<Vec3d> generateNormal(ClientWorld world, Random random, BlockPos blockPos, int veinSize, float discardOnAir) {
        float f = random.nextFloat() * 3.1415927F;
        float g = (float) veinSize / 8.0F;
        int i = MathHelper.ceil(((float) veinSize / 16.0F * 2.0F + 1.0F) / 2.0F);
        double d = (double) blockPos.getX() + Math.sin(f) * (double) g;
        double e = (double) blockPos.getX() - Math.sin(f) * (double) g;
        double h = (double) blockPos.getZ() + Math.cos(f) * (double) g;
        double j = (double) blockPos.getZ() - Math.cos(f) * (double) g;
        double l = (blockPos.getY() + random.nextInt(3) - 2);
        double m = (blockPos.getY() + random.nextInt(3) - 2);
        int n = blockPos.getX() - MathHelper.ceil(g) - i;
        int o = blockPos.getY() - 2 - i;
        int p = blockPos.getZ() - MathHelper.ceil(g) - i;
        int q = 2 * (MathHelper.ceil(g) + i);
        int r = 2 * (2 + i);

        for (int s = n; s <= n + q; ++s) {
            for (int t = p; t <= p + q; ++t) {
                if (o <= world.getTopY(Heightmap.Type.MOTION_BLOCKING, s, t)) {
                    return this.generateVeinPart(world, random, veinSize, d, e, h, j, l, m, n, o, p, q, r, discardOnAir);
                }
            }
        }

        return new ArrayList<>();
    }

    private ArrayList<Vec3d> generateVeinPart(ClientWorld world, Random random, int veinSize, double startX, double endX, double startZ, double endZ, double startY, double endY, int x, int y, int z, int size, int i, float discardOnAir) {

        BitSet bitSet = new BitSet(size * i * size);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        double[] ds = new double[veinSize * 4];

        ArrayList<Vec3d> poses = new ArrayList<>();

        int n;
        double p;
        double q;
        double r;
        double s;
        for (n = 0; n < veinSize; ++n) {
            float f = (float) n / (float) veinSize;
            p = MathHelper.lerp(f, startX, endX);
            q = MathHelper.lerp(f, startY, endY);
            r = MathHelper.lerp(f, startZ, endZ);
            s = random.nextDouble() * (double) veinSize / 16.0D;
            double m = ((double) (MathHelper.sin(3.1415927F * f) + 1.0F) * s + 1.0D) / 2.0D;
            ds[n * 4] = p;
            ds[n * 4 + 1] = q;
            ds[n * 4 + 2] = r;
            ds[n * 4 + 3] = m;
        }

        for (n = 0; n < veinSize - 1; ++n) {
            if (!(ds[n * 4 + 3] <= 0.0D)) {
                for (int o = n + 1; o < veinSize; ++o) {
                    if (!(ds[o * 4 + 3] <= 0.0D)) {
                        p = ds[n * 4] - ds[o * 4];
                        q = ds[n * 4 + 1] - ds[o * 4 + 1];
                        r = ds[n * 4 + 2] - ds[o * 4 + 2];
                        s = ds[n * 4 + 3] - ds[o * 4 + 3];
                        if (s * s > p * p + q * q + r * r) {
                            if (s > 0.0D) {
                                ds[o * 4 + 3] = -1.0D;
                            } else {
                                ds[n * 4 + 3] = -1.0D;
                            }
                        }
                    }
                }
            }
        }

        for (n = 0; n < veinSize; ++n) {
            double u = ds[n * 4 + 3];
            if (!(u < 0.0D)) {
                double v = ds[n * 4];
                double w = ds[n * 4 + 1];
                double aa = ds[n * 4 + 2];
                int ab = Math.max(MathHelper.floor(v - u), x);
                int ac = Math.max(MathHelper.floor(w - u), y);
                int ad = Math.max(MathHelper.floor(aa - u), z);
                int ae = Math.max(MathHelper.floor(v + u), ab);
                int af = Math.max(MathHelper.floor(w + u), ac);
                int ag = Math.max(MathHelper.floor(aa + u), ad);

                for (int ah = ab; ah <= ae; ++ah) {
                    double ai = ((double) ah + 0.5D - v) / u;
                    if (ai * ai < 1.0D) {
                        for (int aj = ac; aj <= af; ++aj) {
                            double ak = ((double) aj + 0.5D - w) / u;
                            if (ai * ai + ak * ak < 1.0D) {
                                for (int al = ad; al <= ag; ++al) {
                                    double am = ((double) al + 0.5D - aa) / u;
                                    if (ai * ai + ak * ak + am * am < 1.0D) {
                                        int an = ah - x + (aj - y) * size + (al - z) * size * i;
                                        if (!bitSet.get(an)) {
                                            bitSet.set(an);
                                            mutable.set(ah, aj, al);
                                            if (aj >= -64 && aj < 320 && (world.getBlockState(mutable).isOpaque())) {
                                                if (shouldPlace(world, mutable, discardOnAir, random)) {
                                                    poses.add(new Vec3d(ah, aj, al));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return poses;
    }

    private boolean shouldPlace(ClientWorld world, BlockPos orePos, float discardOnAir, Random random) {
        if (discardOnAir == 0F || (discardOnAir != 1F && random.nextFloat() >= discardOnAir)) {
            return true;
        }

        for (Direction direction : Direction.values()) {
            if (!world.getBlockState(orePos.add(direction.getVector())).isOpaque() && discardOnAir != 1F) {
                return false;
            }
        }
        return true;
    }

    private ArrayList<Vec3d> generateHidden(ClientWorld world, Random random, BlockPos blockPos, int size) {

        ArrayList<Vec3d> poses = new ArrayList<>();

        int i = random.nextInt(size + 1);

        for (int j = 0; j < i; ++j) {
            size = Math.min(j, 7);
            int x = this.randomCoord(random, size) + blockPos.getX();
            int y = this.randomCoord(random, size) + blockPos.getY();
            int z = this.randomCoord(random, size) + blockPos.getZ();
            if (world.getBlockState(new BlockPos(x, y, z)).isOpaque()) {
                if (shouldPlace(world, new BlockPos(x, y, z), 1F, random)) {
                    poses.add(new Vec3d(x, y, z));
                }
            }
        }

        return poses;
    }

    private int randomCoord(Random random, int size) {
        return Math.round((random.nextFloat() - random.nextFloat()) * (float) size);
    }

    
}

/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
 * Copyright (c) 2019 Bleach.
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
package bleach.hack.module.mods;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.DeflaterOutputStream;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventDrawOverlay;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.world.SimpleTickScheduler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.ChunkTickScheduler;
import net.minecraft.world.Heightmap;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.LightType;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.GenerationStep;

public class ChunkSize extends Module {

	private long timer = 0;
	private int size = 0;

	public ChunkSize() {
		super("ChunkSize", KEY_UNBOUND, Category.WORLD, "Shows the size of the chunk you are standing in");
	}

	@Subscribe
	public void onOverlay(EventDrawOverlay event) {
		mc.textRenderer.drawWithShadow("Chunk: " + (size < 1000 ? size + "B" : size / 1000d + "KB"), 120, 5, -1);
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (System.currentTimeMillis() - 1500 < timer)
			return;
		timer = System.currentTimeMillis();

		if (mc.world.getWorldChunk(mc.player.getBlockPos()) == null)
			return;
		new Thread(() -> {
			CompoundTag tag = serialize(mc.world, mc.world.getWorldChunk(mc.player.getBlockPos()));
			DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new DeflaterOutputStream(new ByteArrayOutputStream(8096))));
			try {
				NbtIo.writeCompressed(tag, output);
			} catch (IOException e) {
			}
			size = output.size();
		}).start();
	}

	public CompoundTag serialize(World serverWorld_1, Chunk chunk_1) {
		ChunkPos chunkPos_1 = chunk_1.getPos();
		CompoundTag compoundTag_1 = new CompoundTag();
		CompoundTag compoundTag_2 = new CompoundTag();
		compoundTag_1.putInt("DataVersion", SharedConstants.getGameVersion().getWorldVersion());
		compoundTag_1.put("Level", compoundTag_2);
		compoundTag_2.putInt("xPos", chunkPos_1.x);
		compoundTag_2.putInt("zPos", chunkPos_1.z);
		compoundTag_2.putLong("LastUpdate", serverWorld_1.getTime());
		compoundTag_2.putLong("InhabitedTime", chunk_1.getInhabitedTime());
		compoundTag_2.putString("Status", chunk_1.getStatus().getName());
		UpgradeData upgradeData_1 = chunk_1.getUpgradeData();
		if (!upgradeData_1.method_12349()) {
			compoundTag_2.put("UpgradeData", upgradeData_1.toTag());
		}

		ChunkSection[] chunkSections_1 = chunk_1.getSectionArray();
		ListTag listTag_1 = new ListTag();
		LightingProvider lightingProvider_1 = serverWorld_1.getChunkManager().getLightingProvider();
		boolean boolean_1 = chunk_1.isLightOn();

		CompoundTag compoundTag_6;
		for (Integer int_1 : IntStream.range(-1, 17).boxed().collect(Collectors.toList())) {
			ChunkSection chunkSection_1 = Arrays.stream(chunkSections_1).filter(chunkSection_1x -> {
				return chunkSection_1x != null && chunkSection_1x.getYOffset() >> 4 == int_1;
			}).findFirst().orElse(WorldChunk.EMPTY_SECTION);
			ChunkNibbleArray chunkNibbleArray_1 = lightingProvider_1.get(LightType.BLOCK).getChunkLightArray(ChunkSectionPos.from(chunkPos_1, int_1));
			ChunkNibbleArray chunkNibbleArray_2 = lightingProvider_1.get(LightType.SKY).getChunkLightArray(ChunkSectionPos.from(chunkPos_1, int_1));
			if (chunkSection_1 != WorldChunk.EMPTY_SECTION || chunkNibbleArray_1 != null || chunkNibbleArray_2 != null) {
				compoundTag_6 = new CompoundTag();
				compoundTag_6.putByte("Y", (byte) (int_1 & 255));
				if (chunkSection_1 != WorldChunk.EMPTY_SECTION) {
					chunkSection_1.getContainer().write(compoundTag_6, "Palette", "BlockStates");
				}

				if (chunkNibbleArray_1 != null && !chunkNibbleArray_1.isUninitialized()) {
					compoundTag_6.putByteArray("BlockLight", chunkNibbleArray_1.asByteArray());
				}

				if (chunkNibbleArray_2 != null && !chunkNibbleArray_2.isUninitialized()) {
					compoundTag_6.putByteArray("SkyLight", chunkNibbleArray_2.asByteArray());
				}

				listTag_1.add(compoundTag_6);
			}
		}

		compoundTag_2.put("Sections", listTag_1);
		if (boolean_1) {
			compoundTag_2.putBoolean("isLightOn", true);
		}

		Biome[] biomes_1 = chunk_1.getBiomeArray();
		int[] ints_1 = biomes_1 != null ? new int[biomes_1.length] : new int[0];
		if (biomes_1 != null) {
			for (int int_3 = 0; int_3 < biomes_1.length; ++int_3) {
				ints_1[int_3] = Registry.BIOME.getRawId(biomes_1[int_3]);
			}
		}

		compoundTag_2.putIntArray("Biomes", ints_1);
		ListTag listTag_2 = new ListTag();
		Iterator<BlockPos> var23 = chunk_1.getBlockEntityPositions().iterator();

		while (var23.hasNext()) {
			BlockPos blockPos_1 = var23.next();
			compoundTag_6 = chunk_1.method_20598(blockPos_1);
			if (compoundTag_6 != null) {
				listTag_2.add(compoundTag_6);
			}
		}

		compoundTag_2.put("TileEntities", listTag_2);
		ListTag listTag_3 = new ListTag();
		if (chunk_1.getStatus().getChunkType() == ChunkStatus.ChunkType.LEVELCHUNK) {
			WorldChunk worldChunk_1 = (WorldChunk) chunk_1;
			worldChunk_1.setUnsaved(false);

			for (int int_4 = 0; int_4 < worldChunk_1.getEntitySectionArray().length; ++int_4) {
				Iterator<Entity> var16 = worldChunk_1.getEntitySectionArray()[int_4].iterator();

				while (var16.hasNext()) {
					Entity entity_1 = var16.next();
					CompoundTag compoundTag_5 = new CompoundTag();
					if (entity_1.saveToTag(compoundTag_5)) {
						worldChunk_1.setUnsaved(true);
						listTag_3.add(compoundTag_5);
					}
				}
			}
		} else {
			ProtoChunk protoChunk_1 = (ProtoChunk) chunk_1;
			listTag_3.addAll(protoChunk_1.getEntities());
			compoundTag_2.put("Lights", ChunkSerializer.toNbt(protoChunk_1.getLightSourcesBySection()));
			compoundTag_6 = new CompoundTag();
			GenerationStep.Carver[] var30 = GenerationStep.Carver.values();
			int var33 = var30.length;

			for (int var35 = 0; var35 < var33; ++var35) {
				GenerationStep.Carver generationStep$Carver_1 = var30[var35];
				compoundTag_6.putByteArray(generationStep$Carver_1.toString(), chunk_1.getCarvingMask(generationStep$Carver_1).toByteArray());
			}

			compoundTag_2.put("CarvingMasks", compoundTag_6);
		}

		compoundTag_2.put("Entities", listTag_3);
		TickScheduler<Block> tickScheduler_1 = chunk_1.getBlockTickScheduler();
		if (tickScheduler_1 instanceof ChunkTickScheduler) {
			compoundTag_2.put("ToBeTicked", ((ChunkTickScheduler<?>) tickScheduler_1).toNbt());
		} else if (tickScheduler_1 instanceof SimpleTickScheduler) {
			compoundTag_2.put("TileTicks", ((SimpleTickScheduler<?>) tickScheduler_1).toNbt(serverWorld_1.getTime()));
		}

		TickScheduler<Fluid> tickScheduler_2 = chunk_1.getFluidTickScheduler();
		if (tickScheduler_2 instanceof ChunkTickScheduler) {
			compoundTag_2.put("LiquidsToBeTicked", ((ChunkTickScheduler<?>) tickScheduler_2).toNbt());
		} else if (tickScheduler_2 instanceof SimpleTickScheduler) {
			compoundTag_2.put("LiquidTicks", ((SimpleTickScheduler<?>) tickScheduler_2).toNbt(serverWorld_1.getTime()));
		}

		compoundTag_2.put("PostProcessing", ChunkSerializer.toNbt(chunk_1.getPostProcessingLists()));
		CompoundTag compoundTag_7 = new CompoundTag();
		Iterator<Entry<Type, Heightmap>> var34 = chunk_1.getHeightmaps().iterator();

		while (var34.hasNext()) {
			Entry<Heightmap.Type, Heightmap> map$Entry_1 = var34.next();
			if (chunk_1.getStatus().isSurfaceGenerated().contains(map$Entry_1.getKey())) {
				compoundTag_7.put(map$Entry_1.getKey().getName(), new LongArrayTag(map$Entry_1.getValue().asLongArray()));
			}
		}

		compoundTag_2.put("Heightmaps", compoundTag_7);
		// compoundTag_2.put("Structures", writeStructures(chunkPos_1,
		// chunk_1.getStructureStarts(), chunk_1.getStructureReferences()));
		return compoundTag_1;
	}
}

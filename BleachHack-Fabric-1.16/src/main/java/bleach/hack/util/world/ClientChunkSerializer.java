/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util.world;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.server.world.SimpleTickScheduler;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.ChunkTickScheduler;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.StructureFeature;

public class ClientChunkSerializer {
	
	public static NbtCompound serialize(ClientWorld world, Chunk chunk) {
		ChunkPos chunkPos = chunk.getPos();
		NbtCompound NbtCompound = new NbtCompound();
		NbtCompound NbtCompound2 = new NbtCompound();
		NbtCompound.putInt("DataVersion", SharedConstants.getGameVersion().getWorldVersion());
		NbtCompound.put("Level", NbtCompound2);
		NbtCompound2.putInt("xPos", chunkPos.x);
		NbtCompound2.putInt("zPos", chunkPos.z);
		NbtCompound2.putLong("LastUpdate", world.getTime());
		NbtCompound2.putLong("InhabitedTime", chunk.getInhabitedTime());
		NbtCompound2.putString("Status", chunk.getStatus().getId());
		UpgradeData upgradeData = chunk.getUpgradeData();
		if (!upgradeData.isDone()) {
			NbtCompound2.put("UpgradeData", upgradeData.toNbt());
		}

		ChunkSection[] chunkSections = chunk.getSectionArray();
		NbtList NbtList = new NbtList();
		LightingProvider lightingProvider = world.getChunkManager().getLightingProvider();
		boolean bl = chunk.isLightOn();

		NbtCompound NbtCompound7;
		for (int i = -1; i < 17; ++i) {
			final int finalI = i;
			ChunkSection chunkSection = Arrays.stream(chunkSections).filter(chunkSectionx -> (chunkSectionx != null && chunkSectionx.getYOffset() >> 4 == finalI)).findFirst().orElse(WorldChunk.EMPTY_SECTION);
			ChunkNibbleArray chunkNibbleArray = lightingProvider.get(LightType.BLOCK)
					.getLightSection(ChunkSectionPos.from(chunkPos, i));
			ChunkNibbleArray chunkNibbleArray2 = lightingProvider.get(LightType.SKY)
					.getLightSection(ChunkSectionPos.from(chunkPos, i));
			if (chunkSection != WorldChunk.EMPTY_SECTION || chunkNibbleArray != null || chunkNibbleArray2 != null) {
				NbtCompound7 = new NbtCompound();
				NbtCompound7.putByte("Y", (byte) (i & 255));
				if (chunkSection != WorldChunk.EMPTY_SECTION) {
					chunkSection.getContainer().write(NbtCompound7, "Palette", "BlockStates");
				}

				if (chunkNibbleArray != null && !chunkNibbleArray.isUninitialized()) {
					NbtCompound7.putByteArray("BlockLight", chunkNibbleArray.asByteArray());
				}

				if (chunkNibbleArray2 != null && !chunkNibbleArray2.isUninitialized()) {
					NbtCompound7.putByteArray("SkyLight", chunkNibbleArray2.asByteArray());
				}

				NbtList.add(NbtCompound7);
			}
		}

		NbtCompound2.put("Sections", NbtList);
		if (bl) {
			NbtCompound2.putBoolean("isLightOn", true);
		}

		BiomeArray biomeArray = chunk.getBiomeArray();
		if (biomeArray != null) {
			NbtCompound2.putIntArray("Biomes", biomeArray.toIntArray());
		}

		NbtList NbtList2 = new NbtList();
		Iterator<BlockPos> var21 = chunk.getBlockEntityPositions().iterator();

		NbtCompound NbtCompound6;
		while (var21.hasNext()) {
			BlockPos blockPos = var21.next();
			NbtCompound6 = chunk.getPackedBlockEntityNbt(blockPos);
			if (NbtCompound6 != null) {
				NbtList2.add(NbtCompound6);
			}
		}

		NbtCompound2.put("TileEntities", NbtList2);
		NbtList NbtList3 = new NbtList();
		if (chunk.getStatus().getChunkType() == ChunkStatus.ChunkType.field_12807) {
			WorldChunk worldChunk = (WorldChunk) chunk;
			worldChunk.setUnsaved(false);

			for (int k = 0; k < worldChunk.getEntitySectionArray().length; ++k) {
				Iterator<Entity> var29 = worldChunk.getEntitySectionArray()[k].iterator();

				while (var29.hasNext()) {
					Entity entity = var29.next();
					NbtCompound NbtCompound5 = new NbtCompound();
					if (entity.saveNbt(NbtCompound5)) {
						worldChunk.setUnsaved(true);
						NbtList3.add(NbtCompound5);
					}
				}
			}
		} else {
			ProtoChunk protoChunk = (ProtoChunk) chunk;
			NbtList3.addAll(protoChunk.getEntities());
			NbtCompound2.put("Lights", toNbt(protoChunk.getLightSourcesBySection()));
			NbtCompound6 = new NbtCompound();
			GenerationStep.Carver[] var30 = GenerationStep.Carver.values();
			int var32 = var30.length;

			for (int var34 = 0; var34 < var32; ++var34) {
				GenerationStep.Carver carver = var30[var34];
				BitSet bitSet = protoChunk.getCarvingMask(carver);
				if (bitSet != null) {
					NbtCompound6.putByteArray(carver.toString(), bitSet.toByteArray());
				}
			}

			NbtCompound2.put("CarvingMasks", NbtCompound6);
		}

		NbtCompound2.put("Entities", NbtList3);
		TickScheduler<Block> tickScheduler = chunk.getBlockTickScheduler();
		if (tickScheduler instanceof ChunkTickScheduler) {
			NbtCompound2.put("ToBeTicked", ((ChunkTickScheduler<Block>) tickScheduler).toNbt());
		} else if (tickScheduler instanceof SimpleTickScheduler) {
			NbtCompound2.put("TileTicks", ((SimpleTickScheduler<Block>) tickScheduler).toNbt());
		}

		TickScheduler<Fluid> tickScheduler2 = chunk.getFluidTickScheduler();
		if (tickScheduler2 instanceof ChunkTickScheduler) {
			NbtCompound2.put("LiquidsToBeTicked", ((ChunkTickScheduler<Fluid>) tickScheduler2).toNbt());
		} else if (tickScheduler2 instanceof SimpleTickScheduler) {
			NbtCompound2.put("LiquidTicks", ((SimpleTickScheduler<Fluid>) tickScheduler2).toNbt());
		}

		NbtCompound2.put("PostProcessing", toNbt(chunk.getPostProcessingLists()));
		NbtCompound7 = new NbtCompound();
		Iterator<Entry<Type, Heightmap>> var33 = chunk.getHeightmaps().iterator();

		while (var33.hasNext()) {
			Entry<Heightmap.Type, Heightmap> entry = var33.next();
			if (chunk.getStatus().getHeightmapTypes().contains(entry.getKey())) {
				NbtCompound7.put(entry.getKey().getName(),
						new NbtLongArray(entry.getValue().asLongArray()));
			}
		}

		NbtCompound2.put("Heightmaps", NbtCompound7);
		NbtCompound2.put("Structures",
				writeStructures(chunkPos, chunk.getStructureStarts(), chunk.getStructureReferences()));
		return NbtCompound;
	}

	private static NbtCompound writeStructures(ChunkPos pos, Map<StructureFeature<?>, StructureStart<?>> structureStarts,
			Map<StructureFeature<?>, LongSet> structureReferences) {
		NbtCompound NbtCompound = new NbtCompound();
		NbtCompound NbtCompound2 = new NbtCompound();
		Iterator<Entry<StructureFeature<?>, StructureStart<?>>> var5 = structureStarts.entrySet().iterator();

		while (var5.hasNext()) {
			Entry<StructureFeature<?>, StructureStart<?>> entry = var5
					.next();
			NbtCompound2.put(((StructureFeature<?>) entry.getKey()).getName(),
					((StructureStart<?>) entry.getValue()).toTag(pos.x, pos.z));
		}

		NbtCompound.put("Starts", NbtCompound2);
		NbtCompound NbtCompound3 = new NbtCompound();
		Iterator<Entry<StructureFeature<?>, LongSet>> var9 = structureReferences.entrySet().iterator();

		while (var9.hasNext()) {
			Entry<StructureFeature<?>, LongSet> entry2 = var9.next();
			NbtCompound3.put(((StructureFeature<?>) entry2.getKey()).getName(),
					new NbtLongArray(entry2.getValue()));
		}

		NbtCompound.put("References", NbtCompound3);
		return NbtCompound;
	}

	private static NbtList toNbt(ShortList[] lists) {
		NbtList NbtList = new NbtList();
		ShortList[] var2 = lists;
		int var3 = lists.length;

		for (int var4 = 0; var4 < var3; ++var4) {
			ShortList shortList = var2[var4];
			NbtList NbtList2 = new NbtList();
			if (shortList != null) {
				ShortListIterator var7 = shortList.iterator();

				while (var7.hasNext()) {
					Short short_ = var7.nextShort();
					NbtList2.add(NbtShort.of(short_));
				}
			}

			NbtList.add(NbtList2);
		}

		return NbtList;
	}
}

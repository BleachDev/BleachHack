/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.world;

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
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.server.world.SimpleTickScheduler;
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
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.StructureFeature;

public class ClientChunkSerializer {

	public static NbtCompound serialize(ClientWorld world, Chunk chunk) {
		ChunkPos chunkPos = chunk.getPos();
		NbtCompound nbtCompound = new NbtCompound();
		NbtCompound nbtCompound2 = new NbtCompound();
		nbtCompound.putInt("DataVersion", SharedConstants.getGameVersion().getWorldVersion());
		nbtCompound.put("Level", nbtCompound2);
		nbtCompound2.putInt("xPos", chunkPos.x);
		nbtCompound2.putInt("zPos", chunkPos.z);
		nbtCompound2.putLong("LastUpdate", world.getTime());
		nbtCompound2.putLong("InhabitedTime", chunk.getInhabitedTime());
		nbtCompound2.putString("Status", chunk.getStatus().getId());
		UpgradeData upgradeData = chunk.getUpgradeData();
		if (!upgradeData.isDone()) {
			nbtCompound2.put("UpgradeData", upgradeData.toNbt());
		}

		if (chunk instanceof EmptyChunk)
			return nbtCompound;

		ChunkSection[] chunkSections = chunk.getSectionArray();
		NbtList nbtList = new NbtList();
		LightingProvider lightingProvider = world.getChunkManager().getLightingProvider();
		boolean bl = chunk.isLightOn();

		for (int i = lightingProvider.getBottomY(); i < lightingProvider.getTopY(); ++i) {
			int fi = i;
			ChunkSection chunkSection = Arrays.stream(chunkSections).filter(cs ->
				cs != null && ChunkSectionPos.getSectionCoord(cs.getYOffset()) == fi
			).findFirst().orElse(WorldChunk.EMPTY_SECTION);
			ChunkNibbleArray chunkNibbleArray = lightingProvider.get(LightType.BLOCK).getLightSection(ChunkSectionPos.from(chunkPos, i));
			ChunkNibbleArray chunkNibbleArray2 = lightingProvider.get(LightType.SKY).getLightSection(ChunkSectionPos.from(chunkPos, i));
			if (chunkSection != WorldChunk.EMPTY_SECTION || chunkNibbleArray != null || chunkNibbleArray2 != null) {
				NbtCompound nbtCompound3 = new NbtCompound();
				nbtCompound3.putByte("Y", (byte) (i & 255));

				if (chunkSection != WorldChunk.EMPTY_SECTION) {
					try {
						chunkSection.getContainer().write(nbtCompound3, "Palette", "BlockStates");
					} catch (Exception ignore) {
					}
				}

				if (chunkNibbleArray != null && !chunkNibbleArray.isUninitialized()) {
					nbtCompound3.putByteArray("BlockLight", chunkNibbleArray.asByteArray());
				}

				if (chunkNibbleArray2 != null && !chunkNibbleArray2.isUninitialized()) {
					nbtCompound3.putByteArray("SkyLight", chunkNibbleArray2.asByteArray());
				}

				nbtList.add(nbtCompound3);
			}
		}

		nbtCompound2.put("Sections", nbtList);
		if (bl) {
			nbtCompound2.putBoolean("isLightOn", true);
		}

		BiomeArray biomeArray = chunk.getBiomeArray();
		if (biomeArray != null) {
			nbtCompound2.putIntArray("Biomes", biomeArray.toIntArray());
		}

		NbtList nbtList2 = new NbtList();
		Iterator<BlockPos> var21 = chunk.getBlockEntityPositions().iterator();

		NbtCompound nbtCompound6;
		while (var21.hasNext()) {
			BlockPos blockPos = var21.next();
			nbtCompound6 = chunk.getPackedBlockEntityNbt(blockPos);
			if (nbtCompound6 != null) {
				nbtList2.add(nbtCompound6);
			}
		}

		nbtCompound2.put("TileEntities", nbtList2);
		if (chunk.getStatus().getChunkType() == ChunkStatus.ChunkType.PROTOCHUNK) {
			ProtoChunk protoChunk = (ProtoChunk) chunk;
			NbtList nbtList3 = new NbtList();
			nbtList3.addAll(protoChunk.getEntities());
			nbtCompound2.put("Entities", nbtList3);
			nbtCompound2.put("Lights", toNbt(protoChunk.getLightSourcesBySection()));
			nbtCompound6 = new NbtCompound();
			GenerationStep.Carver[] var28 = GenerationStep.Carver.values();

			for (GenerationStep.Carver carver : var28) {
				BitSet bitSet = protoChunk.getCarvingMask(carver);
				if (bitSet != null) {
					nbtCompound6.putByteArray(carver.toString(), bitSet.toByteArray());
				}
			}

			nbtCompound2.put("CarvingMasks", nbtCompound6);
		}

		TickScheduler<Block> tickScheduler = chunk.getBlockTickScheduler();
		if (tickScheduler instanceof ChunkTickScheduler) {
			nbtCompound2.put("ToBeTicked", ((ChunkTickScheduler<Block>) tickScheduler).toNbt());
		} else if (tickScheduler instanceof SimpleTickScheduler) {
			nbtCompound2.put("TileTicks", ((SimpleTickScheduler<Block>) tickScheduler).toNbt());
		}

		TickScheduler<Fluid> tickScheduler2 = chunk.getFluidTickScheduler();
		if (tickScheduler2 instanceof ChunkTickScheduler) {
			nbtCompound2.put("LiquidsToBeTicked", ((ChunkTickScheduler<Fluid>) tickScheduler2).toNbt());
		} else if (tickScheduler2 instanceof SimpleTickScheduler) {
			nbtCompound2.put("LiquidTicks", ((SimpleTickScheduler<Fluid>) tickScheduler2).toNbt());
		}

		nbtCompound2.put("PostProcessing", toNbt(chunk.getPostProcessingLists()));
		nbtCompound6 = new NbtCompound();

		for (Entry<Type, Heightmap> entry : chunk.getHeightmaps()) {
			if (chunk.getStatus().getHeightmapTypes().contains(entry.getKey())) {
				nbtCompound6.put(entry.getKey().getName(), new NbtLongArray(entry.getValue().asLongArray()));
			}
		}

		nbtCompound2.put("Heightmaps", nbtCompound6);
		nbtCompound2.put("Structures", writeStructures(chunk.getStructureReferences()));
		return nbtCompound;
	}

	private static NbtCompound writeStructures(Map<StructureFeature<?>, LongSet> map2) {
		NbtCompound nbtCompound = new NbtCompound();
		NbtCompound nbtCompound2 = new NbtCompound();

		nbtCompound.put("Starts", nbtCompound2);
		NbtCompound nbtCompound3 = new NbtCompound();

		for (Entry<StructureFeature<?>, LongSet> entry2 : map2.entrySet()) {
			nbtCompound3.put(entry2.getKey().getName(), new NbtLongArray(entry2.getValue()));
		}

		nbtCompound.put("References", nbtCompound3);
		return nbtCompound;
	}

	private static NbtList toNbt(ShortList[] lists) {
		NbtList nbtList = new NbtList();

		for (ShortList shortList : lists) {
			NbtList nbtList2 = new NbtList();
			if (shortList != null) {
				ShortListIterator var7 = shortList.iterator();

				while (var7.hasNext()) {
					nbtList2.add(NbtShort.of(var7.nextShort()));
				}
			}

			nbtList.add(nbtList2);
		}

		return nbtList;
	}
}

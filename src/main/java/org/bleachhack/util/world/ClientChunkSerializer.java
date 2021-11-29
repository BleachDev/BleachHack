/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.world;

import java.util.Map;
import com.mojang.serialization.Codec;

import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.BelowZeroRetrogen;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.CarvingMask;
import net.minecraft.world.gen.chunk.BlendingData;

public class ClientChunkSerializer {
	
	private static final Codec<PalettedContainer<BlockState>> CODEC = PalettedContainer.createCodec(Block.STATE_IDS, BlockState.CODEC, PalettedContainer.PaletteProvider.BLOCK_STATE, Blocks.AIR.getDefaultState());
	private static final String UPGRADE_DATA_KEY = "UpgradeData";
    private static final String BLOCK_TICKS = "block_ticks";
    private static final String FLUID_TICKS = "fluid_ticks";

	public static NbtCompound serialize(ClientWorld world, Chunk chunk) {
		UpgradeData upgradeData;
		BelowZeroRetrogen belowZeroRetrogen;
		ChunkPos chunkPos = chunk.getPos();
		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.putInt("DataVersion", SharedConstants.getGameVersion().getSaveVersion().getId());
		nbtCompound.putInt("xPos", chunkPos.x);
		nbtCompound.putInt("yPos", chunk.getBottomSectionCoord());
		nbtCompound.putInt("zPos", chunkPos.z);
		nbtCompound.putLong("LastUpdate", world.getTime());
		nbtCompound.putLong("InhabitedTime", chunk.getInhabitedTime());
		nbtCompound.putString("Status", chunk.getStatus().getId());
		
		BlendingData blendingData = chunk.getBlendingData();
		if (blendingData != null) {
			BlendingData.CODEC.encodeStart(NbtOps.INSTANCE, blendingData).result().ifPresent(nbtElement -> nbtCompound.put("blending_data", (NbtElement)nbtElement));
		}
		
		if ((belowZeroRetrogen = chunk.getBelowZeroRetrogen()) != null) {
			BelowZeroRetrogen.CODEC.encodeStart(NbtOps.INSTANCE, belowZeroRetrogen).result().ifPresent(nbtElement -> nbtCompound.put("below_zero_retrogen", (NbtElement)nbtElement));
		}
		
		if (!(upgradeData = chunk.getUpgradeData()).isDone()) {
			nbtCompound.put(UPGRADE_DATA_KEY, upgradeData.toNbt());
		}
		
		ChunkSection[] chunkSections = chunk.getSectionArray();
		NbtList nbtList = new NbtList();
		LightingProvider lightingProvider = world.getChunkManager().getLightingProvider();
		Registry<Biome> registry = world.getRegistryManager().get(Registry.BIOME_KEY);
		Codec<PalettedContainer<Biome>> codec = createCodec(registry);
		boolean bl = chunk.isLightOn();
		for (int i = lightingProvider.getBottomY(); i < lightingProvider.getTopY(); ++i) {
			int j2 = chunk.sectionCoordToIndex(i);
			boolean bl2 = j2 >= 0 && j2 < chunkSections.length;
			ChunkNibbleArray chunkNibbleArray = lightingProvider.get(LightType.BLOCK).getLightSection(ChunkSectionPos.from(chunkPos, i));
			ChunkNibbleArray chunkNibbleArray2 = lightingProvider.get(LightType.SKY).getLightSection(ChunkSectionPos.from(chunkPos, i));
			if (!bl2 && chunkNibbleArray == null && chunkNibbleArray2 == null) continue;
			NbtCompound nbtCompound2 = new NbtCompound();
			if (bl2) {
				ChunkSection chunkSection = chunkSections[j2];
				nbtCompound2.put("block_states", CODEC.encodeStart(NbtOps.INSTANCE, chunkSection.getBlockStateContainer()).get().left().orElse(new NbtCompound()));
				nbtCompound2.put("biomes", codec.encodeStart(NbtOps.INSTANCE, chunkSection.getBiomeContainer()).get().left().orElse(new NbtCompound()));
			}
			
			if (chunkNibbleArray != null && !chunkNibbleArray.isUninitialized()) {
				nbtCompound2.putByteArray("BlockLight", chunkNibbleArray.asByteArray());
			}
			
			if (chunkNibbleArray2 != null && !chunkNibbleArray2.isUninitialized()) {
				nbtCompound2.putByteArray("SkyLight", chunkNibbleArray2.asByteArray());
			}
			
			if (nbtCompound2.isEmpty())
				continue;

			nbtCompound2.putByte("Y", (byte)i);
			nbtList.add(nbtCompound2);
		}
		
		nbtCompound.put("sections", nbtList);
		if (bl) {
			nbtCompound.putBoolean("isLightOn", true);
		}
		
		NbtList i = new NbtList();
		for (BlockPos bl2 : chunk.getBlockEntityPositions()) {
			NbtCompound nbtCompound2 = chunk.getPackedBlockEntityNbt(bl2);
			if (nbtCompound2 == null) continue;
			i.add(nbtCompound2);
		}
		
		nbtCompound.put("block_entities", i);
		if (chunk.getStatus().getChunkType() == ChunkStatus.ChunkType.PROTOCHUNK) {
			NbtList bl2 = new NbtList();
			bl2.addAll(((ProtoChunk) chunk).getEntities());
			nbtCompound.put("entities", bl2);
			nbtCompound.put("Lights", ChunkSerializer.toNbt(((ProtoChunk) chunk).getLightSourcesBySection()));
			NbtCompound nbtCompound3 = new NbtCompound();
			for (GenerationStep.Carver carver : GenerationStep.Carver.values()) {
				CarvingMask carvingMask = ((ProtoChunk) chunk).getCarvingMask(carver);
				if (carvingMask == null)
					continue;
			
				nbtCompound3.putLongArray(carver.toString(), carvingMask.getMask());
			}
			
			nbtCompound.put("CarvingMasks", nbtCompound3);
		}
		
		serializeTicks(world, nbtCompound, chunk.getTickSchedulers());
		nbtCompound.put("PostProcessing", ChunkSerializer.toNbt(chunk.getPostProcessingLists()));
		NbtCompound dabruh = new NbtCompound();
		for (Map.Entry<Heightmap.Type, Heightmap> entry : chunk.getHeightmaps()) {
			if (!chunk.getStatus().getHeightmapTypes().contains(entry.getKey())) continue;
			dabruh.put(entry.getKey().getName(), new NbtLongArray(entry.getValue().asLongArray()));
		}
		
		nbtCompound.put("Heightmaps", dabruh);
		return nbtCompound;
	}

	private static Codec<PalettedContainer<Biome>> createCodec(Registry<Biome> biomeRegistry) {
        return PalettedContainer.createCodec(biomeRegistry, biomeRegistry.getCodec(), PalettedContainer.PaletteProvider.BIOME, biomeRegistry.getOrThrow(BiomeKeys.PLAINS));
    }
	
	private static void serializeTicks(ClientWorld world, NbtCompound nbt, Chunk.TickSchedulers tickSchedulers) {
        long l = world.getLevelProperties().getTime();
        nbt.put(BLOCK_TICKS, tickSchedulers.blocks().toNbt(l, block -> Registry.BLOCK.getId(block).toString()));
        nbt.put(FLUID_TICKS, tickSchedulers.fluids().toNbt(l, fluid -> Registry.FLUID.getId(fluid).toString()));
    }
}

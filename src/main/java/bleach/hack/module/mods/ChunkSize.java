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
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.DeflaterOutputStream;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventDrawOverlay;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.ShortTag;
import net.minecraft.server.world.SimpleTickScheduler;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.ChunkTickScheduler;
import net.minecraft.world.Heightmap;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.LightType;
import net.minecraft.world.TickScheduler;
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

public class ChunkSize extends Module {

	private long timer = 0;
	private int size = 0;

	public ChunkSize() {
		super("ChunkSize", KEY_UNBOUND, Category.WORLD, "Shows the size of the chunk you are standing in");
	}

	@Subscribe
	public void onOverlay(EventDrawOverlay event) {
		mc.textRenderer.drawWithShadow(event.matrix, "Chunk: " + (size < 1000 ? size + "B" : size / 1000d + "KB"), 120,
				5, -1);
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
			DataOutputStream output = new DataOutputStream(
					new BufferedOutputStream(new DeflaterOutputStream(new ByteArrayOutputStream(8096))));
			try {
				NbtIo.writeCompressed(tag, output);
			} catch (IOException e) {
			}
			size = output.size();
		}).start();
	}

	private CompoundTag serialize(ClientWorld world, Chunk chunk) {
		ChunkPos chunkPos = chunk.getPos();
		CompoundTag compoundTag = new CompoundTag();
		CompoundTag compoundTag2 = new CompoundTag();
		compoundTag.putInt("DataVersion", SharedConstants.getGameVersion().getWorldVersion());
		compoundTag.put("Level", compoundTag2);
		compoundTag2.putInt("xPos", chunkPos.x);
		compoundTag2.putInt("zPos", chunkPos.z);
		compoundTag2.putLong("LastUpdate", world.getTime());
		compoundTag2.putLong("InhabitedTime", chunk.getInhabitedTime());
		compoundTag2.putString("Status", chunk.getStatus().getId());
		UpgradeData upgradeData = chunk.getUpgradeData();
		if (!upgradeData.isDone()) {
			compoundTag2.put("UpgradeData", upgradeData.toTag());
		}

		ChunkSection[] chunkSections = chunk.getSectionArray();
		ListTag listTag = new ListTag();
		LightingProvider lightingProvider = world.getChunkManager().getLightingProvider();
		boolean bl = chunk.isLightOn();

		CompoundTag compoundTag7;
		for (int i = -1; i < 17; ++i) {
			final int finalI = i;
			ChunkSection chunkSection = Arrays.stream(chunkSections).filter(chunkSectionx -> (chunkSectionx != null && chunkSectionx.getYOffset() >> 4 == finalI)).findFirst().orElse(WorldChunk.EMPTY_SECTION);
			ChunkNibbleArray chunkNibbleArray = lightingProvider.get(LightType.BLOCK)
					.getLightSection(ChunkSectionPos.from(chunkPos, i));
			ChunkNibbleArray chunkNibbleArray2 = lightingProvider.get(LightType.SKY)
					.getLightSection(ChunkSectionPos.from(chunkPos, i));
			if (chunkSection != WorldChunk.EMPTY_SECTION || chunkNibbleArray != null || chunkNibbleArray2 != null) {
				compoundTag7 = new CompoundTag();
				compoundTag7.putByte("Y", (byte) (i & 255));
				if (chunkSection != WorldChunk.EMPTY_SECTION) {
					chunkSection.getContainer().write(compoundTag7, "Palette", "BlockStates");
				}

				if (chunkNibbleArray != null && !chunkNibbleArray.isUninitialized()) {
					compoundTag7.putByteArray("BlockLight", chunkNibbleArray.asByteArray());
				}

				if (chunkNibbleArray2 != null && !chunkNibbleArray2.isUninitialized()) {
					compoundTag7.putByteArray("SkyLight", chunkNibbleArray2.asByteArray());
				}

				listTag.add(compoundTag7);
			}
		}

		compoundTag2.put("Sections", listTag);
		if (bl) {
			compoundTag2.putBoolean("isLightOn", true);
		}

		BiomeArray biomeArray = chunk.getBiomeArray();
		if (biomeArray != null) {
			compoundTag2.putIntArray("Biomes", biomeArray.toIntArray());
		}

		ListTag listTag2 = new ListTag();
		Iterator<BlockPos> var21 = chunk.getBlockEntityPositions().iterator();

		CompoundTag compoundTag6;
		while (var21.hasNext()) {
			BlockPos blockPos = var21.next();
			compoundTag6 = chunk.getPackedBlockEntityTag(blockPos);
			if (compoundTag6 != null) {
				listTag2.add(compoundTag6);
			}
		}

		compoundTag2.put("TileEntities", listTag2);
		ListTag listTag3 = new ListTag();
		if (chunk.getStatus().getChunkType() == ChunkStatus.ChunkType.field_12807) {
			WorldChunk worldChunk = (WorldChunk) chunk;
			worldChunk.setUnsaved(false);

			for (int k = 0; k < worldChunk.getEntitySectionArray().length; ++k) {
				Iterator<Entity> var29 = worldChunk.getEntitySectionArray()[k].iterator();

				while (var29.hasNext()) {
					Entity entity = var29.next();
					CompoundTag compoundTag5 = new CompoundTag();
					if (entity.saveToTag(compoundTag5)) {
						worldChunk.setUnsaved(true);
						listTag3.add(compoundTag5);
					}
				}
			}
		} else {
			ProtoChunk protoChunk = (ProtoChunk) chunk;
			listTag3.addAll(protoChunk.getEntities());
			compoundTag2.put("Lights", toNbt(protoChunk.getLightSourcesBySection()));
			compoundTag6 = new CompoundTag();
			GenerationStep.Carver[] var30 = GenerationStep.Carver.values();
			int var32 = var30.length;

			for (int var34 = 0; var34 < var32; ++var34) {
				GenerationStep.Carver carver = var30[var34];
				BitSet bitSet = protoChunk.getCarvingMask(carver);
				if (bitSet != null) {
					compoundTag6.putByteArray(carver.toString(), bitSet.toByteArray());
				}
			}

			compoundTag2.put("CarvingMasks", compoundTag6);
		}

		compoundTag2.put("Entities", listTag3);
		TickScheduler<Block> tickScheduler = chunk.getBlockTickScheduler();
		if (tickScheduler instanceof ChunkTickScheduler) {
			compoundTag2.put("ToBeTicked", ((ChunkTickScheduler<Block>) tickScheduler).toNbt());
		} else if (tickScheduler instanceof SimpleTickScheduler) {
			compoundTag2.put("TileTicks", ((SimpleTickScheduler<Block>) tickScheduler).toNbt());
		}

		TickScheduler<Fluid> tickScheduler2 = chunk.getFluidTickScheduler();
		if (tickScheduler2 instanceof ChunkTickScheduler) {
			compoundTag2.put("LiquidsToBeTicked", ((ChunkTickScheduler<Fluid>) tickScheduler2).toNbt());
		} else if (tickScheduler2 instanceof SimpleTickScheduler) {
			compoundTag2.put("LiquidTicks", ((SimpleTickScheduler<Fluid>) tickScheduler2).toNbt());
		}

		compoundTag2.put("PostProcessing", toNbt(chunk.getPostProcessingLists()));
		compoundTag7 = new CompoundTag();
		Iterator<Entry<Type, Heightmap>> var33 = chunk.getHeightmaps().iterator();

		while (var33.hasNext()) {
			Entry<Heightmap.Type, Heightmap> entry = var33.next();
			if (chunk.getStatus().getHeightmapTypes().contains(entry.getKey())) {
				compoundTag7.put(entry.getKey().getName(),
						new LongArrayTag(entry.getValue().asLongArray()));
			}
		}

		compoundTag2.put("Heightmaps", compoundTag7);
		compoundTag2.put("Structures",
				writeStructures(chunkPos, chunk.getStructureStarts(), chunk.getStructureReferences()));
		return compoundTag;
	}

	private CompoundTag writeStructures(ChunkPos pos, Map<StructureFeature<?>, StructureStart<?>> structureStarts,
			Map<StructureFeature<?>, LongSet> structureReferences) {
		CompoundTag compoundTag = new CompoundTag();
		CompoundTag compoundTag2 = new CompoundTag();
		Iterator<Entry<StructureFeature<?>, StructureStart<?>>> var5 = structureStarts.entrySet().iterator();

		while (var5.hasNext()) {
			Entry<StructureFeature<?>, StructureStart<?>> entry = var5
					.next();
			compoundTag2.put(((StructureFeature<?>) entry.getKey()).getName(),
					((StructureStart<?>) entry.getValue()).toTag(pos.x, pos.z));
		}

		compoundTag.put("Starts", compoundTag2);
		CompoundTag compoundTag3 = new CompoundTag();
		Iterator<Entry<StructureFeature<?>, LongSet>> var9 = structureReferences.entrySet().iterator();

		while (var9.hasNext()) {
			Entry<StructureFeature<?>, LongSet> entry2 = var9.next();
			compoundTag3.put(((StructureFeature<?>) entry2.getKey()).getName(),
					new LongArrayTag(entry2.getValue()));
		}

		compoundTag.put("References", compoundTag3);
		return compoundTag;
	}

	private ListTag toNbt(ShortList[] lists) {
		ListTag listTag = new ListTag();
		ShortList[] var2 = lists;
		int var3 = lists.length;

		for (int var4 = 0; var4 < var3; ++var4) {
			ShortList shortList = var2[var4];
			ListTag listTag2 = new ListTag();
			if (shortList != null) {
				ShortListIterator var7 = shortList.iterator();

				while (var7.hasNext()) {
					Short short_ = var7.nextShort();
					listTag2.add(ShortTag.of(short_));
				}
			}

			listTag.add(listTag2);
		}

		return listTag;
	}
}

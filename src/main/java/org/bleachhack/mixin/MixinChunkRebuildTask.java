/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import org.bleachhack.BleachHack;
import org.bleachhack.event.events.EventRenderBlock;
import org.bleachhack.event.events.EventRenderFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkBuilder.ChunkData;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

/**
 * Blocks are still tesselated even if they're transparent because Minecraft's
 * rendering engine is poop.
 */
@Mixin(targets = "net.minecraft.client.render.chunk.ChunkBuilder$BuiltChunk$RebuildTask")
public class MixinChunkRebuildTask {

	@Unique private static boolean OPTIFABRIC_INSTALLED = FabricLoader.getInstance().isModLoaded("optifabric");

	@Shadow private /* outer */ ChunkBuilder.BuiltChunk field_20839;
	@Shadow private ChunkRendererRegion region;

	@Shadow private <E extends BlockEntity> void addBlockEntity(ChunkData data, Set<BlockEntity> blockEntities, E blockEntity) {}
	@Shadow private Set<BlockEntity> render(float cameraX, float cameraY, float cameraZ, ChunkData data, BlockBufferBuilderStorage buffers) { return null; }

	// i have gone past the point of insanity
	@Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk$RebuildTask;render(FFFLnet/minecraft/client/render/chunk/ChunkBuilder$ChunkData;Lnet/minecraft/client/render/chunk/BlockBufferBuilderStorage;)Ljava/util/Set;"))
	private Set<BlockEntity> run_render(@Coerce Object thisObject, float cameraX, float cameraY, float cameraZ, ChunkData data, BlockBufferBuilderStorage buffers) {
		return OPTIFABRIC_INSTALLED 
				? render(cameraX, cameraY, cameraZ, data, buffers) : newRender(cameraX, cameraY, cameraZ, data, buffers);
	}

	private Set<BlockEntity> newRender(float cameraX, float cameraY, float cameraZ, ChunkData data, BlockBufferBuilderStorage buffers) {
		BlockPos blockPos = field_20839.getOrigin().toImmutable();
		BlockPos blockPos2 = blockPos.add(15, 15, 15);

		ChunkOcclusionDataBuilder chunkOcclusionDataBuilder = new ChunkOcclusionDataBuilder();
		Set<BlockEntity> set = new HashSet<>();
		ChunkRendererRegion chunkRendererRegion = this.region;
		this.region = null;
		MatrixStack matrixStack = new MatrixStack();
		if (chunkRendererRegion != null) {
			BlockModelRenderer.enableBrightnessCache();
			Random random = new Random();
			BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();

			for (BlockPos blockPos3 : BlockPos.iterate(blockPos, blockPos2)) {
				BlockState blockState = chunkRendererRegion.getBlockState(blockPos3);
				if (blockState.isOpaqueFullCube(chunkRendererRegion, blockPos3)) {
					chunkOcclusionDataBuilder.markClosed(blockPos3);
				}

				if (blockState.hasBlockEntity()) {
					BlockEntity blockEntityx = chunkRendererRegion.getBlockEntity(blockPos3, WorldChunk.CreationType.CHECK);
					if (blockEntityx != null) {
						this.addBlockEntity(data, set, blockEntityx);
					}
				}

				FluidState fluid = chunkRendererRegion.getFluidState(blockPos3);
				if (!fluid.isEmpty()) {
					RenderLayer renderLayer = RenderLayers.getFluidLayer(fluid);
					BufferBuilder bufferBuilder = buffers.get(renderLayer);
					if (data.initializedLayers.add(renderLayer)) {
						bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
					}

					EventRenderFluid event = new EventRenderFluid(fluid, blockPos3, bufferBuilder);
					BleachHack.eventBus.post(event);

					if (event.isCancelled())
						continue;

					if (blockRenderManager.renderFluid(blockPos3, chunkRendererRegion, bufferBuilder, fluid)) {
						data.empty = false;
						data.nonEmptyLayers.add(renderLayer);
					}
				}

				if (blockState.getRenderType() != BlockRenderType.INVISIBLE) {
					RenderLayer renderLayer = RenderLayers.getBlockLayer(blockState);
					BufferBuilder bufferBuilder = buffers.get(renderLayer);
					if (data.initializedLayers.add(renderLayer)) {
						bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
					}

					EventRenderBlock.Tesselate event = new EventRenderBlock.Tesselate(blockState, blockPos3, matrixStack, bufferBuilder);
					BleachHack.eventBus.post(event);

					if (event.isCancelled())
						continue;

					matrixStack.push();
					matrixStack.translate(blockPos3.getX() & 15, blockPos3.getY() & 15, blockPos3.getZ() & 15);

					if (blockRenderManager.renderBlock(blockState, blockPos3, chunkRendererRegion, matrixStack, bufferBuilder, true, random)) {
						data.empty = false;
						data.nonEmptyLayers.add(renderLayer);
					}

					bufferBuilder.unfixColor();
					matrixStack.pop();
				}
			}

			if (data.nonEmptyLayers.contains(RenderLayer.getTranslucent())) {
				BufferBuilder bufferBuilder2 = buffers.get(RenderLayer.getTranslucent());
				bufferBuilder2.setCameraPosition(cameraX - (float)blockPos.getX(), cameraY - (float)blockPos.getY(), cameraZ - (float)blockPos.getZ());
				data.bufferState = bufferBuilder2.popState();
			}

			Stream<RenderLayer> var10000 = data.initializedLayers.stream();
			Objects.requireNonNull(buffers);
			var10000.map(buffers::get).forEach(BufferBuilder::end);
			BlockModelRenderer.disableBrightnessCache();
		}

		data.occlusionGraph = chunkOcclusionDataBuilder.build();
		return set;
	}
}

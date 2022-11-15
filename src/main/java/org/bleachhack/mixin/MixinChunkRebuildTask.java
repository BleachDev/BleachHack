/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
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

/**
 * Blocks are still tesselated even if they're transparent because Minecraft's
 * rendering engine is poop.
 */
@Mixin(ChunkBuilder.BuiltChunk.RebuildTask.class)
public class MixinChunkRebuildTask {

	@Unique private static boolean OPTIFABRIC_INSTALLED = FabricLoader.getInstance().isModLoaded("optifabric");

	@Shadow private /* outer */ ChunkBuilder.BuiltChunk field_20839;
	@Shadow private ChunkRendererRegion region;

	@Shadow private <E extends BlockEntity> void addBlockEntity(ChunkBuilder.BuiltChunk.RebuildTask.RenderData renderData, E blockEntity) {}
	@Shadow private ChunkBuilder.BuiltChunk.RebuildTask.RenderData render(float cameraX, float cameraY, float cameraZ, BlockBufferBuilderStorage buffers) { return null; }

	// i have gone past the point of insanity
	@Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk$RebuildTask;render(FFFLnet/minecraft/client/render/chunk/BlockBufferBuilderStorage;)Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk$RebuildTask$RenderData;"))
	private ChunkBuilder.BuiltChunk.RebuildTask.RenderData run_render(@Coerce Object thisObject, float cameraX, float cameraY, float cameraZ, BlockBufferBuilderStorage buffers) {
		return OPTIFABRIC_INSTALLED 
				? render(cameraX, cameraY, cameraZ, buffers) : newRender(cameraX, cameraY, cameraZ, buffers);
	}

	private ChunkBuilder.BuiltChunk.RebuildTask.RenderData newRender(float cameraX, float cameraY, float cameraZ, BlockBufferBuilderStorage buffers) {
		ChunkBuilder.BuiltChunk.RebuildTask.RenderData renderData = new ChunkBuilder.BuiltChunk.RebuildTask.RenderData();
		BlockPos blockPos = field_20839.getOrigin().toImmutable();
		BlockPos blockPos2 = blockPos.add(15, 15, 15);
		ChunkOcclusionDataBuilder chunkOcclusionDataBuilder = new ChunkOcclusionDataBuilder();
		ChunkRendererRegion chunkRendererRegion = this.region;
		this.region = null;
		MatrixStack matrixStack = new MatrixStack();
		if (chunkRendererRegion != null) {
			BlockModelRenderer.enableBrightnessCache();
			Set<RenderLayer> set = new ReferenceArraySet(RenderLayer.getBlockLayers().size());
			net.minecraft.util.math.random.Random random = net.minecraft.util.math.random.Random.create();
			BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
			Iterator var15 = BlockPos.iterate(blockPos, blockPos2).iterator();

			while(var15.hasNext()) {
				BlockPos blockPos3 = (BlockPos)var15.next();
				BlockState blockState = chunkRendererRegion.getBlockState(blockPos3);
				if (blockState.isOpaqueFullCube(chunkRendererRegion, blockPos3)) {
					chunkOcclusionDataBuilder.markClosed(blockPos3);
				}

				if (blockState.hasBlockEntity()) {
					BlockEntity blockEntity = chunkRendererRegion.getBlockEntity(blockPos3);
					if (blockEntity != null) {
						this.addBlockEntity(renderData, blockEntity);
					}
				}

				BlockState blockState2 = chunkRendererRegion.getBlockState(blockPos3);
				FluidState fluidState = blockState2.getFluidState();
				RenderLayer renderLayer;
				BufferBuilder bufferBuilder;
				if (!fluidState.isEmpty()) {
					renderLayer = RenderLayers.getFluidLayer(fluidState);
					bufferBuilder = buffers.get(renderLayer);

					EventRenderFluid event = new EventRenderFluid(fluidState, blockPos3, bufferBuilder);
					BleachHack.eventBus.post(event);

					if (event.isCancelled())
						continue;

					if (set.add(renderLayer)) {
						bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
					}

					blockRenderManager.renderFluid(blockPos3, chunkRendererRegion, bufferBuilder, blockState2, fluidState);
				}

				if (blockState.getRenderType() != BlockRenderType.INVISIBLE) {
					renderLayer = RenderLayers.getBlockLayer(blockState);
					bufferBuilder = buffers.get(renderLayer);
					if (set.add(renderLayer)) {
						bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
					}

					EventRenderBlock.Tesselate event = new EventRenderBlock.Tesselate(blockState, blockPos3, matrixStack, bufferBuilder);
					BleachHack.eventBus.post(event);

					if (event.isCancelled())
						continue;

					matrixStack.push();
					matrixStack.translate((double)(blockPos3.getX() & 15), (double)(blockPos3.getY() & 15), (double)(blockPos3.getZ() & 15));
					blockRenderManager.renderBlock(blockState, blockPos3, chunkRendererRegion, matrixStack, bufferBuilder, true, random);
					matrixStack.pop();
				}
			}

			if (set.contains(RenderLayer.getTranslucent())) {
				BufferBuilder bufferBuilder2 = buffers.get(RenderLayer.getTranslucent());
				if (!bufferBuilder2.isBatchEmpty()) {
					bufferBuilder2.sortFrom(cameraX - (float)blockPos.getX(), cameraY - (float)blockPos.getY(), cameraZ - (float)blockPos.getZ());
					renderData.translucencySortingData = bufferBuilder2.popState();
				}
			}

			var15 = set.iterator();

			while(var15.hasNext()) {
				RenderLayer renderLayer2 = (RenderLayer)var15.next();
				BufferBuilder.BuiltBuffer builtBuffer = buffers.get(renderLayer2).endNullable();
				if (builtBuffer != null) {
					renderData.field_39081.put(renderLayer2, builtBuffer);
				}
			}

			BlockModelRenderer.disableBrightnessCache();
		}

		renderData.chunkOcclusionData = chunkOcclusionDataBuilder.build();
		return renderData;
	}
}

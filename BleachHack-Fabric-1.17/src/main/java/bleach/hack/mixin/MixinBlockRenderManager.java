/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Xray;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.FernBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

/**
 * Blocks are still tesselated even if they're transparent because Minecraft's
 * rendering engine is retarded.
 */
@Mixin(BlockRenderManager.class)
public class MixinBlockRenderManager {

	@Inject(method = "renderBlock", at = @At("HEAD"), cancellable = true)
	private void renderBlock_head(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random, CallbackInfoReturnable<Boolean> ci) {
		Xray xray = (Xray) ModuleManager.getModule("Xray");

		if (xray.isEnabled() && !xray.isVisible(state.getBlock())) {
			if (xray.getSetting(1).asToggle().state) {
				if (xray.getSetting(1).asToggle().getChild(1).asToggle().state
						&& (state.getBlock() instanceof FernBlock
								|| state.getBlock().getClass() == TallPlantBlock.class
								|| WorldUtils.getTopBlockIgnoreLeaves(pos.getX(), pos.getZ()) == pos.getY())) {
					ci.setReturnValue(false);
					return;
				}

				vertexConsumer.fixedColor(-1, -1, -1, xray.getSetting(1).asToggle().getChild(0).asSlider().getValueInt());
			} else {
				ci.setReturnValue(false);
			}
		}
	}

	@Inject(method = "renderBlock", at = @At("RETURN"))
	private void renderBlock_return(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random, CallbackInfoReturnable<Boolean> ci) {
		vertexConsumer.unfixColor();
	}
}

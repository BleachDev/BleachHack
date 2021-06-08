/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Xray;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;

@Mixin(FluidRenderer.class)
public class MixinFluidRenderer {

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	public void render(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, FluidState state, CallbackInfoReturnable<Boolean> callback) {
		Xray xray = (Xray) ModuleManager.getModule("Xray");
		if (xray.getSetting(0).asToggle().state)
			return;

		if (xray.isEnabled() && !xray.isVisible(state.getBlockState().getBlock())) {
			callback.setReturnValue(false);
		}
	}

	@Inject(method = "isSideCovered(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;F)Z", at = @At("HEAD"), cancellable = true)
	private static void isSideCovered(BlockView world, BlockPos pos, Direction direction, float maxDeviation, CallbackInfoReturnable<Boolean> callback) {
		Xray xray = (Xray) ModuleManager.getModule("Xray");

		if (!xray.getSetting(0).asToggle().state && xray.isVisible(world.getBlockState(pos).getBlock())) {
			callback.setReturnValue(false);
		}
	}
}

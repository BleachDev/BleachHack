/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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

	@Inject(method = "isSideCovered", at = @At("HEAD"), cancellable = true)
	private static void isSideCovered(BlockView world, BlockPos pos, Direction direction, float maxDeviation, CallbackInfoReturnable<Boolean> callback) {
		Xray xray = (Xray) ModuleManager.getModule("Xray");

		if (!xray.getSetting(0).asToggle().state && xray.isEnabled() && xray.isVisible(world.getBlockState(pos).getBlock())) {
			callback.setReturnValue(false);
		}
	}
}

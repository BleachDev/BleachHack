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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

@Mixin(Block.class)
public class MixinBlock {

	@Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
	private static void shouldDrawSide(BlockState state, BlockView world, BlockPos pos, Direction facing, CallbackInfoReturnable<Boolean> callback) {
		Xray xray = (Xray) ModuleManager.getModule("Xray");
		if (xray.isEnabled()) {
			callback.setReturnValue(xray.isVisible(state.getBlock()));
			callback.cancel();
		}
	}

	@Inject(method = "isShapeFullCube", at = @At("HEAD"), cancellable = true)
	private static void isShapeFullCube(VoxelShape shape, CallbackInfoReturnable<Boolean> callback) {
		if (ModuleManager.getModule("Xray").isEnabled()) {
			callback.setReturnValue(false);
		}
	}

	/* @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
	 * public void getRenderType(CallbackInfoReturnable<BlockRenderType> callback) {
	 * try { if (ModuleManager.getModule(Xray.class).isToggled()) {
	 * callback.setReturnValue(BlockRenderType.INVISIBLE); callback.cancel(); } }
	 * catch (Exception ignored) {} } */
}

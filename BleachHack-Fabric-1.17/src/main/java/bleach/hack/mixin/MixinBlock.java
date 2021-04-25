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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

@Mixin(Block.class)
public class MixinBlock {

	@Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
	private static void shouldDrawSide(BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos blockPos, CallbackInfoReturnable<Boolean> callback) {
		Xray xray = (Xray) ModuleManager.getModule("Xray");

		if (xray.isEnabled()) {
			callback.setReturnValue(xray.isVisible(state.getBlock()));
		}
	}

	@Inject(method = "isShapeFullCube", at = @At("HEAD"), cancellable = true)
	private static void isShapeFullCube(VoxelShape shape, CallbackInfoReturnable<Boolean> callback) {
		if (ModuleManager.getModule("Xray").isEnabled()) {
			callback.setReturnValue(false);
		}
	}

	/*@Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
	public void getRenderType(CallbackInfoReturnable<BlockRenderType> callback) {
		try {
			if (ModuleManager.getModuleByClass(Xray.class).isToggled()) {
				callback.setReturnValue(BlockRenderType.INVISIBLE); callback.cancel();
			}
		} catch (Exception ignored) {
		}
	}*/
}

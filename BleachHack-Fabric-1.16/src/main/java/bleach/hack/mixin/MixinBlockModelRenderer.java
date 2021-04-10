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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Xray;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

/**
 * Blocks are still tesselated even if they're transparent because Minecraft's
 * rendering engine is retarded.
 */
@Mixin(BlockModelRenderer.class)
public class MixinBlockModelRenderer {

	// don't use render because sodium small pp
	@Inject(method = "renderQuad", at = @At("HEAD"), cancellable = true)
	private void renderQuad(BlockRenderView world, BlockState state, BlockPos pos, VertexConsumer vertexConsumer, MatrixStack.Entry matrixEntry, BakedQuad quad,
			float brightness0, float brightness1, float brightness2, float brightness3, int light0, int light1, int light2, int light3, int overlay, CallbackInfo ci) {
		if (!((Xray) ModuleManager.getModule("Xray")).isVisible(state.getBlock())) {
			ci.cancel();
		}
	}

	/* @ModifyArg(method = "render", at = @At(value = "HEAD", target = "Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/Random;JI)Z"))
	 * private boolean renderSmooth(boolean boolean_1) {
	 *     if (ModuleManager.getModule(Xray.class).isToggled()) {
	 *         return false;
	 *     }
	 * 
	 *     return boolean_1;
	 * }
	 *
	 * @ModifyArg(method = "render", at = @At(value = "HEAD", target = "Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/Random;JI)Z"))
	 * private boolean renderFlat(boolean boolean_1) {
	 *     if (ModuleManager.getModule(Xray.class).isToggled()) {
	 *         return false;
	 *     }
	 *
	 *     return boolean_1;
	 * } */

}

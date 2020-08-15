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
package bleach.hack.mixin;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Xray;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidRenderer.class)
public class MixinFluidRenderer {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(BlockRenderView extendedBlockView_1, BlockPos blockPos_1, VertexConsumer vertexConsumer_1, FluidState fluidState_1, CallbackInfoReturnable<Boolean> callbackInfo) {
        Xray xray = (Xray) ModuleManager.getModule(Xray.class);
        if (xray.getSetting(0).asToggle().state) return;
        if (xray.isToggled() && !xray.isVisible(fluidState_1.getBlockState().getBlock())) {
            callbackInfo.setReturnValue(false);
            callbackInfo.cancel();
        }
    }

    @Inject(method = "isSideCovered", at = @At("HEAD"), cancellable = true)
    private static void isSideCovered(BlockView blockView_1, BlockPos blockPos_1, Direction direction_1, float float_1, CallbackInfoReturnable<Boolean> callbackInfo) {
        Xray xray = (Xray) ModuleManager.getModule(Xray.class);
        if (xray.getSetting(0).asToggle().state) return;
        if (xray.isToggled() && xray.isVisible(blockView_1.getBlockState(blockPos_1).getBlock())) {
            callbackInfo.setReturnValue(false);
            callbackInfo.cancel();
        }
    }
}

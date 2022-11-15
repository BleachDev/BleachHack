/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import org.bleachhack.BleachHack;
import org.bleachhack.event.events.EventBiomeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.level.ColorResolver;

@Mixin(BiomeColors.class)
public class MixinBiomeColors {

	@Inject(method = "getColor(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/level/ColorResolver;)I", at = @At("RETURN"), cancellable = true)
	private static void getColor(BlockRenderView world, BlockPos pos, ColorResolver resolver, CallbackInfoReturnable<Integer> callback) {
		if (MinecraftClient.getInstance().world != null) {
			EventBiomeColor event = 
					resolver == BiomeColors.FOLIAGE_COLOR ? new EventBiomeColor.Foilage(world, pos, callback.getReturnValueI()) :
						resolver == BiomeColors.GRASS_COLOR ? new EventBiomeColor.Grass(world, pos, callback.getReturnValueI()) :
							resolver == BiomeColors.WATER_COLOR ? new EventBiomeColor.Water(world, pos, callback.getReturnValueI()) :
								null;

			if (event != null) {
				BleachHack.eventBus.post(event);
				callback.setReturnValue(event.getColor());
			}
		}
	}
}

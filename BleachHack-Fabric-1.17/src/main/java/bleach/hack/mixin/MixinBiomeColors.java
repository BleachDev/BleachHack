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
import org.spongepowered.asm.mixin.Overwrite;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventBiomeColor;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.level.ColorResolver;

@Mixin(BiomeColors.class)
public class MixinBiomeColors {

	@Overwrite
	private static int getColor(BlockRenderView world, BlockPos pos, ColorResolver resolver) {
		EventBiomeColor event = 
				resolver == BiomeColors.FOLIAGE_COLOR ? new EventBiomeColor.Foilage(world, pos) :
					resolver == BiomeColors.GRASS_COLOR ? new EventBiomeColor.Grass(world, pos) :
						resolver == BiomeColors.WATER_COLOR ? new EventBiomeColor.Water(world, pos) : null;
		
		if (event != null) {
			BleachHack.eventBus.post(event);
			
			if (event.getColor() != null) {
				return event.getColor();
			}
		}
		
		return world.getColor(pos, resolver);
	}
}

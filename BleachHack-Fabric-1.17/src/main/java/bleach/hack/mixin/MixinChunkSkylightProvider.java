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
import bleach.hack.module.mods.NoRender;
import net.minecraft.world.chunk.light.ChunkSkyLightProvider;

@Mixin(ChunkSkyLightProvider.class)
public class MixinChunkSkylightProvider {

	@Inject(method = "recalculateLevel", at = @At("HEAD"), cancellable = true)
	protected void recalculateLevel(long id, long excludedId, int maxLevel, CallbackInfoReturnable<Integer> ci) {
		if (((NoRender) ModuleManager.getModule("NoRender")).shouldRemoveWorld(4)) {
			ci.setReturnValue(15);
		}
	}
}

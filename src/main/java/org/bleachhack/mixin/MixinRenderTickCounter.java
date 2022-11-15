/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.RenderTickCounter;

@Mixin(RenderTickCounter.class)
public class MixinRenderTickCounter {

	@Shadow private float lastFrameDuration;
	@Shadow private float tickDelta;
	@Shadow private long prevTimeMillis;
	@Shadow private float tickTime;

	@Inject(method = "beginRenderTick", at = @At("HEAD"), cancellable = true)
	private void beginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> ci) {
		if (ModuleManager.getModule(Timer.class).isEnabled()) {
			this.lastFrameDuration = (float) (((timeMillis - this.prevTimeMillis) / this.tickTime)
					* ModuleManager.getModule(Timer.class).getSetting(0).asSlider().getValue());
			this.prevTimeMillis = timeMillis;
			this.tickDelta += this.lastFrameDuration;
			int i = (int) this.tickDelta;
			this.tickDelta -= i;

			ci.setReturnValue(i);
		}
	}

}

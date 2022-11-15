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
import org.bleachhack.event.events.EventSkyRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.DimensionEffects;

@Mixin(DimensionEffects.class)
public class MixinDimensionEffects {

	@Inject(method = "getFogColorOverride", at = @At("HEAD"), cancellable = true)
	private void getFogColorOverride(float skyAngle, float tickDelta, CallbackInfoReturnable<float[]> ci) {
		EventSkyRender.Color.FogColor  event = new EventSkyRender.Color.FogColor(tickDelta);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			ci.setReturnValue(null);
		} else if (event.getColor() != null) {
			ci.setReturnValue(new float[] { (float) event.getColor().x, (float) event.getColor().y, (float) event.getColor().z, 1f });
		}
	}
}

/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Camera;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer {

	@Inject(method = "applyFog", at = @At("TAIL"))
	private static void onApplyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
		if (ModuleManager.getModule(NoRender.class).isOverlayToggled(0)) {
			if (fogType == BackgroundRenderer.FogType.FOG_TERRAIN) {
				RenderSystem.setShaderFogStart(viewDistance * 4);
				RenderSystem.setShaderFogEnd(viewDistance * 4.25f);
			}
		}
	}
}

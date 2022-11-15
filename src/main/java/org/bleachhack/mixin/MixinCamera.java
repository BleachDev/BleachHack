/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.BetterCamera;
import org.bleachhack.module.mods.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;

@Mixin(Camera.class)
public class MixinCamera {

	@Unique private boolean bypassCameraClip;

	@Shadow private double clipToSpace(double desiredCameraDistance) { return 0; }

	@Inject(method = "getSubmersionType", at = @At("HEAD"), cancellable = true)
	private void getSubmergedFluidState(CallbackInfoReturnable<CameraSubmersionType> ci) {
		if (ModuleManager.getModule(NoRender.class).isOverlayToggled(3)) {
			ci.setReturnValue(CameraSubmersionType.NONE);
		}
	}

	@Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
	private void onClipToSpace(double desiredCameraDistance, CallbackInfoReturnable<Double> info) {
		if (bypassCameraClip) {
			bypassCameraClip = false;
		} else {
			Module betterCamera = ModuleManager.getModule(BetterCamera.class);

			if (betterCamera.isEnabled()) {
				if (betterCamera.getSetting(0).asToggle().getState()) {
					info.setReturnValue(betterCamera.getSetting(1).asToggle().getState()
							? betterCamera.getSetting(1).asToggle().getChild(0).asSlider().getValue() : desiredCameraDistance);
				} else if (betterCamera.getSetting(1).asToggle().getState()) {
					bypassCameraClip = true;
					info.setReturnValue(clipToSpace(betterCamera.getSetting(1).asToggle().getChild(0).asSlider().getValue()));
				}
			}
		}
	}
}

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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

@Mixin(ShaderEffect.class)
public class MixinShaderEffect {

	// Quick fix to allow overriding the ShaderEffect class with a null location to add custom behavior
	@Inject(method = "parseEffect", at = @At("HEAD"), cancellable = true)
	public void parseEffect(TextureManager textureManager, Identifier location, CallbackInfo info) {
		if (textureManager == null || location == null || location.getPath().isEmpty()) {
			info.cancel();
		}
	}
}

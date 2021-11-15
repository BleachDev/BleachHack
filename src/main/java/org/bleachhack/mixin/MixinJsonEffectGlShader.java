/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.client.gl.JsonEffectGlShader;
import net.minecraft.util.Identifier;

// Tweaks to the json effect shader class to make it compatible with custom identifiers
@Mixin(JsonEffectGlShader.class)
public class MixinJsonEffectGlShader {
	
	@Redirect(method = "<init>", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/util/Identifier;"))
	public Identifier init_identifier(String string) {
		return replaceIdentifier(string);
	}

	@Redirect(method = "loadEffect", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/util/Identifier;"))
	private static Identifier loadEffect_identifier(String string) {
		return replaceIdentifier(string);
	}
	
	private static Identifier replaceIdentifier(String string) {
		int idEnd = string.indexOf(':');
		if (idEnd != -1) {
			int idStart = string.substring(0, idEnd).lastIndexOf('/') + 1;
			return new Identifier(string.substring(idStart, idEnd), string.substring(0, idStart) + string.substring(idEnd + 1));
		}

		return new Identifier(string);
	}
}

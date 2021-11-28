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
import org.spongepowered.asm.mixin.injection.ModifyArg;
import net.minecraft.client.gl.JsonEffectGlShader;

// Tweaks to the json effect shader class to make it compatible with custom identifiers
@Mixin(JsonEffectGlShader.class)
public class MixinJsonEffectGlShader {
	
	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Identifier;<init>(Ljava/lang/String;)V"), index = 0)
	public String init_identifier(String string) {
		return replaceIdentifier(string);
	}

	@ModifyArg(method = "loadEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Identifier;<init>(Ljava/lang/String;)V"), index = 0)
	private static String loadEffect_identifier(String string) {
		return replaceIdentifier(string);
	}
	
	private static String replaceIdentifier(String string) {
		int idEnd = string.indexOf(':');
		if (idEnd != -1) {
			int idStart = string.substring(0, idEnd).lastIndexOf('/') + 1;
			return idStart == 0 ? string : string.substring(idStart, idEnd) + ":" + string.substring(0, idStart) + string.substring(idEnd + 1);
		}

		return string;
	}
}

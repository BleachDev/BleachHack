/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gl.JsonEffectGlShader;
import net.minecraft.client.gl.Program;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

// Tweaks to the json effect shader class to make it compatible with OpenResourceManager
@Mixin(JsonEffectGlShader.class)
public class MixinJsonEffectGlShader {
	
	// Patch for Architectury
	@Inject(method = "mojangPls(Lnet/minecraft/util/Identifier;Ljava/lang/String;)Lnet/minecraft/util/Identifier;", at = @At("HEAD"), cancellable = true, require = 0)
	private static void mojangPls(Identifier id, String ext, CallbackInfoReturnable<Identifier> callback) {
		callback.setReturnValue(
				"__url__".equals(id.getNamespace()) ? id : new Identifier(id.getNamespace(), "shaders/program/" + id.getPath() + ext));
	}
	
	@ModifyArgs(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Identifier;<init>(Ljava/lang/String;)V"))
	private void init_identifier(Args args, ResourceManager resourceManager, String name) {
		args.set(0, replaceIdentifier(args.get(0), name));
	}

	@ModifyArgs(method = "loadEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Identifier;<init>(Ljava/lang/String;)V"))
	private static void loadEffect_identifier(Args args, ResourceManager resourceManager, Program.Type type, String name) {
		args.set(0, replaceIdentifier(args.get(0), name));
	}

	private static String replaceIdentifier(String string, String name) {
		String[] split = name.split(":");
		if (split.length > 1) {
			if ("__url__".equals(split[0]))
				return name;

			return split[0] + ":" + string.replace(name, split[1]);
		}

		return string;
	}
}

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
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.client.gl.Program;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

// Tweaks to the shader class to make it compatible with OpenResourceManager
@Mixin(Shader.class)
public class MixinShader {

	// Redirect because optifine is tarded
	@Redirect(method = "<init>", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/util/Identifier;"))
	private static Identifier init_identifier(String string, ResourceFactory factory, String name, VertexFormat format) {
		return new Identifier(replaceIdentifier(string, name));
	}

	@ModifyArgs(method = "loadProgram", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Identifier;<init>(Ljava/lang/String;)V"))
	private static void loadProgram_identifier(Args args, ResourceFactory factory, Program.Type type, String name) {
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

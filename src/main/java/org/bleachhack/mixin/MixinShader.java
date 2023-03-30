/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderStage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

// Tweaks to the shader class to make it compatible with OpenResourceManager
@Mixin(value = ShaderProgram.class, priority = 1100)
public class MixinShader {

	@Shadow @Final private String name;

	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Identifier;<init>(Ljava/lang/String;)V"), allow = 1)
	private String modifyProgramId(String id) {
		return replaceIdentifier(id, name);
	}

	@ModifyVariable(method = "loadShader", at = @At("STORE"), ordinal = 1)
	private static String modifyStageId(String id, ResourceFactory factory, ShaderStage.Type type, String name) {
		return replaceIdentifier(id, name);
	}
	
	private static String replaceIdentifier(String id, String name) {
		String[] split = name.split(":");
		if (split.length > 1 && id.indexOf('/') < id.indexOf(':')) {
			if ("__url__".equals(split[0]))
				return name;

			return split[0] + ":" + id.replace(name, split[1]);
		}

		return id;
	}
}

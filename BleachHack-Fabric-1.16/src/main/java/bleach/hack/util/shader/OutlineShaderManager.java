/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util.shader;

import bleach.hack.mixinterface.IMixinWorldRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;

public class OutlineShaderManager {
	
	public static void loadShader(ShaderEffect shader) {
		if (getCurrentShader() != null) {
			getCurrentShader().close();
		}

		((IMixinWorldRenderer) MinecraftClient.getInstance().worldRenderer).setOutlineShader(shader);
		((IMixinWorldRenderer) MinecraftClient.getInstance().worldRenderer).setOutlineFramebuffer(shader.getSecondaryTarget("final"));
	}
	
	public static void loadDefaultShader() {
		MinecraftClient.getInstance().worldRenderer.loadEntityOutlineShader();
	}
	
	public static ShaderEffect getCurrentShader() {
		return ((IMixinWorldRenderer) MinecraftClient.getInstance().worldRenderer).getOutlineShader();
	}
}

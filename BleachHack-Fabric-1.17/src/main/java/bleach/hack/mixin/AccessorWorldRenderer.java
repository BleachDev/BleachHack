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
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.WorldRenderer;

@Mixin(WorldRenderer.class)
public interface AccessorWorldRenderer {

	@Accessor
	public abstract Framebuffer getEntityOutlinesFramebuffer();
	
	@Accessor
	public abstract void setEntityOutlinesFramebuffer(Framebuffer framebuffer);
	
	@Accessor
	public abstract ShaderEffect getEntityOutlineShader();
	
	@Accessor
	public abstract void setEntityOutlineShader(ShaderEffect shaderEffect);
}

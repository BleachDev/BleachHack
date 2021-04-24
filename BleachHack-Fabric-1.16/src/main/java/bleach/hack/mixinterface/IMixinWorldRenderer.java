/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.mixinterface;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;

public interface IMixinWorldRenderer {

	public Framebuffer getOutlineFramebuffer();
	public void setOutlineFramebuffer(Framebuffer framebuffer);

	public ShaderEffect getOutlineShader();
	public void setOutlineShader(ShaderEffect shader);
}

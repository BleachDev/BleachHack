/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2019 Bleach.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.module.mods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonSyntaxException;

import bleach.hack.event.events.EventRenderShader;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.util.Identifier;

public class ShaderRender extends Module {

	private Identifier lastId = null;
	private ShaderEffect lastShader = null;
	private int lastWidth;
	private int lastHeight;

	private List<Identifier> shaders = new ArrayList<>();

	public ShaderRender() {
		super("ShaderRender", KEY_UNBOUND, Category.RENDER, "1.7 Super secret settings (tell mojang to fix their shit if you don't want a white hotbar)",
				new SettingMode("Shader", "Notch", "FXAA", "Art", "Bumpy", "Blobs", "Blobs2", "Pencil", "Vibrant",
						"Deconverge", "Flip", "Invert", "NTSC", "Outline", "Phosphor", "Scanline", "Sobel",
						"Bits", "Desaturate", "Green", "Blur", "Wobble", "Antialias", "Creeper", "Spider").withDesc("Shader mode"));
		
		for (String s: getSetting(0).asMode().modes) {
			if (s.equals("Vibrant")) {
				shaders.add(new Identifier("shaders/post/color_convolve.json"));
			} else if (s.equals("Scanline")) {
				shaders.add(new Identifier("shaders/post/scan_pincushion.json"));
			} else {
				shaders.add(new Identifier("shaders/post/" + s.toLowerCase(Locale.ENGLISH) + ".json"));
			}
		}
	}

	@Subscribe
	public void onWorldRender(EventRenderShader event) {
		if (lastShader == null || lastWidth != mc.getWindow().getFramebufferWidth() || lastHeight != mc.getWindow().getFramebufferHeight()
				|| !shaders.get(getSetting(0).asMode().mode).equals(lastId)) {
			lastId = shaders.get(getSetting(0).asMode().mode);
			lastWidth = mc.getWindow().getFramebufferWidth();
			lastHeight = mc.getWindow().getFramebufferHeight();

			try {
				if (lastShader != null) {
					lastShader.close();
				}

				lastShader = new ShaderEffect(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), lastId);
				lastShader.setupDimensions(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
			} catch (JsonSyntaxException | IOException e) {
				e.printStackTrace();
			}
		}

		event.setEffect(lastShader);
	}

}

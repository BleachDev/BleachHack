/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
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

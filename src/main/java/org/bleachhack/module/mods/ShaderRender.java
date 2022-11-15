/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bleachhack.event.events.EventRenderShader;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;

import com.google.gson.JsonSyntaxException;

import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.util.Identifier;

public class ShaderRender extends Module {

	private Identifier lastId = null;
	private ShaderEffect lastShader = null;
	private int lastWidth;
	private int lastHeight;

	private List<Identifier> shaders = new ArrayList<>();

	public ShaderRender() {
		super("ShaderRender", KEY_UNBOUND, ModuleCategory.RENDER, "1.7 Super secret settings.",
				new SettingMode("Shader", "Notch", "FXAA", "Art", "Bumpy", "Blobs", "Blobs2", "Pencil", "Vibrant",
						"Deconverge", "Flip", "Invert", "NTSC", "Outline", "Phosphor", "Scanline", "Sobel",
						"Bits", "Desaturate", "Green", "Blur", "Wobble", "Antialias", "Creeper", "Spider").withDesc("Shader to use."));
		
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

	@BleachSubscribe
	public void onWorldRender(EventRenderShader event) {
		if (lastShader == null || lastWidth != mc.getWindow().getFramebufferWidth() || lastHeight != mc.getWindow().getFramebufferHeight()
				|| !shaders.get(getSetting(0).asMode().getMode()).equals(lastId)) {
			lastId = shaders.get(getSetting(0).asMode().getMode());
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

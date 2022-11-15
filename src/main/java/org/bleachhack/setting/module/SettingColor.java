/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.setting.module;

import org.bleachhack.gui.clickgui.window.ModuleWindow;
import org.bleachhack.gui.window.Window;
import org.bleachhack.setting.SettingDataHandlers;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;

public class SettingColor extends ModuleSetting<float[]> {

	public SettingColor(String text, int r, int g, int b) {
		super(text, rgbToHsv(r, g, b), float[]::clone, SettingDataHandlers.FLOAT_ARRAY);
	}

	public void render(ModuleWindow window, MatrixStack matrices, int x, int y, int len) {
		int sx = x + 3;
		int sy = y + 2;
		int ex = x + len - 18;
		int ey = y + getHeight(len) - 2;

		float[] hsv = getValue();
		int[] rgb = hsvToRgb(hsv[0], 1f, 1f);

		Window.fill(matrices, sx - 1, sy - 1, ex + 1, ey + 1, 0xff8070b0, 0xff6060b0, 0x00000000);

		DrawableHelper.fill(matrices, sx, sy, ex, ey, -1);

		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		// Color square
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(ex, sy, 0).color(rgb[0], rgb[1], rgb[2], 255).next();
		bufferBuilder.vertex(sx, sy, 0).color(255, 255, 255, 255).next();
		bufferBuilder.vertex(sx, ey, 0).color(255, 255, 255, 255).next();
		bufferBuilder.vertex(ex, ey, 0).color(rgb[0], rgb[1], rgb[2], 255).next();

		bufferBuilder.vertex(ex, sy, 0).color(0, 0, 0, 0).next();
		bufferBuilder.vertex(sx, sy, 0).color(0, 0, 0, 0).next();
		bufferBuilder.vertex(sx, ey, 0).color(0, 0, 0, 255).next();
		bufferBuilder.vertex(ex, ey, 0).color(0, 0, 0, 255).next();
		tessellator.draw();

		RenderSystem.disableBlend();
		RenderSystem.enableTexture();

		// Color square input handler
		if (window.mouseOver(sx, sy, ex, ey) && window.lmHeld) {
			hsv[2] = 1f - (float) (window.mouseY - sy) / (ey - sy);
			hsv[1] = (float) (window.mouseX - sx) / (ex - sx);
			setValue(hsv);
		}

		// Color square cursor
		int cursorX = (int) (sx + (ex - sx) * hsv[1]);
		int cursorY = (int) (ey - (ey - sy) * hsv[2]);

		DrawableHelper.fill(matrices, cursorX - 2, cursorY, cursorX, cursorY + 1, 0xffd0d0d0);
		DrawableHelper.fill(matrices, cursorX + 1, cursorY, cursorX + 3, cursorY + 1, 0xffd0d0d0);
		DrawableHelper.fill(matrices, cursorX, cursorY - 2, cursorX + 1, cursorY, 0xffd0d0d0);
		DrawableHelper.fill(matrices, cursorX, cursorY + 1, cursorX + 1, cursorY + 3, 0xffd0d0d0);

		matrices.push();
		matrices.scale(0.75f, 0.75f, 1f);
		MinecraftClient.getInstance().textRenderer.draw(matrices, getName(), (int) ((sx + 1) / 0.75), (int) ((sy + 1) / 0.75), 0x000000);
		matrices.pop();

		// Hue bar
		sx = ex + 5;
		ex = ex + 12;
		Window.fill(matrices, sx - 1, sy - 1, ex + 1, ey + 1, 0xff8070b0, 0xff6060b0, 0x00000000);

		for (int i = sy; i < ey; i++) {
			float curHue = (float) (i - sy) / (ey - sy);
			DrawableHelper.fill(matrices, sx, i, ex, i + 1, 0xff000000 | pack(hsvToRgb(curHue, 1f, 1f)));
		}

		// Hue bar input handler
		if (window.mouseOver(sx, sy, ex, ey) && window.lmHeld) {
			hsv[0] = (float) (window.mouseY - sy) / (ey - sy);
			setValue(hsv);
		}

		// Hue bar cursor
		cursorY = (int) (sy + (ey - sy) * hsv[0]);
		DrawableHelper.fill(matrices, sx, cursorY - 1, sx + 1, cursorY + 2, 0xffd0d0d0);
		DrawableHelper.fill(matrices, ex - 1, cursorY - 1, ex, cursorY + 2, 0xffd0d0d0);
		DrawableHelper.fill(matrices, sx, cursorY, sx + 2, cursorY + 1, 0xffd0d0d0);
		DrawableHelper.fill(matrices, ex - 2, cursorY, ex, cursorY + 1, 0xffd0d0d0);
	}

	public SettingColor withDesc(String desc) {
		setTooltip(desc);
		return this;
	}

	public int getHeight(int len) {
		return len - len / 4 - 1;
	}
	
	public int[] getRGBArray() {
		float[] hsv = getValue();
		return hsvToRgb(hsv[0], hsv[1], hsv[2]);
	}

	public int getRGB() {
		return pack(getRGBArray());
	}

	private static int pack(int... vals) {
		return (vals[0] << 16) | (vals[1] << 8) | vals[2];
	}

	private static float[] rgbToHsv(int r, int g, int b) {
		float fr = r / 255f, fg = g / 255f, fb = b / 255f; 
		float minRGB = Math.min(fr, Math.min(fg, fb));
		float maxRGB = Math.max(fr, Math.max(fg, fb));

		// Black-gray-white
		if (minRGB == maxRGB) {
			return new float[] { 0f, 0f, maxRGB };
		}

		// Colors other than black-gray-white:
		float d = (fr == minRGB) ? fg - fb : (fb == minRGB) ? fr - fg : fb - fr;
		float h = (fr == minRGB) ? 3f : (fb == minRGB) ? 1f : 5f;
		float computedH = 60 * (h - d / (maxRGB - minRGB)) / 360f;
		float computedS = (maxRGB - minRGB) / maxRGB;
		float computedV = maxRGB;

		return new float[] { computedH, computedS, computedV };
	}


	private static int[] hsvToRgb(float h, float s, float v) {
		int o = (int) (v * 255f);

		// Black-gray-white
		if (s == 0) {
			return new int[] { o, o, o };
		}

		int region = (int) (h * 6f) % 6;
		float remainder = h * 6f - (float) region; 

		int p = (int) (v * (1f - s) * 255f);
		int q = (int) (v * (1f - remainder * s) * 255f);
		int t = (int) (v * (1f - (1f - remainder) * s) * 255f);

		return switch (region) {
			case 0 -> new int[] { o, t, p };
			case 1 -> new int[] { q, o, p };
			case 2 -> new int[] { p, o, t };
			case 3 -> new int[] { p, q, o };
			case 4 -> new int[] { t, p, o };
			default -> new int[] { o, p, q };
		};
	}
}

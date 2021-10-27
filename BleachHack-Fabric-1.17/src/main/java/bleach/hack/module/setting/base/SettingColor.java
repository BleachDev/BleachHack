/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.setting.base;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.systems.RenderSystem;

import bleach.hack.gui.clickgui.window.ModuleWindow;
import bleach.hack.gui.window.Window;
import bleach.hack.util.io.BleachFileHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class SettingColor extends SettingBase {

	public String text;
	public float hue;
	public float sat;
	public float bri;

	protected float defaultHue;
	protected float defaultSat;
	protected float defaultBri;

	public SettingColor(String text, float r, float g, float b, boolean hsv) {
		this.text = text;
		if (hsv) {
			this.hue = r;
			this.sat = g;
			this.bri = b;
		} else {
			float[] vals = rgbToHsv(r, g, b);
			this.hue = vals[0];
			this.sat = vals[1];
			this.bri = vals[2];
		}

		defaultHue = hue;
		defaultSat = sat;
		defaultBri = bri;
	}

	public String getName() {
		return text;
	}

	public void render(ModuleWindow window, MatrixStack matrices, int x, int y, int len) {
		int sx = x + 3,
				sy = y + 2,
				ex = x + len - 18,
				ey = y + getHeight(len) - 2;

		Window.fill(matrices, sx - 1, sy - 1, ex + 1, ey + 1, 0xff8070b0, 0xff6060b0, 0x00000000);

		DrawableHelper.fill(matrices, sx, sy, ex, ey, -1);
		int satColor = MathHelper.hsvToRgb(hue, 1f, 1f);
		int red = satColor >> 16 & 255;
		int green = satColor >> 8 & 255;
		int blue = satColor & 255;

		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(ex, sy, 0).color(red, green, blue, 255).next();
		bufferBuilder.vertex(sx, sy, 0).color(red, green, blue, 0).next();
		bufferBuilder.vertex(sx, ey, 0).color(red, green, blue, 0).next();
		bufferBuilder.vertex(ex, ey, 0).color(red, green, blue, 255).next();
		tessellator.draw();

		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(ex, sy, 0).color(0, 0, 0, 0).next();
		bufferBuilder.vertex(sx, sy, 0).color(0, 0, 0, 0).next();
		bufferBuilder.vertex(sx, ey, 0).color(0, 0, 0, 255).next();
		bufferBuilder.vertex(ex, ey, 0).color(0, 0, 0, 255).next();
		tessellator.draw();

		RenderSystem.disableBlend();
		RenderSystem.enableTexture();

		if (window.mouseOver(sx, sy, ex, ey) && window.lmHeld) {
			bri = 1f - 1f / ((float) (ey - sy) / (window.mouseY - sy));
			sat = 1f / ((float) (ex - sx) / (window.mouseX - sx));
		}

		int briY = (int) (ey - (ey - sy) * bri);
		int satX = (int) (sx + (ex - sx) * sat);

		DrawableHelper.fill(matrices, satX - 2, briY, satX, briY + 1, 0xffd0d0d0);
		DrawableHelper.fill(matrices, satX + 1, briY, satX + 3, briY + 1, 0xffd0d0d0);
		DrawableHelper.fill(matrices, satX, briY - 2, satX + 1, briY, 0xffd0d0d0);
		DrawableHelper.fill(matrices, satX, briY + 1, satX + 1, briY + 3, 0xffd0d0d0);

		matrices.push();
		matrices.scale(0.75f, 0.75f, 1f);
		MinecraftClient.getInstance().textRenderer.draw(matrices, text, (int) ((sx + 1) * 1 / 0.75), (int) ((sy + 1) * 1 / 0.75), 0x000000);
		matrices.pop();

		sx = ex + 5;
		ex = ex + 12;
		Window.fill(matrices, sx - 1, sy - 1, ex + 1, ey + 1, 0xff8070b0, 0xff6060b0, 0x00000000);

		for (int i = sy; i < ey; i++) {
			float curHue = 1f / ((float) (ey - sy) / (i - sy));
			DrawableHelper.fill(matrices, sx, i, ex, i + 1, 0xff000000 | MathHelper.hsvToRgb(curHue, 1f, 1f));
		}

		if (window.mouseOver(sx, sy, ex, ey) && window.lmHeld) {
			BleachFileHelper.SCHEDULE_SAVE_MODULES.set(true);
			hue = 1f / ((float) (ey - sy) / (window.mouseY - sy));
		}

		int hueY = (int) (sy + (ey - sy) * hue);
		DrawableHelper.fill(matrices, sx, hueY - 1, sx + 1, hueY + 2, 0xffa0a0a0);
		DrawableHelper.fill(matrices, ex - 1, hueY - 1, ex, hueY + 2, 0xffa0a0a0);
		DrawableHelper.fill(matrices, sx, hueY, sx + 2, hueY + 1, 0xffa0a0a0);
		DrawableHelper.fill(matrices, ex - 2, hueY, ex, hueY + 1, 0xffa0a0a0);
	}

	public SettingColor withDesc(String desc) {
		description = desc;
		return this;
	}

	public int getHeight(int len) {
		return len - len / 4 - 1;
	}

	public void readSettings(JsonElement settings) {
		if (settings.isJsonObject()) {
			JsonObject jo = settings.getAsJsonObject();
			hue = jo.get("hue").getAsFloat();
			sat = jo.get("sat").getAsFloat();
			bri = jo.get("bri").getAsFloat();
		}
	}

	public JsonElement saveSettings() {
		JsonObject jo = new JsonObject();
		jo.add("hue", new JsonPrimitive(hue));
		jo.add("sat", new JsonPrimitive(sat));
		jo.add("bri", new JsonPrimitive(bri));

		return jo;
	}

	public int getRGB() {
		return MathHelper.hsvToRgb(hue, sat, bri);
	}

	public float[] getRGBFloat() {
		int col = MathHelper.hsvToRgb(hue, sat, bri);
		return new float[] { (col >> 16 & 255) / 255f, (col >> 8 & 255) / 255f, (col & 255) / 255f };
	}

	@Override
	public boolean isDefault() {
		return hue == defaultHue && sat == defaultSat && bri == defaultBri;
	}

	private float[] rgbToHsv(float r, float g, float b) {
		float minRGB = Math.min(r, Math.min(g, b));
		float maxRGB = Math.max(r, Math.max(g, b));

		// Black-gray-white
		if (minRGB == maxRGB) {
			return new float[] { 0f, 0f, minRGB };
		}

		// Colors other than black-gray-white:
		float d = (r == minRGB) ? g - b : (b == minRGB) ? r - g : b - r;
		float h = (r == minRGB) ? 3 : (b == minRGB) ? 1 : 5;
		float computedH = 60 * (h - d / (maxRGB - minRGB)) / 360f;
		float computedS = (maxRGB - minRGB) / maxRGB;
		float computedV = maxRGB;

		return new float[] { computedH, computedS, computedV };
	}
}

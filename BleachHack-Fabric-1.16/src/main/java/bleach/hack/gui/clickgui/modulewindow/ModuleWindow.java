/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
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
package bleach.hack.gui.clickgui.modulewindow;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingColor;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class ModuleWindow extends ClickGuiWindow {

	public List<Module> modList = new ArrayList<>();
	public LinkedHashMap<Module, Boolean> mods = new LinkedHashMap<>();

	public boolean hiding;

	public int len;

	private Set<Module> searchedModules;

	private Triple<Integer, Integer, String> tooltip = null;

	public ModuleWindow(List<Module> mods, int x1, int y1, int len, String title, ItemStack icon) {
		super(x1, y1, x1 + len, 0, title, icon);

		this.len = len;
		modList = mods;

		for (Module m : mods)
			this.mods.put(m, false);
		y2 = getHeight();
	}

	public void render(MatrixStack matrix, int mX, int mY) {
		super.render(matrix, mX, mY);

		TextRenderer textRend = mc.textRenderer;

		tooltip = null;
		int x = x1 + 1;
		int y = y1 + 13;
		x2 = x + len + 1;

		if (rmDown && mouseOver(x, y - 12, x + len, y)) {
			mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			hiding = !hiding;
		}

		if (hiding) {
			y2 = y;
			return;
		} else {
			y2 = y + getHeight();
		}

		int curY = 0;
		for (Entry<Module, Boolean> m : new LinkedHashMap<>(mods).entrySet()) {
			if (m.getValue()) {
				//fillReverseGrey(x, y + curY, x+len-1, y + 12 + curY);
				fillGreySides(matrix, x, y + curY, x+len-1, y + 12 + curY);
				DrawableHelper.fill(matrix, x, y + curY, x + len - 2, y + curY + 1, 0x90000000);
				DrawableHelper.fill(matrix, x + len - 3, y + curY + 1, x + len - 2, y + curY + 12, 0x90b0b0b0);
			}
			
			DrawableHelper.fill(matrix, x, y + curY, x+len, y + 12 + curY,
					mouseOver(x, y + curY, x+len, y + 12 + curY) ? 0x70303070 : 0x00000000);

			textRend.drawWithShadow(matrix, textRend.trimToWidth(m.getKey().getName(), len),
					x+2, y + 2 + curY, m.getKey().isToggled() ? 0x70efe0 : 0xc0c0c0);

			//If they match: Module gets marked red
			if (searchedModules != null && searchedModules.contains(m.getKey()) && ModuleManager.getModule(ClickGui.class).getSettings().get(1).asToggle().state) {
				DrawableHelper.fill(matrix, m.getValue() ? x + 1 : x, y + curY + (m.getValue() ? 1 : 0),
						m.getValue() ? x + len - 3 : x + len, y + 12 + curY, 0x50ff0000);
			}

			/* Set which module settings show on */
			if (mouseOver(x, y + curY, x+len, y + 12 + curY)) {
				tooltip = Triple.of(x + len + 2, y + curY, m.getKey().getDesc());

				if (lmDown) m.getKey().toggle();
				if (rmDown) mods.replace(m.getKey(), !m.getValue());
				if (lmDown || rmDown) mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			}
			
			curY += 12;

			/* draw settings */
			if (m.getValue()) {
				for (SettingBase s: m.getKey().getSettings()) {
					if (s instanceof SettingMode) drawModeSetting(matrix, s.asMode(), x, y + curY, textRend);
					else if (s instanceof SettingToggle) drawToggleSetting(matrix, s.asToggle(), x, y + curY, textRend);
					else if (s instanceof SettingSlider) drawSliderSetting(matrix, s.asSlider(), x, y + curY, textRend);
					else if (s instanceof SettingColor) {
						//System.out.println(s.getHeight(len));
						drawColorSetting(matrix, s.asColor(), x, y + curY, textRend);
					}

					if (!s.getDesc().isEmpty() && mouseOver(x, y + curY, x+len, y + s.getHeight(len) + curY)) {
						tooltip = Triple.of(x + len + 2, y + curY, s.getDesc());
					}
					
					curY += s.getHeight(len);
				}

				drawBindSetting(matrix, m.getKey(), keyDown, x, y + curY, textRend);
				curY += 12;
				//fill(x+len-1, y+(count*12), x+len, y+12+(count*12), 0x9f70fff0);
			}
		}
	}

	public void drawBindSetting(MatrixStack matrix, Module m, int key, int x, int y, TextRenderer textRend) {
		DrawableHelper.fill(matrix, x, y + 11, x + len - 2, y + 12, 0x90b0b0b0);
		DrawableHelper.fill(matrix, x + len - 2, y, x + len - 1, y + 12, 0x90b0b0b0);
		DrawableHelper.fill(matrix, x, y - 1, x + 1, y + 11, 0x90000000);

		if (key >= 0 && mouseOver(x, y, x + len, y + 12))
			m.setKey((key != GLFW.GLFW_KEY_DELETE && key != GLFW.GLFW_KEY_ESCAPE) ? key : Module.KEY_UNBOUND);

		String name = m.getKey() < 0 ? "NONE" : InputUtil.fromKeyCode(m.getKey(), -1).getLocalizedText().getString();
		if (name == null)
			name = "KEY" + m.getKey();
		else if (name.isEmpty())
			name = "NONE";

		textRend.drawWithShadow(matrix, "Bind: " + name + (mouseOver(x, y, x + len, y + 12) ? "..." : ""), x + 2, y + 2,
				mouseOver(x, y, x + len, y + 12) ? 0xcfc3cf : 0xcfe0cf);
	}

	public void drawModeSetting(MatrixStack matrix, SettingMode s, int x, int y, TextRenderer textRend) {
		fillGreySides(matrix, x, y - 1, x + len - 1, y + 12);
		textRend.drawWithShadow(matrix, s.text + s.modes[s.mode], x + 2, y + 2,
				mouseOver(x, y, x + len, y + 12) ? 0xcfc3cf : 0xcfe0cf);

		if (mouseOver(x, y, x + len, y + 12) && lmDown)
			s.mode = s.getNextMode();
	}

	public void drawToggleSetting(MatrixStack matrix, SettingToggle s, int x, int y, TextRenderer textRend) {
		String color2;

		if (s.state) {
			if (mouseOver(x, y, x + len, y + 12))
				color2 = "\u00a72";
			else
				color2 = "\u00a7a";
		} else {
			if (mouseOver(x, y, x + len, y + 12))
				color2 = "\u00a74";
			else
				color2 = "\u00a7c";
		}

		fillGreySides(matrix, x, y - 1, x + len - 1, y + 12);
		textRend.drawWithShadow(matrix, color2 + s.text, x + 3, y + 2, -1);

		if (mouseOver(x, y, x + len, y + 12) && lmDown)
			s.state = !s.state;
	}

	public void drawSliderSetting(MatrixStack matrix, SettingSlider s, int x, int y, TextRenderer textRend) {
		int pixels = (int) Math
				.round(MathHelper.clamp((len - 2) * ((s.getValue() - s.min) / (s.max - s.min)), 0, len - 2));
		fillGreySides(matrix, x, y - 1, x + len - 1, y + 12);
		fillGradient(matrix, x + 1, y, x + pixels, y + 12, 0xf03080a0, 0xf02070b0);

		textRend.drawWithShadow(matrix,
				s.text + (s.round == 0 && s.getValue() > 100 ? Integer.toString((int) s.getValue()) : s.getValue()),
				x + 2, y + 2, mouseOver(x, y, x + len, y + 12) ? 0xcfc3cf : 0xcfe0cf);

		if (mouseOver(x + 1, y, x + len - 2, y + 12) && lmHeld) {
			int percent = ((mouseX - x) * 100) / (len - 2);

			s.setValue(s.round(percent * ((s.max - s.min) / 100) + s.min, s.round));
		}
	}
	
	public void drawColorSetting(MatrixStack matrix, SettingColor s, int x, int y, TextRenderer textRend) {
		fillGreySides(matrix, x, y - 1, x + len - 1, y + s.getHeight(len));

		int sx = x + 3,
			sy = y + 2,
			ex = x + len - 18,
			ey = y + s.getHeight(len) - 2;

		fillReverseGrey(matrix, sx - 1, sy - 1, ex + 1, ey + 1);

		DrawableHelper.fill(matrix, sx, sy, ex, ey, -1);
		Color satColor = Color.getHSBColor(1f - s.hue, 1f, 1f);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glShadeModel(7425);
		Tessellator tessellator_1 = Tessellator.getInstance();
		BufferBuilder bufferBuilder_1 = Tessellator.getInstance().getBuffer();
		bufferBuilder_1.begin(7, VertexFormats.POSITION_COLOR);
		bufferBuilder_1.vertex(ex, sy, 0).color(satColor.getRed(), satColor.getBlue(), satColor.getGreen(), 255).next();
		bufferBuilder_1.vertex(sx, sy, 0).color(satColor.getRed(), satColor.getBlue(), satColor.getGreen(), 0).next();
		bufferBuilder_1.vertex(sx, ey, 0).color(satColor.getRed(), satColor.getBlue(), satColor.getGreen(), 0).next();
		bufferBuilder_1.vertex(ex, ey, 0).color(satColor.getRed(), satColor.getBlue(), satColor.getGreen(), 255).next();
		tessellator_1.draw();

		bufferBuilder_1.begin(7, VertexFormats.POSITION_COLOR);
		bufferBuilder_1.vertex(ex, sy, 0).color(0, 0, 0, 0).next();
		bufferBuilder_1.vertex(sx, sy, 0).color(0, 0, 0, 0).next();
		bufferBuilder_1.vertex(sx, ey, 0).color(0, 0, 0, 255).next();
		bufferBuilder_1.vertex(ex, ey, 0).color(0, 0, 0, 255).next();
		tessellator_1.draw();
		GL11.glShadeModel(7424);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		if (mouseOver(sx, sy, ex, ey) && lmHeld) {
			s.bri = 1f - 1f / ((float) (ey - sy) / (mouseY - sy));
			s.sat = 1f / ((float) (ex - sx) / (mouseX - sx));
		}

		int briY = (int) (ey - (ey - sy) * s.bri);
		int satX = (int) (sx + (ex - sx) * s.sat);

		DrawableHelper.fill(matrix, satX - 2, briY, satX, briY + 1, 0xffd0d0d0);
		DrawableHelper.fill(matrix, satX + 1, briY, satX + 3, briY + 1, 0xffd0d0d0);
		DrawableHelper.fill(matrix, satX, briY - 2, satX + 1, briY, 0xffd0d0d0);
		DrawableHelper.fill(matrix, satX, briY + 1, satX + 1, briY + 3, 0xffd0d0d0);

		GL11.glPushMatrix();
		GL11.glScaled(0.75, 0.75, 1);
		textRend.draw(matrix, s.text, (int) ((sx + 1) * 1/0.75), (int) ((sy + 1) * 1/0.75), 0x000000);
		GL11.glPopMatrix();

		sx = ex + 5;
		ex = ex + 12;
		fillReverseGrey(matrix, sx - 1, sy - 1, ex + 1, ey + 1);

		for (int i = sy; i < ey; i++) {
			float curHue = 1f / ((float) (ey - sy) / (i - sy));
			DrawableHelper.fill(matrix, sx, i, ex, i + 1, Color.getHSBColor(curHue, 1f, 1f).getRGB());
		}

		if (mouseOver(sx, sy, ex, ey) && lmHeld) {
			s.hue = 1f / ((float) (ey - sy) / (mouseY - sy));
		}

		int hueY = (int) (sy + (ey - sy) * s.hue);
		DrawableHelper.fill(matrix, sx, hueY - 1, sx + 1, hueY + 2, 0xffa0a0a0);
		DrawableHelper.fill(matrix, ex - 1, hueY - 1, ex, hueY + 2, 0xffa0a0a0);
		DrawableHelper.fill(matrix, sx, hueY, sx + 2, hueY + 1, 0xffa0a0a0);
		DrawableHelper.fill(matrix, ex - 2, hueY, ex, hueY + 1, 0xffa0a0a0);
	}

	protected void fillReverseGrey(MatrixStack matrix, int x1, int y1, int x2, int y2) {
		DrawableHelper.fill(matrix, x1, y1, x1 + 1, y2 - 1, 0x90000000);
		DrawableHelper.fill(matrix, x1 + 1, y1, x2 - 1, y1 + 1, 0x90000000);
		DrawableHelper.fill(matrix, x1 + 1, y2 - 1, x2, y2, 0x90b0b0b0);
		DrawableHelper.fill(matrix, x2 - 1, y1 + 1, x2, y2 - 1, 0x90b0b0b0);
		DrawableHelper.fill(matrix, x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xff505059);
	}

	protected void fillGreySides(MatrixStack matrix, int x1, int y1, int x2, int y2) {
		DrawableHelper.fill(matrix, x1, y1, x1 + 1, y2 - 1, 0x90000000);
		DrawableHelper.fill(matrix, x2 - 1, y1 + 1, x2, y2, 0x90b0b0b0);
	}

	protected void drawBar(MatrixStack matrix, int mX, int mY, TextRenderer textRend) {
		super.drawBar(matrix, mX, mY, textRend);
		textRend.draw(matrix, hiding ? "+" : "_", x2 - 11, y1 + (hiding ? 3 : 1), 0xffffff);
	}

	public Triple<Integer, Integer, String> getTooltip() {
		return tooltip;
	}

	public void setSearchedModule(Set<Module> mods) {
		searchedModules = mods;
	}

	public int getHeight() {
		int h = 1;
		for (Entry<Module, Boolean> e: mods.entrySet()) {
			h += 12;

			if (e.getValue()) {
				for (SettingBase s: e.getKey().getSettings()) {
					h += s.getHeight(len);
				}
				
				h += 12;
			}
		}

		return h;
	}
}

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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.ClickGuiScreen;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Module;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;

public class ModuleWindow extends ClickGuiWindow {
	
	public List<Module> modList = new ArrayList<>();
	public LinkedHashMap<Module, Boolean> mods = new LinkedHashMap<>();
	
	public boolean hiding;
	
	public int len;
	
	private static Module module;
	
	private Triple<Integer, Integer, String> tooltip = null;
	
	public ModuleWindow(List<Module> mods, int x1, int y1, int len, String title, ItemStack icon) {
		super(x1, y1, x1 + len, 0, title, icon);
		
		this.len = len;
		modList = mods;
		
		for (Module m: mods) this.mods.put(m, false);
		y2 = getHeight();
	}
	
	public void render(MatrixStack matrix, int mX, int mY) {
		super.render(matrix, mX, mY);
		
		TextRenderer textRend = mc.textRenderer;
		
		tooltip = null;
		int x = x1 + 1;
		int y = y1 + 13;
		x2 = x + len + 1;

		if (rmDown && mouseOver(x, y-12, x+len, y)) {
			mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			hiding = !hiding;
		}

		if (hiding) {
			y2 = y;
			return;
		} else {
			y2 = y + getHeight();
		}

		int count = 0;
		for (Entry<Module, Boolean> m: new LinkedHashMap<>(mods).entrySet()) {
			if (m.getValue()) fillReverseGrey(matrix, x, y+(count*12), x+len-1, y+12+(count*12));
			
			if(m.getKey() == module)
				Screen.fill(matrix, x, y+(count*12), x+len, y+12+(count*12),
						mouseOver(x, y+(count*12), x+len, y+12+(count*12)) ? 0xFFCC0000 : 0xFFFF0000);
			
			else
				Screen.fill(matrix, x, y+(count*12), x+len, y+12+(count*12),
						mouseOver(x, y+(count*12), x+len, y+12+(count*12)) ? 0x70303070 : 0x00000000);
			
			textRend.drawWithShadow(matrix, textRend.trimToWidth(m.getKey().getName(), len),
					x+2, y+2+(count*12), m.getKey().isToggled() ? 0x70efe0 : 0xc0c0c0);


			/* Set which module settings show on */
			if (mouseOver(x, y+(count*12), x+len, y+12+(count*12))) {
				tooltip = Triple.of(x + len + 2, y + count * 12, m.getKey().getDesc());
				
				if (lmDown) m.getKey().toggle();
				if (rmDown) mods.replace(m.getKey(), !m.getValue());
				if (lmDown || rmDown) mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			}

			/* draw settings */
			if (m.getValue()) {
				for (SettingBase s: m.getKey().getSettings()) {
					count++;
					if (s instanceof SettingMode) drawModeSetting(matrix, s.toMode(), x, y+(count*12), textRend);
					if (s instanceof SettingToggle) drawToggleSetting(matrix, s.toToggle(), x, y+(count*12), textRend);
					if (s instanceof SettingSlider) drawSliderSetting(matrix, s.toSlider(), x, y+(count*12), textRend);
					//fill(x+len-1, y+(count*12), x+len, y+12+(count*12), 0x9f70fff0);
				}
				count++;
				drawBindSetting(matrix, m.getKey(), keyDown, x, y+(count*12), textRend);
				//fill(x+len-1, y+(count*12), x+len, y+12+(count*12), 0x9f70fff0);
			}
			count++;
		}
	}
	
	public static void setModule(Module mod){
		module = mod;
	}
	
	public void drawBindSetting(MatrixStack matrix, Module m, int key, int x, int y, TextRenderer textRend) {
		Screen.fill(matrix, x, y+11, x+len-2, y+12, 0x90b0b0b0);
		Screen.fill(matrix, x+len - 2, y, x+len-1, y+12, 0x90b0b0b0);
		Screen.fill(matrix, x, y - 1, x + 1, y+11, 0x90000000);

		if (key >= 0 && mouseOver(x, y, x+len, y+12)) m.setKey((key != GLFW.GLFW_KEY_DELETE && key != GLFW.GLFW_KEY_ESCAPE) ? key : Module.KEY_UNBOUND);

		String name = m.getKey() < 0 ? "NONE" : InputUtil.fromKeyCode(m.getKey(), -1).getLocalizedText().getString();
		if (name == null) name = "KEY" + m.getKey();
		else if (name.isEmpty()) name = "NONE";

		textRend.drawWithShadow(matrix, "Bind: " + name + (mouseOver(x, y, x+len, y+12) ? "..." : "")
				, x+2, y+2, mouseOver(x, y, x+len, y+12) ? 0xcfc3cf : 0xcfe0cf);
	}

	public void drawModeSetting(MatrixStack matrix, SettingMode s, int x, int y, TextRenderer textRend) {
		fillGreySides(matrix, x, y-1, x+len-1, y+12);
		textRend.drawWithShadow(matrix, s.text + s.modes[s.mode],x+2, y+2,
				mouseOver(x, y, x+len, y+12) ? 0xcfc3cf : 0xcfe0cf);

		if (mouseOver(x, y, x+len, y+12) && lmDown) s.mode = s.getNextMode();
	}

	public void drawToggleSetting(MatrixStack matrix, SettingToggle s, int x, int y, TextRenderer textRend) {
		String color2;

		if (s.state) { if (mouseOver(x, y, x+len, y+12)) color2 = "\u00a72"; else color2 = "\u00a7a";
		} else { if (mouseOver(x, y, x+len, y+12)) color2 = "\u00a74"; else color2 = "\u00a7c"; }

		fillGreySides(matrix, x, y-1, x+len-1, y+12);
		textRend.drawWithShadow(matrix, color2 + s.text, x+3, y+2, -1);

		if (mouseOver(x, y, x+len, y+12) && lmDown) s.state = !s.state;
	}

	public void drawSliderSetting(MatrixStack matrix, SettingSlider s, int x, int y, TextRenderer textRend) {
		int pixels = (int) Math.round(MathHelper.clamp((len-2)*((s.getValue() - s.min) / (s.max - s.min)), 0, len-2));
		fillGreySides(matrix, x, y-1, x+len-1, y+12);
		fillGradient(matrix, x+1, y, x+pixels, y+12, 0xf03080a0, 0xf02070b0);

		textRend.drawWithShadow(matrix, s.text + (s.round == 0  && s.getValue() > 100 ? Integer.toString((int)s.getValue()) : s.getValue()),
				x+2, y+2, mouseOver(x, y, x+len, y+12) ? 0xcfc3cf : 0xcfe0cf);

		if (mouseOver(x+1, y, x+len-2, y+12) && lmHeld) {
			int percent = ((mouseX - x) * 100) / (len - 2);

			s.setValue(s.round(percent*((s.max - s.min) / 100) + s.min, s.round));
		}
	}
	
	protected void fillReverseGrey(MatrixStack matrix, int x1, int y1, int x2, int y2) {
		Screen.fill(matrix, x1, y1, x1 + 1, y2 - 1, 0x90000000);
		Screen.fill(matrix, x1 + 1, y1, x2 - 1, y1 + 1, 0x90000000);
		Screen.fill(matrix, x2 - 2, y1 + 1, x2, y2, 0x90b0b0b0);
	}

	protected void fillGreySides(MatrixStack matrix, int x1, int y1, int x2, int y2) {
		Screen.fill(matrix, x1, y1, x1 + 1, y2 - 1, 0x90000000);
		Screen.fill(matrix, x2 - 1, y1 + 1, x2, y2, 0x90b0b0b0);
	}
	
	protected void drawBar(MatrixStack matrix, int mX, int mY, TextRenderer textRend) {
		super.drawBar(matrix, mX, mY, textRend);
		textRend.draw(matrix, hiding ? "+" : "_", x2 - 11, y1 + (hiding ? 3 : 1), 0xffffff);
	}
	
	public Triple<Integer, Integer, String> getTooltip() {
		return tooltip;
	}
	
	public int getHeight() {
		int h = 1;
		for (Entry<Module, Boolean> e: mods.entrySet()) {
			h += 12;
			
			if (e.getValue()) {
				h += (12 * (e.getKey().getSettings().size() + 1));
			}
		}
		
		return h;
	}
}

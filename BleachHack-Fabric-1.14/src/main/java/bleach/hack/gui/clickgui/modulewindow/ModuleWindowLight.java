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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.opengl.GL11;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class ModuleWindowLight extends ModuleWindow {
	
	public ModuleWindowLight(List<Module> mods, String name, int len, int posX, int posY) {
		super(mods, name, len, posX, posY);
	}
	
	public void draw(int mX, int mY, int leng) {
		mouseX = mX; mouseY = mY; len = leng;
		font = MinecraftClient.getInstance().textRenderer;
		
		Screen.fill(posX, posY-10, posX+len, posY, 
				mouseOver(posX, posY-10, posX+len, posY) ? 0x704060cf : 0x704040ff);
		font.drawWithShadow(name, posX+(len/2)-(font.getStringWidth(name)/2), posY-9, 0xffaaff);
		
		/* window dragging */
		if (mouseOver(posX, posY-10, posX+len, posY)) {
			if (rmDown) hiding = !hiding;
			if (lmDown) dragging = true;
		}
		
		if (!lmHeld) dragging = false;
		if (dragging) {
			posX = mouseX-(prevmX - posX);
			posY = mouseY-(prevmY - posY);
		}
		
		prevmX = mouseX;
		prevmY = mouseY;
		
		/* Draw Modules */
		if (hiding) {
			font.drawWithShadow("-", posX+len-8, posY-9, 0xffaaff);
			return;
		} else font.drawWithShadow("+", posX+len-8, posY-9, 0xffaaff);
			
		int count = 0;
		for (Entry<Module, Boolean> m: new LinkedHashMap<>(mods).entrySet()) {
			Screen.fill(posX, posY+(count*12), posX+len, posY+12+(count*12),
					mouseOver(posX, posY+(count*12), posX+len, posY+12+(count*12)) ? 0x70303070 : 0x70000000);
			font.drawWithShadow(cutText(m.getKey().getName(), len),
					posX+2, posY+2+(count*12), m.getKey().isToggled() ? 0x6060ff : 0xffffff);
			
			Screen.fill(m.getValue() ? posX+len-2 : posX+len-1, posY+(count*12), 
					posX+len, posY+12+(count*12), m.getValue() ? 0x9fffffff : 0x5fffffff);
			
			/* Set which module settings show on */
			if (mouseOver(posX, posY+(count*12), posX+len, posY+12+(count*12))) {
				GL11.glTranslated(0, 0, 300);
				/* Match lines to end of words */
		        Matcher mat = Pattern.compile("\\b.{1,22}\\b\\W?").matcher(m.getKey().getDesc());

		        int c2 = 0;
		        int c3 = 0;
		        while(mat.find()) { c2++; } mat.reset();
		        
		        while(mat.find()) {
		        	Screen.fill(posX+len+3, posY-1+(count*12)-(c2 * 10)+(c3 * 10),
							posX+len+6+font.getStringWidth(mat.group().trim()), posY+(count*12)-(c2 * 10)+(c3 * 10)+9,
							0x90000000);
		        	font.drawWithShadow(mat.group(), posX+len+5, posY+(count*12)-(c2 * 10)+(c3 * 10), -1);
					c3++;
				}
				if (lmDown) m.getKey().toggle();
				if (rmDown) mods.replace(m.getKey(), !m.getValue());
				if (lmDown || rmDown) MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
				GL11.glTranslated(0, 0, -300);
			}
			
			/* draw settings */
			if (m.getValue()) {
				for (SettingBase s: m.getKey().getSettings()) {
					count++;
					if (s instanceof SettingMode) drawModeSetting(s.toMode(), posX, posY+(count*12));
					if (s instanceof SettingToggle) drawToggleSetting(s.toToggle(), posX, posY+(count*12));
					if (s instanceof SettingSlider) drawSliderSetting(s.toSlider(), posX, posY+(count*12));
					Screen.fill(posX+len-1, posY+(count*12), posX+len, posY+12+(count*12), 0x9fffffff);
				}
				count++;
				drawBindSetting(m.getKey(), keyDown, posX, posY+(count*12));
				Screen.fill(posX+len-1, posY+(count*12), posX+len, posY+12+(count*12), 0x9fffffff);
			}
			count++;
		}
		
		lmDown = false;
		rmDown = false;
		keyDown = -1;
	}
	
	public void drawBindSetting(Module m, int key, int x, int y) {
		Screen.fill(x, y, x+len, y+12, 0x70000000);
		
		if (key != -1 && mouseOver(x, y, x+len, y+12)) m.setKey((key != 261 && key != 256) ? key : -1);
		String name = InputUtil.getKeycodeName(m.getKey());
		if (name == null) name = "KEY" + m.getKey();
		if (name.isEmpty()) name = "NONE";
		
		font.drawWithShadow("Bind: " + name + (mouseOver(x, y, x+len, y+12) ? "..." : "")
				, x+2, y+2, mouseOver(x, y, x+len, y+12) ? 0xffc3ff : 0xffe0ff);
	}
	
	public void drawModeSetting(SettingMode s, int x, int y) {
		Screen.fill(x, y, x+len, y+12, 0x70000000);
		font.drawWithShadow(s.text + s.modes[s.mode],x+2, y+2,
				mouseOver(x, y, x+len, y+12) ? 0xffc3ff : 0xffe0ff);
		
		if (mouseOver(x, y, x+len, y+12) && lmDown) s.mode = s.getNextMode();
	}
	
	public void drawToggleSetting(SettingToggle s, int x, int y) {
		int color;
		String color2;
		
		if (s.state) { if (mouseOver(x, y, x+len, y+12)) {color = 0x9020ff20; color2 = "§2";} else {color = 0x7020ff20; color2 = "§a";}
		} else { if (mouseOver(x, y, x+len, y+12)) {color = 0x90ff2020; color2 = "§4";} else {color = 0x70ff2020; color2 = "§c";} }
		
		Screen.fill(x, y, x+len, y+12, 0x70000000);
		Screen.fill(x, y, x+1, y+12, color);
		font.drawWithShadow(color2 + s.text, x+3, y+2, -1);
		
		if (mouseOver(x, y, x+len, y+12) && lmDown) s.state = !s.state;
	}
	
	public void drawSliderSetting(SettingSlider s, int x, int y) {
		int pixels = (int) Math.round(MathHelper.clamp(len*((s.getValue() - s.min) / (s.max - s.min)), 0, len));
		Screen.fill(x, y, x+len, y+12, 0x70000000);
		Screen.fill(x, y, x+pixels, y+12, 0xf03080a0);
		
		font.drawWithShadow(s.text + (s.round == 0  && s.getValue() > 100 ? Integer.toString((int)s.getValue()) : s.getValue()),
				x+2, y+2, mouseOver(x, y, x+len, y+12) ? 0xffc3ff : 0xffe0ff);
		
		if (mouseOver(x, y, x+len, y+12) && lmHeld) {
			int percent = ((mouseX - x) * 100) / len;
				
			s.setValue(s.round(percent*((s.max - s.min) / 100) + s.min, s.round));
		}
	}
}

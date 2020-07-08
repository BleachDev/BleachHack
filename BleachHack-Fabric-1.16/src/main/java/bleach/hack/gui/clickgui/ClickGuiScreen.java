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
package bleach.hack.gui.clickgui;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import bleach.hack.BleachHack;
import bleach.hack.command.Command;
import bleach.hack.gui.clickgui.modulewindow.ModuleWindow;
import bleach.hack.gui.window.AbstractWindowScreen;
import bleach.hack.gui.window.Window;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;

public class ClickGuiScreen extends AbstractWindowScreen {

	private int len;

	private int mouseX;
	private int mouseY;

	private int keyDown;
	private boolean lmDown;
	private boolean rmDown;
	private boolean lmHeld;

	public ClickGuiScreen() {
		super(new LiteralText("ClickGui"));
	}

	public void initWindows() {
		len = 70;

		int i = 30;
		for (Category c: Category.values()) {
			windows.add(new ModuleWindow(ModuleManager.getModulesInCat(c), i, 35, len,
					StringUtils.capitalize(StringUtils.lowerCase(c.toString())), new ItemStack(Items.AIR)));

			i += len + 5;
		}
	}

	public boolean isPauseScreen() {
		return false;
	}

	public void onClose() {
		ModuleManager.getModule(ClickGui.class).setToggled(false);
		this.client.openScreen(null);
	}

	public void render(MatrixStack matrix, int mX, int mY, float float_1) {
		this.renderBackground(matrix);
		textRenderer.draw(matrix, "BleachHack-1.16-" + BleachHack.VERSION, 3, 3, 0x305090);
		textRenderer.draw(matrix, "BleachHack-1.16-" + BleachHack.VERSION, 2, 2, 0x6090d0);
		textRenderer.drawWithShadow(matrix, "Current prefix is: \"" + Command.PREFIX + "\" (" + Command.PREFIX + "help)", 2, height-20, 0x99ff99);
		textRenderer.drawWithShadow(matrix, "Use .guireset to reset the gui" , 2, height-10, 0x9999ff);

		super.render(matrix, mX, mY, float_1);

		mouseX = mX;
		mouseY = mY;

		len = Math.max(70, (int) Math.round(ModuleManager.getModule(ClickGui.class).getSettings().get(0).toSlider().getValue()));
		for (Window w: windows) {
			if (w instanceof ModuleWindow) {
				ModuleWindow mw = (ModuleWindow) w;

				int x = mw.x1 + 1;
				int y = mw.y1 + 13;
				mw.x2 = x + len + 1;

				if (rmDown && mouseOver(x, y-12, x+len, y)) {
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
					mw.hiding = !mw.hiding;
				}

				if (mw.hiding) {
					mw.y2 = y;
					continue;
				} else {
					mw.y2 = y + mw.getHeight();
				}

				int count = 0;
				for (Entry<Module, Boolean> m: new LinkedHashMap<>(mw.mods).entrySet()) {
					if (m.getValue()) fillReverseGrey(matrix, x, y+(count*12), x+len-1, y+12+(count*12));
					fill(matrix, x, y+(count*12), x+len, y+12+(count*12),
							mouseOver(x, y+(count*12), x+len, y+12+(count*12)) ? 0x70303070 : 0x00000000);

					textRenderer.drawWithShadow(matrix, cutText(m.getKey().getName(), len),
							x+2, y+2+(count*12), m.getKey().isToggled() ? 0x70efe0 : 0xc0c0c0);

					//fill(m.getValue() ? x+len-2 : x+len-1, y+(count*12), 
					//		x+len, y+12+(count*12), m.getValue() ? 0x9f70fff0 : 0x5f70fff0);

					/* Set which module settings show on */
					if (mouseOver(x, y+(count*12), x+len, y+12+(count*12))) {
						GL11.glTranslated(0, 0, 300);
						/* Match lines to end of words */
						Matcher mat = Pattern.compile("\\b.{1,22}\\b\\W?").matcher(m.getKey().getDesc());

						int c2 = 0;
						int c3 = 0;
						while(mat.find()) { c2++; } mat.reset();

						while(mat.find()) {
							fill(matrix, x+len+3, y-1+(count*12)-(c2 * 10)+(c3 * 10),
									x+len+6+textRenderer.getWidth(mat.group().trim()), y+(count*12)-(c2 * 10)+(c3 * 10)+9,
									0x90000000);
							textRenderer.drawWithShadow(matrix, mat.group(), x+len+5, y+(count*12)-(c2 * 10)+(c3 * 10), -1);
							c3++;
						}
						if (lmDown) m.getKey().toggle();
						if (rmDown) mw.mods.replace(m.getKey(), !m.getValue());
						if (lmDown || rmDown) client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
						GL11.glTranslated(0, 0, -300);
					}

					/* draw settings */
					if (m.getValue()) {
						for (SettingBase s: m.getKey().getSettings()) {
							count++;
							if (s instanceof SettingMode) drawModeSetting(matrix, s.toMode(), x, y+(count*12));
							if (s instanceof SettingToggle) drawToggleSetting(matrix, s.toToggle(), x, y+(count*12));
							if (s instanceof SettingSlider) drawSliderSetting(matrix, s.toSlider(), x, y+(count*12));
							//fill(x+len-1, y+(count*12), x+len, y+12+(count*12), 0x9f70fff0);
						}
						count++;
						drawBindSetting(matrix, m.getKey(), keyDown, x, y+(count*12));
						//fill(x+len-1, y+(count*12), x+len, y+12+(count*12), 0x9f70fff0);
					}
					count++;
				}
			}
		}

		lmDown = false;
		rmDown = false;
		keyDown = -1;
	}

	public void drawBindSetting(MatrixStack matrix, Module m, int key, int x, int y) {
		Screen.fill(matrix, x, y+11, x+len-2, y+12, 0x90b0b0b0);
		Screen.fill(matrix, x+len - 2, y, x+len-1, y+12, 0x90b0b0b0);
		Screen.fill(matrix, x, y - 1, x + 1, y+11, 0x90000000);

		if (key >= 0 && mouseOver(x, y, x+len, y+12)) m.setKey((key != GLFW.GLFW_KEY_DELETE && key != GLFW.GLFW_KEY_ESCAPE) ? key : Module.KEY_UNBOUND);
		//System.out.println(m.getName() + " | " + key);

		String name = m.getKey() < 0 ? "NONE" : InputUtil.fromKeyCode(m.getKey(), -1).getLocalizedText().getString();
		if (name == null) name = "KEY" + m.getKey();
		else if (name.isEmpty()) name = "NONE";

		textRenderer.drawWithShadow(matrix, "Bind: " + name + (mouseOver(x, y, x+len, y+12) ? "..." : "")
				, x+2, y+2, mouseOver(x, y, x+len, y+12) ? 0xcfc3cf : 0xcfe0cf);
	}

	public void drawModeSetting(MatrixStack matrix, SettingMode s, int x, int y) {
		fillGreySides(matrix, x, y-1, x+len-1, y+12);
		textRenderer.drawWithShadow(matrix, s.text + s.modes[s.mode],x+2, y+2,
				mouseOver(x, y, x+len, y+12) ? 0xcfc3cf : 0xcfe0cf);

		if (mouseOver(x, y, x+len, y+12) && lmDown) s.mode = s.getNextMode();
	}

	public void drawToggleSetting(MatrixStack matrix, SettingToggle s, int x, int y) {
		String color2;

		if (s.state) { if (mouseOver(x, y, x+len, y+12)) color2 = "\u00a72"; else color2 = "\u00a7a";
		} else { if (mouseOver(x, y, x+len, y+12)) color2 = "\u00a74"; else color2 = "\u00a7c"; }

		fillGreySides(matrix, x, y-1, x+len-1, y+12);
		textRenderer.drawWithShadow(matrix, color2 + s.text, x+3, y+2, -1);

		if (mouseOver(x, y, x+len, y+12) && lmDown) s.state = !s.state;
	}

	public void drawSliderSetting(MatrixStack matrix, SettingSlider s, int x, int y) {
		int pixels = (int) Math.round(MathHelper.clamp((len-2)*((s.getValue() - s.min) / (s.max - s.min)), 0, len-2));
		fillGreySides(matrix, x, y-1, x+len-1, y+12);
		fillGradient(matrix, x+1, y, x+pixels, y+12, 0xf03080a0, 0xf02070b0);

		textRenderer.drawWithShadow(matrix, s.text + (s.round == 0  && s.getValue() > 100 ? Integer.toString((int)s.getValue()) : s.getValue()),
				x+2, y+2, mouseOver(x, y, x+len, y+12) ? 0xcfc3cf : 0xcfe0cf);

		if (mouseOver(x+1, y, x+len-2, y+12) && lmHeld) {
			int percent = ((mouseX - x) * 100) / (len - 2);

			s.setValue(s.round(percent*((s.max - s.min) / 100) + s.min, s.round));
		}
	}

	private String cutText(String text, int leng) {
		String text1 = text;
		for (int i = 0; i < text.length(); i++) {
			if (textRenderer.getWidth(text1) < len-2) return text1;
			text1 = text1.replaceAll(".$", "");
		}
		return "";
	}

	protected boolean mouseOver(int minX, int minY, int maxX, int maxY) {
		return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY < maxY;
	}

	public void fillReverseGrey(MatrixStack matrix, int x1, int y1, int x2, int y2) {
		Screen.fill(matrix, x1, y1, x1 + 1, y2 - 1, 0x90000000);
		Screen.fill(matrix, x1 + 1, y1, x2 - 1, y1 + 1, 0x90000000);
		Screen.fill(matrix, x2 - 2, y1 + 1, x2, y2, 0x90b0b0b0);
	}

	public void fillGreySides(MatrixStack matrix, int x1, int y1, int x2, int y2) {
		Screen.fill(matrix, x1, y1, x1 + 1, y2 - 1, 0x90000000);
		Screen.fill(matrix, x2 - 1, y1 + 1, x2, y2, 0x90b0b0b0);
	}

	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		if (p_mouseClicked_5_ == 0) {
			lmDown = true;
			lmHeld = true;
		} else if (p_mouseClicked_5_ == 1) rmDown = true;

		// Fix having to double click windows to move them
		for (Window w: windows) {
			if (mouseOver(w.x1, w.y1, w.x2, w.y2)) {
				w.onMousePressed((int) p_mouseClicked_1_, (int) p_mouseClicked_3_);
				break;
			}
		}

		return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
	}

	public boolean mouseReleased(double double_1, double double_2, int int_1) {
		if (int_1 == 0) lmHeld = false;
		return super.mouseReleased(double_1, double_2, int_1);
	}

	public boolean keyPressed(int int_1, int int_2, int int_3) {
		keyDown = int_1;
		return super.keyPressed(int_1, int_2, int_3);
	}

	public void resetGui() {
		int x = 30;
		for (Window m: windows) {
			m.x1 = x;
			m.y2 = 35;
			x += len + 5;
		}
	}
}

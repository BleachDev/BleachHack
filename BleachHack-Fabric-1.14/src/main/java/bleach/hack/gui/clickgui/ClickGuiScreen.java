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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import bleach.hack.BleachHack;
import bleach.hack.command.Command;
import bleach.hack.gui.clickgui.modulewindow.ClickGuiWindow;
import bleach.hack.gui.clickgui.modulewindow.ModuleWindow;
import bleach.hack.gui.window.AbstractWindowScreen;
import bleach.hack.gui.window.Window;
import bleach.hack.module.Category;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;

public class ClickGuiScreen extends AbstractWindowScreen {

	private int keyDown = -1;
	private boolean lmDown = false;
	private boolean rmDown = false;
	private boolean lmHeld = false;
	
	public ClickGuiScreen() {
		super(new LiteralText("ClickGui"));
	}

	public void initWindows() {
		int len = (int) ModuleManager.getModule(ClickGui.class).getSettings().get(0).toSlider().getValue();
		
		int i = 10;
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
		this.minecraft.openScreen(null);
	}

	public void render(int mX, int mY, float float_1) {
		this.renderBackground();
		font.draw("BleachHack-1.15-" + BleachHack.VERSION, 3, 3, 0x305090);
		font.draw("BleachHack-1.15-" + BleachHack.VERSION, 2, 2, 0x6090d0);
		font.drawWithShadow("Current prefix is: \"" + Command.PREFIX + "\" (" + Command.PREFIX + "help)", 2, height-20, 0x99ff99);
		font.drawWithShadow("Use " + Command.PREFIX + "guireset to reset the gui" , 2, height-10, 0x9999ff);
		
		for (Window w: windows) {
			if (w instanceof ClickGuiWindow) ((ClickGuiWindow)w).updateKeys(mX, mY, keyDown, lmDown, rmDown, lmHeld);
		}
		
		super.render(mX, mY, float_1);
		
		for (Window w: windows) {
			if (w instanceof ClickGuiWindow) {
				Triple<Integer, Integer, String> tooltip = ((ClickGuiWindow) w).getTooltip();
				if (tooltip != null) {
					/* Match lines to end of words */
					Matcher mat = Pattern.compile("\\b.{1,22}\\b\\W?").matcher(tooltip.getRight());

					int c2 = 0;
					int c3 = 0;
					while (mat.find()) { c2++; } mat.reset();

					while (mat.find()) {
						fill(tooltip.getLeft(), tooltip.getMiddle() - 1 - (c2 * 10) + (c3 * 10),
								tooltip.getLeft() + 3 + font.getStringWidth(mat.group().trim()), tooltip.getMiddle() - (c2 * 10) + (c3 * 10) + 9,
								0xff000000);
						font.drawWithShadow(mat.group(), tooltip.getLeft() + 2, tooltip.getMiddle() - (c2 * 10) + (c3 * 10), -1);
						c3++;
					}
				}
			}
		}

		lmDown = false;
		rmDown = false;
		keyDown = -1;
	}

	public boolean mouseClicked(double double_1, double double_2, int int_1) {
		if (int_1 == 0) {
			lmDown = true;
			lmHeld = true;
		} else if (int_1 == 1) rmDown = true;

		// Fix having to double click windows to move them
		for (Window w: windows) {
			if (double_1 > w.x1 && double_1 < w.x2 && double_2 > w.y1 && double_2 < w.y2 && !w.closed) {
				w.onMousePressed((int) double_1, (int) double_2);
				break;
			}
		}

		return super.mouseClicked(double_1, double_2, int_1);
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
			x += (int) ModuleManager.getModule(ClickGui.class).getSettings().get(0).toSlider().getValue() + 5;
		}
	}
}

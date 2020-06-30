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

import bleach.hack.gui.window.Window;
import bleach.hack.module.Module;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class ModuleWindow extends Window {
	
	public List<Module> modList = new ArrayList<>();
	public LinkedHashMap<Module, Boolean> mods = new LinkedHashMap<>();
	
	public boolean hiding;
	
	public ModuleWindow(List<Module> mods, int x1, int y1, int len, String title, ItemStack icon) {
		super(x1, y1, x1 + len, 0, title, icon);
		modList = mods;
		for (Module m: mods) this.mods.put(m, false);
		y2 = getHeight();
	}
	
	public boolean shouldClose(int mX, int mY) {
		return false;
	}
	
	public void fillGrey(MatrixStack matrix, int x1, int y1, int x2, int y2) {
		Screen.fill(matrix, x1, y1, x1 + 1, y2 - 1, 0x90b0b0b0);
		Screen.fill(matrix, x1 + 1, y1, x2 - 1, y1 + 1, 0x90b0b0b0);
		Screen.fill(matrix, x1 + 1, y2 - 1, x2, y2, 0x90000000);
		Screen.fill(matrix, x2 - 1, y1 + 1, x2, y2, 0x90000000);
		Screen.fill(matrix, x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xff505059);
	}
	
	protected void drawBar(MatrixStack matrix, int mX, int mY, TextRenderer textRend) {
		/* background and title bar */
		fillGrey(matrix, x1, y1, x2, y2);
		fillGradient(matrix, x1 + 1, y1 + 1, x2 - 2, y1 + 12, 0xff0000ff, 0xff4080ff);
		
		/* buttons */
		//fillRealGrey(x2 - 12, y1 + 3, x2 - 4, y1 + 11);
		textRend.draw(matrix, hiding ? "+" : "_", x2 - 11, y1 + (hiding ? 3 : 1), 0xffffff);
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

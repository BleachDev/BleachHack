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
package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.file.BleachFileMang;

public class Spammer extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode(new String[] {"Random", "Order"}, "Read: "),
			new SettingSlider(1, 120, 20, 0, "Delay: "));
	
	private BleachFileMang fileMang = new BleachFileMang();
	private Random rand = new Random();
	private List<String> lines = new ArrayList<>();
	private int lineCount = 0;
	
	public Spammer() {
		super("Spammer", -1, Category.MISC, "Spams chat with messagees you set (edit in spammer.txt)", settings);
	}
	
	public void onEnable() {
		fileMang.createFile("spammer.txt", "");
		lines = fileMang.readFileLines("spammer.txt");
		lineCount = 0;
	}
	
	public void onUpdate() {
		if (this.isToggled()) {
			if (lines.isEmpty()) return;
			
			if (mc.player.ticksExisted % (int) (getSettings().get(1).toSlider().getValue() * 20) == 0) {
				if (getSettings().get(0).toMode().mode == 0) {
					mc.player.sendChatMessage(lines.get(rand.nextInt(lines.size())));
				} else if (getSettings().get(0).toMode().mode == 1) {
					mc.player.sendChatMessage(lines.get(lineCount));
				}
				
				if (lineCount >= lines.size() -1) lineCount = 0;
				else lineCount++;
			}
		}
	}

}

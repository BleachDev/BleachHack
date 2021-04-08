/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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
import java.util.List;
import java.util.Random;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.util.file.BleachFileMang;

public class Spammer extends Module {

	private Random rand = new Random();
	private List<String> lines = new ArrayList<>();
	private int lineCount = 0;

	public Spammer() {
		super("Spammer", KEY_UNBOUND, Category.MISC, "Spams chat with messagees you set (edit in spammer.txt)",
				new SettingMode("Read", "Random", "Order").withDesc("How to read the spammer file"),
				new SettingSlider("Delay", 1, 120, 20, 0).withDesc("Delay between messages"));
	}

	@Override
	public void onEnable() {
		super.onEnable();
		BleachFileMang.createFile("spammer.txt");
		lines = BleachFileMang.readFileLines("spammer.txt");
		lineCount = 0;
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (lines.isEmpty())
			return;

		if (mc.player.age % (int) (getSetting(1).asSlider().getValue() * 20) == 0) {
			if (getSetting(0).asMode().mode == 0) {
				mc.player.sendChatMessage(lines.get(rand.nextInt(lines.size())));
			} else if (getSetting(0).asMode().mode == 1) {
				mc.player.sendChatMessage(lines.get(lineCount));
			}

			if (lineCount >= lines.size() - 1) {
				lineCount = 0;
			} else {
				lineCount++;
			}
		}
	}

}

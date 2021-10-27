/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.ModuleClickGuiScreen;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingSlider;
import bleach.hack.module.setting.base.SettingToggle;
import bleach.hack.module.Module;

public class ClickGui extends Module {

	public static final ModuleClickGuiScreen clickGui = new ModuleClickGuiScreen();

	public ClickGui() {
		super("ClickGui", GLFW.GLFW_KEY_RIGHT_SHIFT, ModuleCategory.RENDER, "Draws the clickgui.",
				new SettingSlider("Length", 70, 85, 75, 0).withDesc("The length of each window."),
				new SettingToggle("Search bar", true).withDesc("Shows a search bar."),
				new SettingToggle("Help", true).withDesc("Shows the help text."));
	}

	@Override
	public void onEnable(boolean inWorld) {
		super.onEnable(inWorld);

		mc.setScreen(clickGui);
	}

	@Override
	public void onDisable(boolean inWorld) {
		if (mc.currentScreen instanceof ModuleClickGuiScreen) {
			mc.setScreen(null);
		}

		super.onDisable(inWorld);
	}
}

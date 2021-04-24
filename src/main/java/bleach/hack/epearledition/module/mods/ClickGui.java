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
package bleach.hack.epearledition.module.mods;

import bleach.hack.epearledition.gui.clickgui.ClickGuiScreen;
import bleach.hack.epearledition.module.Category;
import bleach.hack.epearledition.module.Module;
import bleach.hack.epearledition.setting.base.SettingMode;
import bleach.hack.epearledition.setting.base.SettingSlider;
import bleach.hack.epearledition.setting.base.SettingToggle;
import org.lwjgl.glfw.GLFW;

public class ClickGui extends Module {

    public static ClickGuiScreen clickGui = new ClickGuiScreen();

    public ClickGui() {
        super("ClickGUI", GLFW.GLFW_KEY_RIGHT_SHIFT, Category.CLIENT, "Draws the clickgui",
                new SettingSlider("Length", 70, 85, 85, 0),
                new SettingToggle("Search bar", true),
                new SettingToggle("Help", true),
                new SettingToggle("Static descriptions", true),
                new SettingMode("Theme", "SalHackSkid", "Wire", "Full", "Clear"),
                new SettingToggle("Icons", true),
                new SettingToggle("Enable Padding", true));
    }

    public void onEnable() {
        mc.openScreen(clickGui);
        setToggled(false);
    }
}

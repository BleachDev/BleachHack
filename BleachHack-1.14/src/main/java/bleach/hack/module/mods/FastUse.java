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

import java.util.Arrays;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class FastUse extends Module {
	
	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode(new String[] {"Single", "Multi"}, "Mode: "),
			new SettingSlider(1, 100, 20, 0, "Multi: "));
	
	public FastUse() {
		super("FastUse", GLFW.GLFW_KEY_B, Category.PLAYER, "Allows you to use items faster", settings);
	}
	
	public void onUpdate() {
		if (this.isToggled()) {
			try {
				/* set rightClickDelay to 0 */
				ObfuscationReflectionHelper.findField(Minecraft.class, "field_71467_ac").setInt(mc, 0);
				
				/* call rightClickMouse */
				if (getSettings().get(0).toMode().mode == 1 && mc.gameSettings.keyBindUseItem.isKeyDown()) {
					for (int i = 0; i < (int) getSettings().get(1).toSlider().getValue(); i++) {
						ObfuscationReflectionHelper.findMethod(Minecraft.class, "func_147121_ag").invoke(mc);
					}
				}
			} catch (Exception e) {}
		}
	}

}

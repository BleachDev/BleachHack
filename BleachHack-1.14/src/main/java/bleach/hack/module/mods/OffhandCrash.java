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
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket.Action;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class OffhandCrash extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingSlider(0, 2000, 420, 0, "Switches: "),
			new SettingToggle(true, "Player Packet"));
	
	public OffhandCrash() {
		super("OffhandCrash", GLFW.GLFW_KEY_P, Category.EXPLOITS, "Lags people using the snowball exploit", settings);
	}
	
	public void onUpdate() {
		if (this.isToggled()) {
			for (int i = 0; i < getSettings().get(0).toSlider().getValue(); i++) {
				mc.player.connection.sendPacket(new CPlayerDiggingPacket(Action.SWAP_HELD_ITEMS, BlockPos.ZERO, Direction.UP));
				if (getSettings().get(1).toToggle().state) mc.player.connection.sendPacket(new CPlayerPacket(true));
			}
		}
	}
}

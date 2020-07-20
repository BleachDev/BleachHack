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

import java.util.HashMap;
import java.util.Map.Entry;

import bleach.hack.event.events.EventTick;
import com.google.common.eventbus.Subscribe;
import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.WorldUtils;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Scaffold extends Module {

	private HashMap<BlockPos, Integer> lastPlaced = new HashMap<>();

	public Scaffold() {
		super("Scaffold", GLFW.GLFW_KEY_N, Category.WORLD, "Places blocks under you",
				new SettingSlider("Range: ", 0, 1, 0.3, 1),
				new SettingMode("Mode: ", "Normal", "3x3", "5x5"),
				new SettingToggle("Rotate", false).withDesc("Rotate serverside"),
				new SettingSlider("BPT: ", 1, 10, 2, 0).withDesc("Blocks Per Tick, how many blocks to place per tick"));
	}

	@Subscribe
	public void onTick(EventTick event) {
		for (Entry<BlockPos, Integer> e: new HashMap<>(lastPlaced).entrySet()) {
			if (e.getValue() > 0) lastPlaced.replace(e.getKey(), e.getValue() - 1);
			else lastPlaced.remove(e.getKey());
		}

		int slot = -1;
		int prevSlot = mc.player.inventory.selectedSlot;

		if (mc.player.inventory.getMainHandStack().getItem() instanceof BlockItem) {
			slot = mc.player.inventory.selectedSlot;
		} else for (int i = 0; i < 9; i++) {
			if (mc.player.inventory.getInvStack(i).getItem() instanceof BlockItem) {
				slot = i;
				break;
			}
		}

		if (slot == -1) return;

		mc.player.inventory.selectedSlot = slot;
		double range = getSettings().get(0).asSlider().getValue();
		int mode = getSettings().get(1).asMode().mode;
		boolean rotate = getSettings().get(2).asToggle().state;

		if (mode == 0) {
			for (int r = 0; r < 5; r++) {
				Vec3d r1 = new Vec3d(0,-0.85,0);
				if (r == 1) r1 = r1.add(range, 0, 0);
				if (r == 2) r1 = r1.add(-range, 0, 0);
				if (r == 3) r1 = r1.add(0, 0, range);
				if (r == 4) r1 = r1.add(0, 0, -range);

				if (WorldUtils.placeBlock(new BlockPos(mc.player.getPos().add(r1)), -1, rotate, false)) {
					return;
				}
			}
		} else {
			int cap = 1;
			for (int x = (mode == 1 ? -1 : -2); x <= (mode == 1 ? 1 : 2); x++) {
				for (int z = (mode == 1 ? -1 : -2); z <= (mode == 1 ? 1 : 2); z++) {
					if (WorldUtils.placeBlock(new BlockPos(mc.player.getPos().add(x, -0.85, z)), -1, rotate, false)) {
						cap++;
					}
					
					if (cap > getSettings().get(3).asSlider().getValue()) return;
				}
			}
		}

		mc.player.inventory.selectedSlot = prevSlot;
	}

}

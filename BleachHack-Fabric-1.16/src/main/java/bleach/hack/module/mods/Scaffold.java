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
import java.util.LinkedHashSet;
import java.util.Set;

import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;

import com.google.common.eventbus.Subscribe;
import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingColor;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.RenderUtils;
import bleach.hack.utils.WorldUtils;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Scaffold extends Module {
	
	private Set<BlockPos> renderBlocks = new LinkedHashSet<>();

	public Scaffold() {
		super("Scaffold", GLFW.GLFW_KEY_N, Category.WORLD, "Places blocks under you",
				new SettingSlider("Range: ", 0, 1, 0.3, 1),
				new SettingMode("Mode: ", "Normal", "3x3", "5x5", "7x7"),
				new SettingToggle("Rotate", false).withDesc("Rotate serverside"),
				new SettingToggle("SafeWalk", true).withDesc("Prevents you from walking of edges when scaffold is on"),
				new SettingToggle("Highlight", false).withDesc("Highlights the blocks you are placing").withChildren(
						new SettingColor("Color", 1f, 0.75f, 0.2f, false).withDesc("Color for the block highlight"),
						new SettingToggle("Placed", false).withDesc("Highlights blocks that are already placed")),
				new SettingSlider("BPT: ", 1, 10, 2, 0).withDesc("Blocks Per Tick, how many blocks to place per tick"));
	}

	@Subscribe
	public void onTick(EventTick event) {
		renderBlocks.clear();
		
		int slot = -1;
		int prevSlot = mc.player.inventory.selectedSlot;

		if (mc.player.inventory.getMainHandStack().getItem() instanceof BlockItem) {
			slot = mc.player.inventory.selectedSlot;
		} else for (int i = 0; i < 9; i++) {
			if (mc.player.inventory.getStack(i).getItem() instanceof BlockItem) {
				slot = i;
				break;
			}
		}

		if (slot == -1) return;

		mc.player.inventory.selectedSlot = slot;
		double range = getSetting(0).asSlider().getValue();
		int mode = getSetting(1).asMode().mode;
		boolean rotate = getSetting(2).asToggle().state;

		Vec3d placeVec = mc.player.getPos().add(0, -0.85, 0);
		Set<BlockPos> blocks = (mode == 0
				? new LinkedHashSet<>(Arrays.asList(new BlockPos(placeVec), new BlockPos(placeVec.add(range, 0, 0)), new BlockPos(placeVec.add(-range, 0, 0)),
						new BlockPos(placeVec.add(0, 0, range)), new BlockPos(placeVec.add(0, 0, -range))))
						: getSpiral(mode, new BlockPos(placeVec)));
		
		if (getSetting(4).asToggle().state) {
			for (BlockPos bp: blocks) {
				if (getSetting(4).asToggle().getChild(1).asToggle().state || WorldUtils.isBlockEmpty(bp)) {
					renderBlocks.add(bp);
				}
			}
		}

		int cap = 0;
		for (BlockPos bp: blocks) {
			if (WorldUtils.placeBlock(bp, -1, rotate, false)) {
				cap++;
				if (cap >= (int) getSetting(5).asSlider().getValue()) return;
			}
		}

		mc.player.inventory.selectedSlot = prevSlot;
	}
	
	@Subscribe
	public void onWorldRender(EventWorldRender event) {
		if (getSetting(4).asToggle().state) {
			float[] col = getSetting(4).asToggle().getChild(0).asColor().getRGBFloat();
			for (BlockPos bp: renderBlocks) {
				RenderUtils.drawFilledBox(bp, col[0], col[1], col[2], 0.7f);
				
				col[0] = Math.max(0f, col[0] - 0.01f);
				col[2] = Math.min(1f, col[2] + 0.01f);
			}
		}
	}

	private Set<BlockPos> getSpiral(int size, BlockPos center) {
		Set<BlockPos> set = new LinkedHashSet<>(Arrays.asList(center));

		if (size == 0) return set;

		int step = 1;
		int neededSteps = size * 4;
		BlockPos currentPos = center;
		for (int i = 0; i <= neededSteps; i++) {
			// Do 1 less step on the last side to not overshoot the spiral
			if (i == neededSteps) step--;

			for (int j = 0; j < step; j++) {
				if (i % 4 == 0) currentPos = currentPos.add(-1, 0, 0);
				else if (i % 4 == 1) currentPos = currentPos.add(0, 0, -1);
				else if (i % 4 == 2) currentPos = currentPos.add(1, 0, 0);
				else currentPos = currentPos.add(0, 0, 1);

				set.add(currentPos);
			}

			if (i % 2 != 0) step++;
		}

		return set;
	}
}

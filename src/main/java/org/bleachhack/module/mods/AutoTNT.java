/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.util.InventoryUtils;
import org.bleachhack.util.world.WorldUtils;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class AutoTNT extends Module {

	private IntList blacklist = new IntArrayList();

	public AutoTNT() {
		super("AutoTNT", KEY_UNBOUND, ModuleCategory.MISC, "Automatically places tnt in a grid pattern.",
				new SettingSlider("Distance", 1, 5, 3, 0).withDesc("How far away from eachother the tnt should be."));
	}

	@Override
	public void onDisable(boolean inWorld) {
		blacklist.clear();

		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		int tntSlot = InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.TNT);
		if (tntSlot == -1)
			return;

		int dist = getSetting(0).asSlider().getValueInt();
		for (int i = -3; i < 4; i++)  {
			for (int j = -3; j < 4; j++)  {
				int x = (int) mc.player.getX() - (int) mc.player.getX() % dist - i * dist;
				int z = (int) mc.player.getZ() - (int) mc.player.getZ() % dist - j * dist;

				boolean skip = false;
				for (int l = 0; l < blacklist.size(); l += 2) {
					if (x == blacklist.getInt(l) && z == blacklist.getInt(l + 1)) {
						skip = true;
						break;
					}
				}

				if (skip)
					continue;

				for (int k = -3; k < 4; k++) {
					int y = (int) mc.player.getY() + k;
					if (mc.player.squaredDistanceTo(x + 0.5, y + 0.5, z + 0.5) < 4.25
							&& WorldUtils.placeBlock(new BlockPos(x, y, z), tntSlot, 0, false, false, true)) {
						blacklist.add(x);
						blacklist.add(z);
						return;
					}
				}
			}
		}
	}

}

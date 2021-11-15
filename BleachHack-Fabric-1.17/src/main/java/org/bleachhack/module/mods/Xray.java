/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.module.setting.base.SettingSlider;
import org.bleachhack.module.setting.base.SettingToggle;
import org.bleachhack.module.setting.other.SettingBlockList;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class Xray extends Module {

	private double gamma;

	public Xray() {
		super("Xray", KEY_UNBOUND, ModuleCategory.RENDER, "Baritone is for zoomers.",
				new SettingToggle("Fluids", false).withDesc("Show fluids."),
				new SettingToggle("Opacity", true).withDesc("Toggles an adjustable alpha level for non-xray blocks.").withChildren(
						new SettingSlider("Value", 0, 255, 64, 0).withDesc("Block alpha value."),
						new SettingToggle("HideSurface", false).withDesc("Hides the surface of the world to make it easier to see blocks.")),
				new SettingBlockList("Edit Blocks", "Edit Xray Blocks",
						Blocks.COPPER_ORE,
						Blocks.IRON_ORE,
						Blocks.GOLD_ORE,
						Blocks.LAPIS_ORE,
						Blocks.REDSTONE_ORE,
						Blocks.DIAMOND_ORE,
						Blocks.EMERALD_ORE,
						Blocks.DEEPSLATE_COPPER_ORE,
						Blocks.DEEPSLATE_IRON_ORE,
						Blocks.DEEPSLATE_GOLD_ORE,
						Blocks.DEEPSLATE_LAPIS_ORE,
						Blocks.DEEPSLATE_REDSTONE_ORE,
						Blocks.DEEPSLATE_DIAMOND_ORE,
						Blocks.DEEPSLATE_EMERALD_ORE,
						Blocks.COPPER_BLOCK,
						Blocks.IRON_BLOCK,
						Blocks.GOLD_BLOCK,
						Blocks.LAPIS_BLOCK,
						Blocks.REDSTONE_BLOCK,
						Blocks.DIAMOND_BLOCK,
						Blocks.EMERALD_BLOCK,
						Blocks.NETHER_GOLD_ORE,
						Blocks.ANCIENT_DEBRIS).withDesc("Edit the xray blocks."));
	}

	public boolean isVisible(Block block) {
		return !isEnabled() || getSetting(2).asList(Block.class).contains(block);
	}

	@Override
	public void onEnable(boolean inWorld) {
		super.onEnable(inWorld);

		mc.chunkCullingEnabled = false;
		mc.worldRenderer.reload();

		gamma = mc.options.gamma;
	}

	@Override
	public void onDisable(boolean inWorld) {
		mc.options.gamma = gamma;

		mc.chunkCullingEnabled = true;
		mc.worldRenderer.reload();

		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick eventPreUpdate) {
		mc.options.gamma = 69.420;
	}
}

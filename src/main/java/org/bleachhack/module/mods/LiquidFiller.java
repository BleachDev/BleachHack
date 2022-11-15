/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingColor;
import org.bleachhack.setting.module.SettingItemList;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingRotate;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.InventoryUtils;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.QuadColor;
import org.bleachhack.util.world.WorldUtils;

import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public class LiquidFiller extends Module {

	public LiquidFiller() {
		super("LiquidFiller", KEY_UNBOUND, ModuleCategory.WORLD, "Fills in liquids by placing blocks in them.",
				new SettingMode("Liquid", "Lava", "Water", "Both").withDesc("What liquids to fill."),
				new SettingSlider("Range", 1, 6, 4.5, 1).withDesc("How far to fill liquids."),
				new SettingSlider("BPT", 1, 6, 1, 0).withDesc("How many blocks to place per tick."),
				new SettingToggle("AirPlace", true).withDesc("Places blocks in the air."),
				new SettingToggle("LegitPlace", false).withDesc("Only places on sides of blocks you can see."),
				new SettingToggle("Filter", false).withDesc("Filters certain blocks from being placed.").withChildren(
						new SettingMode("Mode", "Blacklist", "Whitelist").withDesc("How to handle the list."),
						new SettingItemList("Edit Blocks", "Edit Filtered Blocks", i -> i instanceof BlockItem).withDesc("Edit the filtered blocks.")),
				new SettingRotate(false).withDesc("Rotates when placing blocks."),
				new SettingToggle("Highlight", true).withDesc("Highlights liquids to fill.").withChildren(
						new SettingSlider("Opacity", 0.01, 1, 0.3, 2),
						new SettingColor("Water Color", 0, 128, 255).withDesc("Color of water."),
						new SettingColor("Lava Color", 255, 190, 0).withDesc("Color of lava.")));
	}

	private boolean shouldUseItem(Item item) {
		if (!(item instanceof BlockItem)) {
			return false;
		}

		if (getSetting(5).asToggle().getState()) {
			boolean contains = getSetting(5).asToggle().getChild(1).asList(Item.class).contains(item);

			return (getSetting(5).asToggle().getChild(0).asMode().getMode() == 0 && !contains)
					|| (getSetting(5).asToggle().getChild(0).asMode().getMode() == 1 && contains);
		}

		return true;
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		int cap = 0;
		int ceilRange = (int) Math.ceil(getSetting(1).asSlider().getValue());
		for (BlockPos pos: BlockPos.iterateOutwards(mc.player.getBlockPos().up(), ceilRange, ceilRange, ceilRange)) {
			FluidState fluid = mc.world.getFluidState(pos);

			int slot = InventoryUtils.getSlot(true, i -> shouldUseItem(mc.player.getInventory().getStack(i).getItem()));
			if ((fluid.getFluid() instanceof WaterFluid.Still && getSetting(0).asMode().getMode() != 0)
							|| (fluid.getFluid() instanceof LavaFluid.Still && getSetting(0).asMode().getMode() != 1)) {
					if (WorldUtils.placeBlock(
							pos, slot,
							getSetting(6).asRotate(),
							getSetting(4).asToggle().getState(),
							getSetting(3).asToggle().getState(),
							true)) {
						cap++;

						if (cap >= getSetting(2).asSlider().getValueInt()) {
							return;
						}
					}
			}
		}
	}

	@BleachSubscribe
	public void onWorldRender(EventWorldRender.Post event) {
		if (getSetting(7).asToggle().getState()) {
			int opacity = (int) (getSetting(7).asToggle().getChild(0).asSlider().getValue() * 255);
			QuadColor waterColor = QuadColor.single((opacity << 24) | getSetting(7).asToggle().getChild(1).asColor().getRGB());
			QuadColor lavaColor = QuadColor.single((opacity << 24) | getSetting(7).asToggle().getChild(2).asColor().getRGB());
			
			int ceilRange = (int) Math.ceil(getSetting(1).asSlider().getValue());
			for (BlockPos pos: BlockPos.iterateOutwards(mc.player.getBlockPos().up(), ceilRange, ceilRange, ceilRange)) {
				FluidState fluid = mc.world.getFluidState(pos);

				if (fluid.getFluid() instanceof WaterFluid.Still && getSetting(0).asMode().getMode() != 0) {
					Renderer.drawBoxBoth(fluid.getShape(mc.world, pos).getBoundingBox().offset(pos), waterColor, 3f);
				} else if (fluid.getFluid() instanceof LavaFluid.Still && getSetting(0).asMode().getMode() != 1) {
					Renderer.drawBoxBoth(fluid.getShape(mc.world, pos).getBoundingBox().offset(pos), lavaColor, 3f);
				}
			}
		}
	}
}

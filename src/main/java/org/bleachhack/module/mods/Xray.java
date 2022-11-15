/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventRenderBlock;
import org.bleachhack.event.events.EventRenderFluid;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingBlockList;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.world.WorldUtils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FernBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.client.render.RenderLayer;

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

	@Override
	public void onEnable(boolean inWorld) {
		super.onEnable(inWorld);

		mc.chunkCullingEnabled = false;
		mc.worldRenderer.reload();

		gamma = mc.options.getGamma().getValue();
	}

	@Override
	public void onDisable(boolean inWorld) {
		mc.options.getGamma().setValue(gamma);

		mc.chunkCullingEnabled = true;
		mc.worldRenderer.reload();

		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick eventPreUpdate) {
		mc.options.getGamma().setValue(69.420);
	}

	@BleachSubscribe
	public void onRenderBlockLight(EventRenderBlock.Light event) {
		event.setLight(1f);
	}

	@BleachSubscribe
	public void onRenderBlockOpaque(EventRenderBlock.Opaque event) {
		event.setOpaque(true);
	}

	@BleachSubscribe
	public void onRenderBlockDrawSide(EventRenderBlock.ShouldDrawSide event) {
		if (getSetting(2).asList(Block.class).contains(event.getState().getBlock())) {
			event.setDrawSide(true);
		} else if (!getSetting(1).asToggle().getState()) {
			event.setDrawSide(false);
		}
	}

	@BleachSubscribe
	public void onRenderBlockTesselate(EventRenderBlock.Tesselate event) {
		if (!getSetting(2).asList(Block.class).contains(event.getState().getBlock())) {
			if (getSetting(1).asToggle().getState()) {
				if (getSetting(1).asToggle().getChild(1).asToggle().getState()
						&& (event.getState().getBlock() instanceof FernBlock
								|| event.getState().getBlock() instanceof TallPlantBlock
								|| WorldUtils.getTopBlockIgnoreLeaves(event.getPos().getX(), event.getPos().getZ()) == event.getPos().getY())) {
					event.setCancelled(true);
					return;
				}

				event.getVertexConsumer().fixedColor(-1, -1, -1, getSetting(1).asToggle().getChild(0).asSlider().getValueInt());
			} else {
				event.setCancelled(true);
			}
		}
	}

	@BleachSubscribe
	public void onRenderBlockLayer(EventRenderBlock.Layer event) {
		if (getSetting(1).asToggle().getState() && !getSetting(2).asList(Block.class).contains(event.getState().getBlock())) {
			event.setLayer(RenderLayer.getTranslucent());
		}
	}

	@BleachSubscribe
	public void onRenderFluid(EventRenderFluid event) {
		if (!getSetting(0).asToggle().getState()) {
			event.setCancelled(true);
		}
	}
}

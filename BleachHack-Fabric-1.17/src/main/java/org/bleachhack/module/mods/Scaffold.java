/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import java.util.LinkedHashSet;
import java.util.Set;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.module.setting.base.SettingColor;
import org.bleachhack.module.setting.base.SettingMode;
import org.bleachhack.module.setting.base.SettingSlider;
import org.bleachhack.module.setting.base.SettingToggle;
import org.bleachhack.module.setting.other.SettingItemList;
import org.bleachhack.module.setting.other.SettingRotate;
import org.bleachhack.util.InventoryUtils;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.QuadColor;
import org.bleachhack.util.world.WorldUtils;

import com.google.common.collect.Sets;

import net.minecraft.client.util.InputUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Scaffold extends Module {

	private Set<BlockPos> renderBlocks = new LinkedHashSet<>();

	public Scaffold() {
		super("Scaffold", KEY_UNBOUND, ModuleCategory.WORLD, "Places blocks under you",
				new SettingMode("Area", "Normal", "3x3", "5x5", "7x7").withDesc("How big of an area to scaffold."),
				new SettingSlider("BPT", 1, 10, 2, 0).withDesc("Blocks Per Tick, how many blocks to place per tick."),
				new SettingSlider("Range", 0, 1, 0.3, 1).withDesc("How far to place ahead of you in Normal mode."),
				new SettingRotate(false).withDesc("Rotates when placing blocks."),
				new SettingToggle("LegitPlace", false).withDesc("Only places on sides you can see."),
				new SettingToggle("Filter", false).withDesc("Filters certain blocks.").withChildren(
						new SettingMode("Mode", "Blacklist", "Whitelist").withDesc("How to handle the list."),
						new SettingItemList("Edit Blocks", "Edit Filtered Blocks", i -> i instanceof BlockItem).withDesc("Edit the filtered blocks.")),
				new SettingToggle("Tower", true).withDesc("Makes scaffolding straight up much easier.").withChildren(
						new SettingToggle("Legit", false).withDesc("Slower mode that bypasses some anticheats.")),
				new SettingToggle("AirPlace", false).withDesc("Places blocks in the air without support blocks."),
				new SettingToggle("SafeWalk", true).withDesc("Prevents you from walking of edges when scaffold is on."),
				new SettingToggle("NoSwing", false).withDesc("Doesn't swing your hand clientside."),
				new SettingToggle("EmptyToggle", false).withDesc("Turns off when you run out of blocks."),
				new SettingToggle("Highlight", false).withDesc("Highlights the blocks you are placing.").withChildren(
						new SettingColor("Color", 1f, 0.75f, 0.2f, false).withDesc("Color for the block highlight."),
						new SettingToggle("Placed", false).withDesc("Highlights blocks that are already placed.")));
	}

	private boolean shouldUseItem(Item item) {
		if (!(item instanceof BlockItem)) {
			return false;
		}

		if (getSetting(5).asToggle().state) {
			boolean contains = getSetting(5).asToggle().getChild(1).asList(Item.class).contains(item);

			return (getSetting(5).asToggle().getChild(0).asMode().mode == 0 && !contains)
					|| (getSetting(5).asToggle().getChild(0).asMode().mode == 1 && contains);
		}

		return true;
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		renderBlocks.clear();

		int slot = InventoryUtils.getSlot(false, i -> shouldUseItem(mc.player.getInventory().getStack(i).getItem()));

		if (slot == -1) {
			if (getSetting(10).asToggle().state) {
				setEnabled(false);
			}

			return;
		}

		double range = getSetting(2).asSlider().getValue();
		int mode = getSetting(0).asMode().mode;

		Vec3d placeVec = mc.player.getPos().add(0, -0.85, 0);
		Set<BlockPos> blocks = mode == 0
				? Sets.newHashSet(
						new BlockPos(placeVec),
						new BlockPos(placeVec.add(range, 0, 0)),
						new BlockPos(placeVec.add(-range, 0, 0)),
						new BlockPos(placeVec.add(0, 0, range)),
						new BlockPos(placeVec.add(0, 0, -range)))
						: getSpiral(mode, new BlockPos(placeVec));

		if (getSetting(6).asToggle().state
				&& InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.keyJump.getBoundKeyTranslationKey()).getCode())) {

			if (mc.world.getBlockState(mc.player.getBlockPos().down()).getMaterial().isReplaceable()
					&& !mc.world.getBlockState(mc.player.getBlockPos().down(2)).getMaterial().isReplaceable()
					&& mc.player.getVelocity().y > 0) {
				mc.player.setVelocity(mc.player.getVelocity().x, -0.1, mc.player.getVelocity().z);

				if (!getSetting(6).asToggle().getChild(0).asToggle().state) {
					mc.player.jump();
				}
			}

			if (getSetting(6).asToggle().getChild(0).asToggle().state && mc.player.isOnGround()) {
				mc.player.jump();
			}
		}

		// Don't bother doing anything if there aren't any blocks to place on
		if (blocks.stream().allMatch(b -> !WorldUtils.isBlockEmpty(b))) {
			return;
		}

		if (getSetting(11).asToggle().state) {
			for (BlockPos bp : blocks) {
				if (getSetting(11).asToggle().getChild(1).asToggle().state || WorldUtils.isBlockEmpty(bp)) {
					renderBlocks.add(bp);
				}
			}
		}

		int cap = 0;
		for (BlockPos bp : blocks) {
			boolean placed = WorldUtils.placeBlock(
					bp, slot,
					getSetting(3).asRotate(),
					getSetting(4).asToggle().state,
					getSetting(7).asToggle().state,
					!getSetting(9).asToggle().state);

			if (placed) {
				cap++;

				if (cap >= getSetting(1).asSlider().getValueInt()) {
					return;
				}
			}
		}
	}

	@BleachSubscribe
	public void onWorldRender(EventWorldRender.Post event) {
		if (getSetting(11).asToggle().state) {
			float[] col = getSetting(11).asToggle().getChild(0).asColor().getRGBFloat();
			for (BlockPos bp : renderBlocks) {
				Renderer.drawBoxBoth(bp, QuadColor.single(col[0], col[1], col[2], 0.5f), 2.5f);

				col[0] = Math.max(0f, col[0] - 0.01f);
				col[2] = Math.min(1f, col[2] + 0.01f);
			}
		}
	}

	private Set<BlockPos> getSpiral(int size, BlockPos center) {
		Set<BlockPos> list = new LinkedHashSet<>();
		list.add(center);

		if (size == 0)
			return list;

		int step = 1;
		int neededSteps = size * 4;
		BlockPos currentPos = center;
		for (int i = 0; i <= neededSteps; i++) {
			// Do 1 less step on the last side to not overshoot the spiral
			if (i == neededSteps)
				step--;

			for (int j = 0; j < step; j++) {
				if (i % 4 == 0) {
					currentPos = currentPos.add(-1, 0, 0);
				} else if (i % 4 == 1) {
					currentPos = currentPos.add(0, 0, -1);
				} else if (i % 4 == 2) {
					currentPos = currentPos.add(1, 0, 0);
				} else {
					currentPos = currentPos.add(0, 0, 1);
				}

				list.add(currentPos);
			}

			if (i % 2 != 0)
				step++;
		}

		return list;
	}
}

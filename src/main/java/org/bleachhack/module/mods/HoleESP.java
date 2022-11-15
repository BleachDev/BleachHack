/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.ArrayUtils;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingColor;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.QuadColor;
import org.bleachhack.util.render.color.QuadColor.CardinalDirection;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="https://github.com/lasnikprogram">Lasnik</a>
 */
public class HoleESP extends Module {

	private Map<BlockPos, int[]> holes = new HashMap<>();

	public HoleESP() {
		super("HoleESP", KEY_UNBOUND, ModuleCategory.RENDER, "Highlights safe and not so safe holes. Used for Crystalpvp.",
				new SettingSlider("Radius", 1, 20, 10, 0).withDesc("Radius in which holes are getting searched."),
				new SettingToggle("RenderBottom", true).withDesc("Render the bottom of holes.").withChildren(
						new SettingMode("Render", "Box+Fill", "Box", "Fill").withDesc("The rendering method."),
						new SettingSlider("Box", 0.1, 4, 2, 1).withDesc("The thickness of the box lines."),
						new SettingSlider("Fill", 0, 1, 0.3, 2).withDesc("The opacity of the fill.")),
				new SettingToggle("RenderSides", true).withDesc("Render the sides of holes.").withChildren(
						new SettingMode("Render", "GlowUp", "GlowDown", "Box+Fill", "Box", "Fill").withDesc("The rendering method."),
						new SettingSlider("Box", 0.1, 4, 2, 1).withDesc("The thickness of the box lines."),
						new SettingSlider("Fill", 0, 1, 0.3, 2).withDesc("The opacity of the fill/glow"),
						new SettingSlider("Height", 0.1, 8, 1, 1).withDesc("The height to render the sides.")),
				new SettingToggle("Bedrock", true).withDesc("Shows holes with full bedrock.").withChildren(
						new SettingColor("Color", 0, 255, 0).withDesc("Color for bedrock holes.")),
				new SettingToggle("Mixed", true).withDesc("Shows holes with a mix of obsidian and bedrock.").withChildren(
						new SettingColor("Mixed", 255, 255, 0).withDesc("Color for mixed holes.")),
				new SettingToggle("Obsidian", true).withDesc("Shows holes with a mix of obsidian and bedrock.").withChildren(
						new SettingColor("Obsidian", 255, 0, 0).withDesc("Color for obsidian holes.")),
				new SettingToggle("HideWhenIn", true).withDesc("Hides the hole you're currently in to prevent blocking out your screen."));
	}

	@Override
	public void onDisable(boolean inWorld) {
		holes.clear();

		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (mc.player.age % 14 == 0) {
			holes.clear();

			int dist = getSetting(0).asSlider().getValueInt();

			for (BlockPos pos : BlockPos.iterateOutwards(mc.player.getBlockPos(), dist, dist, dist)) {
				if (!mc.world.isInBuildLimit(pos.down())
						|| (mc.world.getBlockState(pos.down()).getBlock() != Blocks.BEDROCK
						&& mc.world.getBlockState(pos.down()).getBlock() != Blocks.OBSIDIAN)
						|| !mc.world.getBlockState(pos).getCollisionShape(mc.world, pos).isEmpty()
						|| !mc.world.getBlockState(pos.up(1)).getCollisionShape(mc.world, pos.up(1)).isEmpty()
						|| !mc.world.getBlockState(pos.up(2)).getCollisionShape(mc.world, pos.up(2)).isEmpty()) {
					continue;
				}

				if (getSetting(6).asToggle().getState()
						&& mc.player.getBoundingBox().getCenter().x > pos.getX() + 0.1
						&& mc.player.getBoundingBox().getCenter().x < pos.getX() + 0.9
						&& mc.player.getBoundingBox().getCenter().z > pos.getZ() + 0.1
						&& mc.player.getBoundingBox().getCenter().z < pos.getZ() + 0.9) {
					continue;
				}

				int bedrockCounter = 0;
				int obsidianCounter = 0;
				for (BlockPos pos1 : neighbours(pos)) {
					if (mc.world.getBlockState(pos1).getBlock() == Blocks.BEDROCK) {
						bedrockCounter++;
					} else if (mc.world.getBlockState(pos1).getBlock() == Blocks.OBSIDIAN) {
						obsidianCounter++;
					} else {
						break;
					}
				}

				if (bedrockCounter == 5 && getSetting(3).asToggle().getState()) {
					holes.put(pos.toImmutable(), getSetting(3).asToggle().getChild(0).asColor().getRGBArray());
				} else if (obsidianCounter == 5 && getSetting(5).asToggle().getState()) {
					holes.put(pos.toImmutable(), getSetting(5).asToggle().getChild(0).asColor().getRGBArray());
				} else if (bedrockCounter >= 1 && obsidianCounter >= 1
						&& bedrockCounter + obsidianCounter == 5 && getSetting(4).asToggle().getState()) {
					holes.put(pos.toImmutable(), getSetting(4).asToggle().getChild(0).asColor().getRGBArray());
				}
			}
		}
	}

	@BleachSubscribe
	public void onRender(EventWorldRender.Post event) {
		if (getSetting(1).asToggle().getState()) {
			int bottomMode = getSetting(1).asToggle().getChild(0).asMode().getMode();
			Direction[] excludeDirs = ArrayUtils.remove(Direction.values(), 0);

			if (bottomMode == 0 || bottomMode == 2) {
				int alpha = (int) (getSetting(1).asToggle().getChild(2).asSlider().getValueFloat() * 255);
				holes.forEach((pos, color) ->
						Renderer.drawBoxFill(pos, QuadColor.single(color[0], color[1], color[2], alpha), excludeDirs));
			}

			if (bottomMode == 0 || bottomMode == 1) {
				holes.forEach((pos, color) ->
						Renderer.drawBoxOutline(pos, QuadColor.single(color[0], color[1], color[2], 255), getSetting(1).asToggle().getChild(1).asSlider().getValueFloat(), excludeDirs));
			}
		}

		if (getSetting(2).asToggle().getState()) {
			int sideMode = getSetting(2).asToggle().getChild(0).asMode().getMode();
			float height = getSetting(2).asToggle().getChild(3).asSlider().getValueFloat();
			int alpha = (int) (getSetting(2).asToggle().getChild(2).asSlider().getValueFloat() * 255);
			Direction[] excludeDirs = new Direction[] { Direction.UP, Direction.DOWN };

			if (sideMode == 0 || sideMode == 1) {
				CardinalDirection gradientDir = sideMode == 0 ? CardinalDirection.NORTH : CardinalDirection.SOUTH;

				holes.forEach((pos, color) ->
						Renderer.drawBoxFill(new Box(pos, pos.add(1, 0, 1)).stretch(0, height, 0),
								QuadColor.gradient(
										color[0], color[1], color[2], alpha,
										color[0], color[1], color[2], 0, gradientDir), excludeDirs));
			} else {
				if (sideMode == 2 || sideMode == 4) {
					holes.forEach((pos, color) ->
							Renderer.drawBoxFill(new Box(pos, pos.add(1, 0, 1)).stretch(0, height, 0),
									QuadColor.single(color[0], color[1], color[2], alpha), excludeDirs));
				}

				if (sideMode == 2 || sideMode == 3) {
					holes.forEach((pos, color) ->
							Renderer.drawBoxOutline(new Box(pos, pos.add(1, 0, 1)).stretch(0, height, 0),
									QuadColor.single(color[0], color[1], color[2], 255), getSetting(2).asToggle().getChild(1).asSlider().getValueFloat(), excludeDirs));
				}
			}
		}
	}

	private BlockPos[] neighbours(BlockPos pos) {
		return new BlockPos[] {
				pos.west(), pos.east(), pos.south(), pos.north(), pos.down()
		};
	}
}

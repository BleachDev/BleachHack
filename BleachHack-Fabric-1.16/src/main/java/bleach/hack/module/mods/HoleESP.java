/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingColor;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.render.RenderUtils;
import bleach.hack.util.render.color.QuadColor;
import bleach.hack.util.render.color.QuadColor.CardinalDirection;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * @author <a href="https://github.com/lasnikprogram">Lasnik</a>
 */
public class HoleESP extends Module {

	private Map<BlockPos, float[]> holes = new HashMap<>();

	public HoleESP() {
		super("HoleESP", KEY_UNBOUND, Category.RENDER, "Highlights save and not so save holes. Used for CrystalPvP",
				new SettingSlider("Radius", 1, 20, 10, 0).withDesc("Radius in which holes are getting searched"),
				new SettingToggle("RenderBottom", true).withDesc("Render the bottom of this hole").withChildren(
						new SettingMode("Render", "Box+Fill", "Box", "Fill").withDesc("The rendering method"),
						new SettingSlider("Box", 0.1, 4, 2, 1).withDesc("The thickness of the box lines"),
						new SettingSlider("Fill", 0, 1, 0.3, 2).withDesc("The opacity of the fill")),
				new SettingToggle("RenderSides", true).withDesc("Render the sides of this hole").withChildren(
						new SettingMode("Render", "GlowUp", "GlowDown", "Box+Fill", "Box", "Fill").withDesc("The rendering method"),
						new SettingSlider("Box", 0.1, 4, 2, 1).withDesc("The thickness of the box lines"),
						new SettingSlider("Fill", 0, 1, 0.3, 2).withDesc("The opacity of the fill/glow"),
						new SettingSlider("Height", 0.1, 8, 1, 1).withDesc("The height to render the sides")),
				new SettingToggle("Bedrock", true).withDesc("Shows holes with full bedrock").withChildren(
						new SettingColor("Color", 0f, 1f, 0f, false).withDesc("Color for bedrock holes")),
				new SettingToggle("Mixed", true).withDesc("Shows holes with a mix of obsidian and bedrock").withChildren(
						new SettingColor("Mixed", 1f, 1f, 0f, false).withDesc("Color for mixed holes")),
				new SettingToggle("Obsidian", true).withDesc("Shows holes with a mix of obsidian and bedrock").withChildren(
						new SettingColor("Obsidian", 1f, 0f, 0f, false).withDesc("Color for obsidian holes")),
				new SettingToggle("HideWhenIn", true).withDesc("Hides the hole you're currently in to prevent blocking out screen"));
	}

	@Override
	public void onDisable() {
		holes.clear();

		super.onDisable();
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (mc.player.age % 14 == 0) {
			holes.clear();

			int dist = getSetting(0).asSlider().getValueInt();

			for (BlockPos pos : BlockPos.iterateOutwards(mc.player.getBlockPos(), dist, dist, dist)) {
				if (!World.isInBuildLimit(pos.down())
						|| (mc.world.getBlockState(pos.down()).getBlock() != Blocks.BEDROCK
						&& mc.world.getBlockState(pos.down()).getBlock() != Blocks.OBSIDIAN)
						|| !mc.world.getBlockState(pos).getCollisionShape(mc.world, pos).isEmpty()
						|| !mc.world.getBlockState(pos.up(1)).getCollisionShape(mc.world, pos.up(1)).isEmpty()
						|| !mc.world.getBlockState(pos.up(2)).getCollisionShape(mc.world, pos.up(2)).isEmpty()) {
					continue;
				}

				if (getSetting(6).asToggle().state
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

				if (bedrockCounter == 5 && getSetting(3).asToggle().state) {
					holes.put(pos.toImmutable(), getSetting(3).asToggle().getChild(0).asColor().getRGBFloat());
				} else if (obsidianCounter == 5 && getSetting(5).asToggle().state) {
					holes.put(pos.toImmutable(), getSetting(5).asToggle().getChild(0).asColor().getRGBFloat());
				} else if (bedrockCounter >= 1 && obsidianCounter >= 1
						&& bedrockCounter + obsidianCounter == 5 && getSetting(4).asToggle().state) {
					holes.put(pos.toImmutable(), getSetting(4).asToggle().getChild(0).asColor().getRGBFloat());
				}
			}
		}
	}

	@Subscribe
	public void onRender(EventWorldRender.Post event) {
		if (getSetting(1).asToggle().state) {
			int bottomMode = getSetting(1).asToggle().getChild(0).asMode().mode;
			Direction[] excludeDirs = ArrayUtils.remove(Direction.values(), 0);

			if (bottomMode == 0 || bottomMode == 2) {
				holes.forEach((pos, color) -> {
					RenderUtils.drawBoxFill(pos, QuadColor.single(color[0], color[1], color[2], getSetting(1).asToggle().getChild(2).asSlider().getValueFloat()), excludeDirs);
				});
			}

			if (bottomMode == 0 || bottomMode == 1) {
				holes.forEach((pos, color) -> {
					RenderUtils.drawBoxOutline(pos, QuadColor.single(color[0], color[1], color[2], 1f), getSetting(1).asToggle().getChild(1).asSlider().getValueFloat(), excludeDirs);
				});
			}
		}

		if (getSetting(2).asToggle().state) {
			int sideMode = getSetting(2).asToggle().getChild(0).asMode().mode;
			float height = getSetting(2).asToggle().getChild(3).asSlider().getValueFloat();
			Direction[] excludeDirs = new Direction[] { Direction.UP, Direction.DOWN, };

			if (sideMode == 0 || sideMode == 1) {
				CardinalDirection gradientDir = sideMode == 0 ? CardinalDirection.NORTH : CardinalDirection.SOUTH;

				holes.forEach((pos, color) -> {
					RenderUtils.drawBoxFill(new Box(Vec3d.of(pos), Vec3d.of(pos).add(1, 0, 1)).stretch(0, height, 0),
							QuadColor.gradient(
									color[0], color[1], color[2], getSetting(2).asToggle().getChild(2).asSlider().getValueFloat(),
									color[0], color[1], color[2], 0f, gradientDir), excludeDirs);
				});
			} else {
				if (sideMode == 2 || sideMode == 4) {
					holes.forEach((pos, color) -> {
						RenderUtils.drawBoxFill(new Box(Vec3d.of(pos), Vec3d.of(pos).add(1, 0, 1)).stretch(0, height, 0),
								QuadColor.single(color[0], color[1], color[2], getSetting(2).asToggle().getChild(2).asSlider().getValueFloat()), excludeDirs);
					});
				}

				if (sideMode == 2 || sideMode == 3) {
					holes.forEach((pos, color) -> {
						RenderUtils.drawBoxOutline(new Box(Vec3d.of(pos), Vec3d.of(pos).add(1, 0, 1)).stretch(0, height, 0),
								QuadColor.single(color[0], color[1], color[2], 1f), getSetting(2).asToggle().getChild(1).asSlider().getValueFloat(), excludeDirs);
					});
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

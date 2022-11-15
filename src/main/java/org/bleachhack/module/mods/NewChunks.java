/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingColor;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.QuadColor;
import org.bleachhack.util.world.ChunkProcessor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NewChunks extends Module {

	private static final Direction[] SKIP_DIRS = new Direction[] { Direction.DOWN, Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH };
	private static final Direction[] SEARCH_DIRS = new Direction[] { Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.UP };
	
	private final Set<ChunkPos> newChunks = Collections.synchronizedSet(new HashSet<>());
	private final Set<ChunkPos> oldChunks = Collections.synchronizedSet(new HashSet<>());
	
	private ChunkProcessor processor = new ChunkProcessor(1,
			(cp, chunk) -> {
				if (!newChunks.contains(cp) && mc.world.getChunkManager().getChunk(cp.x, cp.z) == null) {
					for (int x = 0; x < 16; x++) {
						for (int y = mc.world.getBottomY(); y < mc.world.getTopY(); y++) {
							for (int z = 0; z < 16; z++) {
								FluidState fluid = chunk.getFluidState(x, y, z);
								
								if (!fluid.isEmpty() && !fluid.isStill()) {
									oldChunks.add(cp);
									return;
								}
							}
						}
					}
				}
			},
			null,
			(pos, state) -> {
				if (!state.getFluidState().isEmpty() && !state.getFluidState().isStill()) {
					ChunkPos cpos = new ChunkPos(pos);
					for (Direction dir: SEARCH_DIRS) {
						if (mc.world.getBlockState(pos.offset(dir)).getFluidState().isStill() && !oldChunks.contains(cpos)) {
							newChunks.add(cpos);
							return;
						}
					}
				}
			});

	public NewChunks() {
		super("NewChunks", KEY_UNBOUND, ModuleCategory.WORLD, "Detects completely new chunks using certain traits of them.",
				new SettingSlider("Y-Offset", -100, 100, 0, 0).withDesc("The offset from the bottom of the world to render the squares at."),
				new SettingToggle("Remove", true).withDesc("Removes the cached chunks when disabling the module."),
				new SettingToggle("Fill", true).withDesc("Fills in the newchunks.").withChildren(
						new SettingSlider("Opacity", 0.01, 1, 0.3, 2).withDesc("The opacity of the fill.")),
				new SettingToggle("NewChunks", true).withDesc("Shows all the chunks that are (most likely) completely new.").withChildren(
						new SettingColor("Color", 200, 150, 215).withDesc("The color of NewChunks.")),
				new SettingToggle("OldChunks", false).withDesc("Shows all the chunks that have (most likely) been loaded before.").withChildren(
						new SettingColor("Color", 230, 50, 50).withDesc("The color of OldChunks.")));
	}
	
	@Override
	public void onDisable(boolean inWorld) {
		if (getSetting(1).asToggle().getState()) {
			newChunks.clear();
			oldChunks.clear();
		}

		processor.stop();
		super.onDisable(inWorld);
	}

	@Override
	public void onEnable(boolean inWorld) {
		super.onEnable(inWorld);
		processor.start();
	}

	@BleachSubscribe
	public void onWorldRender(EventWorldRender.Post event) {
		int renderY = mc.world.getBottomY() + getSetting(0).asSlider().getValueInt();
		int opacity = (int) (getSetting(2).asToggle().getChild(0).asSlider().getValueFloat() * 255);

		if (getSetting(3).asToggle().getState()) {
			int[] color = getSetting(3).asToggle().getChild(0).asColor().getRGBArray();
			QuadColor outlineColor = QuadColor.single(color[0], color[1], color[2], 255);
			QuadColor fillColor = QuadColor.single(color[0], color[1], color[2], opacity);

			synchronized (newChunks) {
				for (ChunkPos c: newChunks) {
					if (mc.getCameraEntity().getBlockPos().isWithinDistance(c.getStartPos(), 1024)) {
						Box box = new Box(
								c.getStartX(), renderY, c.getStartZ(),
								c.getStartX() + 16, renderY, c.getStartZ() + 16);

						if (getSetting(2).asToggle().getState()) {
							Renderer.drawBoxFill(box, fillColor, SKIP_DIRS);
						}
	
						Renderer.drawBoxOutline(box, outlineColor, 2f, SKIP_DIRS);
					}
				}
			}
		}

		if (getSetting(4).asToggle().getState()) {
			int[] color = getSetting(4).asToggle().getChild(0).asColor().getRGBArray();
			QuadColor outlineColor = QuadColor.single(color[0], color[1], color[2], 255);
			QuadColor fillColor = QuadColor.single(color[0], color[1], color[2], opacity);

			synchronized (oldChunks) {
				for (ChunkPos c: oldChunks) {
					if (mc.getCameraEntity().getBlockPos().isWithinDistance(c.getStartPos(), 1024)) {
						Box box = new Box(
								c.getStartX(), renderY, c.getStartZ(),
								c.getStartX() + 16, renderY, c.getStartZ() + 16);

						if (getSetting(2).asToggle().getState()) {
							Renderer.drawBoxFill(box, fillColor, SKIP_DIRS);
						}
	
						Renderer.drawBoxOutline(box, outlineColor, 2f, SKIP_DIRS);
					}
				}
			}
		}
	}
}

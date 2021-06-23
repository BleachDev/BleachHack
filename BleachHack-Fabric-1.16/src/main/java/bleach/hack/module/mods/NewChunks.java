/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import bleach.hack.eventbus.BleachSubscribe;

import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingColor;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.render.RenderUtils;
import bleach.hack.util.render.color.QuadColor;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.WorldChunk;

public class NewChunks extends Module {

	private Set<ChunkPos> newChunks = Collections.synchronizedSet(new HashSet<>());
	private Set<ChunkPos> oldChunks = Collections.synchronizedSet(new HashSet<>());

	public NewChunks() {
		super("NewChunks", KEY_UNBOUND, ModuleCategory.WORLD, "Detects completely new chunks using certain traits of them",
				new SettingToggle("Remove", true).withDesc("Removes the cached chunks when disabling the module"),
				new SettingToggle("Fill", true).withDesc("Fills in the newchunks").withChildren(
						new SettingSlider("Opacity", 0.01, 1, 0.3, 2).withDesc("The opacity of the fill")),
				new SettingToggle("NewChunks", true).withDesc("Shows all the chunks that are (most likely) completely new").withChildren(
						new SettingColor("Color", 0.8f, 0.6f, 0.85f, false)),
				new SettingToggle("OldChunks", false).withDesc("Shows all the chunks that have (most likely) been loaded before").withChildren(
						new SettingColor("Color", 0.9f, 0.2f, 0.2f, false)));
	}

	@Override
	public void onDisable() {
		if (getSetting(0).asToggle().state) {
			newChunks.clear();
			oldChunks.clear();
		}

		super.onDisable();
	}

	@BleachSubscribe
	public void onReadPacket(EventReadPacket event) {
		Direction[] searchDirs = new Direction[] { Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.UP };

		if (event.getPacket() instanceof ChunkDeltaUpdateS2CPacket) {
			ChunkDeltaUpdateS2CPacket packet = (ChunkDeltaUpdateS2CPacket) event.getPacket();

			packet.visitUpdates((pos, state) -> {
				if (!state.getFluidState().isEmpty() && !state.getFluidState().isStill()) {
					ChunkPos chunkPos = new ChunkPos(pos);

					for (Direction dir: searchDirs) {
						if (mc.world.getBlockState(pos.offset(dir)).getFluidState().isStill() && !oldChunks.contains(chunkPos)) {
							newChunks.add(chunkPos);
							return;
						}
					}
				}
			});
		} else if (event.getPacket() instanceof BlockUpdateS2CPacket) {
			BlockUpdateS2CPacket packet = (BlockUpdateS2CPacket) event.getPacket();

			if (!packet.getState().getFluidState().isEmpty() && !packet.getState().getFluidState().isStill()) {
				ChunkPos chunkPos = new ChunkPos(packet.getPos());

				for (Direction dir: searchDirs) {
					if (mc.world.getBlockState(packet.getPos().offset(dir)).getFluidState().isStill() && !oldChunks.contains(chunkPos)) {
						newChunks.add(chunkPos);
						return;
					}
				}
			}
		} else if (event.getPacket() instanceof ChunkDataS2CPacket && mc.world != null) {
			ChunkDataS2CPacket packet = (ChunkDataS2CPacket) event.getPacket();

			ChunkPos pos = new ChunkPos(packet.getX(), packet.getZ());
			
			if (!newChunks.contains(pos) && mc.world.getChunkManager().getChunk(packet.getX(), packet.getZ()) == null) {
				WorldChunk chunk = new WorldChunk(mc.world, pos, null);
				chunk.loadFromPacket(null, packet.getReadBuffer(), new NbtCompound(), packet.getVerticalStripBitmask());
				
				for (int x = 0; x < 16; x++) {
					for (int y = 0; y < mc.world.getHeight(); y++) {
						for (int z = 0; z < 16; z++) {
							FluidState fluid = chunk.getFluidState(x, y, z);
							
							if (!fluid.isEmpty() && !fluid.isStill()) {
								oldChunks.add(pos);
								return;
							}
						}
					}
				}
			}
		}
	}

	@BleachSubscribe
	public void onWorldRender(EventWorldRender.Post event) {
		Direction[] skipDirs = new Direction[] { Direction.DOWN, Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH };

		if (getSetting(2).asToggle().state) {
			int color = getSetting(2).asToggle().getChild(0).asColor().getRGB();
			QuadColor outlineColor = QuadColor.single(0xff000000 | color);
			QuadColor fillColor = QuadColor.single(((int) (getSetting(1).asToggle().getChild(0).asSlider().getValueFloat() * 255) << 24) | color);

			synchronized (newChunks) {
				for (ChunkPos c: newChunks) {
					if (mc.getCameraEntity().getBlockPos().isWithinDistance(c.getStartPos(), 1024)) {
						Box box = new Box(c.getStartX(), 0, c.getStartZ(), c.getStartX() + 16, 0, c.getStartZ() + 16);

						if (getSetting(1).asToggle().state) {
							RenderUtils.drawBoxFill(box, fillColor, skipDirs);
						}
	
						RenderUtils.drawBoxOutline(box, outlineColor, 2f, skipDirs);
					}
				}
			}
		}

		if (getSetting(3).asToggle().state) {
			int color = getSetting(3).asToggle().getChild(0).asColor().getRGB();
			QuadColor outlineColor = QuadColor.single(0xff000000 | color);
			QuadColor fillColor = QuadColor.single(((int) (getSetting(1).asToggle().getChild(0).asSlider().getValueFloat() * 255) << 24) | color);

			synchronized (oldChunks) {
				for (ChunkPos c: oldChunks) {
					if (mc.getCameraEntity().getBlockPos().isWithinDistance(c.getStartPos(), 1024)) {
						Box box = new Box(c.getStartX(), 0, c.getStartZ(), c.getStartX() + 16, 0, c.getStartZ() + 16);

						if (getSetting(1).asToggle().state) {
							RenderUtils.drawBoxFill(box, fillColor, skipDirs);
						}
	
						RenderUtils.drawBoxOutline(box, outlineColor, 2f, skipDirs);
					}
				}
			}
		}
	}
}

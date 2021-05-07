/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import java.util.HashSet;
import java.util.Set;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingColor;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.render.RenderUtils;
import bleach.hack.util.render.color.QuadColor;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;

public class NewChunks extends Module {

	private Set<ChunkPos> chunks = new HashSet<>();

	public NewChunks() {
		super("NewChunks", KEY_UNBOUND, Category.WORLD, "Detects completely new chunks using certain traits of them",
				new SettingToggle("Remove", true).withDesc("Removes the newchunks when disabling the module"),
				new SettingToggle("Fill", true).withDesc("Fills in the newchunks").withChildren(
						new SettingSlider("Opacity", 0.01, 1, 0.3, 2).withDesc("The opacity of the fill")),
				new SettingColor("Color", 0.8f, 0.6f, 0.85f, false));
	}

	@Override
	public void onDisable() {
		if (getSetting(0).asToggle().state) {
			chunks.clear();
		}

		super.onDisable();
	}

	@Subscribe
	public void onReadPacket(EventReadPacket event) {
		Direction[] searchDirs = new Direction[] { Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.UP };

		if (event.getPacket() instanceof ChunkDeltaUpdateS2CPacket) {
			ChunkDeltaUpdateS2CPacket packet = (ChunkDeltaUpdateS2CPacket) event.getPacket();

			synchronized (chunks) {
				packet.visitUpdates((pos, state) -> {
					if (!state.getFluidState().isEmpty() && !state.getFluidState().isStill()) {
						for (Direction dir: searchDirs) {
							if (mc.world.getBlockState(pos.offset(dir)).getFluidState().isStill()) {
								chunks.add(new ChunkPos(pos));
								return;
							}
						}
					}
				});
			}
		} else if (event.getPacket() instanceof BlockUpdateS2CPacket) {
			BlockUpdateS2CPacket packet = (BlockUpdateS2CPacket) event.getPacket();

			synchronized (chunks) {
				if (!packet.getState().getFluidState().isEmpty() && !packet.getState().getFluidState().isStill()) {
					for (Direction dir: searchDirs) {
						if (mc.world.getBlockState(packet.getPos().offset(dir)).getFluidState().isStill()) {
							chunks.add(new ChunkPos(packet.getPos()));
							return;
						}
					}
				}
			}
		}
	}

	@Subscribe
	public void onWorldRender(EventWorldRender.Post event) {
		QuadColor outlineColor = QuadColor.single(0xff000000 | getSetting(2).asColor().getRGB());
		QuadColor fillColor = QuadColor.single(
				((int) (getSetting(1).asToggle().getChild(0).asSlider().getValueFloat() * 255) << 24) | getSetting(2).asColor().getRGB());

		synchronized (chunks) {
			for (ChunkPos c: chunks) {
				if (mc.getCameraEntity().getBlockPos().isWithinDistance(c.getStartPos(), 1024)) {
					if (getSetting(1).asToggle().state) {
						RenderUtils.drawBoxFill(new Box(c.getStartPos(), c.getStartPos().add(16, 0, 16)), fillColor);
					}
		
					RenderUtils.drawBoxOutline(new Box(c.getStartPos(), c.getStartPos().add(16, 0, 16)), outlineColor, 2f);
				}
			}
		}
	}
}

/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.bleachhack.event.events.EventPacket;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingBlockList;
import org.bleachhack.setting.module.SettingList;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.io.BleachFileMang;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.LineColor;
import org.bleachhack.util.render.color.QuadColor;
import org.bleachhack.util.world.ChunkProcessor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="https://github.com/lasnikprogram">Lasnik</a>
 */

public class Search extends Module {

	private String logFile;
	private Set<BlockPos> foundBlocks = Sets.newConcurrentHashSet();

	private ChunkProcessor processor = new ChunkProcessor(1,
			(cp, chunk) -> {
				if (foundBlocks.size() > 1000000)
					return;

				boolean log = getSetting(4).asToggle().getState();
				StringBuilder logBuilder = new StringBuilder();

				SettingList<Block> list = getSetting(5).asList(Block.class);
				for (int x = 0; x < 16; x++) {
					for (int y = mc.world.getBottomY(); y < mc.world.getTopY(); y++) {
						for (int z = 0; z < 16; z++) {
							BlockPos pos = new BlockPos(cp.getStartX() + x, y, cp.getStartZ() + z);
							BlockState state = chunk.getBlockState(pos);
							if (list.contains(state.getBlock())) {
								foundBlocks.add(pos);
								if (log) {
									logBuilder
									.append('[')
									.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
									.append("] {")
									.append(pos.toShortString()).append("} ")
									.append(state);

									BlockEntity be = mc.world.getBlockEntity(pos);
									if (be != null) {
										logBuilder
										.append(" BlockEntity")
										.append(be.createNbt().asString());
									}

									logBuilder.append('\n');
								}
							}
						}
					}
				}

				if (logBuilder.length() != 0) {
					BleachFileMang.createFile("search/" + logFile);
					BleachFileMang.appendFile("search/" + logFile, logBuilder.toString());
				}
			},
			(cp, chunk) ->
				foundBlocks.removeIf(pos
						-> pos.getX() >= cp.getStartX()
						&& pos.getX() <= cp.getEndX()
						&& pos.getZ() >= cp.getStartZ()
						&& pos.getZ() <= cp.getEndZ()),
			(pos, state) -> {
				if (getSetting(5).asList(Block.class).contains(state.getBlock())) {
					foundBlocks.add(pos);
				} else {
					foundBlocks.remove(pos);
				}
			});

	private Set<Block> prevBlockList = new HashSet<>();
	private int oldViewDistance = -1;

	public Search() {
		super("Search", KEY_UNBOUND, ModuleCategory.RENDER, "Highlights certain blocks.",
				new SettingMode("Render", "Box+Fill", "Box", "Fill").withDesc("The rendering method."),
				new SettingSlider("Box", 0.1, 4, 2, 1).withDesc("The thickness of the box lines."),
				new SettingSlider("Fill", 0, 1, 0.3, 2).withDesc("The opacity of the fill."),
				new SettingToggle("Tracers", false).withDesc("Renders a line from the player to all found blocks.").withChildren(
						new SettingSlider("Width", 0.1, 5, 1.5, 1).withDesc("Thickness of the tracers."),
						new SettingSlider("Opacity", 0, 1, 0.75, 2).withDesc("Opacity of the tracers.")),
				new SettingToggle("LogBlocks", false).withDesc("Saves all the found blocks in .minecraft/bleach/search/"),
				new SettingBlockList("Edit Blocks", "Edit Search Blocks",
						Blocks.DIAMOND_ORE,
						Blocks.EMERALD_ORE,
						Blocks.DIAMOND_BLOCK,
						Blocks.EMERALD_BLOCK,
						Blocks.ANCIENT_DEBRIS).withDesc("Edit the Search blocks."));
	}

	@Override
	public void onDisable(boolean inWorld) {
		foundBlocks.clear();
		prevBlockList.clear();
		processor.stop();

		super.onDisable(inWorld);
	}

	@Override
	public void onEnable(boolean inWorld) {
		super.onEnable(inWorld);

		processor.start();
		logFile = "Search-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".log";
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		Set<Block> blockList = getSetting(5).asList(Block.class).getValue();

		if (!prevBlockList.equals(blockList) || oldViewDistance != mc.options.getViewDistance().getValue()) {
			foundBlocks.clear();

			processor.submitAllLoadedChunks();

			prevBlockList = new HashSet<>(blockList);
			oldViewDistance = mc.options.getViewDistance().getValue();
		}
	}

	@BleachSubscribe
	public void onReadPacket(EventPacket.Read event) {
		if (event.getPacket() instanceof DisconnectS2CPacket
				|| event.getPacket() instanceof GameJoinS2CPacket
				|| event.getPacket() instanceof PlayerRespawnS2CPacket) {
			foundBlocks.clear();
			prevBlockList.clear();
			processor.restartExecutor();
		}
	}

	@BleachSubscribe
	public void onRender(EventWorldRender.Post event) {
		int mode = getSetting(0).asMode().getMode();

		int i = 0;
		for (BlockPos pos : foundBlocks) {
			if (i > 3000)
				return;

			BlockState state = mc.world.getBlockState(pos);

			int[] color = getColorForBlock(state, pos);

			VoxelShape voxelShape = state.getOutlineShape(mc.world, pos);
			if (voxelShape.isEmpty()) {
				voxelShape = VoxelShapes.cuboid(0, 0, 0, 1, 1, 1);
			}

			if (mode == 0 || mode == 2) {
				int fillAlpha = (int) (getSetting(2).asSlider().getValue() * 255);

				for (Box box: voxelShape.getBoundingBoxes()) {
					Renderer.drawBoxFill(box.offset(pos), QuadColor.single(color[0], color[1], color[2], fillAlpha));
				}
			}

			if (mode == 0 || mode == 1) {
				float outlineWidth = getSetting(1).asSlider().getValueFloat();

				for (Box box: voxelShape.getBoundingBoxes()) {
					Renderer.drawBoxOutline(box.offset(pos), QuadColor.single(color[0], color[1], color[2], 255), outlineWidth);
				}
			}

			SettingToggle tracers = getSetting(3).asToggle();
			if (tracers.getState()) {
				// This is bad when bobbing is enabled!
				Vec3d lookVec = new Vec3d(0, 0, 75)
						.rotateX(-(float) Math.toRadians(mc.gameRenderer.getCamera().getPitch()))
						.rotateY(-(float) Math.toRadians(mc.gameRenderer.getCamera().getYaw()))
						.add(mc.cameraEntity.getEyePos());

				Renderer.drawLine(
						lookVec.x, lookVec.y, lookVec.z,
						pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						LineColor.single(color[0], color[1], color[2], (int) (tracers.getChild(1).asSlider().getValue() * 255)),
						tracers.getChild(0).asSlider().getValueFloat());
			}

			i++;
		}
	}

	public int[] getColorForBlock(BlockState state, BlockPos pos) {
		if (state.getBlock() == Blocks.NETHER_PORTAL) {
			return new int[] { 107, 0, 209 };
		}

		int color = state.getMapColor(mc.world, pos).color;
		return new int[] { (color & 0xff0000) >> 16, (color & 0xff00) >> 8, color & 0xff };
	}
}

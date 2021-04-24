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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingColor;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.setting.other.SettingLists;
import bleach.hack.setting.other.SettingRotate;
import bleach.hack.util.Boxes;
import bleach.hack.util.FabricReflect;
import bleach.hack.util.render.RenderUtils;
import bleach.hack.util.render.color.QuadColor;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class Nuker extends Module {

	private Set<BlockPos> renderBlocks = new HashSet<>();

	public Nuker() {
		super("Nuker", KEY_UNBOUND, Category.WORLD, "Breaks blocks around you",
				new SettingMode("Mode", "Normal", "SurvMulti", "Multi", "Instant").withDesc("Mining mode"),
				new SettingSlider("Multi", 1, 10, 2, 0).withDesc("How many blocks to mine at once if Multi/SurvMulti mode is on"),
				new SettingSlider("Range", 1, 6, 4.2, 1).withDesc("Mining range"),
				new SettingSlider("Cooldown", 0, 4, 0, 0).withDesc("Cooldown between mining blocks"),
				new SettingMode("Sort", "Closest", "Furthest", "Hardness", "None").withDesc("Which order to mine blocks in"),
				new SettingToggle("Filter", false).withDesc("Filters certain blocks").withChildren(
						new SettingMode("Mode", "Blacklist", "Whitelist").withDesc("How to handle the list"),
						SettingLists.newBlockList("Edit Blocks", "Edit Filtered Blocks").withDesc("Edit the filtered blocks")),
				new SettingToggle("Flatten", false).withDesc("Flatten the area around you"),
				new SettingRotate(false),
				new SettingToggle("NoParticles", false).withDesc("Removes block breaking paritcles"),
				new SettingToggle("Highlight", false).withDesc("Highlights the blocks you are currently mining").withChildren(
						new SettingMode("Mode", "Opacity", "Expand").withDesc("How to show the mining progress"),
						new SettingColor("Color", 1f, 0.5f, 0.5f, false).withDesc("The color of the highlight")));
	}

	@Override
	public void onDisable() {
		renderBlocks.clear();

		super.onDisable();
	}

	@Subscribe
	public void onTick(EventTick event) {
		renderBlocks.clear();

		double range = getSetting(2).asSlider().getValue();

		Map<BlockPos, Pair<Vec3d, Direction>> blocks = new LinkedHashMap<>();

		/* Add blocks around player */
		for (int x = (int) range; x >= (int) -range; x--) {
			for (int y = (int) range; y >= (getSetting(6).asToggle().state ? 0 : (int) -range); y--) {
				for (int z = (int) range; z >= (int) -range; z--) {
					BlockPos pos = new BlockPos(mc.player.getPos().add(x, y + 0.2, z));

					if (mc.world.getBlockState(pos).getBlock() != Blocks.AIR && !WorldUtils.isFluid(pos)) {
						Pair<Vec3d, Direction> vec = getBlockAngle(pos);

						if (vec != null) {
							blocks.put(pos, vec);
						}
					}
				}
			}
		}

		if (blocks.isEmpty())
			return;

		if (getSetting(4).asMode().mode != 3) {
			Vec3d eyePos = new Vec3d(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());

			blocks = blocks.entrySet().stream()
					.sorted((a, b) -> getSetting(4).asMode().mode <= 1 ?
							Double.compare(
									eyePos.distanceTo((getSetting(4).asMode().mode == 0 ? a : b).getValue().getLeft()),
									eyePos.distanceTo((getSetting(4).asMode().mode == 0 ? b : a).getValue().getLeft()))
							: Float.compare(
									mc.world.getBlockState(a.getKey()).getHardness(mc.world, a.getKey()),
									mc.world.getBlockState(b.getKey()).getHardness(mc.world, b.getKey())))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
		}

		/* Move the block under the player to last so it doesn't mine itself down
		 * without clearing everything above first */
		if (blocks.containsKey(mc.player.getBlockPos().down())) {
			Pair<Vec3d, Direction> v = blocks.get(mc.player.getBlockPos().down());
			blocks.remove(mc.player.getBlockPos().down());
			blocks.put(mc.player.getBlockPos().down(), v);
		}

		int broken = 0;
		for (Entry<BlockPos, Pair<Vec3d, Direction>> pos : blocks.entrySet()) {
			if (getSetting(5).asToggle().state) {
				boolean contains = getSetting(5).asToggle().getChild(1).asList(Block.class).contains(mc.world.getBlockState(pos.getKey()).getBlock());

				if ((getSetting(5).asToggle().getChild(0).asMode().mode == 0 && contains)
						|| (getSetting(5).asToggle().getChild(0).asMode().mode == 1 && !contains)) {
					continue;
				}
			}

			float hardness = mc.world.getBlockState(pos.getKey()).calcBlockBreakingDelta(mc.player, mc.world, pos.getKey());

			if (getSetting(0).asMode().mode == 1 && hardness <= 1f && broken > 0) {
				return;
			}

			if (getSetting(7).asRotate().state) {
				Vec3d v = pos.getValue().getLeft();
				WorldUtils.facePosAuto(v.x, v.y, v.z, getSetting(7).asRotate());
			}

			mc.interactionManager.updateBlockBreakingProgress(pos.getKey(), pos.getValue().getRight());
			renderBlocks.add(pos.getKey());

			mc.player.swingHand(Hand.MAIN_HAND);

			broken++;
			if (getSetting(0).asMode().mode == 0
					|| (getSetting(0).asMode().mode == 1 && hardness <= 1f)
					|| (getSetting(0).asMode().mode == 1 && broken >= getSetting(1).asSlider().getValueInt())
					|| (getSetting(0).asMode().mode == 2 && broken >= getSetting(1).asSlider().getValueInt())) {
				return;
			}
		}
	}

	@Subscribe
	public void onWorldRender(EventWorldRender.Post event) {
		if (getSetting(9).asToggle().state) {
			float[] color = getSetting(9).asToggle().getChild(1).asColor().getRGBFloat();

			float breakingProgress = (float) FabricReflect.getFieldValue(mc.interactionManager, "field_3715", "currentBreakingProgress");
			
			for (BlockPos pos: renderBlocks) {
				VoxelShape shape = mc.world.getBlockState(pos).getOutlineShape(mc.world, pos);

				if (!shape.isEmpty()) {
					if (getSetting(9).asToggle().getChild(0).asMode().mode == 0) {
						RenderUtils.drawBoxBoth(shape.getBoundingBox().offset(pos),
								QuadColor.single(color[0], color[1], color[2], breakingProgress * 0.8f), 2.5f);
					} else {
						RenderUtils.drawBoxBoth(Boxes.multiply(shape.getBoundingBox().offset(pos), breakingProgress),
								QuadColor.single(color[0], color[1], color[2], 0.5f), 2.5f);
					}
				}
			}
		}
	}

	public Pair<Vec3d, Direction> getBlockAngle(BlockPos pos) {
		for (Direction d: Direction.values()) {
			if (!mc.world.getBlockState(pos.offset(d)).isFullCube(mc.world, pos)) {
				Vec3d vec = WorldUtils.getLegitLookPos(pos, d, true, 5);

				if (vec != null) {
					return Pair.of(vec, d);
				}
			}
		}

		return null;
	}

}

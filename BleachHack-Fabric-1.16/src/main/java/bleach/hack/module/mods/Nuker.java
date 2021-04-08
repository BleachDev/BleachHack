/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2019 Bleach.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.module.mods;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.setting.other.SettingLists;
import bleach.hack.setting.other.SettingRotate;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Nuker extends Module {

	public Nuker() {
		super("Nuker", KEY_UNBOUND, Category.WORLD, "Breaks blocks around you",
				new SettingMode("Mode", "Normal", "SurvMulti", "Multi", "Instant").withDesc("Mining mode"),
				new SettingSlider("Multi", 1, 10, 2, 0).withDesc("How many blocks to mine at once if Multi/SurvMulti mode is on"),
				new SettingSlider("Range", 1, 6, 4.2, 1).withDesc("Mining range"),
				new SettingSlider("Cooldown", 0, 4, 0, 0).withDesc("Cooldown between mining blocks"),
				new SettingToggle("Filter", false).withDesc("Filters certain blocks").withChildren(
						new SettingMode("Mode", "Blacklist", "Whitelist").withDesc("How to handle the list"),
						SettingLists.newBlockList("Edit Blocks", "Edit Filtered Blocks").withDesc("Edit the filtered blocks")),
				new SettingToggle("Flatten", false).withDesc("Flatten the area around you"),
				new SettingRotate(false),
				new SettingToggle("NoParticles", false).withDesc("Removes block breaking paritcles"),
				new SettingMode("Sort", "Closest", "Furthest", "Hardness", "None").withDesc("Which order to mine blocks in"));
	}

	@Subscribe
	public void onTick(EventTick event) {
		double range = getSetting(2).asSlider().getValue();

		LinkedHashMap<BlockPos, Pair<Vec3d, Direction>> blocks = new LinkedHashMap<>();

		/* Add blocks around player */
		for (int x = (int) range; x >= (int) -range; x--) {
			for (int y = (int) range; y >= (getSetting(5).asToggle().state ? 0 : (int) -range); y--) {
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

		if (getSetting(8).asMode().mode != 3) {
			Vec3d eyePos = new Vec3d(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());

			blocks = blocks.entrySet().stream()
					.sorted((a, b) -> getSetting(8).asMode().mode <= 1 ?
							Double.compare(
									eyePos.distanceTo((getSetting(8).asMode().mode == 0 ? a : b).getValue().getLeft()),
									eyePos.distanceTo((getSetting(8).asMode().mode == 0 ? b : a).getValue().getLeft()))
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
			if (getSetting(4).asToggle().state) {
				boolean contains = getSetting(4).asToggle().getChild(1).asList(Block.class).contains(mc.world.getBlockState(pos.getKey()).getBlock());

				if ((getSetting(4).asToggle().getChild(0).asMode().mode == 0 && contains)
						|| (getSetting(4).asToggle().getChild(0).asMode().mode == 1 && !contains)) {
					continue;
				}
			}

			float hardness = mc.world.getBlockState(pos.getKey()).calcBlockBreakingDelta(mc.player, mc.world, pos.getKey());

			if (getSetting(0).asMode().mode == 1 && hardness <= 1f && broken > 0) {
				return;
			}

			if (getSetting(6).asRotate().state) {
				Vec3d v = pos.getValue().getLeft();
				WorldUtils.facePosAuto(v.x, v.y, v.z, getSetting(6).asRotate());
			}

			mc.interactionManager.updateBlockBreakingProgress(pos.getKey(), pos.getValue().getRight());

			mc.player.swingHand(Hand.MAIN_HAND);

			broken++;
			if (getSetting(0).asMode().mode == 0
					|| (getSetting(0).asMode().mode == 1 && hardness <= 1f)
					|| (getSetting(0).asMode().mode == 1 && broken >= (int) getSetting(1).asSlider().getValue())
					|| (getSetting(0).asMode().mode == 2 && broken >= (int) getSetting(1).asSlider().getValue())) {
				return;
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

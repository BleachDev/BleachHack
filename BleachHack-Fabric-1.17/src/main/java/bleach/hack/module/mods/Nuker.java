/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import bleach.hack.eventbus.BleachSubscribe;

import bleach.hack.event.events.EventBlockBreakCooldown;
import bleach.hack.event.events.EventParticle;
import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingColor;
import bleach.hack.module.setting.base.SettingMode;
import bleach.hack.module.setting.base.SettingSlider;
import bleach.hack.module.setting.base.SettingToggle;
import bleach.hack.module.setting.other.SettingBlockList;
import bleach.hack.module.setting.other.SettingRotate;
import bleach.hack.module.Module;
import bleach.hack.util.Boxes;
import bleach.hack.util.FabricReflect;
import bleach.hack.util.collections.ImmutablePairList;
import bleach.hack.util.render.Renderer;
import bleach.hack.util.render.color.LineColor;
import bleach.hack.util.render.color.QuadColor;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class Nuker extends Module {

	private Set<BlockPos> renderBlocks = new HashSet<>();

	public Nuker() {
		super("Nuker", KEY_UNBOUND, ModuleCategory.WORLD, "Breaks blocks around you.",
				new SettingMode("Mode", "Normal", "SurvMulti", "Multi", "Instant").withDesc("Mining mode."),
				new SettingSlider("Multi", 1, 10, 2, 0).withDesc("How many blocks to mine at once if Multi/SurvMulti mode is selected."),
				new SettingSlider("Cooldown", 0, 4, 0, 0).withDesc("Cooldown between mining blocks (in ticks)."),
				new SettingMode("Shape", "Cube", "Sphere").withDesc("What shape to mine in."),
				new SettingSlider("Range", 1, 6, 4.2, 1).withDesc("Mining range."),
				new SettingMode("Sort", "Closest", "Furthest", "Softest", "Hardest", "None").withDesc("Which order to mine blocks in."),
				new SettingToggle("Filter", false).withDesc("Filters certain blocks.").withChildren(
						new SettingMode("Mode", "Blacklist", "Whitelist").withDesc("How to handle the list."),
						new SettingBlockList("Edit Blocks", "Edit Filtered Blocks").withDesc("Edit the filtered blocks.")),
				new SettingToggle("Raycast", true).withDesc("Only mines blocks you can see."),
				new SettingToggle("Flatten", false).withDesc("Flatten the area around you."),
				new SettingRotate(false).withDesc("Rotate to blocks that you're mining."),
				new SettingToggle("NoParticles", false).withDesc("Removes block breaking particles."),
				new SettingToggle("Highlight", false).withDesc("Highlights the blocks you're currently mining.").withChildren(
						new SettingMode("Mode", "Opacity", "Expand").withDesc("How to show the mining progress."),
						new SettingColor("Color", 1f, 0.5f, 0.5f, false).withDesc("The color of the highlight.")),
				new SettingToggle("RangeHighlight", false).withDesc("Highlights the range you can mine.").withChildren(
						new SettingSlider("Width", 0.1, 10, 3, 1).withDesc("The width of the lines."),
						new SettingColor("Color", 1f, 0f, 0f, false).withDesc("The color of the highlight.")));
	}

	@Override
	public void onDisable(boolean inWorld) {
		renderBlocks.clear();

		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		renderBlocks.clear();

		double range = getSetting(4).asSlider().getValue();

		ImmutablePairList<BlockPos, Pair<Vec3d, Direction>> blocks = new ImmutablePairList<>();

		// Add blocks around player
		SettingToggle filterToggler = getSetting(6).asToggle();
		for (int x = MathHelper.ceil(range); x >= MathHelper.floor(-range); x--) {
			for (int y = MathHelper.ceil(range); y >= (getSetting(8).asToggle().state ? -mc.player.getEyeHeight(mc.player.getPose()) + 0.2 : MathHelper.floor(-range)); y--) {
				for (int z = MathHelper.ceil(range); z >= MathHelper.floor(-range); z--) {
					BlockPos pos = new BlockPos(mc.player.getEyePos().add(x, y, z));

					double distTo = getSetting(3).asMode().mode == 0
							? MathHelper.absMax(MathHelper.absMax(mc.player.getX() - (pos.getX() + 0.5), mc.player.getEyeY() - (pos.getY() + 0.5)), mc.player.getZ() - (pos.getZ() + 0.5))
									: mc.player.getPos().distanceTo(Vec3d.ofCenter(pos));

					BlockState state = mc.world.getBlockState(pos);
					if (distTo - 0.5 > getSetting(4).asSlider().getValue() || state.isAir() || state.getBlock() instanceof FluidBlock)
						continue;

					if (filterToggler.state) {
						boolean contains = filterToggler.getChild(1).asList(Block.class).contains(state.getBlock());

						if ((filterToggler.getChild(0).asMode().mode == 0 && contains)
								|| (filterToggler.getChild(0).asMode().mode == 1 && !contains)) {
							continue;
						}
					}

					Pair<Vec3d, Direction> vec = getBlockAngle(pos);

					if (vec != null) {
						blocks.add(pos, vec);
					} else if (!getSetting(7).asToggle().state) {
						blocks.add(pos, Pair.of(Vec3d.ofCenter(pos), Direction.UP));
					}
				}
			}
		}

		if (blocks.isEmpty())
			return;

		blocks.sortByKey(getBlockOrderComparator());

		int broken = 0;
		for (ImmutablePair<BlockPos, Pair<Vec3d, Direction>> pos : blocks) {
			float breakingDelta = mc.world.getBlockState(pos.getKey()).calcBlockBreakingDelta(mc.player, mc.world, pos.getKey());

			// Unbreakable block
			if (mc.interactionManager.getCurrentGameMode().isSurvivalLike() && breakingDelta == 0) {
				continue;
			}

			if (getSetting(0).asMode().mode == 1 && breakingDelta <= 1f && broken > 0) {
				return;
			}

			if (getSetting(9).asRotate().state) {
				Vec3d v = pos.getValue().getLeft();
				WorldUtils.facePosAuto(v.x, v.y, v.z, getSetting(9).asRotate());
			}

			mc.interactionManager.updateBlockBreakingProgress(pos.getKey(), pos.getValue().getRight());
			renderBlocks.add(pos.getKey());

			mc.player.swingHand(Hand.MAIN_HAND);

			broken++;
			if (getSetting(0).asMode().mode == 0
					|| (getSetting(0).asMode().mode == 1 && breakingDelta <= 1f)
					|| (getSetting(0).asMode().mode == 1 && broken >= getSetting(1).asSlider().getValueInt())
					|| (getSetting(0).asMode().mode == 2 && broken >= getSetting(1).asSlider().getValueInt())) {
				return;
			}
		}
	}

	@BleachSubscribe
	public void onWorldRender(EventWorldRender.Post event) {
		if (getSetting(11).asToggle().state) {
			float[] color = getSetting(11).asToggle().getChild(1).asColor().getRGBFloat();

			float breakingProgress = (float) FabricReflect.getFieldValue(mc.interactionManager, "field_3715", "currentBreakingProgress");

			for (BlockPos pos: renderBlocks) {
				VoxelShape shape = mc.world.getBlockState(pos).getOutlineShape(mc.world, pos);

				if (!shape.isEmpty()) {
					if (getSetting(11).asToggle().getChild(0).asMode().mode == 0) {
						Renderer.drawBoxBoth(shape.getBoundingBox().offset(pos),
								QuadColor.single(color[0], color[1], color[2], breakingProgress * 0.8f), 2.5f);
					} else {
						Renderer.drawBoxBoth(Boxes.multiply(shape.getBoundingBox().offset(pos), breakingProgress),
								QuadColor.single(color[0], color[1], color[2], 0.5f), 2.5f);
					}
				}
			}
		}

		if (getSetting(12).asToggle().state) {
			Vec3d pos = mc.player.getPos().subtract(Renderer.getInterpolationOffset(mc.player));
			double range = getSetting(4).asSlider().getValue();
			int color = 0xff000000 | getSetting(12).asToggle().getChild(1).asColor().getRGB();
			float width = getSetting(12).asToggle().getChild(0).asSlider().getValueFloat();

			if (getSetting(3).asMode().mode == 0) {
				Renderer.drawBoxOutline(new Box(pos, pos).expand(range, 0, range), QuadColor.single(color), width,
						Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST, Direction.DOWN);
			} else {
				double lastX = 0;
				double lastZ = range;
				for (int angle = 0; angle <= 360; angle += 6) {
					float cos = MathHelper.cos((float) Math.toRadians(angle));
					float sin = MathHelper.sin((float) Math.toRadians(angle));

					double x = range * sin;
					double z = range * cos;
					Renderer.drawLine(
							pos.x + lastX, pos.y, pos.z + lastZ,
							pos.x + x, pos.y, pos.z + z,
							LineColor.single(color), width);

					lastX = x;
					lastZ = z;
				}
			}
		}
	}

	@BleachSubscribe
	public void onParticle(EventParticle.Normal event) {
		if (event.getParticle() instanceof BlockDustParticle && getSetting(10).asToggle().state) {
			event.setCancelled(true);
		}
	}

	@BleachSubscribe
	public void onBlockBreakCooldown(EventBlockBreakCooldown event) {
		event.setCooldown(getSetting(2).asSlider().getValueInt());
	}

	private Pair<Vec3d, Direction> getBlockAngle(BlockPos pos) {
		for (Direction d: Direction.values()) {
			if (!mc.world.getBlockState(pos.offset(d)).isFullCube(mc.world, pos.offset(d))) {
				Vec3d vec = WorldUtils.getLegitLookPos(pos, d, true, 5);

				if (vec != null) {
					return Pair.of(vec, d);
				}
			}
		}

		return null;
	}

	private Comparator<BlockPos> getBlockOrderComparator() {
		// Comparator that moves the block under the player to last
		// so it doesn't mine itself down without clearing everything above first
		Comparator<BlockPos> keepBlockUnderComparator = Comparator.comparing(new BlockPos(mc.player.getPos().add(0, -0.8, 0))::equals);

		Comparator<BlockPos> distComparator = Comparator.comparingDouble(b -> mc.player.getEyePos().distanceTo(Vec3d.ofCenter(b)));
		Comparator<BlockPos> hardnessComparator = Comparator.comparing(b -> mc.world.getBlockState(b).getHardness(mc.world, b));

		switch (getSetting(5).asMode().mode) {
			case 0:
				return keepBlockUnderComparator.thenComparing(distComparator);
			case 1:
				return keepBlockUnderComparator.thenComparing(distComparator.reversed());
			case 2:
				return keepBlockUnderComparator.thenComparing(hardnessComparator);
			case 3:
				return keepBlockUnderComparator.thenComparing(hardnessComparator.reversed());
			default:
				return keepBlockUnderComparator;
		}
	}
}

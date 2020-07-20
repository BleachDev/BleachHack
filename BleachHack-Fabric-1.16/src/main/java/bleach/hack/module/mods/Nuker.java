/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
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

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import bleach.hack.utils.FabricReflect;
import bleach.hack.utils.WorldUtils;
import bleach.hack.utils.file.BleachFileMang;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.RayTraceContext;

public class Nuker extends Module {

	private List<Block> blockList = new ArrayList<>();

	public Nuker() {
		super("Nuker", KEY_UNBOUND, Category.WORLD, "Breaks blocks around you",
				new SettingMode("Mode: ", "Normal", "Multi", "Instant"),
				new SettingSlider("Range: ", 1, 6, 4.2, 1),
				new SettingSlider("Cooldown: ", 0, 4, 0, 0),
				new SettingToggle("All Blocks", true),
				new SettingToggle("Flatten", false),
				new SettingToggle("Rotate", false),
				new SettingToggle("NoParticles", false),
				new SettingMode("Sort: ", "Normal", "Hardness"),
				new SettingSlider("Multi: ", 1, 10, 2, 0));
	}

	public void onEnable() {
		blockList.clear();
		for (String s: BleachFileMang.readFileLines("nukerblocks.txt")) blockList.add(Registry.BLOCK.get(new Identifier(s)));

		super.onEnable();
	}

	@Subscribe
	public void onTick(EventTick event) {
		double range = getSettings().get(1).asSlider().getValue();
		List<BlockPos> blocks = new ArrayList<>();

		/* Add blocks around player */
		for (int x = (int) range; x >= (int) -range; x--) {
			for (int y = (int) range; y >= (getSettings().get(4).asToggle().state ? 0 : (int) -range); y--) {
				for (int z = (int) range; z >= (int) -range; z--) {
					BlockPos pos = new BlockPos(mc.player.getPos().add(x, y + 0.1, z));
					if (!canSeeBlock(pos) || mc.world.getBlockState(pos).getBlock() == Blocks.AIR || WorldUtils.isFluid(pos)) continue;
					blocks.add(pos);
				}
			}
		}

		if (blocks.isEmpty()) return;

		if (getSettings().get(6).asToggle().state) FabricReflect.writeField(
				mc.particleManager, Maps.newIdentityHashMap(), "field_3830", "particles");

		if (getSettings().get(7).asMode().mode == 1) blocks.sort((a, b) -> Float.compare(
				mc.world.getBlockState(a).getHardness(null, a), mc.world.getBlockState(b).getHardness(null, b)));

		/* Move the block under the player to last so it doesn't mine itself down without clearing everything above first */
		if (blocks.contains(mc.player.getBlockPos().down())) {
			blocks.remove(mc.player.getBlockPos().down());
			blocks.add(mc.player.getBlockPos().down());
		}

		int broken = 0;
		for (BlockPos pos: blocks) {
			if (!getSettings().get(3).asToggle().state && !blockList.contains(mc.world.getBlockState(pos).getBlock())) continue;

			Vec3d vec = Vec3d.of(pos).add(0.5, 0.5, 0.5);

			if (mc.player.getPos().distanceTo(vec) > range + 0.5) continue;

			Direction dir = null;
			double dist = Double.MAX_VALUE;
			for (Direction d: Direction.values()) {
				double dist2 = mc.player.getPos().distanceTo(Vec3d.of(pos.offset(d)).add(0.5, 0.5, 0.5));
				if (dist2 > range || mc.world.getBlockState(pos.offset(d)).getBlock() != Blocks.AIR || dist2 > dist) continue;
				dist = dist2;
				dir = d;
			}

			if (dir == null) continue;

			if (getSettings().get(5).asToggle().state) {
				float[] prevRot = new float[] {mc.player.yaw, mc.player.pitch};
				EntityUtils.facePos(vec.x, vec.y, vec.z);
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(
						mc.player.yaw, mc.player.pitch, mc.player.isOnGround()));
				mc.player.yaw = prevRot[0];
				mc.player.pitch = prevRot[1];
			}

			if (getSettings().get(0).asMode().mode == 1) mc.interactionManager.attackBlock(pos, dir);
			else mc.interactionManager.updateBlockBreakingProgress(pos, dir);

			mc.player.swingHand(Hand.MAIN_HAND);

			broken++;
			if (getSettings().get(0).asMode().mode == 0
					|| (getSettings().get(0).asMode().mode == 1 && broken >= (int) getSettings().get(8).asSlider().getValue())) return;
		}
	}

	public boolean canSeeBlock(BlockPos pos) {
		double diffX = pos.getX() + 0.5 - mc.player.getCameraPosVec(mc.getTickDelta()).x;
		double diffY = pos.getY() + 0.5 - mc.player.getCameraPosVec(mc.getTickDelta()).y;
		double diffZ = pos.getZ() + 0.5 - mc.player.getCameraPosVec(mc.getTickDelta()).z;

		double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

		float yaw = mc.player.yaw + MathHelper.wrapDegrees((float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90 - mc.player.yaw);
		float pitch = mc.player.pitch + MathHelper.wrapDegrees((float)-Math.toDegrees(Math.atan2(diffY, diffXZ)) - mc.player.pitch);

		Vec3d rotation = new Vec3d(
				MathHelper.sin(-yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F),
				(-MathHelper.sin(pitch * 0.017453292F)),
				MathHelper.cos(-yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F));

		Vec3d rayVec = mc.player.getCameraPosVec(mc.getTickDelta()).add(rotation.x * 6, rotation.y * 6, rotation.z * 6);
		return mc.world.rayTrace(new RayTraceContext(mc.player.getCameraPosVec(mc.getTickDelta()),
				rayVec, RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.NONE, mc.player))
				.getBlockPos().equals(pos);
	}

}

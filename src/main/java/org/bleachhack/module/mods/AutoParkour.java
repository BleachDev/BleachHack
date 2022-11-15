/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import net.minecraft.block.LadderBlock;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.bleachhack.event.events.EventClientMove;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingColor;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.QuadColor;

import java.util.Comparator;

// credit: https://github.com/Wurst-Imperium/Wurst7/blob/21w11a/src/main/java/net/wurstclient/hacks/ParkourHack.java
// modified by https://github.com/lasnikprogram
// modified by https://github.com/BleachDev

public class AutoParkour extends Module {

	private BlockPos smartPos = null;

	public AutoParkour() {
		super("AutoParkour", KEY_UNBOUND, ModuleCategory.MOVEMENT, "Makes you jump when you reach the edge of a block (Use this if you suck at parkouring).",
				new SettingToggle("AutoSprint", true).withDesc("Automatically makes you sprint when jumping."),
				new SettingToggle("Smart", true).withDesc("Tries to figure out what block you're jumping to then jumps to that block.").withChildren(
						new SettingToggle("Snap", false).withDesc("Snaps you to the target block to prevent you from overshooting it."),
						new SettingToggle("Highlight", false).withDesc("Highlights the target block you're jumping to.").withChildren(
								new SettingColor("Color", 215, 110, 145).withDesc("The color of the highlight."))));
	}

	@Override
	public void onDisable(boolean inWorld) {
		smartPos = null;

		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (smartPos != null) {
			if (mc.player.getY() - 0.5 < smartPos.getY() && mc.player.getVelocity().y < 0) {
				smartPos = null;
			}
		}

		if (!mc.player.isSneaking() && mc.player.isOnGround()) {
			smartPos = null;

			Box box = mc.player.getBoundingBox().offset(0, -0.51, 0);
			Iterable<VoxelShape> blockCollisions = mc.world.getBlockCollisions(mc.player, box);

			if (!blockCollisions.iterator().hasNext()) {
				if (getSetting(0).asToggle().getState() && !mc.player.isSprinting()) {
					mc.player.setSprinting(true);
					mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_SPRINTING));
				}

				if (getSetting(1).asToggle().getState()) {
					Vec3d lookVec = mc.player.getPos().add(new Vec3d(0, 0, 3.5).rotateY(-(float) Math.toRadians(mc.player.getYaw())));

					BlockPos nearestPos = BlockPos.streamOutwards(mc.player.getBlockPos().down(), 4, 1, 4)
							.map(BlockPos::toImmutable)
							.filter(pos -> (mc.world.isTopSolid(pos, mc.player) && !mc.world.getBlockCollisions(mc.player, new Box(pos.up(), pos.add(1, 3, 1))).iterator().hasNext())
									|| mc.world.getBlockState(pos).getBlock() instanceof LadderBlock
									|| mc.world.getBlockState(pos.up()).getBlock() instanceof LadderBlock)
							.filter(pos -> mc.player.getPos().distanceTo(Vec3d.of(pos).add(0.5, 1, 0.5)) >= 1)
							.filter(pos -> mc.player.getPos().distanceTo(Vec3d.of(pos).add(0.5, 1, 0.5)) <= 4.5 /* ? */)
							.sorted(Comparator.comparing(pos -> pos.getSquaredDistance(lookVec)))
							.findFirst().orElse(null);

					if (nearestPos != null) {
						smartPos = nearestPos;
					}
				}

				mc.player.jump();
			}
		}
	}
	
	@BleachSubscribe
	public void onWorldRender(EventWorldRender.Post event) {
		if (smartPos != null && getSetting(1).asToggle().getChild(1).asToggle().getState()) {
			int[] rgb = getSetting(1).asToggle().getChild(1).asToggle().getChild(0).asColor().getRGBArray();
			Renderer.drawBoxBoth(smartPos, QuadColor.single(rgb[0], rgb[1], rgb[2], 128), 2.5f);
		}
	}

	@BleachSubscribe
	public void onClientMove(EventClientMove event) {
		if (smartPos != null && getSetting(1).asToggle().getState()) {
			if (!getSetting(1).asToggle().getChild(0).asToggle().getState()
					&& (mc.player.getBoundingBox().maxX < smartPos.getX()
						|| mc.player.getBoundingBox().minX > smartPos.getX() + 1
						|| mc.player.getBoundingBox().maxZ < smartPos.getZ()
						|| mc.player.getBoundingBox().minZ > smartPos.getZ() + 1)) {
				return;
			}

			// maffs
			double targetDiffX = (smartPos.getX() + 0.5) - mc.player.getX();
			double targetDiffZ = (smartPos.getZ() + 0.5) - mc.player.getZ();

			float targetYaw = (float) Math.toDegrees(Math.atan2(targetDiffZ, targetDiffX)) - 90F;

			double currentDiffX = (mc.player.getX() + event.getVec().x) - mc.player.getX();
			double currentDiffZ = (mc.player.getZ() + event.getVec().z) - mc.player.getZ();

			float currentYaw = (float) Math.toDegrees(Math.atan2(currentDiffZ, currentDiffX)) - 90F;

			event.setVec(event.getVec().rotateY(-(float) Math.toRadians(targetYaw - currentYaw)));
		}
	}
}

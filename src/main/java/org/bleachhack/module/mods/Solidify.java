/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventBlockShape;
import org.bleachhack.event.events.EventClientMove;
import org.bleachhack.event.events.EventPacket;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingToggle;

import net.minecraft.block.CactusBlock;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.block.HoneyBlock;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.shape.VoxelShapes;

public class Solidify extends Module {

	public Solidify() {
		super("Solidify", KEY_UNBOUND, ModuleCategory.WORLD, "Adds collision boxes to certain blocks/areas.",
				new SettingToggle("Cactus", true).withDesc("Makes cactuses solid so they don't prickle you."),
				new SettingToggle("Fire", true).withDesc("Makes fire solid."),
				new SettingToggle("Lava", true).withDesc("Makes lava solid."),
				new SettingToggle("Cobweb", false).withDesc("Makes cobwebs solid."),
				new SettingToggle("BerryBushes", false).withDesc("Makes berry bushes solid."),
				new SettingToggle("Honeyblocks", false).withDesc("Makes honey blocks solid so you don't slide on the edges."),
				new SettingToggle("PowderSnow", false).withDesc("Makes powdered snow solid even if you don't have lether boots."),
				new SettingToggle("Unloaded", true).withDesc("Adds walls to unloaded chunks."));
	}

	@BleachSubscribe
	public void onBlockShape(EventBlockShape event) {
		if ((getSetting(0).asToggle().getState() && event.getState().getBlock() instanceof CactusBlock)
				|| (getSetting(1).asToggle().getState() && event.getState().getBlock() instanceof FireBlock)
				|| (getSetting(2).asToggle().getState() && event.getState().getFluidState().getFluid() instanceof LavaFluid)
				|| (getSetting(3).asToggle().getState() && event.getState().getBlock() instanceof CobwebBlock)
				|| (getSetting(4).asToggle().getState() && event.getState().getBlock() instanceof SweetBerryBushBlock)
				|| (getSetting(5).asToggle().getState() && event.getState().getBlock() instanceof HoneyBlock)
				|| (getSetting(6).asToggle().getState() && event.getState().getBlock() instanceof PowderSnowBlock)) {
			event.setShape(VoxelShapes.fullCube());
		}
	}

	@BleachSubscribe
	public void onClientMove(EventClientMove event) {
		int x = (int) (mc.player.getX() + event.getVec().x) >> 4;
		int z = (int) (mc.player.getZ() + event.getVec().z) >> 4;
		if (getSetting(7).asToggle().getState() && !mc.world.getChunkManager().isChunkLoaded(x, z)) {
			event.setCancelled(true);
		}
	}

	@BleachSubscribe
	public void onSendPacket(EventPacket.Send event) {
		if (getSetting(7).asToggle().getState()) {
			if (event.getPacket() instanceof VehicleMoveC2SPacket) {
				VehicleMoveC2SPacket packet = (VehicleMoveC2SPacket) event.getPacket();
				if (!mc.world.getChunkManager().isChunkLoaded((int) packet.getX() >> 4, (int) packet.getZ() >> 4)) {
					mc.player.getVehicle().updatePosition(mc.player.getVehicle().prevX, mc.player.getVehicle().prevY, mc.player.getVehicle().prevZ);
					event.setCancelled(true);
				}
			} else if (event.getPacket() instanceof PlayerMoveC2SPacket) {
				PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket) event.getPacket();
				if (!mc.world.getChunkManager().isChunkLoaded((int) packet.getX(mc.player.getX()) >> 4, (int) packet.getZ(mc.player.getZ()) >> 4)) {
					event.setCancelled(true);
				}
			}
		}
	}
}

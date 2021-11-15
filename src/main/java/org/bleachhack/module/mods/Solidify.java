/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventBlockShape;
import org.bleachhack.event.events.EventClientMove;
import org.bleachhack.event.events.EventSendPacket;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.module.setting.base.SettingToggle;

import net.minecraft.block.CactusBlock;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.block.HoneyBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.shape.VoxelShapes;

public class Solidify extends Module {

	public Solidify() {
		super("Solidify", KEY_UNBOUND, ModuleCategory.WORLD, "Adds collision boxes to certain blocks/areas.",
				new SettingToggle("Cactus", true).withDesc("Adds a bigger collision box to cactuses."),
				new SettingToggle("Fire", true).withDesc("Adds a collision box to fire."),
				new SettingToggle("Lava", true).withDesc("Adds a collision box to lava."),
				new SettingToggle("Cobweb", false).withDesc("Adds a collision box to cobweb."),
				new SettingToggle("BerryBushes", false).withDesc("Adds a collision box to berry bushes."),
				new SettingToggle("Honeyblocks", false).withDesc("Adds a bigger collision box to honey blocks so you don't slide on the edges."),
				new SettingToggle("Unloaded", true).withDesc("Adds walls to unloaded chunks."));
	}

	@BleachSubscribe
	public void onBlockShape(EventBlockShape event) {
		if ((getSetting(0).asToggle().state && event.getState().getBlock() instanceof CactusBlock)
				|| (getSetting(1).asToggle().state && event.getState().getBlock() instanceof FireBlock)
				|| (getSetting(2).asToggle().state && event.getState().getFluidState().getFluid() instanceof LavaFluid)
				|| (getSetting(3).asToggle().state && event.getState().getBlock() instanceof CobwebBlock)
				|| (getSetting(4).asToggle().state && event.getState().getBlock() instanceof SweetBerryBushBlock)
				|| (getSetting(5).asToggle().state && event.getState().getBlock() instanceof HoneyBlock)) {
			event.setShape(VoxelShapes.fullCube());
		}
	}

	@BleachSubscribe
	public void onClientMove(EventClientMove event) {
		int x = (int) (mc.player.getX() + event.getVec().x) >> 4;
		int z = (int) (mc.player.getZ() + event.getVec().z) >> 4;
		if (getSetting(6).asToggle().state && !mc.world.getChunkManager().isChunkLoaded(x, z)) {
			event.setCancelled(true);
		}
	}

	@BleachSubscribe
	public void onSendPacket(EventSendPacket event) {
		if (getSetting(6).asToggle().state) {
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

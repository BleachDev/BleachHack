/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import bleach.hack.eventbus.BleachSubscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingMode;
import bleach.hack.module.Module;
import bleach.hack.module.setting.base.SettingToggle;
import bleach.hack.module.setting.other.SettingRotate;
import bleach.hack.util.InventoryUtils;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

public class Nofall extends Module {

	public Nofall() {
		super("Nofall", KEY_UNBOUND, ModuleCategory.PLAYER, "Prevents you from taking fall damage.",
				new SettingMode("Mode", "Simple", "Packet", "Cobweb").withDesc("What Nofall mode to use."),
				new SettingRotate(false).withDesc("Rotates when placing blocks (Cobweb mode)"),
				new SettingToggle("LegitPlace", false).withDesc("Only places on sides you can see (Cobweb mode)"),
				new SettingToggle("AirPlace", false).withDesc("Places blocks in the air without support blocks (Cobweb mode)"),
				new SettingToggle("NoSwing", false).withDesc("Doesn't swing your hand clientside (Cobweb mode)"));
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		switch(getSetting(0).asMode().mode) {
			case 0:
				if (mc.player.fallDistance > 2.5f) {
					if (mc.player.isFallFlying())
						return;
					mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
				}
				break;
			case 1:
				if (mc.player.fallDistance > 2.5f && mc.world.getBlockState(mc.player.getBlockPos().add(
						0, -1.5 + (mc.player.getVelocity().y * 0.1), 0)).getBlock() != Blocks.AIR) {
					mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
					mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
							mc.player.getX(), mc.player.getY() - 420.69, mc.player.getZ(), true));
					mc.player.fallDistance = 0;
				}
				break;
			case 2:
				if (mc.player.fallDistance > 2.5f) {
					if (mc.player.isFallFlying())
						return;
					int slot = InventoryUtils.getSlot(false, i -> mc.player.getInventory().getStack(i).getItem()
							== Registry.ITEM.get(new Identifier("cobweb")));
					if (slot == -1) return;
					WorldUtils.placeBlock(
							new BlockPos(mc.player.getPos().add(0, -1, 0)), slot,
							getSetting(1).asRotate(),
							getSetting(2).asToggle().state,
							getSetting(3).asToggle().state,
							!getSetting(4).asToggle().state);
				}
				break;
		}
	}
}

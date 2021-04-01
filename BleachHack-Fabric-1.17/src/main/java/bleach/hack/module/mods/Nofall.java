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

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.util.FabricReflect;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Nofall extends Module {
	
	private boolean altBool = false;

	public Nofall() {
		super("Nofall", KEY_UNBOUND, Category.PLAYER, "Prevents you from taking fall damage.",
				new SettingMode("Mode", "Simple", "Packet", "ec.me").withDesc("What nofall mode to use"));
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (mc.player.fallDistance > 2.5f && getSetting(0).asMode().mode == 0) {
			if (mc.player.isFallFlying())
				return;
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
		}

		if (mc.player.fallDistance > 2.5f && getSetting(0).asMode().mode == 1 &&
				mc.world.getBlockState(mc.player.getBlockPos().method_35849(
						0, -1.5 + (mc.player.getVelocity().y * 0.1), 0)).getBlock() != Blocks.AIR) {
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
					mc.player.getX(), mc.player.getY() - 420.69, mc.player.getZ(), true));
			mc.player.fallDistance = 0;
		}
	}
	
	@Subscribe
	public void onSendPacket(EventSendPacket event) {
		if (event.getPacket() instanceof PlayerMoveC2SPacket && getSetting(0).asMode().mode == 2) {
			if (mc.player.fallDistance > 2.5f && !altBool) {
				mc.player.setOnGround(true);
				FabricReflect.writeField(event.getPacket(), true, "field_12891", "onGround");
				altBool = true;
			} else {
				altBool = false;
			}
		}
	}

}

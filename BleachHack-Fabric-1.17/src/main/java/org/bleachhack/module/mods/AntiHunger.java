/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventSendPacket;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.module.setting.base.SettingToggle;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class AntiHunger extends Module {

	private boolean bool = false;

	public AntiHunger() {
		super("AntiHunger", KEY_UNBOUND, ModuleCategory.PLAYER, "Minimizes the amount of hunger you use.",
				new SettingToggle("Relaxed", false).withDesc("Only activates every other ticks, might fix getting fly kicked."));
	}

	@BleachSubscribe
	public void onSendPacket(EventSendPacket event) {
		if (event.getPacket() instanceof PlayerMoveC2SPacket) {
			if (mc.player.getVelocity().y != 0 && !mc.options.keyJump.isPressed() && (!bool || !getSetting(0).asToggle().state)) {
				// if (((PlayerMoveC2SPacket) event.getPacket()).isOnGround())
				// event.setCancelled(true);
				boolean onGround = mc.player.fallDistance >= 0.1f;
				mc.player.setOnGround(onGround);
				((PlayerMoveC2SPacket) event.getPacket()).onGround = onGround;
				bool = true;
			} else {
				bool = false;
			}
		}
	}

}

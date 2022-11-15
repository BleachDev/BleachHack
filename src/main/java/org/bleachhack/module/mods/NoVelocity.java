/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventPlayerPushed;
import org.bleachhack.event.events.EventPacket;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

/**
 * @author sl First Module utilizing EventBus!
 */
public class NoVelocity extends Module {

	public NoVelocity() {
		super("NoVelocity", KEY_UNBOUND, ModuleCategory.PLAYER, "If you take some damage, you don't move.",
				new SettingToggle("Knockback", true).withDesc("Reduces knockback from other entities.").withChildren(
						new SettingSlider("VelXZ", 0, 100, 0, 1).withDesc("How much horizontal velocity to keep."),
						new SettingSlider("VelY", 0, 100, 0, 1).withDesc("How much vertical velocity  to keep.")),
				new SettingToggle("Explosions", true).withDesc("Reduces explosion velocity.").withChildren(
						new SettingSlider("VelXZ", 0, 100, 0, 1).withDesc("How much horizontal velocity to keep."),
						new SettingSlider("VelY", 0, 100, 0, 1).withDesc("How much vertical velocity to keep.")),
				new SettingToggle("Pushing", true).withDesc("Reduces how much you get pushed by entitie.s").withChildren(
						new SettingSlider("Amount", 0, 100, 0, 1).withDesc("How much pushing to keep.")),
				new SettingToggle("Fluids", true).withDesc("Reduces how much you get pushed from fluids."));
	}

	@BleachSubscribe
	public void onPlayerPushed(EventPlayerPushed event) {
		if (getSetting(2).asToggle().getState()) {
			double amount = getSetting(2).asToggle().getChild(0).asSlider().getValue() / 100d;
			event.setPushX(event.getPushX() * amount);
			event.setPushY(event.getPushY() * amount);
			event.setPushZ(event.getPushZ() * amount);
		}
	}

	@BleachSubscribe
	public void readPacket(EventPacket.Read event) {
		if (mc.player == null)
			return;

		if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket && getSetting(0).asToggle().getState()) {
			EntityVelocityUpdateS2CPacket packet = (EntityVelocityUpdateS2CPacket) event.getPacket();
			if (packet.getId() == mc.player.getId()) {
				double velXZ = getSetting(0).asToggle().getChild(0).asSlider().getValue() / 100;
				double velY = getSetting(0).asToggle().getChild(1).asSlider().getValue() / 100;
				
				double pvelX = (packet.getVelocityX() / 8000d - mc.player.getVelocity().x) * velXZ;
				double pvelY = (packet.getVelocityY() / 8000d - mc.player.getVelocity().y) * velY;
				double pvelZ = (packet.getVelocityZ() / 8000d - mc.player.getVelocity().z) * velXZ;

				packet.velocityX = (int) (pvelX * 8000 + mc.player.getVelocity().x * 8000);
				packet.velocityY = (int) (pvelY * 8000 + mc.player.getVelocity().y * 8000);
				packet.velocityZ = (int) (pvelZ * 8000 + mc.player.getVelocity().z * 8000);
			}
		} else if (event.getPacket() instanceof ExplosionS2CPacket && getSetting(1).asToggle().getState()) {
			ExplosionS2CPacket packet = (ExplosionS2CPacket) event.getPacket();

			double velXZ = getSetting(1).asToggle().getChild(0).asSlider().getValue() / 100;
			double velY = getSetting(1).asToggle().getChild(1).asSlider().getValue() / 100;
			
			packet.playerVelocityX = (float) (packet.getPlayerVelocityX() * velXZ);
			packet.playerVelocityY = (float) (packet.getPlayerVelocityY() * velY);
			packet.playerVelocityZ = (float) (packet.getPlayerVelocityZ() * velXZ);
		}
	}

	// Fluid handling in MixinFlowableFluid.getVelocity_hasNext()
}

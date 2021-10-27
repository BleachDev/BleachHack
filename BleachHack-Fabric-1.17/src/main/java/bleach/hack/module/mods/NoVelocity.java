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

import bleach.hack.event.events.EventPlayerPushed;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingSlider;
import bleach.hack.module.setting.base.SettingToggle;
import bleach.hack.module.Module;
import bleach.hack.util.FabricReflect;
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
		if (getSetting(2).asToggle().state) {
			event.setPush(event.getPush().multiply(getSetting(2).asToggle().getChild(0).asSlider().getValue() / 100d));
		}
	}

	@BleachSubscribe
	public void readPacket(EventReadPacket event) {
		if (mc.player == null)
			return;

		if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket && getSetting(0).asToggle().state) {
			EntityVelocityUpdateS2CPacket packet = (EntityVelocityUpdateS2CPacket) event.getPacket();
			if (packet.getId() == mc.player.getId()) {
				double velXZ = getSetting(0).asToggle().getChild(0).asSlider().getValue() / 100;
				double velY = getSetting(0).asToggle().getChild(1).asSlider().getValue() / 100;
				
				double pvelX = (packet.getVelocityX() / 8000d - mc.player.getVelocity().x) * velXZ;
				double pvelY = (packet.getVelocityY() / 8000d - mc.player.getVelocity().y) * velY;
				double pvelZ = (packet.getVelocityZ() / 8000d - mc.player.getVelocity().z) * velXZ;

				FabricReflect.writeField(packet, (int) (pvelX * 8000 + mc.player.getVelocity().x * 8000), "field_12563", "velocityX");
				FabricReflect.writeField(packet, (int) (pvelY * 8000 + mc.player.getVelocity().y * 8000), "field_12562", "velocityY");
				FabricReflect.writeField(packet, (int) (pvelZ * 8000 + mc.player.getVelocity().z * 8000), "field_12561", "velocityZ");
			}
		} else if (event.getPacket() instanceof ExplosionS2CPacket && getSetting(1).asToggle().state) {
			ExplosionS2CPacket packet = (ExplosionS2CPacket) event.getPacket();

			double velXZ = getSetting(1).asToggle().getChild(0).asSlider().getValue() / 100;
			double velY = getSetting(1).asToggle().getChild(1).asSlider().getValue() / 100;
			
			FabricReflect.writeField(event.getPacket(), (float) (packet.getPlayerVelocityX() * velXZ), "field_12176", "playerVelocityX");
			FabricReflect.writeField(event.getPacket(), (float) (packet.getPlayerVelocityY() * velY), "field_12183", "playerVelocityY");
			FabricReflect.writeField(event.getPacket(), (float) (packet.getPlayerVelocityZ() * velXZ), "field_12182", "playerVelocityZ");
		}
	}

	// Fluid handling in MixinFlowableFluid.getVelocity_hasNext()
}

/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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

import bleach.hack.event.events.EventPlayerPushed;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.FabricReflect;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

/**
 * @author sl First Module utilizing EventBus!
 */
public class NoVelocity extends Module {

	public NoVelocity() {
		super("NoVelocity", KEY_UNBOUND, Category.PLAYER, "If you take some damage, you don't move.",
				new SettingToggle("Knockback", true).withDesc("Reduces knockback from other entites").withChildren(
						new SettingSlider("VelXZ", 0, 100, 0, 1).withDesc("How much horizontal velocity"),
						new SettingSlider("VelY", 0, 100, 0, 1).withDesc("How much vertical velocity")),
				new SettingToggle("Explosions", true).withDesc("Reduces explosion velocity").withChildren(
						new SettingSlider("VelXZ", 0, 100, 0, 1).withDesc("How much horizontal velocity"),
						new SettingSlider("VelY", 0, 100, 0, 1).withDesc("How much vertical velocity")),
				new SettingToggle("Pushing", true).withDesc("Reduces how much you get pushed by entites").withChildren(
						new SettingSlider("Amount", 0, 100, 0, 1).withDesc("How much to reduce pushing")),
				new SettingToggle("Fluids", true).withDesc("Reduces how much you get pushed from fluids"));
	}

	@Subscribe
	public void onPlayerPushed(EventPlayerPushed event) {
		if (getSetting(2).asToggle().state) {
			event.setPush(event.getPush().multiply(getSetting(2).asToggle().getChild(0).asSlider().getValue() / 100d));
		}
	}

	@Subscribe
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
			FabricReflect.writeField(event.getPacket(), (float) (packet.getPlayerVelocityY() * velY), "field_12182", "playerVelocityY");
			FabricReflect.writeField(event.getPacket(), (float) (packet.getPlayerVelocityZ() * velXZ), "field_12183", "playerVelocityZ");
		}
	}

	// Fluid handling in MixinFlowableFluid.getVelocity_hasNext()
}

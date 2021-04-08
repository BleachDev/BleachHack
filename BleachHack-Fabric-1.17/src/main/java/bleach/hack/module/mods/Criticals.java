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

import java.util.Random;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.util.PlayerInteractEntityC2SUtils;
import bleach.hack.util.PlayerInteractEntityC2SUtils.InteractType;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.particle.ParticleTypes;

/**
 * @author sl, Bleach
 */
public class Criticals extends Module {

	public Criticals() {
		super("Criticals", KEY_UNBOUND, Category.COMBAT, "Attempts to force Critical hits on entities you hit.");
	}

	@Subscribe
	public void sendPacket(EventSendPacket event) {
		if (event.getPacket() instanceof PlayerInteractEntityC2SPacket) {
			PlayerInteractEntityC2SPacket packet = (PlayerInteractEntityC2SPacket) event.getPacket();
			if (PlayerInteractEntityC2SUtils.getInteractType(packet) == InteractType.INTERACT_AT) {
				this.doCritical();

				/* Lets fake some extra paricles why not */
				Entity e = PlayerInteractEntityC2SUtils.getEntity(packet);

				if (e != null) {
					Random r = new Random();
					for (int i = 0; i < 10; i++) {
						mc.particleManager.addParticle(ParticleTypes.CRIT, e.getX(), e.getY() + e.getHeight() / 2, e.getZ(),
								r.nextDouble() - 0.5, r.nextDouble() - 0.5, r.nextDouble() - 0.5);
					}
				}
			}
		}
	}

	private void doCritical() {
		if (!mc.player.isOnGround() || mc.player.isInLava() || mc.player.isTouchingWater()) {
			return;
		}

		double posX = mc.player.getX();
		double posY = mc.player.getY();
		double posZ = mc.player.getZ();
		mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(posX, posY + 0.0625, posZ, true));
		mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(posX, posY, posZ, false));
	}
}

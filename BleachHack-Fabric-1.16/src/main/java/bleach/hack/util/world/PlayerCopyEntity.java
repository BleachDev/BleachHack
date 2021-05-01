/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util.world;

import java.util.UUID;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerCopyEntity extends OtherClientPlayerEntity {

	public PlayerCopyEntity() {
		this(MinecraftClient.getInstance().player);
	}

	public PlayerCopyEntity(PlayerEntity player) {
		this(player, player.getX(), player.getY(), player.getZ());
	}

	public PlayerCopyEntity(PlayerEntity player, double x, double y, double z) {
		super(MinecraftClient.getInstance().world, player.getGameProfile());

		updateTrackedPosition(player.getX(), player.getY(), player.getZ());
		refreshPositionAfterTeleport(player.getX(), player.getY(), player.getZ());
		pitch = player.pitch;
		yaw = headYaw = bodyYaw = player.yaw;

		// Cache the player textures, then switch to a random uuid
		// because the world doesn't allow duplicate uuids in 1.17+
		getPlayerListEntry();
		setUuid(UUID.randomUUID());

		setHealth(player.getHealth());
		setAbsorptionAmount(player.getAbsorptionAmount());

		for (StatusEffectInstance effect: player.getStatusEffects()) {
			addStatusEffect(effect);
		}

		inventory.main.set(inventory.selectedSlot, player.getMainHandStack());
		inventory.offHand.set(0, player.getOffHandStack());
		inventory.armor.set(0, player.inventory.armor.get(0));
		inventory.armor.set(1, player.inventory.armor.get(1));
		inventory.armor.set(2, player.inventory.armor.get(2));
		inventory.armor.set(3, player.inventory.armor.get(3));
	}

	public void spawn() {
		MinecraftClient.getInstance().world.addEntity(this.getEntityId(), this);
	}

	public void despawn() {
		MinecraftClient.getInstance().world.removeEntity(this.getEntityId());
	}

}

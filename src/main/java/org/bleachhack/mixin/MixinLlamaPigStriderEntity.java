/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.bleachhack.BleachHack;
import org.bleachhack.event.events.EventEntityControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.world.World;

@Mixin({AbstractHorseEntity.class, PigEntity.class, StriderEntity.class})
public abstract class MixinLlamaPigStriderEntity extends AnimalEntity {

	private MixinLlamaPigStriderEntity(EntityType<? extends AnimalEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "isSaddled", at = @At("HEAD"), cancellable = true)
	private void isSaddled(CallbackInfoReturnable<Boolean> info) {
		EventEntityControl event = new EventEntityControl();
		BleachHack.eventBus.post(event);

		if (event.canBeControlled() != null) {
			info.setReturnValue(event.canBeControlled());
		}
	}
}
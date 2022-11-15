/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import org.bleachhack.BleachHack;
import org.bleachhack.event.events.EventDamage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

	private MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Redirect(method = "takeKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(DDD)V"))
	private void takeKnockback_setVelocity(LivingEntity entity, double x, double y, double z) {
		EventDamage.Knockback event = new EventDamage.Knockback(x - getVelocity().getX(), y - getVelocity().getY(), z - getVelocity().getZ());
		BleachHack.eventBus.post(event);
		
		if (!event.isCancelled()) {
			setVelocity(event.getVelX() + getVelocity().getX(), event.getVelY() + getVelocity().getY(), event.getVelZ() + getVelocity().getZ());
		}
	}
	
	@Inject(method = "damage", at = @At("HEAD"), cancellable = true)
	private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callbackInfo) {
		EventDamage.Normal event = new EventDamage.Normal(source, amount);
		BleachHack.eventBus.post(event);
		
		if (event.isCancelled()) {
			callbackInfo.setReturnValue(false);
			callbackInfo.cancel();
		}
	}
}

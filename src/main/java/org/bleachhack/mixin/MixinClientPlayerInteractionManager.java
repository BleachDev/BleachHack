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
import org.bleachhack.event.events.EventBlockBreakCooldown;
import org.bleachhack.event.events.EventInteract;
import org.bleachhack.event.events.EventReach;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

	@Shadow private int blockBreakingCooldown;

	@Redirect(method = "updateBlockBreakingProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", ordinal = 3),
			require = 0 /* TODO: meteor compatibility */)
	private void updateBlockBreakingProgress(ClientPlayerInteractionManager clientPlayerInteractionManager, int newCooldown) {
		EventBlockBreakCooldown event = new EventBlockBreakCooldown(newCooldown);
		BleachHack.eventBus.post(event);

		this.blockBreakingCooldown = event.getCooldown();
	}

	@Redirect(method = "updateBlockBreakingProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", ordinal = 4),
			require = 0 /* TODO: meteor compatibility */)
	private void updateBlockBreakingProgress2(ClientPlayerInteractionManager clientPlayerInteractionManager, int newCooldown) {
		EventBlockBreakCooldown event = new EventBlockBreakCooldown(newCooldown);
		BleachHack.eventBus.post(event);

		this.blockBreakingCooldown = event.getCooldown();
	}

	@Redirect(method = "attackBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I"),
			require = 0 /* TODO: meteor compatibility */)
	private void attackBlock(ClientPlayerInteractionManager clientPlayerInteractionManager, int newCooldown) {
		EventBlockBreakCooldown event = new EventBlockBreakCooldown(newCooldown);
		BleachHack.eventBus.post(event);

		this.blockBreakingCooldown = event.getCooldown();
	}

	@Inject(method = "breakBlock", at = @At("HEAD"), cancellable = true)
	private void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
		EventInteract.BreakBlock event = new EventInteract.BreakBlock(pos);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			callback.setReturnValue(false);
		}
	}

	@Inject(method = { "attackBlock", "updateBlockBreakingProgress" }, at = @At("HEAD"), cancellable = true)
	private void attackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> callback) {
		EventInteract.AttackBlock event = new EventInteract.AttackBlock(pos, direction);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			callback.setReturnValue(false);
		}
	}

	@Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
	private void interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> callback) {
		EventInteract.InteractBlock event = new EventInteract.InteractBlock(hand, hitResult);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			callback.setReturnValue(ActionResult.PASS);
		}
	}

	@Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
	private void interactItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> callback) {
		EventInteract.InteractItem event = new EventInteract.InteractItem(hand);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			callback.setReturnValue(ActionResult.PASS);
		}
	}

	@Inject(method = "getReachDistance", at = @At("RETURN"), cancellable = true)
	private void getReachDistance(CallbackInfoReturnable<Float> callback) {
		EventReach event = new EventReach(callback.getReturnValueF());
		BleachHack.eventBus.post(event);

		callback.setReturnValue(event.getReach());
	}
}

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
import org.bleachhack.event.events.EventClientMove;
import org.bleachhack.event.events.EventSendMovementPackets;
import org.bleachhack.event.events.EventSwingHand;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.BetterPortal;
import org.bleachhack.module.mods.EntityControl;
import org.bleachhack.module.mods.Freecam;
import org.bleachhack.module.mods.NoSlow;
import org.bleachhack.module.mods.SafeWalk;
import org.bleachhack.module.mods.Scaffold;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

	@Shadow private float mountJumpStrength;

	@Shadow private ClientPlayNetworkHandler networkHandler;
	@Shadow private MinecraftClient client;

	private MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
		super(world, profile, null);
	}

	@Shadow private void autoJump(float dx, float dz) {}

	@Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
	private void sendMovementPackets(CallbackInfo info) {
		EventSendMovementPackets event = new EventSendMovementPackets();
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			info.cancel();
		}
	}

	@Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"),
			require = 0 /* TODO: meteor compatibility */)
	private boolean tickMovement_isUsingItem(ClientPlayerEntity player) {
		NoSlow noSlow = ModuleManager.getModule(NoSlow.class);
		if (noSlow.isEnabled() && noSlow.getSetting(5).asToggle().getState())
			return false;

		return player.isUsingItem();
	}

	@Inject(method = "move", at = @At("HEAD"), cancellable = true)
	private void move(MovementType type, Vec3d movement, CallbackInfo info) {
		EventClientMove event = new EventClientMove(type, movement);
		BleachHack.eventBus.post(event);
		if (event.isCancelled()) {
			info.cancel();
		} else if (!type.equals(event.getType()) || !movement.equals(event.getVec())) {
			double double_1 = this.getX();
			double double_2 = this.getZ();
			super.move(event.getType(), event.getVec());
			this.autoJump((float) (this.getX() - double_1), (float) (this.getZ() - double_2));
			info.cancel();
		}
	}

	@Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
	private void pushOutOfBlocks(double x, double d, CallbackInfo ci) {
		if (ModuleManager.getModule(Freecam.class).isEnabled()) {
			ci.cancel();
		}
	}

	@Redirect(method = "updateNausea", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;closeHandledScreen()V", ordinal = 0),
			require = 0 /* TODO: inertia compatibility */)
	private void updateNausea_closeHandledScreen(ClientPlayerEntity player) {
		if (!ModuleManager.getModule(BetterPortal.class).isEnabled()
				|| !ModuleManager.getModule(BetterPortal.class).getSetting(0).asToggle().getState()) {
			closeHandledScreen();
		}
	}

	@Redirect(method = "updateNausea", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V", ordinal = 0),
			require = 0 /* TODO: inertia compatibility */)
	private void updateNausea_setScreen(MinecraftClient client, Screen screen) {
		if (!ModuleManager.getModule(BetterPortal.class).isEnabled()
				|| !ModuleManager.getModule(BetterPortal.class).getSetting(0).asToggle().getState()) {
			client.setScreen(screen);
		}
	}

	@Overwrite
	public void swingHand(Hand hand) {
		EventSwingHand event = new EventSwingHand(hand);
		BleachHack.eventBus.post(event);

		if (!event.isCancelled()) {
			super.swingHand(event.getHand());
		}

		networkHandler.sendPacket(new HandSwingC2SPacket(hand));
	}

	@Override
	protected boolean clipAtLedge() {
		return super.clipAtLedge()
				|| ModuleManager.getModule(SafeWalk.class).isEnabled()
				|| (ModuleManager.getModule(Scaffold.class).isEnabled()
						&& ModuleManager.getModule(Scaffold.class).getSetting(8).asToggle().getState());
	}

	@Overwrite
	public float getMountJumpStrength() {
		return ModuleManager.getModule(EntityControl.class).isEnabled()
				&& ModuleManager.getModule(EntityControl.class).getSetting(2).asToggle().getState() ? 1F : mountJumpStrength;
	}
}

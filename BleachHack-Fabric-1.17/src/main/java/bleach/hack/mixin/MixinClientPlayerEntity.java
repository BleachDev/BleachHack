/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventClientMove;
import bleach.hack.event.events.EventSendMovementPackets;
import bleach.hack.event.events.EventSwingHand;
import bleach.hack.module.ModuleManager;
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

	@Shadow public ClientPlayNetworkHandler networkHandler;
	@Shadow protected MinecraftClient client;

	public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
		super(world, profile);
	}

	@Shadow protected void autoJump(float dx, float dz) {}

	@Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
	public void sendMovementPackets(CallbackInfo info) {
		EventSendMovementPackets event = new EventSendMovementPackets();
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			info.cancel();
		}
	}

	@Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"),
			require = 0 /* TODO: meteor */)
	private boolean tickMovement_isUsingItem(ClientPlayerEntity player) {
		if (ModuleManager.getModule("NoSlow").isEnabled() && ModuleManager.getModule("NoSlow").getSetting(5).asToggle().state) {
			return false;
		}

		return player.isUsingItem();
	}

	@Inject(method = "move", at = @At("HEAD"), cancellable = true)
	public void move(MovementType type, Vec3d movement, CallbackInfo info) {
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
	protected void pushOutOfBlocks(double x, double d, CallbackInfo ci) {
		if (ModuleManager.getModule("Freecam").isEnabled()) {
			ci.cancel();
		}
	}

	@Redirect(method = "updateNausea", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;closeHandledScreen()V", ordinal = 0),
			require = 0 /* TODO: inertia */)
	private void updateNausea_closeHandledScreen(ClientPlayerEntity player) {
		if (!ModuleManager.getModule("BetterPortal").isEnabled()
				|| !ModuleManager.getModule("BetterPortal").getSetting(0).asToggle().state) {
			closeHandledScreen();
		}
	}

	@Redirect(method = "updateNausea", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V", ordinal = 0),
			require = 0 /* TODO: inertia */)
	private void updateNausea_setScreen(MinecraftClient client, Screen screen) {
		if (!ModuleManager.getModule("BetterPortal").isEnabled()
				|| !ModuleManager.getModule("BetterPortal").getSetting(0).asToggle().state) {
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
				|| ModuleManager.getModule("SafeWalk").isEnabled()
				|| (ModuleManager.getModule("Scaffold").isEnabled()
						&& ModuleManager.getModule("Scaffold").getSetting(8).asToggle().state);
	}

	@Overwrite
	public float getMountJumpStrength() {
		return ModuleManager.getModule("EntityControl").isEnabled()
				&& ModuleManager.getModule("EntityControl").getSetting(2).asToggle().state ? 1F : mountJumpStrength;
	}
}

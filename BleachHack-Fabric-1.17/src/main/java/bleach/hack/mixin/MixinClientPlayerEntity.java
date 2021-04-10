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
import bleach.hack.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

	@Shadow private float field_3922;

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
		} else if (!type.equals(event.type) || !movement.equals(event.vec3d)) {
			double double_1 = this.getX();
			double double_2 = this.getZ();
			super.move(event.type, event.vec3d);
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

	@Redirect(method = "updateNausea", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;openScreen(Lnet/minecraft/client/gui/screen/Screen;)V", ordinal = 0),
			require = 0 /* TODO: inertia */)
	private void updateNausea_openScreen(MinecraftClient player, Screen screen) {
		if (!ModuleManager.getModule("BetterPortal").isEnabled()
				|| !ModuleManager.getModule("BetterPortal").getSetting(0).asToggle().state) {
			client.openScreen(screen);
		}
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
				&& ModuleManager.getModule("EntityControl").getSetting(2).asToggle().state ? 1F : field_3922;
	}
}

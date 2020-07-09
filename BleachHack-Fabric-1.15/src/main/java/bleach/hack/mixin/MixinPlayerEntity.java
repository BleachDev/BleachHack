/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
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

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventClientMove;
import bleach.hack.event.events.EventMovementTick;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.NoSlow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.utils.BleachQueue;
import bleach.hack.utils.file.BleachFileHelper;

@Mixin(ClientPlayerEntity.class)
public class MixinPlayerEntity {
	
	@Inject(at = @At("RETURN"), method = "tick()V", cancellable = true)
	public void tick(CallbackInfo info) {
		try {
			if (MinecraftClient.getInstance().player.age % 100 == 0) {
				BleachFileHelper.saveModules();
				BleachFileHelper.saveSettings();
				BleachFileHelper.saveBinds();
				BleachFileHelper.saveClickGui();
			}
			
			BleachQueue.nextQueue();
		} catch (Exception e) {}
		EventTick event = new EventTick();
		BleachHack.eventBus.post(event);
		if (event.isCancelled()) info.cancel();
	}
	
	@Inject(at = @At("HEAD"), method = "sendMovementPackets()V", cancellable = true)
	public void sendMovementPackets(CallbackInfo info) {
		EventMovementTick event = new EventMovementTick();
		BleachHack.eventBus.post(event);
		if (event.isCancelled()) info.cancel();
	}
	
	@Redirect(method = "tickMovement()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
	private boolean tickMovement_isUsingItem(ClientPlayerEntity player) {
		if (ModuleManager.getModule(NoSlow.class).isToggled() && ModuleManager.getModule(NoSlow.class).getSettings().get(4).toToggle().state) {
			return false;
		}

		return player.isUsingItem();
	}
	
	@Inject(at = @At("HEAD"), method = "move", cancellable = true)
	public void move(MovementType movementType_1, Vec3d vec3d_1, CallbackInfo info) {
		EventClientMove event = new EventClientMove(vec3d_1);
		BleachHack.eventBus.post(event);
		if (event.isCancelled()) info.cancel();
	}

}


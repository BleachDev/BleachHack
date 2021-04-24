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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventPlayerPushed;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

@Mixin(Entity.class)
public class MixinEntity {
	
	@Shadow public void addVelocity(double deltaX, double deltaY, double deltaZ) {}

	@Redirect(method = "pushAwayFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
	private void pushAwayFrom_addVelocity(Entity entity, double deltaX, double deltaY, double deltaZ) {
		if (entity == MinecraftClient.getInstance().player) {
			EventPlayerPushed event = new EventPlayerPushed(new Vec3d(deltaX, deltaY, deltaZ));
			BleachHack.eventBus.post(event);
			
			addVelocity(event.getPush().x, event.getPush().y, event.getPush().z);
		}
	}
}

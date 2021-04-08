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

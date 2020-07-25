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

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Team;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventEntityRender;
import bleach.hack.event.events.EventOutlineColor;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity> {

	@Inject(at = @At("HEAD"), method = "getOutlineColor", cancellable = true)
	protected void getOutlineColor(T entity_1, CallbackInfoReturnable<Integer> ci) {
		Team team_1 = (Team) (entity_1).getScoreboardTeam();
		int col = team_1 != null && team_1.getColor().getColorValue() != null ? team_1.getColor().getColorValue() : 16777215;
		
		EventOutlineColor event = new EventOutlineColor(entity_1, col);
		BleachHack.eventBus.post(event);
		if (event.isCancelled()) {
			ci.setReturnValue(16777215);
			ci.cancel();
		} else if (event.color != col) {
			ci.setReturnValue(event.color);
			ci.cancel();
		}
	}

	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	public void render(T entity_1, double double_1, double double_2, double double_3, float float_1, float float_2, CallbackInfo info) {
		EventEntityRender event = new EventEntityRender(entity_1);
		BleachHack.eventBus.post(event);
		if (event.isCancelled()) info.cancel();
	}
}

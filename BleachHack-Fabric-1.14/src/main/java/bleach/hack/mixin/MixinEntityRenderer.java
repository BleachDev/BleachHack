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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventEntityRender;
import bleach.hack.event.events.EventOutlineColor;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Team;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity> {

	/**
	 * fuck javadoc
	 * 
	 * @param entity_1
	 * @reason fuck
	 * @author bleach
	 * @return
	 */
	@Overwrite
	public int getOutlineColor(T entity_1) {
		Team team_1 = (Team) (entity_1).getScoreboardTeam();
		int col = team_1 != null && team_1.getColor().getColorValue() != null ? team_1.getColor().getColorValue() : 16777215;

		EventOutlineColor event = new EventOutlineColor(entity_1, col);
		BleachHack.eventBus.post(event);
		if (event.isCancelled()) {
			return 16777215;
		}

		return event.color;
	}

	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	public void render(T entity_1, double double_1, double double_2, double double_3, float float_1, float float_2, CallbackInfo info) {
		EventEntityRender.Render event = new EventEntityRender.Render(entity_1);
		BleachHack.eventBus.post(event);
		if (event.isCancelled())
			info.cancel();
	}

	@Inject(at = @At("HEAD"), method = "renderLabel(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V", cancellable = true)
	public void renderLabel(T entity_1, String string_1, double double_1, double double_2, double double_3, int int_1, CallbackInfo info) {
		EventEntityRender.Label event = new EventEntityRender.Label(entity_1);
		BleachHack.eventBus.post(event);
		if (event.isCancelled())
			info.cancel();
	}
}

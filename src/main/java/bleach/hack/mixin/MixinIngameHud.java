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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventDrawOverlay;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.NoRender;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(InGameHud.class)
public class MixinIngameHud {

	@Inject(at = @At(value = "RETURN"), method = "render", cancellable = true)
	public void render(MatrixStack matrixStack, float float_1, CallbackInfo info) {
		EventDrawOverlay event = new EventDrawOverlay(matrixStack);
		BleachHack.eventBus.post(event);
		if (event.isCancelled())
			info.cancel();
	}

	@Inject(at = @At("HEAD"), method = "renderPumpkinOverlay()V", cancellable = true)
	private void onRenderPumpkinOverlay(CallbackInfo ci) {
		if (ModuleManager.getModule(NoRender.class).isToggled() && ModuleManager.getModule(NoRender.class).getSetting(4).asToggle().state)
			ci.cancel();
	}
}

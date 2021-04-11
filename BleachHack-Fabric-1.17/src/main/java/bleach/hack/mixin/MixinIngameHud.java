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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventDrawOverlay;
import bleach.hack.event.events.EventRenderOverlay;
import bleach.hack.gui.EntityMenuScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Mixin(InGameHud.class)
public class MixinIngameHud {
	@Unique private boolean bypassRenderOverlay = false;
	
	@Shadow private void renderOverlay(Identifier texture, float opacity) {}

	@Inject(method = "render", at = @At("RETURN"), cancellable = true)
	public void render(MatrixStack matrixStack, float tickDelta, CallbackInfo info) {
		EventDrawOverlay event = new EventDrawOverlay(matrixStack);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			info.cancel();
		}
	}

	@Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
	private void renderOverlay(Identifier texture, float opacity, CallbackInfo ci) {
		if (!bypassRenderOverlay) {
			EventRenderOverlay event = new EventRenderOverlay(texture, opacity);
			BleachHack.eventBus.post(event);

			if (event.isCancelled()) {
				ci.cancel();
				return;
			}

			bypassRenderOverlay = true;
			renderOverlay(event.getTexture(), event.getOpacity());
			bypassRenderOverlay = false;
		}
	}
	
	
	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	private void renderCrosshair(MatrixStack matrices, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.currentScreen instanceof EntityMenuScreen) {
			ci.cancel();
		}
	}
}

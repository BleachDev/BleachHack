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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventDrawInGameHud;
import bleach.hack.event.events.EventRenderOverlay;
import bleach.hack.gui.EntityMenuScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Mixin(InGameHud.class)
public class MixinInGameHud {
	@Unique private boolean bypassRenderOverlay = false;
	
	@Shadow private void renderOverlay(Identifier texture, float opacity) {}

	@Inject(method = "render", at = @At("RETURN"), cancellable = true)
	public void render(MatrixStack matrixStack, float tickDelta, CallbackInfo info) {
		EventDrawInGameHud event = new EventDrawInGameHud(matrixStack);
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

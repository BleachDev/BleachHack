/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import java.util.List;

import org.bleachhack.BleachHack;
import org.bleachhack.event.events.EventRenderScreenBackground;
import org.bleachhack.event.events.EventRenderTooltip;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(Screen.class)
public class MixinScreen {

	@Unique private int lastMX;
	@Unique private int lastMY;

	@Unique private boolean skipTooltip;

	@Shadow private void renderTooltipFromComponents(MatrixStack matrices, List<TooltipComponent> components, int x, int y) {}

	@Inject(method = "render", at = @At("HEAD"))
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo callback) {
		lastMX = mouseX;
		lastMY = mouseY;
	}

	@Inject(method = "renderTooltipFromComponents", at = @At("HEAD"), cancellable = true)
	private void renderTooltipFromComponents(MatrixStack matrices, List<TooltipComponent> components, int x, int y, CallbackInfo callback) {
		if (skipTooltip) {
			skipTooltip = false;
			return;
		}

		EventRenderTooltip event = new EventRenderTooltip((Screen) (Object) this, matrices, components, x, y, lastMX, lastMY);
		BleachHack.eventBus.post(event);

		callback.cancel();
		if (event.isCancelled()) {
			return;
		}

		skipTooltip = true;
		renderTooltipFromComponents(event.getMatrix(), event.getComponents(), event.getX(), event.getY());
	}

	@Inject(method = "renderBackground(Lnet/minecraft/client/util/math/MatrixStack;I)V", at = @At("HEAD"), cancellable = true)
	private void renderBackground(MatrixStack matrices, int vOffset, CallbackInfo callback) {
		EventRenderScreenBackground event = new EventRenderScreenBackground(matrices, vOffset);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			callback.cancel();
		}
	}
}

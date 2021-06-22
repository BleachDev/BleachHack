/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.mixin;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventRenderTooltip;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Mixin(Screen.class)
public class MixinScreen {

	@Unique private int lastMX;
	@Unique private int lastMY;

	@Unique private boolean skipTooltip;

	@Inject(method = "render", at = @At("HEAD"))
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo callback) {
		lastMX = mouseX;
		lastMY = mouseY;
	}

	@Inject(at = @At("HEAD"), method = "renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Ljava/util/List;Ljava/util/Optional;II)V", cancellable = true)
	public void renderTooltip(MatrixStack matrices, List<Text> lines, Optional<TooltipData> data, int x, int y, CallbackInfo callback) {
		if (skipTooltip) {
			skipTooltip = false;
			return;
		}

		EventRenderTooltip event = new EventRenderTooltip((Screen) (Object) this, matrices, lines, x, y, lastMX, lastMY);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			callback.cancel();
		} else if (!event.getText().equals(lines) || event.getX() != x || event.getY() != y) {
			skipTooltip = true;
			((Screen) (Object) this).renderTooltip(matrices, lines, data, x, y);
			callback.cancel();
		}
	}
}

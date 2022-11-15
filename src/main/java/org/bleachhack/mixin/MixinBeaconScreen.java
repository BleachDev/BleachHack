/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ingame.BeaconScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.BeaconScreenHandler;

import net.minecraft.text.Text;

@Mixin(BeaconScreen.class)
public abstract class MixinBeaconScreen extends HandledScreen<BeaconScreenHandler> {

	@Unique private boolean unlocked = false;

	private MixinBeaconScreen(BeaconScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void init(CallbackInfo callback) {
		addDrawableChild(new ButtonWidget((width - backgroundWidth) / 2 + 2, (height - backgroundHeight) / 2 - 15, 46, 14, Text.literal("Unlock"), button -> unlocked = true));
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo callback) {
		if (unlocked) {
			for (Drawable b: ((AccessorScreen) this).getDrawables()) {
				if (b instanceof ClickableWidget) {
					((ClickableWidget) b).active = true;
				}
			}
		}
	}
}

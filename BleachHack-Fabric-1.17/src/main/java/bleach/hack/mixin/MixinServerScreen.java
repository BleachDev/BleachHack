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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.gui.CleanUpScreen;
import bleach.hack.gui.ProtocolScreen;
import bleach.hack.gui.ServerScraperScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

@Mixin(MultiplayerScreen.class)
public class MixinServerScreen extends Screen {

	protected MixinServerScreen(Text title) {
		super(title);
	}

	@Inject(method = "init()V", at = @At("HEAD"))
	private void init(CallbackInfo info) {
		addDrawableChild(new ButtonWidget(5, 7, 50, 20, new LiteralText("Scraper"), button -> {
			client.setScreen(new ServerScraperScreen((MultiplayerScreen) client.currentScreen));
		}));
		addDrawableChild(new ButtonWidget(58, 7, 50, 20, new LiteralText("Cleanup"), button -> {
			client.setScreen(new CleanUpScreen((MultiplayerScreen) client.currentScreen));
		}));
		addDrawableChild(new ButtonWidget(111, 7, 50, 20, new LiteralText("Protocol"), button -> {
			client.setScreen(new ProtocolScreen((MultiplayerScreen) client.currentScreen));
		}));
	}
}

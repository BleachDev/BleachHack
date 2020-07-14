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

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.gui.CleanUpScreen;
import bleach.hack.gui.ProtocolScreen;
import bleach.hack.gui.ServerScraperScreen;

@Mixin(MultiplayerScreen.class)
public class MixinServerScreen extends Screen {
	
	protected MixinServerScreen(Text text_1) {
		super(text_1);
	}

	@Inject(at = @At("HEAD"), method = "init()V")
	private void init(CallbackInfo info) {
		addButton(new ButtonWidget(5, 7, 50, 20, "Scraper", button -> {
			minecraft.openScreen(new ServerScraperScreen((MultiplayerScreen) minecraft.currentScreen));
		}));
		addButton(new ButtonWidget(58, 7, 50, 20, "Cleanup", button -> {
			minecraft.openScreen(new CleanUpScreen((MultiplayerScreen) minecraft.currentScreen));
		}));
		addButton(new ButtonWidget(111, 7, 50, 20, "Protocol", button -> {
			minecraft.openScreen(new ProtocolScreen((MultiplayerScreen) minecraft.currentScreen));
		}));
	}
}

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
package bleach.hack.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ScreenInjector {

	@SubscribeEvent
	public void initGui(InitGuiEvent.Pre event) {
		if (!(Minecraft.getInstance().currentScreen instanceof MainMenuScreen)) return;
		
		Minecraft.getInstance().displayGuiScreen(new BleachMainMenu());
	}
	
	@SubscribeEvent
	public void initGui(InitGuiEvent.Post event) {
		if (!(Minecraft.getInstance().currentScreen instanceof MultiplayerScreen)) return;
		
		event.addWidget(new Button(5, 7, 50, 18, "Scraper", button -> {
			Minecraft.getInstance().displayGuiScreen(new ServerScraperScreen((MultiplayerScreen) event.getGui()));
		}));
		event.addWidget(new Button(58, 7, 50, 18, "Cleanup", button -> {
			Minecraft.getInstance().displayGuiScreen(new CleanUpScreen((MultiplayerScreen) event.getGui()));
		}));
	}
}

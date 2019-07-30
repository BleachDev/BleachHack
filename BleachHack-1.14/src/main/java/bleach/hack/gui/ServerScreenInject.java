package bleach.hack.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerScreenInject {

	@SubscribeEvent
	public void initGui(InitGuiEvent.Post event) {
		if(!(Minecraft.getInstance().currentScreen instanceof MultiplayerScreen)) return;
			
		System.out.println(event.getWidgetList());
		event.addWidget(new Button(5, 7, 50, 18, "Scraper", button -> {
			Minecraft.getInstance().displayGuiScreen(new ServerScraperScreen((MultiplayerScreen) event.getGui()));
		}));
	}
}

package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.client.gui.screen.Screen;

public class EventOpenScreen extends Event {

	private Screen screen;
	
	public EventOpenScreen(Screen screen) {
		this.screen = screen;
	}
	
	public Screen getScreen() {
		return screen;
	}
}

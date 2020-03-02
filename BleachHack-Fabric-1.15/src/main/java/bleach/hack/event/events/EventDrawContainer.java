package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;

public class EventDrawContainer extends Event {

	private ContainerScreen<?> screen;
	public int mouseX;
	public int mouseY;
	
	public EventDrawContainer(ContainerScreen<?> screen, int mX, int mY) {
		this.screen = screen;
		this.mouseX = mX;
		this.mouseY = mY;
	}
	
	public Screen getScreen() {
		return screen;
	}
}

package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;

public class EventDrawContainer extends Event {

	private AbstractContainerScreen<?> screen;
	public int mouseX;
	public int mouseY;
	
	public EventDrawContainer(AbstractContainerScreen<?> screen, int mX, int mY) {
		this.screen = screen;
		this.mouseX = mX;
		this.mouseY = mY;
	}
	
	public Screen getScreen() {
		return screen;
	}
}

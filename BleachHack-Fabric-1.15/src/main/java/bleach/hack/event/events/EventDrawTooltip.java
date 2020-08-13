package bleach.hack.event.events;

import java.util.List;

import bleach.hack.event.Event;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public class EventDrawTooltip extends Event {

	public Screen screen;
	public List<String> text;
	public int x;
	public int y;
	public int mX;
	public int mY;

	public EventDrawTooltip(List<String> text, int x, int y, int mX, int mY) {
		screen = MinecraftClient.getInstance().currentScreen;
		this.text = text;
		this.x = x;
		this.y = y;
		this.mX = mX;
		this.mY = mY;
	}

}

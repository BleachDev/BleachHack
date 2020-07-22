package bleach.hack.event.events;

import java.util.List;

import bleach.hack.event.Event;
import net.minecraft.class_5481;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

public class EventDrawTooltip extends Event {

	public Screen screen;
	public MatrixStack matrix;
	public List<? extends class_5481> text;
	public int x;
	public int y;
	public int mX;
	public int mY;

	public EventDrawTooltip(MatrixStack matrix, List<? extends class_5481> text, int x, int y, int mX, int mY) {
		this.matrix = matrix;
		screen = MinecraftClient.getInstance().currentScreen;
		this.text = text;
		this.x = x;
		this.y = y;
		this.mX = mX;
		this.mY = mY;
	}

}

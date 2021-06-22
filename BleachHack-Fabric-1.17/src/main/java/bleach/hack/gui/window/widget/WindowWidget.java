package bleach.hack.gui.window.widget;

import java.util.function.Consumer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

public abstract class WindowWidget {

	protected static MinecraftClient mc = MinecraftClient.getInstance();

	public int x1;
	public int y1;
	public int x2;
	public int y2;
	public boolean visible = true;

	protected Consumer<WindowWidget> renderEvent;
	protected Consumer<WindowWidget> hoverEvent;
	protected Consumer<WindowWidget> clickEvent;
	protected Consumer<WindowWidget> releaseEvent;

	public WindowWidget(int x1, int y1, int x2, int y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	public void render(MatrixStack matrices, int windowX, int windowY, int mouseX, int mouseY) {
		if (renderEvent != null) {
			renderEvent.accept(this);
		}

		if (hoverEvent != null && isInBounds(windowX, windowY, mouseX, mouseY)) {
			hoverEvent.accept(this);
		}
	}

	public void mouseClicked(int windowX, int windowY, int mouseX, int mouseY, int button) {
		if (clickEvent != null && isInBounds(windowX, windowY, mouseX, mouseY)) {
			clickEvent.accept(this);
		}
	}
	
	public void mouseReleased(int windowX, int windowY, int mouseX, int mouseY, int button) {
		if (releaseEvent != null && isInBounds(windowX, windowY, mouseX, mouseY)) {
			releaseEvent.accept(this);
		}
	}
	
	public void tick() {
	}

	public void charTyped(char chr, int modifiers) {
	}

	public void keyPressed(int keyCode, int scanCode, int modifiers) {
	}
	
	protected boolean isInBounds(int windowX, int windowY, int x, int y) {
		return x >= windowX + x1 && y >= windowY + y1 && x <= windowX + x2 && y <= windowY + y2;
	}
	
	public WindowWidget withRenderEvent(Consumer<WindowWidget> consumer) {
		renderEvent = renderEvent == null ? renderEvent = consumer : renderEvent.andThen(consumer);
		return this;
	}

	public WindowWidget withHoverEvent(Consumer<WindowWidget> consumer) {
		hoverEvent = hoverEvent == null ? hoverEvent = consumer : hoverEvent.andThen(consumer);
		return this;
	}

	public WindowWidget withClickEvent(Consumer<WindowWidget> consumer) {
		clickEvent = clickEvent == null ? clickEvent = consumer : clickEvent.andThen(consumer);
		return this;
	}
	
	public WindowWidget withReleaseEvent(Consumer<WindowWidget> consumer) {
		releaseEvent = releaseEvent == null ? releaseEvent = consumer : releaseEvent.andThen(consumer);
		return this;
	}
}

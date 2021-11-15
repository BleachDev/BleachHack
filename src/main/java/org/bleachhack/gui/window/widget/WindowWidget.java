package org.bleachhack.gui.window.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

public abstract class WindowWidget {

	protected static MinecraftClient mc = MinecraftClient.getInstance();

	public int x1;
	public int y1;
	public int x2;
	public int y2;

	public boolean visible = true;
	public boolean cullX;
	public boolean cullY;

	protected RenderEvent renderEvent;
	protected RenderEvent hoverEvent;
	protected MouseEvent clickEvent;
	protected MouseEvent releaseEvent;

	public WindowWidget(int x1, int y1, int x2, int y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	public void render(MatrixStack matrices, int windowX, int windowY, int mouseX, int mouseY) {
		if (renderEvent != null) {
			renderEvent.accept(this, matrices, windowX, windowY);
		}

		if (hoverEvent != null && isInBounds(windowX, windowY, mouseX, mouseY)) {
			hoverEvent.accept(this, matrices, windowX, windowY);
		}
	}

	public void mouseClicked(int windowX, int windowY, int mouseX, int mouseY, int button) {
		if (clickEvent != null && isInBounds(windowX, windowY, mouseX, mouseY)) {
			clickEvent.accept(this, mouseX, mouseY, windowX, windowY);
		}
	}

	public void mouseReleased(int windowX, int windowY, int mouseX, int mouseY, int button) {
		if (releaseEvent != null && isInBounds(windowX, windowY, mouseX, mouseY)) {
			releaseEvent.accept(this, mouseX, mouseY, windowX, windowY);
		}
	}

	public void tick() {
	}

	public void charTyped(char chr, int modifiers) {
	}

	public void keyPressed(int keyCode, int scanCode, int modifiers) {
	}

	public boolean isInBounds(int windowX, int windowY, int x, int y) {
		return x >= windowX + x1 && y >= windowY + y1 && x <= windowX + x2 && y <= windowY + y2;
	}

	public boolean shouldRender(int windowX1, int windowY1, int windowX2, int windowY2) {
		return visible && (!cullX || (x1 >= 0 && x2 <= windowX2 - windowX1)) && (!cullY || (y1 >= 12 && y2 <= windowY2 - windowY1 + 1));
	}

	public WindowWidget withRenderEvent(RenderEvent event) {
		renderEvent = event;
		return this;
	}

	public WindowWidget withHoverEvent(RenderEvent event) {
		hoverEvent = event;
		return this;
	}

	public WindowWidget withClickEvent(MouseEvent event) {
		clickEvent = event;
		return this;
	}

	public WindowWidget withReleaseEvent(MouseEvent event) {
		releaseEvent = event;
		return this;
	}
	
	@FunctionalInterface
	public interface RenderEvent {
		public void accept(WindowWidget widget, MatrixStack matrices, int wx, int wy);
	}
	
	@FunctionalInterface
	public interface MouseEvent {
		public void accept(WindowWidget widget, int mx, int my, int wx, int wy);
	}
}

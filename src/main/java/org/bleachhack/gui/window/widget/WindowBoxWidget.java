package org.bleachhack.gui.window.widget;

import org.bleachhack.gui.window.Window;

import net.minecraft.client.util.math.MatrixStack;

public class WindowBoxWidget extends WindowWidget {

	private int topColor;
	private int bottomColor;
	private int fillColor;
	private int hoverColor;

	public WindowBoxWidget(int x1, int y1, int x2, int y2) {
		this(x1, y1, x2, y2, 0x00000000);
	}

	public WindowBoxWidget(int x1, int y1, int x2, int y2, int fill) {
		this(x1, y1, x2, y2, 0xff6060b0, 0xff8070b0, fill);
	}

	public WindowBoxWidget(int x1, int y1, int x2, int y2, int top, int bottom, int fill) {
		this(x1, y1, x2, y2, top, bottom, fill, fill);
	}

	public WindowBoxWidget(int x1, int y1, int x2, int y2, int top, int bottom, int fill, int hover) {
		super(x1, y1, x2, y2);
		topColor = top;
		bottomColor = bottom;
		fillColor = fill;
		hoverColor = hover;
	}

	@Override
	public void render(MatrixStack matrices, int windowX, int windowY, int mouseX, int mouseY) {
		super.render(matrices, windowX, windowY, mouseX, mouseY);

		Window.fill(matrices, windowX + x1, windowY + y1, windowX + x2, windowY + y2,
				topColor, bottomColor, isInBounds(windowX, windowY, mouseX, mouseY) ? hoverColor : fillColor);
	}
}

package bleach.hack.gui.clickgui.modulewindow;

import org.apache.commons.lang3.tuple.Triple;

import bleach.hack.gui.window.Window;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.item.ItemStack;

public abstract class ClickGuiWindow extends Window {

	protected MinecraftClient mc;

	protected int mouseX;
	protected int mouseY;

	protected int keyDown = -1;
	protected boolean lmDown = false;
	protected boolean rmDown = false;
	protected boolean lmHeld = false;

	public ClickGuiWindow(int x1, int y1, int x2, int y2, String title, ItemStack icon) {
		super(x1, y1, x2, y2, title, icon);
		mc = MinecraftClient.getInstance();
	}

	public boolean shouldClose(int mX, int mY) {
		return false;
	}

	protected void drawBar(int mX, int mY, TextRenderer textRend) {
		/* background and title bar */
		fillGrey(x1, y1, x2, y2);
		fillGradient(x1 + 1, y1 + 1, x2 - 2, y1 + 12, 0xff0000ff, 0xff4080ff);
	}

	public void fillGrey(int x1, int y1, int x2, int y2) {
		DrawableHelper.fill(x1, y1, x1 + 1, y2 - 1, 0x90b0b0b0);
		DrawableHelper.fill(x1 + 1, y1, x2 - 1, y1 + 1, 0x90b0b0b0);
		DrawableHelper.fill(x1 + 1, y2 - 1, x2, y2, 0x90000000);
		DrawableHelper.fill(x2 - 1, y1 + 1, x2, y2, 0x90000000);
		DrawableHelper.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xff505059);
	}

	protected boolean mouseOver(int minX, int minY, int maxX, int maxY) {
		return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY < maxY;
	}

	public Triple<Integer, Integer, String> getTooltip() {
		return null;
	}

	public void updateKeys(int mouseX, int mouseY, int keyDown, boolean lmDown, boolean rmDown, boolean lmHeld) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.keyDown = keyDown;
		this.lmDown = lmDown;
		this.rmDown = rmDown;
		this.lmHeld = lmHeld;
	}
}

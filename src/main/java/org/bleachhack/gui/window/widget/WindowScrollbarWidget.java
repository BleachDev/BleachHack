package org.bleachhack.gui.window.widget;

import org.bleachhack.gui.window.Window;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class WindowScrollbarWidget extends WindowWidget {

	private int totalHeight;
	private int pageHeight;
	private int pageOffset;
	private int lastRenderOffset;

	private boolean buttonDown;
	private int lastY;

	public WindowScrollbarWidget(int x, int y, int totalHeight, int pageHeight, int pageOffset) {
		super(x, y, x + 10, y + pageHeight);
		this.totalHeight = totalHeight;
		this.pageHeight = pageHeight;

		setPageOffset(pageOffset);
		this.lastRenderOffset = getPageOffset();
	}

	@Override
	public void render(MatrixStack matrices, int windowX, int windowY, int mouseX, int mouseY) {
		super.render(matrices, windowX, windowY, mouseX, mouseY);

		int[] pos = getCurrentPos(windowX, windowY);
		lastRenderOffset = getPageOffset();

		//System.out.println(getScrollbarHeight());
		if (pos != null) {
			int color = mouseX >= pos[0] && mouseX <= pos[0] + pos[2] && mouseY >= pos[1] && mouseY <= pos[1] + pos[3] ? 0x9025e635 : 0x9025e635;

			Window.fill(matrices, pos[0], pos[1], pos[0] + pos[2], pos[1] + pos[3], color);

			int middleY = pos[1] + (int) (pos[3] / 2d);

			DrawableHelper.fill(matrices, pos[0] + 3, middleY - 3, pos[0] + 7, middleY - 2, 0xff4fff5e);
			DrawableHelper.fill(matrices, pos[0] + 3, middleY, pos[0] + 7, middleY + 1, 0xff4fff5e);
			DrawableHelper.fill(matrices, pos[0] + 3, middleY + 3, pos[0] + 7, middleY + 4, 0xff4fff5e);

			if (buttonDown) {
				moveScrollbar(mouseY - lastY);
				lastY = mouseY;
			}
		}
	}

	@Override
	public void mouseClicked(int windowX, int windowY, int mouseX, int mouseY, int button) {
		super.mouseClicked(windowX, windowY, mouseX, mouseY, button);

		int[] pos = getCurrentPos(windowX, windowY);

		if (pos != null && mouseX >= pos[0] && mouseX <= pos[0] + pos[2] && mouseY >= pos[1] && mouseY <= pos[1] + pos[3]) {
			buttonDown = true;
			lastY = mouseY;
		}
	}

	@Override
	public void mouseReleased(int windowX, int windowY, int mouseX, int mouseY, int button) {
		buttonDown = false;
	}

	public void scroll(double amount) {
		setPageOffset(getPageOffset() + (int) Math.round(-amount * 20));
	}

	/* [x, y, w, h] */
	public int[] getCurrentPos(int windowX, int windowY) {
		if (totalHeight <= pageHeight) {
			return null;
		}

		int bottom = pageHeight - getScrollbarHeight();

		return new int[] {
				windowX + x1,
				windowY + y1 + (int) (bottom * (pageOffset / (double) (totalHeight - pageHeight))),
				10,
				getScrollbarHeight()
		};
	}

	private int getScrollbarHeight() {
		if (totalHeight <= pageHeight) {
			return 0;
		}

		return (int) Math.max(25, pageHeight * (pageHeight / (double) totalHeight));
	}
	
	public int getOffsetSinceRender() {
		return getPageOffset() - lastRenderOffset;
	}

	public int getTotalHeight() {
		return totalHeight;
	}

	public void setTotalHeight(int totalHeight) {
		this.totalHeight = totalHeight;
		setPageOffset(getPageOffset());
	}

	public int getPageOffset() {
		return pageOffset;
	}

	public void setPageOffset(int offset) {
		pageOffset = totalHeight - pageHeight <= 0 ? 0 : MathHelper.clamp(offset, 0, totalHeight - pageHeight);
	}

	public void moveScrollbar(int yDiff) {
		setPageOffset(pageOffset + (int) (yDiff * ((totalHeight - getScrollbarHeight() / 2.2) / (pageHeight - getScrollbarHeight() / 2.2))));
	}
}

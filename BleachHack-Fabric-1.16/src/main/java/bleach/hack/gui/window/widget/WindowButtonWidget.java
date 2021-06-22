package bleach.hack.gui.window.widget;

import bleach.hack.gui.window.Window;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

public class WindowButtonWidget extends WindowWidget {

	public int colorTop;
	public int colorBottom;
	public int colorFill;
	public int colorHoverFill;

	public String text;
	public Runnable action;

	public WindowButtonWidget(int x1, int y1, int x2, int y2, String text, Runnable action) {
		this(x1, y1, x2, y2, 0x60606090, 0x4fb070f0, text, action);
	}

	public WindowButtonWidget(int x1, int y1, int x2, int y2, int colorFill, int colorHoverFill, String text, Runnable action) {
		this(x1, y1, x2, y2, 0xff6060b0, 0xff8070b0, colorFill, colorHoverFill, text, action);
	}

	public WindowButtonWidget(int x1, int y1, int x2, int y2, int colorTop, int colorBottom, int colorFill, int colorHoverFill, String text, Runnable action) {
		super(x1, y1, x2, y2);
		this.colorTop = colorTop;
		this.colorBottom = colorBottom;
		this.colorFill = colorFill;
		this.colorHoverFill = colorHoverFill;
		this.text = text;
		this.action = action;
	}

	@Override
	public void render(MatrixStack matrices, int windowX, int windowY, int mouseX, int mouseY) {
		super.render(matrices, windowX, windowY, mouseX, mouseY);

		int bx1 = windowX + x1;
		int by1 = windowY + y1;
		int bx2 = windowX + x2;
		int by2 = windowY + y2;

		Window.fill(matrices,
				bx1, by1, bx2, by2,
				isInBounds(windowX, windowY, mouseX, mouseY) ? colorHoverFill : colorFill);

		mc.textRenderer.drawWithShadow(
				matrices, text, bx1 + (bx2 - bx1) / 2 - mc.textRenderer.getWidth(text) / 2, by1 + (by2 - by1) / 2 - 4, -1);
	}

	@Override
	public void mouseClicked(int windowX, int windowY, int mouseX, int mouseY, int button) {
		super.mouseClicked(windowX, windowY, mouseX, mouseY, button);

		if (mouseX >= windowX + x1 && mouseX <= windowX + x2 && mouseY >= windowY + y1 && mouseY <= windowY + y2) {
			action.run();
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		}
	}
}

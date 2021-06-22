package bleach.hack.gui.window.widget;

import bleach.hack.gui.window.Window;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class WindowCheckboxWidget extends WindowWidget {

	public boolean checked;
	public Text text;
	
	public WindowCheckboxWidget(int x, int y, String text, boolean pressed) {
		this(x, y, new LiteralText(text), pressed);
	}

	public WindowCheckboxWidget(int x, int y, Text text, boolean pressed) {
		super(x, y, 10 + MinecraftClient.getInstance().textRenderer.getWidth(text), 10);
		this.checked = pressed;
		this.text = text;
	}

	@Override
	public void render(MatrixStack matrices, int windowX, int windowY, int mouseX, int mouseY) {
		super.render(matrices, windowX, windowY, mouseX, mouseY);

		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

		int x = windowX + x1;
		int y = windowY + y1;
		int color = mouseX >= x && mouseX <= x + 10 && mouseY >= y && mouseY <= y + 10 ? 0x906060ff : 0x9040409f;

		Window.fill(matrices, x, y, x + 11, y + 11, color);

		if (checked) {
			textRenderer.draw(matrices, "\u2714", x + 2, y + 2, 0xffeeff);
			//fill(matrix, x + 3, y + 3, x + 7, y + 7, 0xffffffff);
		}

		textRenderer.drawWithShadow(matrices, text, x + 15, y + 2, 0xc0c0c0);
	}

	@Override
	public void mouseClicked(int windowX, int windowY, int mouseX, int mouseY, int button) {
		super.mouseClicked(windowX, windowY, mouseX, mouseY, button);

		if (mouseX >= windowX + x1 && mouseX <= windowX + x1 + 10 && mouseY >= windowY + y1 && mouseY <= windowY + y1 + 10) {
			checked = !checked;
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		}
	}
}

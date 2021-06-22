package bleach.hack.gui.window.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class WindowTextFieldWidget extends WindowWidget {

	public TextFieldWidget textField;
	
	public WindowTextFieldWidget(int x, int y, int width, int height, String text) {
		this(x, y, width, height, new LiteralText(text));
	}

	public WindowTextFieldWidget(int x, int y, int width, int height, Text text) {
		super(x, y, width, height);
		this.textField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x, y, width, height, text);
	}

	@Override
	public void render(MatrixStack matrices, int windowX, int windowY, int mouseX, int mouseY) {
		super.render(matrices, windowX, windowY, mouseX, mouseY);

		textField.x = windowX + x1;
		textField.y = windowY + y1;
		textField.render(matrices, mouseX, mouseY, MinecraftClient.getInstance().getTickDelta());
	}

	@Override
	public void mouseClicked(int windowX, int windowY, int mouseX, int mouseY, int button) {
		super.mouseClicked(windowX, windowY, mouseX, mouseY, button);

		textField.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		textField.tick();
	}

	@Override
	public void charTyped(char chr, int modifiers) {
		super.charTyped(chr, modifiers);
		
		textField.charTyped(chr, modifiers);
	}

	@Override
	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		super.keyPressed(keyCode, scanCode, modifiers);
		
		textField.keyPressed(keyCode, scanCode, modifiers);
	}
}

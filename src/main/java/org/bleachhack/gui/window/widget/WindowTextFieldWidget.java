package org.bleachhack.gui.window.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class WindowTextFieldWidget extends WindowWidget {

	public TextFieldWidget textField;

	public WindowTextFieldWidget(int x, int y, int width, int height, String text) {
		super(x, y, x + width, y + height);
		this.textField = new TextFieldWidget(mc.textRenderer, x, y, width, height, LiteralText.EMPTY);
		this.textField.setText(text);
		this.textField.setMaxLength(32767);
	}

	protected WindowTextFieldWidget(int x, int y, int width, int height) {
		super(x, y, x + width, y + height);
	}

	@Override
	public void render(MatrixStack matrices, int windowX, int windowY, int mouseX, int mouseY) {
		textField.x = windowX + x1;
		textField.y = windowY + y1;
		textField.render(matrices, mouseX, mouseY, MinecraftClient.getInstance().getTickDelta());

		super.render(matrices, windowX, windowY, mouseX, mouseY);
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

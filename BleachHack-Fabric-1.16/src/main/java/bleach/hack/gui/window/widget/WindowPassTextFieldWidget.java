package bleach.hack.gui.window.widget;

import java.lang.reflect.Field;

import bleach.hack.util.BleachLogger;
import bleach.hack.util.FabricReflect;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class WindowPassTextFieldWidget extends WindowTextFieldWidget {

	private static final Field TEXT_FIELD = FabricReflect.getField(TextFieldWidget.class, "field_2092", "text");

	public WindowPassTextFieldWidget(int x, int y, int width, int height, String text) {
		super(x, y, width, height, text);
	}

	public WindowPassTextFieldWidget(int x, int y, int width, int height, Text text) {
		super(x, y, width, height, text);
	}

	@Override
	public void render(MatrixStack matrices, int windowX, int windowY, int mouseX, int mouseY) {
		String realText = textField.getText();

		try {
			TEXT_FIELD.set(textField, new String(new char[realText.length()]).replace("\0", "\u2022"));
			super.render(matrices, windowX, windowY, mouseX, mouseY);
			TEXT_FIELD.set(textField, realText);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			BleachLogger.logger.error("Error reflecting TextFieldWidget text field");
		}
	}
}

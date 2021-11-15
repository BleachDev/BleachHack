package org.bleachhack.gui.window.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Matrix4f;

public class WindowPassTextFieldWidget extends WindowTextFieldWidget {

	public WindowPassTextFieldWidget(int x, int y, int width, int height, String text) {
		super(x, y, width, height);
		this.textField = new TextFieldWidget(createTextRenderer(), x, y, width, height, LiteralText.EMPTY);
		this.textField.setText(text);
		this.textField.setMaxLength(32767);
	}

	private TextRenderer createTextRenderer() {
		return new TextRenderer(mc.textRenderer.fontStorageAccessor) {
			@Override
			public int draw(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light, boolean rightToLeft) {
				return super.draw(hide(text), x, y, color, shadow, matrix, vertexConsumers, seeThrough, backgroundColor, light, rightToLeft);
			}

			@Override
			public String trimToWidth(String text, int maxWidth) {
				return super.trimToWidth(hide(text), maxWidth);
			}

			@Override
			public int getWidth(String text) {
				return super.getWidth(hide(text));
			}

			private String hide(String text) {
				if (text != textField.getText()) // literal equal becasue dabruh
					return text;

				return new String(new char[textField.getText().length()]).replace('\0', '\u2022');
			}
		};
	}
}

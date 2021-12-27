package org.bleachhack.setting.option;

import java.util.function.Function;

import org.bleachhack.gui.window.widget.WindowTextFieldWidget;
import org.bleachhack.gui.window.widget.WindowWidget;
import org.bleachhack.setting.SettingDataHandlers;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class OptionString extends Option<String> {

	protected Function<String, Boolean> validator;
	protected String lastValidValue;

	public OptionString(String name, String tooltip, String value) {
		super(name, tooltip, value, SettingDataHandlers.STRING);
	}

	public OptionString(String name, String tooltip, String value, Function<String, Boolean> validator) {
		this(name, tooltip, value);
		this.lastValidValue = value;
		this.validator = validator;
	}

	@Override
	public WindowWidget getWidget(int x, int y, int width, int height) {
		return new WindowTextFieldWidget(x + 1, y + 1, width - 2, height - 2, "") {

			@Override
			public void charTyped(char chr, int modifiers) {
				super.charTyped(chr, modifiers);

				setValue(textField.getText());
			}

			@Override
			public void keyPressed(int keyCode, int scanCode, int modifiers) {
				super.keyPressed(keyCode, scanCode, modifiers);

				setValue(textField.getText());
			}

		}.withRenderEvent((w, ms, wx, wy) -> {
			TextFieldWidget textField = ((WindowTextFieldWidget) w).textField;
			if (!textField.getText().equals(getRealValue()))
				textField.setText(getRealValue());

			if (validator != null && !validator.apply(getRealValue())) {
				DrawableHelper.fill(ms, wx + w.x1 - 1, wy + w.y1 - 1, wx + w.x2 + 1, wy + w.y1, 0xffd07070);
				DrawableHelper.fill(ms, wx + w.x1 - 1, wy + w.y2, wx + w.x2 + 1, wy + w.y2 + 1, 0xffd07070);
				DrawableHelper.fill(ms, wx + w.x1 - 1, wy + w.y1, wx + w.x1, wy + w.y2, 0xffd07070);
				DrawableHelper.fill(ms, wx + w.x2, wy + w.y1, wx + w.x2 + 1, wy + w.y2, 0xffd07070);
			}
		});
	}

	private String getRealValue() {
		return super.getValue();
	}

	// Overridden getValue to avoid getting a invalid value
	@Override
	public String getValue() {
		return lastValidValue;
	}
	
	@Override
	public void setValue(String value) {
		super.setValue(value);

		if (validator == null || validator.apply(getRealValue()))
			lastValidValue = value;
	}
}

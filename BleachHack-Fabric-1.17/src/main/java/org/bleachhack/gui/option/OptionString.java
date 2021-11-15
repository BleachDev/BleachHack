package org.bleachhack.gui.option;

import java.util.function.Function;

import org.bleachhack.gui.window.widget.WindowTextFieldWidget;
import org.bleachhack.gui.window.widget.WindowWidget;
import org.bleachhack.util.io.BleachFileHelper;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class OptionString extends Option<String> {

	protected Function<String, Boolean> validator;
	protected String lastValidValue;

	public OptionString(String name, String tooltip, String value) {
		super(name, tooltip, value);
	}

	public OptionString(String name, String tooltip, String value, Function<String, Boolean> validator) {
		super(name, tooltip, value);
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
				if (validator == null || validator.apply(getRealValue()))
					lastValidValue = getRealValue();

				BleachFileHelper.SCHEDULE_SAVE_OPTIONS.set(true);
			}

			@Override
			public void keyPressed(int keyCode, int scanCode, int modifiers) {
				super.keyPressed(keyCode, scanCode, modifiers);

				setValue(textField.getText());
				if (validator == null || validator.apply(getRealValue()))
					lastValidValue = getRealValue();

				BleachFileHelper.SCHEDULE_SAVE_OPTIONS.set(true);
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
	public JsonElement serialize() {
		return new JsonPrimitive(getValue());
	}

	@Override
	public void deserialize(JsonElement json) {
		if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
			setValue(json.getAsString());
			lastValidValue = json.getAsString();
		}
	}
}

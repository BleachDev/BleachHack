package bleach.hack.gui.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import bleach.hack.gui.window.widget.WindowTextFieldWidget;
import bleach.hack.gui.window.widget.WindowWidget;
import bleach.hack.util.io.BleachFileHelper;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class OptionString extends Option<String> {

	public OptionString(String name, String value) {
		super(name, value);
	}

	public OptionString(String name, String tooltip, String value) {
		super(name, tooltip, value);
	}

	@Override
	public WindowWidget getWidget(int x, int y, int width, int height) {
		return new WindowTextFieldWidget(x + 1, y + 1, width - 2, height - 2, "") {

			@Override
			public void charTyped(char chr, int modifiers) {
				super.charTyped(chr, modifiers);

				setValue(textField.getText());
				BleachFileHelper.SCHEDULE_SAVE_OPTIONS = true;
			}

			@Override
			public void keyPressed(int keyCode, int scanCode, int modifiers) {
				super.keyPressed(keyCode, scanCode, modifiers);

				setValue(textField.getText());
				BleachFileHelper.SCHEDULE_SAVE_OPTIONS = true;
			}

		}.withRenderEvent(w -> {
			TextFieldWidget textField = ((WindowTextFieldWidget) w).textField;
			if (!textField.getText().equals(getValue())) {
				textField.setText(getValue());
			}
		});
	}

	@Override
	public JsonElement serialize() {
		return new JsonPrimitive(getValue());
	}

	@Override
	public void deserialize(JsonElement json) {
		if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
			setValue(json.getAsString());
		}
	}
}

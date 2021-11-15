package org.bleachhack.gui.option;

import java.util.function.Consumer;

import org.bleachhack.gui.window.widget.WindowButtonWidget;
import org.bleachhack.gui.window.widget.WindowWidget;
import org.bleachhack.util.io.BleachFileHelper;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class OptionBoolean extends Option<Boolean> {

	private Consumer<Boolean> onToggle;

	public OptionBoolean(String name, String tooltip, Boolean value) {
		super(name, tooltip, value);
	}

	public OptionBoolean(String name, String tooltip, Boolean value, Consumer<Boolean> onToggle) {
		super(name, tooltip, value);
		this.onToggle = onToggle;
	}

	@Override
	public WindowWidget getWidget(int x, int y, int width, int height) {
		return new WindowButtonWidget(x, y, x + width, y + height, "", () -> {
			setValue(!getValue());

			if (onToggle != null)
				onToggle.accept(getValue());

			BleachFileHelper.SCHEDULE_SAVE_OPTIONS.set(true);
		}).withRenderEvent((w, ms, wx, wy) -> ((WindowButtonWidget) w).text = getValue() ? "\u00a7aTrue" : "\u00a7cFalse");
	}

	@Override
	public JsonElement serialize() {
		return new JsonPrimitive(getValue());
	}

	@Override
	public void deserialize(JsonElement json) {
		if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isBoolean()) {
			setValue(json.getAsBoolean());
		}
	}
}

package org.bleachhack.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * A simple data handler that can read and write a value type to json.
 * 
 * <p>For more advanced data like children, <code>Setting.read()</code> and <code>Setting.write()</code> should be overridden.</p>
 * 
 * @param <T> the type of data to be handled
 */
public interface SettingDataHandler<T> {

	JsonElement write(T value);

	T read(JsonElement json) throws JsonParseException;

	default T readOrNull(JsonElement json) {
		try {
			return read(json);
		} catch (JsonParseException | IllegalStateException | UnsupportedOperationException e) {
			return null;
		}
	}
}

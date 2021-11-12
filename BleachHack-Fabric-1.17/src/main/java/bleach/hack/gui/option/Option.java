package bleach.hack.gui.option;

import java.util.UUID;
import java.util.stream.Stream;

import org.apache.logging.log4j.core.util.ReflectionUtil;

import com.google.gson.JsonElement;

import bleach.hack.BleachHack;
import bleach.hack.gui.window.widget.WindowWidget;
import bleach.hack.util.BleachPlayerManager;
import net.minecraft.client.MinecraftClient;

public abstract class Option<T> {

	public static Option<Boolean> GENERAL_CHECK_FOR_UPDATES = new OptionBoolean("Check For Updates", "Checks for BleachHack updates on startup.", true);
	public static Option<Boolean> GENERAL_SHOW_UPDATE_SCREEN = new OptionBoolean("Show Update Screen", "Automatically shows the update screen on startup if an update is found.", true);

	public static Option<Boolean> PLAYERLIST_SHOW_FRIENDS = new OptionBoolean("Highlight Friends", "Highlights friends in aqua on the playerlist.", true);
	public static Option<Boolean> PLAYERLIST_SHOW_SELF = new OptionBoolean("Highlight Self", "Highlights yourself in yellow on the playerlist.", false);
	public static Option<Boolean> PLAYERLIST_SHOW_PING = new OptionBoolean("Show Players Ping", "Shows players ping on the playerlist.", false);
	public static Option<Boolean> PLAYERLIST_SHOW_BH_USERS = new OptionBoolean("Show BH Users", "Shows other BleachHack players on the playerlist.", true);
	public static Option<Boolean> PLAYERLIST_SHOW_AS_BH_USER = new OptionBoolean("Appear As BH User", "Makes you show up as a BleachHack user to others.", true, b -> {
		String uuid = BleachPlayerManager.toProperUUID(MinecraftClient.getInstance().getSession().getUuid());
		if (b) {
			BleachHack.playerMang.getPlayers().add(UUID.fromString(uuid));
			BleachHack.playerMang.startPinger();
		} else {
			BleachHack.playerMang.getPlayers().remove(UUID.fromString(uuid));
			BleachHack.playerMang.stopPinger();
		}
	});

	public static Option<String> CHAT_COMMAND_PREFIX = new OptionString("Command Prefix", "The BleachHack command prefix.", "$", s -> !s.isEmpty());
	public static Option<Boolean> CHAT_SHOW_SUGGESTIONS = new OptionBoolean("Show Suggestions", "Shows command suggestions when typing a BleachHack command.", true);
	public static Option<Boolean> CHAT_QUICK_PREFIX = new OptionBoolean("Enable Quick Prefix", "Automatically opens chat with the BleachHack prefix when pressing that key.", false);

	public static final Option<?>[] OPTIONS = Stream.of(Option.class.getDeclaredFields())
			.filter(f -> Option.class.isAssignableFrom(f.getType()))
			.map(ReflectionUtil::getStaticFieldValue)
			.toArray(Option[]::new);

	private final String name;
	private final String tooltip;

	private final T defaultValue;
	private T value;

	public Option(String name, String tooltip, T value) {
		this.name = name;
		this.tooltip = tooltip;
		this.defaultValue = value;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getTooltip() {
		return tooltip;
	}

	public void resetValue() {
		value = defaultValue;
	}

	public boolean isDefault() {
		return value.equals(defaultValue);
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public abstract WindowWidget getWidget(int x, int y, int width, int height);

	public abstract JsonElement serialize();
	public abstract void deserialize(JsonElement json);
}

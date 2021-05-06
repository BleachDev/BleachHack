package bleach.hack.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.lwjgl.glfw.GLFW;

import com.google.common.eventbus.Subscribe;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventDrawOverlay;
import bleach.hack.event.events.EventKeyPress;
import bleach.hack.event.events.EventOpenScreen;
import bleach.hack.mixin.AccessorChatScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class CommandSuggestor {

	private static CommandSuggestor INSTANCE;

	private String curText = "";
	private List<String> suggestions = new ArrayList<>();
	private int selected = -1;
	private int scroll;

	public static CommandSuggestor getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CommandSuggestor();
		}

		return INSTANCE;
	}

	public static void init() {
		BleachHack.eventBus.register(getInstance());
	}

	public static void terminate() {
		getInstance().reset();
		BleachHack.eventBus.unregister(getInstance());
	}

	@Subscribe
	public void onDrawOverlay(EventDrawOverlay event) {
		Screen screen = MinecraftClient.getInstance().currentScreen;

		if (screen instanceof ChatScreen) {
			TextFieldWidget field = ((AccessorChatScreen) screen).getChatField();
			String text = field.getText();

			if (!text.equals(curText)) {
				suggestions.clear();
				curText = text;

				if (text.startsWith(Command.PREFIX)) {
					for (Command c: CommandManager.getCommands()) {
						if (c.getAliases()[0].startsWith(text.substring(Command.PREFIX.length()))) {
							suggestions.add(c.getAliases()[0]);
						}
					}
				}
				
				selected = 0;
				scroll = 0;
			}
			
			if (selected >= 0 && selected < suggestions.size()) {
				field.setSuggestion(suggestions.get(selected).substring(text.length() - Command.PREFIX.length()));
			}

			if (!suggestions.isEmpty()) {
				event.matrix.translate(0, 0, 200);

				int length = suggestions.stream()
						.map(s -> MinecraftClient.getInstance().textRenderer.getWidth(s))
						.sorted(Comparator.reverseOrder())
						.findFirst().orElse(0);

				int startY = screen.height - Math.min(suggestions.size(), 10) * 12 - 15;
				for (int i = scroll; i < suggestions.size() && i < scroll + 10; i++) {
					String suggestion = suggestions.get(i);

					DrawableHelper.fill(event.matrix, 10, startY, 10 + length + 2, startY + 12, 0xe0000000);
					MinecraftClient.getInstance().textRenderer.drawWithShadow(
							event.matrix, suggestion, 11, startY + 2, i == selected ? 0xffff00: 0xb0b0b0);

					startY += 12;
				}

				event.matrix.translate(0, 0, -200);
			}
		}
	}

	@Subscribe
	public void onKeyPress(EventKeyPress.Global event) {
		if (event.getAction() != 0 && !suggestions.isEmpty() && !curText.isEmpty()) {
			if (event.getKey() == GLFW.GLFW_KEY_DOWN || event.getKey() == GLFW.GLFW_KEY_TAB) {
				selected = selected >= suggestions.size() - 1 ? 0 : selected + 1;
				updateScroll();
			} else if (event.getKey() == GLFW.GLFW_KEY_UP) {
				selected = selected <= 0 ? suggestions.size() - 1 : selected - 1;
				updateScroll();
			} else if (event.getKey() == GLFW.GLFW_KEY_SPACE) {
				if (selected >= 0 && selected < suggestions.size()) {
					TextFieldWidget field = ((AccessorChatScreen) MinecraftClient.getInstance().currentScreen).getChatField();
					field.setText(field.getText() + suggestions.get(selected).substring(field.getText().length() - Command.PREFIX.length()));
				}
			}
		}
	}
	
	private void updateScroll() {
		if (scroll > selected) {
			scroll = Math.max(selected, 0);
		} else if (scroll + 10 <= selected) {
			scroll = Math.min(suggestions.size(), selected - 9);
		}
	}

	@Subscribe
	public void onOpenScreen(EventOpenScreen event) {
		if (MinecraftClient.getInstance().currentScreen instanceof ChatScreen) {
			reset();
		}
	}

	public void reset() {
		curText = "";
		suggestions.clear();
		selected = 0;
		scroll = 0;
	}
}

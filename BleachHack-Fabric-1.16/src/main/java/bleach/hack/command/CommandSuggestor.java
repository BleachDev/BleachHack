package bleach.hack.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.lwjgl.glfw.GLFW;

import bleach.hack.eventbus.BleachSubscribe;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventRenderInGameHud;
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

	public static void start() {
		BleachHack.eventBus.subscribe(getInstance());
	}

	public static void stop() {
		getInstance().reset();
		BleachHack.eventBus.unsubscribe(getInstance());
	}

	@BleachSubscribe
	public void onDrawOverlay(EventRenderInGameHud event) {
		Screen screen = MinecraftClient.getInstance().currentScreen;

		if (screen instanceof ChatScreen) {
			TextFieldWidget field = ((AccessorChatScreen) screen).getChatField();
			String text = field.getText();

			if (!text.equals(curText)) {
				suggestions.clear();
				curText = text;

				if (text.startsWith(Command.PREFIX)) {
					suggestions.addAll(CommandManager.getSuggestionProvider().getSuggestions(text.substring(Command.PREFIX.length()).split(" ", -1)));
				}

				selected = 0;
				scroll = 0;
			}

			if (selected >= 0 && selected < suggestions.size()) {
				String[] split = field.getText().split(" ", -1);
				int offset = split[split.length - 1].length() - (split.length == 1 ? Command.PREFIX.length() : 0);

				if (offset > suggestions.get(selected).length()) {
					field.setSuggestion("");
				} else {
					field.setSuggestion(suggestions.get(selected).substring(offset));
				}
			}

			if (!suggestions.isEmpty()) {
				event.getMatrix().push();
				event.getMatrix().translate(0, 0, 200);

				int length = suggestions.stream()
						.map(s -> MinecraftClient.getInstance().textRenderer.getWidth(s))
						.sorted(Comparator.reverseOrder())
						.findFirst().orElse(0);

				int startX = MinecraftClient.getInstance().textRenderer.getWidth(
						field.getText().replaceFirst("[^ ]*$", "") + (!field.getText().contains(" ") ? Command.PREFIX : "")) + 3;
				int startY = screen.height - Math.min(suggestions.size(), 10) * 12 - 15;
				for (int i = scroll; i < suggestions.size() && i < scroll + 10; i++) {
					String suggestion = suggestions.get(i);

					DrawableHelper.fill(event.getMatrix(), startX, startY, startX + length + 2, startY + 12, 0xd0000000);
					MinecraftClient.getInstance().textRenderer.drawWithShadow(
							event.getMatrix(), suggestion, startX + 1, startY + 2, i == selected ? 0xffff00: 0xb0b0b0);

					startY += 12;
				}

				event.getMatrix().pop();
			}
		}
	}

	@BleachSubscribe
	public void onKeyPressGlobal(EventKeyPress.Global event) {
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
					String[] split = field.getText().split(" ", -1);
					int offset = split[split.length - 1].length() - (split.length == 1 ? Command.PREFIX.length() : 0);

					if (offset < suggestions.get(selected).length() && !suggestions.get(selected).matches("^<.*>$")) {
						field.setText(field.getText() + suggestions.get(selected).substring(offset));
					}
				}
			}
		}
	}
	
	@BleachSubscribe
	public void onKeyPressChat(EventKeyPress.InChat event) {
		TextFieldWidget field = ((AccessorChatScreen) MinecraftClient.getInstance().currentScreen).getChatField();
		if (field.getText().startsWith(Command.PREFIX)
				&& (event.getKey() == GLFW.GLFW_KEY_TAB || event.getKey() == GLFW.GLFW_KEY_UP || event.getKey() == GLFW.GLFW_KEY_DOWN)) {
			event.setCancelled(true);
		}
	}

	private void updateScroll() {
		if (scroll > selected) {
			scroll = Math.max(selected, 0);
		} else if (scroll + 10 <= selected) {
			scroll = Math.min(suggestions.size(), selected - 9);
		}
	}

	@BleachSubscribe
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

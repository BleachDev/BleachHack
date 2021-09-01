package bleach.hack.gui.clickgui.window;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;

public class UIContainer {

	public Map<String, UIWindow> windows = new LinkedHashMap<>();

	public UIContainer() {
	}

	public void render(MatrixStack matrices) {
		for (UIWindow w: windows.values()) {
			if (!w.shouldClose()) {
				w.renderUI(matrices);
			}
		}
	}

	public void resizeScreen(int width, int height) {
		windows.forEach((id, window) -> {
			window.x1 = getLeft(id, width, height);
			window.y1 = getTop(id, width, height);
			window.x2 = getRight(id, width, height);
			window.y2 = getBottom(id, width, height);
		});
	}

	protected int getLeft(String id, int width, int height, String... passIds) {
		UIWindow window = windows.get(id);

		for (Pair<String, Integer> atm: window.position.getAttachments()) {
			if (ArrayUtils.contains(passIds, id)) continue;
			if (atm.getLeft().equals("l")) return -1;
			if (atm.getLeft().equals("r")) return width + 1 - window.getSize()[0];
			if (atm.getLeft().equals("c")) return width / 2 - window.getSize()[0] / 2;
			if (atm.getRight() == 1) return getRight(atm.getLeft(), width, height, ArrayUtils.add(passIds, id));
			if (atm.getRight() == 3) return getLeft(atm.getLeft(), width, height, ArrayUtils.add(passIds, id)) - window.getSize()[0];
		}

		return (int) (width * window.position.xPercent);
	}

	protected int getRight(String id, int width, int height, String... passIds) {
		UIWindow window = windows.get(id);

		for (Pair<String, Integer> atm: window.position.getAttachments()) {
			if (ArrayUtils.contains(passIds, id)) continue;
			if (atm.getLeft().equals("l")) return window.getSize()[0] - 1;
			if (atm.getLeft().equals("r")) return width + 1;
			if (atm.getLeft().equals("c")) return width / 2 + window.getSize()[0] / 2;
			if (atm.getRight() == 1) return getRight(atm.getLeft(), width, height, ArrayUtils.add(passIds, id)) + window.getSize()[0];
			if (atm.getRight() == 3) return getLeft(atm.getLeft(), width, height, ArrayUtils.add(passIds, id));
		}

		return (int) (width * window.position.xPercent) + window.getSize()[0];
	}

	protected int getTop(String id, int width, int height, String... passIds) {
		UIWindow window = windows.get(id);

		for (Pair<String, Integer> atm: window.position.getAttachments()) {
			if (ArrayUtils.contains(passIds, id)) continue;
			if (atm.getLeft().equals("t")) return -1;
			if (atm.getLeft().equals("b")) return getScreenBottom(height) + 1 - window.getSize()[1];
			if (atm.getRight() == 0) return getTop(atm.getLeft(), width, height, ArrayUtils.add(passIds, id)) - window.getSize()[1];
			if (atm.getRight() == 2) return getBottom(atm.getLeft(), width, height, ArrayUtils.add(passIds, id));
		}

		return (int) (height * window.position.yPercent);
	}

	protected int getBottom(String id, int width, int height, String... passIds) {
		UIWindow window = windows.get(id);

		for (Pair<String, Integer> atm: window.position.getAttachments()) {
			if (ArrayUtils.contains(passIds, id)) continue;
			if (atm.getLeft().equals("t")) return window.getSize()[1] - 1;
			if (atm.getLeft().equals("b")) return getScreenBottom(height) + 1;
			if (atm.getRight() == 0) return getTop(atm.getLeft(), width, height, ArrayUtils.add(passIds, id));
			if (atm.getRight() == 2) return getBottom(atm.getLeft(), width, height, ArrayUtils.add(passIds, id)) + window.getSize()[1];
		}

		return (int) (height * window.position.yPercent) + window.getSize()[1];
	}

	public int getScreenBottom(int height) {
		return height - (MinecraftClient.getInstance().currentScreen instanceof ChatScreen ? 14 : 0);
	}
}

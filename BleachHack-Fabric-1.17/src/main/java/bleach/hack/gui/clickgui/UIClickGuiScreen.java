/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.gui.clickgui;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import bleach.hack.gui.clickgui.window.ModuleWindow;
import bleach.hack.gui.clickgui.window.UIWindow;
import bleach.hack.gui.clickgui.window.UIWindow.Position;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.UI;
import bleach.hack.util.io.BleachFileHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;

public class UIClickGuiScreen extends ClickGuiScreen {

	public Map<String, UIWindow> uiWindows = new LinkedHashMap<>();
	private Screen parent;

	public UIClickGuiScreen(Screen parent, UI uiModule) {
		super(new LiteralText("UI Editor"));
		this.parent = parent;

		uiWindows.put("ml",
				new UIWindow(new Position(Pair.of("l", 1), Pair.of("t", 2)), uiWindows,
						ui -> ui.getSetting(0).asToggle().state,
						() -> uiModule.getModuleListSize(),
						(ms, x, y) -> uiModule.drawModuleList(ms, x, y))
				);

		uiWindows.put("if",
				new UIWindow(new Position(Pair.of("l", 1), Pair.of("b", 0)), uiWindows,
						ui -> ui.getSetting(1).asToggle().state,
						() -> uiModule.getInfoSize(),
						(ms, x, y) -> uiModule.drawInfo(ms, x, y))
				);

		uiWindows.put("pl",
				new UIWindow(new Position(Pair.of("l", 1), Pair.of("ml", 2)), uiWindows,
						ui -> ui.getSetting(2).asToggle().state,
						() -> uiModule.getPlayerSize(),
						(ms, x, y) -> uiModule.drawPlayerList(ms, x, y))
				);

		uiWindows.put("ar",
				new UIWindow(new Position(0.5, 0.85), uiWindows,
						ui -> ui.getSetting(3).asToggle().state,
						() -> uiModule.getArmorSize(),
						(ms, x, y) -> uiModule.drawArmor(ms, x, y))
				);

		uiWindows.put("lm",
				new UIWindow(new Position(0, 0.05, Pair.of("c", 1)), uiWindows,
						ui -> ui.getSetting(4).asToggle().state,
						() -> uiModule.getLagMeterSize(),
						(ms, x, y) -> uiModule.drawLagMeter(ms, x, y))
				);
	}

	public void init() {
		super.init();

		clearWindows();

		uiWindows.forEach((id, window) -> {
			addWindow(window);
			window.x1 = getLeft(id);
			window.y1 = getTop(id);
			window.x2 = getRight(id);
			window.y2 = getBottom(id);
		});

		addWindow(new ModuleWindow(Arrays.asList(ModuleManager.getModule("UI")),
				200, 200, 75, "Render", new ItemStack(Items.YELLOW_STAINED_GLASS)));
	}

	public void onClose() {
		client.setScreen(parent);
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		BleachFileHelper.SCHEDULE_SAVE_UI = true;

		uiWindows.forEach((id, window) -> {
			window.x1 = getLeft(id);
			window.y1 = getTop(id);
			window.x2 = getRight(id);
			window.y2 = getBottom(id);
		});

		super.render(matrices, mouseX, mouseY, delta);
	}

	protected int getLeft(String id, String... passIds) {
		UIWindow window = uiWindows.get(id);

		for (Pair<String, Integer> atm: window.position.getAttachments()) {
			if (ArrayUtils.contains(passIds, id)) continue;
			if (atm.getLeft().equals("l")) return -1;
			if (atm.getLeft().equals("r")) return client.getWindow().getScaledWidth() + 1 - window.getSize()[0];
			if (atm.getLeft().equals("c")) return client.getWindow().getScaledWidth() / 2 - window.getSize()[0] / 2;
			if (atm.getRight() == 1) return getRight(atm.getLeft(), ArrayUtils.add(passIds, id));
			if (atm.getRight() == 3) return getLeft(atm.getLeft(), ArrayUtils.add(passIds, id)) - window.getSize()[0];
		}

		return (int) (client.getWindow().getScaledWidth() * window.position.xPercent);
	}

	protected int getRight(String id, String... passIds) {
		UIWindow window = uiWindows.get(id);

		for (Pair<String, Integer> atm: window.position.getAttachments()) {
			if (ArrayUtils.contains(passIds, id)) continue;
			if (atm.getLeft().equals("l")) return window.getSize()[0] - 1;
			if (atm.getLeft().equals("r")) return client.getWindow().getScaledWidth() + 1;
			if (atm.getLeft().equals("c")) return client.getWindow().getScaledWidth() / 2 + window.getSize()[0] / 2;
			if (atm.getRight() == 1) return getRight(atm.getLeft(), ArrayUtils.add(passIds, id)) + window.getSize()[0];
			if (atm.getRight() == 3) return getLeft(atm.getLeft(), ArrayUtils.add(passIds, id));
		}

		return (int) (client.getWindow().getScaledWidth() * window.position.xPercent) + window.getSize()[0];
	}

	protected int getTop(String id, String... passIds) {
		UIWindow window = uiWindows.get(id);

		for (Pair<String, Integer> atm: window.position.getAttachments()) {
			if (ArrayUtils.contains(passIds, id)) continue;
			if (atm.getLeft().equals("t")) return -1;
			if (atm.getLeft().equals("b")) return getScreenBottom(client) + 1 - window.getSize()[1];
			if (atm.getRight() == 0) return getTop(atm.getLeft(), ArrayUtils.add(passIds, id)) - window.getSize()[1];
			if (atm.getRight() == 2) return getBottom(atm.getLeft(), ArrayUtils.add(passIds, id));
		}

		return (int) (client.getWindow().getScaledHeight() * window.position.yPercent);
	}

	protected int getBottom(String id, String... passIds) {
		UIWindow window = uiWindows.get(id);

		for (Pair<String, Integer> atm: window.position.getAttachments()) {
			if (ArrayUtils.contains(passIds, id)) continue;
			if (atm.getLeft().equals("t")) return window.getSize()[1] - 1;
			if (atm.getLeft().equals("b")) return getScreenBottom(client) + 1;
			if (atm.getRight() == 0) return getTop(atm.getLeft(), ArrayUtils.add(passIds, id));
			if (atm.getRight() == 2) return getBottom(atm.getLeft(), ArrayUtils.add(passIds, id)) + window.getSize()[1];
		}

		return (int) (client.getWindow().getScaledHeight() * window.position.yPercent) + window.getSize()[1];
	}
	
	public static int getScreenBottom(MinecraftClient mc) {
		return mc.getWindow().getScaledHeight() - (mc.currentScreen instanceof ChatScreen ? 14 : 0);
	}
}

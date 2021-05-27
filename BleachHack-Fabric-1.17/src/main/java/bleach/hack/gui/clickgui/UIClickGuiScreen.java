/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.gui.clickgui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import bleach.hack.gui.clickgui.window.ModuleWindow;
import bleach.hack.gui.clickgui.window.UIWindow;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.UI;
import bleach.hack.util.file.BleachFileHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;

public class UIClickGuiScreen extends ClickGuiScreen {

	public Map<String, MutablePair<Position, UIWindow>> uiWindows = new LinkedHashMap<>();
	private Screen parent;

	public UIClickGuiScreen(Screen parent, UI uiModule) {
		super(new LiteralText("UI Editor"));
		this.parent = parent;

		uiWindows.put("ml", new MutablePair<>(
				new Position(Pair.of("l", 1), Pair.of("t", 2)),
				new UIWindow(uiWindows,
						() -> uiModule.getModuleListSize(),
						(ms, x, y) -> uiModule.drawModuleList(ms, x, y))
				));

		uiWindows.put("pl", new MutablePair<>(
				new Position(Pair.of("l", 1), Pair.of("ml", 2)),
				new UIWindow(uiWindows,
						() -> uiModule.getPlayerSize(),
						(ms, x, y) -> uiModule.drawPlayerList(ms, x, y))
				));

		uiWindows.put("if", new MutablePair<>(
				new Position(Pair.of("l", 1), Pair.of("b", 0)),
				new UIWindow(uiWindows,
						() -> uiModule.getInfoSize(),
						(ms, x, y) -> uiModule.drawInfo(ms, x, y))
				));

		uiWindows.put("ar", new MutablePair<>(
				new Position(0.5, 0.85),
				new UIWindow(uiWindows,
						() -> uiModule.getArmorSize(),
						(ms, x, y) -> uiModule.drawArmor(ms, x, y))
				));

		uiWindows.put("lm", new MutablePair<>(
				new Position(0.5, 0),
				new UIWindow(uiWindows,
						() -> uiModule.getLagMeterSize(),
						(ms, x, y) -> uiModule.drawLagMeter(ms, x, y))
				));
	}

	public void init() {
		super.init();

		clearWindows();

		uiWindows.forEach((id, pair) -> {
			addWindow(pair.right);
			pair.right.x1 = getLeft(id);
			pair.right.y1 = getTop(id);
			pair.right.x2 = getRight(id);
			pair.right.y2 = getBottom(id);
		});

		addWindow(new ModuleWindow(Arrays.asList(ModuleManager.getModule("UI")),
				200, 200, 75, "Render", new ItemStack(Items.YELLOW_STAINED_GLASS)));
	}
	
	public void onClose() {
		client.openScreen(parent);
	}

	public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
		BleachFileHelper.SCHEDULE_SAVE_UI = true;

		uiWindows.forEach((id, pair) -> {
			pair.right.x1 = getLeft(id);
			pair.right.y1 = getTop(id);
			pair.right.x2 = getRight(id);
			pair.right.y2 = getBottom(id);
		});

		super.render(matrix, mouseX, mouseY, delta);
	}

	protected int getLeft(String id, String... passIds) {
		MutablePair<Position, UIWindow> uiPair = uiWindows.get(id);

		for (Pair<String, Integer> atm: uiPair.left.getAttachments()) {
			if (ArrayUtils.contains(passIds, id)) continue;
			if (atm.getLeft().equals("l")) return 0;
			if (atm.getLeft().equals("r")) return client.getWindow().getScaledWidth() - uiPair.right.getSize()[0];
			if (atm.getRight() == 1) return getRight(atm.getLeft(), ArrayUtils.add(passIds, id));
			if (atm.getRight() == 3) return getLeft(atm.getLeft(), ArrayUtils.add(passIds, id)) - uiPair.right.getSize()[0];
		}

		return (int) (client.getWindow().getScaledWidth() * uiPair.left.xPercent);
	}

	protected int getRight(String id, String... passIds) {
		MutablePair<Position, UIWindow> uiPair = uiWindows.get(id);

		for (Pair<String, Integer> atm: uiPair.left.getAttachments()) {
			if (ArrayUtils.contains(passIds, id)) continue;
			if (atm.getLeft().equals("l")) return uiPair.right.getSize()[0];
			if (atm.getLeft().equals("r")) return client.getWindow().getScaledWidth();
			if (atm.getRight() == 1) return getRight(atm.getLeft(), ArrayUtils.add(passIds, id)) + uiPair.right.getSize()[0];
			if (atm.getRight() == 3) return getLeft(atm.getLeft(), ArrayUtils.add(passIds, id));
		}

		return (int) (client.getWindow().getScaledWidth() * uiPair.left.xPercent) + uiPair.right.getSize()[0];
	}

	protected int getTop(String id, String... passIds) {
		MutablePair<Position, UIWindow> uiPair = uiWindows.get(id);

		for (Pair<String, Integer> atm: uiPair.left.getAttachments()) {
			if (ArrayUtils.contains(passIds, id)) continue;
			if (atm.getLeft().equals("t")) return 0;
			if (atm.getLeft().equals("b")) return client.getWindow().getScaledHeight() - uiPair.right.getSize()[1];
			if (atm.getRight() == 0) return getTop(atm.getLeft(), ArrayUtils.add(passIds, id)) - uiPair.right.getSize()[1];
			if (atm.getRight() == 2) return getBottom(atm.getLeft(), ArrayUtils.add(passIds, id));
		}

		return (int) (client.getWindow().getScaledHeight() * uiPair.left.yPercent);
	}

	protected int getBottom(String id, String... passIds) {
		MutablePair<Position, UIWindow> uiPair = uiWindows.get(id);

		for (Pair<String, Integer> atm: uiPair.left.getAttachments()) {
			if (ArrayUtils.contains(passIds, id)) continue;
			if (atm.getLeft().equals("t")) return uiPair.right.getSize()[1];
			if (atm.getLeft().equals("b")) return client.getWindow().getScaledHeight();
			if (atm.getRight() == 0) return getTop(atm.getLeft(), ArrayUtils.add(passIds, id));
			if (atm.getRight() == 2) return getBottom(atm.getLeft(), ArrayUtils.add(passIds, id)) + uiPair.right.getSize()[1];
		}

		return (int) (client.getWindow().getScaledHeight() * uiPair.left.yPercent) + uiPair.right.getSize()[1];
	}

	public static class Position {

		public double xPercent;
		public double yPercent;
		private List<Pair<String, Integer>> attachments = new ArrayList<>();

		public Position(double xPercent, double yPercent) {
			this.xPercent = xPercent;
			this.yPercent = yPercent;
		}

		public Position(double xPercent, double yPercent, Pair<String, Integer> attachment) {
			this.xPercent = xPercent;
			this.yPercent = yPercent;
			addAttachment(attachment);
		}

		public Position(Pair<String, Integer> attachment1, Pair<String, Integer> attachment2) {
			addAttachment(attachment1);
			addAttachment(attachment2);
		}

		public List<Pair<String, Integer>> getAttachments() {
			return attachments;
		}

		public boolean addAttachment(Pair<String, Integer> attachment) {
			if (attachments.isEmpty()) {
				attachments.add(attachment);
				return true;
			}

			if (attachments.size() == 1) {
				int side = attachments.get(0).getRight();
				int newSide = attachment.getRight();
				if (newSide != side && newSide != side + 2 % 4) {
					attachments.add(attachment);
					return true;
				}

			}

			return false;
		}
	}
}

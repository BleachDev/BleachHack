/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.gui;

import org.bleachhack.gui.window.Window;
import org.bleachhack.gui.window.WindowScreen;
import org.bleachhack.gui.window.widget.WindowButtonWidget;
import org.bleachhack.gui.window.widget.WindowScrollbarWidget;
import org.bleachhack.gui.window.widget.WindowTextWidget;
import org.bleachhack.gui.window.widget.WindowWidget;
import org.bleachhack.setting.option.Option;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;


public class BleachOptionsScreen extends WindowScreen {

	private Screen parent;

	private WindowScrollbarWidget scrollbar;

	public BleachOptionsScreen(Screen parent) {
		super(Text.literal("BleachHack Options"));
		this.parent = parent;
	}

	@Override
	public void init() {
		super.init();

		addWindow(new Window(width / 8,
				height / 8,
				width - width / 8,
				height - height / 8, "Options", new ItemStack(Items.REDSTONE)));

		int w = getWindow(0).x2 - getWindow(0).x1;
		int h = getWindow(0).y2 - getWindow(0).y1;
		int y = 20;

		y = addCategory(0, w / 2, y, "General Settings",
				Option.GENERAL_CHECK_FOR_UPDATES,
				Option.GENERAL_SHOW_UPDATE_SCREEN);

		y = addCategory(0, w / 2, y + 15, "Playerlist Settings",
				Option.PLAYERLIST_SHOW_FRIENDS);

		y = addCategory(0, w / 2, y + 15, "Chat Settings",
				Option.CHAT_COMMAND_PREFIX,
				Option.CHAT_SHOW_SUGGESTIONS,
				Option.CHAT_QUICK_PREFIX);

		for (WindowWidget widget: getWindow(0).getWidgets()) {
			if (!(widget instanceof WindowScrollbarWidget)) {
				widget.cullY = true;
			}
		}

		scrollbar = getWindow(0).addWidget(new WindowScrollbarWidget(w - 11, 12, y - 7, h - 13, 0));
	}

	private int addCategory(int window, int x, int y, String name, Option<?>... entries) {
		getWindow(window).addWidget(new WindowTextWidget("- " + name + " -", true, WindowTextWidget.TextAlign.MIDDLE, x, y, 0xe0e0e0));
		y += 20;

		for (Option<?> entry: entries) {
			// Option
			getWindow(0).addWidget(entry.getWidget(x + 10, y - 3, 56, 16));

			// Revert button
			getWindow(window).addWidget(new WindowButtonWidget(x + 68, y - 3, x + 84, y + 13, "", entry::resetValue)
					.withRenderEvent((w, ms, wx, wy)
							-> ((WindowButtonWidget) w).text = entry.isDefault() ? "\u00a77\u21c4" : "\u21c4"));

			// Name text (at the end because of... reasons)
			getWindow(window).addWidget(new WindowTextWidget(
					Text.literal(entry.getName()).styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(entry.getTooltip())))),
					true, x - 107, y, 0xffffff));


			y += 17;
		}

		return y;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);

		int offset = scrollbar.getOffsetSinceRender();
		for (WindowWidget widget: getWindow(0).getWidgets()) {
			if (!(widget instanceof WindowScrollbarWidget)) {
				widget.y1 -= offset;
				widget.y2 -= offset;
			}
		}

		super.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		scrollbar.moveScrollbar((int) -amount * 7);

		return super.mouseScrolled(mouseX, mouseY, amount);
	}

	@Override
	public void close() {
		client.setScreen(parent);
	}
}

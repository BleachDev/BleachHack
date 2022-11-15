/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.gui;

import java.net.http.HttpResponse.BodyHandlers;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bleachhack.BleachHack;
import org.bleachhack.gui.window.Window;
import org.bleachhack.gui.window.WindowScreen;
import org.bleachhack.gui.window.widget.WindowScrollbarWidget;
import org.bleachhack.gui.window.widget.WindowTextWidget;
import org.bleachhack.gui.window.widget.WindowWidget;
import org.bleachhack.util.collections.ImmutablePairList;
import org.bleachhack.util.io.BleachJsonHelper;
import org.bleachhack.util.io.BleachOnlineMang;

import com.google.gson.JsonObject;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.HoverEvent;

import net.minecraft.text.Text;

public class BleachCreditsScreen extends WindowScreen {

	// <If Donator, Name/Discord tag>
	private static ImmutablePairList<Boolean, String> boosterList;
	private static boolean boostersLoaded;

	private WindowScrollbarWidget scrollbar;

	static {
		BleachOnlineMang.getResourceAsync("credits.json", BodyHandlers.ofString()).thenAccept(st -> {
			boosterList = new ImmutablePairList<>();
			JsonObject json = BleachJsonHelper.parseOrNull(st, JsonObject.class);

			if (json != null) {
				if (json.has("donators") && json.get("donators").isJsonArray()) {
					json.get("donators").getAsJsonArray().forEach(j -> boosterList.add(new ImmutablePair<>(true, j.getAsString())));
				}

				if (json.has("boosters") && json.get("boosters").isJsonArray()) {
					json.get("boosters").getAsJsonArray().forEach(j -> boosterList.add(new ImmutablePair<>(false, j.getAsString())));
				}
			}
		});
	}

	public BleachCreditsScreen() {
		super(Text.literal("BleachHack Credits"));
	}

	public void init() {
		super.init();

		addWindow(new Window(width / 8,
				height / 8,
				width - width / 8,
				height - height / 8, "Credits", new ItemStack(Items.DRAGON_HEAD)));

		int w = getWindow(0).x2 - getWindow(0).x1;
		int h = getWindow(0).y2 - getWindow(0).y1;

		getWindow(0).addWidget(new WindowTextWidget(BleachHack.watermark.getText(), true, WindowTextWidget.TextAlign.MIDDLE, 3f, w / 2, 22, 0xb0b0b0));
		getWindow(0).addWidget(new WindowTextWidget(BleachHack.VERSION, true, WindowTextWidget.TextAlign.MIDDLE, 1.5f, w / 2, 41, 0xffc050));

		getWindow(0).addWidget(new WindowTextWidget("- Main Developer -", true, WindowTextWidget.TextAlign.MIDDLE, w / 2, 65, 0xe0e0e0));
		getWindow(0).addWidget(new WindowTextWidget(
				Text.literal("Bleach").styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("\u00a77https://github.com/BleachDev\n\n\u00a7eMain Developer!")))),
				true, WindowTextWidget.TextAlign.MIDDLE, w / 2, 80, 0x51eff5));

		getWindow(0).addWidget(new WindowTextWidget("- Contributors -", true, WindowTextWidget.TextAlign.MIDDLE, w / 2, 100, 0xe0e0e0));
		getWindow(0).addWidget(new WindowTextWidget(
				Text.literal("LasnikProgram").styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("\u00a77https://github.com/lasnikprogram\n\n\u00a7fMade first version of LogoutSpot, AirPlace, EntityMenu, HoleESP, AutoParkour and Search.")))),
				true, WindowTextWidget.TextAlign.MIDDLE, w / 2, 115, 0x00a0a0));
		getWindow(0).addWidget(new WindowTextWidget(
				Text.literal("slcoolj").styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("\u00a77https://github.com/slcoolj\n\n\u00a7fMade Criticals, Speedmine OG mode and did the module system rewrite.")))),
				true, WindowTextWidget.TextAlign.MIDDLE, w / 2, 127, 0x00a0a0));
		getWindow(0).addWidget(new WindowTextWidget(
				Text.literal("DevScyu").styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("\u00a77https://github.com/DevScyu\n\n\u00a7fMade first version of AutoTool, Trajectories, NoRender, AutoWalk, ElytraReplace, HandProgress and added Login manager encryption.")))),
				true, WindowTextWidget.TextAlign.MIDDLE, w / 2, 139, 0x00a0a0));
		getWindow(0).addWidget(new WindowTextWidget(
				Text.literal("Bunt3rhund").styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("\u00a77https://github.com/Bunt3rhund\n\n\u00a7fMade first version of Zoom.")))),
				true, WindowTextWidget.TextAlign.MIDDLE, w / 2, 151, 0x00a0a0));
		getWindow(0).addWidget(new WindowTextWidget(
				Text.literal("MorganAnkan").styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("\u00a77https://github.com/MorganAnkan\n\n\u00a7fMade the title screen text Rgb.")))),
				true, WindowTextWidget.TextAlign.MIDDLE, w / 2, 163, 0x00a0a0));
		getWindow(0).addWidget(new WindowTextWidget(
				Text.literal("ThePapanoob").styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("\u00a77https://github.com/thepapanoob\n\n\u00a7fAdded Projectiles mode in Killaura.")))),
				true, WindowTextWidget.TextAlign.MIDDLE, w / 2, 175, 0x00a0a0));

		getWindow(0).addWidget(new WindowTextWidget("- Donators/Boosters -", true, WindowTextWidget.TextAlign.MIDDLE, w / 2, 195, 0xe0e0e0));
		int y = 210;
		if (boosterList != null) {
			boostersLoaded = true;
			for (ImmutablePair<Boolean, String> i: boosterList) {
				getWindow(0).addWidget(new WindowTextWidget(getBoosterText(i), true, WindowTextWidget.TextAlign.MIDDLE, w / 2, y, 0));
				y += 12;
			}
		} else {
			getWindow(0).addWidget(new WindowTextWidget("\u00a77Loading..", true, WindowTextWidget.TextAlign.MIDDLE, w / 2, y, 0));
			y += 12;
		}

		for (WindowWidget widget: getWindow(0).getWidgets()) {
			if (!(widget instanceof WindowScrollbarWidget)) {
				widget.cullY = true;
			}
		}

		scrollbar = getWindow(0).addWidget(new WindowScrollbarWidget(w - 11, 12, y - 10, h - 13, 0));
	}

	private Text getBoosterText(ImmutablePair<Boolean, String> pair) {
		int color = pair.getLeft() ? 0x1abc9c : 0xf579ff;
		String[] split = pair.getRight().split("#");
		return Text.literal(split[0]).styled(s -> s
				.withColor(color)
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
						Text.literal(pair.getRight()).styled(s1 -> s1.withColor(color)))));
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		
		if (!boostersLoaded && boosterList != null) {
			int scroll = scrollbar.getPageOffset();
			init();
			scrollbar.setPageOffset(scroll);
		}

		int offset = scrollbar.getOffsetSinceRender();
		for (WindowWidget widget: getWindow(0).getWidgets()) {
			if (!(widget instanceof WindowScrollbarWidget)) {
				widget.y1 -= offset;
				widget.y2 -= offset;
			}
		}

		super.render(matrices, mouseX, mouseY, delta);
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		scrollbar.moveScrollbar((int) -amount * 7);

		return super.mouseScrolled(mouseX, mouseY, amount);
	}
}

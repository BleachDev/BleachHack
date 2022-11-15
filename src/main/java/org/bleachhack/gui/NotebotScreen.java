/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.gui;

import net.minecraft.block.enums.Instrument;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;
import org.bleachhack.gui.window.Window;
import org.bleachhack.gui.window.WindowScreen;
import org.bleachhack.gui.window.widget.WindowButtonWidget;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.Notebot;
import org.bleachhack.module.mods.Notebot.Song;
import org.bleachhack.util.NotebotUtils;
import org.bleachhack.util.io.BleachFileMang;

import java.net.URI;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class NotebotScreen extends WindowScreen {

	private Set<String> files = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
	private int page = 0;

	private Song entry;
	private boolean playing;
	private int playTick;

	public NotebotScreen() {
		super(Text.literal("Notebot Gui"));
	}

	public void init() {
		super.init();

		files.clear();

		BleachFileMang.getDir().resolve("notebot/").toFile().mkdirs();
		for (String f: BleachFileMang.getDir().resolve("notebot/").toFile().list())
			files.add(f);

		int ww = Math.max(width / 2, 360);
		int wh = Math.max(height / 2, 200);
		addWindow(new Window(
				width / 2 - ww / 2,
				height / 2 - wh / 2,
				width / 2 + ww / 2,
				height / 2 + wh / 2,
				"Notebot Gui", new ItemStack(Items.NOTE_BLOCK)));

		getWindow(0).addWidget(new WindowButtonWidget(22, 14, 32, 24, "<", () -> page = page <= 0 ? 0 : page - 1));
		getWindow(0).addWidget(new WindowButtonWidget(77, 14, 87, 24, ">", () -> page++));

		int xEnd = getWindow(0).x2 - getWindow(0).x1;

		getWindow(0).addWidget(new WindowButtonWidget(xEnd - 30, 14, xEnd - 3, 24, "Help", () ->
		Util.getOperatingSystem().open(URI.create("https://www.youtube.com/watch?v=Z6O80jItoAk"))));
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
	}

	public void onRenderWindow(MatrixStack matrices, int window, int mouseX, int mouseY) {
		super.onRenderWindow(matrices, window, mouseX, mouseY);

		if (window == 0) {
			int x = getWindow(0).x1;
			int y = getWindow(0).y1 + 10;
			int w = getWindow(0).x2 - x;
			int h = getWindow(0).y2 - y;

			int pageEntries = 0;
			for (int i = y + 20; i < y + h - 27; i += 10)
				pageEntries++;

			drawCenteredText(matrices, textRenderer, "Page " + (page + 1), x + 55, y + 5, 0xc0c0ff);

			fillButton(matrices, x + 10, y + h - 13, x + 99, y + h - 3, 0xff3a3a3a, 0xff353535, mouseX, mouseY);
			drawCenteredText(matrices, textRenderer, "Download Songs..", x + 55, y + h - 12, 0xc0dfdf);

			Song nbSong = ModuleManager.getModule(Notebot.class).song;
			int c = 0, c1 = -1;
			for (String s : files) {
				c1++;
				if (c1 < page * pageEntries)
					continue;
				if (c1 > (page + 1) * pageEntries)
					break;

				fillButton(matrices, x + 5, y + 15 + c * 10, x + 105, y + 25 + c * 10,
						nbSong != null && s.equals(nbSong.filename) ? 0xf0408040 : entry != null && s.equals(entry.filename) ? 0xf0202020 : 0xf0404040, 0xf0303030, mouseX, mouseY);

				drawCenteredText(matrices, textRenderer, textRenderer.trimToWidth(s, 100), x + 55, y + 16 + c * 10, -1);

				c++;
			}

			if (entry != null) {
				int textX = x + w - w / 4;
				drawCenteredText(matrices, textRenderer, entry.name, textX, y + 8, 0xffffff);
				drawCenteredText(matrices, textRenderer, "By: " + entry.author, textX, y + 18, 0xb0b0b0);
				
				drawCenteredText(matrices, textRenderer, "Format: \u00a7a" + entry.format, textX, y + 35, 0xb0b0b0);
				drawCenteredText(matrices, textRenderer, "Length: \u00a7f" +  entry.length / 20 + "s", textX, y + 45, 0xb0b0b0);
				//drawCenteredText(matrices, textRenderer, "Notes: \u00a7f" + entry.notes.size(), textX, y + 55, 0xb0b0b0);
				drawCenteredText(matrices, textRenderer, "Noteblocks: ", textX, y + 62, 0x80f080);

				int c2 = 0;
				for (Entry<Instrument, ItemStack> e : NotebotUtils.INSTRUMENT_TO_ITEM.entrySet()) {
					int count = (int) entry.requirements.stream().filter(n -> n.instrument == e.getKey().ordinal()).count();

					if (count != 0) {
						itemRenderer.zOffset = 500 - c2 * 20;
						drawCenteredText(matrices, textRenderer, StringUtils.capitalize(e.getKey().asString()) + " x" + count,
								textX, y + 74 + c2 * 10, 0x50f050);

						DiffuseLighting.enableGuiDepthLighting();
						itemRenderer.renderGuiItemIcon(e.getValue(), textX + 55, y + 70 + c2 * 10);
						DiffuseLighting.disableGuiDepthLighting();

						c2++;
					}
				}

				fillButton(matrices, x + w - w / 2 + 10, y + h - 15, x + w - w / 4, y + h - 5, 0xff903030, 0xff802020, mouseX, mouseY);
				fillButton(matrices, x + w - w / 4 + 5, y + h - 15, x + w - 5, y + h - 5, 0xff308030, 0xff207020, mouseX, mouseY);
				fillButton(matrices, x + w - w / 4 - w / 8, y + h - 27, x + w - w / 4 + w / 8, y + h - 17, 0xff303080, 0xff202070, mouseX, mouseY);

				int pixels = (int) Math.round(MathHelper.clamp((w / 4d) * ((double) playTick / (double) entry.length), 0, w / 4d));
				fill(matrices, x + w - w / 4 - w / 8, y + h - 27, (x + w - w / 4 - w / 8) + pixels, y + h - 17, 0x507050ff);

				drawCenteredText(matrices, textRenderer, "Delete", (int) (x + w - w / 2.8), y + h - 14, 0xff0000);
				drawCenteredText(matrices, textRenderer, "Select", x + w - w / 8, y + h - 14, 0x00ff00);
				drawCenteredText(matrices, textRenderer, playing ? "Previewing.." : "Preview", x + w - w / 4, y + h - 26, 0x6060ff);
			}
		}
	}

	public void tick() {
		if (entry != null && playing) {
			playTick++;
			NotebotUtils.playNote(entry.notes, playTick);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!getWindow(0).closed) {
			int x = getWindow(0).x1;
			int y = getWindow(0).y1 + 10;
			int w = getWindow(0).x2 - x;
			int h = getWindow(0).y2 - y;

			if (mouseX > x + 10 && mouseX < x + 99 && mouseY > y + h - 13 && mouseY < y + h - 3) {
				NotebotUtils.downloadSongs(true);
				init();
			}

			if (entry != null) {
				/* Pfft why use buttons when you can use meaningless rectangles with messy code */
				if (mouseX > x + w - w / 2 + 10 && mouseX < x + w - w / 4 && mouseY > y + h - 15 && mouseY < y + h - 5) {
					BleachFileMang.deleteFile("notebot/" + entry.filename);
					client.setScreen(this);
				}
				if (mouseX > x + w - w / 4 + 5 && mouseX < x + w - 5 && mouseY > y + h - 15 && mouseY < y + h - 5) {
					ModuleManager.getModule(Notebot.class).song = entry;
				}
				if (mouseX > x + w - w / 4 - w / 8 && mouseX < x + w - w / 4 + w / 8 && mouseY > y + h - 27 && mouseY < y + h - 17) {
					playing = !playing;
				}
			}

			int pageEntries = 0;
			for (int i = y + 20; i < y + h - 27; i += 10)
				pageEntries++;

			int c = 0;
			int c1 = -1;
			for (String s : files) {
				c1++;
				if (c1 < page * pageEntries)
					continue;
	
				if (mouseX > x + 5 && mouseX < x + 105 && mouseY > y + 15 + c * 10 && mouseY < y + 25 + c * 10) {
					entry = NotebotUtils.parse(BleachFileMang.getDir().resolve("notebot/" + s));
					playing = false;
					playTick = 0;
					break;
				}

				c++;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	private void fillButton(MatrixStack matrices, int x1, int y1, int x2, int y2, int color, int colorHover, int mouseX, int mouseY) {
		fill(matrices, x1, y1, x2, y2, (mouseX > x1 && mouseX < x2 && mouseY > y1 && mouseY < y2 ? colorHover : color));
	}
}

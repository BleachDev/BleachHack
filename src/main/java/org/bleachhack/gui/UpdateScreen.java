/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.apache.commons.io.FileUtils;
import org.bleachhack.BleachHack;
import org.bleachhack.gui.window.Window;
import org.bleachhack.gui.window.WindowScreen;
import org.bleachhack.gui.window.widget.*;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.collections.ImmutablePairList;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UpdateScreen extends WindowScreen {

	private Screen parent;
	private JsonObject updateJson;

	private WindowScrollbarWidget scrollbar;
	private Set<WindowWidget> changelogWidgets = new HashSet<>();

	private String updateResult = "";

	public UpdateScreen(Screen parent, JsonObject updateJson) {
		super(Text.literal("BleachHack Update Available"));
		this.parent = parent;
		this.updateJson = updateJson;
	}

	public void init() {
		super.init();

		int wd = Math.min(width / 2 - 30, 175);
		addWindow(new Window(width / 2 - wd,
				height / 16,
				width / 2 + wd,
				height - height / 16, String.format("BleachHack Update [%s -> %s]", BleachHack.VERSION, updateJson.get("name").getAsString()), new ItemStack(Items.MAGENTA_GLAZED_TERRACOTTA)));

		int w = getWindow(0).x2 - getWindow(0).x1;
		int h = getWindow(0).y2 - getWindow(0).y1;

		getWindow(0).addWidget(new WindowTextWidget("A new BleachHack update is available.", true, WindowTextWidget.TextAlign.MIDDLE, 1.5f, w / 2, 18, 0xe0e0e0));
		getWindow(0).addWidget(new WindowBoxWidget(3, 50, w - 3, h - 23));

		ImmutablePairList<String, Boolean> changelog = new ImmutablePairList<>();
		if (updateJson.has("changelog") && updateJson.get("changelog").isJsonArray()) {
			for (JsonElement je : updateJson.get("changelog").getAsJsonArray()) {
				if (je.isJsonPrimitive()) {
					String string = je.getAsString();
					if (string.charAt(0) == '-')
						string = "\u00a77-\u00a7r" + string.substring(1);

					List<StringVisitable> wrapped = client.textRenderer.getTextHandler().wrapLines(string, w - 32, Style.EMPTY);
					for (int i = 0; i < wrapped.size(); i++)
						changelog.add(wrapped.get(i).getString(), i == 0);
				}
			}
		} else {
			changelog.add("Could not find changelog.", false);
		}

		changelogWidgets.clear();
		changelogWidgets.add(
				getWindow(0).addWidget(new WindowTextWidget(updateJson.get("name").getAsString(), true, WindowTextWidget.TextAlign.MIDDLE, 2.5f, w / 2, 58, 0xe0e0e0)));

		for (int i = 0; i < changelog.size(); i++) {
			if (changelog.get(i).getValue()) {
				changelogWidgets.add(
						getWindow(0).addWidget(new WindowTextWidget("*", true, 10, 87 + i * 10, 0xa0a0a0)));
			}

			changelogWidgets.add(
					getWindow(0).addWidget(new WindowTextWidget(changelog.get(i).getKey(), true, 19, 85 + i * 10, 0xe0e0e0)));
		}

		scrollbar = getWindow(0).addWidget(new WindowScrollbarWidget(w - 14, 51, 37 + changelog.size() * 10, h - 75, 0));

		getWindow(0).addWidget(
				new WindowButtonWidget(3, h - 21, w / 2 - 2, h - 3, "Website", () ->
				Util.getOperatingSystem().open(URI.create("https://bleachhack.org/"))
						));

		getWindow(0).addWidget(
				new WindowButtonWidget(w / 2 + 2, h - 21, w - 3, h - 3, "Update", () -> {
					try {
						JsonObject installerJson = updateJson.get("installer").getAsJsonObject();

						if (installerJson.has("os")
								&& installerJson.get("os").isJsonPrimitive()
								&& !System.getProperty("os.name").startsWith(installerJson.get("os").getAsString())) {
							updateResult = "Updater doesn't support your OS!";
							selectWindow(1);
							return;
						}

						File modpath = new File(FabricLoader.getInstance().getModContainer("bleachhack").get().getOrigin().getPaths().get(0).toUri());

						if (!modpath.isFile()) {
							updateResult = "Invalid mod path!";
							selectWindow(1);
							return;
						}

						String link = installerJson.get("link").getAsString();
						String name = link.replaceFirst("^.*\\/", "");

						File installerFile = new File(System.getProperty("java.io.tmpdir"), name);

						BleachLogger.logger.info(
								"\n> Installer path: " + installerFile
								+ "\n> Installer URL: " + link
								+ "\n> Installer file name: " + name
								+ "\n> Regular File: " + Files.isRegularFile(installerFile.toPath())
								+ "\n> File Length: " + installerFile.length());

						if (!Files.isRegularFile(installerFile.toPath()) || installerFile.length() <= 1024L) {
							FileUtils.copyURLToFile(new URL(link), installerFile);
						}

						String execCommand = link.endsWith(".jar") ? "java -jar " : "cmd /c start ";
						Runtime.getRuntime().exec(execCommand
								+ installerFile.getAbsolutePath()
								+ " "
								+ modpath
								+ " "
								+ installerJson.get("url").getAsString());

						client.scheduleStop();
					} catch (Exception e) {
						updateResult = "Unknown error!";
						selectWindow(1);
						e.printStackTrace();
					}
				}));

		wd = Math.min(width / 2 - 20, 90);
		addWindow(new Window(width / 2 - wd,
				height / 2 - 15,
				width / 2 + wd,
				height / 2 + 15, "Error updating!", new ItemStack(Items.RED_BANNER), true));

		getWindow(1).addWidget(new WindowTextWidget("", true, WindowTextWidget.TextAlign.MIDDLE, wd, 16, 0xc05050)
				.withRenderEvent((wg, ms, wx, wy)
						-> ((WindowTextWidget) wg).setText(Text.literal(updateResult))));
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);

		int offset = scrollbar.getOffsetSinceRender();
		int wh = getWindow(0).y2 - getWindow(0).y1;
		for (WindowWidget widget: changelogWidgets) {
			widget.visible = widget.y1 >= 50 && widget.y2 <= wh - 22;
			widget.y1 -= offset;
			widget.y2 -= offset;
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

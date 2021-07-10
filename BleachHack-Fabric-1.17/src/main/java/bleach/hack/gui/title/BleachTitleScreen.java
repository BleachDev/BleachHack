/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.gui.title;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import bleach.hack.BleachHack;
import bleach.hack.gui.window.WindowScreen;
import bleach.hack.gui.window.widget.WindowButtonWidget;
import bleach.hack.gui.window.widget.WindowTextWidget;
import bleach.hack.gui.title.effect.ParticleManager;
import bleach.hack.gui.window.Window;
import bleach.hack.module.mods.UI;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.io.BleachFileHelper;
import bleach.hack.util.io.BleachGithubReader;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class BleachTitleScreen extends WindowScreen {

	private ParticleManager particleMang = new ParticleManager();
	public static boolean customTitleScreen = true;

	public static String splash = "";
	public static JsonObject version = null;

	private static String updaterText = "";

	public BleachTitleScreen() {
		super(new TranslatableText("narrator.screen.title"));
	}

	public void init() {
		super.init();

		if (version == null) {
			version = BleachGithubReader.readJson("update", SharedConstants.getGameVersion().getName().replace(' ', '_') + ".json");

			if (version == null) {
				version = new JsonObject();
			}
		}

		if (splash.isEmpty()) {
			List<String> sp = BleachGithubReader.readFileLines("splashes.txt");
			splash = !sp.isEmpty() ? sp.get(new Random().nextInt(sp.size())) : "";
		}

		clearWindows();
		addWindow(new Window(width / 8,
				height / 8,
				width - width / 8,
				height - height / 8, "BleachHack", new ItemStack(Items.MUSIC_DISC_CAT)));

		int w = getWindow(0).x2 - getWindow(0).x1;
		int h = getWindow(0).y2 - getWindow(0).y1;
		int maxY = MathHelper.clamp(h / 4 + 119, 0, h - 22);

		getWindow(0).addWidget(new WindowButtonWidget(w / 2 - 100, h / 4 + 38, w / 2 + 100, h / 4 + 58, I18n.translate("menu.singleplayer"), () -> {
			client.setScreen(new SelectWorldScreen(client.currentScreen));
		}));

		getWindow(0).addWidget(new WindowButtonWidget(w / 2 - 100, h / 4 + 62, w / 2 + 100, h / 4 + 82, I18n.translate("menu.multiplayer"), () -> {
			client.setScreen(new MultiplayerScreen(client.currentScreen));
		}));

		getWindow(0).addWidget(new WindowButtonWidget(w / 2 - 100, h / 4 + 86, w / 2 + 100, h / 4 + 106, I18n.translate("menu.online"), () -> {
			client.setScreen(new RealmsMainScreen(this));
		}));

		getWindow(0).addWidget(new WindowButtonWidget(w / 2 - 124, h / 4 + 86, w / 2 - 104, h / 4 + 106, "MC", () -> {
			customTitleScreen = !customTitleScreen;
			BleachFileHelper.saveMiscSetting("customTitleScreen", new JsonPrimitive(false));
			client.setScreen(new TitleScreen(false));
		}));

		getWindow(0).addWidget(new WindowButtonWidget(w / 2 - 100, maxY, w / 2 - 2, maxY + 20, I18n.translate("menu.options"), () -> {
			client.setScreen(new OptionsScreen(client.currentScreen, client.options));
		}));

		getWindow(0).addWidget(new WindowButtonWidget(w / 2 + 2, maxY, w / 2 + 100, maxY + 20, I18n.translate("menu.quit"), () -> {
			client.scheduleStop();
		}));

		// Main Text
		getWindow(0).addWidget(new WindowTextWidget(LiteralText.EMPTY, true, WindowTextWidget.TextAlign.MIDDLE, 3f, w / 2, h / 4 - 25, 0)
				.withRenderEvent(widget -> {
					MutableText bhText = new LiteralText("");

					int i = 0;
					for (char c: "BleachHack".toCharArray()) {
						int fi = i++;
						bhText.append(
								new LiteralText(String.valueOf(c)).styled(s -> s.withColor(TextColor.fromRgb(UI.getRainbowFromSettings(fi)))));
					}

					((WindowTextWidget) widget).setText(bhText);
				}));

		// Version Text
		getWindow(0).addWidget(new WindowTextWidget(BleachHack.VERSION, true, WindowTextWidget.TextAlign.MIDDLE, 1.5f, w / 2, h / 4 - 6, 0xffc050));

		// Splash
		if (!splash.isEmpty()) {
			getWindow(0).addWidget(new WindowTextWidget(new LiteralText(splash), true, WindowTextWidget.TextAlign.MIDDLE, 2f, -20f, w / 2 + 80, h / 4 + 6, 0xffff00)
					.withRenderEvent(widget -> {
						float scale = 1.8F - MathHelper.abs(MathHelper.sin(Util.getMeasuringTimeMs() % 1000L / 1000.0F * 6.2831855F) * 0.1F);
						scale = scale * 66.0F / (textRenderer.getWidth(splash) + 32);
						((WindowTextWidget) widget).setScale(scale);
					}));
		}

		// Update Text
		if (version != null && version.has("version") && version.get("version").getAsInt() > BleachHack.INTVERSION) {
			getWindow(0).addWidget(new WindowTextWidget("\u00a76\u00a7nUpdate\u00a76", true, 4, h - 12, 0xffffff)
					.withClickEvent(widget -> {
						getWindow(1).closed = false;
						selectWindow(1);
					}));
		}

		addWindow(new Window(width / 2 - 100,
				height / 2 - 70,
				width / 2 + 100,
				height / 2 + 70, "Update", new ItemStack(Items.MAGENTA_GLAZED_TERRACOTTA),
				!(version != null && version.has("version") && version.get("version").getAsInt() > BleachHack.INTVERSION)) {

			protected void drawBar(MatrixStack matrices, int mouseX, int mouseY, TextRenderer textRend) {
				super.drawBar(matrices, mouseX, mouseY, textRend);

				Window.verticalGradient(matrices, x1 + 1, y1 + 12, x2 - 1, y2 - 1, 0xff606090, 0x00606090);
			}
		});

		getWindow(1).addWidget(
				new WindowButtonWidget(5, 115, 95, 135, "Update", () -> {
					try {
						if (!SystemUtils.IS_OS_WINDOWS) {
							updaterText = "Updater only supports Windows";
							return;
						}

						File modpath = new File(((ModContainer) FabricLoader.getInstance().getModContainer("bleachhack").get()).getOriginUrl().toURI());

						if (!modpath.isFile()) {
							updaterText = "Invalid mod path";
							return;
						}

						if (version == null || !version.has("installer")) {
							updaterText = "Invalid metadata json";
							return;
						}

						String link = version.get("installer").getAsJsonObject().get("link").getAsString();
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

						Runtime.getRuntime().exec("cmd /c start "
								+ installerFile.getAbsolutePath().toString()
								+ " "
								+ modpath.toString()
								+ " "
								+ version.get("installer").getAsJsonObject().get("url").getAsString());

						client.scheduleStop();
					} catch (Exception e) {
						updaterText = "Unknown error";
						e.printStackTrace();
					}
				}));

		getWindow(1).addWidget(
				new WindowButtonWidget(105, 115, 195, 135, "Github", () -> {
					Util.getOperatingSystem().open(URI.create("https://github.com/BleachDrinker420/BleachHack/"));
				}));

		getWindow(1).addWidget(new WindowTextWidget("\u00a7cOutdated BleachHack version!", true, WindowTextWidget.TextAlign.MIDDLE, 100, 15, 0xffffff));
		getWindow(1).addWidget(new WindowTextWidget("\u00a7eClick update to auto-update", true, WindowTextWidget.TextAlign.MIDDLE, 100, 28, 0xffffff));
		getWindow(1).addWidget(new WindowTextWidget("\u00a7eOr Github to manually update", true, WindowTextWidget.TextAlign.MIDDLE, 100, 38, 0xffffff));
		getWindow(1).addWidget(new WindowTextWidget("\u00a7c\u00a7o" + updaterText, true, WindowTextWidget.TextAlign.MIDDLE, 100, 58, 0xffffff));
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);

		int copyWidth = this.textRenderer.getWidth("Copyright Mojang AB. Do not distribute!") + 2;
		textRenderer.drawWithShadow(matrices, "Copyright Mojang AB. Do not distribute!", width - copyWidth, height - 10, -1);
		textRenderer.drawWithShadow(matrices, "Fabric: " + FabricLoader.getInstance().getModContainer("fabricloader").get().getMetadata().getVersion().getFriendlyString(),
				4, height - 30, -1);
		textRenderer.drawWithShadow(matrices, "Minecraft: " + SharedConstants.getGameVersion().getName(), 4, height - 20, -1);
		textRenderer.drawWithShadow(matrices, "Logged in as: \u00a7a" + client.getSession().getUsername(), 4, height - 10, -1);

		super.render(matrices, mouseX, mouseY, delta);

		particleMang.addParticle(mouseX, mouseY);
		particleMang.renderParticles(matrices);

	}
}

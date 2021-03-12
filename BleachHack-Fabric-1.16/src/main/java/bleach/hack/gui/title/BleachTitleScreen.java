/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
 * Copyright (c) 2019 Bleach.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.gui.title;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.opengl.GL11;
import bleach.hack.BleachHack;
import bleach.hack.gui.particle.ParticleManager;
import bleach.hack.gui.window.WindowScreen;
import bleach.hack.gui.window.Window;
import bleach.hack.gui.window.WindowButton;
import bleach.hack.module.mods.UI;
import bleach.hack.util.file.BleachFileHelper;
import bleach.hack.util.file.BleachGithubReader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.options.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsBridgeScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class BleachTitleScreen extends WindowScreen {

	private ParticleManager particleMang = new ParticleManager();
	public static boolean customTitleScreen = true;

	public static String splash = "";
	public static final List<String> versions = new ArrayList<>();

	public BleachTitleScreen() {
		super(new TranslatableText("narrator.screen.title"));
	}

	public void init() {
		super.init();

		clearWindows();
		addWindow(new Window(width / 8,
				height / 8,
				width / 8 + (width - width / 4),
				height / 8 + (height - height / 4), "BleachHack", new ItemStack(Items.MUSIC_DISC_CAT)));

		int w = getWindow(0).x2 - getWindow(0).x1,
				h = getWindow(0).y2 - getWindow(0).y1;
		int maxY = MathHelper.clamp(h / 4 + 119, 0, h - 22);

		getWindow(0).buttons.add(
				new WindowButton(w / 2 - 100, h / 4 + 38, w / 2 + 100, h / 4 + 58, I18n.translate("menu.singleplayer"), () -> {
					client.openScreen(new SelectWorldScreen(this));
				}));
		getWindow(0).buttons.add(
				new WindowButton(w / 2 - 100, h / 4 + 62, w / 2 + 100, h / 4 + 82, I18n.translate("menu.multiplayer"), () -> {
					client.openScreen(new MultiplayerScreen(this));
				}));

		getWindow(0).buttons.add(
				new WindowButton(w / 2 - 100, h / 4 + 86, w / 2 + 100, h / 4 + 106, I18n.translate("menu.online"), () -> {
					RealmsBridgeScreen realmsBridgeScreen = new RealmsBridgeScreen();
					realmsBridgeScreen.switchToRealms(this);
				}));
		getWindow(0).buttons.add(
				new WindowButton(w / 2 - 124, h / 4 + 86, w / 2 - 104, h / 4 + 106, "MC", () -> {
					customTitleScreen = !customTitleScreen;
					BleachFileHelper.saveMiscSetting("customTitleScreen", "false");
					client.openScreen(new TitleScreen(false));
				}));
		getWindow(0).buttons.add(
				new WindowButton(w / 2 - 100, maxY, w / 2 - 2, maxY + 20, I18n.translate("menu.options"), () -> {
					client.openScreen(new OptionsScreen(this, client.options));
				}));
		getWindow(0).buttons.add(
				new WindowButton(w / 2 + 2, maxY, w / 2 + 100, maxY + 20, I18n.translate("menu.quit"), () -> {
					client.scheduleStop();
				}));

		if (versions.isEmpty()) {
			versions.clear();
			versions.addAll(BleachGithubReader.readFileLines("latestversion.txt"));
		}

		if (splash.isEmpty()) {
			List<String> sp = BleachGithubReader.readFileLines("splashes.txt");
			splash = !sp.isEmpty() ? sp.get(new Random().nextInt(sp.size())) : "";
		}
	}

	public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrix);

		int copyWidth = this.textRenderer.getWidth("Copyright Mojang AB. Do not distribute!") + 2;

		textRenderer.drawWithShadow(matrix, "Copyright Mojang AB. Do not distribute!", width - copyWidth, height - 10, -1);
		textRenderer.drawWithShadow(matrix, "Fabric: " + FabricLoader.getInstance().getModContainer("fabricloader").get().getMetadata().getVersion().getFriendlyString(),
				4, height - 30, -1);
		textRenderer.drawWithShadow(matrix, "Minecraft: " + SharedConstants.getGameVersion().getName(), 4, height - 20, -1);
		textRenderer.drawWithShadow(matrix, "Logged in as: \u00a7a" + client.getSession().getUsername(), 4, height - 10, -1);

		try {
			if (Integer.parseInt(versions.get(1)) > BleachHack.INTVERSION) {
				drawCenteredString(matrix, this.textRenderer, "\u00a7cOutdated BleachHack Version!", width / 2, 2, -1);
				drawCenteredString(matrix, this.textRenderer, "\00a74\u00a7n[Update]", width / 2, 11, -1);
			}
		} catch (Exception e) {
		}

		super.render(matrix, mouseX, mouseY, delta);

		particleMang.addParticle(mouseX, mouseY);
		particleMang.renderParticles(matrix);

	}

	public void onRenderWindow(MatrixStack matrix, int window, int mX, int mY) {
		super.onRenderWindow(matrix, window, mX, mY);

		if (window == 0) {
			int x = getWindow(0).x1,
					y = getWindow(0).y1 - 10,
					w = width - width / 4,
					h = height - height / 4;

			/* Main Text */
			GL11.glPushMatrix();
			GL11.glScaled(3, 3, 0);

			// drawString(this.font, "BleachHack", (x + w/2 - 81)/3, (y + h/4 - 15)/3,
			// 0xffc0e0);
			int[] intarray = { 7, 13, 16, 22, 28, 34, 40, 46, 52, 58 };
			String[] bruh = { "B", "l", "e", "a", "c", "h", "H", "a", "c", "k" };
			for (int i = 0; i < bruh.length; i++) {
				drawStringWithShadow(matrix, this.textRenderer, bruh[i], (x + w / 2 - 81) / 3 + intarray[i] - 8, (y + h / 4 - 15) / 3, UI.getRainbowFromSettings(i * 25));
			}

			GL11.glScaled(1d / 3d, 1d / 3d, 0);

			/* Version Text */
			GL11.glScaled(1.5, 1.5, 1.5);
			drawCenteredString(matrix, this.textRenderer, BleachHack.VERSION, (int) ((x + w / 2) / 1.5), (int) ((y + h / 4 + 6) / 1.5), 0xffc050);
			GL11.glScaled(1 / 1.5, 1 / 1.5, 1 / 1.5);

			/* Splash Text */
			GL11.glTranslated(x + w / 2 + 80, y + h / 4 + 8, 0.0F);
			GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
			float float_4 = 1.8F - MathHelper.abs(MathHelper.sin(Util.getMeasuringTimeMs() % 1000L / 1000.0F * 6.2831855F) * 0.1F);
			float_4 = float_4 * 60.0F / (textRenderer.getWidth(splash) + 32);
			GL11.glScalef(float_4, float_4, float_4);
			DrawableHelper.drawCenteredString(matrix, textRenderer, splash, 0, -8, 16776960);
			GL11.glPopMatrix();
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (versions.size() >= 2 && NumberUtils.toInt(versions.get(1), Integer.MAX_VALUE) > BleachHack.INTVERSION) {
			if (mouseX > width / 2 - 80 && mouseX < width / 2 + 80 && mouseY > 0 && mouseY < 20) {
				Util.getOperatingSystem().open(URI.create("https://github.com/BleachDrinker420/bleachhack-1.14/releases"));
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}
}

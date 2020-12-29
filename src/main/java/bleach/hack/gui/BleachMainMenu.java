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
package bleach.hack.gui;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import bleach.hack.BleachHack;
import bleach.hack.gui.particle.ParticleManager;
import bleach.hack.gui.widget.BleachCheckbox;
import bleach.hack.gui.widget.TextPassFieldWidget;
import bleach.hack.gui.window.AbstractWindowScreen;
import bleach.hack.gui.window.Window;
import bleach.hack.gui.window.WindowButton;
import bleach.hack.module.mods.UI;
import bleach.hack.utils.Decrypter;
import bleach.hack.utils.LoginManager;
import bleach.hack.utils.file.BleachFileHelper;
import bleach.hack.utils.file.BleachFileMang;
import bleach.hack.utils.file.BleachGithubReader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.options.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class BleachMainMenu extends AbstractWindowScreen {

	private ParticleManager particleMang = new ParticleManager();
	public static boolean customTitleScreen = true;

	public static String splash = "";
	public static List<String> versions = new ArrayList<>();

	/* login manager variables */
	public TextFieldWidget userField;
	public TextPassFieldWidget passField;
	public BleachCheckbox checkBox;

	public String loginResult = "";

	private List<List<String>> entries = new ArrayList<>();

	public BleachMainMenu() {
		super(new TranslatableText("narrator.screen.title"));
	}

	public void init() {
		windows.clear();
		windows.add(new Window(width / 8,
				height / 8,
				width / 8 + (width - width / 4),
				height / 8 + (height - height / 4), "BleachHack", new ItemStack(Items.MUSIC_DISC_CAT)));
		windows.add(new Window(width / 8 + 15,
				height / 8 + 15,
				width / 8 + 15 + (width - width / 2),
				height / 8 + 15 + (height - height / 2), "Login Manager", new ItemStack(Items.PAPER), true));
		windows.add(new Window(width / 8 + 30,
				height / 8 + 30,
				width / 8 + 30 + (width - width / 2),
				height / 8 + 30 + (height - height / 2), "Accounts", new ItemStack(Items.WRITABLE_BOOK), true));

		int w = windows.get(0).x2 - windows.get(0).x1,
				h = windows.get(0).y2 - windows.get(0).y1;
		int maxY = MathHelper.clamp(h / 4 + 119, 0, h - 22);

		windows.get(0).buttons.add(
				new WindowButton(w / 2 - 100, h / 4 + 38, w / 2 + 100, h / 4 + 58, I18n.translate("menu.singleplayer"), () -> {
					client.openScreen(new SelectWorldScreen(this));
				}));
		windows.get(0).buttons.add(
				new WindowButton(w / 2 - 100, h / 4 + 62, w / 2 + 100, h / 4 + 82, I18n.translate("menu.multiplayer"), () -> {
					client.openScreen(new MultiplayerScreen(this));
				}));
		windows.get(0).buttons.add(
				new WindowButton(w / 2 - 100, h / 4 + 86, w / 2 - 2, h / 4 + 106, "MC Menu", () -> {
					customTitleScreen = !customTitleScreen;
					BleachFileHelper.saveMiscSetting("customTitleScreen", "false");
					client.openScreen(new TitleScreen(false));
				}));
		windows.get(0).buttons.add(
				new WindowButton(w / 2 + 2, h / 4 + 86, w / 2 + 100, h / 4 + 106, "Login Manager", () -> {
					windows.get(1).closed = false;
					selectWindow(1);
				}));
		windows.get(0).buttons.add(
				new WindowButton(w / 2 - 100, maxY, w / 2 - 2, maxY + 20, I18n.translate("menu.options"), () -> {
					client.openScreen(new OptionsScreen(this, client.options));
				}));
		windows.get(0).buttons.add(
				new WindowButton(w / 2 + 2, maxY, w / 2 + 100, maxY + 20, I18n.translate("menu.quit"), () -> {
					client.scheduleStop();
				}));

		int x = windows.get(1).x1;
		int y = windows.get(1).y1;
		w = width - width / 2;
		h = height - height / 2;

		if (userField == null)
			userField = new TextFieldWidget(textRenderer, x + w / 2 - 98, y + h / 4, 196, 18, LiteralText.EMPTY);
		if (passField == null)
			passField = new TextPassFieldWidget(textRenderer, x + w / 2 - 98, y + h / 4 + 30, 196, 18, LiteralText.EMPTY);
		userField.x = x + w / 2 - 98;
		userField.y = y + h / 4;
		passField.x = x + w / 2 - 98;
		passField.y = y + h / 4 + 30;
		if (checkBox == null)
			checkBox = new BleachCheckbox(x + w / 2 - 99, y + h / 4 + 53, new LiteralText("Save Login"), false);
		checkBox.x = x + w / 2 - 99;
		checkBox.y = y + h / 4 + 53;
		userField.setMaxLength(32767);
		passField.setMaxLength(32767);

		windows.get(1).buttons.add(
				new WindowButton(w / 2 - 100, h / 3 + 84, w / 2 + 100, h / 3 + 104, "Done", () -> {
					windows.get(1).closed = true;
					selectWindow(1);
				}));
		windows.get(1).buttons.add(
				new WindowButton(w / 2 - 100, h / 3 + 62, w / 2 - 2, h / 3 + 82, "Accounts", () -> {
					windows.get(2).closed = false;
					selectWindow(2);
				}));
		windows.get(1).buttons.add(
				new WindowButton(w / 2 + 2, h / 3 + 62, w / 2 + 100, h / 3 + 82, "Login", () -> {
					for (String s : BleachFileMang.readFileLines("logins.txt")) {
						entries.add(new ArrayList<>(Arrays.asList(s.split(":"))));
					}

					loginResult = LoginManager.login(userField.getText(), passField.getText());
					try {
						Decrypter decrypter = new Decrypter(Decrypter.getPassPhrase());
						String text = userField.getText() + ":" + decrypter.encrypt(passField.getText());

						if (checkBox.checked && (loginResult.equals("\u00a7aLogin Successful")
								|| loginResult.equals("\u00a76Logged in as an unverified account"))
								&& !entries.contains(new ArrayList<>(Arrays.asList(text.split(":"))))) {
							entries.add(new ArrayList<>(Arrays.asList(text.split(":"))));
							BleachFileMang.createFile("logins.txt");
							BleachFileMang.appendFile(text, "logins.txt");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}));

		if (versions.isEmpty()) {
			versions.clear();
			versions.addAll(BleachGithubReader.readFileLines("latestversion.txt"));
		}

		if (splash.isEmpty()) {
			List<String> sp = BleachGithubReader.readFileLines("splashes.txt");
			splash = !sp.isEmpty() ? sp.get(new Random().nextInt(sp.size())) : "";
		}

		entries.clear();
		BleachFileMang.createFile("logins.txt");

		for (String s : BleachFileMang.readFileLines("logins.txt")) {
			entries.add(new ArrayList<>(Arrays.asList(s.split(":"))));
		}
	}

	public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrix);
		fill(matrix, 0, 0, width, height, 0xff008080);

		int copyWidth = this.textRenderer.getWidth("Copyright Mojang AB. Do not distribute!") + 2;

		textRenderer.drawWithShadow(matrix, "Copyright Mojang AB. Do not distribute!", width - copyWidth, height - 24, -1);
		textRenderer.drawWithShadow(matrix, "Fabric: " + FabricLoader.getInstance().getModContainer("fabricloader").get().getMetadata().getVersion().getFriendlyString(),
				4, height - 44, -1);
		textRenderer.drawWithShadow(matrix, "Minecraft " + SharedConstants.getGameVersion().getName(), 4, height - 34, -1);
		textRenderer.drawWithShadow(matrix, "Logged in as: \u00a7a" + client.getSession().getUsername(), 4, height - 24, -1);

		try {
			if (Integer.parseInt(versions.get(1)) > BleachHack.INTVERSION) {
				drawCenteredString(matrix, this.textRenderer, "\u00a7cOutdated BleachHack Version!", width / 2, 2, -1);
				drawCenteredString(matrix, this.textRenderer, "\00a74\u00a7n[Update]", width / 2, 11, -1);
			}
		} catch (Exception e) {
		}

		drawButton(matrix, "", 0, height - 14, width, height);
		drawButton(matrix, "\u00a7cX", 0, height - 13, 20, height - 1);

		int wid = 20;
		for (Window w : windows) {
			if (w.closed)
				continue;
			DrawableHelper.fill(matrix, wid, height - 13, wid + 80 - 1, height - 1 - 1, 0xffb0b0b0);
			DrawableHelper.fill(matrix, wid + 1, height - 13 + 1, wid + 80, height - 1, 0xff000000);
			DrawableHelper.fill(matrix, wid + 1, height - 13 + 1, wid + 80 - 1, height - 1 - 1, (w.selected ? 0xffb0b0b0 : 0xff858585));
			textRenderer.draw(matrix, w.title, wid + 2, height - 11, 0x000000);
			wid += 80;
		}

		super.render(matrix, mouseX, mouseY, delta);

		particleMang.addParticle(mouseX, mouseY);
		particleMang.renderParticles(matrix);

	}

	public void onRenderWindow(MatrixStack matrix, int window, int mX, int mY) {
		super.onRenderWindow(matrix, window, mX, mY);

		if (window == 0) {
			int x = windows.get(0).x1,
					y = windows.get(0).y1 - 10,
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
		} else if (window == 1) {
			int x = windows.get(1).x1,
					y = windows.get(1).y1 - 10,
					w = width - width / 2,
					h = height - height / 2;

			drawStringWithShadow(matrix, textRenderer, "Email: ", x + w / 2 - 130, y + h / 4 + 15, 0xC0C0C0);
			drawStringWithShadow(matrix, textRenderer, "Password: ", x + w / 2 - 154, y + h / 4 + 45, 0xC0C0C0);

			drawStringWithShadow(matrix, textRenderer, loginResult.isEmpty() ? "" : "|  " + loginResult, x + w / 2 - 24, y + h / 4 + 65, 0xC0C0C0);

			userField.x = x + w / 2 - 98;
			userField.y = y + h / 4 + 10;
			passField.x = x + w / 2 - 98;
			passField.y = y + h / 4 + 40;
			checkBox.x = x + w / 2 - 99;
			checkBox.y = y + h / 4 + 63;

			userField.render(matrix, mX, mY, 1f);
			passField.render(matrix, mX, mY, 1f);
			checkBox.render(matrix, mX, mY, 1f);
		} else if (window == 2) {
			int x = windows.get(2).x1,
					y = windows.get(2).y1 - 10,
					w = width - width / 2,
					h = height - height / 2;

			drawCenteredString(matrix, textRenderer, "\u00a7cTemporary\u2122 alt manager", x + w / 2, y + h / 4 - 30, -1);
			drawCenteredString(matrix, textRenderer, "\u00a74(accounts stored in plaintext for now)", x + w / 2, y + h / 4 - 20, -1);

			int c = 0;
			for (List<String> e : entries) {
				String text = (e.size() > 1 ? "\u00a7a" + e.get(0) + ":***" : "\u00a76" + e.get(0));
				int length = client.textRenderer.getWidth(text);

				fill(matrix, x + w / 2 - length / 2 - 1, y + h / 4 + c - 2, x + w / 2 + length / 2 + 1, y + h / 4 + c - 1, 0xFF303030);
				fill(matrix, x + w / 2 - length / 2 - 1, y + h / 4 + c + 9, x + w / 2 + length / 2 + 1, y + h / 4 + c + 10, 0xFF303030);
				fill(matrix, x + w / 2 - length / 2 - 2, y + h / 4 + c - 2, x + w / 2 - length / 2 - 1, y + h / 4 + c + 10, 0xFF303030);
				fill(matrix, x + w / 2 + length / 2 + 1, y + h / 4 + c - 2, x + w / 2 + length / 2 + 2, y + h / 4 + c + 10, 0xFF303030);
				drawCenteredString(matrix, textRenderer, "\u00a7cx", x + w / 2 + length / 2 + 9, y + h / 4 + c, -1);
				drawCenteredString(matrix, textRenderer, text, x + w / 2, y + h / 4 + c, -1);
				c += 14;
			}
		}
	}

	public boolean mouseClicked(double double_1, double double_2, int int_1) {
		try {
			if (Integer.parseInt(versions.get(1)) > BleachHack.INTVERSION) {
				if (double_1 > width / 2 - 80 && double_1 < width / 2 + 80 && double_2 > 0 && double_2 < 20) {
					Util.getOperatingSystem().open(new URI("https://github.com/BleachDrinker420/bleachhack-1.14/releases"));
				}
			}
		} catch (Exception e) {
		}

		if (double_1 > 0 && double_1 < 20 && double_2 > height - 14 && double_2 < height) {
			client.openScreen(this);
		}

		if (double_2 > height - 14 && double_2 < height) {
			int count = 0;
			for (Window w : windows) {
				if (!w.closed)
					count++;
				if (count == (int) ((double_1 + 60) / 80)) {
					selectWindow(windows.indexOf(w));
					// w.selected = true;
					break;
				}
			}
		}

		if (!windows.get(1).closed && windows.get(1).selected) {
			userField.mouseClicked(double_1, double_2, int_1);
			passField.mouseClicked(double_1, double_2, int_1);

			if (double_1 > checkBox.x && double_1 < checkBox.x + 10 && double_2 > checkBox.y && double_2 < checkBox.y + 10) {
				checkBox.checked = !checkBox.checked;
			}
		} else if (!windows.get(2).closed && windows.get(2).selected) {
			int x = windows.get(2).x1,
					y = windows.get(2).y1 - 10,
					w = width - width / 2,
					h = height - height / 2;

			int c = 0;
			for (List<String> e : new ArrayList<>(entries)) {
				String text = (e.size() > 1 ? "\u00a7a" + e.get(0) + ":***" : "\u00a76" + e.get(0));
				int length = client.textRenderer.getWidth(text);

				if (double_1 > x + w / 2 - length / 2 - 1 && double_1 < x + w / 2 + length / 2 + 1 && double_2 > y + h / 4 + c * 14 - 2
						&& double_2 < y + h / 4 + c * 14 + 11) {
					try {
						userField.setText(e.get(0));
					} catch (Exception e1) {
						userField.setText("");
					}
					try {
						Decrypter decrypter = new Decrypter(Decrypter.getPassPhrase());
						passField.setText(decrypter.decrypt(e.get(1)));
					} catch (Exception e1) {
						passField.setText("");
						e1.printStackTrace();
					}
					windows.get(2).closed = true;
					windows.get(1).closed = false;
					selectWindow(1);
				}

				if (double_1 > x + w / 2 + length / 2 + 4 && double_1 < x + w / 2 + length / 2 + 14 && double_2 > y + h / 4 + c * 14 - 2
						&& double_2 < y + h / 4 + c * 14 + 11) {
					int c1 = 0;
					String lines = "";
					for (String l : BleachFileMang.readFileLines("logins.txt")) {
						if (l.trim().replace("\r", "").replace("\n", "").isEmpty())
							continue;
						if (c1 != c)
							lines += l + "\r\n";
						c1++;
					}
					BleachFileMang.createEmptyFile("logins.txt");
					BleachFileMang.appendFile(lines, "logins.txt");
					break;
				}
				c++;
			}
		}

		return super.mouseClicked(double_1, double_2, int_1);
	}

	public boolean charTyped(char char_1, int int_1) {
		if (!windows.get(1).closed) {
			if (userField.isFocused())
				userField.charTyped(char_1, int_1);
			if (passField.isFocused())
				passField.charTyped(char_1, int_1);
		}

		return super.charTyped(char_1, int_1);
	}

	public void tick() {
		if (!windows.get(1).closed) {
			userField.tick();
			passField.tick();
		}
	}

	public boolean keyPressed(int int_1, int int_2, int int_3) {
		if (!windows.get(1).closed) {
			if (userField.isFocused())
				userField.keyPressed(int_1, int_2, int_3);
			if (passField.isFocused())
				passField.keyPressed(int_1, int_2, int_3);
		}

		return super.keyPressed(int_1, int_2, int_3);
	}
}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import bleach.hack.gui.widget.BleachCheckbox;
import bleach.hack.gui.widget.TextPassFieldWidget;
import bleach.hack.gui.window.WindowScreen;
import bleach.hack.gui.window.Window;
import bleach.hack.gui.window.WindowButton;
import bleach.hack.utils.Decrypter;
import bleach.hack.utils.LoginManager;
import bleach.hack.utils.file.BleachFileMang;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;

public class AccountManagerScreen extends WindowScreen {

	/* login manager variables */
	public TextFieldWidget userField;
	public TextPassFieldWidget passField;
	public BleachCheckbox checkBox;

	public String loginResult = "";

	private List<List<String>> entries = new ArrayList<>();

	public AccountManagerScreen() {
		super(new LiteralText("Account Manager"));
	}

	public void init() {
		clearWindows();
		addWindow(new Window(width / 8,
				height / 8,
				width / 8 + (width - width / 4),
				height / 8 + (height - height / 4), "Login Manager", new ItemStack(Items.PAPER), false));
		addWindow(new Window(width / 8 + 15,
				height / 8 + 15,
				width / 8 + 15 + (width - width / 4),
				height / 8 + 15 + (height - height / 4), "Accounts", new ItemStack(Items.WRITABLE_BOOK), true));

		int x = getWindow(0).x1;
		int y = getWindow(0).y1;
		int w = width - width / 4;
		int h = height - height / 4;

		if (userField == null)
			userField = new TextFieldWidget(textRenderer, x + w / 2 - 98, y + h / 4, 196, 18, LiteralText.EMPTY);

		if (passField == null)
			passField = new TextPassFieldWidget(textRenderer, x + w / 2 - 98, y + h / 4 + 30, 196, 18, LiteralText.EMPTY);

		if (checkBox == null)
			checkBox = new BleachCheckbox(x + w / 2 - 99, y + h / 4 + 53, new LiteralText("Save Login"), false);

		checkBox.x = x + w / 2 - 99;
		checkBox.y = y + h / 4 + 53;
		userField.setMaxLength(32767);
		passField.setMaxLength(32767);

		getWindow(0).buttons.add(
				new WindowButton(w / 2 - 100, h / 3 + 84, w / 2 + 100, h / 3 + 104, "Done", () -> {
					//getWindow(0).closed = true;
					onClose();
				}));
		getWindow(0).buttons.add(
				new WindowButton(w / 2 - 100, h / 3 + 62, w / 2 - 2, h / 3 + 82, "Accounts", () -> {
					getWindow(1).closed = false;
					selectWindow(1);
				}));
		getWindow(0).buttons.add(
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

		entries.clear();
		BleachFileMang.createFile("logins.txt");

		for (String s : BleachFileMang.readFileLines("logins.txt")) {
			entries.add(new ArrayList<>(Arrays.asList(s.split(":"))));
		}
	}

	public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, delta);
	}

	public void onRenderWindow(MatrixStack matrix, int window, int mX, int mY) {
		super.onRenderWindow(matrix, window, mX, mY);

		if (window == 0) {
			int x = getWindow(0).x1,
					y = getWindow(0).y1 - 10,
					w = width - width / 4,
					h = height - height / 4;

			drawStringWithShadow(matrix, textRenderer, "Email: ", x + w / 2 - 130, y + h / 4 + 15, 0xC0C0C0);
			drawStringWithShadow(matrix, textRenderer, "Pass: ", x + w / 2 - 131, y + h / 4 + 45, 0xC0C0C0);

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
		} else if (window == 1) {
			int x = getWindow(1).x1,
					y = getWindow(1).y1,
					w = width - width / 4,
					h = height - height / 4;

			drawCenteredString(matrix, textRenderer, "\u00a7cTemporary\u2122 alt manager", x + w / 2, y + h / 4 - 30, -1);

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
		if (!getWindow(0).closed && getWindow(0).selected) {
			userField.mouseClicked(double_1, double_2, int_1);
			passField.mouseClicked(double_1, double_2, int_1);

			if (double_1 > checkBox.x && double_1 < checkBox.x + 10 && double_2 > checkBox.y && double_2 < checkBox.y + 10) {
				checkBox.checked = !checkBox.checked;
			}
		} else if (!getWindow(1).closed && getWindow(1).selected) {
			int x = getWindow(1).x1,
					y = getWindow(1).y1 - 10,
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
					getWindow(1).closed = true;
					getWindow(0).closed = false;
					selectWindow(0);
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
		if (!getWindow(0).closed) {
			if (userField.isFocused()) userField.charTyped(char_1, int_1);
			if (passField.isFocused()) passField.charTyped(char_1, int_1);
		}

		return super.charTyped(char_1, int_1);
	}

	public void tick() {
		if (!getWindow(0).closed) {
			userField.tick();
			passField.tick();
		}
	}

	public boolean keyPressed(int int_1, int int_2, int int_3) {
		if (!getWindow(0).closed) {
			if (userField.isFocused()) userField.keyPressed(int_1, int_2, int_3);
			if (passField.isFocused()) passField.keyPressed(int_1, int_2, int_3);
		}

		return super.keyPressed(int_1, int_2, int_3);
	}
}

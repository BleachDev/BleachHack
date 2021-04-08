/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import bleach.hack.gui.widget.BleachCheckbox;
import bleach.hack.gui.widget.TextPassFieldWidget;
import bleach.hack.gui.window.WindowScreen;
import bleach.hack.util.auth.LoginCrypter;
import bleach.hack.util.auth.LoginManager;
import bleach.hack.util.file.BleachFileMang;
import bleach.hack.gui.window.Window;
import bleach.hack.gui.window.WindowButton;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.Session;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public class AccountManagerScreen extends WindowScreen {

	private static final LoginCrypter crypter = new LoginCrypter(LoginCrypter.getPassPhrase());

	private static final ExecutorService accountExecutor = Executors.newFixedThreadPool(4);
	private static final List<Future<Account>> accountFutures = new ArrayList<>();
	private static final Queue<Account> accountQueue = new ArrayDeque<>();

	private static int accHeight = 32;
	private static int accStart = 20;

	private static AccountList accounts = null;

	public TextFieldWidget userField;
	public TextPassFieldWidget passField;
	public BleachCheckbox checkBox = new BleachCheckbox(0, 0, new LiteralText("Save Login"), false);

	public String loginResult = "";

	public AccountManagerScreen() {
		super(new LiteralText("Account Manager"));
	}

	public void init() {
		super.init();

		clearWindows();
		addWindow(new Window(width / 8,
				height / 8,
				width / 8 + (width - width / 4),
				height / 8 + (height - height / 4), "Login Manager", new ItemStack(Items.PAPER), false));
		addWindow(new Window(width / 8 + 15,
				height / 8 + 15,
				width / 8 + 15 + (width - width / 3),
				height / 8 + 15 + (height - height / 3), "Accounts", new ItemStack(Items.WRITABLE_BOOK), true));

		int w = width - width / 4;
		int h = height - height / 4;

		if (userField == null) {
			userField = new TextFieldWidget(textRenderer, 0, 0, 196, 18, LiteralText.EMPTY);
			userField.setMaxLength(32767);
		}

		if (passField == null) {
			passField = new TextPassFieldWidget(textRenderer, 0, 0, 196, 18, LiteralText.EMPTY);
			passField.setMaxLength(32767);
		}

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
					Pair<String, Session> login = LoginManager.login(userField.getText(), passField.getText());
					loginResult = login.getLeft();

					try {
						String email = userField.getText();
						String pass = passField.getText();
						Session session = login.getRight();

						if (checkBox.checked && login.getRight() != null && !accounts.hasEmail(email)) {
							accountQueue.add(new Account(email, pass, session.getUuid(), session.getUsername()));
							BleachFileMang.createFile("logins.txt");
							BleachFileMang.appendFile(
									email + ":" + session.getUuid() + ":" + session.getUsername() + ":" + crypter.encrypt(pass), "logins.txt");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}));

		if (accounts == null) {
			accounts = new AccountList();

			BleachFileMang.createFile("logins.txt");

			for (String s : BleachFileMang.readFileLines("logins.txt")) {
				String[] split = s.replace("\r", "").replace("\n", "").split(":", -1);

				try {
					if (split.length == 2) {
						accountQueue.add(new Account(split[0], crypter.decrypt(split[1])));
					} else if (split.length == 4) {
						accountQueue.add(new Account(split[0], crypter.decrypt(split[3]), split[1], split[2]));
					}
				} catch (Exception e) {
					System.out.println("Error decrypting accout: " + split[0]);
				}
			}
		}
	}

	public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrix);

		textRenderer.drawWithShadow(matrix, "Fabric: " + FabricLoader.getInstance().getModContainer("fabricloader").get().getMetadata().getVersion().getFriendlyString(),
				4, height - 30, -1);
		textRenderer.drawWithShadow(matrix, "Minecraft: " + SharedConstants.getGameVersion().getName(), 4, height - 20, -1);
		textRenderer.drawWithShadow(matrix, "Logged in as: \u00a7a" + client.getSession().getUsername(), 4, height - 10, -1);

		super.render(matrix, mouseX, mouseY, delta);
	}

	public void onRenderWindow(MatrixStack matrix, int window, int mouseX, int mouseY) {
		super.onRenderWindow(matrix, window, mouseX, mouseY);

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

			userField.render(matrix, mouseX, mouseY, 1f);
			passField.render(matrix, mouseX, mouseY, 1f);
			checkBox.render(matrix, mouseX, mouseY, 1f);
		} else if (window == 1) {
			int x = getWindow(1).x1,
					y = getWindow(1).y1,
					w = width - width / 3;
			//h = height - height / 3;

			int c = 0;
			for (Account a: accounts.getAccounts()) {
				int length = 250;
				drawEntry(matrix, a,
						x + w / 2 - length / 2,
						y + accStart + c,
						length,
						28,
						mouseX, mouseY,
						0x60606090, 0x60b070f0);

				c += accHeight;
			}
		}
	}

	private void drawEntry(MatrixStack matrix, Account acc, int x, int y, int width, int height, int mouseX, int mouseY, int color, int hoverColor) {
		Window.fill(matrix, x, y, x + width, y + height,
				mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height ? hoverColor : color);

		drawStringWithShadow(matrix, textRenderer, "\u00a7cx", x + width + 2, y + 2, -1);

		if (acc.bindSkin()) {
			double pixelSize = (height - 6) / 8d;
			DrawableHelper.fill(matrix,
					x + 2, y + 2,
					x + height - 2, y + height - 2,
					0x60d86ceb);
			DrawableHelper.drawTexture(matrix,
					x + 3, y + 3,
					(int) (pixelSize * 8), (int) (pixelSize * 8),
					(int) (pixelSize * 8), (int) (pixelSize * 8),
					(int) (pixelSize * 64), (int) (pixelSize * 64));
		}

		boolean extendText = acc.bindCape();
		if (extendText) {
			double pixelSize = ((height - 6) / 10d) * 0.625;
			DrawableHelper.fill(matrix,
					x + height - 1, y + 2,
					(int) (x + height + pixelSize * 10 + 1), y + height - 2,
					0x60d86ceb);
			DrawableHelper.drawTexture(matrix,
					x + height, y + 3,
					(int) Math.ceil(pixelSize), (int) Math.ceil(pixelSize),
					(int) (pixelSize * 10), (int) (pixelSize * 16),
					(int) (pixelSize * 64), (int) (pixelSize * 32));
		}

		double pixelSize = ((height - 6) / 10d) * 0.625;
		drawStringWithShadow(matrix, textRenderer, "\u00a77Name: " + (acc.username == null ? "\u00a78Unknown" : acc.username),
				extendText ? (int) (x + height + pixelSize * 10 + 3) : x + height, y + 4, -1);
		drawStringWithShadow(matrix, textRenderer,
				(acc.pass == null ? "\u00aeCracked" : acc.username == null ? "\u00a78Unchecked" : "\u00a7aWorking"),
				extendText ? (int) (x + height + pixelSize * 10 + 3) : x + height, y + height - 11, -1);
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!getWindow(0).closed && getWindow(0).selected) {
			userField.mouseClicked(mouseX, mouseY, button);
			passField.mouseClicked(mouseX, mouseY, button);
			checkBox.mouseClicked(mouseX, mouseY, button);
		} else if (!getWindow(1).closed && getWindow(1).selected) {
			int x = getWindow(1).x1,
					y = getWindow(1).y1,
					w = width - width / 3;

			int c = 0;
			for (Account a: new ArrayList<>(accounts.getAccounts())) {
				int length = 250;

				if (mouseX >= x + w / 2 - length / 2 && mouseX <= x + w / 2 + length / 2
						&& mouseY >= y + accStart + c * accHeight && mouseY <= y + accStart + c * accHeight + 28) {
					userField.setText(a.email);

					try {
						passField.setText(a.pass);
					} catch (Exception e1) {
						passField.setText("");
						e1.printStackTrace();
					}

					getWindow(1).closed = true;
					selectWindow(0);
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
					return super.mouseClicked(mouseX, mouseY, button);
				}

				if (mouseX >= x + w / 2 + length / 2 + 1 && mouseX <= x + w / 2 + length / 2 + 14
						&& mouseY >= y + accStart + c * accHeight && mouseY <= y + accStart + c * accHeight + 11) {
					List<String> lines = BleachFileMang.readFileLines("logins.txt");
					lines.removeIf(s -> s.startsWith(a.email));

					BleachFileMang.createEmptyFile("logins.txt");
					BleachFileMang.appendFile(String.join("", lines), "logins.txt");
					accounts.getAccounts().removeIf(ac -> ac.email.equals(a.email));
					break;
				}

				c++;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	public void tick() {
		userField.tick();
		passField.tick();

		for (Future<Account> f: new ArrayList<>(accountFutures)) {
			if (f.isDone()) {
				try {
					accounts.addAccount(f.get());

					accountFutures.remove(f);
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		}

		if (!accountQueue.isEmpty()) {
			Account account = accountQueue.poll();
			System.out.println("Queue >> " + account);
			accountFutures.add(accountExecutor.submit(new Callable<Account>() {

				@Override
				public Account call() throws Exception {
					if (StringUtils.isEmpty(account.pass) || StringUtils.isEmpty(account.pass)) {
						return account;
					}

					if (account.uuid == null) {
						Session session = LoginManager.createSessionSilent(account.email, account.pass);

						if (session == null) {
							return account;
						}

						account.uuid = session.getUuid();
						account.username = session.getUsername();

						account.textures.clear();
						client.getSkinProvider().loadSkin(session.getProfile(), (type, identifier, minecraftProfileTexture) -> {
							account.textures.put(type, identifier);
						}, true);
					} else {
						GameProfile profile = new GameProfile(UUID.fromString(account.uuid), account.uuid);

						account.textures.clear();
						client.getSkinProvider().loadSkin(profile, (type, identifier, minecraftProfileTexture) -> {
							account.textures.put(type, identifier);
						}, true);
					}

					return account;
				}
			}));
		}
	}

	public boolean charTyped(char chr, int modifiers) {
		if (userField.isFocused()) userField.charTyped(chr, modifiers);
		if (passField.isFocused()) passField.charTyped(chr, modifiers);

		return super.charTyped(chr, modifiers);
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (userField.isFocused()) userField.keyPressed(keyCode, scanCode, modifiers);
		if (passField.isFocused()) passField.keyPressed(keyCode, scanCode, modifiers);

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private static class AccountList {

		private Set<Account> accounts = new TreeSet<Account>((i, j) -> i.email.compareTo(j.email));

		public AccountList() {
		}

		public Set<Account> getAccounts() {
			return accounts;
		}

		public void addAccount(Account account) {
			accounts.add(account);
		}

		public boolean hasEmail(String email) {
			return accounts.stream().anyMatch(a -> a.email.equals(email));
		}

	}

	private static class Account {

		public String email;
		public String pass;
		public String uuid;
		public String username;

		public Map<Type, Identifier> textures = new EnumMap<>(Type.class);

		public Account(String email, String pass) {
			this.email = email;
			this.pass = pass;
		}

		public Account(String email, String pass, String uuid, String username) {
			this.email = email;
			this.pass = pass;
			this.uuid = uuid;
			this.username = username;
		}

		public boolean bindSkin() {
			if (textures.containsKey(Type.SKIN)) {
				MinecraftClient.getInstance().getTextureManager().bindTexture(textures.get(Type.SKIN));
				//System.out.println("Binded custom skin: " + skin);
			} else {
				MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultSkinHelper.getTexture());
			}

			return true;
		}

		public boolean bindCape() {
			if (textures.containsKey(Type.CAPE)) {
				MinecraftClient.getInstance().getTextureManager().bindTexture(textures.get(Type.CAPE));
				return true;
			}

			return false;
		}

		@Override
		public boolean equals(Object obj) {
			if (super.equals(obj)) {
				return true;
			}

			return obj instanceof Account && this.email.equals(((Account) obj).email);
		}
	}
}

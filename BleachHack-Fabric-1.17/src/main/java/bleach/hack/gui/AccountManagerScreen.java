/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.gui;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.io.Resources;
import com.google.common.util.concurrent.Runnables;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;

import bleach.hack.gui.window.WindowScreen;
import bleach.hack.gui.window.widget.WindowButtonWidget;
import bleach.hack.gui.window.widget.WindowPassTextFieldWidget;
import bleach.hack.gui.window.widget.WindowScrollbarWidget;
import bleach.hack.gui.window.widget.WindowTextFieldWidget;
import bleach.hack.gui.window.widget.WindowTextWidget;
import bleach.hack.gui.window.widget.WindowWidget;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.FabricReflect;
import bleach.hack.util.auth.LoginCrypter;
import bleach.hack.util.auth.LoginManager;
import bleach.hack.util.io.BleachFileMang;
import bleach.hack.BleachHack;
import bleach.hack.gui.option.Option;
import bleach.hack.gui.window.Window;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
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

	private static final LoginCrypter crypter = new LoginCrypter(LoginCrypter.PASS_PHRASE);

	private static List<Account> accounts;
	private static int selected = -1;
	private static int hovered = -1;

	private WindowScrollbarWidget scrollbar;

	private List<WindowWidget> rightsideWidgets = new ArrayList<>();
	private List<WindowTextFieldWidget> textFieldWidgets = new ArrayList<>();
	private List<WindowTextWidget> textWidgets = new ArrayList<>();
	private WindowTextWidget loginResult;

	public AccountManagerScreen() {
		super(new LiteralText("Account Manager"));
	}

	public void init() {
		super.init();

		clearWindows();
		Window mainWindow = addWindow(new Window(
				width / 8,
				height / 8,
				width - width / 8,
				height - height / 8, "Accounts", new ItemStack(Items.PAPER)));

		int w = mainWindow.x2 - mainWindow.x1;
		int h = mainWindow.y2 - mainWindow.y1;
		int listW = Math.max(140, w / 3);

		// Right side
		loginResult = mainWindow.addWidget(new WindowTextWidget(loginResult != null ? loginResult.getText() : LiteralText.EMPTY, true, listW + 11, 96, 0xc0c0c0));

		mainWindow.addWidget(new WindowButtonWidget(w - 70, h - 22, w - 3, h - 3, "Login", () -> {
			if (Option.PLAYERLIST_SHOW_AS_BH_USER.getValue())
				BleachHack.playerMang.stopPinger();

			Account account = accounts.get(selected);
			for (int i = 0; i < textFieldWidgets.size(); i++) {
				account.input[i] = textFieldWidgets.get(i).textField.getText();
			}

			Either<Session, String> either = account.login();
			loginResult.setText(new LiteralText(either.left().isPresent() ? "\u00a7aLogin Successful!" : either.right().get()));
			account.success = either.left().isPresent() ? 2 : 1;
			saveAccounts();

			if (Option.PLAYERLIST_SHOW_AS_BH_USER.getValue())
				BleachHack.playerMang.startPinger();
		}));

		mainWindow.getWidgets().forEach(rightsideWidgets::add);
		updateRightside();

		// Left side
		scrollbar = mainWindow.addWidget(
				new WindowScrollbarWidget(listW - 10, 28, accounts == null ? 0 : accounts.size() * 28 - 1, h - 29, 0));

		if (accounts == null) {
			accounts = new ArrayList<>();

			BleachFileMang.createFile("logins.txt");

			for (String s : BleachFileMang.readFileLines("logins.txt")) {
				addAccount(Account.deserialize(s.replace("\r", "").replace("\n", "").split(":", -1)));
			}
		}

		mainWindow.addWidget(new WindowTextWidget("Accounts", true, 6, 17, 0xf0f0f0));
		mainWindow.addWidget(new WindowButtonWidget(listW - 14, 14, listW - 2, 26, "\u00a7a+", () -> {
			selectWindow(1);
			removeWindow(2);
		}));
		mainWindow.addWidget(new WindowButtonWidget(listW - 29, 14, listW - 17, 26, "\u00a7c-", () -> {
			if (selected >= 0 && selected < accounts.size()) {
				accounts.remove(selected);
				selected = -1;
				scrollbar.setTotalHeight(accounts.size() * 28 - 1);
				updateRightside();
				saveAccounts();
			}
		}).withRenderEvent(wg -> ((WindowButtonWidget) wg).text = selected >= 0 && selected < accounts.size() ? "\u00a7c-" : "\u00a77-"));

		// Select type to add window
		Window typeWindow = addWindow(new Window(
				width / 2 - 127,
				height / 2 - 17,
				width / 2 + 127,
				height / 2 + 17, "Add Account..", new ItemStack(Items.LIME_GLAZED_TERRACOTTA), true));

		typeWindow.addWidget(new WindowButtonWidget(3, 15, 63, 31, "No Auth",
				() -> openAddAccWindow(AccountType.NO_AUTH, "No Auth", new ItemStack(Items.LIGHT_BLUE_GLAZED_TERRACOTTA))));
		typeWindow.addWidget(new WindowButtonWidget(66, 15, 126, 31, "Mojang",
				() -> openAddAccWindow(AccountType.MOJANG, "Mojang", new ItemStack(Items.GREEN_GLAZED_TERRACOTTA))));
		typeWindow.addWidget(new WindowButtonWidget(129, 15, 189, 31, "Microsoft",
				Runnables::doNothing /*() -> openAddAccWindow(AccountType.MICROSOFT, "Microsoft", new ItemStack(Items.PURPLE_GLAZED_TERRACOTTA)))*/));
		typeWindow.addWidget(new WindowButtonWidget(192, 15, 252, 31, "Altening",
				() -> openAddAccWindow(AccountType.ALTENING, "Altening", new ItemStack(Items.RED_GLAZED_TERRACOTTA))));
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);

		textRenderer.drawWithShadow(matrices, "Fabric: " + FabricLoader.getInstance().getModContainer("fabricloader").get().getMetadata().getVersion().getFriendlyString(),
				4, height - 30, -1);
		textRenderer.drawWithShadow(matrices, "Minecraft: " + SharedConstants.getGameVersion().getName(), 4, height - 20, -1);
		textRenderer.drawWithShadow(matrices, "Logged in as: \u00a7a" + client.getSession().getUsername(), 4, height - 10, -1);

		hovered = -1;
		super.render(matrices, mouseX, mouseY, delta);
	}

	public void onRenderWindow(MatrixStack matrices, int window, int mouseX, int mouseY) {
		super.onRenderWindow(matrices, window, mouseX, mouseY);

		if (window == 0) {
			int x = getWindow(0).x1;
			int y = getWindow(0).y1;
			int w = getWindow(0).x2 - x;
			int h = getWindow(0).y2 - y;
			int listW = Math.max(140, w / 3);

			boolean shrink = accounts.size() * 28 >= h - 28;
			for (int c = 0; c < accounts.size(); c++) {
				int curY = y + 28 + c * 28 - scrollbar.getPageOffset();

				if (curY + 28 > y + h || curY < y + 27)
					continue;

				boolean hover = getWindow(0).selected && mouseX >= x + 1 && mouseX <= x + listW - (shrink ? 12 : 1) && mouseY >= curY && mouseY <= curY + 27;
				drawEntry(matrices, accounts.get(c), x + 2, curY + 1, listW - (shrink ? 13 : 3), 26,
						selected == c ? 0x6090e090 : hover ? 0x60b070f0 : 0x60606090);

				if (hover)
					hovered = c;
			}

			fill(matrices, x + listW, y + 12, x + listW + 1, y + h - 1, 0xff606090);
		}
	}

	private void drawEntry(MatrixStack matrices, Account acc, int x, int y, int width, int height, int color) {
		Window.fill(matrices, x, y, x + width, y + height, color);

		if (acc.bindSkin()) {
			double pixelSize = (height - 6) / 8d;
			DrawableHelper.fill(matrices,
					x + 2, y + 2,
					x + height - 2, y + height - 2,
					0x60d86ceb);
			DrawableHelper.drawTexture(matrices,
					x + 3, y + 3,
					(int) (pixelSize * 8), (int) (pixelSize * 8),
					(int) (pixelSize * 8), (int) (pixelSize * 8),
					(int) (pixelSize * 64), (int) (pixelSize * 64));
		}

		boolean extendText = acc.bindCape();
		if (extendText) {
			double pixelSize = ((height - 6) / 10d) * 0.625;
			DrawableHelper.fill(matrices,
					x + height - 1, y + 2,
					(int) (x + height + pixelSize * 10 + 1), y + height - 2,
					0x60d86ceb);
			DrawableHelper.drawTexture(matrices,
					x + height, y + 3,
					(int) Math.ceil(pixelSize), (int) Math.ceil(pixelSize),
					(int) (pixelSize * 10), (int) (pixelSize * 16),
					(int) (pixelSize * 64), (int) (pixelSize * 32));
		}

		double pixelSize = ((height - 6) / 10d) * 0.625;
		textRenderer.drawWithShadow(matrices, "\u00a77Name: " + acc.username,
				extendText ? (int) (x + height + pixelSize * 10 + 3) : x + height, y + 4, -1);
		textRenderer.drawWithShadow(matrices,
				(acc.type == AccountType.NO_AUTH ? "\u00a7eNo Auth" : acc.type == AccountType.MOJANG ? "\u00a7aMojang" : "\u00a7bMicrosoft"),
				extendText ? (int) (x + height + pixelSize * 10 + 3) : x + height, y + height - 11, -1);

		if (acc.type != AccountType.NO_AUTH) {
			textRenderer.drawWithShadow(matrices,
					(acc.success == 0 ? "\u00a76?" : acc.success == 1 ? "\u00a7cx" : "\u00a7a+"),
					x + width - 10, y + height - 11, -1);
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (hovered >= 0 && hovered < accounts.size()) {
			if (selected >= 0 && selected < accounts.size()) {
				for (int i = 0; i < textFieldWidgets.size(); i++) {
					accounts.get(selected).input[i] = textFieldWidgets.get(i).textField.getText();
				}
			}

			selected = hovered;
			updateRightside();
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	private void saveAccounts() {
		BleachFileMang.createEmptyFile("logins.txt");
		BleachFileMang.appendFile("logins.txt", accounts.stream()
				.map(a -> {
					try {
						return a.type.ordinal() + ":" + a.success + ":"
								+ a.uuid + ":" + a.username + ":"
								+ IntStream.range(0, a.input.length).mapToObj(i -> {
									try {
										return a.type.fields[i].getRight() ? crypter.encrypt(a.input[i]) : a.input[i];
									} catch (Exception e) {
										throw new RuntimeException();
									}
								}).collect(Collectors.joining(":"));
					} catch (Exception e) {
						return null;
					}
				})
				.filter(a -> a != null)
				.collect(Collectors.joining("\n")));
	}

	private void openAddAccWindow(AccountType type, String name, ItemStack item) {
		getWindow(1).closed = true;

		int h = 40 + type.fields.length * 40;
		Window addWindow = addWindow(new Window(
				width / 2 - 80,
				height / 2 - h / 2,
				width / 2 + 80,
				height / 2 + h / 2, "Add " + name + " Account", item));

		WindowTextWidget result = addWindow.addWidget(new WindowTextWidget("", true, 10, h - 16, -1));
		List<WindowTextFieldWidget> tf = new ArrayList<>();
		for (int i = 0; i < type.fields.length; i++) {
			addWindow.addWidget(new WindowTextWidget(type.getFields()[i].getLeft(), true, 10, 20 + i * 40, 0xf0f0f0));

			if (type.getFields()[i].getRight()) {
				tf.add(addWindow.addWidget(new WindowPassTextFieldWidget(10, 33 + i * 40, 140, 18, "")));
			} else {
				tf.add(addWindow.addWidget(new WindowTextFieldWidget(10, 33 + i * 40, 140, 18, "")));
			}
		}

		addWindow.addWidget(new WindowButtonWidget(100, h - 20, 157, h - 3, "Add", () -> {
			Account account = new Account(type.ordinal(), 0, null, null, tf.stream().map(t -> t.textField.getText()).toArray(String[]::new));
			account.getSesson().ifLeft(s -> {
				account.uuid = s.getUuid();
				account.username = s.getUsername();
				addAccount(account);
				getWindow(2).closed = true;
			}).ifRight(s -> {
				result.setText(new LiteralText(s));
			});
		}));
	}

	private void updateRightside() {
		getWindow(0).getWidgets().removeAll(textFieldWidgets);
		getWindow(0).getWidgets().removeAll(textWidgets);
		textFieldWidgets.clear();
		textWidgets.clear();
		loginResult.setText(LiteralText.EMPTY);

		if (selected != -1) {
			Account a = accounts.get(selected);
			int w = getWindow(0).x2 - getWindow(0).x1;
			int listW = Math.max(140, w / 3);

			for (int i = 0; i < a.input.length; i++) {
				textWidgets.add(getWindow(0).addWidget(
						new WindowTextWidget(a.type.getFields()[i].getLeft(), true, listW + 10, 20 + i * 40, 0xf0f0f0)));

				if (a.type.getFields()[i].getRight()) {
					textFieldWidgets.add(getWindow(0).addWidget(
							new WindowPassTextFieldWidget(listW + 10, 33 + i * 40, w - listW - 20, 18, a.input[i])));
				} else {
					textFieldWidgets.add(getWindow(0).addWidget(
							new WindowTextFieldWidget(listW + 10, 33 + i * 40, w - listW - 20, 18, a.input[i])));
				}
			}

			loginResult.y1 = 16 + a.input.length * 40;
			loginResult.y2 = loginResult.y1 + 10;
			rightsideWidgets.forEach(wg -> wg.visible = true);
		} else {
			rightsideWidgets.forEach(wg -> wg.visible = false);
		}
	}

	private void addAccount(Account account) {
		if (account == null)
			return;

		if (account.uuid == null) {
			Session session = account.getSesson().left().orElse(null);

			if (session == null)
				return;

			account.uuid = session.getUuid();
			account.username = session.getUsername();

			account.textures.clear();
			client.getSkinProvider().loadSkin(session.getProfile(), (type, identifier, minecraftProfileTexture) -> {
				account.textures.put(type, identifier);
			}, true);
		} else {
			GameProfile profile = new GameProfile(UUID.fromString(account.uuid), account.username);

			account.textures.clear();
			client.getSkinProvider().loadSkin(profile, (type, identifier, minecraftProfileTexture) -> {
				account.textures.put(type, identifier);
			}, true);
		}

		for (int i = 0; i <= accounts.size(); i++) {
			if (i == accounts.size() || String.CASE_INSENSITIVE_ORDER.compare(accounts.get(i).username, account.username) >= 0) {
				accounts.add(i, account);
				break;
			}
		}

		scrollbar.setTotalHeight(accounts.size() * 28 - 1);
	}

	private static class Account {

		public String[] input;
		public String uuid;
		public String username;
		public AccountType type;
		public int success;

		public Map<Type, Identifier> textures = new EnumMap<>(Type.class);

		public static Account deserialize(String[] data) {
			try {
				if (data.length == 4) { // Old 4-part accounts
					return new Account(1, 0, data[1], data[2], data[0], crypter.decrypt(data[3]));
				} else if (data.length > 4) {
					AccountType type = AccountType.values()[Integer.parseInt(data[0])];
					return new Account(
							Integer.parseInt(data[0]), Integer.parseInt(data[1]), data[2], data[3],
							IntStream.range(0, data.length - 4).mapToObj(i -> {
								try {
									return type.fields[i].getRight() ? crypter.decrypt(data[i + 4]) : data[i + 4];
								} catch (Exception e) {
									throw new RuntimeException();
								}
							}).toArray(String[]::new));
				}
			} catch (Exception e) {
				BleachLogger.logger.error("Unable to deserialize account " + data[0], e);
			}

			return null;
		}

		public Account(int type, int success, String uuid, String username, String... input) {
			this.type = AccountType.values()[type];
			this.success = success;
			this.uuid = uuid;
			this.username = username;
			this.input = input;
		}

		public Either<Session, String> login() {
			Either<Session, String> either = getSesson();
			if (either.left().isPresent()) {
				FabricReflect.writeField(MinecraftClient.getInstance(), either.left().get(), "field_1726", "session");
			}

			return either;
		}

		public Either<Session, String> getSesson() {
			return type.createSession(input);
		}

		public boolean bindSkin() {
			if (textures.containsKey(Type.SKIN)) {
				RenderSystem.setShaderTexture(0, textures.get(Type.SKIN));
			} else {
				RenderSystem.setShaderTexture(0, DefaultSkinHelper.getTexture());
			}

			return true;
		}

		public boolean bindCape() {
			if (textures.containsKey(Type.CAPE)) {
				RenderSystem.setShaderTexture(0, textures.get(Type.CAPE));
				return true;
			}

			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private static enum AccountType {

		NO_AUTH((input) -> {
			try {
				String id = new JsonParser().parse(
						Resources.toString(new URL("https://api.mojang.com/users/profiles/minecraft/" + input[0]), StandardCharsets.UTF_8))
						.getAsJsonObject().get("id").getAsString();

				if (id.length() == 32)
					id = id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20);

				return Either.left(new Session(input[0], id, "", "mojang"));
			} catch (Exception e) {
				return Either.left(new Session(input[0], "00000000-0000-0000-0000-000000000000", "", "mojang"));
			}
		}, Pair.of("Username", false)),
		MOJANG((input) -> {
			try {
				return Either.left(LoginManager.createSession(input[0], input[1]));
			} catch (AuthenticationException e) {
				if (e.getMessage().toLowerCase(Locale.ENGLISH).contains("invalid username or password") || e.getMessage().toLowerCase(Locale.ENGLISH).contains("account migrated")) {
					return Either.right("\u00a74Wrong password!");
				}

				BleachLogger.logger.error(e);
				return Either.right("\u00a7cCannot contact authentication server!");
			}
		}, Pair.of("Email", false), Pair.of("Password", true)),
		MICROSOFT((input) -> {
			return Either.right("\u00a7aMS Support not added yet!");
		}, Pair.of("Email", false), Pair.of("Password", true)),
		ALTENING((input) -> {
			try {
				return Either.left(LoginManager.createAlteningSession(input[0]));
			} catch (Exception e) {
				BleachLogger.logger.error(e);
				return Either.right("\u00a7cException occurred!");
			}
		}, Pair.of("Token", false));

		private Pair<String, Boolean>[] fields;
		private Function<String[], Either<Session, String>> sessionCreator;

		private AccountType(Function<String[], Either<Session, String>> sessionCreator, Pair<String, Boolean>... fields) {
			this.fields = fields;
			this.sessionCreator = sessionCreator;
		}

		public Pair<String, Boolean>[] getFields() {
			return fields;
		}

		public Either<Session, String> createSession(String... input) {
			return sessionCreator.apply(input);
		}
	}
}

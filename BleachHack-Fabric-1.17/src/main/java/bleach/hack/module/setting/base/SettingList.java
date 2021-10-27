/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.setting.base;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.systems.RenderSystem;

import bleach.hack.gui.clickgui.window.ModuleWindow;
import bleach.hack.gui.window.Window;
import bleach.hack.gui.window.WindowScreen;
import bleach.hack.gui.window.widget.WindowButtonWidget;
import bleach.hack.gui.window.widget.WindowScrollbarWidget;
import bleach.hack.gui.window.widget.WindowTextFieldWidget;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.io.BleachFileHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;

public abstract class SettingList<T> extends SettingBase {

	protected String text;
	protected String windowText;

	protected Set<T> itemPool = new LinkedHashSet<>();
	protected Set<T> items = new LinkedHashSet<>();

	protected Set<T> defaultItems = new LinkedHashSet<>();

	@SafeVarargs // eclipse bruh hours
	public SettingList(String text, String windowText, Collection<T> itemPool, T... defaultItems) {
		this.text = text;
		this.windowText = windowText;

		Collections.addAll(this.defaultItems, defaultItems);
		this.itemPool.addAll(itemPool);
		this.itemPool.removeAll(this.defaultItems);
		this.items.addAll(this.defaultItems);
	}

	public String getName() {
		return windowText;
	}

	public void render(ModuleWindow window, MatrixStack matrices, int x, int y, int len) {
		if (window.mouseOver(x, y, x + len, y + 12)) {
			DrawableHelper.fill(matrices, x + 1, y, x + len, y + 12, 0x70303070);
		}

		MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, text, x + 3, y + 2, 0xcfe0cf);

		MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, "...", x + len - 7, y + 2, 0xcfd0cf);

		if (window.mouseOver(x, y, x + len, y + 12) && window.lmDown) {
			window.mouseReleased(window.mouseX, window.mouseY, 1);
			MinecraftClient.getInstance().currentScreen.mouseReleased(window.mouseX, window.mouseY, 0);
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
			MinecraftClient.getInstance().setScreen(new ListWidowScreen(MinecraftClient.getInstance().currentScreen));
		}
	}

	public boolean contains(T item) {
		return items.contains(item);
	}

	public Set<T> getItems() {
		return items;
	}

	public void renderItem(MinecraftClient mc, MatrixStack matrices, T item, int x, int y, int w, int h) {
		matrices.push();

		float scale = (h - 2) / 10f;
		float offset = 1f / scale;

		matrices.scale(scale, scale, 1f);

		mc.textRenderer.drawWithShadow(matrices, "?", (x + 5) * offset, (y + 4) * offset, -1);

		matrices.pop();
	}

	public abstract T getItemFromString(String string);
	public abstract String getStringFromItem(T item);

	public SettingList<T> withDesc(String desc) {
		description = desc;
		return this;
	}

	public int getHeight(int len) {
		return 12;
	}

	public void readSettings(JsonElement settings) {
		JsonArray ja = (JsonArray) settings;

		itemPool.addAll(items);
		items.clear();
		for (JsonElement je: ja) {
			if (je.isJsonPrimitive()) {
				T item = getItemFromString(je.getAsString());
				if (item != null) {
					itemPool.remove(item);
					items.add(item);
				} else {
					BleachLogger.logger.error("Error Importing item: " + je.toString());
				}
			}
		}
	}

	public JsonElement saveSettings() {
		JsonArray ja = new JsonArray();

		for (T e: items) {
			ja.add(getStringFromItem(e));
		}

		return ja;
	}

	@Override
	public boolean isDefault() {
		return items.equals(defaultItems);
	}

	private class ListWidowScreen extends WindowScreen {

		private Screen parent;
		private WindowTextFieldWidget inputField;
		private WindowScrollbarWidget scrollbar;

		private T toDeleteItem;
		private T toAddItem;

		public ListWidowScreen(Screen parent) {
			super(new LiteralText(windowText));
			this.parent = parent;
		}

		public void init() {
			super.init();

			clearWindows();

			addWindow(new Window(
					(int) (width / 3.25),
					height / 12,
					(int) (width - width / 3.25),
					height - height / 12,
					windowText, new ItemStack(Items.OAK_SIGN)));

			int x2 = getWindow(0).x2 - getWindow(0).x1;
			int y2 = getWindow(0).y2 - getWindow(0).y1;

			getWindow(0).addWidget(new WindowButtonWidget(x2 - 50, y2 - 22, x2 - 5, y2 - 5, "Reset", () -> {
				itemPool.addAll(items);
				items.clear();
				items.addAll(defaultItems);
				itemPool.removeAll(defaultItems);
				BleachFileHelper.SCHEDULE_SAVE_MODULES.set(true);
			}));

			getWindow(0).addWidget(new WindowButtonWidget(x2 - 100, y2 - 22, x2 - 55, y2 - 5, "Clear", () -> {
				itemPool.addAll(items);
				items.clear();
				BleachFileHelper.SCHEDULE_SAVE_MODULES.set(true);
			}));

			getWindow(0).addWidget(new WindowButtonWidget(x2 - 150, y2 - 22, x2 - 105, y2 - 5, "Add All", () -> {
				items.addAll(itemPool);
				itemPool.clear();
				BleachFileHelper.SCHEDULE_SAVE_MODULES.set(true);
			}));

			inputField = getWindow(0).addWidget(new WindowTextFieldWidget(5, y2 - 22, x2 / 3, 17, inputField != null ? inputField.textField.getText() : ""));

			scrollbar = getWindow(0).addWidget(new WindowScrollbarWidget(x2 - 11, 12, 0, y2 - 39, scrollbar == null ? 0 : scrollbar.getPageOffset()));
		}

		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			renderBackground(matrices);
			super.render(matrices, mouseX, mouseY, delta);
		}

		public void onRenderWindow(MatrixStack matrices, int window, int mouseX, int mouseY) {
			super.onRenderWindow(matrices, window, mouseX, mouseY);

			toAddItem = null;
			toDeleteItem = null;

			if (window == 0) {
				int x1 = getWindow(0).x1;
				int y1 = getWindow(0).y1;
				int x2 = getWindow(0).x2;
				int y2 = getWindow(0).y2;

				int maxEntries = Math.max(1, (y2 - y1) / 21 - 1);
				int renderEntries = 0;
				int entries = 0;

				scrollbar.setTotalHeight(items.size() * 21);
				int offset = scrollbar.getPageOffset();

				for (T e: items) {
					if (entries >= offset / 21 && renderEntries < maxEntries) {
						drawEntry(matrices, e, x1 + 6, y1 + 15 + entries * 21 - offset, x2 - x1 - 19, 20, mouseX, mouseY);
						renderEntries++;
					}

					entries++;
				}

				//Window.horizontalGradient(matrix, x1 + 1, y2 - 25, x2 - 1, y2 - 1, 0x70606090, 0x00606090);
				Window.horizontalGradient(matrices, x1 + 1, y2 - 27, x2 - 1, y2 - 26, 0xff606090, 0x50606090);

				if (inputField.textField.isFocused()) {
					Set<T> toDraw = new LinkedHashSet<>();

					for (T e: itemPool) {
						if (toDraw.size() >= 10) break;

						if (getStringFromItem(e).toLowerCase(Locale.ENGLISH).contains(inputField.textField.getText().toLowerCase(Locale.ENGLISH))) {
							toDraw.add(e);
						}
					}

					int curY = y1 + inputField.y1 - 4 - toDraw.size() * 17;
					int longest = toDraw.stream().map(e -> textRenderer.getWidth(getStringFromItem(e))).sorted(Comparator.reverseOrder()).findFirst().orElse(0);

					RenderSystem.getModelViewStack().push();
					RenderSystem.getModelViewStack().translate(0, 0, 150);

					matrices.push();
					matrices.translate(0, 0, 150);

					for (T e: toDraw) {
						drawSearchEntry(matrices, e, x1 + inputField.x1, curY, longest + 23, 16, mouseX, mouseY);
						curY += 17;
					}

					matrices.pop();
					RenderSystem.getModelViewStack().pop();
					RenderSystem.applyModelViewMatrix();
				}
			}
		}

		private void drawEntry(MatrixStack matrices, T item, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean mouseOverDelete = mouseX >= x + width - 14 && mouseX <= x + width - 1 && mouseY >= y + 2 && mouseY <= y + height - 2;
			Window.fill(matrices, x + width - 14, y + 2, x + width - 1, y + height - 2, mouseOverDelete ? 0x4fb070f0 : 0x60606090);

			if (mouseOverDelete) {
				toDeleteItem = item;
			}

			renderItem(client, matrices, item, x, y, height, height);

			drawStringWithShadow(matrices, textRenderer, getStringFromItem(item), x + height + 4, y + 4, -1);
			drawStringWithShadow(matrices, textRenderer, "\u00a7cx", x + width - 10, y + 5, -1);
		}

		private void drawSearchEntry(MatrixStack matrices, T item, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean mouseOver = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
			DrawableHelper.fill(matrices, x, y - 1, x + width, y + height, mouseOver ? 0xdf8070d0 : 0xb0606090);

			if (mouseOver) {
				toAddItem = item;
			}

			renderItem(client, matrices, item, x, y, height, height);

			drawStringWithShadow(matrices, textRenderer, getStringFromItem(item), x + height + 4, y + 4, -1);
		}

		public void onClose() {
			this.client.setScreen(parent);
		}

		@Override
		public boolean isPauseScreen() {
			return false;
		}

		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (toAddItem != null) {
				items.add(toAddItem);
				itemPool.remove(toAddItem);
				inputField.textField.setTextFieldFocused(true);
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
				BleachFileHelper.SCHEDULE_SAVE_MODULES.set(true);
				return false;
			} else if (toDeleteItem != null) {
				itemPool.add(toDeleteItem);
				items.remove(toDeleteItem);
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
				BleachFileHelper.SCHEDULE_SAVE_MODULES.set(true);
			}

			return super.mouseClicked(mouseX, mouseY, button);
		}

		public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
			if (!inputField.textField.isFocused() || inputField.textField.getText().isEmpty()) {
				scrollbar.scroll(amount);
			}

			return super.mouseScrolled(mouseX, mouseY, amount);
		}
	}
}

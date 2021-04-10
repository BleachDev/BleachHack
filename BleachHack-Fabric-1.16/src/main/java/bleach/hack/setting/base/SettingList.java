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
package bleach.hack.setting.base;

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
import bleach.hack.gui.widget.BleachScrollbar;
import bleach.hack.gui.window.Window;
import bleach.hack.gui.window.WindowButton;
import bleach.hack.gui.window.WindowScreen;
import bleach.hack.util.file.BleachFileHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;

public abstract class SettingList<E> extends SettingBase {

	protected String text;
	protected String windowText;

	protected Set<E> itemPool = new LinkedHashSet<>();
	protected Set<E> items = new LinkedHashSet<>();

	protected Set<E> defaultItems = new LinkedHashSet<>();

	@SafeVarargs // eclipse bruh hours
	public SettingList(String text, String windowText, Collection<E> itemPool, E... defaultItems) {
		this.text = text;
		this.windowText = windowText;
		this.itemPool.addAll(itemPool);

		Collections.addAll(this.defaultItems, defaultItems);
		items.addAll(this.defaultItems);
	}

	public String getName() {
		return windowText;
	}

	public void render(ModuleWindow window, MatrixStack matrix, int x, int y, int len) {
		if (window.mouseOver(x, y, x + len, y + 12)) {
			DrawableHelper.fill(matrix, x + 1, y, x + len, y + 12, 0x70303070);
		}

		MinecraftClient.getInstance().textRenderer.drawWithShadow(matrix, text, x + 3, y + 2, 0xcfe0cf);

		MinecraftClient.getInstance().textRenderer.drawWithShadow(matrix, "...", x + len - 7, y + 2, 0xcfd0cf);

		if (window.mouseOver(x, y, x + len, y + 12) && window.lmDown) {
			window.onMouseReleased(window.mouseX, window.mouseY);
			MinecraftClient.getInstance().currentScreen.mouseReleased(window.mouseX, window.mouseY, 0);
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
			MinecraftClient.getInstance().openScreen(new ListWidowScreen(MinecraftClient.getInstance().currentScreen));
		}
	}

	public boolean contains(E item) {
		return items.contains(item);
	}

	public Set<E> getItems() {
		return items;
	}

	public void renderItem(MinecraftClient mc, MatrixStack matrix, E item, int x, int y, int w, int h) {
		matrix.push();

		float scale = (h - 2) / 10f;
		float offset = 1f / scale;

		matrix.scale(scale, scale, 1f);

		mc.textRenderer.drawWithShadow(matrix, "?", (x + 5) * offset, (y + 4) * offset, -1);

		matrix.pop();
	}

	public abstract E getItemFromString(String string);
	public abstract String getStringFromItem(E item);

	public SettingList<E> withDesc(String desc) {
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
				E item = getItemFromString(je.getAsString());
				if (item != null) {
					itemPool.remove(item);
					items.add(item);
				} else {
					System.out.println("Error Importing item: " + je.toString());
				}
			}
		}
	}

	public JsonElement saveSettings() {
		JsonArray ja = new JsonArray();

		for (E e: items) {
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
		private TextFieldWidget inputField;
		private BleachScrollbar scrollbar;

		private E toDeleteItem;
		private E toAddItem;

		public ListWidowScreen(Screen parent) {
			super(new LiteralText(windowText));
			this.parent = parent;
		}

		public void init() {
			super.init();

			clearWindows();
			int x1 = (int) (width / 3.25);
			int y1 = height / 12;
			int x2 = (int) (width - width / 3.25);
			int y2 = height - height / 12;

			addWindow(new Window(x1, y1, x2, y2, windowText, new ItemStack(Items.OAK_SIGN)));

			getWindow(0).buttons.add(new WindowButton((x2 - x1) - 50, y2 - y1 - 22, (x2 - x1) - 5, y2 - y1 - 5, "Reset", () -> {
				itemPool.addAll(items);
				items.clear();
				items.addAll(defaultItems);
				itemPool.removeAll(defaultItems);
				BleachFileHelper.SCHEDULE_SAVE_MODULES = true;
			}));

			getWindow(0).buttons.add(new WindowButton((x2 - x1) - 100, y2 - y1 - 22, (x2 - x1) - 55, y2 - y1 - 5, "Clear", () -> {
				itemPool.addAll(items);
				items.clear();
				BleachFileHelper.SCHEDULE_SAVE_MODULES = true;
			}));

			getWindow(0).buttons.add(new WindowButton((x2 - x1) - 150, y2 - y1 - 22, (x2 - x1) - 105, y2 - y1 - 5, "Add All", () -> {
				items.addAll(itemPool);
				itemPool.clear();
				BleachFileHelper.SCHEDULE_SAVE_MODULES = true;
			}));

			if (inputField == null) {
				inputField = new TextFieldWidget(textRenderer, 0, 0, 0, 17, LiteralText.EMPTY);
				inputField.setMaxLength(32767);
			}

			scrollbar = new BleachScrollbar(0, 0, 0, y2 - y1 - 39, scrollbar == null ? 0 : scrollbar.getPageOffset());
		}

		public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
			renderBackground(matrix);
			super.render(matrix, mouseX, mouseY, delta);
		}

		public void onRenderWindow(MatrixStack matrix, int window, int mouseX, int mouseY) {
			super.onRenderWindow(matrix, window, mouseX, mouseY);

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

				int offset = scrollbar.getPageOffset();

				for (E e: items) {
					if (entries >= offset / 21 && renderEntries < maxEntries) {
						drawEntry(matrix, e, x1 + 6, y1 + 15 + entries * 21 - offset, x2 - x1 - 19, 20, mouseX, mouseY);
						renderEntries++;
					}

					entries++;
				}

				Window.horizontalGradient(matrix, x1 + 1, y2 - 25, x2 - 1, y2 - 1, 0x70606090, 0x00606090);
				DrawableHelper.fill(matrix, x1 + 1, y2 - 27, x2 - 1, y2 - 25, 0xa0606090);

				if (inputField.isFocused() && !inputField.getText().isEmpty()) {
					Set<E> toDraw = new LinkedHashSet<>();

					for (E e: itemPool) {
						if (toDraw.size() >= 10) break;

						if (getStringFromItem(e).toLowerCase(Locale.ENGLISH).contains(inputField.getText().toLowerCase(Locale.ENGLISH))) {
							toDraw.add(e);
						}
					}

					int curY = inputField.y - 4 - toDraw.size() * 17;
					int longest = toDraw.stream().map(e -> textRenderer.getWidth(getStringFromItem(e))).sorted(Comparator.reverseOrder()).findFirst().orElse(0);

					RenderSystem.pushMatrix();
					RenderSystem.translatef(0f, 0f, 150f);

					matrix.push();
					matrix.translate(0f, 0f, 150f);

					for (E e: toDraw) {
						drawSearchEntry(matrix, e, inputField.x, curY, longest + 23, 16, mouseX, mouseY);
						curY += 17;
					}

					matrix.pop();

					RenderSystem.popMatrix();
				}
				
				matrix.push();
				matrix.translate(0f, 0f, 250f);
				
				inputField.x = x1 + 5;
				inputField.y = y2 - 22;
				inputField.setWidth((x2 - x1) / 3);
				inputField.render(matrix, mouseX, mouseY, client.getTickDelta());

				scrollbar.x = x2 - 11;
				scrollbar.y = y1 + 12;
				scrollbar.setTotalHeight(entries * 21);
				scrollbar.render(matrix, mouseX, mouseY, client.getTickDelta());

				matrix.pop();
			}
		}

		private void drawEntry(MatrixStack matrix, E item, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean mouseOverDelete = mouseX >= x + width - 14 && mouseX <= x + width - 1 && mouseY >= y + 2 && mouseY <= y + height - 2;
			Window.fill(matrix, x + width - 14, y + 2, x + width - 1, y + height - 2, mouseOverDelete ? 0x4fb070f0 : 0x60606090);

			if (mouseOverDelete) {
				toDeleteItem = item;
			}

			renderItem(client, matrix, item, x, y, height, height);

			drawStringWithShadow(matrix, textRenderer, getStringFromItem(item), x + height + 4, y + 4, -1);
			drawStringWithShadow(matrix, textRenderer, "\u00a7cx", x + width - 10, y + 5, -1);
		}

		private void drawSearchEntry(MatrixStack matrix, E item, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean mouseOver = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
			DrawableHelper.fill(matrix, x, y - 1, x + width, y + height, mouseOver ? 0xdf8070d0 : 0xb0606090);

			if (mouseOver) {
				toAddItem = item;
			}

			renderItem(client, matrix, item, x, y, height, height);

			drawStringWithShadow(matrix, textRenderer, getStringFromItem(item), x + height + 4, y + 4, -1);
		}

		public void onClose() {
			this.client.openScreen(parent);
		}

		@Override
		public boolean isPauseScreen() {
			return false;
		}

		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			inputField.mouseClicked(mouseX, mouseY, button);
			scrollbar.mouseClicked(mouseX, mouseY, button);

			if (toAddItem != null) {
				items.add(toAddItem);
				itemPool.remove(toAddItem);
				inputField.setTextFieldFocused(true);
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
				BleachFileHelper.SCHEDULE_SAVE_MODULES = true;
			} else if (toDeleteItem != null) {
				itemPool.add(toDeleteItem);
				items.remove(toDeleteItem);
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
				BleachFileHelper.SCHEDULE_SAVE_MODULES = true;
			}

			return super.mouseClicked(mouseX, mouseY, button);
		}

		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			scrollbar.mouseReleased(mouseX, mouseY, button);

			return super.mouseReleased(mouseX, mouseY, button);
		}

		public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
			if (!inputField.isFocused() || inputField.getText().isEmpty()) {
				scrollbar.mouseScrolled(mouseX, mouseY, amount);
			}

			return super.mouseScrolled(mouseX, mouseY, amount);
		}

		public void tick() {
			super.tick();

			inputField.tick();
		}

		public boolean charTyped(char chr, int modifiers) {
			if (inputField.isFocused()) inputField.charTyped(chr, modifiers);

			return super.charTyped(chr, modifiers);
		}

		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			if (inputField.isFocused()) inputField.keyPressed(keyCode, scanCode, modifiers);

			return super.keyPressed(keyCode, scanCode, modifiers);
		}
	}
}

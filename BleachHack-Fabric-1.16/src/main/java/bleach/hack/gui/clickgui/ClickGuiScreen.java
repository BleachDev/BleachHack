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
package bleach.hack.gui.clickgui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Triple;
import bleach.hack.BleachHack;
import bleach.hack.command.Command;
import bleach.hack.gui.window.WindowScreen;
import bleach.hack.gui.clickgui.window.ClickGuiWindow;
import bleach.hack.gui.clickgui.window.ModuleWindow;
import bleach.hack.gui.window.Window;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.util.file.BleachFileHelper;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;

public class ClickGuiScreen extends WindowScreen {

	private int keyDown = -1;
	private boolean lmDown = false;
	private boolean rmDown = false;
	private boolean lmHeld = false;
	private int mwScroll = 0;

	private TextFieldWidget searchField;

	public ClickGuiScreen() {
		super(new LiteralText("ClickGui"));
	}

	public void init() {
		super.init();

		searchField = new TextFieldWidget(textRenderer, 2, 14, 100, 12, LiteralText.EMPTY /* @LasnikProgram is author lol */);
		searchField.visible = false;
		searchField.setMaxLength(20);
		searchField.setSuggestion("Search here");
		addButton(searchField);
	}

	public void initWindows() {
		int len = (int) ModuleManager.getModule("ClickGui").getSetting(0).asSlider().getValue();

		int startX = 10;
		addWindow(new ModuleWindow(ModuleManager.getModulesInCat(Category.PLAYER),
				startX, 35, len, "Player", new ItemStack(Items.ARMOR_STAND)));

		addWindow(new ModuleWindow(ModuleManager.getModulesInCat(Category.RENDER),
				startX + len + 5, 35, len, "Render", new ItemStack(Items.YELLOW_STAINED_GLASS)));

		addWindow(new ModuleWindow(ModuleManager.getModulesInCat(Category.COMBAT),
				startX + len * 2 + 10, 35, len, "Combat", new ItemStack(Items.TOTEM_OF_UNDYING)));

		addWindow(new ModuleWindow(ModuleManager.getModulesInCat(Category.MOVEMENT),
				startX + len * 3 + 15, 35, len, "Movement", new ItemStack(Items.POTION)));

		addWindow(new ModuleWindow(ModuleManager.getModulesInCat(Category.EXPLOITS),
				startX + len * 4 + 20, 35, len, "Exploits", new ItemStack(Items.REPEATING_COMMAND_BLOCK)));

		addWindow(new ModuleWindow(ModuleManager.getModulesInCat(Category.MISC),
				startX + len * 5 + 25, 35, len, "Misc", new ItemStack(Items.NAUTILUS_SHELL)));

		addWindow(new ModuleWindow(ModuleManager.getModulesInCat(Category.WORLD),
				startX + len * 6 + 30, 35, len, "World", new ItemStack(Items.GRASS_BLOCK)));
	}

	public boolean isPauseScreen() {
		return false;
	}

	public void onClose() {
		ModuleManager.getModule("ClickGui").setEnabled(false);
		client.openScreen(null);
	}

	public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
		BleachFileHelper.SCHEDULE_SAVE_CLICKGUI = true;

		searchField.visible = ModuleManager.getModule("ClickGui").getSetting(1).asToggle().state;

		this.renderBackground(matrix);
		textRenderer.draw(matrix, "BleachHack-1.16-" + BleachHack.VERSION, 3, 3, 0x305090);
		textRenderer.draw(matrix, "BleachHack-1.16-" + BleachHack.VERSION, 2, 2, 0x6090d0);

		if (ModuleManager.getModule("ClickGui").getSetting(2).asToggle().state) {
			textRenderer.drawWithShadow(matrix, "Current prefix is: \"" + Command.PREFIX + "\" (" + Command.PREFIX + "help)", 2, height - 20, 0x99ff99);
			textRenderer.drawWithShadow(matrix, "Use " + Command.PREFIX + "guireset to reset the gui", 2, height - 10, 0x9999ff);
		}

		if (ModuleManager.getModule("ClickGui").getSetting(1).asToggle().state) {
			searchField.setSuggestion(searchField.getText().isEmpty() ? "Search here" : "");

			Set<Module> seachMods = new HashSet<>();
			if (!searchField.getText().isEmpty()) {
				for (Module m : ModuleManager.getModules()) {
					if (m.getName().toLowerCase(Locale.ENGLISH).contains(searchField.getText().toLowerCase(Locale.ENGLISH).replace(" ", ""))) {
						seachMods.add(m);
					}
				}
			}

			for (Window w : getWindows()) {
				if (w instanceof ModuleWindow) {
					((ModuleWindow) w).setSearchedModule(seachMods);
				}
			}
		}

		int len = (int) ModuleManager.getModule("ClickGui").getSetting(0).asSlider().getValue();
		for (Window w : getWindows()) {
			if (w instanceof ClickGuiWindow) {
				if (w instanceof ModuleWindow) {
					((ModuleWindow) w).setLen(len);
				}

				((ClickGuiWindow) w).updateKeys(mouseX, mouseY, keyDown, lmDown, rmDown, lmHeld, mwScroll);
			}
		}

		super.render(matrix, mouseX, mouseY, delta);

		matrix.push();
		matrix.translate(0, 0, 250);

		for (Window w : getWindows()) {
			if (w instanceof ClickGuiWindow) {
				Triple<Integer, Integer, String> tooltip = ((ClickGuiWindow) w).getTooltip();

				if (tooltip != null) {
					int tooltipY = tooltip.getMiddle();

					String[] split = tooltip.getRight().split("\n", -1 /* Adding -1 makes it keep empty splits */);
					ArrayUtils.reverse(split);
					for (String s: split) {
						/* Match lines to end of words after it reaches 22 characters long */
						Matcher mat = Pattern.compile(".{1,22}\\b\\W*").matcher(s);

						List<String> lines = new ArrayList<>();

						while (mat.find())
							lines.add(mat.group().trim());

						if (lines.isEmpty())
							lines.add(s);

						int start = tooltipY - lines.size() * 10;
						for (int l = 0; l < lines.size(); l++) {
							fill(matrix, tooltip.getLeft(), start + (l * 10) - 1,
									tooltip.getLeft() + textRenderer.getWidth(lines.get(l)) + 3,
									start + (l * 10) + 9, 0xff000000);
							textRenderer.drawWithShadow(matrix, lines.get(l), tooltip.getLeft() + 2, start + (l * 10), -1);
						}

						tooltipY -= lines.size() * 10;
					}
				}
			}
		}

		matrix.pop();

		lmDown = false;
		rmDown = false;
		keyDown = -1;
		mwScroll = 0;
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			lmDown = true;
			lmHeld = true;
		} else if (button == 1)
			rmDown = true;

		// Fix having to double click windows to move them
		for (Window w : getWindows()) {
			if (mouseX > w.x1 && mouseX < w.x2 && mouseY > w.y1 && mouseY < w.y2 && !w.closed) {
				w.onMousePressed((int) mouseX, (int) mouseY);
				break;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0)
			lmHeld = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		keyDown = keyCode;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		mwScroll = (int) amount;
		return super.mouseScrolled(mouseX, mouseY, amount);
	}

	public void resetGui() {
		int x = 10;
		for (Window m : getWindows()) {
			m.x1 = x;
			m.y1 = 35;
			x += (int) ModuleManager.getModule("ClickGui").getSetting(0).asSlider().getValue() + 5;
		}
	}
}

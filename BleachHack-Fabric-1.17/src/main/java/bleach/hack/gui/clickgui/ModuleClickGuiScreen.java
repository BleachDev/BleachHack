/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.gui.clickgui;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import bleach.hack.command.Command;
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

public class ModuleClickGuiScreen extends ClickGuiScreen {

	private TextFieldWidget searchField;

	public ModuleClickGuiScreen() {
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

	public void onClose() {
		ModuleManager.getModule("ClickGui").setEnabled(false);
	}

	public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
		BleachFileHelper.SCHEDULE_SAVE_CLICKGUI = true;

		searchField.visible = ModuleManager.getModule("ClickGui").getSetting(1).asToggle().state;

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
			if (w instanceof ModuleWindow) {
				((ModuleWindow) w).setLen(len);
			}
		}

		super.render(matrix, mouseX, mouseY, delta);
		
		if (ModuleManager.getModule("ClickGui").getSetting(2).asToggle().state) {
			textRenderer.drawWithShadow(matrix, "Current prefix is: \"" + Command.PREFIX + "\" (" + Command.PREFIX + "help)", 2, height - 20, 0x99ff99);
			textRenderer.drawWithShadow(matrix, "Use " + Command.PREFIX + "clickgui to reset the clickgui", 2, height - 10, 0x9999ff);
		}
	}
}

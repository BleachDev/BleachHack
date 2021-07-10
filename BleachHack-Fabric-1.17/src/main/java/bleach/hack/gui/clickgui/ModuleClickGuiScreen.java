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

import bleach.hack.BleachHack;
import bleach.hack.command.Command;
import bleach.hack.gui.clickgui.window.ClickGuiWindow;
import bleach.hack.gui.clickgui.window.ModuleWindow;
import bleach.hack.gui.window.Window;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.util.io.BleachFileHelper;
import net.minecraft.SharedConstants;
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
		addDrawableChild(searchField);
	}

	public void initWindows() {
		int len = (int) ModuleManager.getModule("ClickGui").getSetting(0).asSlider().getValue();

		addWindow(new ModuleWindow(ModuleManager.getModulesInCat(ModuleCategory.PLAYER),
				30, 50, len, "Player", new ItemStack(Items.ARMOR_STAND)));

		addWindow(new ModuleWindow(ModuleManager.getModulesInCat(ModuleCategory.RENDER),
				30, 66, len, "Render", new ItemStack(Items.YELLOW_STAINED_GLASS)));

		addWindow(new ModuleWindow(ModuleManager.getModulesInCat(ModuleCategory.COMBAT),
				30, 82, len, "Combat", new ItemStack(Items.TOTEM_OF_UNDYING)));

		addWindow(new ModuleWindow(ModuleManager.getModulesInCat(ModuleCategory.MOVEMENT),
				30, 98, len, "Movement", new ItemStack(Items.POTION)));

		addWindow(new ModuleWindow(ModuleManager.getModulesInCat(ModuleCategory.EXPLOITS),
				30, 114, len, "Exploits", new ItemStack(Items.REPEATING_COMMAND_BLOCK)));

		addWindow(new ModuleWindow(ModuleManager.getModulesInCat(ModuleCategory.MISC),
				30, 130, len, "Misc", new ItemStack(Items.NAUTILUS_SHELL)));

		addWindow(new ModuleWindow(ModuleManager.getModulesInCat(ModuleCategory.WORLD),
				30, 146, len, "World", new ItemStack(Items.GRASS_BLOCK)));

		for (Window w: getWindows()) {
			if (w instanceof ClickGuiWindow) {
				((ClickGuiWindow) w).hiding = true;
			}
		}
	}

	public void onClose() {
		ModuleManager.getModule("ClickGui").setEnabled(false);
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
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

		super.render(matrices, mouseX, mouseY, delta);

		textRenderer.draw(matrices, "BleachHack-" + BleachHack.VERSION + "-" + SharedConstants.getGameVersion().getName(), 3, 3, 0x305090);
		textRenderer.draw(matrices, "BleachHack-" + BleachHack.VERSION + "-" + SharedConstants.getGameVersion().getName(), 2, 2, 0x6090d0);

		if (ModuleManager.getModule("ClickGui").getSetting(2).asToggle().state) {
			textRenderer.drawWithShadow(matrices, "Current prefix is: \"" + Command.PREFIX + "\" (" + Command.PREFIX + "help)", 2, height - 20, 0x99ff99);
			textRenderer.drawWithShadow(matrices, "Use " + Command.PREFIX + "clickgui to reset the clickgui", 2, height - 10, 0x9999ff);
		}
	}
}

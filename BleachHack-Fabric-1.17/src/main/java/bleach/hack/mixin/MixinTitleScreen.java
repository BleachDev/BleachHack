/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.mixin;

import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.gson.JsonPrimitive;

import bleach.hack.gui.title.AccountManagerScreen;
import bleach.hack.gui.title.BleachTitleScreen;
import bleach.hack.gui.window.WindowManagerScreen;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.util.file.BleachFileHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen {

	protected MixinTitleScreen(Text title) {
		super(title);
	}

	@Inject(method = "init()V", at = @At("HEAD"))
	private void init(CallbackInfo info) {
		if (BleachTitleScreen.customTitleScreen) {
			MinecraftClient.getInstance().openScreen(
					new WindowManagerScreen(
							Triple.of(new BleachTitleScreen(), "BleachHack", new ItemStack(Items.MUSIC_DISC_CAT)),
							Triple.of(new AccountManagerScreen(), "Account Mang", new ItemStack(Items.PAPER)),
							Triple.of(ClickGui.clickGui, "ClickGui", new ItemStack(Items.TOTEM_OF_UNDYING))) {
						
						public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
							if (keyCode == ModuleManager.getModule("ClickGui").getKey()) {
								selectWindow(2);
							}

							return super.keyPressed(keyCode, scanCode, modifiers);
						}
					});
		} else {
			addDrawableChild(new ButtonWidget(width / 2 - 124, height / 4 + 96, 20, 20, new LiteralText("BH"), button -> {
				BleachTitleScreen.customTitleScreen = !BleachTitleScreen.customTitleScreen;
				BleachFileHelper.saveMiscSetting("customTitleScreen", new JsonPrimitive(true));
				client.openScreen(new TitleScreen(false));
			}));
		}
	}
}

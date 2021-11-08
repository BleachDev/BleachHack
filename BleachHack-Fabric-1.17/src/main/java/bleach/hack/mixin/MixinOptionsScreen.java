/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import bleach.hack.gui.BleachOptionsScreen;
import bleach.hack.gui.BleachTitleScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.Element;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

@Mixin(OptionsScreen.class)
public abstract class MixinOptionsScreen extends Screen {

	protected MixinOptionsScreen(Text title) {
		super(title);
	}

	@Inject(method = "init", at = @At("RETURN"))
	protected void init(CallbackInfo callback) {
		if (!BleachTitleScreen.unload) {
            addDrawableChild(new ButtonWidget(this.width / 2 - 180, this.height / 6 + 120 - 6, 20, 20, new LiteralText("BH"), button -> {
                client.setScreen(new BleachOptionsScreen(this));
            }));
		}
	}
}

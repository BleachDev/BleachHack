/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.gui.widget;

import java.lang.reflect.Field;

import bleach.hack.util.FabricReflect;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class TextPassFieldWidget extends TextFieldWidget {

	public TextPassFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
		super(textRenderer, x, y, width, height, text);
	}

	public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float delta) {
		String realText = getText();

		Field textField = FabricReflect.getField(TextFieldWidget.class, "field_2092", "text");

		try {
			textField.set(this, new String(new char[realText.length()]).replace("\0", "\u2022"));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			System.err.println("Error reflecting text");
		}

		super.renderButton(matrix, mouseX, mouseY, delta);

		try {
			textField.set(this, realText);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			System.err.println("Error reflecting text");
		}
	}
}

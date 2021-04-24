/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.gui.widget;

import bleach.hack.gui.window.Window;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class BleachCheckbox extends AbstractPressableButtonWidget {

	public boolean checked;

	public BleachCheckbox(int x, int y, Text text, boolean pressed) {
		super(x, y, 10 + MinecraftClient.getInstance().textRenderer.getWidth(text), 10, text);
		checked = pressed;
	}

	public void onPress() {
		checked = !checked;
	}

	public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float delta) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		
		int color = mouseX > x && mouseX < x + 10 && mouseY > y && mouseY < y + 10 ? 0x906060ff : 0x9040409f;
		/*fill(matrix, x, y, x + 11, y + 11, color);
		fill(matrix, x, y, x + 1, y + 11, 0xff303030);
		fill(matrix, x, y + 10, x + 11, y + 11, 0xffb0b0b0);
		fill(matrix, x, y, x + 10, y + 1, 0xff303030);
		fill(matrix, x + 10, y, x + 11, y + 11, 0xffb0b0b0);*/
		Window.fill(matrix, x, y, x + 11, y + 11, color);

		if (checked) {
			textRenderer.draw(matrix, "\u2714", x + 2, y + 2, 0xffeeff);
			//fill(matrix, x + 3, y + 3, x + 7, y + 7, 0xffffffff);
		}

		drawStringWithShadow(matrix, textRenderer, getMessage().getString(), x + 15, y + 2, 0xC0C0C0);
	}
}

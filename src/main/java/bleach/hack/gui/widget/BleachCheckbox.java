/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
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
package bleach.hack.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.lwjgl.opengl.GL11;

public class BleachCheckbox extends AbstractPressableButtonWidget {

    public boolean checked;

    public BleachCheckbox(int int_1, int int_2, Text text, boolean pressed) {
        super(int_1, int_2, 10 + MinecraftClient.getInstance().textRenderer.getWidth(text), 10, text);
        checked = pressed;
    }

    public void onPress() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        checked = !checked;
    }

    public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient_1 = MinecraftClient.getInstance();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        TextRenderer textRenderer = minecraftClient_1.textRenderer;
        int color = mouseX > x && mouseX < x + 10 && mouseY > y && mouseY < y + 10 ? 0xffc0c0c0 : 0xffe0e0e0;
        fill(matrix, x, y, x + 11, y + 11, color);
        fill(matrix, x, y, x + 1, y + 11, 0xff303030);
        fill(matrix, x, y + 10, x + 11, y + 11, 0xffb0b0b0);
        fill(matrix, x, y, x + 10, y + 1, 0xff303030);
        fill(matrix, x + 10, y, x + 11, y + 11, 0xffb0b0b0);
        if (checked)
            textRenderer.draw(matrix, "\u2714", x + 2, y + 2, 0x000000); //fill(x + 3, y + 5, x + 8, y + 6, 0xff000000);
        drawStringWithShadow(matrix, textRenderer, getMessage().getString(), x + 15, y + 2, 0xC0C0C0);
    }
}

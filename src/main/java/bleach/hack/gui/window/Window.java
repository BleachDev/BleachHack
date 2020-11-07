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
package bleach.hack.gui.window;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.utils.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.ArrayList;
import java.util.List;

public class Window {

    public int x1;
    public int y1;
    public int x2;
    public int y2;

    public String title;
    public ItemStack icon;

    public boolean closed;
    public boolean selected = false;

    public List<WindowButton> buttons = new ArrayList<>();

    private boolean dragging = false;
    private int dragOffX;
    private int dragOffY;

    public int inactiveTime = 0;

    public Window(int x1, int y1, int x2, int y2, String title, ItemStack icon) {
        this(x1, y1, x2, y2, title, icon, false);
    }

    public Window(int x1, int y1, int x2, int y2, String title, ItemStack icon, boolean closed) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.title = title;
        this.icon = icon;
        this.closed = closed;
    }

    public void render(MatrixStack matrix, int mX, int mY) {
        TextRenderer textRend = MinecraftClient.getInstance().textRenderer;

        if (dragging) {
            x1 = Math.max(0, mX - dragOffX);
            y1 = Math.max(0, mY - dragOffY);
            x2 = (x2 - x1) + mX - dragOffX - Math.min(0, mX - dragOffX);
            y2 = (y2 - y1) + mY - dragOffY - Math.min(0, mY - dragOffY);
        }

        drawBar(matrix, mX, mY, textRend);

        for (WindowButton w : buttons) {
            int bx1 = x1 + w.x1;
            int by1 = y1 + w.y1;
            int bx2 = x1 + w.x2;
            int by2 = y1 + w.y2;

            DrawableHelper.fill(matrix, bx1, by1, bx2 - 1, by2 - 1, 0xffb0b0b0);
            DrawableHelper.fill(matrix, bx1 + 1, by1 + 1, bx2, by2, 0xff000000);
            DrawableHelper.fill(matrix, bx1 + 1, by1 + 1, bx2 - 1, by2 - 1,
                    selected && mX >= bx1 && mX <= bx2 && mY >= by1 && mY <= by2 ? 0xff959595 : 0xff858585);
            textRend.drawWithShadow(matrix, w.text, bx1 + (bx2 - bx1) / 2 - textRend.getWidth(w.text) / 2, by1 + (by2 - by1) / 2 - 4, -1);
        }

        /* window icon */
        boolean drawIcon = icon != null && ModuleManager.getModule(ClickGui.class).getSetting(5).asToggle().state;
        if (drawIcon) {
            GL11.glPushMatrix();
            GL11.glScaled(0.55, 0.55, 1);
            DiffuseLighting.enableGuiDepthLighting();
            MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(icon, (int) ((x1 + 3) * 1 / 0.55), (int) ((y1 + 3) * 1 / 0.55));
            DiffuseLighting.disableGuiDepthLighting();
            GL11.glPopMatrix();
        }

        /* window title */
        int iconWidth = drawIcon ? (icon.getItem() == Items.ARMOR_STAND || icon.getItem() == Items.POTION ? 13 : 14) : 4;
        textRend.drawWithShadow(matrix, title, x1 + iconWidth, y1 + 3, ColorUtils.textColor());
        if (inactiveTime >= 0) {
            inactiveTime--;
        }
    }

    protected void drawBar(MatrixStack matrix, int mX, int mY, TextRenderer textRend) {
        /* background and title bar */
        fillGrey(matrix, x1, y1, x2, y2);
        fillGradient(matrix, x1 + 2, y1 + 2, x2 - 2, y1 + 12, (selected ? 0xff0000ff : 0xff606060), (selected ? 0xff4080ff : 0xffa0a0a0));

        /* buttons */
        fillGrey(matrix, x2 - 12, y1 + 3, x2 - 4, y1 + 11);
        textRend.draw(matrix, "x", x2 - 11, y1 + 2, 0x000000);

        fillGrey(matrix, x2 - 22, y1 + 3, x2 - 14, y1 + 11);
        textRend.draw(matrix, "_", x2 - 21, y1 + 1, 0x000000);
    }

    public boolean shouldClose(int mX, int mY) {
        return selected && mX > x2 - 23 && mX < x2 && mY > y1 + 2 && mY < y1 + 12;
    }

    public void onMousePressed(int x, int y) {
        if (inactiveTime > 0) {
            return;
        }

        if (x > x1 + 2 && x < x2 - 2 && y > y1 + 2 && y < y1 + 12) {
            dragging = true;
            dragOffX = x - x1;
            dragOffY = y - y1;
        }

        for (WindowButton w : buttons) {
            if (x >= x1 + w.x1 && x <= x1 + w.x2 && y >= y1 + w.y1 && y <= y1 + w.y2) {
                w.action.run();
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
        }
    }

    public void onMouseReleased(int x, int y) {
        dragging = false;
    }

    public void fillGrey(MatrixStack matrix, int x1, int y1, int x2, int y2) {
        DrawableHelper.fill(matrix, x1, y1, x2 - 1, y2 - 1, 0xffb0b0b0);
        DrawableHelper.fill(matrix, x1 + 1, y1 + 1, x2, y2, 0xff000000);
        DrawableHelper.fill(matrix, x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xff858585);
    }

    public void fillGradient(MatrixStack matrix, int x1, int y1, int x2, int y2, int color1, int color2) {
        float float_1 = (color1 >> 24 & 255) / 255.0F;
        float float_2 = (color1 >> 16 & 255) / 255.0F;
        float float_3 = (color1 >> 8 & 255) / 255.0F;
        float float_4 = (color1 & 255) / 255.0F;
        float float_5 = (color2 >> 24 & 255) / 255.0F;
        float float_6 = (color2 >> 16 & 255) / 255.0F;
        float float_7 = (color2 >> 8 & 255) / 255.0F;
        float float_8 = (color2 & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glShadeModel(7425);
        Tessellator tessellator_1 = Tessellator.getInstance();
        BufferBuilder bufferBuilder_1 = tessellator_1.getBuffer();
        bufferBuilder_1.begin(7, VertexFormats.POSITION_COLOR);
        bufferBuilder_1.vertex(x1, y1, 0).color(float_2, float_3, float_4, float_1).next();
        bufferBuilder_1.vertex(x1, y2, 0).color(float_2, float_3, float_4, float_1).next();
        bufferBuilder_1.vertex(x2, y2, 0).color(float_6, float_7, float_8, float_5).next();
        bufferBuilder_1.vertex(x2, y1, 0).color(float_6, float_7, float_8, float_5).next();
        tessellator_1.draw();
        GL11.glShadeModel(7424);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}

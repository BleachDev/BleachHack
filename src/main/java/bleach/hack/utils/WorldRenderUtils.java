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
package bleach.hack.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.Map.Entry;

public class WorldRenderUtils {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void drawText(String str, double x, double y, double z, double scale) {
        glSetup(x, y, z);

        GL11.glScaled(-0.025 * scale, -0.025 * scale, 0.025 * scale);

        int i = mc.textRenderer.getWidth(str) / 2;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, VertexFormats.POSITION_COLOR);
        float f = mc.options.getTextBackgroundOpacity(0.25F);
        bufferbuilder.vertex(-i - 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, f).next();
        bufferbuilder.vertex(-i - 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, f).next();
        bufferbuilder.vertex(i + 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, f).next();
        bufferbuilder.vertex(i + 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, f).next();
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        mc.textRenderer.draw(new MatrixStack(), str, -i, 0, 553648127);
        mc.textRenderer.draw(new MatrixStack(), str, -i, 0, -1);

        glCleanup();
    }

    public static void drawItem(double x, double y, double z, double offX, double offY, double scale, ItemStack item) {
        glSetup(x, y, z);

        GL11.glScaled(0.4 * scale, 0.4 * scale, 0);

        GL11.glTranslated(offX, offY, 0);
        if (item.getItem() instanceof BlockItem) GL11.glRotatef(180F, 1F, 180F, 10F);
        mc.getItemRenderer().renderItem(new ItemStack(
                item.getItem()), Mode.GUI, 0, 0, new MatrixStack(), mc.getBufferBuilders().getEntityVertexConsumers());
        if (item.getItem() instanceof BlockItem) GL11.glRotatef(-180F, -1F, -180F, -10F);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glScalef(-0.05F, -0.05F, 0);

        if (item.getCount() > 0) {
            int w = mc.textRenderer.getWidth("x" + item.getCount()) / 2;
            mc.textRenderer.drawWithShadow(new MatrixStack(), "x" + item.getCount(), 7 - w, 5, 0xffffff);
        }

        GL11.glScalef(0.85F, 0.85F, 0.85F);

        int c = 0;
        for (Entry<Enchantment, Integer> m : EnchantmentHelper.get(item).entrySet()) {
            String text = I18n.translate(m.getKey().getName(2).getString());

            if (text.isEmpty()) continue;

            String subText = text.substring(0, Math.min(text.length(), 2)) + m.getValue();

            int w1 = mc.textRenderer.getWidth(subText) / 2;
            mc.textRenderer.drawWithShadow(new MatrixStack(),
                    subText, -4 - w1, c * 10 - 1,
                    m.getKey() == Enchantments.VANISHING_CURSE || m.getKey() == Enchantments.BINDING_CURSE
                            ? 0xff5050 : 0xffb0e0);
            c--;
        }

        GL11.glScalef(0.6F, 0.6F, 0.6F);
        if (item.isDamageable() && item.getMaxDamage() > 0) {
            String dur = item.getMaxDamage() - item.getDamage() + "";
            int color = MathHelper.hsvToRgb((float) MathHelper.clamp(item.getMaxDamage() - item.getDamage() / item.getMaxDamage(),
                    0f,
                    1f) / 3.0F, 1.0F, 1.0F);
            mc.textRenderer.drawWithShadow(new MatrixStack(), dur, -8 - dur.length() * 3, 15, new Color(color >> 16 & 255, color >> 8 & 255, color & 255).getRGB());
        }

        glCleanup();
    }

    public static void glSetup(double x, double y, double z) {
        GL11.glPushMatrix();
        RenderUtils.offsetRender();
        GL11.glTranslated(x, y, z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-mc.player.yaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(mc.player.pitch, 1.0F, 0.0F, 0.0F);
        GL11.glDepthFunc(GL11.GL_ALWAYS);

        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

    }

    public static void glCleanup() {
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }
}

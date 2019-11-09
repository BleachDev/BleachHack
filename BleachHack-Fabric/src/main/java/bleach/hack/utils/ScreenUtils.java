package bleach.hack.utils;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.item.ItemStack;

public class ScreenUtils {
	
	public static void drawBackground(ItemStack icon, String title, int x1, int y1, int x2, int y2) {
		fillGrey(x1, y1, x2, y2);
		fillGradient(x1 + 2, y1 + 2, x2 - 2, y1 + 12, 0xff0000ff, 0xff4080ff);
		
		/* buttons */
		fillGrey(x2 - 12, y1 + 3, x2 - 4, y1 + 11);
		MinecraftClient.getInstance().textRenderer.draw("x", x2 - 11, y1 + 2, 0x000000);
		
		fillGrey(x2 - 22, y1 + 3, x2 - 14, y1 + 11);
		MinecraftClient.getInstance().textRenderer.draw("_", x2 - 21, y1 + 1, 0x000000);
		
		/* window icon */
		if(icon != null) {
			GL11.glPushMatrix();
			GL11.glScaled(0.55, 0.55, 1);
			GuiLighting.enableForItems();
			MinecraftClient.getInstance().getItemRenderer().renderGuiItem(icon, (int)((x1 + 3) * 1/0.55), (int)((y1 + 3) * 1/0.55));
			GL11.glPopMatrix();
		}
		
		/* window title */
		MinecraftClient.getInstance().textRenderer.draw(title, x1 + (icon == null ? 4 : 15), y1 + 3, -1);
	}
	
	private static void fillGrey(int x1, int y1, int x2, int y2) {
		Screen.fill(x1, y1, x2 - 1, y2 - 1, 0xffb0b0b0);
		Screen.fill(x1 + 1, y1 + 1, x2, y2, 0xff000000);
		Screen.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xff7a7a7a);
	}
	
	private static void fillGradient(int x1, int y1, int x2, int y2, int color1, int color2) {
		float float_1 = (float)(color1 >> 24 & 255) / 255.0F;
		float float_2 = (float)(color1 >> 16 & 255) / 255.0F;
		float float_3 = (float)(color1 >> 8 & 255) / 255.0F;
		float float_4 = (float)(color1 & 255) / 255.0F;
		float float_5 = (float)(color2 >> 24 & 255) / 255.0F;
		float float_6 = (float)(color2 >> 16 & 255) / 255.0F;
		float float_7 = (float)(color2 >> 8 & 255) / 255.0F;
		float float_8 = (float)(color2 & 255) / 255.0F;
		GlStateManager.disableTexture();
		GlStateManager.enableBlend();
		GlStateManager.disableAlphaTest();
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.shadeModel(7425);
		Tessellator tessellator_1 = Tessellator.getInstance();
		BufferBuilder bufferBuilder_1 = tessellator_1.getBufferBuilder();
		bufferBuilder_1.begin(7, VertexFormats.POSITION_COLOR);
		bufferBuilder_1.vertex((double)x1, (double)y1, 0.0).color(float_2, float_3, float_4, float_1).next();
		bufferBuilder_1.vertex((double)x1, (double)y2, 0.0).color(float_2, float_3, float_4, float_1).next();
		bufferBuilder_1.vertex((double)x2, (double)y2, 0.0).color(float_6, float_7, float_8, float_5).next();
		bufferBuilder_1.vertex((double)x2, (double)y1, 0.0).color(float_6, float_7, float_8, float_5).next();
		tessellator_1.draw();
		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableAlphaTest();
		GlStateManager.enableTexture();
	}
}

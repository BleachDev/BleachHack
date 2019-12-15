package bleach.hack.gui.window;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.item.ItemStack;

public class Window {

	public int x1;
	public int y1;
	public int x2;
	public int y2;
	
	public String title;
	public ItemStack icon;
	
	public boolean closed;
	public boolean selected = false;
	
	private boolean dragging = false;
	private int dragOffX;
	private int dragOffY;
	
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
	
	public void render(int mX, int mY) {
		if(dragging) {
			x2 = (x2 - x1) + mX - dragOffX;
			y2 = (y2 - y1) + mY - dragOffY;
			x1 = mX - dragOffX;
			y1 = mY - dragOffY;
		}
		
		/* background and title bar */
		fillGrey(x1, y1, x2, y2);
		fillGradient(x1 + 2, y1 + 2, x2 - 2, y1 + 12, (selected ? 0xff0000ff : 0xff606060), (selected ? 0xff4080ff : 0xffa0a0a0));
		
		/* buttons */
		fillGrey(x2 - 12, y1 + 3, x2 - 4, y1 + 11);
		MinecraftClient.getInstance().textRenderer.draw("x", x2 - 11, y1 + 2, 0x000000);
		
		fillGrey(x2 - 22, y1 + 3, x2 - 14, y1 + 11);
		MinecraftClient.getInstance().textRenderer.draw("_", x2 - 21, y1 + 1, 0x000000);
		
		/* window icon */
		if(icon != null && selected) {
			GL11.glPushMatrix();
			GL11.glScaled(0.55, 0.55, 1);
			DiffuseLighting.enableGuiDepthLighting();
			MinecraftClient.getInstance().getItemRenderer().renderGuiItem(icon, (int)((x1 + 3) * 1/0.55), (int)((y1 + 3) * 1/0.55));
			GL11.glPopMatrix();
		}
		
		/* window title */
		MinecraftClient.getInstance().textRenderer.draw(title, x1 + (icon == null  || !selected ? 4 : 15), y1 + 3, -1);
	}
	
	public boolean shouldClose(int mX, int mY) {
		return selected && mX > x2 - 23 && mX < x2 && mY > y1 + 2 && mY < y1 + 12;
	}
	
	public void onMousePressed(int x, int y) {
		if(x > x1 + 2 && x < x2 - 2 && y > y1 + 2 && y < y1 + 12) {
			dragging = true;
			dragOffX = x - x1;
			dragOffY = y - y1;
		}
	}
	
	public void onMouseReleased(int x, int y) {
		dragging = false;
	}
	
	private void fillGrey(int x1, int y1, int x2, int y2) {
		Screen.fill(x1, y1, x2 - 1, y2 - 1, 0xffb0b0b0);
		Screen.fill(x1 + 1, y1 + 1, x2, y2, 0xff000000);
		Screen.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xff858585);
	}
		
	private void fillGradient(int x1, int y1, int x2, int y2, int color1, int color2) {
		float float_1 = (float)(color1 >> 24 & 255) / 255.0F;
		float float_2 = (float)(color1 >> 16 & 255) / 255.0F;
		float float_3 = (float)(color1 >> 8 & 255) / 255.0F;
		float float_4 = (float)(color1 & 255) / 255.0F;
		float float_5 = (float)(color2 >> 24 & 255) / 255.0F;
		float float_6 = (float)(color2 >> 16 & 255) / 255.0F;
		float float_7 = (float)(color2 >> 8 & 255) / 255.0F;
		float float_8 = (float)(color2 & 255) / 255.0F;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glShadeModel(7425);
		Tessellator tessellator_1 = Tessellator.getInstance();
		BufferBuilder bufferBuilder_1 = tessellator_1.getBuffer();
		bufferBuilder_1.begin(7, VertexFormats.POSITION_COLOR);
		bufferBuilder_1.vertex((double)x1, (double)y1, 0).color(float_2, float_3, float_4, float_1).next();
		bufferBuilder_1.vertex((double)x1, (double)y2, 0).color(float_2, float_3, float_4, float_1).next();
		bufferBuilder_1.vertex((double)x2, (double)y2, 0).color(float_6, float_7, float_8, float_5).next();
		bufferBuilder_1.vertex((double)x2, (double)y1, 0).color(float_6, float_7, float_8, float_5).next();
		tessellator_1.draw();
		GL11.glShadeModel(7424);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
}

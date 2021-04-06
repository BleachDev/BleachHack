package bleach.hack.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import bleach.hack.BleachHack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;

public class InteractionScreen extends Screen {

	private String name, current;
	private int crosshairX, crosshairY, at = -1;
	private float yaw, pitch;

	public InteractionScreen(String name) {
		super(new LiteralText("Menu Screen"));
		this.name = name;
		System.out.println(name);
	}

	public void init() {
		super.init();
		this.cursorMode(GLFW.GLFW_CURSOR_HIDDEN);
		yaw = client.player.yaw;
		pitch = client.player.pitch;
	}

	private void cursorMode(int mode) {
		KeyBinding.unpressAll();
		double x = (double) (this.client.getWindow().getWidth() / 2);
		double y = (double) (this.client.getWindow().getHeight() / 2);
		InputUtil.setCursorParameters(this.client.getWindow().getHandle(), GLFW.GLFW_CURSOR_HIDDEN, x, y);
	}

	public void tick() {
		if (GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(),
				GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_RELEASE)
			onClose();
	}

	public void onClose() {
		cursorMode(GLFW.GLFW_CURSOR_NORMAL);
		if (current != null) {
			String message = BleachHack.interaction.get(current).replaceAll("%name", name);
			if (message.startsWith("%suggestion")) 
				client.openScreen(new ChatScreen(message.replaceFirst("%suggestion", "")));
			else {
				client.player.sendChatMessage(message);
				client.openScreen((Screen) null);
			}
		} else
			client.openScreen((Screen) null);
	}

	public boolean isPauseScreen() {
		return false;
	}

	public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE_MINUS_DST_COLOR,
				GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SrcFactor.ONE,
				GlStateManager.DstFactor.ZERO);
		drawTexture(matrix, crosshairX - 8, crosshairY - 8, 0, 0, 15, 15);
		
		drawDots(matrix, (int) (Math.min(height, width) / 2 * 0.75), mouseX, mouseY);
		
		int scale = client.options.guiScale;
		Vector2 mouse = new Vector2(mouseX, mouseY);
		Vector2 center = new Vector2(width / 2, height / 2);
		mouse.subtract(center);
		mouse.normalize();
		Vector2 cross = mouse;
		
		if (scale == 0)
			scale = 4;
		
		if (Math.hypot(width / 2 - mouseX, height / 2 - mouseY) < 1f / scale * 200f)
			mouse.multiply((float) Math.hypot(width / 2 - mouseX, height / 2 - mouseY));
		else 
			mouse.multiply(1f / scale * 200f);
		
		
		this.crosshairX = (int) mouse.x + width / 2;
		this.crosshairY = (int) mouse.y + height / 2;
		
		client.player.yaw = yaw + cross.x / 3;
		client.player.pitch = MathHelper.clamp(pitch + cross.y / 3, -90f, 90f);
		super.render(matrix, mouseX, mouseY, delta);
	}


	private void drawRect(MatrixStack matrix, int startX, int startY, int width, int height, int colorInner,int colorOuter) {
		drawHorizontalLine(matrix, startX, startX + width, startY, colorOuter);
		drawHorizontalLine(matrix, startX, startX + width, startY + height, colorOuter);
		drawVerticalLine(matrix, startX, startY, startY + height, colorOuter);
		drawVerticalLine(matrix, startX + width, startY, startY + height, colorOuter);
		fill(matrix, startX + 1, startY + 1, startX + width, startY + height, colorInner);
	}

	private void drawDots(MatrixStack matrix, int radius, int mouseX, int mouseY) {
		HashMap<String, String> map = BleachHack.interaction;
		ArrayList<Point> pointList = new ArrayList<Point>();
		String cache[] = new String[map.size()];
		double lowestDistance = Double.MAX_VALUE;
		int i = -1;
		for (String string: map.keySet()) {
			i++;
			double s = (double) i / map.size() * 2 * Math.PI;
			int x = (int) Math.round(radius * Math.cos(s) + width / 2);
			int y = (int) Math.round(radius * Math.sin(s) + height / 2);
			drawTextField(matrix, x, y, string);
			cache[i] = string;
			pointList.add(new Point(x, y));
			
			if (Math.hypot(x - mouseX, y - mouseY) < lowestDistance) {
				lowestDistance = Math.hypot(x - mouseX, y - mouseY);
				at = i;
			}
		}
		for (int j = 0; j < map.size(); j++) {
			Point current = pointList.get(j);
			if (pointList.get(at) == current) {
				drawDot(matrix, current.x - 4, current.y - 4, 0xFF4CFF00);
				this.current = cache[at];
			}
			else
				drawDot(matrix, current.x - 4, current.y - 4, 0xFF0094FF);
		}
	}
	

	private void drawTextField(MatrixStack matrix, int x, int y, String key) {
		if (x >= width / 2) {
			drawRect(matrix, x + 10, y - 8, textRenderer.getWidth(key) + 3, 15, 0x80808080, 0xFF000000);
			drawStringWithShadow(matrix, textRenderer, key, x + 12, y - 4, 0xFFFFFFFF);
		} else {
			drawRect(matrix, x - 14 - textRenderer.getWidth(key), y - 8, textRenderer.getWidth(key) + 3, 15, 0x80808080, 0xFF000000);
			drawStringWithShadow(matrix, textRenderer, key, x - 12 - textRenderer.getWidth(key), y - 4, 0xFFFFFFFF);
		}
	}
	
	// Literally drawing it in code
	private void drawDot(MatrixStack matrix, int startX, int startY, int colorInner) {
		// Draw dot itself
		drawHorizontalLine(matrix, startX + 2, startX + 5, startY, 0xFF000000);
		drawHorizontalLine(matrix, startX + 1, startX + 6, startY + 1, 0xFF000000);
		drawHorizontalLine(matrix, startX + 2, startX + 5, startY + 1, colorInner);
		fill(matrix, startX, startY + 2, startX + 8, startY + 6, 0xFF000000);
		fill(matrix, startX + 1, startY + 2, startX + 7, startY + 6, colorInner);
		drawHorizontalLine(matrix, startX + 1, startX + 6, startY + 6, 0xFF000000);
		drawHorizontalLine(matrix, startX + 2, startX + 5, startY + 6, colorInner);
		drawHorizontalLine(matrix, startX + 2, startX + 5, startY + 7, 0xFF000000);
		
		// Draw light overlay
		drawHorizontalLine(matrix, startX + 2, startX + 3, startY + 1, 0x80FFFFFF);
		drawHorizontalLine(matrix, startX + 1, startX + 1, startY + 2, 0x80FFFFFF);
	}
}


// Bruh literally making my own Vector class because I am smart
class Vector2 {
    float x, y;

    Vector2 (float x, float y) {
        this.x = x;
        this.y = y;
    }

    void normalize() {
        float mag = getMag();
        if (mag != 0 && mag != 1)
            divide(mag);
    }

    void subtract (Vector2 vec) {
        this.x -= vec.x;
        this.y -= vec.y;
    }

    void divide(float n) {
        x /= n;
        y /= n;
    }

    void multiply(float n) {
        x *= n;
        y *= n;
    }

    private float getMag() {
        return (float) Math.sqrt(x * x + y * y);
    }
}

package bleach.hack.setting.base;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.systems.RenderSystem;

import bleach.hack.gui.clickgui.window.ModuleWindow;
import bleach.hack.gui.window.Window;
import bleach.hack.util.file.BleachFileHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;

public class SettingColor extends SettingBase {

	public String text;
	public float hue;
	public float sat;
	public float bri;

	protected float defaultHue;
	protected float defaultSat;
	protected float defaultBri;

	public SettingColor(String text, float r, float g, float b, boolean hsb) {
		this.text = text;
		if (hsb) {
			this.hue = r;
			this.sat = g;
			this.bri = b;
		} else {
			float[] vals = Color.RGBtoHSB((int) (r * 255), (int) (g * 255), (int) (b * 255), null);
			this.hue = vals[0];
			this.sat = vals[1];
			this.bri = vals[2];
		}

		defaultHue = hue;
		defaultSat = sat;
		defaultBri = bri;
	}

	public String getName() {
		return text;
	}

	public void render(ModuleWindow window, MatrixStack matrix, int x, int y, int len) {
		int sx = x + 3,
				sy = y + 2,
				ex = x + len - 18,
				ey = y + getHeight(len) - 2;

		Window.fill(matrix, sx - 1, sy - 1, ex + 1, ey + 1, 0xff8070b0, 0xff6060b0, 0x00000000);

		DrawableHelper.fill(matrix, sx, sy, ex, ey, -1);
		Color satColor = Color.getHSBColor(1f - hue, 1f, 1f);

		RenderSystem.enableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.shadeModel(GL11.GL_SMOOTH);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(ex, sy, 0).color(satColor.getRed(), satColor.getBlue(), satColor.getGreen(), 255).next();
		bufferBuilder.vertex(sx, sy, 0).color(satColor.getRed(), satColor.getBlue(), satColor.getGreen(), 0).next();
		bufferBuilder.vertex(sx, ey, 0).color(satColor.getRed(), satColor.getBlue(), satColor.getGreen(), 0).next();
		bufferBuilder.vertex(ex, ey, 0).color(satColor.getRed(), satColor.getBlue(), satColor.getGreen(), 255).next();
		tessellator.draw();

		bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(ex, sy, 0).color(0, 0, 0, 0).next();
		bufferBuilder.vertex(sx, sy, 0).color(0, 0, 0, 0).next();
		bufferBuilder.vertex(sx, ey, 0).color(0, 0, 0, 255).next();
		bufferBuilder.vertex(ex, ey, 0).color(0, 0, 0, 255).next();
		tessellator.draw();

		RenderSystem.shadeModel(GL11.GL_FLAT);
		RenderSystem.disableBlend();
		RenderSystem.disableTexture();

		if (window.mouseOver(sx, sy, ex, ey) && window.lmHeld) {
			bri = 1f - 1f / ((float) (ey - sy) / (window.mouseY - sy));
			sat = 1f / ((float) (ex - sx) / (window.mouseX - sx));
		}

		int briY = (int) (ey - (ey - sy) * bri);
		int satX = (int) (sx + (ex - sx) * sat);

		DrawableHelper.fill(matrix, satX - 2, briY, satX, briY + 1, 0xffd0d0d0);
		DrawableHelper.fill(matrix, satX + 1, briY, satX + 3, briY + 1, 0xffd0d0d0);
		DrawableHelper.fill(matrix, satX, briY - 2, satX + 1, briY, 0xffd0d0d0);
		DrawableHelper.fill(matrix, satX, briY + 1, satX + 1, briY + 3, 0xffd0d0d0);

		matrix.push();
		matrix.scale(0.75f, 0.75f, 1f);
		MinecraftClient.getInstance().textRenderer.draw(matrix, text, (int) ((sx + 1) * 1 / 0.75), (int) ((sy + 1) * 1 / 0.75), 0x000000);
		matrix.pop();

		sx = ex + 5;
		ex = ex + 12;
		Window.fill(matrix, sx - 1, sy - 1, ex + 1, ey + 1, 0xff8070b0, 0xff6060b0, 0x00000000);

		for (int i = sy; i < ey; i++) {
			float curHue = 1f / ((float) (ey - sy) / (i - sy));
			DrawableHelper.fill(matrix, sx, i, ex, i + 1, Color.getHSBColor(curHue, 1f, 1f).getRGB());
		}

		if (window.mouseOver(sx, sy, ex, ey) && window.lmHeld) {
			BleachFileHelper.SCHEDULE_SAVE_MODULES = true;
			hue = 1f / ((float) (ey - sy) / (window.mouseY - sy));
		}

		int hueY = (int) (sy + (ey - sy) * hue);
		DrawableHelper.fill(matrix, sx, hueY - 1, sx + 1, hueY + 2, 0xffa0a0a0);
		DrawableHelper.fill(matrix, ex - 1, hueY - 1, ex, hueY + 2, 0xffa0a0a0);
		DrawableHelper.fill(matrix, sx, hueY, sx + 2, hueY + 1, 0xffa0a0a0);
		DrawableHelper.fill(matrix, ex - 2, hueY, ex, hueY + 1, 0xffa0a0a0);
	}

	public SettingColor withDesc(String desc) {
		description = desc;
		return this;
	}

	public int getHeight(int len) {
		return len - len / 4 - 1;
	}

	public void readSettings(JsonElement settings) {
		if (settings.isJsonObject()) {
			JsonObject jo = settings.getAsJsonObject();
			hue = jo.get("hue").getAsFloat();
			sat = jo.get("sat").getAsFloat();
			bri = jo.get("bri").getAsFloat();
		}
	}

	public JsonElement saveSettings() {
		JsonObject jo = new JsonObject();
		jo.add("hue", new JsonPrimitive(hue));
		jo.add("sat", new JsonPrimitive(sat));
		jo.add("bri", new JsonPrimitive(bri));

		return jo;
	}

	public int getRGB() {
		return Color.HSBtoRGB(hue, sat, bri);
	}

	public float[] getRGBFloat() {
		Color col = Color.getHSBColor(hue, sat, bri);
		return new float[] { col.getRed() / 255f, col.getGreen() / 255f, col.getBlue() / 255f };
	}

	@Override
	public boolean isDefault() {
		return hue == defaultHue && sat == defaultSat && bri == defaultBri;
	}
}

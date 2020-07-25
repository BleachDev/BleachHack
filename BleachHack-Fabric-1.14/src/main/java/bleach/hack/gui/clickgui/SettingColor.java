package bleach.hack.gui.clickgui;

import java.awt.Color;

import org.lwjgl.opengl.GL11;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import bleach.hack.gui.clickgui.modulewindow.ModuleWindow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;

public class SettingColor extends SettingBase {
	
	public String text;
	public float hue;
	public float sat;
	public float bri;
	
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
	}

	public String getName() {
		return text;
	}
	
	public void render(ModuleWindow window, int x, int y, int len) {
		window.fillGreySides(x, y - 1, x + len - 1, y + getHeight(len));
		
		int sx = x + 3,
			sy = y + 2,
			ex = x + len - 18,
			ey = y + getHeight(len) - 2;
		
		window.fillReverseGrey(sx - 1, sy - 1, ex + 1, ey + 1);
		
		DrawableHelper.fill(sx, sy, ex, ey, -1);
		
		// opengl workaround because mc is rarted with <10% transparency
		for (int i = sy; i < ey; i++) {
			float bri = 1f - 1f / ((float) (ey - sy) / (i - sy));
			window.fillGradient(sx, i, ex, i + 1, Color.getHSBColor(hue, 0f, bri).getRGB(), Color.getHSBColor(hue, 1f, bri).getRGB());
		}

		if (window.mouseOver(sx, sy, ex, ey) && window.lmHeld) {
			bri = 1f - 1f / ((float) (ey - sy) / (window.mouseY - sy));
			sat = 1f / ((float) (ex - sx) / (window.mouseX - sx));
		}
		
		int briY = (int) (ey - (ey - sy) * bri);
		int satX = (int) (sx + (ex - sx) * sat);
		
		DrawableHelper.fill(satX - 2, briY, satX, briY + 1, 0xffd0d0d0);
		DrawableHelper.fill(satX + 1, briY, satX + 3, briY + 1, 0xffd0d0d0);
		DrawableHelper.fill(satX, briY - 2, satX + 1, briY, 0xffd0d0d0);
		DrawableHelper.fill(satX, briY + 1, satX + 1, briY + 3, 0xffd0d0d0);
		
		GL11.glPushMatrix();
		GL11.glScaled(0.75, 0.75, 1);
		MinecraftClient.getInstance().textRenderer.draw(text, (int) ((sx + 1) * 1/0.75), (int) ((sy + 1) * 1/0.75), 0x000000);
		GL11.glPopMatrix();
		
		sx = ex + 5;
		ex = ex + 12;
		window.fillReverseGrey(sx - 1, sy - 1, ex + 1, ey + 1);
		
		for (int i = sy; i < ey; i++) {
			float curHue = 1f / ((float) (ey - sy) / (i - sy));
			DrawableHelper.fill(sx, i, ex, i + 1, Color.getHSBColor(curHue, 1f, 1f).getRGB());
		}
		
		if (window.mouseOver(sx, sy, ex, ey) && window.lmHeld) {
			hue = 1f / ((float) (ey - sy) / (window.mouseY - sy));
		}
		
		int hueY = (int) (sy + (ey - sy) * hue);
		DrawableHelper.fill(sx, hueY - 1, sx + 1, hueY + 2, 0xffa0a0a0);
		DrawableHelper.fill(ex - 1, hueY - 1, ex, hueY + 2, 0xffa0a0a0);
		DrawableHelper.fill(sx, hueY, sx + 2, hueY + 1, 0xffa0a0a0);
		DrawableHelper.fill(ex - 2, hueY, ex, hueY + 1, 0xffa0a0a0);
	}

	public int getHeight(int len) {
		return len - len / 4;
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
}

package bleach.hack.gui.clickgui;

import java.awt.Color;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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

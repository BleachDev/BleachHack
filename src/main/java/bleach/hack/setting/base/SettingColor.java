package bleach.hack.setting.base;

import bleach.hack.gui.clickgui.modulewindow.ModuleWindow;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;

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
                ey = y + getHeight(len) - 3;

        window.fillReverseGrey(matrix, sx - 1, sy - 1, ex + 1, ey + 1);

        DrawableHelper.fill(matrix, sx, sy, ex, ey, -1);
        Color satColor = Color.getHSBColor(1f - hue, 1f, 1f);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glShadeModel(7425);
        Tessellator tessellator_1 = Tessellator.getInstance();
        BufferBuilder bufferBuilder_1 = Tessellator.getInstance().getBuffer();
        bufferBuilder_1.begin(7, VertexFormats.POSITION_COLOR);
        bufferBuilder_1.vertex(ex, sy, 0).color(satColor.getRed(), satColor.getBlue(), satColor.getGreen(), 255).next();
        bufferBuilder_1.vertex(sx, sy, 0).color(satColor.getRed(), satColor.getBlue(), satColor.getGreen(), 0).next();
        bufferBuilder_1.vertex(sx, ey, 0).color(satColor.getRed(), satColor.getBlue(), satColor.getGreen(), 0).next();
        bufferBuilder_1.vertex(ex, ey, 0).color(satColor.getRed(), satColor.getBlue(), satColor.getGreen(), 255).next();
        tessellator_1.draw();

        bufferBuilder_1.begin(7, VertexFormats.POSITION_COLOR);
        bufferBuilder_1.vertex(ex, sy, 0).color(0, 0, 0, 0).next();
        bufferBuilder_1.vertex(sx, sy, 0).color(0, 0, 0, 0).next();
        bufferBuilder_1.vertex(sx, ey, 0).color(0, 0, 0, 255).next();
        bufferBuilder_1.vertex(ex, ey, 0).color(0, 0, 0, 255).next();
        tessellator_1.draw();
        GL11.glShadeModel(7424);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

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

        GL11.glPushMatrix();
        GL11.glScaled(0.75, 0.75, 1);
        MinecraftClient.getInstance().textRenderer.draw(matrix, text, (int) ((sx + 1) * 1 / 0.75), (int) ((sy + 1) * 1 / 0.75), 0x000000);
        GL11.glPopMatrix();

        sx = ex + 5;
        ex = ex + 12;
        window.fillReverseGrey(matrix, sx - 1, sy - 1, ex + 1, ey + 1);

        for (int i = sy; i < ey; i++) {
            float curHue = 1f / ((float) (ey - sy) / (i - sy));
            DrawableHelper.fill(matrix, sx, i, ex, i + 1, Color.getHSBColor(curHue, 1f, 1f).getRGB());
        }

        if (window.mouseOver(sx, sy, ex, ey) && window.lmHeld) {
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
        return new float[]{col.getRed() / 255f, col.getGreen() / 255f, col.getBlue() / 255f};
    }

    @Override
    public boolean isDefault() {
        return hue == defaultHue && sat == defaultSat && bri == defaultBri;
    }
}

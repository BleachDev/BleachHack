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
package bleach.hack.setting.base;

import bleach.hack.gui.clickgui.modulewindow.ModuleWindow;
import bleach.hack.utils.ColorUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SettingSlider extends SettingBase {

    public double min;
    public double max;
    private double value;
    public int decimals;
    public String text;

    protected double defaultValue;

    public SettingSlider(String text, double min, double max, double value, int decimals) {
        this.min = min;
        this.max = max;
        this.value = value;
        this.decimals = decimals;
        this.text = text;

        defaultValue = value;
    }

    public double getValue() {
        return round(value, decimals);
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double round(double value, int places) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public String getName() {
        return text;
    }

    public void render(ModuleWindow window, MatrixStack matrix, int x, int y, int len) {
        int pixels = (int) Math.round(MathHelper.clamp((len - 2) * ((getValue() - min) / (max - min)), 0, len - 2));
        //TODO make it fill the slider w/ static selected color instead of pasting it twice into a gradient LMFAO also implement rainbow
        window.fillGradient(matrix, x + 1, y, x + pixels, y + 12, ColorUtils.guiColour(), ColorUtils.guiColour());

        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrix,
                text + ": " + (decimals == 0 && getValue() > 100 ? Integer.toString((int) getValue()) : getValue()),
                x + 2, y + 2, window.mouseOver(x, y, x + len, y + 12) ? 0xcfc3cf : 0xcfe0cf);

        if (window.mouseOver(x + 1, y, x + len - 2, y + 12)) {
            if (window.lmHeld) {
                int percent = ((window.mouseX - x) * 100) / (len - 2);

                setValue(round(percent * ((max - min) / 100) + min, decimals));
            }

            if (window.mwScroll != 0) {
                double units = 1 / (Math.pow(10, decimals));

                setValue(MathHelper.clamp(getValue() + units * window.mwScroll, min, max));
            }
        }
    }

    public SettingSlider withDesc(String desc) {
        description = desc;
        return this;
    }

    public int getHeight(int len) {
        return 12;
    }

    public void readSettings(JsonElement settings) {
        if (settings.isJsonPrimitive()) {
            setValue(settings.getAsDouble());
        }
    }

    public JsonElement saveSettings() {
        return new JsonPrimitive(getValue());
    }

    @Override
    public boolean isDefault() {
        BigDecimal bd = new BigDecimal(defaultValue);
        bd = bd.setScale(decimals, RoundingMode.HALF_UP);

        return bd.doubleValue() == getValue();
    }
}
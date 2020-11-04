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
package bleach.hack.gui.clickgui.modulewindow;

import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.setting.base.SettingBase;
import bleach.hack.utils.ColorUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class ModuleWindow extends ClickGuiWindow {

    public List<Module> modList = new ArrayList<>();
    public LinkedHashMap<Module, Boolean> mods = new LinkedHashMap<>();

    public boolean hiding;

    private int len;

    private Set<Module> searchedModules;

    private Triple<Integer, Integer, String> tooltip = null;

    public ModuleWindow(List<Module> mods, int x1, int y1, int len, String title, ItemStack icon) {
        super(x1, y1, x1 + len, 0, title, icon);

        this.len = len;
        modList = mods;

        for (Module m : mods)
            this.mods.put(m, false);
        y2 = getHeight();
    }

    public void render(MatrixStack matrix, int mX, int mY) {
        super.render(matrix, mX, mY);

        TextRenderer textRend = mc.textRenderer;

        tooltip = null;
        int x = x1 + 1;
        int y = y1 + 13;
        x2 = x + len + 1;

        if (rmDown && mouseOver(x, y - 12, x + len, y)) {
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            hiding = !hiding;
        }

        if (hiding) {
            y2 = y;
            return;
        } else {
            y2 = y + getHeight();
        }

        int curY = 0;
        for (Entry<Module, Boolean> m : new LinkedHashMap<>(mods).entrySet()) {
            if (m.getValue()) {
                //fillReverseGrey(x, y + curY, x+len-1, y + 12 + curY);
                fillGreySides(matrix, x, y + curY, x + len - 1, y + 12 + curY);
                //DrawableHelper.fill(matrix, x, y + curY, x + len - 2, y + curY + 1, 0x90000000);
                //DrawableHelper.fill(matrix, x + len - 3, y + curY + 1, x + len - 2, y + curY + 12, 0x90b0b0b0);
            }
            DrawableHelper.fill(matrix, x, y + curY, x + len + 1, y + 13 + curY,
                    mouseOver(x, y + curY, x + len, y + 12 + curY) ? 0x70303070 : 0x00000000);

            if(ModuleManager.getModule(ClickGui.class).getSetting(6).asToggle().state) {
                DrawableHelper.fill(matrix, x + 1, y + curY, x + len, y + 12 + curY,
                        m.getKey().isToggled() ? ColorUtils.guiColour() : 0x00ff0000);
            }else
                {
                    DrawableHelper.fill(matrix, x, y + curY, x + len + 1, y + 12 + curY,
                            m.getKey().isToggled() ? ColorUtils.guiColour() : 0x00ff0000);
                }

            textRend.drawWithShadow(matrix, textRend.trimToWidth(m.getKey().getName(), len),
                    x + 3, y + 2 + curY, m.getKey().isToggled() ? 0xFFFFFF : 0xc0c0c0);

            //If they match: Module gets marked red
            if (searchedModules != null && searchedModules.contains(m.getKey()) && ModuleManager.getModule(ClickGui.class).getSetting(1).asToggle().state) {
                DrawableHelper.fill(matrix, m.getValue() ? x + 1 : x, y + curY + (m.getValue() ? 1 : 0),
                        m.getValue() ? x + len - 3 : x + len, y + 12 + curY, 0x50ff0000);
            }

            /* Set which module settings show on */
            if (mouseOver(x, y + curY, x + len, y + 12 + curY)) {
                tooltip = Triple.of(x + len + 2, y + curY, m.getKey().getDesc());

                if (lmDown) m.getKey().toggle();
                if (rmDown) mods.replace(m.getKey(), !m.getValue());
                if (lmDown || rmDown)
                    mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }

            curY += 12;

            /* draw settings */
            if (m.getValue()) {
                for (SettingBase s : m.getKey().getSettings()) {
                    s.render(this, matrix, x, y + curY, len);

                    if (!s.getDesc().isEmpty() && mouseOver(x, y + curY, x + len, y + s.getHeight(len) + curY)) {
                        tooltip = s.getGuiDesc(this, x, y + curY, len);
                    }

                    fillGreySides(matrix, x, y + curY - 1, x + len - 1, y + curY + s.getHeight(len));

                    curY += s.getHeight(len);
                }

                drawBindSetting(matrix, m.getKey(), keyDown, x, y + curY, textRend);
                curY += 12;
                //fill(x+len-1, y+(count*12), x+len, y+12+(count*12), 0x9f70fff0);
            }
        }
    }

    public void drawBindSetting(MatrixStack matrix, Module m, int key, int x, int y, TextRenderer textRend) {
        //DrawableHelper.fill(matrix, x, y + 11, x + len - 2, y + 12, 0x90b0b0b0);
        //DrawableHelper.fill(matrix, x + len - 2, y, x + len - 1, y + 12, 0x90b0b0b0);
        //DrawableHelper.fill(matrix, x, y - 1, x + 1, y + 11, 0x90000000);

        if (key >= 0 && mouseOver(x, y, x + len, y + 12))
            m.setKey((key != GLFW.GLFW_KEY_DELETE && key != GLFW.GLFW_KEY_ESCAPE) ? key : Module.KEY_UNBOUND);

        String name = m.getKey() < 0 ? "NONE" : InputUtil.fromKeyCode(m.getKey(), -1).getLocalizedText().getString();
        if (name == null)
            name = "KEY" + m.getKey();
        else if (name.isEmpty())
            name = "NONE";

        textRend.drawWithShadow(matrix, "Bind: " + name + (mouseOver(x, y, x + len, y + 12) ? "..." : ""), x + 2, y + 2,
                mouseOver(x, y, x + len, y + 12) ? 0xcfc3cf : 0xcfe0cf);
    }

    public void fillReverseGrey(MatrixStack matrix, int x1, int y1, int x2, int y2) {
        DrawableHelper.fill(matrix, x1, y1, x1 + 1, y2 - 1, 0x90000000);
        DrawableHelper.fill(matrix, x1 + 1, y1, x2 - 1, y1 + 1, 0x90000000);
        DrawableHelper.fill(matrix, x1 + 1, y2 - 1, x2, y2, 0x90b0b0b0);
        DrawableHelper.fill(matrix, x2 - 1, y1 + 1, x2, y2 - 1, 0x90b0b0b0);
        DrawableHelper.fill(matrix, x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xff505059);
    }

    private void fillGreySides(MatrixStack matrix, int x1, int y1, int x2, int y2) {
        //DrawableHelper.fill(matrix, x1, y1, x1 + 1, y2 - 1, 0x90000000);
        //DrawableHelper.fill(matrix, x2 - 1, y1 + 1, x2, y2, 0x90b0b0b0);
    }

    protected void drawBar(MatrixStack matrix, int mX, int mY, TextRenderer textRend) {
        super.drawBar(matrix, mX, mY, textRend);
        textRend.draw(matrix, hiding ? "+" : "_", x2 - 11, y1 + (hiding ? 4 : 0), ColorUtils.textColor());
    }

    public Triple<Integer, Integer, String> getTooltip() {
        return tooltip;
    }

    public void setSearchedModule(Set<Module> mods) {
        searchedModules = mods;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public int getHeight() {
        int h = 1;
        for (Entry<Module, Boolean> e : mods.entrySet()) {
            h += 12;

            if (e.getValue()) {
                for (SettingBase s : e.getKey().getSettings()) {
                    h += s.getHeight(len);
                }

                h += 12;
            }
        }

        return h;
    }
}

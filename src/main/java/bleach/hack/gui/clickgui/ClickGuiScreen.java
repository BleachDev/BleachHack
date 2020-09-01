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
package bleach.hack.gui.clickgui;

import bleach.hack.BleachHack;
import bleach.hack.command.Command;
import bleach.hack.gui.clickgui.modulewindow.ClickGuiWindow;
import bleach.hack.gui.clickgui.modulewindow.ModuleWindow;
import bleach.hack.gui.window.AbstractWindowScreen;
import bleach.hack.gui.window.Window;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.utils.ColourThingy;
import bleach.hack.utils.file.BleachFileHelper;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClickGuiScreen extends AbstractWindowScreen {

    private int keyDown = -1;
    private boolean lmDown = false;
    private boolean rmDown = false;
    private boolean lmHeld = false;
    private int mwScroll = 0;

    private TextFieldWidget searchField;

    public ClickGuiScreen() {
        super(new LiteralText("ClickGui"));
    }

    public void init() {
        searchField = new TextFieldWidget(textRenderer, 2, 14, 100, 12, LiteralText.EMPTY /* @LasnikProgram is author lol*/);
        searchField.visible = false;
        searchField.setMaxLength(20);
        searchField.setSuggestion("Search here");
        addButton(searchField);
    }

    public void initWindows() {
        int len = (int) ModuleManager.getModule(ClickGui.class).getSetting(0).asSlider().getValue();

        int i = 10;
        for (Category c : Category.values()) {
            windows.add(new ModuleWindow(ModuleManager.getModulesInCat(c), i, 35, len,
                    StringUtils.capitalize(StringUtils.lowerCase(c.toString())), new ItemStack(Items.AIR)));

            i += len + 5;
        }
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        ModuleManager.getModule(ClickGui.class).setToggled(false);
        client.openScreen(null);
    }

    public void render(MatrixStack matrix, int mX, int mY, float float_1) {
        BleachFileHelper.SCHEDULE_SAVE_CLICKGUI = true;

        searchField.visible = ModuleManager.getModule(ClickGui.class).getSetting(1).asToggle().state;

        this.renderBackground(matrix);
        textRenderer.drawWithShadow(matrix, "BleachHack epearl edition " + BleachHack.VERSION, 1, 1, ColourThingy.guiColour());
        if (ModuleManager.getModule(ClickGui.class).getSetting(2).asToggle().state) {
            textRenderer.drawWithShadow(matrix,
                    "Current prefix is: \"" + Command.PREFIX + "\" (" + Command.PREFIX + "help)", 2, height - 20, ColourThingy.guiColour());
            textRenderer.drawWithShadow(matrix, "Use " + Command.PREFIX + "guireset to reset the gui", 2, height - 10,
                    ColourThingy.guiColour());

        }
        if (ModuleManager.getModule(ClickGui.class).getSetting(1).asToggle().state) {
            searchField.setSuggestion(searchField.getText().isEmpty() ? "Search here" : "");

            Set<Module> seachMods = new HashSet<>();
            if (!searchField.getText().isEmpty()) {
                for (Module m : ModuleManager.getModules()) {
                    if (m.getName().toLowerCase().contains(searchField.getText().toLowerCase().replace(" ", ""))) {
                        seachMods.add(m);
                    }
                }
            }

            for (Window w : windows) {
                if (w instanceof ModuleWindow) {
                    ((ModuleWindow) w).setSearchedModule(seachMods);
                }
            }
        }

        int len = (int) ModuleManager.getModule(ClickGui.class).getSetting(0).asSlider().getValue();
        for (Window w : windows) {
            if (w instanceof ClickGuiWindow) {
                if (w instanceof ModuleWindow) {
                    ((ModuleWindow) w).setLen(len);
                }

                ((ClickGuiWindow) w).updateKeys(mX, mY, keyDown, lmDown, rmDown, lmHeld, mwScroll);
            }
        }

        super.render(matrix, mX, mY, float_1);

        for (Window w : windows) {
            if (!ModuleManager.getModule(ClickGui.class).getSetting(3).asToggle().state) {
                if (w instanceof ClickGuiWindow) {
                    Triple<Integer, Integer, String> tooltip = ((ClickGuiWindow) w).getTooltip();
                    if (tooltip != null) {
                        int tooltipY = tooltip.getMiddle();

                        String[] split = tooltip.getRight().split("\n", -1 /* Adding -1 makes it keep empty splits */);
                        ArrayUtils.reverse(split);
                        for (String s: split) {
                            /* Match lines to end of words after it reaches 22 characters long */
                            Matcher mat = Pattern.compile(".{1,22}\\b\\W*").matcher(s);

                            List<String> lines = new ArrayList<>();

                            while (mat.find())
                                lines.add(mat.group().trim());

                            if (lines.isEmpty())
                                lines.add(s);

                            int start = tooltipY - lines.size() * 10;
                            for (int l = 0; l < lines.size(); l++) {
                                textRenderer.drawWithShadow(matrix, lines.get(l), tooltip.getLeft() + 2, start + (l * 10), ColourThingy.guiColour());
                            }

                            tooltipY -= lines.size() * 10;
                        }
                    }
                }
            } else if (ModuleManager.getModule(ClickGui.class).getSetting(3).asToggle().state && !ModuleManager.getModule(ClickGui.class).getSetting(2).asToggle().state) {
                if (w instanceof ClickGuiWindow) {
                    Triple<Integer, Integer, String> tooltip = ((ClickGuiWindow) w).getTooltip();
                    if (tooltip != null) {
                        textRenderer.drawWithShadow(matrix, tooltip.getRight(), 2, height - 11, ColourThingy.guiColour());
                    }
                }
            } else if (ModuleManager.getModule(ClickGui.class).getSetting(3).asToggle().state && ModuleManager.getModule(ClickGui.class).getSetting(2).asToggle().state) {
                if (w instanceof ClickGuiWindow) {
                    Triple<Integer, Integer, String> tooltip = ((ClickGuiWindow) w).getTooltip();
                    if (tooltip != null) {
                        textRenderer.drawWithShadow(matrix, tooltip.getRight(), 2, height - 30, ColourThingy.guiColour());
                    }
                }
            }
        }

        lmDown = false;
        rmDown = false;
        keyDown = -1;
        mwScroll = 0;
    }

    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (int_1 == 0) {
            lmDown = true;
            lmHeld = true;
        } else if (int_1 == 1)
            rmDown = true;

        // Fix having to double click windows to move them
        for (Window w : windows) {
            if (double_1 > w.x1 && double_1 < w.x2 && double_2 > w.y1 && double_2 < w.y2 && !w.closed) {
                w.onMousePressed((int) double_1, (int) double_2);
                break;
            }
        }

        return super.mouseClicked(double_1, double_2, int_1);
    }

    public boolean mouseReleased(double double_1, double double_2, int int_1) {
        if (int_1 == 0)
            lmHeld = false;
        return super.mouseReleased(double_1, double_2, int_1);
    }

    public boolean keyPressed(int int_1, int int_2, int int_3) {
        keyDown = int_1;
        return super.keyPressed(int_1, int_2, int_3);
    }
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        mwScroll = (int) double_3;
        return super.mouseScrolled(double_1, double_2, double_3);
    }
    public void resetGui() {
        int x = 30;
        for (Window m : windows) {
            m.x1 = x;
            m.y2 = 35;
            x += (int) ModuleManager.getModule(ClickGui.class).getSetting(0).asSlider().getValue() + 5;
        }
    }
}

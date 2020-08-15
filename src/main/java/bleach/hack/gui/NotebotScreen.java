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
package bleach.hack.gui;

import bleach.hack.gui.window.AbstractWindowScreen;
import bleach.hack.gui.window.Window;
import bleach.hack.module.mods.Notebot;
import bleach.hack.utils.NotebotUtils;
import bleach.hack.utils.file.BleachFileMang;
import net.minecraft.block.enums.Instrument;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NotebotScreen extends AbstractWindowScreen {

    public List<String> files;
    public NotebotEntry entry;
    public String selected = "";
    public int page = 0;

    public NotebotScreen() {
        super(new LiteralText("Notebot Gui"));
    }

    public void init() {
        files = new ArrayList<>();

        try {
            Stream<Path> paths = Files.walk(BleachFileMang.getDir().resolve("notebot"));
            paths.forEach(p -> files.add(p.getFileName().toString()));
            paths.close();
            files.remove(0);
        } catch (IOException e) {
        }

        windows.clear();
        windows.add(new Window(width / 4, height / 4 - 10, width / 4 + width / 2, height / 4 + height / 2, "Notebot Gui", new ItemStack(Items.NOTE_BLOCK)));
    }

    public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
        renderBackground(matrix);
        super.render(matrix, mouseX, mouseY, delta);
    }

    public void onRenderWindow(MatrixStack matrix, int window, int mX, int mY) {
        super.onRenderWindow(matrix, window, mX, mY);

        if (window == 0) {
            int x = windows.get(0).x1,
                    y = windows.get(0).y1 + 10,
                    w = width / 2,
                    h = height / 2;

            drawCenteredString(matrix, textRenderer, "Tutorial..", x + w - 24, y + 4, 0x9090c0);

            int pageEntries = 0;
            for (int i = y + 20; i < y + h - 27; i += 10) pageEntries++;

            drawCenteredString(matrix, textRenderer, "<  Page " + (page + 1) + "  >", x + 55, y + 5, 0xc0c0ff);

            fillButton(matrix, x + 10, y + h - 13, x + 99, y + h - 3, 0xff3a3a3a, 0xff353535, mX, mY);
            drawCenteredString(matrix, textRenderer, "Download Songs..", x + 55, y + h - 12, 0xc0dfdf);

            int c = 0, c1 = -1;
            for (String s : files) {
                c1++;
                if (c1 < page * pageEntries) continue;
                if (c1 > (page + 1) * pageEntries) break;

                fillButton(matrix, x + 5, y + 15 + c * 10, x + 105, y + 25 + c * 10,
                        Notebot.filePath.equals(s) ? 0xf0408040 : selected.equals(s) ? 0xf0202020 : 0xf0404040, 0xf0303030, mX, mY);
                if (cutText(s, 105).equals(s)) drawCenteredString(matrix, textRenderer, s, x + 55, y + 16 + c * 10, -1);
                else drawStringWithShadow(matrix, textRenderer, cutText(s, 105), x + 5, y + 16 + c * 10, -1);
                c++;
            }

            if (entry != null) {
                drawCenteredString(matrix, textRenderer, entry.fileName, x + w - w / 4, y + 10, 0xa030a0);
                drawCenteredString(matrix, textRenderer, entry.length / 20 + "s", x + w - w / 4, y + 20, 0xc000c0);
                drawCenteredString(matrix, textRenderer, "Notes: ", x + w - w / 4, y + 38, 0x80f080);

                int c2 = 0;
                for (Entry<Instrument, Integer> e : entry.notes.entrySet()) {
                    itemRenderer.zOffset = 500 - c2 * 20;
                    drawCenteredString(matrix, textRenderer, StringUtils.capitalize(e.getKey().asString()) + " x" + e.getValue(),
                            x + w - w / 4, y + 50 + c2 * 10, 0x50f050);
                    GL11.glPushMatrix();
                    DiffuseLighting.enableGuiDepthLighting();
                    if (e.getKey() == Instrument.HARP)
                        itemRenderer.renderGuiItemIcon(new ItemStack(Items.DIRT), x + w - w / 4 + 40, y + 46 + c2 * 10);
                    if (e.getKey() == Instrument.BASEDRUM)
                        itemRenderer.renderGuiItemIcon(new ItemStack(Items.STONE), x + w - w / 4 + 40, y + 46 + c2 * 10);
                    if (e.getKey() == Instrument.SNARE)
                        itemRenderer.renderGuiItemIcon(new ItemStack(Items.SAND), x + w - w / 4 + 40, y + 46 + c2 * 10);
                    if (e.getKey() == Instrument.HAT)
                        itemRenderer.renderGuiItemIcon(new ItemStack(Items.GLASS), x + w - w / 4 + 40, y + 46 + c2 * 10);
                    if (e.getKey() == Instrument.BASS)
                        itemRenderer.renderGuiItemIcon(new ItemStack(Items.OAK_WOOD), x + w - w / 4 + 40, y + 46 + c2 * 10);
                    if (e.getKey() == Instrument.FLUTE)
                        itemRenderer.renderGuiItemIcon(new ItemStack(Items.CLAY), x + w - w / 4 + 40, y + 46 + c2 * 10);
                    if (e.getKey() == Instrument.BELL)
                        itemRenderer.renderGuiItemIcon(new ItemStack(Items.GOLD_BLOCK), x + w - w / 4 + 40, y + 46 + c2 * 10);
                    if (e.getKey() == Instrument.GUITAR)
                        itemRenderer.renderGuiItemIcon(new ItemStack(Items.WHITE_WOOL), x + w - w / 4 + 40, y + 46 + c2 * 10);
                    if (e.getKey() == Instrument.CHIME)
                        itemRenderer.renderGuiItemIcon(new ItemStack(Items.PACKED_ICE), x + w - w / 4 + 40, y + 46 + c2 * 10);
                    if (e.getKey() == Instrument.XYLOPHONE)
                        itemRenderer.renderGuiItemIcon(new ItemStack(Items.BONE_BLOCK), x + w - w / 4 + 40, y + 46 + c2 * 10);
                    if (e.getKey() == Instrument.IRON_XYLOPHONE)
                        itemRenderer.renderGuiItemIcon(new ItemStack(Items.IRON_BLOCK), x + w - w / 4 + 40, y + 46 + c2 * 10);
                    if (e.getKey() == Instrument.COW_BELL)
                        itemRenderer.renderGuiItemIcon(new ItemStack(Items.SOUL_SAND), x + w - w / 4 + 40, y + 46 + c2 * 10);
                    if (e.getKey() == Instrument.DIDGERIDOO)
                        itemRenderer.renderGuiItemIcon(new ItemStack(Items.PUMPKIN), x + w - w / 4 + 40, y + 46 + c2 * 10);
                    if (e.getKey() == Instrument.BIT)
                        itemRenderer.renderGuiItemIcon(new ItemStack(Items.EMERALD_BLOCK), x + w - w / 4 + 40, y + 46 + c2 * 10);
                    if (e.getKey() == Instrument.BANJO)
                        itemRenderer.renderGuiItemIcon(new ItemStack(Items.HAY_BLOCK), x + w - w / 4 + 40, y + 46 + c2 * 10);
                    if (e.getKey() == Instrument.PLING)
                        itemRenderer.renderGuiItemIcon(new ItemStack(Items.GLOWSTONE), x + w - w / 4 + 40, y + 46 + c2 * 10);
                    c2++;
                    GL11.glPopMatrix();
                }

                fillButton(matrix, x + w - w / 2 + 10, y + h - 15, x + w - w / 4, y + h - 5, 0xff903030, 0xff802020, mX, mY);
                fillButton(matrix, x + w - w / 4 + 5, y + h - 15, x + w - 5, y + h - 5, 0xff308030, 0xff207020, mX, mY);
                fillButton(matrix, x + w - w / 4 - w / 8, y + h - 27, x + w - w / 4 + w / 8, y + h - 17, 0xff303080, 0xff202070, mX, mY);

                int pixels = (int) Math.round(MathHelper.clamp((w / 4) * ((double) entry.playTick / (double) entry.length), 0, w / 4));
                fill(matrix, x + w - w / 4 - w / 8, y + h - 27, (x + w - w / 4 - w / 8) + pixels, y + h - 17, 0x507050ff);

                drawCenteredString(matrix, textRenderer, "Delete", (int) (x + w - w / 2.8), y + h - 14, 0xff0000);
                drawCenteredString(matrix, textRenderer, "Select", x + w - w / 8, y + h - 14, 0x00ff00);
                drawCenteredString(matrix, textRenderer, (entry.playing ? "Playing" : "Play") + " (scuffed)", x + w - w / 4, y + h - 26, 0x6060ff);
            }
        }
    }

    public void tick() {
        if (entry != null) {
            if (entry.playing) {
                entry.playTick++;
                NotebotUtils.playNote(entry.lines, entry.playTick);
            }
        }
    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (!windows.get(0).closed) {
            int x = windows.get(0).x1,
                    y = windows.get(0).y1 + 10,
                    w = width / 2,
                    h = height / 2;

            if (double_1 > x + 20 && double_1 < x + 35 && double_2 > y + 5 && double_2 < y + 15) if (page > 0) page--;
            if (double_1 > x + 77 && double_1 < x + 92 && double_2 > y + 5 && double_2 < y + 15) page++;
            if (double_1 > x + w - 44 && double_1 < x + w && double_2 > y + 3 && double_2 < y + 15) {
                try {
                    Util.getOperatingSystem().open(new URI("https://www.youtube.com/watch?v=Z6O80jItoAk"));
                } catch (Exception e) {
                }
            }
            if (double_1 > x + 10 && double_1 < x + 99 && double_2 > y + h - 13 && double_2 < y + h - 3) {
                NotebotUtils.downloadSongs(true);
            }

            if (entry != null) {
                /* Pfft why use buttons when you can use meaningless rectangles with messy code */
                if (double_1 > x + w - w / 2 + 10 && double_1 < x + w - w / 4 && double_2 > y + h - 15 && double_2 < y + h - 5) {
                    BleachFileMang.deleteFile("notebot", entry.fileName);
                    client.openScreen(this);
                }
                if (double_1 > x + w - w / 4 + 5 && double_1 < x + w - 5 && double_2 > y + h - 15 && double_2 < y + h - 5) {
                    Notebot.filePath = entry.fileName;
                }
                if (double_1 > x + w - w / 4 - w / 8 && double_1 < x + w - w / 4 + w / 8 && double_2 > y + h - 27 && double_2 < y + h - 17) {
                    entry.playing = !entry.playing;
                }
            }

            int pageEntries = 0;
            for (int i = y + 20; i < y + h - 27; i += 10) pageEntries++;

            int c = 0;
            int c1 = -1;
            for (String s : files) {
                c1++;
                if (c1 < page * pageEntries) continue;
                if (double_1 > x + 5 && double_1 < x + 105 && double_2 > y + 15 + c * 10 && double_2 < y + 25 + c * 10) {
                    entry = new NotebotEntry(s);
                    selected = s;
                }
                c++;
            }
        }

        return super.mouseClicked(double_1, double_2, int_1);
    }

    private void fillButton(MatrixStack matrix, int x1, int y1, int x2, int y2, int color, int colorHover, int mX, int mY) {
        fill(matrix, x1, y1, x2, y2, (mX > x1 && mX < x2 && mY > y1 && mY < y2 ? colorHover : color));
    }

    private String cutText(String text, int leng) {
        String text1 = text;
        for (int i = 0; i < text.length(); i++) {
            if (textRenderer.getWidth(text1) < leng) return text1;
            text1 = text1.replaceAll(".$", "");
        }
        return "";
    }

    public class NotebotEntry {
        public String fileName;
        public List<String> lines = new ArrayList<>();
        public HashMap<Instrument, Integer> notes = new HashMap<>();
        public int length;

        public boolean playing = false;
        public int playTick = 0;

        public NotebotEntry(String file) {
            /* File and lines */
            fileName = file;
            lines = BleachFileMang.readFileLines("notebot", file)
                    .stream().filter(s -> !(s.isEmpty() || s.startsWith("//") || s.startsWith(";"))).collect(Collectors.toList());

            /* Get length */
            int maxLeng = 0;
            for (String s : lines) {
                try {
                    if (Integer.parseInt(s.split(":")[0]) > maxLeng) maxLeng = Integer.parseInt(s.split(":")[0]);
                } catch (Exception e) {
                }
            }
            length = maxLeng;

            /* Requirements */
            List<List<Integer>> t = new ArrayList<>();

            for (String s : lines) {
                try {
                    List<String> strings = Arrays.asList(s.split(":"));
                    if (!t.contains(Arrays.asList(Integer.parseInt(strings.get(1)), Integer.parseInt(strings.get(2))))) {
                        t.add(Arrays.asList(Integer.parseInt(strings.get(1)), Integer.parseInt(strings.get(2))));
                    }
                } catch (Exception e) {
                }
            }

            List<Integer> t1 = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            for (List<Integer> i : t) t1.set(i.get(1), t1.get(i.get(1)) + 1);
            for (int i = 0; i < t1.size(); i++) {
                if (t1.get(i) != 0) notes.put(Instrument.values()[i], t1.get(i));
            }
        }
    }
}

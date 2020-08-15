package bleach.hack.module.mods;

import bleach.hack.event.events.EventDrawTooltip;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.FabricReflect;
import bleach.hack.utils.ItemContentUtils;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.*;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.Arrays;
import java.util.List;

public class Peek extends Module {

    private List<List<String>> pages;
    private int[] slotPos;
    private int pageCount = 0;
    private boolean shown = false;

    public Peek() {
        super("Peek", KEY_UNBOUND, Category.MISC, "Shows whats inside containers",
                new SettingToggle("Containers", true).withDesc("Shows a tooltip for containers").withChildren(
                        new SettingMode("Info", "None", "Name", "All").withDesc("How to show the old tooltip")),
                new SettingToggle("Books", true).withDesc("Show tooltips for books"),
                new SettingToggle("Maps", true).withDesc("Show tooltips for maps").withChildren(
                        new SettingSlider("Map Size", 0.25, 1.5, 0.5, 2).withDesc("How big to make the map")));
    }

    @Subscribe
    public void drawScreen(EventDrawTooltip event) {
        if (!(event.screen instanceof HandledScreen)) {
            return;
        }

        Slot slot = (Slot) FabricReflect.getFieldValue(event.screen, "field_2787", "focusedSlot");
        if (slot == null) return;

        if (!Arrays.equals(new int[]{slot.x, slot.y}, slotPos)) {
            pageCount = 0;
            pages = null;
        }

        slotPos = new int[]{slot.x, slot.y};

        event.matrix.push();
        event.matrix.translate(0, 0, 300);

        if (getSetting(0).asToggle().state) drawShulkerToolTip(event, slot, event.mX, event.mY);
        if (getSetting(1).asToggle().state) drawBookToolTip(event.matrix, slot, event.mX, event.mY);
        if (getSetting(2).asToggle().state) drawMapToolTip(event.matrix, slot, event.mX, event.mY);

        event.matrix.pop();
    }

    public void drawShulkerToolTip(EventDrawTooltip event, Slot slot, int mX, int mY) {
        if (!(slot.getStack().getItem() instanceof BlockItem)) return;
        if (!(((BlockItem) slot.getStack().getItem()).getBlock() instanceof ShulkerBoxBlock)
                && !(((BlockItem) slot.getStack().getItem()).getBlock() instanceof ChestBlock)
                && !(((BlockItem) slot.getStack().getItem()).getBlock() instanceof BarrelBlock)
                && !(((BlockItem) slot.getStack().getItem()).getBlock() instanceof DispenserBlock)
                && !(((BlockItem) slot.getStack().getItem()).getBlock() instanceof HopperBlock)
                && !(((BlockItem) slot.getStack().getItem()).getBlock() instanceof AbstractFurnaceBlock)) return;

        List<ItemStack> items = ItemContentUtils.getItemsInContainer(slot.getStack());

        boolean empty = true;
        for (ItemStack i : items) {
            if (i.getItem() != Items.AIR) {
                empty = false;
                break;
            }
        }

        if (empty) return;

        Block block = ((BlockItem) slot.getStack().getItem()).getBlock();

        if (getSetting(0).asToggle().getChild(0).asMode().mode == 0) {
            event.setCancelled(true);
        } else if (getSetting(0).asToggle().getChild(0).asMode().mode == 1) {
            event.text = Lists.transform(Arrays.asList(slot.getStack().getName()), Text::asOrderedText);
        }

        int realY = getSetting(0).asToggle().getChild(0).asMode().mode == 0 ? mY + 24 : mY;

        int count = block instanceof HopperBlock || block instanceof DispenserBlock || block instanceof AbstractFurnaceBlock ? 18 : 0;

        if (block instanceof AbstractFurnaceBlock) {
            renderTooltipBox(event.matrix, mX, realY - 21, 13, 47, true);
        } else if (block instanceof HopperBlock) {
            renderTooltipBox(event.matrix, mX, realY - 21, 13, 82, true);
        } else if (block instanceof DispenserBlock) {
            renderTooltipBox(event.matrix, mX, realY - 21, 13, 150, true);
        } else {
            renderTooltipBox(event.matrix, mX, realY - 55, 47, 150, true);
        }

        for (ItemStack i : items) {
            if (count > 26) break;
            int x = mX + 10 + (17 * (count % 9));
            int y = realY - 69 + (17 * (count / 9));

            mc.getItemRenderer().zOffset = 400;
            mc.getItemRenderer().renderGuiItemIcon(i, x, y);
            mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, i, x, y, i.getCount() > 1 ? i.getCount() + "" : "");
            mc.getItemRenderer().zOffset = 300;
            count++;
        }
    }

    public void drawBookToolTip(MatrixStack matrix, Slot slot, int mX, int mY) {
        if (slot.getStack().getItem() != Items.WRITABLE_BOOK && slot.getStack().getItem() != Items.WRITTEN_BOOK) return;

        if (pages == null) pages = ItemContentUtils.getTextInBook(slot.getStack());
        if (pages.isEmpty()) return;

        /* Cycle through pages */
        if (mc.player.age % 80 == 0 && !shown) {
            shown = true;
            if (pageCount == pages.size() - 1) pageCount = 0;
            else pageCount++;
        } else if (mc.player.age % 80 != 0) shown = false;

        int length = mc.textRenderer.getWidth("Page: " + (pageCount + 1) + "/" + pages.size());

        renderTooltipBox(matrix, mX + 56 - length / 2, mY - pages.get(pageCount).size() * 10 - 19, 5, length, true);
        renderTooltipBox(matrix, mX, mY - pages.get(pageCount).size() * 10 - 6, pages.get(pageCount).size() * 10 - 2, 120, true);
        mc.textRenderer.drawWithShadow(matrix, "Page: " + (pageCount + 1) + "/" + pages.size(),
                mX + 68 - length / 2, mY - pages.get(pageCount).size() * 10 - 32, -1);

        int count = 0;
        for (String s : pages.get(pageCount)) {
            mc.textRenderer.drawWithShadow(matrix, s, mX + 12, mY - 18 - pages.get(pageCount).size() * 10 + count * 10, 0x00c0c0);
            count++;
        }

    }

    public void drawMapToolTip(MatrixStack matrix, Slot slot, int mX, int mY) {
        if (slot.getStack().getItem() != Items.FILLED_MAP) return;

        MapState data = FilledMapItem.getMapState(slot.getStack(), mc.world);
        if (data == null || data.colors == null) return;

        byte[] colors = data.colors;

        double size = getSetting(2).asToggle().getChild(0).asSlider().getValue();

        GL11.glPushMatrix();
        GL11.glScaled(size, size, 1.0);
        GL11.glTranslatef(0.0F, 0.0F, 300.0F);
        int x = (int) (mX * (1 / size) + 12 * (1 / size));
        int y = (int) (mY * (1 / size) - 12 * (1 / size) - 140);

        renderTooltipBox(matrix, x - 12, y + 12, 128, 128, false);
        for (byte c : colors) {
            int c1 = c & 255;

            if (c1 / 4 != 0)
                DrawableHelper.fill(matrix, x, y, x + 1, y + 1, getRenderColorFix(MaterialColor.COLORS[c1 / 4].color, c1 & 3));
            if (x - (int) (mX * (1 / size) + 12 * (1 / size)) == 127) {
                x = (int) (mX * (1 / size) + 12 * (1 / size));
                y++;
            } else x++;
        }

        GL11.glPopMatrix();
    }

    /* Fix your game [B]ojang */
    private int getRenderColorFix(int color, int offset) {
        int int_2 = (offset == 3 ? 135 : offset == 2 ? 255 : offset == 0 ? 180 : 220);

        int r = (color >> 16 & 255) * int_2 / 255;
        int g = (color >> 8 & 255) * int_2 / 255;
        int b = (color & 255) * int_2 / 255;
        return -16777216 | r << 16 | g << 8 | b;
    }

    private void renderTooltipBox(MatrixStack matrix, int x1, int y1, int x2, int y2, boolean wrap) {
        int int_5 = x1 + 12;
        int int_6 = y1 - 12;
        if (wrap) {
            if (int_5 + y2 > mc.currentScreen.width) int_5 -= 28 + y2;
            if (int_6 + x2 + 6 > mc.currentScreen.height) int_6 = mc.currentScreen.height - x2 - 6;
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glShadeModel(7425);
        fillGradient(matrix, int_5 - 3, int_6 - 4, int_5 + y2 + 3, int_6 - 3, -267386864, -267386864);
        fillGradient(matrix, int_5 - 3, int_6 + x2 + 3, int_5 + y2 + 3, int_6 + x2 + 4, -267386864, -267386864);
        fillGradient(matrix, int_5 - 3, int_6 - 3, int_5 + y2 + 3, int_6 + x2 + 3, -267386864, -267386864);
        fillGradient(matrix, int_5 - 4, int_6 - 3, int_5 - 3, int_6 + x2 + 3, -267386864, -267386864);
        fillGradient(matrix, int_5 + y2 + 3, int_6 - 3, int_5 + y2 + 4, int_6 + x2 + 3, -267386864, -267386864);
        fillGradient(matrix, int_5 - 3, int_6 - 3 + 1, int_5 - 3 + 1, int_6 + x2 + 3 - 1, 1347420415, 1344798847);
        fillGradient(matrix, int_5 + y2 + 2, int_6 - 3 + 1, int_5 + y2 + 3, int_6 + x2 + 3 - 1, 1347420415, 1344798847);
        fillGradient(matrix, int_5 - 3, int_6 - 3, int_5 + y2 + 3, int_6 - 3 + 1, 1347420415, 1347420415);
        fillGradient(matrix, int_5 - 3, int_6 + x2 + 2, int_5 + y2 + 3, int_6 + x2 + 3, 1344798847, 1344798847);
        GL11.glShadeModel(7424);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void fillGradient(MatrixStack matrix, int x1, int y1, int x2, int y2, int color1, int color2) {
        float float_1 = (color1 >> 24 & 255) / 255.0F;
        float float_2 = (color1 >> 16 & 255) / 255.0F;
        float float_3 = (color1 >> 8 & 255) / 255.0F;
        float float_4 = (color1 & 255) / 255.0F;
        float float_5 = (color2 >> 24 & 255) / 255.0F;
        float float_6 = (color2 >> 16 & 255) / 255.0F;
        float float_7 = (color2 >> 8 & 255) / 255.0F;
        float float_8 = (color2 & 255) / 255.0F;
        Tessellator tessellator_1 = Tessellator.getInstance();
        BufferBuilder bufferBuilder_1 = tessellator_1.getBuffer();
        bufferBuilder_1.begin(7, VertexFormats.POSITION_COLOR);
        bufferBuilder_1.vertex(x1, y1, 0).color(float_2, float_3, float_4, float_1).next();
        bufferBuilder_1.vertex(x1, y2, 0).color(float_2, float_3, float_4, float_1).next();
        bufferBuilder_1.vertex(x2, y2, 0).color(float_6, float_7, float_8, float_5).next();
        bufferBuilder_1.vertex(x2, y1, 0).color(float_6, float_7, float_8, float_5).next();
        tessellator_1.draw();
    }
}

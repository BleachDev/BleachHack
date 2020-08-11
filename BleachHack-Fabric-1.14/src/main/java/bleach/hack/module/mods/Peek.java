package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import bleach.hack.event.events.EventDrawTooltip;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.FabricReflect;
import bleach.hack.utils.ItemContentUtils;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;

import com.google.common.eventbus.Subscribe;

public class Peek extends Module {

	private List<List<String>> pages;
	private int[] slotPos;
	private int pageCount = 0;
	private boolean shown = false;

	public Peek() {
		super("Peek", KEY_UNBOUND, Category.MISC, "Shows whats inside containers",
				new SettingToggle("Containers", true).withDesc("Shows a tooltip for containers").withChildren(
						new SettingMode("Info: ", "All", "Name", "None").withDesc("How to show the old tooltip")),
				new SettingToggle("Books", true).withDesc("Show tooltips for books"),
				new SettingToggle("Maps", true).withDesc("Show tooltips for maps").withChildren(
						new SettingSlider("Map Size: ", 0.25, 1.5, 0.5, 2).withDesc("How big to make the map")));
	}

	@Subscribe
	public void drawScreen(EventDrawTooltip event) {
		if (!(event.screen instanceof AbstractContainerScreen)) {
			return;
		}

		Slot slot = (Slot) FabricReflect.getFieldValue(event.screen, "field_2787", "focusedSlot");
		if (slot == null) return;

		if (!Arrays.equals(new int[] {slot.xPosition, slot.yPosition}, slotPos)) {
			pageCount = 0;
			pages = null;
		}

		slotPos = new int[] {slot.xPosition, slot.yPosition};

		GL11.glPushMatrix();
		GL11.glTranslatef(0f, 0f, 500f);

		if (getSetting(0).asToggle().state) drawShulkerToolTip(event, slot, event.mX, event.mY);
		if (getSetting(2).asToggle().state) drawBookToolTip(slot, event.mX, event.mY);
		if (getSetting(3).asToggle().state) drawMapToolTip(slot, event.mX, event.mY);
		
		GL11.glPopMatrix();
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
		for (ItemStack i: items) {
			if (i.getItem() != Items.AIR) {
				empty = false;
				break;
			}
		}

		if (empty) return;

		Block block = ((BlockItem) slot.getStack().getItem()).getBlock();

		if (getSetting(1).asMode().mode == 2) {
			event.setCancelled(true);
		} else if (getSetting(1).asMode().mode == 1) {
			event.text = Arrays.asList(slot.getStack().getName().asString());
		}

		int realY = getSetting(1).asMode().mode == 2 ? mY + 24 : mY;

		int count = block instanceof HopperBlock || block instanceof DispenserBlock || block instanceof AbstractFurnaceBlock ? 18 : 0;

		if (block instanceof AbstractFurnaceBlock) {
			renderTooltipBox(mX, realY - 21, 13, 47, true);
		} else if (block instanceof HopperBlock) {
			renderTooltipBox(mX, realY - 21, 13, 82, true);
		} else if (block instanceof DispenserBlock) {
			renderTooltipBox(mX, realY - 21, 13, 150, true);
		} else {
			renderTooltipBox(mX, realY - 55, 47, 150, true);
		}

		for (ItemStack i: items) {
			if (count > 26) break;
			int x = mX + 10 + (17 * (count % 9));
			int y = realY - 69 + (17 * (count / 9));

			mc.getItemRenderer().renderGuiItem(i, x, y);
			mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, i, x, y, i.getCount() > 1 ? i.getCount() + "" : "");
			count++;
		}
	}

	public void drawBookToolTip(Slot slot, int mX, int mY) {
		if (slot.getStack().getItem() != Items.WRITABLE_BOOK && slot.getStack().getItem() != Items.WRITTEN_BOOK) return;

		if (pages == null) pages = ItemContentUtils.getTextInBook(slot.getStack());
		if (pages.isEmpty()) return;

		/* Cycle through pages */
		if (mc.player.age % 80 == 0 && !shown) {
			shown = true;
			if (pageCount == pages.size() - 1) pageCount = 0;
			else pageCount++;
		} else if (mc.player.age % 80 != 0) shown = false;

		int length = mc.textRenderer.getStringWidth("Page: " + (pageCount + 1) + "/" + pages.size());

		renderTooltipBox(mX + 56 - length / 2, mY - pages.get(pageCount).size() * 10 - 19, 5, length, true);
		renderTooltipBox(mX, mY - pages.get(pageCount).size() * 10 - 6, pages.get(pageCount).size() * 10 - 2, 120, true);
		mc.textRenderer.drawWithShadow("Page: " + (pageCount + 1) + "/" + pages.size(),
				mX + 68 - length / 2, mY - pages.get(pageCount).size() * 10 - 32, -1);

		int count = 0;
		for (String s: pages.get(pageCount)) {
			mc.textRenderer.drawWithShadow(s, mX + 12, mY - 18 - pages.get(pageCount).size() * 10 + count * 10, 0x00c0c0);
			count++;
		}

	}

	public void drawMapToolTip(Slot slot, int mX, int mY) {
		if (slot.getStack().getItem() != Items.FILLED_MAP) return;

		MapState data = FilledMapItem.getMapState(slot.getStack(), mc.world);
		if (data == null || data.colors == null) return;
		
		byte[] colors = data.colors;

		double size = getSetting(4).asSlider().getValue();

		GL11.glPushMatrix();
		GL11.glScaled(size, size, 1.0);
		GL11.glTranslatef(0.0F, 0.0F, 300.0F);
		int x = (int) (mX*(1/size) + 12*(1/size));
		int y = (int) (mY*(1/size) - 12*(1/size) - 140);

		renderTooltipBox(x - 12, y + 12, 128, 128, false);
		for (byte c: colors) {
			int c1 = c & 255;

			if (c1 / 4 != 0) DrawableHelper.fill(x, y, x+1, y+1, getRenderColorFix(MaterialColor.COLORS[c1 / 4].color, c1 & 3));
			if (x - (int) (mX*(1/size)+12*(1/size)) == 127) { x = (int) (mX*(1/size)+12*(1/size)); y++; }
			else x++;
		}

		GL11.glPopMatrix();
	}

	/* Fix your game 🅱️ojang */
	private int getRenderColorFix(int color, int offset) {
		int int_2 = (offset == 3 ? 135 : offset == 2 ? 255 : offset == 0 ? 180 : 220);

		int r = (color >> 16 & 255) * int_2 / 255;
		int g = (color >> 8 & 255) * int_2 / 255;
		int b = (color & 255) * int_2 / 255;
		return -16777216 | r << 16 | g << 8 | b;
	}

	private void renderTooltipBox(int x1, int y1, int x2, int y2, boolean wrap) {
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
		fillGradient(int_5 - 3, int_6 - 4, int_5 + y2 + 3, int_6 - 3, -267386864, -267386864);
		fillGradient(int_5 - 3, int_6 + x2 + 3, int_5 + y2 + 3, int_6 + x2 + 4, -267386864, -267386864);
		fillGradient(int_5 - 3, int_6 - 3, int_5 + y2 + 3, int_6 + x2 + 3, -267386864, -267386864);
		fillGradient(int_5 - 4, int_6 - 3, int_5 - 3, int_6 + x2 + 3, -267386864, -267386864);
		fillGradient(int_5 + y2 + 3, int_6 - 3, int_5 + y2 + 4, int_6 + x2 + 3, -267386864, -267386864);
		fillGradient(int_5 - 3, int_6 - 3 + 1, int_5 - 3 + 1, int_6 + x2 + 3 - 1, 1347420415, 1344798847);
		fillGradient(int_5 + y2 + 2, int_6 - 3 + 1, int_5 + y2 + 3, int_6 + x2 + 3 - 1, 1347420415, 1344798847);
		fillGradient(int_5 - 3, int_6 - 3, int_5 + y2 + 3, int_6 - 3 + 1, 1347420415, 1347420415);
		fillGradient(int_5 - 3, int_6 + x2 + 2, int_5 + y2 + 3, int_6 + x2 + 3, 1344798847, 1344798847);
		GL11.glShadeModel(7424);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	private void fillGradient(int x1, int y1, int x2, int y2, int color1, int color2) {
		float float_1 = (color1 >> 24 & 255) / 255.0F;
		float float_2 = (color1 >> 16 & 255) / 255.0F;
		float float_3 = (color1 >> 8 & 255) / 255.0F;
		float float_4 = (color1 & 255) / 255.0F;
		float float_5 = (color2 >> 24 & 255) / 255.0F;
		float float_6 = (color2 >> 16 & 255) / 255.0F;
		float float_7 = (color2 >> 8 & 255) / 255.0F;
		float float_8 = (color2 & 255) / 255.0F;
		Tessellator tessellator_1 = Tessellator.getInstance();
		BufferBuilder bufferBuilder_1 = tessellator_1.getBufferBuilder();
		bufferBuilder_1.begin(7, VertexFormats.POSITION_COLOR);
		bufferBuilder_1.vertex(x1, y1, 0).color(float_2, float_3, float_4, float_1).next();
		bufferBuilder_1.vertex(x1, y2, 0).color(float_2, float_3, float_4, float_1).next();
		bufferBuilder_1.vertex(x2, y2, 0).color(float_6, float_7, float_8, float_5).next();
		bufferBuilder_1.vertex(x2, y1, 0).color(float_6, float_7, float_8, float_5).next();
		tessellator_1.draw();
	}
}

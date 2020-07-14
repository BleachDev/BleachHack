package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;

import bleach.hack.event.events.EventDrawTooltip;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.Matrix4f;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;

public class Peek extends Module {
	
	private List<List<String>> pages;
	private int[] slotPos;
	private int pageCount = 0;
	private boolean shown = false;
	
	public Peek() {
		super("Peek", KEY_UNBOUND, Category.MISC, "Shows whats inside containers",
				new SettingToggle("Containers", true),
				new SettingMode("Info: ", "All", "Name", "None"),
				new SettingToggle("Books", true),
				new SettingToggle("Maps", true),
				new SettingSlider("Map Size: ", 0.25, 1.5, 0.5, 2));
	}
	
	@Subscribe
	public void drawScreen(EventDrawTooltip event) {
		if (!(event.screen instanceof HandledScreen)) {
			return;
		}
		
		Slot slot = (Slot) FabricReflect.getFieldValue(event.screen, "field_2787", "focusedSlot");
		if (slot == null) return;
		
		if (!Arrays.equals(new int[] {slot.x, slot.y}, slotPos)) {
			pageCount = 0;
			pages = null;
		}
		
		slotPos = new int[] {slot.x, slot.y};
		
		if (getSettings().get(0).toToggle().state) drawShulkerToolTip(event, slot, event.mX, event.mY);
		if (getSettings().get(2).toToggle().state) drawBookToolTip(event.matrix, slot, event.mX, event.mY);
		if (getSettings().get(3).toToggle().state) drawMapToolTip(event.matrix, slot, event.mX, event.mY);
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
		
		if (getSettings().get(1).toMode().mode == 2) {
			event.setCancelled(true);
		} else if (getSettings().get(1).toMode().mode == 1) {
			event.text = Arrays.asList(slot.getStack().getName());
		}
		
		int realY = getSettings().get(1).toMode().mode == 2 ? mY + 24 : mY;
		
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
		
		for (ItemStack i: items) {
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
		
		int lenght = mc.textRenderer.getWidth("Page: " + (pageCount + 1) + "/" + pages.size());
		
		renderTooltipBox(matrix, mX + 56 - lenght / 2, mY - pages.get(pageCount).size() * 10 - 19, 5, lenght, true);
		renderTooltipBox(matrix, mX, mY - pages.get(pageCount).size() * 10 - 6, pages.get(pageCount).size() * 10 - 2, 120, true);
		mc.textRenderer.drawWithShadow(matrix, "Page: " + (pageCount + 1) + "/" + pages.size(),
				mX + 68 - lenght / 2, mY - pages.get(pageCount).size() * 10 - 32, -1);
		
		int count = 0;
		for (String s: pages.get(pageCount)) {
			mc.textRenderer.drawWithShadow(matrix, s, mX + 12, mY - 18 - pages.get(pageCount).size() * 10 + count * 10, 0x00c0c0);
			count++;
		}
		
	}
	
	public void drawMapToolTip(MatrixStack matrix, Slot slot, int mX, int mY) {
		if (slot.getStack().getItem() != Items.FILLED_MAP) return;
		
		MapState data = FilledMapItem.getMapState(slot.getStack(), mc.world);
		byte[] colors = data.colors;
		
		double size = getSettings().get(3).toSlider().getValue();
		
		GL11.glPushMatrix();
		GL11.glScaled(size, size, 1.0);
		GL11.glTranslatef(0.0F, 0.0F, 300.0F);
		int x = (int) (mX*(1/size) + 12*(1/size));
		int y = (int) (mY*(1/size) - 12*(1/size) - 140);
		
		renderTooltipBox(matrix, x - 12, y + 12, 128, 128, false);
		for (byte c: colors) {
			int c1 = c & 255;
			
			if (c1 / 4 != 0) Screen.fill(matrix, x, y, x+1, y+1, getRenderColorFix(MaterialColor.COLORS[c1 / 4].color, c1 & 3));
			if (x - (int) (mX*(1/size)+12*(1/size)) == 127) { x = (int) (mX*(1/size)+12*(1/size)); y++; }
			else x++;
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
	
	public void renderTooltipBox(MatrixStack matrix, int x1, int y1, int x2, int y2, boolean wrap) {
		int int_5 = x1 + 12;
		int int_6 = y1 - 12;
		if (wrap) {
			if (int_5 + y2 > mc.currentScreen.width) int_5 -= 28 + y2;
			if (int_6 + x2 + 6 > mc.currentScreen.height) int_6 = mc.currentScreen.height - x2 - 6;
		}

		matrix.push();
		Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, VertexFormats.POSITION_COLOR);
        Matrix4f matrix4f = matrix.peek().getModel();
		FabricReflect.invokeMethod(mc.currentScreen, "", "fillGradient", matrix4f, buffer, int_5 - 3, int_6 - 4, int_5 + y2 + 3, int_6 - 3, 400, -267386864, -267386864);
		FabricReflect.invokeMethod(mc.currentScreen, "", "fillGradient", matrix4f, buffer, int_5 - 3, int_6 + x2 + 3, int_5 + y2 + 3, int_6 + x2 + 4, 400, -267386864, -267386864);
		FabricReflect.invokeMethod(mc.currentScreen, "", "fillGradient", matrix4f, buffer, int_5 - 3, int_6 - 3, int_5 + y2 + 3, int_6 + x2 + 3, 400, -267386864, -267386864);
		FabricReflect.invokeMethod(mc.currentScreen, "", "fillGradient", matrix4f, buffer, int_5 - 4, int_6 - 3, int_5 - 3, int_6 + x2 + 3, 400, -267386864, -267386864);
		FabricReflect.invokeMethod(mc.currentScreen, "", "fillGradient", matrix4f, buffer, int_5 + y2 + 3, int_6 - 3, int_5 + y2 + 4, int_6 + x2 + 3, 400, -267386864, -267386864);
		FabricReflect.invokeMethod(mc.currentScreen, "", "fillGradient", matrix4f, buffer, int_5 - 3, int_6 - 3 + 1, int_5 - 3 + 1, int_6 + x2 + 3 - 1, 400, 1347420415, 1344798847);
		FabricReflect.invokeMethod(mc.currentScreen, "", "fillGradient", matrix4f, buffer, int_5 + y2 + 2, int_6 - 3 + 1, int_5 + y2 + 3, int_6 + x2 + 3 - 1, 400, 1347420415, 1344798847);
		FabricReflect.invokeMethod(mc.currentScreen, "", "fillGradient", matrix4f, buffer, int_5 - 3, int_6 - 3, int_5 + y2 + 3, int_6 - 3 + 1, 400, 1347420415, 1347420415);
		FabricReflect.invokeMethod(mc.currentScreen, "", "fillGradient", matrix4f, buffer, int_5 - 3, int_6 + x2 + 2, int_5 + y2 + 3, int_6 + x2 + 3, 400, 1344798847, 1344798847);

		RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        buffer.end();
        BufferRenderer.draw(buffer);
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        matrix.pop();
	}
}

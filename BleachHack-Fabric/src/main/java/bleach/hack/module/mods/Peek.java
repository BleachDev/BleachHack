package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.FabricReflect;
import bleach.hack.utils.ItemContentUtils;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class Peek extends Module {

	public Peek() {
		super("Peek", -1, Category.MISC, "Shows whats inside containers", null);
	}
	
	public void drawTooltip(int mX, int mY) {
		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.currentScreen;
		
		Slot slot = null;
		try { slot = (Slot) FabricReflect.getFieldValue(screen, "field_2787", "focusedSlot"); } catch (Exception e) {}
		if(slot == null) return;
		
		drawShulkerToolTip(slot, mX, mY);
		drawBookToolTip(slot, mX, mY);
	}
	
	public void drawShulkerToolTip(Slot slot, int mX, int mY) {
		if(!(slot.getStack().getItem() instanceof BlockItem)) return;
		if(!(((BlockItem) slot.getStack().getItem()).getBlock() instanceof ShulkerBoxBlock)
				 && !(((BlockItem) slot.getStack().getItem()).getBlock() instanceof ChestBlock)
				 && !(((BlockItem) slot.getStack().getItem()).getBlock() instanceof DispenserBlock)
				 && !(((BlockItem) slot.getStack().getItem()).getBlock() instanceof HopperBlock)) return;
		
		List<ItemStack> items = ItemContentUtils.getItemsInContainer(slot.getStack());
		
		Block block = ((BlockItem) slot.getStack().getItem()).getBlock();
		
		int count = block instanceof HopperBlock || block instanceof DispenserBlock ? 18 : 0;
		if(block instanceof HopperBlock) renderTooltipBox(mX, mY - 21, 13, 82);
		else if(block instanceof DispenserBlock) renderTooltipBox(mX, mY - 21, 13, 150);
		else renderTooltipBox(mX, mY - 55, 47, 150);
		for(ItemStack i: items) {
			if(count > 26) break;
			int x = mX + 10 + (17 * (count % 9));
			int y = mY - 69 + (17 * (count / 9));
			
			mc.getItemRenderer().renderGuiItem(i, x, y);
		    mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, i, x, y, i.getCount() > 1 ? i.getCount() + "" : "");
			count++;
		}
	}
	
	public void drawBookToolTip(Slot slot, int mX, int mY) {
		if(slot.getStack().getItem() != Items.WRITABLE_BOOK && slot.getStack().getItem() != Items.WRITTEN_BOOK) return;
		
		List<String> pages = ItemContentUtils.getTextInBook(slot.getStack());
		if(pages.isEmpty()) return;
		
		List<String> page = new ArrayList<>();
		
		String buffer = "";
		for(char c: pages.get(0).toCharArray()) {
			if(mc.textRenderer.getStringWidth(buffer) > 114 || buffer.endsWith("\n")) {
				page.add(buffer.replace("\n", ""));
				buffer = "";
			}
			
			buffer += c;
		}
		page.add(buffer);
		
		renderTooltipBox(mX, mY - page.size() * 10 - 6, page.size() * 10 - 2, 120);
		mc.textRenderer.drawWithShadow("Page: " + 1 + "/" + pages.size(),
				mX + 68 - mc.textRenderer.getStringWidth("Page: " + 1 + "/" + pages.size()) / 2, mY - page.size() * 10 - 32, -1);
		
		int count = 0;
		for(String s: page) {
			mc.textRenderer.drawWithShadow(s, mX + 12, mY - 18 - page.size() * 10 + count * 10, 0x00c0c0);
			count++;
		}
		
		
	}
	
	public void renderTooltipBox(int x1, int y1, int x2, int y2) {
         GlStateManager.disableRescaleNormal();
         GuiLighting.disable();
         GlStateManager.disableLighting();
         GlStateManager.disableDepthTest();
         GL11.glTranslatef(0.0F, 0.0F, 300.0F);

         int int_5 = x1 + 12;
         int int_6 = y1 - 12;

         if (int_5 + y2 > mc.currentScreen.width) int_5 -= 28 + y2;
         if (int_6 + x2 + 6 > mc.currentScreen.height) int_6 = mc.currentScreen.height - x2 - 6;

         /* why the fork is this private? */
         FabricReflect.invokeMethod(mc.currentScreen, "", "fillGradient", int_5 - 3, int_6 - 4, int_5 + y2 + 3, int_6 - 3, -267386864, -267386864);
         FabricReflect.invokeMethod(mc.currentScreen, "", "fillGradient", int_5 - 3, int_6 + x2 + 3, int_5 + y2 + 3, int_6 + x2 + 4, -267386864, -267386864);
         FabricReflect.invokeMethod(mc.currentScreen, "", "fillGradient", int_5 - 3, int_6 - 3, int_5 + y2 + 3, int_6 + x2 + 3, -267386864, -267386864);
         FabricReflect.invokeMethod(mc.currentScreen, "", "fillGradient", int_5 - 4, int_6 - 3, int_5 - 3, int_6 + x2 + 3, -267386864, -267386864);
         FabricReflect.invokeMethod(mc.currentScreen, "", "fillGradient", int_5 + y2 + 3, int_6 - 3, int_5 + y2 + 4, int_6 + x2 + 3, -267386864, -267386864);
         FabricReflect.invokeMethod(mc.currentScreen, "", "fillGradient", int_5 - 3, int_6 - 3 + 1, int_5 - 3 + 1, int_6 + x2 + 3 - 1, 1347420415, 1344798847);
         FabricReflect.invokeMethod(mc.currentScreen, "", "fillGradient", int_5 + y2 + 2, int_6 - 3 + 1, int_5 + y2 + 3, int_6 + x2 + 3 - 1, 1347420415, 1344798847);
         FabricReflect.invokeMethod(mc.currentScreen, "", "fillGradient", int_5 - 3, int_6 - 3, int_5 + y2 + 3, int_6 - 3 + 1, 1347420415, 1347420415);
         FabricReflect.invokeMethod(mc.currentScreen, "", "fillGradient", int_5 - 3, int_6 + x2 + 2, int_5 + y2 + 3, int_6 + x2 + 3, 1344798847, 1344798847);
         
         GlStateManager.enableLighting();
         GlStateManager.enableDepthTest();
         GuiLighting.enable();
         GlStateManager.enableRescaleNormal();
	}

}

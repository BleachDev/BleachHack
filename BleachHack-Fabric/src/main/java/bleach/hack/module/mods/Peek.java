package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.FabricReflect;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class Peek extends Module {

	public Peek() {
		super("Peek", -1, Category.MISC, "Shows whats inside containers", null);
	}
	
	public void drawTooltip(int mX, int mY) {
		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.currentScreen;
		
		Slot slot = null;
		try { slot = (Slot) FabricReflect.getField(AbstractContainerScreen.class, "field_2787", "focusedSlot").get(screen); } catch (Exception e) {}
		
		if(slot == null) return;
		if(!(slot.getStack().getItem() instanceof BlockItem)) return;
		if(!(((BlockItem) slot.getStack().getItem()).getBlock() instanceof ShulkerBoxBlock)
				 && !(((BlockItem) slot.getStack().getItem()).getBlock() instanceof ChestBlock)
				 && !(((BlockItem) slot.getStack().getItem()).getBlock() instanceof DispenserBlock)
				 && !(((BlockItem) slot.getStack().getItem()).getBlock() instanceof HopperBlock)) return;
		
		List<ItemStack> items = new ArrayList<>(Collections.nCopies(27, new ItemStack(Items.AIR)));
		CompoundTag nbt = slot.getStack().getTag();
		if(nbt != null && nbt.containsKey("BlockEntityTag")) {
			CompoundTag nbt2 = nbt.getCompound("BlockEntityTag");
			if(nbt2.containsKey("Items")) {
				ListTag nbt3 = (ListTag) nbt2.getTag("Items");
				System.out.println(nbt3);
				for(int i = 0; i < nbt3.size(); i++) {
					items.set(nbt3.getCompoundTag(i).getByte("Slot"), ItemStack.fromTag(nbt3.getCompoundTag(i)));
				}
			}
		}
		
		GlStateManager.translatef(0.0F, 0.0F, 500.0F);
		Block block = ((BlockItem) slot.getStack().getItem()).getBlock();
		
		int count = block instanceof HopperBlock || block instanceof DispenserBlock ? 18 : 0;
		String s = "                                     ";
		if(block instanceof HopperBlock) mc.currentScreen.renderTooltip(Arrays.asList("                    "), mX, mY - 19);
		else if(block instanceof DispenserBlock) mc.currentScreen.renderTooltip(Arrays.asList(s), mX, mY - 19);
		else mc.currentScreen.renderTooltip(Arrays.asList(s,s,s,s,s), mX, mY - 58);
		for(ItemStack i: items) {
			if(count > 26) break;
			int x = mX + 8 + (17 * (count % 9));
			int y = mY - 70 + (17 * (count / 9));
			
			mc.getItemRenderer().renderGuiItem(i, x, y);
		    mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, i, x, y, i.getCount() > 1 ? i.getCount() + "" : "");
			count++;
		}
	}

}

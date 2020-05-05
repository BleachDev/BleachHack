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
package bleach.hack.module.mods;

import com.mojang.blaze3d.platform.GlStateManager;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Peek extends Module {

	public Peek() {
		super("Peek", -1, Category.MISC, "Shows whats inside containers", null);
	}
	
	public void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@SubscribeEvent
	public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
		if (!(event.getGui() instanceof ContainerScreen<?>)) return;
		ContainerScreen<?> screen = (ContainerScreen<?>) event.getGui();
		
		if (screen.getSlotUnderMouse() == null) return;
		if (!(screen.getSlotUnderMouse().getStack().getItem() instanceof BlockItem)) return;
		if (!(((BlockItem) screen.getSlotUnderMouse().getStack().getItem()).getBlock() instanceof ContainerBlock)) return;
		
		NonNullList<ItemStack> items = NonNullList.withSize(27, new ItemStack(Items.AIR));
		CompoundNBT nbt = screen.getSlotUnderMouse().getStack().getTag();
		
		if (nbt != null && nbt.contains("BlockEntityTag")) {
			CompoundNBT itemnbt = nbt.getCompound("BlockEntityTag");
			if (itemnbt.contains("Items")) ItemStackHelper.loadAllItems(itemnbt, items);
		}
		
		GlStateManager.translatef(0.0F, 0.0F, 500.0F);
		Block block = ((BlockItem) screen.getSlotUnderMouse().getStack().getItem()).getBlock();
		
		int count = block instanceof HopperBlock || block instanceof DispenserBlock ? 18 : 0;
		for (ItemStack i: items) {
			if (count > 26) break;
			int x = event.getMouseX() + 8 + (17 * (count % 9));
			int y = event.getMouseY() - 68 + (17 * (count / 9));
			
			if (i.getItem() != Items.AIR) {
				Screen.fill(x, y, x+17, y+17, 0x90000000);
				Screen.fill(x, y, x+17, y+1, 0xff000000); Screen.fill(x, y+1, x+1, y+17, 0xff000000);
				Screen.fill(x+16, y+1, x+17, y+17, 0xff000000); Screen.fill(x+1, y+16, x+17, y+17, 0xff000000);
			}
			
		    mc.getItemRenderer().renderItemAndEffectIntoGUI(i, x, y);
		    mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, i, x, y, i.getCount() > 1 ? i.getCount() + "" : "");
			count++;
		}
	}

}

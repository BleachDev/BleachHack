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
package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.BleachQueue;
import net.minecraft.block.ContainerBlock;
import net.minecraft.client.gui.screen.inventory.ShulkerBoxScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.ShulkerBoxContainer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;

public class CmdPeek extends Command {

	@Override
	public String getAlias() {
		return "peek";
	}

	@Override
	public String getDescription() {
		return "Shows whats inside a container";
	}

	@Override
	public String getSyntax() {
		return ".peek";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		ItemStack item = mc.player.inventory.getCurrentItem();
		
		if(!(item.getItem() instanceof BlockItem)) {
			BleachLogger.errorMessage("Must be holding a containter to peek.");
			return;
		}
		
		if(!(((BlockItem) item.getItem()).getBlock() instanceof ContainerBlock)) {
			BleachLogger.errorMessage("Must be holding a containter to peek.");
			return;
		}
		
		NonNullList<ItemStack> items = NonNullList.withSize(27, new ItemStack(Items.AIR));
		CompoundNBT nbt = item.getTag();
		
		if(nbt != null && nbt.contains("BlockEntityTag")) {
			CompoundNBT itemnbt = nbt.getCompound("BlockEntityTag");
			if(itemnbt.contains("Items")) ItemStackHelper.loadAllItems(itemnbt, items);
		}
		
		Inventory inv = new Inventory(items.toArray(new ItemStack[27]));
		
		BleachQueue.queue.add(() -> {
			mc.displayGuiScreen(new ShulkerBoxScreen(
					new ShulkerBoxContainer(420, mc.player.inventory, inv),
					mc.player.inventory,
					item.getDisplayName()));
		});
	}

}

/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command.commands;

import java.util.List;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.BleachQueue;
import bleach.hack.util.ItemContentUtils;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.Text;

public class CmdPeek extends Command {

	public CmdPeek() {
		super("peek", "Shows whats inside the container you're holder.", "peek", CommandCategory.MISC);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		ItemStack item = mc.player.getInventory().getMainHandStack();

		if (!(item.getItem() instanceof BlockItem)) {
			BleachLogger.errorMessage("Must be holding a containter to peek.");
			return;
		}

		if (!(((BlockItem) item.getItem()).getBlock() instanceof ShulkerBoxBlock)
				&& !(((BlockItem) item.getItem()).getBlock() instanceof ChestBlock)
				&& !(((BlockItem) item.getItem()).getBlock() instanceof DispenserBlock)
				&& !(((BlockItem) item.getItem()).getBlock() instanceof HopperBlock)) {
			BleachLogger.errorMessage("Must be holding a containter to peek.");
			return;
		}

		List<ItemStack> items = ItemContentUtils.getItemsInContainer(item);

		SimpleInventory inv = new SimpleInventory(items.toArray(new ItemStack[27]));

		BleachQueue.add(() -> {
			mc.openScreen(new PeekShulkerScreen(
					new ShulkerBoxScreenHandler(420, mc.player.getInventory(), inv),
					mc.player.getInventory(),
					item.getName()));
		});
	}

	class PeekShulkerScreen extends ShulkerBoxScreen {

		public PeekShulkerScreen(ShulkerBoxScreenHandler handler, PlayerInventory inventory, Text title) {
			super(handler, inventory, title);
		}

		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return false;
		}
	}

}

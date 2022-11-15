/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import net.minecraft.block.*;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.Text;
import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.BleachQueue;
import org.bleachhack.util.ItemContentUtils;

import java.util.List;

public class CmdPeek extends Command {

	public CmdPeek() {
		super("peek", "Shows whats inside the container you're holder.", "peek", CommandCategory.MISC);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		ItemStack item = mc.player.getInventory().getMainHandStack();

		if (item.getItem() instanceof BlockItem) {
			Block block = ((BlockItem) item.getItem()).getBlock();
			if (!(block instanceof ShulkerBoxBlock
					|| block instanceof ChestBlock
					|| block instanceof DispenserBlock
					|| block instanceof HopperBlock)) {
				BleachLogger.error("Must be holding a containter to peek.");
				return;
			}
		} else if (item.getItem() != Items.BUNDLE) {
			BleachLogger.error("Must be holding a containter to peek.");
			return;
		}

		List<ItemStack> items = ItemContentUtils.getItemsInContainer(item);

		SimpleInventory inv = new SimpleInventory(items.toArray(new ItemStack[27]));

		BleachQueue.add(() ->
				mc.setScreen(new PeekShulkerScreen(
						new ShulkerBoxScreenHandler(420, mc.player.getInventory(), inv),
						mc.player.getInventory(),
						item.getName())));
	}

	static class PeekShulkerScreen extends ShulkerBoxScreen {

		public PeekShulkerScreen(ShulkerBoxScreenHandler handler, PlayerInventory inventory, Text title) {
			super(handler, inventory, title);
		}

		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return false;
		}
	}

}

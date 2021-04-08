/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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

import java.util.List;

import bleach.hack.command.Command;
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
		return "peek";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		ItemStack item = mc.player.inventory.getMainHandStack();

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
					new ShulkerBoxScreenHandler(420, mc.player.inventory, inv),
					mc.player.inventory,
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

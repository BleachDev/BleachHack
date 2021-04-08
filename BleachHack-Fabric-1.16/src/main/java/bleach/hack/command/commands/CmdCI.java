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

import bleach.hack.command.Command;
import bleach.hack.util.BleachLogger;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameMode;

public class CmdCI extends Command {

	@Override
	public String getAlias() {
		return "ci";
	}

	@Override
	public String getDescription() {
		return "Clears inventory (Creative)";
	}

	@Override
	public String getSyntax() {
		return "ci";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if (mc.interactionManager.getCurrentGameMode() != GameMode.CREATIVE) {
			printSyntaxError("Bruh you're not in creative.");
			return;
		}
		
		for (int i = 0; i < mc.player.playerScreenHandler.getStacks().size(); i++) {
			mc.interactionManager.clickCreativeStack(ItemStack.EMPTY, i);
		}

		BleachLogger.infoMessage("Cleared all items");
	}

}

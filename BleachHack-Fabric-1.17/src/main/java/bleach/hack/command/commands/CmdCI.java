/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.command.exception.CmdSyntaxException;
import bleach.hack.util.BleachLogger;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameMode;

public class CmdCI extends Command {

	public CmdCI() {
		super("ci", "Clears your inventory.", "ci", CommandCategory.CREATIVE,
				"clear", "clearinv");
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (mc.interactionManager.getCurrentGameMode() != GameMode.CREATIVE) {
			throw new CmdSyntaxException("Bruh you're not in creative.");
		}

		for (int i = 0; i < mc.player.playerScreenHandler.getStacks().size(); i++) {
			mc.interactionManager.clickCreativeStack(ItemStack.EMPTY, i);
		}

		BleachLogger.infoMessage("Cleared all items");
	}

}

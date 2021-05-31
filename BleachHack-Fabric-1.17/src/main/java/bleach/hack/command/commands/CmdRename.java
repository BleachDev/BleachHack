/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command.commands;

import org.apache.commons.lang3.StringUtils;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.util.BleachLogger;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;

public class CmdRename extends Command {

	public CmdRename() {
		super("rename", "Renames an item, use \"&\" for color.", "rename <name>", CommandCategory.CREATIVE);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (!mc.player.getAbilities().creativeMode) {
			BleachLogger.errorMessage("Not In Creative Mode!");
			return;
		}

		ItemStack i = mc.player.getInventory().getMainHandStack();

		i.setCustomName(new LiteralText(StringUtils.join(args, ' ').replace("&", "\u00a7").replace("\u00a7\u00a7", "&")));
		BleachLogger.infoMessage("Renamed Item");
	}

}

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
import bleach.hack.gui.EntityMenuEditScreen;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.EntityMenu;
import bleach.hack.util.BleachQueue;
import bleach.hack.util.collections.MutablePairList;

/**
 * @author <a href="https://github.com/lasnikprogram">Lasnik</a>
 */
public class CmdEntityMenu extends Command {

	public CmdEntityMenu() {
		super("entitymenu", "Opens the gui to manage the things which appear on the entitymenu interaction screen.", "entitymenu", CommandCategory.MODULES,
				"playermenu", "interactionmenu");
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		MutablePairList<String, String> interactions = ((EntityMenu) ModuleManager.getModule("EntityMenu")).interactions;

		BleachQueue.add(() -> mc.openScreen(new EntityMenuEditScreen(interactions)));
	}
}

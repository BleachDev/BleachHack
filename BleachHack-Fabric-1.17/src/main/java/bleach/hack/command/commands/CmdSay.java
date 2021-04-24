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
import bleach.hack.command.CommandManager;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

public class CmdSay extends Command {

	public CmdSay() {
		super("say", "Says a message in chat.", "say <message>", CommandCategory.MISC);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		CommandManager.allowNextMsg = true;
		mc.player.networkHandler.sendPacket(new ChatMessageC2SPacket(String.join(" ", args)));
	}

}

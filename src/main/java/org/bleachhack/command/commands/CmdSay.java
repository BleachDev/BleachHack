/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import net.minecraft.network.encryption.Signer;
import net.minecraft.network.message.MessageSignature;
import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.command.CommandManager;

import net.minecraft.network.message.ChatMessageSigner;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

public class CmdSay extends Command {

	public CmdSay() {
		super("say", "Says a message in chat.", "say <message>", CommandCategory.MISC);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		CommandManager.allowNextMsg = true;

		String message = String.join(" ", args);
		MessageSignature sig = MessageSignature.none();
		try {
			Signer signer2 = mc.getProfileKeys().getSigner();
			if (signer2 != null) {
				sig = ChatMessageSigner.create(mc.player.getUuid()).sign(signer2, message);
			}
		}
		catch (Exception exception) {
		}

		mc.player.networkHandler.sendPacket(new ChatMessageC2SPacket(String.join(" ", args), sig, true));
	}

}

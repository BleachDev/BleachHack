/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.command.exception.CmdSyntaxException;
import org.bleachhack.util.BleachLogger;

import com.google.common.io.Resources;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;

public class CmdSkull extends Command {

	public CmdSkull() {
		super("skull", "Gives you a player skull.", "skull <player> | skull img <image url>", CommandCategory.CREATIVE,
				"playerhead", "head");
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (!mc.interactionManager.getCurrentGameMode().isCreative()) {
			BleachLogger.error("Not In Creative Mode!");
			return;
		}

		if (args.length == 0) {
			throw new CmdSyntaxException();
		}

		ItemStack item = new ItemStack(Items.PLAYER_HEAD, 64);

		Random random = new Random();
		String id = "[I;" + random.nextInt() + "," + random.nextInt() + "," + random.nextInt() + "," + random.nextInt() + "]";

		if (args.length < 2) {
			try {
				JsonObject json = JsonParser.parseString(
						Resources.toString(new URL("https://api.mojang.com/users/profiles/minecraft/" + args[0]), StandardCharsets.UTF_8))
						.getAsJsonObject();

				JsonObject json2 = JsonParser.parseString(
						Resources.toString(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + json.get("id").getAsString()), StandardCharsets.UTF_8))
						.getAsJsonObject();

				item.setNbt(StringNbtReader.parse("{SkullOwner:{Id:" + id + ",Properties:{textures:[{Value:\""
						+ json2.get("properties").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString()
						+ "\"}]}}}"));
			} catch (Exception e) {
				e.printStackTrace();
				BleachLogger.error("Error getting head! (" + e.getClass().getSimpleName() + ")");
			}
		} else if (args[0].equalsIgnoreCase("img")) {
			NbtCompound tag = StringNbtReader.parse(
					"{SkullOwner:{Id:" + id + ",Properties:{textures:[{Value:\"" + encodeUrl(args[1]) + "\"}]}}}");
			item.setNbt(tag);
			BleachLogger.logger.info(tag);
		}

		mc.player.getInventory().addPickBlock(item);
	}

	private String encodeUrl(String url) {
		return Base64.getEncoder().encodeToString(("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}").getBytes());
	}

}

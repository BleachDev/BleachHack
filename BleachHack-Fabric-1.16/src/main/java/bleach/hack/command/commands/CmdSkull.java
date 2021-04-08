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

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

import com.google.common.io.Resources;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import bleach.hack.command.Command;
import bleach.hack.util.BleachLogger;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;

public class CmdSkull extends Command {

	@Override
	public String getAlias() {
		return "skull";
	}

	@Override
	public String getDescription() {
		return "Gives you a player skull";
	}

	@Override
	public String getSyntax() {
		return "skull <Player> | skull img <Image url>";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if (args.length == 0) {
			printSyntaxError();
			return;
		}

		ItemStack item = new ItemStack(Items.PLAYER_HEAD, 64);

		Random random = new Random();
		String id = "[I;" + random.nextInt() + "," + random.nextInt() + "," + random.nextInt() + "," + random.nextInt() + "]";

		if (args.length < 2) {
			try {
				JsonObject json = new JsonParser().parse(
						Resources.toString(new URL("https://api.mojang.com/users/profiles/minecraft/" + args[0]), StandardCharsets.UTF_8))
						.getAsJsonObject();

				JsonObject json2 = new JsonParser().parse(
						Resources.toString(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + json.get("id").getAsString()), StandardCharsets.UTF_8))
						.getAsJsonObject();

				item.setTag(StringNbtReader.parse("{SkullOwner:{Id:" + id + ",Properties:{textures:[{Value:\""
						+ json2.get("properties").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString()
						+ "\"}]}}}"));
			} catch (Exception e) {
				e.printStackTrace();
				BleachLogger.errorMessage("Error getting head! (" + e.getClass().getSimpleName() + ")");
			}
		} else if (args[0].equalsIgnoreCase("img")) {
			CompoundTag tag = StringNbtReader.parse(
					"{SkullOwner:{Id:" + id + ",Properties:{textures:[{Value:\"" + encodeUrl(args[1]) + "\"}]}}}");
			item.setTag(tag);
			System.out.println(tag);
		}

		mc.player.inventory.addPickBlock(item);
	}

	private String encodeUrl(String url) {
		return Base64.getEncoder().encodeToString(("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}").getBytes());
	}

}

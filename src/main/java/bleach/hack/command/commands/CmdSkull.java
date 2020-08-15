/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;

import java.util.Base64;
import java.util.UUID;

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
        return "skull [Player] | skull img [Image url]";
    }

    @Override
    public void onCommand(String command, String[] args) throws Exception {
        ItemStack item = new ItemStack(Items.PLAYER_HEAD, 64);

        if (args.length < 2) {
            item.setTag(StringNbtReader.parse("{SkullOwner:{Name:\"" + args[0] + "\"}}"));
        } else if (args[0].equalsIgnoreCase("img")) {
            CompoundTag tag = StringNbtReader.parse("{SkullOwner:{Id:\"" + UUID.randomUUID() + "\",Properties:{textures:[{Value:\""
                    + Base64.getEncoder().encodeToString(("{\"textures\":{\"SKIN\":{\"url\":\"" + args[1] + "\"}}}").getBytes())
                    + "\"}]}}}");
            item.setTag(tag);
            System.out.println(tag);
        }

        mc.player.inventory.addPickBlock(item);
    }

}

/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.math.NumberUtils;
import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.command.exception.CmdSyntaxException;
import org.bleachhack.util.BleachLogger;

import java.util.Locale;
import java.util.Random;

public class CmdGive extends Command {

	public CmdGive() {
		super("give", "Gives you an item.", "give <item> <count> <damage> <nbt> | give preset [negs/stacked/spawners/bookban/eggs] [chest/shulker/egg]", CommandCategory.CREATIVE);
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

		if (args[0].equalsIgnoreCase("preset")) {
			String args2 = (args.length >= 3 ? args[2] : "");

			ItemStack item = new ItemStack(args2.equalsIgnoreCase("egg") ? Items.STRIDER_SPAWN_EGG : args2.equalsIgnoreCase("chest") ? Items.CHEST : Items.PINK_SHULKER_BOX);
			NbtCompound tag = null;

			if (args[1].equalsIgnoreCase("negs")) {
				long dmg = args.length < 2 ? 0 : NumberUtils.toLong(args[1]);
				tag = StringNbtReader.parse(
						"{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Items\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},BlockEntityTag:{Items:[{Slot:0b,id:\"minecraft:diamond_sword\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Sword\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:1b,id:\"minecraft:diamond_sword\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Sword\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:2b,id:\"minecraft:diamond_sword\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Sword\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:3b,id:\"minecraft:diamond_sword\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Sword\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:4b,id:\"minecraft:diamond_sword\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Sword\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:5b,id:\"minecraft:diamond_sword\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Sword\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:6b,id:\"minecraft:diamond_sword\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Sword\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:7b,id:\"minecraft:diamond_sword\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Sword\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:8b,id:\"minecraft:diamond_sword\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Sword\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:9b,id:\"minecraft:diamond_sword\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Sword\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:10b,id:\"minecraft:diamond_axe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Axe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:11b,id:\"minecraft:diamond_axe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Axe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:12b,id:\"minecraft:diamond_axe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Axe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:13b,id:\"minecraft:diamond_axe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Axe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:14b,id:\"minecraft:diamond_axe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Axe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:15b,id:\"minecraft:diamond_axe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Axe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:16b,id:\"minecraft:diamond_axe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Axe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:17b,id:\"minecraft:diamond_axe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Axe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:18b,id:\"minecraft:diamond_axe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Axe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:19b,id:\"minecraft:diamond_hoe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Hoe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:20b,id:\"minecraft:diamond_hoe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Hoe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:21b,id:\"minecraft:diamond_hoe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Hoe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:22b,id:\"minecraft:diamond_hoe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Hoe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:23b,id:\"minecraft:diamond_hoe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Hoe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:24b,id:\"minecraft:diamond_hoe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Hoe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:25b,id:\"minecraft:diamond_hoe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Hoe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg)
								+ ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:26b,id:\"minecraft:diamond_hoe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Negative Hoe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Damage:"
								+ (dmg == 0 ? 1981 : dmg) + ",Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}}]}}");
			}
			else if (args[1].equalsIgnoreCase("stacked")) {
				tag = StringNbtReader.parse(
						"{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Items\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},BlockEntityTag:{Items:[{Slot:0b,id:\"minecraft:diamond_helmet\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Helmet\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:1b,id:\"minecraft:diamond_chestplate\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Chestplate\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:2b,id:\"minecraft:diamond_leggings\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Leggings\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:3b,id:\"minecraft:diamond_boots\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Boots\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:4b,id:\"minecraft:diamond_sword\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Sword\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:5b,id:\"minecraft:diamond_shovel\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Shovel\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:6b,id:\"minecraft:diamond_pickaxe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Pickaxe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:7b,id:\"minecraft:diamond_axe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Axe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:8b,id:\"minecraft:diamond_hoe\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Hoe\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:9b,id:\"minecraft:water_bucket\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Water Bucket\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"}}},{Slot:10b,id:\"minecraft:lava_bucket\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Lava Buckets\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"}}},{Slot:11b,id:\"minecraft:milk_bucket\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Milk Buckets\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"}}},{Slot:12b,id:\"minecraft:bow\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Bows\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:13b,id:\"minecraft:fishing_rod\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Fishing Rods\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},Enchantments:[{id:\"minecraft:vanishing_curse\",lvl:1s}]}},{Slot:14b,id:\"minecraft:writable_book\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Book And Quills\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},pages:[\"\"]}},{Slot:15b,id:\"minecraft:enchanted_book\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Enchanted Books\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"}}},{Slot:16b,id:\"minecraft:saddle\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Saddles\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"}}},{Slot:17b,id:\"minecraft:potion\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Bottles\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"}}},{Slot:18b,id:\"minecraft:music_disc_11\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Discs\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"}}},{Slot:19b,id:\"minecraft:music_disc_13\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Discs\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"}}},{Slot:20b,id:\"minecraft:music_disc_blocks\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Discs\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"}}},{Slot:21b,id:\"minecraft:music_disc_cat\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Discs\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"}}},{Slot:22b,id:\"minecraft:music_disc_chirp\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Discs\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"}}},{Slot:23b,id:\"minecraft:music_disc_far\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Discs\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"}}},{Slot:24b,id:\"minecraft:music_disc_mall\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Discs\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"}}},{Slot:25b,id:\"minecraft:music_disc_mellohi\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Discs\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"}}},{Slot:26b,id:\"minecraft:music_disc_stal\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Stacked Discs\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"}}}]}}");
			}
			else if (args[1].equalsIgnoreCase("spawners")) {
				tag = StringNbtReader.parse(
						"{display:{Name:\"{\\\"text\\\":\\\"Bleach's Spawners\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},BlockEntityTag:{Items:[{Slot:0b,id:\"minecraft:spawner\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Pig Spawners\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"}}},{Slot:1b,id:\"minecraft:spawner\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Lag Spawners\\\",\\\"color\\\":\\\"dark_red\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},BlockEntityTag:{SpawnCount:32767,SpawnRange:32767,Delay:0,MinSpawnDelay:0,MaxSpawnDelay:0,MaxNearbyEntities:32767,RequiredPlayerRange:32767}}},{Slot:2b,id:\"minecraft:spawner\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Lag Spawners #2\\\",\\\"color\\\":\\\"dark_red\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},BlockEntityTag:{SpawnCount:32767,SpawnRange:32767,Delay:0,MinSpawnDelay:0,MaxSpawnDelay:0,MaxNearbyEntities:32767,RequiredPlayerRange:32767}}},{Slot:3b,id:\"minecraft:spawner\",Count:64b,tag:{display:{Name:\"{\\\"text\\\":\\\"Bleach's Tnt Spawners\\\",\\\"color\\\":\\\"aqua\\\",\\\"bold\\\":true,\\\"italic\\\":true,\\\"underlined\\\":true}\"},BlockEntityTag:{SpawnCount:50,SpawnRange:10,Delay:0,MinSpawnDelay:0,MaxSpawnDelay:0,MaxNearbyEntities:32767,RequiredPlayerRange:32767,SpawnData:{id:\"minecraft:tnt\",Fuse:1}}}},{Slot:4b,id:\"minecraft:spawner\",Count:64b,tag:{display:{Name:'{\"text\":\"Bleach\\'s Boat Spawner\",\"color\":\"aqua\",\"bold\":true,\"italic\":true,\"underlined\":true}'},BlockEntityTag:{SpawnCount:50,SpawnRange:10,SpawnData:{id:\"minecraft:boat\",Glowing:1b,Invulnerable:1b,CustomNameVisible:1b,Type:\"jungle\",CustomName:'{\"text\":\"Bleach_Knight Ontop\",\"color\":\"aqua\",\"bold\":true,\"italic\":true,\"underlined\":true}'}}}}]}}");
			}
			else if (args[1].equalsIgnoreCase("bookban")) {
				tag = StringNbtReader.parse(
						"{display:{Name:'{\"text\":\"Bleach\\'s Bookban Shulker\",\"color\":\"aqua\",\"bold\":true,\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:0b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:1b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:2b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:3b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:4b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:5b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:6b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:7b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:8b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:9b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:10b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:11b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:12b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:13b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:14b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:15b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:16b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:17b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:18b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:19b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:20b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:21b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:22b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:23b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:24b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:25b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag()
								+ "]}},{Slot:26b,id:\"minecraft:writable_book\",Count:16b,tag:{display:{Name:'{\"text\":\"BLEACHHACK OWNS ALL\",\"color\":\"dark_red\",\"bold\":true}'},pages:["
								+ getBookbanTag() + "]}}]}}");
			}
			else if (args[1].equalsIgnoreCase("test")) {
				String s = "";
				String s1 = "";
				Random r = new Random();

				for (int i = 0; i < 500; i++)
					s1 += ",{Type:0}";

				for (int i = 0; i < 100; i++)
					s += "," + r.nextInt(16777215);

				tag = StringNbtReader.parse("{BlockEntityTag:{Items:[{Slot:0b,id:\"minecraft:firework_rocket\",Count:1b,tag:{Fireworks:{Explosions:[{Type:0}" + s1
						+ "]}}},{Slot:1b,id:\"minecraft:firework_rocket\",Count:64b,tag:{Fireworks:{Flight:127b,Explosions:[{Type:0,Flicker:1b,Trail:1b,Colors:[I;16711680],FadeColors:[I;16711680"
						+ s + "]},{Type:1,Flicker:1b,Trail:1b,Colors:[I;16711680],FadeColors:[I;16711680" + s + "]},{Type:2,Flicker:1b,Trail:1b,Colors:[I;16711680" + s
						+ "],FadeColors:[I;16711680" + s + "]},{Type:3,Flicker:1b,Trail:1b,Colors:[I;16711680" + s + "],FadeColors:[I;16711680" + s
						+ "]},{Type:4,Flicker:1b,Trail:1b,Colors:[I;16711680" + s + "],FadeColors:[I;16711680" + s
						+ "]}]}}},{Slot:2b,id:\"minecraft:lingering_potion\",Count:64b,tag:{display:{Name:'{\"text\":\"Bleach\\'s B R U H M O M E N T Potion\",\"color\":\"aqua\",\"bold\":true,\"italic\":true}'},AttributeModifiers:[{AttributeName:\"generic.maxHealth\",Name:\"generic.maxHealth\",Amount:1,Operation:0,UUIDLeast:338793,UUIDMost:213301}],CustomPotionEffects:[{Id:1b,Amplifier:127b,Duration:32767},{Id:2b,Amplifier:127b,Duration:32767},{Id:3b,Amplifier:127b,Duration:32767},{Id:4b,Amplifier:127b,Duration:32767},{Id:5b,Amplifier:127b,Duration:32767},{Id:6b,Amplifier:127b,Duration:32767},{Id:7b,Amplifier:127b,Duration:32767},{Id:8b,Amplifier:127b,Duration:32767},{Id:9b,Amplifier:127b,Duration:32767},{Id:10b,Amplifier:127b,Duration:32767},{Id:11b,Amplifier:127b,Duration:32767},{Id:12b,Amplifier:127b,Duration:32767},{Id:13b,Amplifier:127b,Duration:32767},{Id:14b,Amplifier:127b,Duration:32767},{Id:15b,Amplifier:127b,Duration:32767},{Id:16b,Amplifier:127b,Duration:32767},{Id:17b,Amplifier:127b,Duration:32767},{Id:18b,Amplifier:127b,Duration:32767},{Id:19b,Amplifier:127b,Duration:32767},{Id:20b,Amplifier:127b,Duration:32767},{Id:21b,Amplifier:127b,Duration:32767},{Id:22b,Amplifier:127b,Duration:32767},{Id:23b,Amplifier:127b,Duration:32767},{Id:24b,Amplifier:127b,Duration:32767},{Id:25b,Amplifier:127b,Duration:32767},{Id:26b,Amplifier:127b,Duration:32767},{Id:27b,Amplifier:127b,Duration:32767},{Id:28b,Amplifier:127b,Duration:32767},{Id:29b,Amplifier:127b,Duration:32767},{Id:30b,Amplifier:127b,Duration:32767},{Id:31b,Amplifier:127b,Duration:32767}],Potion:\"minecraft:leaping\"}},{Slot:3b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:13b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:13b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:13b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:13b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:13b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:13b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:13b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:13b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:13b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:13b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:13b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:13b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:13b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:13b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:13b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:13b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:13b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:13b,id:\"minecraft:pink_shulker_box\",Count:64b,tag:{display:{Name:'{\"text\":\"nested shulker boxes\",\"color\":\"red\",\"italic\":true,\"underlined\":true}'}}}]}}}]}}}]}}}]}}}]}}}]}}}]}}}]}}}]}}}]}}}]}}}]}}}]}}}]}}}]}}}]}}}]}}}]}}},{Slot:4b,id:\"minecraft:chicken_spawn_egg\",Count:1b,tag:{EntityTag:{id:\"minecraft:spawner_minecart\",CustomDisplayTile:1b,Delay:1,MinSpawnDelay:0,MaxSpawnDelay:0,MaxNearbyEntities:1000,RequiredPlayerRange:100,DisplayState:{Name:\"minecraft:acacia_door\",Properties:{half:\"upper\",hinge:\"left\",open:\"true\"}},SpawnData:{id:\"minecraft:minecart\"}}}}]}}");
			}
			else if (args[1].equalsIgnoreCase("eggs")) {
				tag = StringNbtReader.parse(
						"{display:{Name:'{\"text\":\"Bleach\\'s Spawn Eggs\",\"color\":\"aqua\",\"bold\":true,\"italic\":true,\"underlined\":true}'},BlockEntityTag:{Items:[{Slot:0b,id:\"minecraft:zombie_spawn_egg\",Count:1b,tag:{display:{Name:'{\"text\":\"Spawn Giant\",\"color\":\"aqua\"}'},EntityTag:{id:\"minecraft:giant\",Invulnerable:1b,Glowing:1b}}},{Slot:1b,id:\"minecraft:enderman_spawn_egg\",Count:1b,tag:{display:{Name:'{\"text\":\"Enderman With Cmd block\",\"color\":\"aqua\"}'},EntityTag:{carriedBlockState:{Name:\"minecraft:command_block\",Properties:{conditional:\"true\"}}}}},{Slot:2b,id:\"minecraft:bat_spawn_egg\",Count:1b,tag:{display:{Name:'{\"text\":\"Cmd minecart (kill @a)\",\"color\":\"aqua\"}'},EntityTag:{id:\"minecraft:command_block_minecart\",Command:\"kill @a\"}}},{Slot:3b,id:\"minecraft:bat_spawn_egg\",Count:1b,tag:{display:{Name:'{\"text\":\"Spawner minecart (turn particles off)\",\"color\":\"aqua\"}'},EntityTag:{id:\"minecraft:spawner_minecart\",SpawnData:{id:\"minecraft:armor_stand\"}}}},{Slot:4b,id:\"minecraft:cave_spider_spawn_egg\",Count:1b,tag:{display:{Name:'{\"text\":\"E G G\",\"color\":\"aqua\"}'},EntityTag:{id:\"minecraft:egg\",NoGravity:1b,Fire:2100000000,Glowing:1b}}},{Slot:5b,id:\"minecraft:stray_spawn_egg\",Count:1b,tag:{display:{Name:'{\"text\":\"area_effect_cloud (50 range)\",\"color\":\"aqua\"}'},EntityTag:{id:\"minecraft:area_effect_cloud\",Particle:\"angry_villager\",ReapplicationDelay:1,Radius:50f,RadiusPerTick:0f,RadiusOnUse:0f,Duration:500000,DurationOnUse:0f,Color:16711680,Potion:\"minecraft:strong_swiftness\"}}},{Slot:6b,id:\"minecraft:evoker_spawn_egg\",Count:1b,tag:{display:{Name:'{\"text\":\"area_effect_cloud (E X P A N D)\",\"color\":\"aqua\"}'},EntityTag:{id:\"minecraft:area_effect_cloud\",Particle:\"angry_villager\",Radius:1f,RadiusPerTick:10f,RadiusOnUse:1f,Duration:10000}}},{Slot:7b,id:\"minecraft:elder_guardian_spawn_egg\",Count:1b,tag:{display:{Name:'{\"text\":\"Arrow (End Portal Sound)\",\"color\":\"aqua\"}'},EntityTag:{id:\"minecraft:arrow\",pickup:1b,SoundEvent:\"block.end_portal.spawn\"}}},{Slot:8b,id:\"minecraft:elder_guardian_spawn_egg\",Count:1b,tag:{display:{Name:'{\"text\":\"Arrow (EG Curse Sound)\",\"color\":\"aqua\"}'},EntityTag:{id:\"minecraft:arrow\",pickup:1b,SoundEvent:\"entity.elder_guardian.curse\"}}},{Slot:9b,id:\"minecraft:drowned_spawn_egg\",Count:1b,tag:{display:{Name:'{\"text\":\"Big chungus slime\",\"color\":\"aqua\"}'},EntityTag:{id:\"minecraft:slime\",Size:50}}},{Slot:10b,id:\"minecraft:fox_spawn_egg\",Count:1b,tag:{display:{Name:'{\"text\":\"Invis Armor Stand\",\"color\":\"aqua\"}'},EntityTag:{id:\"minecraft:armor_stand\",Invulnerable:1b,Invisible:1b,PersistenceRequired:1b,ArmorItems:[{},{},{},{id:\"minecraft:spawner\",Count:1b}]}}},{Slot:11b,id:\"minecraft:ghast_spawn_egg\",Count:1b,tag:{display:{Name:'{\"text\":\"Enderdragon\",\"color\":\"aqua\"}'},EntityTag:{id:\"minecraft:ender_dragon\",DragonPhase:8}}},{Slot:12b,id:\"minecraft:cow_spawn_egg\",Count:1b,tag:{display:{Name:'{\"text\":\"Lightning\",\"color\":\"aqua\"}'},EntityTag:{id:\"minecraft:lightning_bolt\"}}},{Slot:13b,id:\"minecraft:guardian_spawn_egg\",Count:1b,tag:{EntityTag:{id:\"minecraft:iron_golem\"}}},{Slot:14b,id:\"minecraft:evoker_spawn_egg\",Count:1b,tag:{display:{Name:'{\"text\":\"area_effect_cloud (expand slow)\",\"color\":\"aqua\"}'},EntityTag:{id:\"minecraft:area_effect_cloud\",Particle:\"angry_villager\",Radius:1f,RadiusPerTick:10f,RadiusOnUse:1f,Duration:1000000}}},{Slot:15b,id:\"minecraft:bee_spawn_egg\",Count:1b,tag:{display:{Name:'{\"text\":\"Tnt Minecart\",\"color\":\"aqua\"}'},EntityTag:{id:\"minecraft:tnt_minecart\",TNTFuse:1000000}}},{Slot:16b,id:\"minecraft:bat_spawn_egg\",Count:1b,tag:{display:{Name:'{\"text\":\"Invalid translate name test\",\"color\":\"aqua\"}'},EntityTag:{id:\"minecraft:boat\",CustomNameVisible:1b,Type:\"acacia\",CustomName:'{\"translate\":\"translation.test.invalid\"}'}}}]}}");
			} else {
				BleachLogger.error("Invalid preset!");
				return;
			}

			if (args2.equalsIgnoreCase("egg")) {
				NbtCompound ct = new NbtCompound();
				ct.put("EntityTag", StringNbtReader.parse("{Time:1,id:\"minecraft:falling_block\",BlockState:{Name:\"minecraft:chest\"}}"));
				((NbtCompound) ct.get("EntityTag")).put("TileEntityData", tag.get("BlockEntityTag"));

				item.setNbt(ct);
			} else {
				item.setNbt(tag);
			}

			mc.player.getInventory().insertStack(item);
			return;
		}

		ItemStack item = new ItemStack(
				Registry.ITEM.get(new Identifier("minecraft:" + args[0].toLowerCase(Locale.ENGLISH))));

		if (item.getItem() instanceof AirBlockItem)
			throw new CmdSyntaxException();

		if (args.length >= 2 && NumberUtils.isCreatable(args[1]))
			item.setCount(NumberUtils.createNumber(args[1]).intValue());
		if (args.length >= 3 && NumberUtils.isCreatable(args[2]))
			item.setDamage(NumberUtils.createNumber(args[2]).intValue());
		if (args.length >= 4)
			try {
				item.setNbt(StringNbtReader.parse(args[3]));
			} catch (CommandSyntaxException ignored) {
			}

		mc.player.getInventory().insertStack(item);
	}

	private String getBookbanTag() {
		String book = "";
		String page = "";
		for (int i = 0; i < 100; i++)
			page += "\uffff";
		for (int i = 0; i < 99; i++)
			book += "\"" + page + "\",";
		book += "\"" + page + "\"";

		return book;
	}
}

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

import java.util.Locale;

import org.apache.commons.lang3.math.NumberUtils;

import bleach.hack.command.Command;
import bleach.hack.util.BleachLogger;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.registry.Registry;

public class CmdEnchant extends Command {

	@Override
	public String getAlias() {
		return "enchant";
	}

	@Override
	public String getDescription() {
		return "Enchants an item";
	}

	@Override
	public String getSyntax() {
		return "enchant <enchant/id> <level> | enchant all <level> | enchant list";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if (args[0].equalsIgnoreCase("list")) {
			BleachLogger.infoMessage("\u00a7d[Aqua_Affinity/Aqua] \u00a75[Arthropods] \u00a7d[Blast/Blast_Prot] "
					+ "\u00a75[Channeling] \u00a7d[Curse_Binding/Binding] \u00a75[Curse_Vanish/Vanish] \u00a7d[Depth_Strider/Strider] "
					+ "\u00a75[Efficiency/Eff] \u00a7d[Feather_Falling/Fall] \u00a75[Fire_Aspect] \u00a7d[Fire_Prot] "
					+ "\u00a75[Flame] \u00a7d[Fortune] \u00a75[Frost_Walker/Frost] \u00a7d[Impaling] \u00a75[Infinity] \u00a7d[Knockback/Knock] "
					+ "\u00a75[Looting/Loot] \u00a7d[Loyalty] \u00a75[Luck_Of_The_Sea/Luck] \u00a7d[Lure] \u00a75[Mending/Mend] \u00a7d[Multishot] "
					+ "\u00a75[Piercing] \u00a7d[Power] \u00a75[Projectile_Prot/Proj_Prot] \u00a7d[Protection/Prot] "
					+ "\u00a75[Punch] \u00a7d[Quick_Charge/Charge] \u00a75[Respiration/Resp] \u00a7d[Riptide] "
					+ "\u00a75[Sharpness/Sharp] \u00a7d[Silk_Touch/Silk] \u00a75[Smite] \u007d[Soul_Speed/Soul] "
					+ "\u00a75[Sweeping_Edge/Sweep] \u00a7d[Thorns] \u00a75[Unbreaking]");
			return;
		}

		if (!mc.player.abilities.creativeMode) {
			printSyntaxError("Not In Creative Mode!");
			return;
		}

		int level = Integer.parseInt(args[1]);
		ItemStack item = mc.player.inventory.getMainHandStack();

		if (args[0].equalsIgnoreCase("all")) {
			for (Enchantment e : Registry.ENCHANTMENT) {
				enchant(item, e, level);
			}
			
			return;
		}
		
		int i = NumberUtils.toInt(args[0], -1);
		
		if (i != -1) {
			enchant(item, Enchantment.byRawId(i), level);
		} else {
			enchant(item, fromString(args[0]), level);
		}
	}
	
	public Enchantment fromString(String s) {
		// programming
		switch (s.toLowerCase(Locale.ENGLISH)) {
			case "aqua_affinity":
			case "aqua":
				return Enchantments.AQUA_AFFINITY;
			case "arthropods":
				return Enchantments.BANE_OF_ARTHROPODS;
			case "blast":
			case "blast_prot":
				return Enchantments.BLAST_PROTECTION;
			case "channeling":
				return Enchantments.CHANNELING;
			case "curse_binding":
			case "binding":
				return Enchantments.BINDING_CURSE;
			case "curse_vanish":
			case "vanish":
				return Enchantments.VANISHING_CURSE;
			case "depth_strider":
			case "strider":
				return Enchantments.DEPTH_STRIDER;
			case "efficiency":
			case "eff":
				return Enchantments.EFFICIENCY;
			case "feather_falling":
			case "fall":
				return Enchantments.FEATHER_FALLING;
			case "fire_aspect":
				return Enchantments.FIRE_ASPECT;
			case "fire_prot":
				return Enchantments.FIRE_PROTECTION;
			case "flame":
				return Enchantments.FLAME;
			case "fortune":
				return Enchantments.FORTUNE;
			case "frost_walker":
			case "frost":
				return Enchantments.FROST_WALKER;
			case "impaling":
				return Enchantments.IMPALING;
			case "infinity":
				return Enchantments.INFINITY;
			case "knockback":
			case "knock":
				return Enchantments.KNOCKBACK;
			case "looting":
			case "loot":
				return Enchantments.LOOTING;
			case "loyalty":
				return Enchantments.LOYALTY;
			case "luck_of_the_sea":
			case "luck":
				return Enchantments.LUCK_OF_THE_SEA;
			case "lure":
				return Enchantments.LURE;
			case "mending":
			case "mend":
				return Enchantments.MENDING;
			case "multishot":
				return Enchantments.MULTISHOT;
			case "piercing":
				return Enchantments.PIERCING;
			case "power":
				return Enchantments.POWER;
			case "projectile_prot":
			case "proj_prot":
				return Enchantments.PROJECTILE_PROTECTION;
			case "protection":
			case "prot":
				return Enchantments.PROTECTION;
			case "punch":
				return Enchantments.PUNCH;
			case "quick_charge":
			case "charge":
				return Enchantments.QUICK_CHARGE;
			case "respiration":
			case "resp":
				return Enchantments.RESPIRATION;
			case "riptide":
				return Enchantments.RIPTIDE;
			case "sharpness":
			case "sharp":
				return Enchantments.SHARPNESS;
			case "silk_touch":
			case "silk":
				return Enchantments.SILK_TOUCH;
			case "smite":
				return Enchantments.SMITE;
			case "soul_speed":
			case "soul":
				return Enchantments.SOUL_SPEED;
			case "sweeping_edge":
			case "sweep":
				return Enchantments.SWEEPING;
			case "thorns":
				return Enchantments.THORNS;
			case "unbreaking":
				return Enchantments.UNBREAKING;
			default:
				return null;
		}
	}

	public void enchant(ItemStack item, Enchantment e, int level) {
		if (e == null) {
			printSyntaxError("Invalid enchantment!");
			return;
		}
		
		if (item.getTag() == null)
			item.setTag(new CompoundTag());
		if (!item.getTag().contains("Enchantments", 9)) {
			item.getTag().put("Enchantments", new ListTag());
		}

		ListTag listnbt = item.getTag().getList("Enchantments", 10);
		CompoundTag compoundnbt = new CompoundTag();
		compoundnbt.putString("id", String.valueOf(Registry.ENCHANTMENT.getId(e)));
		compoundnbt.putInt("lvl", level);
		listnbt.add(compoundnbt);
	}

}

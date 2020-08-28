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
import bleach.hack.utils.BleachLogger;
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
		return "enchant [enchant] [level] / enchant all [level] / enchant list";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if (args[0].equalsIgnoreCase("list")) {
			BleachLogger.infoMessage("\00a7d[Aqua_Affinity/Aqua] \00a75[Arthropods] \00a7d[Blast/Blast_Prot] "
					+ "\00a75[Channeling] \00a7d[Curse_Binding/Binding] \00a75[Curse_Vanish/Vanish] \00a7d[Depth_Strider/Strider] "
					+ "\00a75[Efficiency/Eff] \00a7d[Feather_Falling/Fall] \00a75[Fire_Aspect] \00a7d[Fire_Prot] "
					+ "\00a75[Flame] \00a7d[Fortune] \00a75[Frost_Walker/Frost] \00a7d[Impaling] \00a75[Infinity] \00a7d[Knockback/Knock] "
					+ "\00a75[Looting/Loot] \00a7d[Loyalty] \00a75[Luck_Of_The_Sea/Luck] \00a7d[Lure] \00a75[Mending/Mend] \00a7d[Multishot] "
					+ "\00a75[Piercing] \00a7d[Power] \00a75[Projectile_Prot/Proj_Prot] \00a7d[Protection/Prot] "
					+ "\00a75[Punch] \00a7d[Quick_Charge/Charge] \00a75[Respiration/Resp] \00a7d[Riptide] "
					+ "\00a75[Sharpness/Sharp] \00a7d[Silk_Touch/Silk] \00a75[Smite] \00a7d[Sweeping_Edge/Sweep] \00a75[Thorns] \00a7d[Unbreaking]");
			return;
		}

		if (!mc.player.abilities.creativeMode) {
			BleachLogger.errorMessage("Not In Creative Mode!");
			return;
		}

		int level = Integer.parseInt(args[1]);
		ItemStack item = mc.player.inventory.getMainHandStack();

		if (args[0].equalsIgnoreCase("all")) {
			for (Enchantment e : Registry.ENCHANTMENT) {
				enchant(item, e, level);
			}
		}

		if (equals(args[0], "Aqua_Affinity", "Aqua"))
			enchant(item, Enchantments.AQUA_AFFINITY, level);
		if (equals(args[0], "Arthropods"))
			enchant(item, Enchantments.BANE_OF_ARTHROPODS, level);
		if (equals(args[0], "Blast", "Blast_Prot"))
			enchant(item, Enchantments.BLAST_PROTECTION, level);
		if (equals(args[0], "Channeling"))
			enchant(item, Enchantments.CHANNELING, level);
		if (equals(args[0], "Curse_Binding", "Binding"))
			enchant(item, Enchantments.BINDING_CURSE, level);
		if (equals(args[0], "Curse_Vanish", "Vanish"))
			enchant(item, Enchantments.VANISHING_CURSE, level);
		if (equals(args[0], "Depth_Strider", "Strider"))
			enchant(item, Enchantments.DEPTH_STRIDER, level);
		if (equals(args[0], "Efficiency", "Eff"))
			enchant(item, Enchantments.EFFICIENCY, level);
		if (equals(args[0], "Feather_Falling", "Fall"))
			enchant(item, Enchantments.FEATHER_FALLING, level);
		if (equals(args[0], "Fire_Aspect"))
			enchant(item, Enchantments.FIRE_ASPECT, level);
		if (equals(args[0], "Fire_Prot"))
			enchant(item, Enchantments.FIRE_PROTECTION, level);
		if (equals(args[0], "Flame"))
			enchant(item, Enchantments.FLAME, level);
		if (equals(args[0], "Fortune"))
			enchant(item, Enchantments.FORTUNE, level);
		if (equals(args[0], "Frost_Walker", "Frost"))
			enchant(item, Enchantments.FROST_WALKER, level);
		if (equals(args[0], "Impaling"))
			enchant(item, Enchantments.IMPALING, level);
		if (equals(args[0], "Infinity"))
			enchant(item, Enchantments.INFINITY, level);
		if (equals(args[0], "Knockback", "Knock"))
			enchant(item, Enchantments.KNOCKBACK, level);
		if (equals(args[0], "Looting", "Loot"))
			enchant(item, Enchantments.LOOTING, level);
		if (equals(args[0], "Loyalty"))
			enchant(item, Enchantments.LOYALTY, level);
		if (equals(args[0], "Luck_Of_The_Sea", "Luck"))
			enchant(item, Enchantments.LUCK_OF_THE_SEA, level);
		if (equals(args[0], "Lure"))
			enchant(item, Enchantments.LURE, level);
		if (equals(args[0], "Mending", "Mend"))
			enchant(item, Enchantments.MENDING, level);
		if (equals(args[0], "Multishot"))
			enchant(item, Enchantments.MULTISHOT, level);
		if (equals(args[0], "Piercing"))
			enchant(item, Enchantments.PIERCING, level);
		if (equals(args[0], "Power"))
			enchant(item, Enchantments.POWER, level);
		if (equals(args[0], "Projectile_Prot", "Proj_Prot"))
			enchant(item, Enchantments.PROJECTILE_PROTECTION, level);
		if (equals(args[0], "Protection", "Prot"))
			enchant(item, Enchantments.PROTECTION, level);
		if (equals(args[0], "Punch"))
			enchant(item, Enchantments.PUNCH, level);
		if (equals(args[0], "Quick_Charge", "Charge"))
			enchant(item, Enchantments.QUICK_CHARGE, level);
		if (equals(args[0], "Respiration", "Resp"))
			enchant(item, Enchantments.RESPIRATION, level);
		if (equals(args[0], "Riptide"))
			enchant(item, Enchantments.RIPTIDE, level);
		if (equals(args[0], "Sharpness", "Sharp"))
			enchant(item, Enchantments.SHARPNESS, level);
		if (equals(args[0], "Silk_Touch", "Silk"))
			enchant(item, Enchantments.SILK_TOUCH, level);
		if (equals(args[0], "Smite"))
			enchant(item, Enchantments.SMITE, level);
		if (equals(args[0], "Sweeping_Edge", "Sweep"))
			enchant(item, Enchantments.SWEEPING, level);
		if (equals(args[0], "Thorns"))
			enchant(item, Enchantments.THORNS, level);
		if (equals(args[0], "Unbreaking"))
			enchant(item, Enchantments.UNBREAKING, level);
	}

	public boolean equals(String a, String... b) {
		for (String element : b) {
			if (a.equalsIgnoreCase(element))
				return true;
		}
		return false;
	}

	public void enchant(ItemStack item, Enchantment e, int level) {
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

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
        return "enchant [enchant] [level] | enchant all [level] | enchant list";
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
                    + "\u00a75[Sharpness/Sharp] \u00a7d[Silk_Touch/Silk] \u00a75[Smite] \u00a7d[Sweeping_Edge/Sweep] \u00a75[Thorns] \u00a7d[Unbreaking]");
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

        if (doesEqual(args[0], "Aqua_Affinity", "Aqua")) enchant(item, Enchantments.AQUA_AFFINITY, level);
        if (doesEqual(args[0], "Arthropods")) enchant(item, Enchantments.BANE_OF_ARTHROPODS, level);
        if (doesEqual(args[0], "Blast", "Blast_Prot")) enchant(item, Enchantments.BLAST_PROTECTION, level);
        if (doesEqual(args[0], "Channeling")) enchant(item, Enchantments.CHANNELING, level);
        if (doesEqual(args[0], "Curse_Binding", "Binding")) enchant(item, Enchantments.BINDING_CURSE, level);
        if (doesEqual(args[0], "Curse_Vanish", "Vanish")) enchant(item, Enchantments.VANISHING_CURSE, level);
        if (doesEqual(args[0], "Depth_Strider", "Strider")) enchant(item, Enchantments.DEPTH_STRIDER, level);
        if (doesEqual(args[0], "Efficiency", "Eff")) enchant(item, Enchantments.EFFICIENCY, level);
        if (doesEqual(args[0], "Feather_Falling", "Fall")) enchant(item, Enchantments.FEATHER_FALLING, level);
        if (doesEqual(args[0], "Fire_Aspect")) enchant(item, Enchantments.FIRE_ASPECT, level);
        if (doesEqual(args[0], "Fire_Prot")) enchant(item, Enchantments.FIRE_PROTECTION, level);
        if (doesEqual(args[0], "Flame")) enchant(item, Enchantments.FLAME, level);
        if (doesEqual(args[0], "Fortune")) enchant(item, Enchantments.FORTUNE, level);
        if (doesEqual(args[0], "Frost_Walker", "Frost")) enchant(item, Enchantments.FROST_WALKER, level);
        if (doesEqual(args[0], "Impaling")) enchant(item, Enchantments.IMPALING, level);
        if (doesEqual(args[0], "Infinity")) enchant(item, Enchantments.INFINITY, level);
        if (doesEqual(args[0], "Knockback", "Knock")) enchant(item, Enchantments.KNOCKBACK, level);
        if (doesEqual(args[0], "Looting", "Loot")) enchant(item, Enchantments.LOOTING, level);
        if (doesEqual(args[0], "Loyalty")) enchant(item, Enchantments.LOYALTY, level);
        if (doesEqual(args[0], "Luck_Of_The_Sea", "Luck")) enchant(item, Enchantments.LUCK_OF_THE_SEA, level);
        if (doesEqual(args[0], "Lure")) enchant(item, Enchantments.LURE, level);
        if (doesEqual(args[0], "Mending", "Mend")) enchant(item, Enchantments.MENDING, level);
        if (doesEqual(args[0], "Multishot")) enchant(item, Enchantments.MULTISHOT, level);
        if (doesEqual(args[0], "Piercing")) enchant(item, Enchantments.PIERCING, level);
        if (doesEqual(args[0], "Power")) enchant(item, Enchantments.POWER, level);
        if (doesEqual(args[0], "Projectile_Prot", "Proj_Prot"))
            enchant(item, Enchantments.PROJECTILE_PROTECTION, level);
        if (doesEqual(args[0], "Protection", "Prot")) enchant(item, Enchantments.PROTECTION, level);
        if (doesEqual(args[0], "Punch")) enchant(item, Enchantments.PUNCH, level);
        if (doesEqual(args[0], "Quick_Charge", "Charge")) enchant(item, Enchantments.QUICK_CHARGE, level);
        if (doesEqual(args[0], "Respiration", "Resp")) enchant(item, Enchantments.RESPIRATION, level);
        if (doesEqual(args[0], "Riptide")) enchant(item, Enchantments.RIPTIDE, level);
        if (doesEqual(args[0], "Sharpness", "Sharp")) enchant(item, Enchantments.SHARPNESS, level);
        if (doesEqual(args[0], "Silk_Touch", "Silk")) enchant(item, Enchantments.SILK_TOUCH, level);
        if (doesEqual(args[0], "Smite")) enchant(item, Enchantments.SMITE, level);
        if (doesEqual(args[0], "Sweeping_Edge", "Sweep")) enchant(item, Enchantments.SWEEPING, level);
        if (doesEqual(args[0], "Thorns")) enchant(item, Enchantments.THORNS, level);
        if (doesEqual(args[0], "Unbreaking")) enchant(item, Enchantments.UNBREAKING, level);
    }

    public boolean doesEqual(String a, String... b) {
        for (int i = 0; i < b.length; i++) {
            if (a.equalsIgnoreCase(b[i])) return true;
        }
        return false;
    }

    public void enchant(ItemStack item, Enchantment e, int level) {
        if (item.getTag() == null) item.setTag(new CompoundTag());
        if (!item.getTag().contains("Enchantments", 9)) {
            item.getTag().put("Enchantments", new ListTag());
        }

        ListTag listnbt = item.getTag().getList("Enchantments", 10);
        CompoundTag compoundnbt = new CompoundTag();
        compoundnbt.putString("id", String.valueOf(Registry.ENCHANTMENT.getRawId(e)));
        compoundnbt.putInt("lvl", level);
        listnbt.add(compoundnbt);
    }

}

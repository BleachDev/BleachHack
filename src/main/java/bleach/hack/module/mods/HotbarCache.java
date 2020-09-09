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
package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Items;

public class HotbarCache extends Module {

    public HotbarCache() {
        super("HotbarCache", KEY_UNBOUND, Category.MISC, "Autototem for items",
                new SettingMode("Item: ", "Pickaxe", "Crystal", "Gapple"));
    }

    @Subscribe
    public void onTick(EventTick event) {
        int mode = getSettings().get(0).asMode().mode;
        switch (mode) {
            case 0:
                /* Inventory */
                if (mc.player.inventory.getMainHandStack().isEmpty() || mc.player.inventory.getMainHandStack().getItem() != Items.DIAMOND_PICKAXE || mc.player.inventory.getMainHandStack().getItem() != Items.NETHERITE_PICKAXE) {
                    for (int i = 0; i < 9; i++) {
                        if (mc.player.inventory.getStack(i).getItem() == Items.DIAMOND_PICKAXE || mc.player.inventory.getStack(i).getItem() == Items.NETHERITE_PICKAXE) {
                            mc.player.inventory.selectedSlot = i;
//                            mc.player.inventory.swapSlotWithHotbar(i);
                            return;
                        }
                    }
                }
                break;

            case 1:
                /* Inventory */
                if (mc.player.inventory.getStack(0).isEmpty() || mc.player.inventory.getStack(0).getItem() != Items.END_CRYSTAL) {
                    for (int i = 0; i < 9; i++) {
                        if (mc.player.inventory.getStack(i).getItem() == Items.END_CRYSTAL) {
                            mc.player.inventory.selectedSlot = i;
                            return;
                        }
                    }
                }
                break;
            case 2:

                /* Inventory */
                if (mc.player.inventory.getStack(0).isEmpty() || mc.player.inventory.getStack(0).getItem() != Items.ENCHANTED_GOLDEN_APPLE) {
                    for (int i = 0; i < 9; i++) {
                        if (mc.player.inventory.getStack(i).getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                            mc.player.inventory.selectedSlot = i;
                            return;
                        }
                    }
                }
                break;
        }
    }
}

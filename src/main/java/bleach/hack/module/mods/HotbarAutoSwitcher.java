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
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.utils.Timer;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;

public class HotbarAutoSwitcher extends Module {

    public HotbarAutoSwitcher() {
        super("HotbarAutoSwitcher", KEY_UNBOUND, Category.MISC, "Autototem for items",
                new SettingMode("Item", "Pickaxe", "Crystal", "Gapple"),
                new SettingMode("Mode", "Switch", "Pull", "Refill"),
                new SettingSlider("Delay", 0.0, 1.0, 0.003, 3));
    }

    private final ArrayList<Item> Hotbar = new ArrayList<Item>();
    private final Timer timer = new Timer();

    @Subscribe
    public void onTick(EventTick event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (getSettings().get(1).asMode().mode == 0) {
            int mode = getSettings().get(0).asMode().mode;
            switch (mode) {
                case 0:
                    /* Inventory */
                    if (player.inventory.getMainHandStack().isEmpty() || player.inventory.getMainHandStack().getItem() != Items.DIAMOND_PICKAXE || player.inventory.getMainHandStack().getItem() != Items.NETHERITE_PICKAXE) {
                        for (int i = 0; i < 9; i++) {
                            if (player.inventory.getStack(i).getItem() == Items.DIAMOND_PICKAXE || player.inventory.getStack(i).getItem() == Items.NETHERITE_PICKAXE) {
                                player.inventory.selectedSlot = i;
                                //                            player.inventory.swapSlotWithHotbar(i);
                                return;
                            }
                        }
                    }
                    break;

                case 1:
                    /* Inventory */
                    if (player.inventory.getStack(0).isEmpty() || player.inventory.getStack(0).getItem() != Items.END_CRYSTAL) {
                        for (int i = 0; i < 9; i++) {
                            if (player.inventory.getStack(i).getItem() == Items.END_CRYSTAL) {
                                player.inventory.selectedSlot = i;
                                return;
                            }
                        }
                    }
                    break;
                case 2:

                    /* Inventory */
                    if (player.inventory.getStack(0).isEmpty() || player.inventory.getStack(0).getItem() != Items.ENCHANTED_GOLDEN_APPLE) {
                        for (int i = 0; i < 9; i++) {
                            if (player.inventory.getStack(i).getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                                player.inventory.selectedSlot = i;
                                return;
                            }
                        }
                    }
                    break;
                case 3:

                    /* Inventory */
                    if (mc.player.inventory.getStack(0).isEmpty() || mc.player.inventory.getStack(0).getItem() != Items.SNOWBALL) {
                        for (int i = 0; i < 9; i++) {
                            if (mc.player.inventory.getStack(i).getItem() == Items.SNOWBALL) {
                                mc.player.inventory.selectedSlot = i;
                                return;
                            }
                        }
                    }
                    break;
            }
        }
    }
}

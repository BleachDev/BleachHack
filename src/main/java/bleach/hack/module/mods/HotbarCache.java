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
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.Timer;
import bleach.hack.utils.Wrapper;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;

public class HotbarCache extends Module {

    public HotbarCache() {
        super("HotbarCache", KEY_UNBOUND, Category.MISC, "Autototem for items",
        Hotbar.clear();

        for (int l_I = 0; l_I < 9; ++l_I)
        {

            if (!l_Stack.isEmpty() && !Hotbar.contains(l_Stack.getItem()))
                Hotbar.add(l_Stack.getItem());
            else
                Hotbar.add(Items.AIR);
        }
    }

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
            }
        }
                }
            }
        }
    }
    private boolean SwitchSlotIfNeed(int p_Slot)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        Item l_Item = Hotbar.get(p_Slot);

        if (l_Item == Items.AIR)
            return false;

        if (!player.inventory.getStack(p_Slot).isEmpty() && player.inventory.getStack(p_Slot).getItem() == l_Item)
            return false;

        int l_Slot = GetItemSlot(l_Item);

        if (l_Slot != -1 && l_Slot != 45)
        {
            mc.interactionManager.clickSlot(player.currentScreenHandler.syncId, l_Slot, 0,
                    SlotActionType.PICKUP, player);
            mc.interactionManager.clickSlot(player.currentScreenHandler.syncId, p_Slot+36, 0, SlotActionType.PICKUP,
                    player);
            mc.interactionManager.clickSlot(player.currentScreenHandler.syncId, l_Slot, 0,
                    SlotActionType.PICKUP, player);
            return true;
        }

        return false;
    }

    public int GetItemSlot(Item input)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (player == null)
            return 0;

        for (int i = 0; i < player.inventory.size(); ++i)
        {
            if (i == 0 || i == 5 || i == 6 || i == 7 || i == 8)
                continue;

            ItemStack s = player.inventory.getStack(i);

            if (s.isEmpty())
                continue;

            if (s.getItem() == input)
            {
                return i;
            }
        }
        return -1;
    }

    private boolean RefillSlotIfNeed(int p_Slot)
    {
        ItemStack l_Stack = mc.player.inventory.getStack(p_Slot);

        if (l_Stack.isEmpty() || l_Stack.getItem() == Items.AIR)
            return false;

        if (!l_Stack.isStackable())
            return false;

        if (l_Stack.getCount() >= l_Stack.getMaxCount())
            return false;

        /// We're going to search the entire inventory for the same stack, WITH THE SAME NAME, and use quick move.
        for (int l_I = 9; l_I < 36; ++l_I)
        {
            final ItemStack l_Item = mc.player.inventory.getStack(l_I);

            if (l_Item.isEmpty())
                continue;

            if (CanItemBeMergedWith(l_Stack, l_Item))
            {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, l_I, 0,
                        SlotActionType.QUICK_MOVE, mc.player);
                /// Check again for more next available tick
                return true;
            }
        }

        return false;
    }

    private boolean CanItemBeMergedWith(ItemStack p_Source, ItemStack p_Target)
    {
        return p_Source.getItem() == p_Target.getItem() && p_Source.getName().equals(p_Target.getName());
    }

}
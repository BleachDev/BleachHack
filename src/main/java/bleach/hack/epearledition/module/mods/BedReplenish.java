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
package bleach.hack.epearledition.module.mods;

import bleach.hack.epearledition.event.events.EventTick;
import bleach.hack.epearledition.module.Category;
import bleach.hack.epearledition.module.Module;
import bleach.hack.epearledition.setting.base.SettingMode;
import bleach.hack.epearledition.setting.base.SettingSlider;
import bleach.hack.epearledition.setting.base.SettingToggle;
import bleach.hack.epearledition.utils.InvUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.BedItem;
import net.minecraft.item.Items;
import net.minecraft.item.SnowballItem;
import net.minecraft.screen.slot.SlotActionType;

public class BedReplenish extends Module {

    public BedReplenish() {
        super("BedReplenish", KEY_UNBOUND, Category.COMBAT, "Automatically equips beds.",
                new SettingToggle("Override", true).withDesc("Equips a bed even if theres another item in the offhand"),
                new SettingSlider("Slot: ", 0, 8, 8, 0),
                new SettingMode("Item: ", "Bed", "Snowball", "Packed Ice"));
    }
    //NO WAY EPEARL WROTE OUT EVERY BED COLOR :BRUH:
    @Subscribe
    public void onTick(EventTick event) {
        int slot = (int) getSetting(1).asSlider().getValue();
        if (getSetting(2).asMode().mode == 0) {
            if (mc.player.inventory.getStack(slot).getItem() instanceof BedItem)
                return;
            if (mc.currentScreen instanceof InventoryScreen || mc.currentScreen == null) {
                for (int i = 9; i < 45; i++) {
                    if (mc.player.inventory.getStack(i >= 36 ? i - 36 : i).getItem() instanceof BedItem)
                    {
                        moveItems(i, slot, false);
                        return;
                    }
                }
            }
        } else if (getSetting(2).asMode().mode == 1) {
            if (mc.player.inventory.getStack(slot).getItem() instanceof SnowballItem)
                return;
            if (mc.currentScreen instanceof InventoryScreen || mc.currentScreen == null) {
                for (int i = 9; i < 45; i++) {
                    if (mc.player.inventory.getStack(i >= 36 ? i - 36 : i).getItem() instanceof SnowballItem)
                    {
                        moveItems(i, slot, false);
                        return;
                    }
                }
            }
        } else if (getSetting(2).asMode().mode == 2) {
            if (mc.player.inventory.getStack(slot).getItem() == Items.PACKED_ICE)
                return;
            if (mc.currentScreen instanceof InventoryScreen || mc.currentScreen == null) {
                for (int i = 9; i < 45; i++) {
                    if (mc.player.inventory.getStack(i >= 36 ? i - 36 : i).getItem() == Items.PACKED_ICE)
                    {
                        moveItems(i, slot, false);
                        return;
                    }
                }
            }
        }
    }
    private void moveItems(int from, int to, boolean stackable) {
        InvUtils.clickSlot(InvUtils.invIndexToSlotId(from), 0, SlotActionType.PICKUP);
        InvUtils.clickSlot(InvUtils.invIndexToSlotId(to), 0, SlotActionType.PICKUP);
        if (stackable) InvUtils.clickSlot(InvUtils.invIndexToSlotId(from), 0, SlotActionType.PICKUP);
    }

}
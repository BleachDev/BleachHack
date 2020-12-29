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
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.InvUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class BedReplenish extends Module {

    public BedReplenish() {
        super("BedReplenish", KEY_UNBOUND, Category.COMBAT, "Automatically equips beds.",
                new SettingToggle("Override", true).withDesc("Equips a bed even if theres another item in the offhand"),
                new SettingSlider("Slot: ", 0, 8, 8, 0));
    }
    // TODO: ADD DIFFERENT BED COLOR TYPES!!
    @Subscribe
    public void onTick(EventTick event) {
        int slot = (int) getSetting(1).asSlider().getValue();
        if (mc.player.inventory.getStack(slot).getItem() == Items.WHITE_BED
                || (!mc.player.inventory.getStack(slot).isEmpty() && !getSetting(0).asToggle().state))
            return;

        if (mc.currentScreen instanceof InventoryScreen || mc.currentScreen == null) {
            for (int i = 9; i < 45; i++) {
                if (mc.player.inventory.getStack(i >= 36 ? i - 36 : i).getItem() == Items.WHITE_BED) {
                    moveItems(i, slot, false);
                    return;
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
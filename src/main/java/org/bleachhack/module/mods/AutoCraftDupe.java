/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;

public class AutoCraftDupe extends Module {

    private int syncedCount = -1;

    public AutoCraftDupe() {
        super("AutoCraftDupe", KEY_UNBOUND, ModuleCategory.EXPLOITS, "Only works on 1.17");
    }

    @Override
    public void onDisable(boolean inWorld) {
        syncedCount = -1;

        super.onDisable(inWorld);
    }

    @BleachSubscribe
    public void onTick(EventTick event) {
        if (mc.currentScreen instanceof InventoryScreen) {
            ScreenHandler handler = mc.player.currentScreenHandler;

            if (!handler.getSlot(0).hasStack()) {
                for (int i = 1; i < 5; i++) {
                    Slot slot = handler.getSlot(i);
                    if (slot.hasStack() && slot.getStack().getCount() > 2) {
                        for (int j = 0; j < 5; j++) {
                            if (syncedCount == -1) {
                                syncedCount = slot.getStack().getCount();
                            }

                            if (syncedCount > 64) {
                                mc.interactionManager.clickSlot(handler.syncId, slot.id, 1, SlotActionType.PICKUP, mc.player);
                                mc.interactionManager.clickSlot(handler.syncId, ScreenHandler.EMPTY_SPACE_SLOT_INDEX, 0, SlotActionType.PICKUP, mc.player);
                                syncedCount /= 2;
                            } else {
                                mc.interactionManager.clickSlot(handler.syncId, handler.getSlot(0).id, 0, SlotActionType.THROW, mc.player);
                                syncedCount = syncedCount * 2 - 2;
                            }
                        }

                        return;
                    }
                }
            }

            syncedCount = -1;
        }

        syncedCount = -1;
    }
}
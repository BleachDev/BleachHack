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

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.container.SlotActionType;
import net.minecraft.item.Items;

public class AutoTotem extends Module {

	public AutoTotem() {
		super("AutoTotem", KEY_UNBOUND, Category.COMBAT, "Automatically equips totems.");
	}

	@Subscribe
	public void onTick(EventTick event) {
		// Cancel at all non-survival-inventory containers
		if ((mc.currentScreen instanceof ContainerScreen && !(mc.currentScreen instanceof InventoryScreen)) && mc.currentScreen != null)
			return;

		if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING)
			return;

		for (int i = 0; i < 36; i++) {
			if (mc.player.inventory.getInvStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
				mc.interactionManager.clickSlot(mc.player.container.syncId, i < 9 ? i + 36 : i, 0, SlotActionType.PICKUP, mc.player);
				mc.interactionManager.clickSlot(mc.player.container.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
				return;
			}
		}
	}

}

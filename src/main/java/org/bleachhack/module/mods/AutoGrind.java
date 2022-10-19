package org.bleachhack.module.mods;

import java.util.List;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingItemList;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.BleachLogger;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoGrind extends Module {

	private int crafted;

	public AutoGrind() {
		super("AutoGrind", KEY_UNBOUND, ModuleCategory.MISC, "Automatically grind enchants off items.",
				new SettingItemList("Edit Items", "Items you want to grind.").withDesc("Edit items to grind."));
	}

	@Override
	public void onEnable(boolean inWorld) {
		super.onEnable(inWorld);

		crafted = 0;
		if (getSetting(0).asList(Item.class).getValue().isEmpty()) {
			BleachLogger.error("AutoGrind items are empty.");
			setEnabled(false);
		}
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (!(mc.player.currentScreenHandler instanceof GrindstoneScreenHandler))
			return;

		GrindstoneScreenHandler handler = (GrindstoneScreenHandler) mc.player.currentScreenHandler;

		for (int slot = 3; slot <= 38; slot++) {
			ItemStack stack = handler.getSlot(slot).getStack();
			if (getSetting(0).asList(Item.class).contains(stack.getItem())) {
				// if item has grindable enchants
				if (EnchantmentHelper.get(stack).values().stream().mapToInt(Integer::intValue).sum() - (EnchantmentHelper.hasVanishingCurse(stack) ? 0 : 1) - (EnchantmentHelper.hasBindingCurse(stack) ? 0 : 1) > 0) {
					// shift-click item into gridstone slot
					mc.interactionManager.clickSlot(handler.syncId, slot, 0, SlotActionType.QUICK_MOVE, mc.player);
					// pick up from grindstone output slot (2)
					mc.interactionManager.clickSlot(handler.syncId, 2, 0, SlotActionType.PICKUP, mc.player);
					// click the original slot to put the de-enchanted item back
					mc.interactionManager.clickSlot(handler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
					// wait til next tick before grinding another one
					return;
				}
			}
		}
	}
}

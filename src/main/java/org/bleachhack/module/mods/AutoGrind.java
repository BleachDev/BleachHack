package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingItemList;
import org.bleachhack.util.BleachLogger;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoGrind extends Module {

	public AutoGrind() {
		super("AutoGrind", KEY_UNBOUND, ModuleCategory.MISC, "Automatically grind enchants off items.",
				new SettingItemList("Edit Items", "Items you want to grind.").withDesc("Edit items to grind."));
	}

	@Override
	public void onEnable(boolean inWorld) {
		super.onEnable(inWorld);

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

		// if there's already an item in the grindstone, don't do anything
		if (!(handler.getSlot(0).getStack().isEmpty() && handler.getSlot(1).getStack().isEmpty()))
			return;

		for (int slot = 3; slot <= 38; slot++) {
			ItemStack stack = handler.getSlot(slot).getStack();
			// if this is one of the items on our list
			if (shouldGrind(stack)) {
				// if item has grindable enchants
				if (canGrind(stack)) {
					// shift-click item into gridstone slot
					mc.interactionManager.clickSlot(handler.syncId, slot, 0, SlotActionType.QUICK_MOVE, mc.player);
					// grind
					doGrind(handler, slot);
					// wait til next tick before grinding another one
					return;
				}
				// continue to next item
			}
		}
	}

	private boolean shouldGrind(ItemStack stack) {
		return getSetting(0).asList(Item.class).contains(stack.getItem());
	}

	private boolean canGrind(ItemStack stack) {
		int enchants = getEnchantCount(stack);
		int curses = getCurseCount(stack);
		return (enchants - curses > 0);
	}

	private void doGrind(GrindstoneScreenHandler handler, int destinationSlot) {
		// pick up from grindstone output slot (2)
		mc.interactionManager.clickSlot(handler.syncId, 2, 0, SlotActionType.PICKUP, mc.player);
		// click the original slot to put the de-enchanted item back
		mc.interactionManager.clickSlot(handler.syncId, destinationSlot, 0, SlotActionType.PICKUP, mc.player);
	}

	private int getEnchantCount(ItemStack stack) {
		// get the number of entries from EnchantmentHelper.get(stack)
		return EnchantmentHelper.get(stack).size();
	}

	private int getCurseCount(ItemStack stack) {
		// needed as EnchantmentHelper's curse checks are bugged for Enchanted Books
		int curses = 0;
		if (EnchantmentHelper.get(stack).containsKey(Enchantments.BINDING_CURSE)) {
			curses += EnchantmentHelper.get(stack).get(Enchantments.BINDING_CURSE);
		}
		if (EnchantmentHelper.get(stack).containsKey(Enchantments.VANISHING_CURSE)) {
			curses += EnchantmentHelper.get(stack).get(Enchantments.VANISHING_CURSE);
		}
		return curses;
	}
}

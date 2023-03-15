package org.bleachhack.module.mods;

import java.util.List;

import net.minecraft.registry.DynamicRegistryManager;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingItemList;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.BleachLogger;

import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.item.Item;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoCraft extends Module {

	private int crafted;

	public AutoCraft() {
		super("AutoCraft", KEY_UNBOUND, ModuleCategory.MISC, "Automatically craft things.",
				new SettingItemList("Edit Items", "Items you want to craft.").withDesc("Edit crafting items."),
				new SettingToggle("CraftAll", false).withDesc("Crafts maximum possible amount amount per craft (shift-clicking)."),
				new SettingToggle("Drop", false).withDesc("Automatically drops crafted items (useful for when not enough inventory space)."),
				new SettingToggle("MaxItems", false).withDesc("Turns AutoCraft off after crafting a certain amount of items.").withChildren(
						new SettingSlider("Items", 1, 512, 64, 0).withDesc("How many items to craft."),
						new SettingToggle("Notify", true).withDesc("Notifies you after it finished crafting the items.")));
	}

	@Override
	public void onEnable(boolean inWorld) {
		super.onEnable(inWorld);

		crafted = 0;
		if (getSetting(0).asList(Item.class).getValue().isEmpty()) {
			BleachLogger.error("AutoCraft items are empty.");
			setEnabled(false);
		}
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		SettingToggle maxItems = getSetting(3).asToggle();
		if (maxItems.getState() && crafted >= maxItems.getChild(0).asSlider().getValueInt()) {
			if (maxItems.getChild(1).asToggle().getState())
				BleachLogger.info("Disabled AutoCraft after crafting " + crafted + " items.");

			setEnabled(false);
			return;
		}

		if (!(mc.player.currentScreenHandler instanceof CraftingScreenHandler))
			return;

		// quick hack
		CraftingScreenHandler handler = (CraftingScreenHandler) mc.player.currentScreenHandler;
		mc.player.getRecipeBook().setGuiOpen(handler.getCategory(), true);

		CraftingScreenHandler currentScreenHandler = (CraftingScreenHandler) mc.player.currentScreenHandler;
		List<RecipeResultCollection> recipeResultCollectionList = mc.player.getRecipeBook().getOrderedResults();

		boolean craftAll = getSetting(1).asToggle().getState();
		boolean drop = getSetting(2).asToggle().getState();

		for (RecipeResultCollection recipeResultCollection : recipeResultCollectionList) {
			for (Recipe<?> recipe : recipeResultCollection.getRecipes(true)) {
				if (getSetting(0).asList(Item.class).contains(recipe.getOutput(DynamicRegistryManager.EMPTY).getItem())) {
					mc.interactionManager.clickRecipe(currentScreenHandler.syncId, recipe, craftAll);
					mc.interactionManager.clickSlot(currentScreenHandler.syncId, 0, 0,
							drop ? SlotActionType.THROW : SlotActionType.QUICK_MOVE, mc.player);

					crafted++;
					return;
				}
			}
		}
	}

}

package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.eventbus.BleachSubscribe;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingToggle;
import bleach.hack.module.setting.other.SettingItemList;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.item.Item;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

/**
 * Originally from CrystalHack (BleachHack) ported from meteor rejects
 */

public class AutoCraft extends Module {

    public AutoCraft(){
        super("AutoCraft", KEY_UNBOUND, ModuleCategory.CRYSTALHACK, "Automatically craft things.",
                new SettingItemList("Edit Items", "Items you want to craft.").withDesc("Edit crafting items."),
                new SettingToggle("Anti desync", false).withDesc("Try to prevent inventory desync."),
                new SettingToggle("Craft all", false).withDesc("Crafts maximum possible amount amount per craft (shift-clicking)"),
                new SettingToggle("Drop", false).withDesc("Automatically drops crafted items (useful for when not enough inventory space)"));
    }

    boolean antiDesync = getSetting(1).asToggle().state;
    boolean craftAll = getSetting(2).asToggle().state;
    boolean drop = getSetting(3).asToggle().state;

    @BleachSubscribe
    public void onTick(EventTick event) {
        if (mc.interactionManager == null) return;
        if (getSetting(0).asList(Item.class).contains(null)) return;

        if (!(mc.player.currentScreenHandler instanceof CraftingScreenHandler)) return;

        if (antiDesync) {
            mc.player.getInventory().updateItems();
        }
        CraftingScreenHandler currentScreenHandler = (CraftingScreenHandler) mc.player.currentScreenHandler;
        //List<Item> itemList = items.get();
        List<RecipeResultCollection> recipeResultCollectionList  = mc.player.getRecipeBook().getOrderedResults();
        for (RecipeResultCollection recipeResultCollection : recipeResultCollectionList) {
            for (Recipe<?> recipe : recipeResultCollection.getRecipes(true)) {
                if (!getSetting(0).asList(Item.class).contains(recipe.getOutput().getItem())) continue;
                mc.interactionManager.clickRecipe(currentScreenHandler.syncId, recipe, craftAll);
                mc.interactionManager.clickSlot(currentScreenHandler.syncId, 0, 1,
                        drop ? SlotActionType.THROW : SlotActionType.QUICK_MOVE, mc.player);
            }
        }
    }

}

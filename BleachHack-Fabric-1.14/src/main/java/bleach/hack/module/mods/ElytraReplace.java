package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;

import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.container.SlotActionType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ElytraReplace extends Module {

    private boolean jump = false;

    public ElytraReplace() {
        super("ElytraReplace", KEY_UNBOUND, Category.PLAYER, "Automatically replaces broken elytra and continues flying");
    }

    @Subscribe
    public void onTick(EventTick event) {
    	if((mc.currentScreen instanceof AbstractContainerScreen && !(mc.currentScreen instanceof InventoryScreen)) && mc.currentScreen != null) return;
    	
        int chestSlot = 38;
        ItemStack chest = mc.player.inventory.getInvStack(chestSlot);
        if (chest.getItem() instanceof ElytraItem && chest.getDamage() == (Items.ELYTRA.getMaxDamage() - 1)) {
            // search inventory for elytra

            Integer elytraSlot = null;
            for (int slot = 0; slot < 36; slot++) {
                ItemStack stack = mc.player.inventory.getInvStack(slot);
                if (stack.isEmpty() || !(stack.getItem() instanceof ElytraItem) || stack.getDamage() == (Items.ELYTRA.getMaxDamage() - 1))
                    continue;
                else {
                    elytraSlot = slot;
                    break;
                }
            }

            if (elytraSlot == null) {
                return;
            }

            mc.interactionManager.method_2906(mc.player.container.syncId, 6, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.method_2906(mc.player.container.syncId, elytraSlot < 9 ? (elytraSlot + 36) : (elytraSlot), 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.method_2906(mc.player.container.syncId, 6, 0, SlotActionType.PICKUP, mc.player);

            KeyBinding.setKeyPressed(InputUtil.fromName(mc.options.keyJump.getName()), true);  // Make them fly again
            jump = true;
        } else if (jump) {
        	KeyBinding.setKeyPressed(InputUtil.fromName(mc.options.keyJump.getName()), false); // Make them fly again
            jump = false;
        }
    }
}


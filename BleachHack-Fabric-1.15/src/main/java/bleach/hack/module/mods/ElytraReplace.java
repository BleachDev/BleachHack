package bleach.hack.module.mods;


import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.container.SlotActionType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ElytraReplace extends Module {

    private boolean jump = false;
    private boolean jump2 = false;

    public ElytraReplace() {
        super("ElytraReplace", KEY_UNBOUND, Category.PLAYER, "Automatically replaces broken elytra and continues flying");
    }

    public void onDisable() {
        super.onDisable();
    }

    @Subscribe
    public void onTick(EventTick event) {
        int chestSlot = 38;
        ItemStack chest = mc.player.inventory.getInvStack(chestSlot);
        if (chest.getItem() instanceof ElytraItem && chest.getDamage() == (Items.ELYTRA.getMaxDamage() - 1)) {
            // search inventory for elytra

            Integer elytraSlot = null;
            for (int slot = 0; slot < 36; slot++) {
                ItemStack stack = mc.player.inventory.getInvStack(slot);
                if (stack.isEmpty() || !(stack.getItem() instanceof ElytraItem) || stack.getDamage() == (Items.ELYTRA.getMaxDamage() - 1))
                    continue;
                elytraSlot = slot;
                break;
            }

            if (elytraSlot == null) {
                return;
            }

            mc.interactionManager.clickSlot(mc.player.container.syncId, 6, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(mc.player.container.syncId, elytraSlot < 9 ? (elytraSlot + 36) : (elytraSlot), 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(mc.player.container.syncId, 6, 0, SlotActionType.PICKUP, mc.player);

            mc.options.keyJump.setPressed(true);  // Make them fly again
            jump = true;
        } else if (jump == true) {
            mc.options.keyJump.setPressed(false); // Make them fly again
            jump = false;
        }
    }
}


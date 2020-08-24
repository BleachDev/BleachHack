package bleach.hack.module.mods;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingToggle;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

public class AutoTool extends Module {

    private int lastSlot = -1;
    private int queueSlot = -1;

    public AutoTool() {
        super("AutoTool", KEY_UNBOUND, Category.PLAYER, "Automatically uses best tool",
                new SettingToggle("Anti Break", false).withDesc("Deosn't use tool if its about to break"),
                new SettingToggle("Switch Back", true).withDesc("Switches back to your previous item when done breaking"),
                new SettingToggle("DurabilitySave", true).withDesc("Swiches to a non-damagable item if possible"));
    }

    @Subscribe
    public void onPacketSend(EventSendPacket event) {
        if (event.getPacket() instanceof PlayerActionC2SPacket) {
            PlayerActionC2SPacket p = (PlayerActionC2SPacket) event.getPacket();

            if (p.getAction() == Action.START_DESTROY_BLOCK) {
                if (mc.player.isCreative() || mc.player.isSpectator())
                    return;

                queueSlot = -1;

                lastSlot = mc.player.inventory.selectedSlot;

                int slot = getBestSlot(p.getPos());

                if (slot != mc.player.inventory.selectedSlot) {
                    if (slot < 9) {
                        mc.player.inventory.selectedSlot = slot;
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                    } else if (mc.currentScreen instanceof InventoryScreen || mc.currentScreen == null) {
                        boolean itemInHand = !mc.player.inventory.getMainHandStack().isEmpty();
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 36 + mc.player.inventory.selectedSlot, 0, SlotActionType.PICKUP, mc.player);

                        if (itemInHand)
                            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
                    }
                }
            } else if (p.getAction() == Action.STOP_DESTROY_BLOCK) {
                if (getSetting(1).asToggle().state) {
                    ItemStack handSlot = mc.player.getMainHandStack();
                    if (getSetting(0).asToggle().state && handSlot.isDamageable() && handSlot.getMaxDamage() - handSlot.getDamage() < 2
                            && queueSlot == mc.player.inventory.selectedSlot) {
                        queueSlot = mc.player.inventory.selectedSlot == 0 ? 1 : mc.player.inventory.selectedSlot - 1;
                    } else if (lastSlot >= 0 && lastSlot <= 8 && lastSlot != mc.player.inventory.selectedSlot) {
                        queueSlot = lastSlot;
                    }
                }
            }
        }
    }

    @Subscribe
    public void onTick(EventTick event) {
        if (queueSlot != -1) {
            mc.player.inventory.selectedSlot = queueSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(queueSlot));
            queueSlot = -1;
        }
    }

    private int getBestSlot(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);

        int bestSlot = mc.player.inventory.selectedSlot;

        ItemStack handSlot = mc.player.inventory.getStack(bestSlot);
        if (getSetting(0).asToggle().state && handSlot.isDamageable() && handSlot.getMaxDamage() - handSlot.getDamage() < 2) {
            bestSlot = bestSlot == 0 ? 1 : bestSlot - 1;
        }

        if (state.isAir())
            return mc.player.inventory.selectedSlot;

        float bestSpeed = getMiningSpeed(mc.player.inventory.getStack(bestSlot), state);

        for (int slot = 0; slot < 36; slot++) {
            if (slot == mc.player.inventory.selectedSlot || slot == bestSlot)
                continue;

            ItemStack stack = mc.player.inventory.getStack(slot);
            if (getSetting(0).asToggle().state && stack.isDamageable() && stack.getMaxDamage() - stack.getDamage() < 2) {
                continue;
            }

            float speed = getMiningSpeed(stack, state);
            if (speed > bestSpeed
                    || (getSetting(2).asToggle().state
                    && speed == bestSpeed && !stack.isDamageable()
                    && mc.player.inventory.getStack(bestSlot).isDamageable()
                    && EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, mc.player.inventory.getStack(bestSlot)) == 0)) {
                bestSpeed = speed;
                bestSlot = slot;
            }
        }

        return bestSlot;
    }

    private float getMiningSpeed(ItemStack stack, BlockState state) {
        float speed = stack.getMiningSpeedMultiplier(state);

        if (speed > 1) {
            int efficiency = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            if (efficiency > 0 && !stack.isEmpty())
                speed += efficiency * efficiency + 1;
        }

        return speed;
    }
}
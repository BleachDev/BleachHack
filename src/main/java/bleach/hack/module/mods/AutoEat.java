package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.mixin.IKeyBinding;
import bleach.hack.mixin.IMinecraftClient;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.Objects;

public class AutoEat extends Module {

    public AutoEat() {
        super("AutoEat", KEY_UNBOUND, Category.PLAYER, "Auto eats when food is low");
    }
    // TODO make this thing switch back after done eating and health
    // mc.player.eatFood(mc.world, mc.player.getMainHandStack());
    private int lastSlot = -1;
    private boolean eating = false;

    private boolean isValid(ItemStack stack, int food) {
        return stack.getItem().getGroup() == ItemGroup.FOOD && (20 - food) >= Objects.requireNonNull(stack.getItem().getFoodComponent()).getHunger();
    }

    @Subscribe
    public void onTick(EventTick event) {
        if (eating && !mc.player.isUsingItem()) {
            if (lastSlot != -1) {
                mc.player.inventory.selectedSlot = lastSlot;
                lastSlot = -1;
            }
            eating = false;
            KeyBinding.setKeyPressed(((IKeyBinding) mc.options.keyUse).getBoundKey(), false);
            return;
        }
        if (eating) return;
        HungerManager stats = mc.player.getHungerManager();
        if (isValid(mc.player.getOffHandStack(), stats.getFoodLevel())) {
            mc.player.setCurrentHand(Hand.OFF_HAND);
            eating = true;
            KeyBinding.setKeyPressed(((IKeyBinding) mc.options.keyUse).getBoundKey(), true);
            ((IMinecraftClient) mc).callDoItemUse();
        } else {
            for (int i = 0; i < 9; i++) {
                if (isValid(mc.player.inventory.getStack(i), stats.getFoodLevel())) {
                    lastSlot = mc.player.inventory.selectedSlot;
                    mc.player.inventory.selectedSlot = i;
                    eating = true;
                    KeyBinding.setKeyPressed(((IKeyBinding) mc.options.keyUse).getBoundKey(), true);
                    ((IMinecraftClient) mc).callDoItemUse();
                    return;
                }
            }
        }
    }
}

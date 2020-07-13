package bleach.hack.module.mods;

import bleach.hack.event.events.EventBlockBreakingProgress;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShapes;

public class AutoTool extends Module {

    private int prevSelectedSlot;

    public AutoTool() {
        super("AutoTool", KEY_UNBOUND, Category.PLAYER, "Automatically uses best tool");
    }

    @Subscribe
    public void onBlockBreak(EventBlockBreakingProgress event) {
        System.out.println("Fire!");
        BlockPos pos = event.getBlockPos();
        if (mc.world.getBlockState(pos).getOutlineShape(MinecraftClient.getInstance().world, pos) == VoxelShapes.empty())
            return;

        if (prevSelectedSlot == -1)
            prevSelectedSlot = mc.player.inventory.selectedSlot;

        equipBestTool(pos);
    }

    public void equipBestTool(BlockPos pos) {
        ClientPlayerEntity player = mc.player;
        if (player.abilities.creativeMode)
            return;

        int bestSlot = getBestSlot(pos);
        if (bestSlot == -1) {
            return;
        }

        player.inventory.selectedSlot = bestSlot;
    }

    private int getBestSlot(BlockPos pos) {
        ClientPlayerEntity player = mc.player;
        PlayerInventory inventory = player.inventory;
        ItemStack heldItem = mc.player.getMainHandStack();

        BlockState state = mc.world.getBlockState(pos);
        float bestSpeed = getMiningSpeed(heldItem, state);
        int bestSlot = -1;

        for (int slot = 0; slot < 9; slot++) {
            if (slot == inventory.selectedSlot)
                continue;

            ItemStack stack = inventory.getInvStack(slot);

            float speed = getMiningSpeed(stack, state);
            if (speed <= bestSpeed)
                continue;

            bestSpeed = speed;
            bestSlot = slot;
        }

        return bestSlot;
    }

    private float getMiningSpeed(ItemStack stack, BlockState state) {
        float speed = stack.getMiningSpeed(state);

        if (speed > 1) {
            int efficiency =
                    EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            if (efficiency > 0 && !stack.isEmpty())
                speed += efficiency * efficiency + 1;
        }

        return speed;
    }
}

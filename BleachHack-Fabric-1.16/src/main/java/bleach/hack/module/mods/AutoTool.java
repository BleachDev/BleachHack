package bleach.hack.module.mods;

import bleach.hack.event.events.EventBlockBreakingProgress;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class AutoTool extends Module {

	public AutoTool() {
		super("AutoTool", KEY_UNBOUND, Category.PLAYER, "Automatically uses best tool",
				new SettingToggle("Anti Break", false));
	}

	@Subscribe
	public void onBlockBreak(EventBlockBreakingProgress event) {
		if (mc.player.abilities.creativeMode) return;

		int bestSlot = getBestSlot(event.getPos());
		if (bestSlot == -1) {
			return;
		}

		mc.player.inventory.selectedSlot = bestSlot;
	}

	private int getBestSlot(BlockPos pos) {
		BlockState state = mc.world.getBlockState(pos);
		
		if (state.isAir()) return -1;
		
		float bestSpeed = 0;
		int bestSlot = -1;

		for (int slot = 0; slot < 9; slot++) {
			ItemStack stack = mc.player.inventory.getStack(slot);

			if (getSettings().get(0).toToggle().state && !stack.isEmpty() && stack.isDamageable()
					&& stack.getMaxDamage() - stack.getDamage() < 2) continue;

			float speed = getMiningSpeed(stack, state);
			if (speed > bestSpeed
					|| (speed == bestSpeed && !stack.isDamageable() && mc.player.inventory.getStack(bestSlot).isDamageable())) {
				bestSpeed = speed;
				bestSlot = slot;
			}
		}

		return bestSlot;
	}

	private float getMiningSpeed(ItemStack stack, BlockState state) {
		float speed = stack.getMiningSpeedMultiplier(state);

		if (speed > 1) {
			int efficiency =
					EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
			if (efficiency > 0 && !stack.isEmpty())
				speed += efficiency * efficiency + 1;
		}

		return speed;
	}
}

/*
 * some licence stuff here
 */
package bleach.hack.module.mods;

import bleach.hack.eventbus.BleachSubscribe;

import java.util.Comparator;

import bleach.hack.event.events.EventSoundPlay;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.ModuleCategory;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.InventoryUtils;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import bleach.hack.module.Module;

public class AutoFish extends Module {

	private boolean threwRod = false;

	public AutoFish() {
		super("AutoFish", KEY_UNBOUND, ModuleCategory.PLAYER, "Automatically fishes for you.",
				new SettingMode("Mode", "Normal", "Aggressive", "Passive").withDesc("AutoFish mode."));
	}

	@Override
	public void onEnable() {
		super.onEnable();

		int slot = getBestRodSlot();
		if (slot == -1) {
			BleachLogger.error("No fishing rods in your inventory!");
			setEnabled(false);
			return;
		}
	}

	@Override
	public void onDisable() {
		threwRod = false;
		super.onDisable();
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (mc.player.fishHook != null)
			threwRod = false;

		if (!threwRod && mc.player.fishHook == null && getSetting(0).asMode().mode != 2) {
			Hand newHand = getSetting(0).asMode().mode == 1 ? InventoryUtils.selectSlot(getBestRodSlot()) : getHandWithRod();
			if (newHand != null) {
				//throw again
				mc.interactionManager.interactItem(mc.player, mc.world, newHand);
				threwRod = true;
			}
		}
	}

	@BleachSubscribe
	public void onSoundPlay(EventSoundPlay.Normal event) {
		if (event.instance.getId().getPath().equals("entity.fishing_bobber.splash")) {
			Hand hand = getHandWithRod();

			if (hand != null) {
				//reel back
				mc.interactionManager.interactItem(mc.player, mc.world, hand);
			}
		}
	}

	private Hand getHandWithRod() {
		return mc.player.getMainHandStack().getItem() == Items.FISHING_ROD ? Hand.MAIN_HAND
				: mc.player.getOffHandStack().getItem() == Items.FISHING_ROD ? Hand.OFF_HAND
						: null;
	}

	private int getBestRodSlot() {
		int slot = InventoryUtils.getSlot(true, false, Comparator.comparingInt(i -> {
			ItemStack is = mc.player.getInventory().getStack(i);
			if (is.getItem() != Items.FISHING_ROD)
				return -1;

			return EnchantmentHelper.get(is).values().stream().mapToInt(Integer::intValue).sum();
		}));

		if (mc.player.getInventory().getStack(slot).getItem() == Items.FISHING_ROD) {
			return slot;
		}

		return -1;
	}
}

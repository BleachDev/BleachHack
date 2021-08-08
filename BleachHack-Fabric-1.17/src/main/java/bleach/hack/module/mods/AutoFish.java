/*
 * some licence stuff here
 */
package bleach.hack.module.mods;

import bleach.hack.eventbus.BleachSubscribe;

import bleach.hack.event.events.EventSoundPlay;
import bleach.hack.module.ModuleCategory;
import net.minecraft.item.FishingRodItem;
import bleach.hack.module.Module;

public class AutoFish extends Module {

	public AutoFish() {
		super("AutoFish", KEY_UNBOUND, ModuleCategory.PLAYER, "Automatically fish.");
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}

	@BleachSubscribe
	public void onSoundPlay(EventSoundPlay.Normal event) {
		if (event.instance.getId().getPath().equals("entity.fishing_bobber.splash") && mc.player.getMainHandStack().getItem() instanceof FishingRodItem) {
			//reel back
			mc.interactionManager.interactItem(mc.player, mc.world, mc.player.preferredHand);
			//throw again
			mc.interactionManager.interactItem(mc.player, mc.world, mc.player.preferredHand);
		}
	}
}

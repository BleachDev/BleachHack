package bleach.hack.module.mods;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class AutoWalk extends Module {

	public AutoWalk() {
		super("AutoWalk", KEY_UNBOUND, Category.MOVEMENT, "Automatically walks/flies forward");
	}

	public void onDisable() {
		KeyBinding.setKeyPressed(InputUtil.fromName(mc.options.keyForward.getName()), false);
		super.onDisable();
	}

	@Subscribe
	public void onTick(EventTick event) {
		KeyBinding.setKeyPressed(InputUtil.fromName(mc.options.keyForward.getName()), true);
	}
}

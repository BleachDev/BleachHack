package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventTick;
import com.google.common.eventbus.Subscribe;
import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.FabricReflect;

public class FastUse extends Module {
	
	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode(new String[] {"Single", "Multi"}, "Mode: "),
			new SettingSlider(1, 100, 20, 0, "Multi: "));
	
	public FastUse() {
		super("FastUse", GLFW.GLFW_KEY_B, Category.PLAYER, "Allows you to use items faster", settings);
	}

	@Override
	public void onEnable() {
		BleachHack.getEventBus().register(this);
	}

	@Override
	public void onDisable() {
		BleachHack.getEventBus().unregister(this);
	}

	@Subscribe
	public void onTick(EventTick eventTick) {
		/* set rightClickDelay to 0 */
		FabricReflect.writeField(mc, 0, "field_1752", "itemUseCooldown");
		
		/* call rightClickMouse */
		if(getSettings().get(0).toMode().mode == 1 && mc.options.keyUse.isPressed()) {
			for(int i = 0; i < (int) getSettings().get(1).toSlider().getValue(); i++) {
				FabricReflect.invokeMethod(mc, "method_1583", "doItemUse", null, null);
			}
		}
	}
}

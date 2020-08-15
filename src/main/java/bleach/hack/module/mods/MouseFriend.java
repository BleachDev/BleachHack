package bleach.hack.module.mods;

import java.util.Optional;

import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.BleachLogger;
import org.lwjgl.glfw.GLFW;

import com.google.common.eventbus.Subscribe;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class MouseFriend extends Module {

	private boolean antiSpamClick = false;

	public MouseFriend() {
		super("MouseFriend", KEY_UNBOUND, Category.MISC, "Add/Remove friends with mouse buttons",
				new SettingMode("Button", "Middle", "Right", "MOUSE4", "MOUSE5", "MOUSE6"),
				new SettingToggle("Messages", true).withDesc("says in chat when a friend is added/removed"));
	}

	@Subscribe
	public void onTick(EventTick event) {
		int setting = getSetting(0).asMode().mode;
		int button = setting == 0 ? GLFW.GLFW_MOUSE_BUTTON_MIDDLE : setting == 1 ? GLFW.GLFW_MOUSE_BUTTON_RIGHT : setting + 2;

		if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), button) == 1 && !antiSpamClick) {
			antiSpamClick = true;

			Optional<Entity> lookingAt = DebugRenderer.getTargetedEntity(mc.player, 200);

			if (lookingAt.isPresent()) {
				Entity e = lookingAt.get();

				if (e instanceof PlayerEntity) {
					if (BleachHack.friendMang.has(e.getName().asString())) {
						BleachHack.friendMang.remove(e.getName().asString());
						if (this.getSetting(1).asToggle().state) {
							BleachLogger.infoMessage("Removed \"" + e.getName().asString() + "\" from the friend list");
						}
					} else {
						BleachHack.friendMang.add(e.getName().asString());
						if (this.getSetting(1).asToggle().state) {
							BleachLogger.infoMessage("Added \"" + e.getName().asString() + "\" to the friend list");
						}
					}
				}
			}
		} else if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), button) == 0) {
			antiSpamClick = false;
		}
	}
}

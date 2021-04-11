package bleach.hack.module.mods;

import java.util.Optional;

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

	private boolean buttonHeld = false;

	public MouseFriend() {
		super("MouseFriend", KEY_UNBOUND, Category.MISC, "Add/Remove friends with mouse buttons",
				new SettingMode("Button", "Middle", "Right", "MOUSE4", "MOUSE5", "MOUSE6").withDesc("What mouse button to use"));
	}

	@Subscribe
	public void onTick(EventTick event) {
		int setting = getSetting(0).asMode().mode;
		int button = setting == 0 ? GLFW.GLFW_MOUSE_BUTTON_MIDDLE : setting == 1 ? GLFW.GLFW_MOUSE_BUTTON_RIGHT : setting + 2;

		if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), button) == GLFW.GLFW_PRESS && !buttonHeld) {
			buttonHeld = true;

			Optional<Entity> lookingAt = DebugRenderer.getTargetedEntity(mc.player, 200);

			if (lookingAt.isPresent()) {
				Entity e = lookingAt.get();

				if (e instanceof PlayerEntity) {
					if (BleachHack.friendMang.has(e.getName().getString())) {
						BleachHack.friendMang.remove(e.getName().getString());
					} else {
						BleachHack.friendMang.add(e.getName().getString());
					}
				}
			}
		} else if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), button) == GLFW.GLFW_RELEASE) {
			buttonHeld = false;
		}
	}
}

package bleach.hack.module.mods;

import java.util.Optional;

import org.lwjgl.glfw.GLFW;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.gui.InteractionScreen;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class Interaction extends Module {

	private boolean released;
	
	public Interaction() {
		super("Interaction screen", KEY_UNBOUND, Category.MISC, "An interaction screen when pressing the middle mouse button while looking to an entity. Customizable via the \"$interaction\" command");
	}
	
	@Subscribe
	public void onTick(EventTick event) {
		if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS && released) {
			released = false;
			
			Optional<Entity> lookingAt = DebugRenderer.getTargetedEntity(mc.player, 20);
			
			if (lookingAt.isPresent()) {
				Entity e = lookingAt.get();

				if (e instanceof LivingEntity) {
					mc.openScreen(new InteractionScreen(e.getName().getString()));
				}
			}
		} else if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_RELEASE)
			released = true;
	}
}

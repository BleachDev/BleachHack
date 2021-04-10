package bleach.hack.module.mods;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.lwjgl.glfw.GLFW;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonElement;

import bleach.hack.event.events.EventTick;
import bleach.hack.gui.InteractionScreen;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.util.file.BleachFileHelper;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

/**
 * @author <a href="https://github.com/lasnikprogram">Lasnik</a>
 */
public class EntityMenu extends Module {
	
	public Map<String, String> interactions = new HashMap<>();

	private boolean released;
	
	public EntityMenu() {
		super("EntityMenu", KEY_UNBOUND, Category.MISC, "An interaction screen when looking at an entity and pressing the middle mouse button. Customizable via the $interaction command");
	
		JsonElement je = BleachFileHelper.readMiscSetting("entityMenu");
		
		if (je != null && je.isJsonObject()) {
			for (Entry<String, JsonElement> entry: je.getAsJsonObject().entrySet()) {
				if (entry.getValue().isJsonPrimitive()) {
					interactions.put(entry.getKey(), entry.getValue().getAsString());
				}
			}
		}
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

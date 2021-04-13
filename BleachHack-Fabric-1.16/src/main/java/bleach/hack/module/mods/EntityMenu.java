package bleach.hack.module.mods;

import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.glfw.GLFW;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonElement;

import bleach.hack.command.Command;
import bleach.hack.event.events.EventTick;
import bleach.hack.gui.EntityMenuScreen;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.PairList;
import bleach.hack.util.file.BleachFileHelper;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * @author <a href="https://github.com/lasnikprogram">Lasnik</a>
 */
public class EntityMenu extends Module {
	
	// fuck maps
	public PairList<String, String> interactions = new PairList<>();

	private boolean buttonHeld;
	
	public EntityMenu() {
		super("EntityMenu", KEY_UNBOUND, Category.MISC, "An interaction screen when looking at an entity and pressing the middle mouse button. Customizable via the " + Command.PREFIX + "interaction command",
				new SettingToggle("PlayersOnly", false).withDesc("Only opens the menu when clicking on players"));
	
		JsonElement je = BleachFileHelper.readMiscSetting("entityMenu");
		
		if (je != null && je.isJsonObject()) {
			for (Entry<String, JsonElement> entry: je.getAsJsonObject().entrySet()) {
				if (entry.getValue().isJsonPrimitive()) {
					interactions.add(new MutablePair<>(entry.getKey(), entry.getValue().getAsString()));
				}
			}
		}
	}
	
	@Subscribe
	public void onTick(EventTick event) {
		if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS && !buttonHeld) {
			buttonHeld = true;
			
			Optional<Entity> lookingAt = DebugRenderer.getTargetedEntity(mc.player, 20);
			
			if (lookingAt.isPresent()) {
				Entity e = lookingAt.get();

				if (e instanceof LivingEntity && (e instanceof PlayerEntity || !getSetting(0).asToggle().state)) {
					mc.openScreen(new EntityMenuScreen((LivingEntity) e));
				}
			}
		} else if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_RELEASE) {
			buttonHeld = false;
		}
	}
}

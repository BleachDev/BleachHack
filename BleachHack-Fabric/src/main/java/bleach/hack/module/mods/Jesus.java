package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import com.google.common.eventbus.Subscribe;
import org.lwjgl.glfw.GLFW;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class Jesus extends Module {

	public Jesus() {
		super("Jesus", GLFW.GLFW_KEY_J, Category.PLAYER, "Allows you to walk on water", null);
	}

	@Subscribe
	public void onTick(EventTick event) {
		Entity e = mc.player.getVehicle() != null ? mc.player.getVehicle() : mc.player;
		
		if(e.isSneaking() || e.fallDistance > 3f) return;
		
		if(WorldUtils.isFluid(new BlockPos(e.getPos().add(0,0.3,0)))) {
			e.setVelocity(e.getVelocity().x, 0.08, e.getVelocity().z);
		}else if(WorldUtils.isFluid(new BlockPos(e.getPos().add(0,0.1,0)))) {
			e.setVelocity(e.getVelocity().x, 0.05, e.getVelocity().z);
		}else if(WorldUtils.isFluid(new BlockPos(e.getPos().add(0,0.05,0)))) {
			e.setVelocity(e.getVelocity().x, 0.01, e.getVelocity().z);
		}else if(WorldUtils.isFluid(new BlockPos(e.getPos()))) {
			e.setVelocity(e.getVelocity().x, -0.005, e.getVelocity().z);
			e.onGround = true;
		}
	}
}

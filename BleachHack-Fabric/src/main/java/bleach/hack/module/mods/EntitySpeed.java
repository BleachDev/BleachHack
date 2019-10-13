package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import com.google.common.eventbus.Subscribe;
import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.math.BlockPos;

public class EntitySpeed extends Module {
	
	public EntitySpeed() {
		super("EntitySpeed", GLFW.GLFW_KEY_GRAVE_ACCENT, Category.MOVEMENT, "Allows you to go fast while riding entities.",
				new SettingSlider(0, 5, 1.2, 2, "Speed: "),
				new SettingToggle(false, "EntityFly"),
				new SettingToggle(false, "Ground Snap"));
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (mc.player.getVehicle() == null) return;
		
		Entity e = mc.player.getVehicle();
		e.yaw = mc.player.yaw;
		double speed = getSettings().get(0).toSlider().getValue();
		
		if (e instanceof LlamaEntity) {
			((LlamaEntity) e).headYaw = mc.player.headYaw;
		}
		double forward = mc.player.forwardSpeed;
		double strafe = mc.player.sidewaysSpeed;
		float yaw = mc.player.yaw;
		
		if ((forward == 0.0D) && (strafe == 0.0D)) {
			e.setVelocity(0, e.getVelocity().y, 0);
		} else {
			if (forward != 0.0D) {
				if (strafe > 0.0D) { yaw += (forward > 0.0D ? -45 : 45);
				} else if (strafe < 0.0D) yaw += (forward > 0.0D ? 45 : -45);
				strafe = 0.0D;
				if (forward > 0.0D) { forward = 1.0D;
				} else if (forward < 0.0D) forward = -1.0D;
			}
			e.setVelocity((forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F))), e.getVelocity().y,
					forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F)));
			if (e instanceof MinecartEntity) {
				MinecartEntity em = (MinecartEntity) e;
				em.setVelocity((forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F))), em.getVelocity().y, (forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F))));
			}
			
			if(getSettings().get(1).toToggle().state) if(mc.options.keyJump.isPressed()) e.setVelocity(e.getVelocity().x, 0.3, e.getVelocity().z);
			
			if(getSettings().get(2).toToggle().state) {
				BlockPos p = new BlockPos(e.getPos());
				if(mc.world.getBlockState(p.down()).getBlock() != Blocks.AIR &&
					mc.world.getBlockState(p.down()).getMaterial() != Material.WATER &&
					e.fallDistance > 0.01) e.setVelocity(e.getVelocity().x, -1, e.getVelocity().z);
			}
		}
	}
}

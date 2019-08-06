package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Jesus extends Module {

	public Jesus() {
		super("Jesus", GLFW.GLFW_KEY_J, Category.PLAYER, "Allows you to walk on water", null);
	}

	public void onUpdate() {
		Entity e = mc.player.isRiding() ? mc.player.getVehicle() : mc.player;
		
		if(e.isSneaking() || e.fallDistance > 3f || e.horizontalCollision) return;

		if(isFluid(e.getPos().add(0,0.3,0))) {
			e.setVelocity(e.getVelocity().x, 0.08, e.getVelocity().z);
		}else if(isFluid(e.getPos().add(0,0.07,0))) {
			e.setVelocity(e.getVelocity().x, 0.01, e.getVelocity().z);
		}else if(isFluid(e.getPos())) {
			e.setVelocity(e.getVelocity().x, -0.005, e.getVelocity().z);
			e.onGround = true;
		}
	}
	
	private boolean isFluid(Vec3d vec) {
		BlockPos p = new BlockPos(vec.x, vec.y, vec.z);
		
		List<Material> fluids = Arrays.asList(Material.WATER, Material.LAVA, Material.SEAGRASS);

		if(fluids.contains(mc.world.getBlockState(p).getMaterial())) return true;
		return false;
	}
}

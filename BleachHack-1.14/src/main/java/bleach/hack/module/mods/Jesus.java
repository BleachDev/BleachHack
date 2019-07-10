package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Jesus extends Module {

	public Jesus() {
		super("Jesus", GLFW.GLFW_KEY_J, Category.PLAYER, "Allows you to walk on water", null);
	}

	/* This works surprisingly well, just threw come code together and it worked */
	public void onUpdate() {
		if(this.isToggled()) {
			Entity e = mc.player.getRidingEntity() != null ? mc.player.getRidingEntity() : mc.player;
			
			if(e.isSneaking() || e.fallDistance > 3f || e.collidedHorizontally) return;

			if(isFluid(e.getPositionVec().add(0,0.3,0))) {
				e.setMotion(e.getMotion().x, 0.08, e.getMotion().z);
			}else if(isFluid(e.getPositionVec().add(0,0.07,0))) {
				e.setMotion(e.getMotion().x, 0.01, e.getMotion().z);
			}else if(isFluid(e.getPositionVec())) {
				e.setMotion(e.getMotion().x, -0.005, e.getMotion().z);
				e.onGround = true;
			}
		}
	}
	
	private boolean isFluid(Vec3d vec) {
		BlockPos p = new BlockPos(vec.x, vec.y, vec.z);
		
		List<Material> fluids = Arrays.asList(Material.WATER, Material.LAVA,
				Material.SEA_GRASS, Material.CORAL);

		if(fluids.contains(mc.world.getBlockState(p).getMaterial())) return true;
		
		return false;
	}
}

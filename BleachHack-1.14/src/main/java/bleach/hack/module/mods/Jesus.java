package bleach.hack.module.mods;

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
			
			if(e.isSneaking() || e.fallDistance > 3f) return;
			
			if(isWater(e.getPositionVec().add(0,0.3,0))) {
				e.setMotion(e.getMotion().x, 0.1, e.getMotion().z);
			}else if(isWater(e.getPositionVec().add(0,0.1,0))) {
				e.setMotion(e.getMotion().x, 0.04, e.getMotion().z);
			}else if(isWater(e.getPositionVec())) {
				e.setMotion(e.getMotion().x, -0.005, e.getMotion().z);
				e.onGround = true;
			}
		}
	}
	
	private boolean isWater(Vec3d vec) {
		BlockPos p = new BlockPos(vec.x, vec.y, vec.z);
		if(mc.world.getBlockState(p).getMaterial() == Material.WATER) return true;
		
		return false;
	}
}

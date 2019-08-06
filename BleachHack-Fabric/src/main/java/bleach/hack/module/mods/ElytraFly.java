package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.util.math.Vec3d;

public class ElytraFly extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingSlider(0, 5, 0.8, 2, "Speed: "));
			
	public ElytraFly() {
		super("ElytraFly", -1, Category.MOVEMENT, "Improves the elytra", settings);
	}
	
	public void onUpdate() {
		if (mc.player.isFallFlying()) {
			Vec3d vec3d = new Vec3d(0,0,getSettings().get(0).toSlider().getValue())
					.rotateX(-(float) Math.toRadians(mc.player.pitch))
					.rotateY(-(float) Math.toRadians(mc.player.yaw));
			
			mc.player.setVelocity(
					mc.player.getVelocity().x + vec3d.x + (vec3d.x - mc.player.getVelocity().x),
					mc.player.getVelocity().y + vec3d.y + (vec3d.y - mc.player.getVelocity().y),
					mc.player.getVelocity().z + vec3d.z + (vec3d.z - mc.player.getVelocity().z));
		}
	}

}

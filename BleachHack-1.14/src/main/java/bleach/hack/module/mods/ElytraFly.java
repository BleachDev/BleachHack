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
		if(this.isToggled()) {
			if (mc.player.isElytraFlying()) {
				Vec3d vec3d = new Vec3d(0,0,getSettings().get(0).toSlider().getValue())
						.rotatePitch(-(float) Math.toRadians(mc.player.rotationPitch))
						.rotateYaw(-(float) Math.toRadians(mc.player.rotationYaw));
				
				mc.player.setMotion(
						mc.player.getMotion().x + vec3d.x + (vec3d.x - mc.player.getMotion().x),
						mc.player.getMotion().y + vec3d.y + (vec3d.y - mc.player.getMotion().y),
						mc.player.getMotion().z + vec3d.z + (vec3d.z - mc.player.getMotion().z));
				
			}
		}
	}

}

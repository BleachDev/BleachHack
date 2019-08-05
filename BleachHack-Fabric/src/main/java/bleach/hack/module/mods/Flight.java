package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.util.math.Vec3d;

public class Flight extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode(new String[] {"Normal","Static","Jetpack"}, "Mode: "),
			new SettingSlider(0, 5, 1, 1, "Speed: "));
	
	public Flight() {
		super("Flight", GLFW.GLFW_KEY_G, Category.MOVEMENT, "Allows you to fly", settings);
	}
	
	public void onDisable() {
		if(!mc.player.abilities.creativeMode) mc.player.abilities.allowFlying = false;
		mc.player.abilities.flying = false;
	}

	public void onUpdate() {
		if(!isToggled()) return;
		
		float speed = (float) getSettings().get(1).toSlider().getValue();
		
		if(getSettings().get(0).toMode().mode == 0) {
			mc.player.abilities.setFlySpeed(speed / 10);
			mc.player.abilities.allowFlying = true;
			mc.player.abilities.flying = true;
		}else if(getSettings().get(0).toMode().mode == 1) {
			mc.player.setVelocity(0, mc.player.age % 20 == 0 ? -0.06 : 0, 0);
			Vec3d forward = new Vec3d(0, 0, speed).rotateY(-(float) Math.toRadians(mc.player.yaw));
			Vec3d strafe = forward.rotateY((float) Math.toRadians(90));
			
			if(mc.options.keyJump.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(0, speed, 0));
			if(mc.options.keySneak.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(0, -speed, 0));
			if(mc.options.keyBack.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(-forward.x, 0, -forward.z));
			if(mc.options.keyForward.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(forward.x, 0, forward.z));
			if(mc.options.keyLeft.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(strafe.x, 0, strafe.z));
			if(mc.options.keyRight.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(-strafe.x, 0, -strafe.z));

		}else if(getSettings().get(0).toMode().mode == 2) {
			if(!mc.options.keyJump.isPressed()) return;
			mc.player.setVelocity(mc.player.getVelocity().x, speed / 3, mc.player.getVelocity().z);
		}
	}
}

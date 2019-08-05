package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class Freecam extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingSlider(0, 2, 0.5, 2, "Speed: "));
	
	private BoatEntity camera; /* Invisible boat used as the camera */
	private ArmorStandEntity dummy; /* Armorstand used as a dummy for the player */
	double[] playerPos;
	
	public Freecam() {
		super("Freecam", GLFW.GLFW_KEY_U, Category.PLAYER, "Its freecam, you know what it does", settings);
	}
	
	public void onEnable() {
		playerPos = new double[]{mc.player.x, mc.player.y, mc.player.z};
		
		camera = new BoatEntity(mc.world, mc.player.z, mc.player.y, mc.player.z);
		camera.copyPositionAndRotation(mc.player);
		camera.horizontalCollision = false;
		camera.verticalCollision = false;
		
		dummy = new ArmorStandEntity(mc.world, mc.player.x, mc.player.y, mc.player.z);
		dummy.copyPositionAndRotation(mc.player);
		dummy.setBoundingBox(dummy.getBoundingBox().expand(0.1));
		EntityUtils.setGlowing(dummy, Formatting.RED, "starmygithubpls");
		
		mc.world.addEntity(camera.getEntityId(), camera);
		mc.world.addEntity(dummy.getEntityId(), dummy);
		mc.cameraEntity = camera;
	}
	
	public void onDisable() {
		mc.cameraEntity = mc.player;
		camera.remove();
		dummy.remove();
	}
	
	public void onUpdate() {
		if(!isToggled()) return;
		
		mc.player.setVelocity(0, 0, 0);
		mc.player.setPosition(playerPos[0], playerPos[1], playerPos[2]);
		
		dummy.yaw = camera.yaw = mc.player.yaw;
		dummy.pitch = camera.pitch = mc.player.pitch;
		
		double speed = getSettings().get(0).toSlider().getValue();
		Vec3d forward = new Vec3d(0, 0, speed * 2.5).rotateY(-(float) Math.toRadians(camera.yaw));
		Vec3d strafe = forward.rotateY((float) Math.toRadians(90));
		Vec3d motion = Vec3d.ZERO;
		
		if(mc.options.keyJump.isPressed()) motion = motion.add(0, speed, 0);
		if(mc.options.keySneak.isPressed()) motion = motion.add(0, -speed, 0);
		if(mc.options.keyForward.isPressed()) motion = motion.add(forward.x, 0, forward.z);
		if(mc.options.keyBack.isPressed()) motion = motion.add(-forward.x, 0, -forward.z);
		if(mc.options.keyLeft.isPressed()) motion = motion.add(strafe.x, 0, strafe.z);
		if(mc.options.keyRight.isPressed()) motion = motion.add(-strafe.x, 0, -strafe.z);
		
		camera.setPosition(camera.x + motion.x, camera.y + motion.y, camera.z + motion.z);
	}

}

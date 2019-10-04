package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.event.events.EventMovementTick;
import bleach.hack.event.events.EventSendPacket;
import com.google.common.eventbus.Subscribe;
import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.PlayerCopyEntity;
import net.minecraft.server.network.packet.ClientCommandC2SPacket;
import net.minecraft.server.network.packet.ClientCommandC2SPacket.Mode;
import net.minecraft.util.math.Vec3d;

public class Freecam extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingSlider(0, 2, 0.5, 2, "Speed: "));
	
	private PlayerCopyEntity camera;
	private PlayerCopyEntity dummy;
	double[] playerPos;
	
	public Freecam() {
		super("Freecam", GLFW.GLFW_KEY_U, Category.PLAYER, "Its freecam, you know what it does", settings);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		playerPos = new double[] {mc.player.x, mc.player.y, mc.player.z};

		camera = new PlayerCopyEntity();
		camera.copyPositionAndRotation(mc.player);
		camera.horizontalCollision = false;
		camera.verticalCollision = false;

		dummy = new PlayerCopyEntity();
		dummy.copyPositionAndRotation(mc.player);
		dummy.setBoundingBox(dummy.getBoundingBox().expand(0.1));

		camera.spawn();
		dummy.spawn();
		mc.cameraEntity = camera;
	}

	@Override
	public void onDisable() {
		super.onDisable();
		mc.cameraEntity = mc.player;
		camera.despawn();
		dummy.despawn();
	}

	@Subscribe
    public void sendPacket(EventSendPacket event) {
        if (event.getPacket() instanceof ClientCommandC2SPacket) {
        	ClientCommandC2SPacket packet = (ClientCommandC2SPacket) event.getPacket();
            if (packet.getMode() == Mode.START_SNEAKING || packet.getMode() == Mode.STOP_SNEAKING) {
            	event.setCancelled(true);
            }
        }
    }
	
	@Subscribe
	public void onMovement(EventMovementTick event) {
		mc.player.setVelocity(0, 0, 0);
		camera.setVelocity(0, 0, 0);
		mc.player.setPosition(playerPos[0], playerPos[1], playerPos[2]);
		
		camera.yaw = mc.player.yaw;
		camera.headYaw = mc.player.headYaw;
		camera.pitch = mc.player.pitch;
		
		double speed = getSettings().get(0).toSlider().getValue();
		Vec3d forward = new Vec3d(0, 0, speed * 2.5).rotateY(-(float) Math.toRadians(camera.headYaw));
		Vec3d strafe = forward.rotateY((float) Math.toRadians(90));
		Vec3d motion = camera.getVelocity();
		
		if(mc.options.keyJump.isPressed()) motion = motion.add(0, speed * 1.5, 0);
		if(mc.options.keySneak.isPressed()) motion = motion.add(0, -speed * 1.5, 0);
		if(mc.options.keyForward.isPressed()) motion = motion.add(forward.x, 0, forward.z);
		if(mc.options.keyBack.isPressed()) motion = motion.add(-forward.x, 0, -forward.z);
		if(mc.options.keyLeft.isPressed()) motion = motion.add(strafe.x, 0, strafe.z);
		if(mc.options.keyRight.isPressed()) motion = motion.add(-strafe.x, 0, -strafe.z);
		mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SNEAKING));
		
		camera.setPosition(camera.x + motion.x, camera.y + motion.y, camera.z + motion.z);
		event.setCancelled(true);
	}

}

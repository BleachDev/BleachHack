package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.event.events.EventTick;
import com.google.common.eventbus.Subscribe;
import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;

public class Speed extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode(new String[] {"OnGround", "MiniHop", "Bhop"}, "Mode: "),
			new SettingSlider(0.1, 10, 2, 1, "Speed: "));
	
	private boolean jumping;
	
	public Speed() {
		super("Speed", GLFW.GLFW_KEY_V, Category.MOVEMENT, "Allows you to go faster, idk what do you expect?", settings);
	}

	@Subscribe
	public void onTick(EventTick eventTick) {
		double speeds = getSettings().get(1).toSlider().getValue() / 30;
		
		/* OnGround */
		if(getSettings().get(0).toMode().mode == 0) {
			if(mc.options.keyJump.isPressed()) return;
			
			if (jumping && mc.player.y >= mc.player.prevY + 0.399994D) {
				mc.player.setVelocity(mc.player.getVelocity().x, -0.9, mc.player.getVelocity().z);
				mc.player.y = mc.player.prevY;
				jumping = false;
			}
			
			if (mc.player.forwardSpeed != 0.0F && !mc.player.horizontalCollision) {
				if (mc.player.verticalCollision) {
					mc.player.setVelocity(mc.player.getVelocity().x * (0.85 + speeds), mc.player.getVelocity().y, mc.player.getVelocity().z * (0.85 + speeds));
					jumping = true;
					mc.player.jump();
					// 1.0379
				}
				
				if (jumping && mc.player.y >= mc.player.prevY + 0.399994D) {
					mc.player.setVelocity(mc.player.getVelocity().x, -100, mc.player.getVelocity().z);
					jumping = false;
				}

			}
			
		/* MiniHop */
		}else if(getSettings().get(0).toMode().mode == 1) {
			if(mc.player.horizontalCollision || mc.options.keyJump.isPressed() || mc.player.forwardSpeed == 0) return;
			if (mc.player.onGround) mc.player.jump();
			else if(mc.player.getVelocity().y > 0){
				mc.player.setVelocity(mc.player.getVelocity().x * (0.9 + speeds), -1, mc.player.getVelocity().z * (0.9 + speeds));
				mc.player.input.movementSideways += 1.5F;
			}
			
		/* Bhop */
		}else if(getSettings().get(0).toMode().mode == 2) {
			if (mc.player.forwardSpeed > 0 && mc.player.onGround) {
				mc.player.jump();
				mc.player.setVelocity(mc.player.getVelocity().x * (0.65 + speeds), 0.255556, mc.player.getVelocity().z * (0.65 + speeds));
				mc.player.sidewaysSpeed += 3.0F;
				mc.player.jump();
				mc.player.setSprinting(true);
			}
		}
	}
}

package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

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
	
	public void onUpdate() {
		if(this.isToggled()) {
			double speeds = getSettings().get(1).toSlider().getValue() / 30;
			
			/* OnGround */
			if(getSettings().get(0).toMode().mode == 0) {
				if(mc.gameSettings.keyBindJump.isKeyDown()) return;
				
				if (jumping && mc.player.posY >= mc.player.prevPosY + 0.399994D) {
					mc.player.setMotion(mc.player.getMotion().x, -0.9, mc.player.getMotion().z);
					mc.player.posY = mc.player.prevPosY;
					jumping = false;
				}
				
				if (mc.player.moveForward != 0.0F && !mc.player.collidedHorizontally) {
					if (mc.player.collidedVertically) {
						mc.player.setMotion(mc.player.getMotion().x * (0.9 + speeds), mc.player.getMotion().y, mc.player.getMotion().z * (0.9 + speeds));
						jumping = true;
						mc.player.jump();
						// 1.0379
					}
					
					if (jumping && mc.player.posY >= mc.player.prevPosY + 0.399994D) {
						mc.player.setMotion(mc.player.getMotion().x, -100, mc.player.getMotion().z);
						jumping = false;
					}
	
				}
			/* MiniHop */
			}else if(getSettings().get(0).toMode().mode == 1) {
				if(mc.player.collidedHorizontally || mc.gameSettings.keyBindJump.isKeyDown() || mc.player.moveForward == 0) return;
				if (mc.player.onGround) mc.player.jump();
				else if(mc.player.getMotion().y > 0){
					mc.player.setMotion(mc.player.getMotion().x * (0.9 + speeds), -1, mc.player.getMotion().z * (0.9 + speeds));
					mc.player.moveStrafing += 1.5F;
				}
			/* Bhop */
			}else if(getSettings().get(0).toMode().mode == 2) {
				if (mc.player.moveForward > 0 && mc.player.onGround) {
					mc.player.jump();
					mc.player.setMotion(mc.player.getMotion().x * (0.65 + speeds), 0.255556, mc.player.getMotion().z * (0.65 + speeds));
					mc.player.moveStrafing += 3.0F;
					mc.player.jump();
					mc.player.setSprinting(true);
				}
			}
		}
	}
}

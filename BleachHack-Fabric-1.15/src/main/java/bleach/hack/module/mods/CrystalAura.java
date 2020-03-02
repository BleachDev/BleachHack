package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import com.google.common.eventbus.Subscribe;
import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EnderCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;

public class CrystalAura extends Module {
	
	private int delay = 0;
	
	public CrystalAura() {
		super("CrystalAura", GLFW.GLFW_KEY_I, Category.COMBAT, "Automatically attacks crystals for you.",
				new SettingToggle("Aimbot", false),
				new SettingToggle("Thru Walls", false),
				new SettingSlider("Range: ", 0, 6, 4.25, 2),
				new SettingSlider("CPS: ", 0, 20, 16, 0));
	}

	@Subscribe
	public void onTick(EventTick event) {
		delay++;
		int reqDelay = (int) Math.round(20/getSettings().get(3).toSlider().getValue());
		
		for(Entity e: mc.world.getEntities()) {
			if(e instanceof EnderCrystalEntity && mc.player.distanceTo(e) < getSettings().get(2).toSlider().getValue()) {
				if(!mc.player.canSee(e) && !getSettings().get(1).toToggle().state) continue;
				if(getSettings().get(0).toToggle().state) EntityUtils.facePos(e.getX(), e.getY() + e.getHeight()/2, e.getZ());
				
				if(delay > reqDelay || reqDelay == 0) {
					mc.player.networkHandler.sendPacket(new PlayerInteractEntityC2SPacket(e));
					mc.player.attack(e);
					mc.player.swingHand(Hand.MAIN_HAND);
					delay=0;
				}
			}
		}
	}

}

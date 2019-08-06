package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EnderCrystalEntity;
import net.minecraft.server.network.packet.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;

public class CrystalAura extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(false, "Aimbot"),
			new SettingToggle(false, "Thru Walls"),
			new SettingSlider(0, 6, 4.25, 2, "Range: "),
			new SettingSlider(0, 20, 16, 0, "CPS: "));
	
	private int delay = 0;
	
	public CrystalAura() {
		super("CrystalAura", GLFW.GLFW_KEY_I, Category.COMBAT, "Automatically attacks crystals for you.", settings);
	}
	
	public void onUpdate() {
		delay++;
		int reqDelay = (int) Math.round(20/getSettings().get(3).toSlider().getValue());
		
		for(Entity e: mc.world.getEntities()) {
			if(e instanceof EnderCrystalEntity && mc.player.distanceTo(e) < getSettings().get(2).toSlider().getValue()) {
				if(!mc.player.canSee(e) && !getSettings().get(1).toToggle().state) continue;
				if(getSettings().get(0).toToggle().state) EntityUtils.facePos(e.x, e.y + e.getHeight()/2, e.z);
				
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

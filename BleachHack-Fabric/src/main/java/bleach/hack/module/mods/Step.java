package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.WorldUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;

public class Step extends Module {
	
	private boolean flag;
	private double pos;
	
	public Step() {
		super("Step", -1, Category.MOVEMENT, "Allows you to Run up blocks like stairs.",
				new SettingMode("Mode: ", "Simple", "Spider", "Jump"));
	}

	@Override
	public void onDisable() {
		super.onDisable();
		mc.player.stepHeight = 0.5F;
	}

	@Subscribe
	public void onTick(EventTick event) {
		if(!WorldUtils.NONSOLID_BLOCKS.contains(
				mc.world.getBlockState(mc.player.getBlockPos().add(0, mc.player.getHeight()+1, 0)).getBlock())) return;
		
		if(getSettings().get(0).toMode().mode == 0) {
			mc.player.stepHeight = 1.065F;
		}else if(getSettings().get(0).toMode().mode == 1) {
			
			if(!mc.player.horizontalCollision && flag) {
				mc.player.setVelocity(mc.player.getVelocity().x, -0.1, mc.player.getVelocity().z);
			}else if(mc.player.horizontalCollision) {
				mc.player.setVelocity(mc.player.getVelocity().x, 1, mc.player.getVelocity().z);
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket(true));
				mc.player.jump();
				flag = true;
			}
			
			if(!mc.player.horizontalCollision) flag = false;
			
		}else if(getSettings().get(0).toMode().mode == 2) {
			
			if(mc.player.horizontalCollision && mc.player.onGround) {
				pos = mc.player.getY();
				mc.player.jump();
				flag = true;
			}
			
			if(flag && pos + 1.065 < mc.player.getY()) {
				mc.player.setVelocity(mc.player.getVelocity().x, -0.1, mc.player.getVelocity().z);
				flag = false;
			}
		}
	}
}

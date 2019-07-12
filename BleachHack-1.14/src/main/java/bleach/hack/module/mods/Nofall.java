package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.network.play.client.CPlayerPacket;

public class Nofall extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode(new String[] {"Simple", "Packet"}, "Mode: "));
			
	public Nofall() {
		super("Nofall", -1, Category.PLAYER, "Prevents you from taking fall damage.", settings);
	}
	
	public void onUpdate() {
		if(this.isToggled()) {
			if(mc.player.fallDistance > 2f && getSettings().get(0).toMode().mode == 0) {
				mc.player.connection.sendPacket(new CPlayerPacket(true));
			}
			
			if(mc.player.fallDistance > 2f && getSettings().get(0).toMode().mode == 1 &&
					mc.world.getBlockState(mc.player.getPosition().add(
							0,-1.5+(mc.player.getMotion().y*0.1),0)).getBlock() != Blocks.AIR) {
				mc.player.connection.sendPacket(new CPlayerPacket(false));
				mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(
						mc.player.posX, mc.player.posY - 420.69, mc.player.posZ, true));
				mc.player.fallDistance = 0;
			}
		}
	}

}

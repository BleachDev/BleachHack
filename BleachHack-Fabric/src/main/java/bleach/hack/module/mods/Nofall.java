package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;

public class Nofall extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode("Mode: ", "Simple", "Packet"));
			
	public Nofall() {
		super("Nofall", -1, Category.PLAYER, "Prevents you from taking fall damage.", settings);
	}

	@Subscribe
	public void onTick(EventTick event) {
		if(mc.player.fallDistance > 2f && getSettings().get(0).toMode().mode == 0) {
			if(mc.player.isFallFlying()) return;
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket(true));
		}
		
		if(mc.player.fallDistance > 2f && getSettings().get(0).toMode().mode == 1 &&
				mc.world.getBlockState(mc.player.getBlockPos().add(
						0,-1.5+(mc.player.getVelocity().y*0.1),0)).getBlock() != Blocks.AIR) {
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket(false));
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(
					mc.player.x, mc.player.y - 420.69, mc.player.z, true));
			mc.player.fallDistance = 0;
		}
	}

}

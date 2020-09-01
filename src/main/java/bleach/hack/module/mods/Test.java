package bleach.hack.module.mods;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.BleachLogger;
import com.google.common.eventbus.Subscribe;

public class Test extends Module {

    public Test() {
        super("Test", KEY_UNBOUND, Category.WORLD, "dumps variables into console");
    }

    @Subscribe
    public void onSendPacket(EventSendPacket event) {
        System.out.println("[BH] TEST: " + event.getPacket().toString());
        BleachLogger.infoMessage("TEST: " + event.getPacket().toString());
    }

}

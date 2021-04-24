package bleach.hack.epearledition.module.mods;

import bleach.hack.epearledition.event.events.EventSendPacket;
import bleach.hack.epearledition.module.Category;
import bleach.hack.epearledition.module.Module;
import bleach.hack.epearledition.utils.BleachLogger;
import com.google.common.eventbus.Subscribe;

public class Test extends Module {

    public Test() {
        super("Test", KEY_UNBOUND, Category.CHAT, "dumps variables into console");
    }

    @Subscribe
    public void onSendPacket(EventSendPacket event) {
        System.out.println("[BH] TEST: " + event.getPacket().toString());
        BleachLogger.infoMessage("TEST: " + event.getPacket().toString());
    }

}

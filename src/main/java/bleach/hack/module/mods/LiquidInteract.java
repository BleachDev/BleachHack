package bleach.hack.module.mods;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.BleachLogger;
import com.google.common.eventbus.Subscribe;

public class LiquidInteract extends Module {

    public LiquidInteract() {
        super("LiquidInteract", KEY_UNBOUND, Category.PLAYER, "Allows you to interact with liquids.");
    }


}

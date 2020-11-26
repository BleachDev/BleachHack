package bleach.hack.module.mods;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

public class FabritoneFix extends Module {

    public FabritoneFix() {
        super("FabritoneFix", KEY_UNBOUND, Category.MISC, "fixes stuff in Fabritone (WIP!!)");
    }

    @Subscribe
    public void onPacketSend(EventSendPacket event) {
        if (event.getPacket() instanceof ChatMessageC2SPacket) {
            String text = ((ChatMessageC2SPacket) event.getPacket()).getChatMessage();
            if (text.equals("#come") && ModuleManager.getModule(Freecam.class).isToggled() || text.equals("@come") && ModuleManager.getModule(Freecam.class).isToggled()) {
                ModuleManager.getModule(Freecam.class).toggle();
            }
        }
    }
}

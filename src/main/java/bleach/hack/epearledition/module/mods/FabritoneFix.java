package bleach.hack.epearledition.module.mods;

import bleach.hack.epearledition.event.events.EventSendPacket;
import bleach.hack.epearledition.module.Category;
import bleach.hack.epearledition.module.Module;
import bleach.hack.epearledition.module.ModuleManager;
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

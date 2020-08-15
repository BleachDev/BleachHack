package bleach.hack.module.mods;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

public class ColorSigns extends Module {

    public ColorSigns() {
        super("ColorSigns", KEY_UNBOUND, Category.EXPLOITS, "Allows you to use colors on signs on NON-PAPER servers (use \"&\" for color symbols)");
    }

    /*
     * This works because the code to strip invalid characters from signs is flawed because it uses a replaceAll for all the
     * formatting codes instead of matching all sections symbols, which means you can basically "stack" two formatting codes ontop of eachother
     * like "&&66", it will when search and find the middle one and remove it to leave "&6" left which is still a valid formatting code.
     * Paper has a patch for it to correct it so it doesn't work there
     */
    @Subscribe
    public void onPacketSend(EventSendPacket event) {
        if (event.getPacket() instanceof UpdateSignC2SPacket) {
            UpdateSignC2SPacket p = (UpdateSignC2SPacket) event.getPacket();

            for (int l = 0; l < p.getText().length; l++) {
                String newText = p.getText()[l].replaceAll("(?i)\u00a7|&([0-9A-FK-OR])", "\u00a7\u00a7$1$1");
                p.getText()[l] = newText;
            }
        }
    }
}

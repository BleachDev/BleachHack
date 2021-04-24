package bleach.hack.epearledition.module.mods;

import bleach.hack.epearledition.event.events.EventSendPacket;
import bleach.hack.epearledition.module.Category;
import bleach.hack.epearledition.module.Module;
import bleach.hack.epearledition.setting.base.SettingToggle;
import bleach.hack.epearledition.utils.BleachLogger;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;

public class PacketCanceller extends Module {

    public PacketCanceller() {
        super("Anti2bKick", KEY_UNBOUND, Category.MISC, "cancels spammy packets",
                new SettingToggle("Debug", false));
    }

    @Subscribe
    public void onSendPacket(EventSendPacket event) {
        if (getSetting(0).asToggle().state) {
            BleachLogger.infoMessage("PACKET: " + event.getPacket().toString());
        }
        if (event.getPacket() instanceof CommandSuggestionsS2CPacket || event.getPacket() instanceof RequestCommandCompletionsC2SPacket) {
            if (getSetting(0).asToggle().state) {
                BleachLogger.infoMessage("CANCELLED PACKET: " + event.getPacket().toString());
            }
            event.setCancelled(true);
        }
    }

}

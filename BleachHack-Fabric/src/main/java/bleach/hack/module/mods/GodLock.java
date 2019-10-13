package bleach.hack.module.mods;

import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.mixin.MixinPlayerEntity;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.network.packet.HealthUpdateS2CPacket;

public class GodLock extends Module {

    public GodLock() {
        super("Vanish", -1, Category.PLAYER, "Prevents you from taking damage.\nVanilla Vanish.", null);
    }

    @Subscribe
    public void readPacket(EventReadPacket event) {
        if (event.getPacket() instanceof HealthUpdateS2CPacket) {
            event.setCancelled(true);
        }
    }

}

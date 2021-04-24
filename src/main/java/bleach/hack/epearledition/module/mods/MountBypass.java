package bleach.hack.epearledition.module.mods;

import bleach.hack.epearledition.event.events.EventSendPacket;
import bleach.hack.epearledition.module.Category;
import bleach.hack.epearledition.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.InteractionType;

public class MountBypass extends Module {

    public boolean dontCancel = false;

    public MountBypass() {
        super("MountBypass", KEY_UNBOUND, Category.EXPLOITS, "Bypasses illegalstack on non bungeecord servers");
    }

    @Subscribe
    public void onPacket(EventSendPacket event) {
        if (dontCancel) return;

        if (event.getPacket() instanceof PlayerInteractEntityC2SPacket
                && ((PlayerInteractEntityC2SPacket) event.getPacket()).getType() == InteractionType.INTERACT_AT
                && ((PlayerInteractEntityC2SPacket) event.getPacket()).getEntity(mc.world) instanceof AbstractDonkeyEntity) {
            event.setCancelled(true);
        }
    }
}

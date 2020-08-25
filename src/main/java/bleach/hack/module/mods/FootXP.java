/*package bleach.hack.module.mods;

import bleach.hack.event.events.EventBruhPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;

public class FootXp extends Module
{
    public FootXp() {
         public FootXp() {
            super("FootXP", KEY_UNBOUND, Category.MOVEMENT, "Automatically points xp at feet");
        }
    }

    @Subscribe
    public void onTick(final PacketEvent.Send event)
        if (event.getPacket() instanceof CPacketPlayer && FootXp.mc.player.getHeldItemMainhand().getItem() instanceof ItemExpBottle) {
            final CPacketPlayer packet = (CPacketPlayer)event.getPacket();
            ((ICPacketPlayer)packet).setPitch(90.0f);
        }
    }
}*/
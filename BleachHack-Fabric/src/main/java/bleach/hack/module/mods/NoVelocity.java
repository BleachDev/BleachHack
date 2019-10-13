package bleach.hack.module.mods;

import bleach.hack.event.events.EventReadPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.FabricReflect;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.network.packet.EntityVelocityUpdateS2CPacket;

/**
 * @author sl
 * First Module utilizing EventBus!
 */

public class NoVelocity extends Module {
    public NoVelocity() {
        super("NoVelocity", -1, Category.PLAYER, "If you take some damage, you don't move. Maybe.", null);
    }

    //The name of the method doesn't matter nor does it need to be consistent between modules, what matters is the argument.
    @Subscribe
    public void readPacket(EventReadPacket event) {
        if(event.getPacket() instanceof EntityVelocityUpdateS2CPacket) {
            EntityVelocityUpdateS2CPacket packet = (EntityVelocityUpdateS2CPacket) event.getPacket();
            if(packet.getId() == mc.player.getEntityId()) {
                FabricReflect.writeField(packet, 0,"field_12563", "velocityX");
                FabricReflect.writeField(packet, 0, "field_12562","velocityY");
                FabricReflect.writeField(packet, 0, "field_12561", "velocityZ");
            }
        }
    }
}

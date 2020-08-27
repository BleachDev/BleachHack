package bleach.hack.module.mods;

import bleach.hack.event.events.EventReadPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.BleachLogger;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;

public class AutoExplode extends Module {

    public AutoExplode() {
        super("AutoExplode", KEY_UNBOUND, Category.PLAYER, "Automatically explode respawn anchors and beds");
    }

    @Subscribe
    public void readPacket(EventReadPacket event) {
        if (event.getPacket() instanceof BlockUpdateS2CPacket) {
            if (((BlockUpdateS2CPacket) event.getPacket()).getState().toString().equals("Block{minecraft:respawn_anchor}[charges=0]")) {
                System.out.println("[BH] TEST: Respawn anchor placed at " + ((BlockUpdateS2CPacket) event.getPacket()).getPos().toString());
            }
        }
    }
}

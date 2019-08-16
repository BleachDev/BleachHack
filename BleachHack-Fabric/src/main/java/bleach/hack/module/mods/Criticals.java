package bleach.hack.module.mods;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.server.network.packet.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;

/**
 * @author sl
 */
public class Criticals extends Module {

    public Criticals() {
        super("Criticals", -1, Category.COMBAT, "Attempts to force Critical hits on entities you hit.", null);
    }

    @Subscribe
    public void sendPacket(EventSendPacket eventSendPacket) {
        if (eventSendPacket.getPacket() instanceof PlayerInteractEntityC2SPacket) {
            PlayerInteractEntityC2SPacket packet = (PlayerInteractEntityC2SPacket) eventSendPacket.getPacket();
            if (packet.getType() == PlayerInteractEntityC2SPacket.InteractionType.ATTACK) {
                this.doCritical();
            }
        }
    }

    private void doCritical() {
        if(!this.mc.player.onGround) return;
        if(this.mc.player.isInLava() || this.mc.player.isInWater()) return;
        double posX = this.mc.player.x;
        double posY = this.mc.player.y;
        double posZ = this.mc.player.z;
        this.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(posX, posY + 0.0625D, posZ, true));
        this.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(posX, posY, posZ, false));
        this.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(posX, posY + 1.1E-5D, posZ, false));
        this.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(posX, posY, posZ, false));
    }
}

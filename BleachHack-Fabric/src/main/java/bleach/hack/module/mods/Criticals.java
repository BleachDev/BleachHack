package bleach.hack.module.mods;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;

import java.util.Random;

import com.google.common.eventbus.Subscribe;

import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
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
    public void sendPacket(EventSendPacket event) {
        if (event.getPacket() instanceof PlayerInteractEntityC2SPacket) {
            PlayerInteractEntityC2SPacket packet = (PlayerInteractEntityC2SPacket) event.getPacket();
            if (packet.getType() == PlayerInteractEntityC2SPacket.InteractionType.ATTACK) {
            	this.doCritical();
            	
            	/* Lets fake some extra paricles to make the player feel good */
            	Entity e = packet.getEntity(mc.world);
            	Random r = new Random();
                for(int i = 0; i < 10; i++) {
                	mc.particleManager.addParticle(ParticleTypes.CRIT, e.x, e.y + e.getHeight() / 2, e.z,
                			r.nextDouble() - 0.5, r.nextDouble() - 0.5, r.nextDouble() - 0.5);
                }
            }
        }
    }

    private void doCritical() {
        if(!mc.player.onGround) return;
        if(mc.player.isInLava() || mc.player.isInWater()) return;
        double posX = mc.player.x;
        double posY = mc.player.y;
        double posZ = mc.player.z;
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(posX, posY + 0.0625, posZ, true));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(posX, posY, posZ, false));
    }
}

package bleach.hack.module.mods;

import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.BleachLogger;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.HashMap;

public class PopCounter extends Module {

    private HashMap<String, Integer> pops = new HashMap<>();

    public PopCounter() {
        super("PopCounter", KEY_UNBOUND, Category.COMBAT, "Counts totem pops");
    }




    public void
    onDisable()
    {
        super.onDisable();
        pops.clear();
    }

    @Subscribe
    public void
    onReadPacket(EventReadPacket event)
    {
        if(event.getPacket() instanceof EntityStatusS2CPacket)
        {
            EntityStatusS2CPacket pack = (EntityStatusS2CPacket) event.getPacket();

            if(pack.getStatus() == 35)
            {
                handlePop(pack.getEntity(mc.world));
            }
        }
    }

    @Subscribe
    public void
    onTick(EventTick tick)
    {
        if(mc.world == null)
            return;

        mc.world.getPlayers().forEach(player -> {
            if(player.getHealth() <= 0)
            {
                if(pops.containsKey(player.getEntityName()))
                {
                    BleachLogger.infoMessage(player.getEntityName() + " died after popping " + pops.get(player.getEntityName()) + " totems");
                    pops.remove(player.getEntityName(), pops.get(player.getEntityName()));
                }
            }
        });
    }

    private void
    handlePop(Entity entity)
    {
        if(pops == null)
            pops = new HashMap<>();

        if(entity == mc.player)
            return;

        if(pops.get(entity.getEntityName()) == null)
        {
            pops.put(entity.getEntityName(), 1);
            BleachLogger.infoMessage(entity.getEntityName() + " popped 1 totem");
        }
        else if(!(pops.get(entity.getEntityName()) == null))
        {
            int popc = pops.get(entity.getEntityName());
            popc += 1;
            pops.put(entity.getEntityName(), popc);
            BleachLogger.infoMessage(entity.getEntityName() + " popped " + popc + " totems");
        }
    }

}

package bleach.hack.module.mods;

import bleach.hack.BleachHack;
import bleach.hack.event.events.PacketEvent;
import bleach.hack.event.events.TotemPopEvent;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.HashMap;

public class PopCounter extends Module
{
    public PopCounter()
    {
        super("PopCounter", KEY_UNBOUND, Category.COMBAT, "Counts how many times a person has popped");
    }

    private HashMap<String, Integer> popList = new HashMap<>();

    @EventHandler
    public Listener<TotemPopEvent> totemPopEvent = new Listener<>(event ->
    {
        if(popList == null)
        {
            popList = new HashMap<>();
        }


        if(popList.get(event.getEntity().getEntityName()) == null)
        {
            popList.put(event.getEntity().getEntityName(), 1);
            mc.inGameHud.getChatHud().addMessage(new LiteralText(Formatting.AQUA + event.getEntity().getEntityName() + Formatting.AQUA + 1 + Formatting.BLUE + "totem(s)"));
        } else if(!(popList.get(event.getEntity().getEntityName()) == null))

        {
            int popCounter = popList.get(event.getEntity().getEntityName());
            int newPopCounter = popCounter += 1;
            popList.put(event.getEntity().getEntityName(), newPopCounter);
            mc.inGameHud.getChatHud().addMessage(new LiteralText(Formatting.AQUA + event.getEntity().getEntityName() + Formatting.BLUE + " popped " + Formatting.AQUA + newPopCounter + Formatting.BLUE + " totems kek"));
        }
    });

    public void onUpdate()
    {
        if(mc.world == null)
        {
            return;
        }

        for(PlayerEntity player : mc.world.getPlayers())
        {
            if(player.getHealth() <= 0)
            {
                if(popList.containsKey(player.getEntityName()))
                {
                    mc.inGameHud.getChatHud().addMessage(new LiteralText(Formatting.AQUA + player.getEntityName() + Formatting.BLUE + " died after popping " + Formatting.AQUA + popList.get(player.getName()) + Formatting.BLUE + " totems Ezzzzz"));
                    popList.remove(player.getEntityName(), popList.get(player.getEntityName()));
                }
            }
        }
    }
    @EventHandler
    public Listener<PacketEvent.Receive> totemPop = new Listener<>(p_Event ->
    {
        if(mc.player == null || mc.world == null)
        {
            return;
        }
        if(p_Event.getPacket() instanceof EntityStatusS2CPacket)
        {
            EntityStatusS2CPacket packet = (EntityStatusS2CPacket) p_Event.getPacket();
            if(packet.getStatus() == 35)
            {
                Entity entity = packet.getEntity(mc.world);
                BleachHack.eventBus.post(new TotemPopEvent(entity));

            }
        }

    });
}

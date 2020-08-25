/*package bleach.hack.module.mods;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import me.zero.alpine.listener.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;

import java.util.HashMap;

public class TotemPopCounter extends Module
{

    public TotemPopCounter() {
        super("TotemPopCounter", KEY_UNBOUND, Category.COMBAT, "Counts how many times people within render distane have popped");
    }
    private HashMap<String, Integer> TotemPopContainer = new HashMap<String, Integer>();

    public static HashMap<String, Integer> popList = new HashMap<>();

    @Subscribe
    public void onUpdate(UpdateEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }
        for(PlayerEntity player : mc.world.getPlayers()) {
            if(player.getHealth() <= 0) {
                if(popList.containsKey(player.getName())) {
                    Logger.printMessage(ChatFormatting.DARK_AQUA + player.getName() + ChatFormatting.DARK_RED + " died after popping " + ChatFormatting.GOLD +  popList.get(player.getName()) + " totems!",true);
                    popList.remove(player.getName(), popList.get(player.getName()));
                }
            }
        }
    }

    @Subscribe
    public void onPacket(PacketEvent event) {
        if (mc.world == null || mc.player == null || event.getType() == EventType.PRE) {
            return;
        }
        if (event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet = (SPacketEntityStatus) event.getPacket();
            if (packet.getOpCode() == 35) {
                Entity entity = packet.getEntity(mc.world);
                if(popList == null) {
                    popList = new HashMap<>();
                }
                if(popList.get(entity.getName()) == null) {
                    popList.put(entity.getName(), 1);
                    Logger.printMessage(ChatFormatting.DARK_AQUA + entity.getName() + ChatFormatting.DARK_RED + " popped " + ChatFormatting.GOLD + "1 totem!",true);
                } else if(!(popList.get(entity.getName()) == null)) {
                    int popCounter = popList.get(entity.getName());
                    int newPopCounter = popCounter += 1;
                    popList.put(entity.getName(), newPopCounter);
                    Logger.printMessage(ChatFormatting.DARK_AQUA + entity.getName() + ChatFormatting.DARK_RED + " popped " + ChatFormatting.GOLD +newPopCounter + " totems!",true);
                }
            }
        }
    }
}
}*/



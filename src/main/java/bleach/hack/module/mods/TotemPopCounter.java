/*package bleach.hack.module.mods;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import com.google.common.eventbus.Subscribe;
import me.zero.alpine.listener.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import bleach.hack.event.events.TotemPopEvent;
import bleach.hack.event.events.PacketEvent
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.text.LiteralText;
//SKIDDED FROM:
//https://github.com/Fentanull/YakGod.cc/blob/ba455af37e62b47861a968a92f359c13897490b9/src/main/java/me/zeroeightsix/kami/module/modules/combat/TotemPopCounter.java

import java.util.HashMap;

public class TotemPopCounter extends Module
{

    public TotemPopCounter() {
        super("TotemPopCounter", KEY_UNBOUND, Category.COMBAT, "Counts how many times people within render distane have popped"
    }
    private HashMap<String, Integer> TotemPopContainer = new HashMap<String, Integer>();

    public static HashMap<String, Integer> popList = new HashMap<>();

    private HashMap<String, Integer> popList = new HashMap();

    @EventHandler
    public Listener<TotemPopEvent> totemPopEvent = new Listener<>(event -> {
        if(popList == null) {
            popList = new HashMap<>();
        }


        if(popList.get(event.getEntity().getName()) == null) {
            popList.put(event.getEntity().getName(), 1);
            mc.inGameHud.getChatHud().addMessage(new LiteralText(event.getEntity().getName() + " &9popped " + 1 + " &9totem"));
        } else if(!(popList.get(event.getEntity().getName()) == null)) {
            int popCounter = popList.get(event.getEntity().getName());
            int newPopCounter = popCounter += 1;
            popList.put(event.getEntity().getName(), newPopCounter);
            mc.inGameHud.getChatHud().addMessage(new LiteralText(event.getEntity().getName() + " &9popped " + newPopCounter + " &9totems kek"));
        }

    });

    @Override
    public void onUpdate() {
        for(PlayerEntity player : mc.world.getPlayers()) {
            if(player.getHealth() <= 0) {
                if(popList.containsKey(player.getName())) {
                    mc.inGameHud.getChatHud().addMessage(new LiteralText(player.getName() + " &9died after popping " + popList.get(player.getName()) + " &9totems Ezzzzz"));
                    popList.remove(player.getName(), popList.get(player.getName()));
                }
            }
        }
    }

    @EventHandler
    public Listener<PacketEvent.Receive> totemPopListener = new Listener<>(event -> {

        if (mc.world == null || mc.player == null) {
            return;
        }

        if (event.getPacket() instanceof EntityStatusS2CPacket) {
            EntityStatusS2CPacket packet = (EntityStatusS2CPacket) event.getPacket();
            if (packet.getStatus(totemPopEvent){
                Entity entity = packet.getEntity(mc.world);
                BleachHack.eventBus.post(new TotemPopEvent(entity));
            }
        }

    });


}*/



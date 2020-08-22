package bleach.hack.module.mods;

import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.mixin.IMinecraftClient;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import me.zero.alpine.listener.EventHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;

import java.util.LinkedList;
import java.util.Queue;

public class Blink extends Module {

    public Blink() {
        super("Blink", KEY_UNBOUND, Category.PLAYER, "Teleport around like a gamer");
    }
    Queue<PlayerMoveC2SPacket> packets = new LinkedList<>();
    @Subscribe
    public void onSendPacket(EventSendPacket event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            event.setCancelled(true);
            packets.add((PlayerMoveC2SPacket) event.getPacket());
        }
    }
    private OtherClientPlayerEntity clonedPlayer;

    @Override
    public void onEnable() {
        if (mc.player != null) {
            clonedPlayer = new OtherClientPlayerEntity(mc.world, mc.getSession().getProfile());
            clonedPlayer.copyFrom(mc.player);
            clonedPlayer.headYaw = mc.player.headYaw;
            mc.world.addEntity(-100, clonedPlayer);
        }
    }

    @Override
    public void onDisable() {
        while (!packets.isEmpty())
            mc.getNetworkHandler().sendPacket(packets.poll());

        PlayerEntity localPlayer = mc.player;
        if (localPlayer != null) {
            mc.world.removeEntity(-100);
            clonedPlayer = null;
        }
    }
}

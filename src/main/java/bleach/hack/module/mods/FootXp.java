package bleach.hack.module.mods;

import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.WorldUtils;
import com.google.common.eventbus.Subscribe;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.text.LiteralText;

public class FootXp extends Module
{
    public FootXp() {
        super("FootXP", KEY_UNBOUND, Category.MOVEMENT, "Automatically points xp at feet");
    }

    @Subscribe
    public void sendPacket(EventSendPacket event) {
        if (mc.world == null || mc.player == null) return;
        if (event.getPacket() instanceof PlayerInteractItemC2SPacket && mc.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE) {
            WorldUtils.facePosPacket(mc.player.getX(), mc.player.getY() - 2, mc.player.getZ());
        }
    }
}
package bleach.hack.module.mods;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;

public class FootXp extends Module
{
    public FootXp() {
        super("FootXP", KEY_UNBOUND, Category.MOVEMENT, "Automatically points xp at feet");
    }

    @Subscribe
    public void onTick(final EventSendPacket event)
    {
        if(mc.world == null || mc.player == null){
            return;
        }
        if(event.getPacket() instanceof PlayerActionC2SPacket && mc.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE){
            final PlayerActionC2SPacket packet = (PlayerActionC2SPacket)event.getPacket();
            mc.player.setYaw(90);
        }
    }
}
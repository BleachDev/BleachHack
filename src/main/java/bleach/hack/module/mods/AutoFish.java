package bleach.hack.module.mods;

import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.mixin.IMinecraftClient;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;

public class AutoFish extends Module {

    public static int ticksPassed;
    public static boolean pullFish;

    public AutoFish() {
        super("AutoFish", KEY_UNBOUND, Category.PLAYER, "Automatically reels in fishes, just start fishing and it will handle the rest",
                new SettingSlider("Recast Delay", 1, 10, 3, 0));
    }

    @Subscribe
    public void onReceivePacket(EventReadPacket event) {
        if (
                mc.player != null
                && (mc.player.getMainHandStack().getItem() == Items.FISHING_ROD || mc.player.getOffHandStack().getItem() == Items.FISHING_ROD)
                && event.getPacket() instanceof PlaySoundS2CPacket
                && SoundEvents.ENTITY_FISHING_BOBBER_SPLASH.equals(((PlaySoundS2CPacket) event.getPacket()).getSound())
        ) {
            pullFish = true;
            ticksPassed = (int) getSetting(0).asSlider().getValue() * 20;
        }
    }
    @Subscribe
    public void onTick(EventTick event) {
        assert mc.player != null;
        if (ticksPassed != 0) {
            ticksPassed--;
        }
        if (pullFish && ticksPassed == (int) getSetting(0).asSlider().getValue() * 20 - 5) {
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND));
            pullFish = false;
        }
        if (ticksPassed == 1) {
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND));
            ticksPassed = 0;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        pullFish = false;
        ticksPassed = 0;
    }
}
